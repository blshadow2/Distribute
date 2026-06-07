from __future__ import annotations

from typing import Any, Dict, List

import pymysql
import pymysql.cursors

from . import config

# (chunk_type, precedent 테이블 컬럼명, 한글 라벨)
# 색인 단위가 되는 필드들. issues/summary 가 가장 강한 검색 신호이다.
CHUNK_FIELDS = [
    ("issues", "issues", "판시사항"),
    ("summary", "summary", "판결요지"),
    ("referenced_statutes", "referenced_statutes", "참조조문"),
    ("full_text", "full_text", "전문"),
]


def _connect() -> "pymysql.connections.Connection":
    return pymysql.connect(
        host=config.DB_HOST,
        port=config.DB_PORT,
        user=config.DB_USER,
        password=config.DB_PASSWORD,
        database=config.DB_NAME,
        charset="utf8mb4",
        cursorclass=pymysql.cursors.DictCursor,
    )


def _load_keywords(conn) -> Dict[str, List[str]]:
    out: Dict[str, List[str]] = {}
    with conn.cursor() as cur:
        cur.execute("SELECT precedent_id, keyword FROM precedent_keyword")
        for row in cur.fetchall():
            out.setdefault(row["precedent_id"], []).append(row["keyword"])
    return out


def iter_precedent_chunks() -> List[Dict[str, Any]]:
    """precedent 테이블의 각 판례를 chunk_type 단위로 쪼갠 청크 리스트를 반환한다.

    반환 레코드의 형태는 기존 privacy_case_chunks.jsonl 한 줄과 동일하므로
    indexer.to_metadata() 가 그대로 동작한다.
    """
    conn = _connect()
    try:
        keywords_map = _load_keywords(conn)
        with conn.cursor() as cur:
            cur.execute(
                "SELECT precedent_id, external_case_id, case_name, case_number, "
                "court_name, decision_date, case_type, judgment_type, "
                "issues, summary, referenced_statutes, referenced_cases, full_text, "
                "domain, source FROM precedent"
            )
            rows = cur.fetchall()
    finally:
        conn.close()

    print(f"[DB] precedent 행 수: {len(rows)}")

    chunks: List[Dict[str, Any]] = []
    for row in rows:
        external_id = str(row["external_case_id"])
        keywords = keywords_map.get(row["precedent_id"], [])
        decision_date = (
            row["decision_date"].strftime("%Y%m%d") if row.get("decision_date") else ""
        )

        base_meta = {
            "case_id": external_id,
            "case_name": row.get("case_name") or "",
            "case_number": row.get("case_number") or "",
            "court": row.get("court_name") or "",
            "decision_date": decision_date,
            "case_type": row.get("case_type") or "",
            "judgment_type": row.get("judgment_type") or "",
            "referenced_statutes": row.get("referenced_statutes") or "",
            "domain": row.get("domain") or "",
            "keywords": keywords,
            "source": row.get("source") or "",
        }

        header = (
            f"[원본 정보]\n사건명: {base_meta['case_name']}\n"
            f"사건번호: {base_meta['case_number']}\n법원: {base_meta['court']}\n"
            f"분야: {base_meta['domain']}\n\n"
        )

        for idx, (chunk_type, column, label) in enumerate(CHUNK_FIELDS):
            content = (row.get(column) or "").strip()
            if not content:
                continue
            chunks.append(
                {
                    "chunk_id": f"{external_id}_{chunk_type}_0",
                    "chunk_type": chunk_type,
                    "chunk_label": label,
                    "chunk_index": idx,
                    "text": f"{header}[{label}]\n{content}",
                    **base_meta,
                }
            )

    print(f"[DB] 생성된 청크 수: {len(chunks)}")
    return chunks
