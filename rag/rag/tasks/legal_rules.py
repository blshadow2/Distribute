from __future__ import annotations

from typing import Any, Dict

from .. import llm
from ..generator import build_context

SYSTEM = """당신은 한국 개인정보 보호 분야 법률 전문가입니다.
아래 [판례 컨텍스트]에 제시된 판례만을 근거로 입력 사건의 법리를 분석합니다.

반드시 아래 JSON 형식 그대로, 키 이름은 영문 그대로 두고 값만 한국어로 채워서 출력하세요.
JSON 외의 다른 텍스트(설명, 코드펜스)는 절대 출력하지 마세요.
{
  "issue_summary": "핵심 법적 쟁점을 2~3문장으로 요약",
  "applicable_law": "적용되는 핵심 법리",
  "legal_explanation": "판례를 인용하며 설명. 인용은 [사건번호] 형식 사용",
  "related_statutes": ["관련 법령 조문", "..."]
}

컨텍스트에 근거가 없으면 추측하지 말고 해당 값에 "주어진 판례에서 확인되지 않습니다" 라고 적으세요."""

# JSON 구조화가 약한 모델(gemma 등)을 위한 산문 폴백 프롬프트
PROSE_SYSTEM = """당신은 한국 개인정보 보호 분야 법률 전문가입니다.
아래 [판례 컨텍스트]에 제시된 판례만 근거로, 입력 사건의 (1) 핵심 쟁점, (2) 적용 법리,
(3) 관련 법령 조문을 한국어로 조리 있게 설명하세요. 인용은 [사건번호] 형식을 쓰고,
컨텍스트에 없는 내용은 추측하지 마세요."""


def analyze_rules(retriever, query: str, top_k: int = 5) -> Dict[str, Any]:
    """RAG 결합: 관련 판례를 검색해 컨텍스트로 주고, 법리 분석을 생성한다.

    1) 구조화(JSON) 시도 → 2) 실패(빈 값)면 산문 설명으로 폴백.
    """
    if not query or not query.strip():
        return _empty()

    cases = retriever.search_grouped(query, top_cases=top_k)
    if not cases:
        return _empty()

    # num_ctx 초과로 질문이 잘리지 않도록 컨텍스트를 짧게 유지
    context = build_context(cases, max_chars_per_chunk=700)
    cited = [c["case_id"] for c in cases]

    # 1) 구조화 시도
    user = (
        f"[판례 컨텍스트]\n{context}\n\n"
        f"[분석 대상 사건]\n{query}\n\n"
        f"위 JSON 형식으로만 답하세요."
    )
    data = llm.chat_json(SYSTEM, user, timeout=300)

    def pick(*keys: str) -> Any:
        for k in keys:
            v = data.get(k)
            if v:
                return v
        return ""

    issue = str(pick("issue_summary", "issueSummary", "쟁점", "핵심쟁점", "핵심_쟁점") or "")
    law = str(pick("applicable_law", "applicableLaw", "적용법리", "적용_법리", "법리") or "")
    explanation = str(pick("legal_explanation", "legalExplanation", "법리설명", "법리_설명", "설명") or "")
    statutes = llm.as_str_list(pick("related_statutes", "relatedStatutes", "관련법령", "관련_법령", "법령"))

    # 2) 구조화 실패 → 산문 폴백 (설명을 반드시 제공)
    if not (issue or law or explanation):
        prose = _prose_fallback(context, query)
        explanation = prose or "법리 설명을 생성하지 못했습니다."
        if not statutes:
            # 검색된 판례의 참조조문을 보조로 노출
            statutes = _statutes_from_cases(cases)

    return {
        "issue_summary": issue,
        "applicable_law": law,
        "legal_explanation": explanation,
        "related_statutes": statutes,
        "cited_cases": cited,
    }


def _prose_fallback(context: str, query: str) -> str:
    user = (
        f"[판례 컨텍스트]\n{context}\n\n"
        f"[분석 대상 사건]\n{query}\n\n"
        f"[법리 설명]"
    )
    try:
        return llm.chat(
            [
                {"role": "system", "content": PROSE_SYSTEM},
                {"role": "user", "content": user},
            ],
            json_mode=False,
            timeout=300,
        ).strip()
    except Exception as exc:  # noqa: BLE001
        print("[legal_rules] 산문 폴백 실패:", exc)
        return ""


def _statutes_from_cases(cases) -> list:
    seen: list = []
    for c in cases:
        raw = (c.get("referenced_statutes") or "").strip()
        if raw and raw not in seen:
            seen.append(raw)
    return seen[:5]


def _empty() -> Dict[str, Any]:
    return {
        "issue_summary": "",
        "applicable_law": "",
        "legal_explanation": "",
        "related_statutes": [],
        "cited_cases": [],
    }
