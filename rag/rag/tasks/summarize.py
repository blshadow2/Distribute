from __future__ import annotations

from typing import Any, Dict

from .. import llm

SYSTEM = """당신은 한국 법률 사건 분석가입니다. 입력된 사건 내용을 분석해 반드시 JSON 으로만 답하세요.
출력 필드:
- summary: 사건의 핵심을 3~5문장으로 요약한 문자열
- main_issues: 주요 법적 쟁점들의 문자열 배열 (3~6개)
- timeline: 사건 전개를 시간순으로 한 줄 요약한 문자열 (정보가 없으면 빈 문자열)

규칙:
- 입력에 없는 사실은 추측하거나 만들어내지 마세요.
- JSON 외의 다른 텍스트는 출력하지 마세요."""


def summarize(text: str) -> Dict[str, Any]:
    if not text or not text.strip():
        return {"summary": "", "main_issues": [], "timeline": ""}

    user = f"[사건 내용]\n{text}\n\n위 내용을 분석해 JSON 으로 출력하세요."
    data = llm.chat_json(SYSTEM, user)

    return {
        "summary": str(data.get("summary", "") or ""),
        "main_issues": llm.as_str_list(data.get("main_issues")),
        "timeline": str(data.get("timeline", "") or ""),
    }
