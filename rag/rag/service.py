from __future__ import annotations

from typing import Any, Dict, List

from . import config
from .generator import generate_answer
from .retriever import Retriever


class PrecedentRAGService:
    def __init__(self) -> None:
        self.retriever = Retriever()

    def search(
        self,
        query: str,
        top_cases: int = config.TOP_CASES,
    ) -> List[Dict[str, Any]]:
        return self.retriever.search_grouped(query, top_cases=top_cases)

    def answer(
        self,
        query: str,
        top_cases: int = config.TOP_CASES,
        stream: bool = False,
    ) -> Dict[str, Any]:
        cases = self.retriever.search_grouped(query, top_cases=top_cases)
        answer_text = generate_answer(query, cases, stream=stream)
        return {
            "answer": answer_text,
            "cases": [
                {
                    "case_id": case["case_id"],
                    "case_name": case["case_name"],
                    "case_number": case["case_number"],
                    "court": case["court"],
                    "decision_date": case["decision_date"],
                    "referenced_statutes": case["referenced_statutes"],
                    "score": case["score"],
                }
                for case in cases
            ],
        }
