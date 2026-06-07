from __future__ import annotations

import json
from pathlib import Path
from typing import Any, Dict, List

import chromadb
from sentence_transformers import SentenceTransformer

from . import config


def load_chunks(path: Path) -> List[Dict[str, Any]]:
    records: List[Dict[str, Any]] = []
    with path.open("r", encoding="utf-8") as file:
        for line in file:
            line = line.strip()
            if not line:
                continue
            records.append(json.loads(line))
    return records


def to_metadata(chunk: Dict[str, Any]) -> Dict[str, Any]:
    keywords = chunk.get("keywords") or []
    if isinstance(keywords, list):
        keywords = ",".join(keywords)

    return {
        "case_id": str(chunk.get("case_id") or ""),
        "case_name": chunk.get("case_name") or "",
        "case_number": chunk.get("case_number") or "",
        "decision_date": chunk.get("decision_date") or "",
        "court": chunk.get("court") or "",
        "case_type": chunk.get("case_type") or "",
        "judgment_type": chunk.get("judgment_type") or "",
        "referenced_statutes": chunk.get("referenced_statutes") or "",
        "domain": chunk.get("domain") or "",
        "keywords": keywords,
        "source": chunk.get("source") or "",
        "chunk_type": chunk.get("chunk_type") or "",
        "chunk_label": chunk.get("chunk_label") or "",
        "chunk_index": int(chunk.get("chunk_index") or 0),
    }


def build_index(reset: bool = True, source: str | None = None) -> None:
    source = source or config.INDEX_SOURCE
    if source == "db":
        from .db_loader import iter_precedent_chunks

        print("[INDEX] 소스: MariaDB precedent 테이블")
        chunks = iter_precedent_chunks()
    else:
        print(f"[INDEX] 소스: JSONL ({config.CHUNKS_PATH})")
        chunks = load_chunks(config.CHUNKS_PATH)
    print(f"[INDEX] 청크 수: {len(chunks)}")

    print(f"[INDEX] 임베딩 모델 로드: {config.EMBED_MODEL} (device={config.EMBED_DEVICE})")
    model = SentenceTransformer(config.EMBED_MODEL, device=config.EMBED_DEVICE)

    config.CHROMA_DIR.mkdir(parents=True, exist_ok=True)
    client = chromadb.PersistentClient(path=str(config.CHROMA_DIR))

    if reset:
        try:
            client.delete_collection(config.COLLECTION_NAME)
            print("[INDEX] 기존 컬렉션 삭제")
        except Exception:
            pass

    collection = client.get_or_create_collection(
        name=config.COLLECTION_NAME,
        metadata={"hnsw:space": "cosine"},
    )

    texts = [c["text"] for c in chunks]
    ids = [c["chunk_id"] for c in chunks]
    metas = [to_metadata(c) for c in chunks]

    print("[INDEX] 임베딩 생성 중...")
    embeddings = model.encode(
        texts,
        batch_size=8,
        show_progress_bar=True,
        normalize_embeddings=True,
        convert_to_numpy=True,
    ).tolist()

    BATCH = 200
    for start in range(0, len(ids), BATCH):
        end = min(start + BATCH, len(ids))
        collection.add(
            ids=ids[start:end],
            embeddings=embeddings[start:end],
            documents=texts[start:end],
            metadatas=metas[start:end],
        )
        print(f"[INDEX] {end}/{len(ids)} 저장")

    print(f"[DONE] 컬렉션 '{config.COLLECTION_NAME}' 완료 - 총 {len(ids)}개 청크")


if __name__ == "__main__":
    build_index(reset=True)
