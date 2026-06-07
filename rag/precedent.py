from __future__ import annotations

import os
import re
import json
import time
import html
from pathlib import Path
from typing import Any, Dict, List, Optional, Set

import requests
from dotenv import load_dotenv


class PrivacyCaseCollector:
    """
    국가법령정보센터 판례 API를 이용해 개인정보 관련 판례를 수집하고,
    RAG에 사용할 수 있는 원문 데이터와 chunk 데이터를 생성하는 클래스
    """

    SEARCH_URL = "http://www.law.go.kr/DRF/lawSearch.do"
    DETAIL_URL = "http://www.law.go.kr/DRF/lawService.do"

    def __init__(
        self,
        oc: str,
        output_dir: str = "data",
        request_interval: float = 0.2,
        timeout: int = 10,
    ):
        self.oc = oc
        self.output_dir = Path(output_dir)
        self.request_interval = request_interval
        self.timeout = timeout
        self.session = requests.Session()

        self.output_dir.mkdir(parents=True, exist_ok=True)

        self.domain = "개인정보보호"

        self.queries = [
            "개인정보",
            "개인정보 보호법",
            "개인정보 유출",
            "개인정보 제3자 제공",
            "정보주체 동의",
            "개인정보 처리자",
            "개인정보처리자",
            "민감정보",
            "고유식별정보",
            "신용정보",
            "위치정보",
        ]

        self.statutes = [
            "개인정보 보호법",
            "정보통신망 이용촉진 및 정보보호 등에 관한 법률",
            "신용정보의 이용 및 보호에 관한 법률",
            "위치정보의 보호 및 이용 등에 관한 법률",
        ]

        self.include_keywords = [
            "개인정보",
            "개인정보 보호법",
            "개인정보처리자",
            "개인정보 처리자",
            "정보주체",
            "제3자 제공",
            "동의",
            "수집",
            "이용",
            "제공",
            "처리",
            "유출",
            "누출",
            "민감정보",
            "고유식별정보",
            "신용정보",
            "위치정보",
            "손해배상",
            "위자료",
        ]

        self.exclude_keywords = [
            "개인정보처리방침",  # 문서 하단 안내문 등 불필요한 경우 방지용
        ]

    def request_json(self, url: str, params: Dict[str, Any]) -> Dict[str, Any]:
        """
        API에 GET 요청을 보내고 JSON 응답을 반환한다.
        일시적 오류가 발생할 수 있으므로 간단한 재시도 로직을 포함한다.
        """
        last_error = None

        for attempt in range(3):
            try:
                response = self.session.get(
                    url,
                    params=params,
                    timeout=self.timeout,
                )
                response.raise_for_status()

                try:
                    return response.json()
                except json.JSONDecodeError:
                    preview = response.text[:500]
                    raise ValueError(f"JSON 응답 파싱 실패: {preview}")

            except Exception as error:
                last_error = error
                wait_time = 1 + attempt
                print(f"[WARN] 요청 실패, {wait_time}초 후 재시도: {error}")
                time.sleep(wait_time)

        raise RuntimeError(f"API 요청 실패: {last_error}")

    def search_cases(
        self,
        query: str,
        statute: Optional[str] = None,
        page: int = 1,
        start_date: Optional[str] = None,
        end_date: Optional[str] = None,
        court_type: Optional[str] = None,
    ) -> List[Dict[str, Any]]:
        """
        판례 목록 API를 호출해 후보 판례 목록을 가져온다.

        court_type 예시:
        - None: 전체
        - "400201": 대법원
        - "400202": 하위법원
        """
        params = {
            "OC": self.oc,
            "target": "prec",
            "type": "JSON",
            "search": 2,
            "query": query,
            "display": 100,
            "page": page,
            "sort": "ddes",
        }

        if statute:
            params["JO"] = statute

        if start_date and end_date:
            params["prncYd"] = f"{start_date}~{end_date}"

        if court_type:
            params["org"] = court_type

        data = self.request_json(self.SEARCH_URL, params)
        cases = self.extract_search_items(data)

        return cases

    def get_case_detail(self, case_id: str) -> Dict[str, Any]:
        """
        판례 일련번호를 이용해 판례 상세 본문을 조회한다.
        """
        params = {
            "OC": self.oc,
            "target": "prec",
            "ID": case_id,
            "type": "JSON",
        }

        return self.request_json(self.DETAIL_URL, params)

    def collect(
        self,
        max_pages_per_query: int = 3,
        start_date: str = "20100101",
        end_date: str = "20261231",
        court_type: Optional[str] = None,
    ) -> List[Dict[str, Any]]:
        """
        개인정보 관련 검색어와 참조법령 조합으로 판례를 수집한다.
        중복 판례는 판례 일련번호 기준으로 제거한다.
        """
        collected_cases: Dict[str, Dict[str, Any]] = {}
        visited_case_ids: Set[str] = set()

        for query in self.queries:
            for statute in self.statutes:
                for page in range(1, max_pages_per_query + 1):
                    print(f"[SEARCH] query={query}, statute={statute}, page={page}")

                    search_results = self.search_cases(
                        query=query,
                        statute=statute,
                        page=page,
                        start_date=start_date,
                        end_date=end_date,
                        court_type=court_type,
                    )

                    if not search_results:
                        break

                    for item in search_results:
                        case_id = self.get_case_id(item)

                        if not case_id:
                            continue

                        if case_id in visited_case_ids:
                            continue

                        visited_case_ids.add(case_id)

                        try:
                            detail_data = self.get_case_detail(case_id)
                            normalized_case = self.normalize_case(detail_data)

                            if self.is_privacy_related(normalized_case):
                                collected_cases[case_id] = normalized_case
                                print(f"  [COLLECTED] {normalized_case.get('case_name')}")

                            time.sleep(self.request_interval)

                        except Exception as error:
                            print(f"  [ERROR] case_id={case_id}, error={error}")

                    time.sleep(self.request_interval)

        return list(collected_cases.values())

    def extract_search_items(self, data: Dict[str, Any]) -> List[Dict[str, Any]]:
        """
        목록 조회 API 응답에서 판례 목록을 안전하게 추출한다.
        응답 구조가 약간 달라도 동작하도록 처리한다.
        """
        prec_items = self.find_key(data, "prec")

        if prec_items is None:
            return []

        if isinstance(prec_items, list):
            return prec_items

        if isinstance(prec_items, dict):
            return [prec_items]

        return []

    def get_case_id(self, item: Dict[str, Any]) -> Optional[str]:
        """
        검색 결과에서 판례 일련번호를 추출한다.
        """
        possible_keys = [
            "판례일련번호",
            "판례정보일련번호",
            "ID",
            "id",
        ]

        for key in possible_keys:
            value = item.get(key)
            if value:
                return str(value)

        return None

    def normalize_case(self, detail_data: Dict[str, Any]) -> Dict[str, Any]:
        """
        판례 상세 응답에서 RAG 저장에 필요한 필드만 정리한다.
        """
        service_data = self.find_key(detail_data, "PrecService")

        if service_data is None:
            service_data = detail_data

        case = {
            "case_id": self.clean_text(self.get_any(service_data, ["판례정보일련번호", "판례일련번호", "ID"])),
            "case_name": self.clean_text(self.get_any(service_data, ["사건명"])),
            "case_number": self.clean_text(self.get_any(service_data, ["사건번호"])),
            "decision_date": self.clean_text(self.get_any(service_data, ["선고일자"])),
            "court": self.clean_text(self.get_any(service_data, ["법원명"])),
            "court_type_code": self.clean_text(self.get_any(service_data, ["법원종류코드"])),
            "case_type": self.clean_text(self.get_any(service_data, ["사건종류명"])),
            "judgment_type": self.clean_text(self.get_any(service_data, ["판결유형"])),
            "issues": self.clean_text(self.get_any(service_data, ["판시사항"])),
            "summary": self.clean_text(self.get_any(service_data, ["판결요지"])),
            "referenced_statutes": self.clean_text(self.get_any(service_data, ["참조조문"])),
            "referenced_cases": self.clean_text(self.get_any(service_data, ["참조판례"])),
            "full_text": self.clean_text(self.get_any(service_data, ["판례내용"])),
            "domain": self.domain,
            "source": "국가법령정보센터",
        }

        case["keywords"] = self.extract_matched_keywords(case)

        return case

    def is_privacy_related(self, case: Dict[str, Any]) -> bool:
        """
        개인정보 관련 판례인지 점수 기반으로 판단한다.
        """
        combined_text = " ".join(
            [
                case.get("case_name", ""),
                case.get("issues", ""),
                case.get("summary", ""),
                case.get("referenced_statutes", ""),
                case.get("full_text", ""),
            ]
        )

        if any(keyword in combined_text for keyword in self.exclude_keywords):
            return False

        statute_score = sum(3 for statute in self.statutes if statute in combined_text)
        keyword_score = sum(1 for keyword in self.include_keywords if keyword in combined_text)

        total_score = statute_score + keyword_score

        return total_score >= 3

    def extract_matched_keywords(self, case: Dict[str, Any]) -> List[str]:
        """
        해당 판례에서 발견된 개인정보 관련 키워드를 추출한다.
        """
        combined_text = " ".join(
            [
                case.get("case_name", ""),
                case.get("issues", ""),
                case.get("summary", ""),
                case.get("referenced_statutes", ""),
                case.get("full_text", ""),
            ]
        )

        matched = []

        for keyword in self.include_keywords:
            if keyword in combined_text:
                matched.append(keyword)

        return sorted(set(matched))

    def build_chunks(
        self,
        cases: List[Dict[str, Any]],
        chunk_size: int = 1200,
        overlap: int = 150,
    ) -> List[Dict[str, Any]]:
        """
        수집된 판례를 RAG 검색에 적합한 chunk 단위로 분리한다.
        판시사항, 판결요지, 본문을 서로 다른 chunk_type으로 저장한다.
        """
        chunks = []

        for case in cases:
            base_metadata = {
                "case_id": case.get("case_id"),
                "case_name": case.get("case_name"),
                "case_number": case.get("case_number"),
                "decision_date": case.get("decision_date"),
                "court": case.get("court"),
                "case_type": case.get("case_type"),
                "judgment_type": case.get("judgment_type"),
                "referenced_statutes": case.get("referenced_statutes"),
                "domain": case.get("domain"),
                "keywords": case.get("keywords"),
                "source": case.get("source"),
            }

            structured_parts = [
                ("issues", "판시사항", case.get("issues", "")),
                ("summary", "판결요지", case.get("summary", "")),
                ("referenced_statutes", "참조조문", case.get("referenced_statutes", "")),
                ("full_text", "판례내용", case.get("full_text", "")),
            ]

            for chunk_type, chunk_label, text in structured_parts:
                if not text:
                    continue

                split_texts = self.split_text(text, chunk_size, overlap)

                for index, split_text in enumerate(split_texts):
                    chunk = {
                        **base_metadata,
                        "chunk_id": f"{case.get('case_id')}_{chunk_type}_{index}",
                        "chunk_type": chunk_type,
                        "chunk_label": chunk_label,
                        "chunk_index": index,
                        "text": self.make_chunk_text(case, chunk_label, split_text),
                    }

                    chunks.append(chunk)

        return chunks

    def make_chunk_text(
        self,
        case: Dict[str, Any],
        chunk_label: str,
        body: str,
    ) -> str:
        """
        벡터 DB에 넣을 최종 텍스트 형태를 만든다.
        메타데이터 일부를 본문 앞에 포함하면 검색 품질이 좋아진다.
        """
        return f"""
[판례 정보]
사건명: {case.get("case_name")}
사건번호: {case.get("case_number")}
법원: {case.get("court")}
선고일자: {case.get("decision_date")}
사건종류: {case.get("case_type")}
참조조문: {case.get("referenced_statutes")}
분야: {case.get("domain")}

[{chunk_label}]
{body}
""".strip()

    def split_text(
        self,
        text: str,
        chunk_size: int,
        overlap: int,
    ) -> List[str]:
        """
        긴 판례 본문을 일정 길이로 나눈다.
        """
        text = text.strip()

        if len(text) <= chunk_size:
            return [text]

        chunks = []
        start = 0

        while start < len(text):
            end = start + chunk_size
            chunk = text[start:end].strip()

            if chunk:
                chunks.append(chunk)

            start = end - overlap

            if start < 0:
                start = 0

            if start >= len(text):
                break

        return chunks

    def save_jsonl(self, records: List[Dict[str, Any]], filename: str) -> None:
        """
        데이터를 JSONL 형식으로 저장한다.
        """
        path = self.output_dir / filename

        with path.open("w", encoding="utf-8") as file:
            for record in records:
                file.write(json.dumps(record, ensure_ascii=False) + "\n")

        print(f"[SAVE] {path} 저장 완료, 개수: {len(records)}")

    def get_any(self, data: Any, keys: List[str]) -> Any:
        """
        딕셔너리에서 여러 후보 키 중 먼저 발견되는 값을 반환한다.
        """
        if not isinstance(data, dict):
            return None

        for key in keys:
            if key in data:
                return data[key]

        for value in data.values():
            if isinstance(value, dict):
                found = self.get_any(value, keys)
                if found is not None:
                    return found

        return None

    def find_key(self, data: Any, target_key: str) -> Any:
        """
        중첩된 dict/list 구조에서 특정 키를 재귀적으로 찾는다.
        """
        if isinstance(data, dict):
            if target_key in data:
                return data[target_key]

            for value in data.values():
                found = self.find_key(value, target_key)
                if found is not None:
                    return found

        elif isinstance(data, list):
            for item in data:
                found = self.find_key(item, target_key)
                if found is not None:
                    return found

        return None

    def clean_text(self, value: Any) -> str:
        """
        HTML 태그, 불필요한 공백, 줄바꿈을 정리한다.
        """
        if value is None:
            return ""

        if isinstance(value, (dict, list)):
            value = json.dumps(value, ensure_ascii=False)

        text = str(value)

        text = html.unescape(text)
        text = re.sub(r"<br\s*/?>", "\n", text, flags=re.IGNORECASE)
        text = re.sub(r"</p>", "\n", text, flags=re.IGNORECASE)
        text = re.sub(r"<[^>]+>", " ", text)
        text = re.sub(r"\r\n?", "\n", text)
        text = re.sub(r"[ \t]+", " ", text)
        text = re.sub(r"\n{3,}", "\n\n", text)

        return text.strip()


def main():
    load_dotenv()

    oc = os.getenv("LAW_API_OC")

    if not oc:
        raise ValueError(".env 파일에 LAW_API_OC 값을 설정해야 합니다.")

    collector = PrivacyCaseCollector(
        oc=oc,
        output_dir=Path(__file__).parent / "data",
        request_interval=0.2,
    )

    cases = collector.collect(
        max_pages_per_query=3,
        start_date="20100101",
        end_date="20261231",
        court_type=None,
    )

    chunks = collector.build_chunks(
        cases=cases,
        chunk_size=1200,
        overlap=150,
    )

    collector.save_jsonl(cases, "privacy_cases.jsonl")
    collector.save_jsonl(chunks, "privacy_case_chunks.jsonl")

    print(f"수집 판례 수: {len(cases)}")
    print(f"생성 chunk 수: {len(chunks)}")


if __name__ == "__main__":
    main()