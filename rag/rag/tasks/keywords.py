from __future__ import annotations

from typing import Any, Dict

from .. import llm

SYSTEM = """당신은 법률 문서에서 핵심 키워드를 추출하는 도구입니다. 반드시 JSON 으로만 답하세요.
출력 필드:
- keywords: 법적으로 의미 있는 핵심 키워드(명사/법률용어) 문자열 배열

규칙:
- 최대 {max_keywords} 개까지만.
- 중복 금지, 너무 일반적인 단어 제외.
- JSON 외의 다른 텍스트는 출력하지 마세요."""


def extract_keywords(text: str, max_keywords: int = 5) -> Dict[str, Any]:
    if not text or not text.strip():
        return {"keywords": []}

    system = SYSTEM.format(max_keywords=max_keywords)
    user = f"[문서]\n{text}\n\n핵심 키워드를 최대 {max_keywords}개 추출해 JSON 으로 출력하세요."
    data = llm.chat_json(system, user)

    keywords = llm.as_str_list(data.get("keywords"))[:max_keywords]
    return {"keywords": keywords}
