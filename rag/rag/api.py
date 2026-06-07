from __future__ import annotations

from typing import List, Optional

from fastapi import FastAPI, HTTPException
from pydantic import BaseModel, Field

from . import config
from .generator import generate_answer
from .service import PrecedentRAGService

app = FastAPI(title="Precedent RAG API", version="1.0.0")
_service: Optional[PrecedentRAGService] = None


def get_service() -> PrecedentRAGService:
    global _service
    if _service is None:
        _service = PrecedentRAGService()
    return _service


@app.on_event("startup")
def warmup() -> None:
    get_service()


class SimilarPrecedentsRequest(BaseModel):
    query: str = Field(..., description="사건 요약 또는 검색 쿼리")
    case_id: Optional[str] = Field(None, description="요청한 사건 ID (참고용)")
    top_k: int = Field(5, ge=1, le=20)
    similarity_threshold: float = Field(0.0, ge=0.0, le=1.0)


class Hit(BaseModel):
    # 본문(사건명/요지/조문 등)은 Java 가 external_case_id 로 DB 에서 조회한다.
    # RAG 서비스는 "어떤 판례가 / 얼마나 유사한지" 만 책임진다.
    case_id: str  # = precedent.external_case_id
    similarity_score: float
    matched_chunk_types: List[str] = Field(default_factory=list)
    matched_excerpt: Optional[str] = None


class SimilarPrecedentsResponse(BaseModel):
    hits: List[Hit]


class AnswerRequest(BaseModel):
    query: str
    top_k: int = 5


class AnswerResponse(BaseModel):
    answer: str
    hits: List[Hit]


# --- Step 2: 웹서비스용 AI (요약 / 키워드 / 법리 설명) ---

class SummarizeRequest(BaseModel):
    text: str = Field(..., description="요약할 사건 내용")
    case_id: Optional[str] = None


class SummarizeResponse(BaseModel):
    summary: str = ""
    main_issues: List[str] = Field(default_factory=list)
    timeline: str = ""


class KeywordsRequest(BaseModel):
    text: str = Field(..., description="키워드를 추출할 텍스트")
    max_keywords: int = Field(5, ge=1, le=20)


class KeywordsResponse(BaseModel):
    keywords: List[str] = Field(default_factory=list)


class AnalyzeRulesRequest(BaseModel):
    query: str = Field(..., description="법리 분석 대상 사건/문서 내용")
    case_id: Optional[str] = None
    top_k: int = Field(5, ge=1, le=20)


class AnalyzeRulesResponse(BaseModel):
    issue_summary: str = ""
    applicable_law: str = ""
    legal_explanation: str = ""
    related_statutes: List[str] = Field(default_factory=list)
    cited_cases: List[str] = Field(default_factory=list)


def _normalize_scores(cases: list) -> None:
    if not cases:
        return
    max_s = max(c["score"] for c in cases)
    min_s = min(c["score"] for c in cases)
    span = (max_s - min_s) or 1.0
    for c in cases:
        c["norm_score"] = (c["score"] - min_s) / span if max_s != min_s else 1.0


def _to_hit(case: dict) -> Hit:
    chunk_types: List[str] = []
    for ch in case["chunks"]:
        ct = ch["metadata"].get("chunk_type")
        if ct and ct not in chunk_types:
            chunk_types.append(ct)
    excerpt = case["chunks"][0]["text"][:300] if case["chunks"] else None
    return Hit(
        case_id=case["case_id"],
        similarity_score=round(case.get("norm_score", 0.0), 4),
        matched_chunk_types=chunk_types,
        matched_excerpt=excerpt,
    )


@app.get("/healthz")
def healthz() -> dict:
    return {"status": "ok", "collection": config.COLLECTION_NAME}


@app.post("/search/similar-precedents", response_model=SimilarPrecedentsResponse)
def search_similar_precedents(payload: SimilarPrecedentsRequest) -> SimilarPrecedentsResponse:
    if not payload.query.strip():
        raise HTTPException(status_code=400, detail="query is empty")

    service = get_service()
    cases = service.retriever.search_grouped(payload.query, top_cases=payload.top_k)
    _normalize_scores(cases)

    hits = [
        _to_hit(c)
        for c in cases
        if c.get("norm_score", 0.0) >= payload.similarity_threshold
    ]
    return SimilarPrecedentsResponse(hits=hits)


@app.post("/answer", response_model=AnswerResponse)
def answer_endpoint(payload: AnswerRequest) -> AnswerResponse:
    if not payload.query.strip():
        raise HTTPException(status_code=400, detail="query is empty")

    service = get_service()
    cases = service.retriever.search_grouped(payload.query, top_cases=payload.top_k)
    answer_text = generate_answer(payload.query, cases, stream=False)
    _normalize_scores(cases)
    hits = [_to_hit(c) for c in cases]
    return AnswerResponse(answer=answer_text, hits=hits)


@app.post("/llm/summarize", response_model=SummarizeResponse)
def llm_summarize(payload: SummarizeRequest) -> SummarizeResponse:
    if not payload.text.strip():
        raise HTTPException(status_code=400, detail="text is empty")
    from .tasks import summarize as task
    return SummarizeResponse(**task.summarize(payload.text))


@app.post("/llm/extract-keywords", response_model=KeywordsResponse)
def llm_extract_keywords(payload: KeywordsRequest) -> KeywordsResponse:
    if not payload.text.strip():
        raise HTTPException(status_code=400, detail="text is empty")
    from .tasks import keywords as task
    return KeywordsResponse(**task.extract_keywords(payload.text, payload.max_keywords))


@app.post("/rag/analyze-rules", response_model=AnalyzeRulesResponse)
def rag_analyze_rules(payload: AnalyzeRulesRequest) -> AnalyzeRulesResponse:
    if not payload.query.strip():
        raise HTTPException(status_code=400, detail="query is empty")
    from .tasks import legal_rules as task
    service = get_service()
    return AnalyzeRulesResponse(**task.analyze_rules(service.retriever, payload.query, payload.top_k))
