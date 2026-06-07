from __future__ import annotations

import re
from collections import defaultdict
from typing import Any, Dict, List, Optional

import chromadb
from rank_bm25 import BM25Okapi
from sentence_transformers import SentenceTransformer

from . import config


def tokenize_ko(text: str) -> List[str]:
    """한글/영문/숫자 단위로 자르는 단순 토크나이저."""
    tokens = re.findall(r"[가-힣]+|[A-Za-z]+|\d+", text)
    return [token for token in tokens if len(token) >= 2]


class Retriever:
    def __init__(self) -> None:
        print(f"[RAG] 임베딩 모델 로드: {config.EMBED_MODEL}")
        self.model = SentenceTransformer(config.EMBED_MODEL, device=config.EMBED_DEVICE)

        self.client = chromadb.PersistentClient(path=str(config.CHROMA_DIR))
        self.collection = self.client.get_collection(config.COLLECTION_NAME)

        data = self.collection.get(include=["documents", "metadatas"])
        self.all_ids: List[str] = data["ids"]
        self.all_docs: List[str] = data["documents"]
        self.all_metas: List[Dict[str, Any]] = data["metadatas"]

        tokenized_corpus = [tokenize_ko(doc) for doc in self.all_docs]
        self.bm25 = BM25Okapi(tokenized_corpus)
        self.id_to_index = {cid: idx for idx, cid in enumerate(self.all_ids)}

        print(f"[RAG] 인덱스 로드 완료 (청크 {len(self.all_ids)}개)")

    def search(
        self,
        query: str,
        top_k: int = config.TOP_K_FINAL,
        chunk_type_filter: Optional[List[str]] = None,
        court_filter: Optional[str] = None,
    ) -> List[Dict[str, Any]]:
        where: Optional[Dict[str, Any]] = None
        clauses: List[Dict[str, Any]] = []
        if chunk_type_filter:
            clauses.append({"chunk_type": {"$in": chunk_type_filter}})
        if court_filter:
            clauses.append({"court": court_filter})
        if len(clauses) == 1:
            where = clauses[0]
        elif len(clauses) > 1:
            where = {"$and": clauses}

        # Dense (벡터) 검색
        query_embedding = self.model.encode(
            [query], normalize_embeddings=True, convert_to_numpy=True
        ).tolist()
        dense = self.collection.query(
            query_embeddings=query_embedding,
            n_results=config.TOP_K_DENSE,
            where=where,
            include=["documents", "metadatas", "distances"],
        )
        dense_ids = dense["ids"][0]
        dense_distances = dense["distances"][0]
        dense_scores = {
            cid: 1.0 - dist for cid, dist in zip(dense_ids, dense_distances)
        }

        # BM25 (필터 적용)
        tokenized_query = tokenize_ko(query)
        bm25_raw = self.bm25.get_scores(tokenized_query)
        ranked_indices = sorted(
            range(len(bm25_raw)), key=lambda i: bm25_raw[i], reverse=True
        )
        bm25_top: Dict[str, float] = {}
        for idx in ranked_indices:
            cid = self.all_ids[idx]
            meta = self.all_metas[idx]
            if chunk_type_filter and meta.get("chunk_type") not in chunk_type_filter:
                continue
            if court_filter and meta.get("court") != court_filter:
                continue
            bm25_top[cid] = float(bm25_raw[idx])
            if len(bm25_top) >= config.TOP_K_BM25:
                break

        # RRF (Reciprocal Rank Fusion)로 결합
        K = 60
        rrf_scores: Dict[str, float] = defaultdict(float)
        for rank, cid in enumerate(
            sorted(dense_scores, key=lambda c: dense_scores[c], reverse=True)
        ):
            rrf_scores[cid] += 1.0 / (K + rank + 1)
        for rank, cid in enumerate(
            sorted(bm25_top, key=lambda c: bm25_top[c], reverse=True)
        ):
            rrf_scores[cid] += 1.0 / (K + rank + 1)

        # chunk_type 가중치 적용 및 최종 정렬
        results: List[Dict[str, Any]] = []
        for cid, score in rrf_scores.items():
            idx = self.id_to_index.get(cid)
            if idx is None:
                continue
            meta = self.all_metas[idx]
            weight = config.CHUNK_TYPE_WEIGHT.get(meta.get("chunk_type", ""), 1.0)
            results.append(
                {
                    "chunk_id": cid,
                    "score": score * weight,
                    "text": self.all_docs[idx],
                    "metadata": meta,
                }
            )

        results.sort(key=lambda r: r["score"], reverse=True)
        return results[:top_k]

    def search_grouped(
        self,
        query: str,
        top_cases: int = config.TOP_CASES,
    ) -> List[Dict[str, Any]]:
        """같은 case_id의 청크를 묶어 판례 단위로 반환."""
        chunks = self.search(query, top_k=config.TOP_K_DENSE)

        grouped: Dict[str, Dict[str, Any]] = {}
        for chunk in chunks:
            case_id = chunk["metadata"].get("case_id")
            if not case_id:
                continue
            if case_id not in grouped:
                grouped[case_id] = {
                    "case_id": case_id,
                    "case_name": chunk["metadata"].get("case_name"),
                    "case_number": chunk["metadata"].get("case_number"),
                    "court": chunk["metadata"].get("court"),
                    "decision_date": chunk["metadata"].get("decision_date"),
                    "referenced_statutes": chunk["metadata"].get("referenced_statutes"),
                    "score": 0.0,
                    "chunks": [],
                }
            grouped[case_id]["score"] += chunk["score"]
            grouped[case_id]["chunks"].append(chunk)

        cases = sorted(grouped.values(), key=lambda c: c["score"], reverse=True)
        return cases[:top_cases]

    def rerank(
        self, query: str, documents: List[Dict[str, str]]
    ) -> List[Dict[str, Any]]:
        """query 와 각 document(text)의 의미 유사도(코사인)를 계산해 내림차순 정렬해 반환.

        판례 인덱스(Chroma)와 무관하게, 호출 측이 보낸 임의의 문서를 즉석에서 임베딩한다.
        정규화 임베딩이므로 내적(dot)이 곧 코사인 유사도이다.

        documents: [{"id": <식별자>, "text": <프로필/본문>}]
        return:    [{"id": <식별자>, "score": <float>}]  (점수 내림차순)
        """
        if not query or not query.strip() or not documents:
            return []

        ids = [d.get("id") for d in documents]
        texts = [(d.get("text") or "") for d in documents]

        embeddings = self.model.encode(
            [query] + texts, normalize_embeddings=True, convert_to_numpy=True
        )
        query_vec = embeddings[0]
        doc_vecs = embeddings[1:]

        scored: List[Dict[str, Any]] = [
            {"id": cid, "score": float((query_vec * vec).sum())}
            for cid, vec in zip(ids, doc_vecs)
        ]
        scored.sort(key=lambda x: x["score"], reverse=True)
        return scored
