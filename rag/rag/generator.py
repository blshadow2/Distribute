from __future__ import annotations

import json
from typing import Any, Dict, List

import requests

from . import config

SYSTEM_PROMPT = """당신은 한국 개인정보 보호 분야 판례 전문가입니다.
아래 [판례 컨텍스트]에 제시된 판례만을 근거로 사용자의 질문에 답변하세요.

규칙:
1. 컨텍스트에 없는 사실은 추측하거나 만들어내지 마세요. 모르면 "주어진 판례에서는 확인되지 않습니다"라고 답하세요.
2. 인용할 때는 반드시 [사건번호 (사건명)] 형식을 사용하세요. 예: [2025도0321 (개인정보보호법위반)]
3. 답변은 한국어로 작성하고, 결론을 먼저 제시한 뒤 근거 판례를 설명하세요.
4. 여러 판례의 입장이 다르면 각각의 입장을 모두 제시하세요.
"""


def build_context(cases: List[Dict[str, Any]], max_chars_per_chunk: int = 1500) -> str:
    blocks: List[str] = []
    for index, case in enumerate(cases, start=1):
        header = (
            f"### 판례 {index}\n"
            f"- 사건명: {case.get('case_name')}\n"
            f"- 사건번호: {case.get('case_number')}\n"
            f"- 법원: {case.get('court')}\n"
            f"- 선고일자: {case.get('decision_date')}\n"
            f"- 참조조문: {case.get('referenced_statutes')}\n"
        )
        chunk_blocks: List[str] = []
        for chunk in case["chunks"][:3]:
            text = chunk["text"]
            if len(text) > max_chars_per_chunk:
                text = text[:max_chars_per_chunk] + "...(이하 생략)"
            chunk_blocks.append(text)
        blocks.append(header + "\n".join(chunk_blocks))
    return "\n\n".join(blocks)


def generate_answer(
    query: str,
    cases: List[Dict[str, Any]],
    stream: bool = False,
) -> str:
    context = build_context(cases)
    user_prompt = (
        f"[판례 컨텍스트]\n{context}\n\n"
        f"[질문]\n{query}\n\n"
        f"[답변]"
    )

    payload: Dict[str, Any] = {
        "model": config.OLLAMA_MODEL,
        "messages": [
            {"role": "system", "content": SYSTEM_PROMPT},
            {"role": "user", "content": user_prompt},
        ],
        "stream": stream,
        "options": {
            "temperature": 0.2,
            "num_ctx": 8192,
        },
    }
    url = f"{config.OLLAMA_HOST}/api/chat"

    if not stream:
        response = requests.post(url, json=payload, timeout=300)
        response.raise_for_status()
        return response.json()["message"]["content"]

    collected: List[str] = []
    with requests.post(url, json=payload, stream=True, timeout=600) as response:
        response.raise_for_status()
        for raw_line in response.iter_lines():
            if not raw_line:
                continue
            data = json.loads(raw_line)
            piece = data.get("message", {}).get("content", "")
            if piece:
                collected.append(piece)
                print(piece, end="", flush=True)
    print()
    return "".join(collected)
