from __future__ import annotations

import json
from typing import Any, Dict, List

import requests

from . import config


def chat(
    messages: List[Dict[str, str]],
    *,
    json_mode: bool = False,
    temperature: float = 0.2,
    num_ctx: int = 8192,
    timeout: int = 300,
) -> str:
    """Ollama /api/chat 단일 호출(비스트리밍). json_mode=True 면 JSON 출력을 강제한다."""
    payload: Dict[str, Any] = {
        "model": config.OLLAMA_MODEL,
        "messages": messages,
        "stream": False,
        "options": {"temperature": temperature, "num_ctx": num_ctx},
    }
    if json_mode:
        payload["format"] = "json"  # Ollama 가 유효한 JSON 만 출력하도록 강제

    resp = requests.post(f"{config.OLLAMA_HOST}/api/chat", json=payload, timeout=timeout)
    resp.raise_for_status()
    return resp.json()["message"]["content"]


def chat_json(system: str, user: str, *, temperature: float = 0.2, timeout: int = 300) -> Dict[str, Any]:
    """system/user 프롬프트로 JSON 응답을 받아 dict 로 파싱한다(실패 시 폴백)."""
    content = chat(
        [
            {"role": "system", "content": system},
            {"role": "user", "content": user},
        ],
        json_mode=True,
        temperature=temperature,
        timeout=timeout,
    )
    data = _safe_json(content)
    if not data:
        # 진단용: 모델이 비정상/빈 JSON을 낼 때 원본 일부를 콘솔에 남긴다.
        print("[llm] JSON 파싱 실패 — 원본 응답 일부:", (content or "")[:500])
    return data


def _safe_json(text: str) -> Dict[str, Any]:
    """모델이 JSON 외 텍스트를 섞어도 최대한 dict 로 복구한다."""
    if not text:
        return {}
    t = text.strip()
    # ```json ... ``` 코드펜스 제거
    if t.startswith("```"):
        t = t.lstrip("`")
        if t[:4].lower() == "json":
            t = t[4:]
        t = t.strip().rstrip("`").strip()
    try:
        return json.loads(t)
    except Exception:
        start = t.find("{")
        end = t.rfind("}")
        if start != -1 and end != -1 and end > start:
            try:
                return json.loads(t[start : end + 1])
            except Exception:
                pass
        return {}


def as_str_list(value: Any) -> List[str]:
    """LLM 출력의 다양한 형태를 문자열 리스트로 정규화한다."""
    if isinstance(value, list):
        return [str(item).strip() for item in value if str(item).strip()]
    if isinstance(value, str) and value.strip():
        # "a, b, c" 형태도 허용
        parts = [p.strip() for p in value.replace("\n", ",").split(",")]
        return [p for p in parts if p]
    return []
