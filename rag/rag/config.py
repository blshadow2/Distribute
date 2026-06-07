from __future__ import annotations

import os
from pathlib import Path
from urllib.parse import urlparse

from dotenv import load_dotenv

# load_dotenv 는 기존 OS 환경변수를 덮어쓰지 않는다(override=False).
# 따라서 시스템 환경변수가 잡혀 있으면 그 값이 .env 보다 우선한다.
load_dotenv()

# 데이터 경로
DATA_DIR = Path(
    os.getenv("PRECEDENT_DATA_DIR", r"\\192.168.0.17\Code\Python\precedent\data")
)
CASES_PATH = DATA_DIR / "privacy_cases.jsonl"
CHUNKS_PATH = DATA_DIR / "privacy_case_chunks.jsonl"

# Chroma 저장 위치
CHROMA_DIR = Path(os.getenv("CHROMA_DIR", "./chroma_db"))
COLLECTION_NAME = "privacy_cases"

# 임베딩
EMBED_MODEL = os.getenv("EMBED_MODEL", "BAAI/bge-m3")
EMBED_DEVICE = os.getenv("EMBED_DEVICE", "cpu")

# 검색 파라미터
TOP_K_DENSE = 20
TOP_K_BM25 = 20
TOP_K_FINAL = 8
TOP_CASES = 5

# chunk_type별 가중치 (요약/판시사항이 더 정확한 신호)
CHUNK_TYPE_WEIGHT = {
    "issues": 1.15,
    "summary": 1.10,
    "referenced_statutes": 1.05,
    "full_text": 1.00,
}

# Ollama
OLLAMA_HOST = os.getenv("OLLAMA_HOST", "http://localhost:11434")
OLLAMA_MODEL = os.getenv("OLLAMA_MODEL", "qwen2.5:14b")

# 색인 소스: "db" (MariaDB precedent 테이블) | "jsonl" (기존 파일)
INDEX_SOURCE = os.getenv("INDEX_SOURCE", "db")

# ---------------------------------------------------------------------------
# MariaDB 접속 정보
#
# 우선순위 (Java lawSystem 의 DBA 와 동일한 철학):
#   1. PRECEDENT_DB_* 가 있으면 그 값을 쓴다 (명시적 override).
#   2. 없으면 Java 가 쓰는 시스템 환경변수 LAWSYSTEM_DB_* 를 그대로 재사용한다.
#      - LAWSYSTEM_DB_URL(JDBC URL)에서 host/port/database 를 파싱한다.
#
# 즉, OS 환경변수 LAWSYSTEM_DB_* 만 잡혀 있으면 .env 에 DB 설정을 따로 둘 필요가 없다.
# ---------------------------------------------------------------------------


def _parse_jdbc_url(jdbc_url: str | None):
    """'jdbc:mariadb://host:port/dbname?params' → (host, port, dbname)."""
    if not jdbc_url:
        return None, None, None
    url = jdbc_url.strip()
    if url.startswith("jdbc:"):
        url = url[len("jdbc:"):]
    parsed = urlparse(url)
    dbname = parsed.path.lstrip("/") or None
    return parsed.hostname, parsed.port, dbname


def _first(*values, default=None):
    """앞에서부터 비어있지 않은 첫 값을 trim 해서 반환한다."""
    for value in values:
        if value is not None and str(value).strip() != "":
            return str(value).strip()
    return default


_jdbc_host, _jdbc_port, _jdbc_db = _parse_jdbc_url(os.getenv("LAWSYSTEM_DB_URL"))

DB_HOST = _first(os.getenv("PRECEDENT_DB_HOST"), _jdbc_host, default="localhost")
DB_PORT = int(_first(os.getenv("PRECEDENT_DB_PORT"), _jdbc_port, default="3306"))
DB_USER = _first(os.getenv("PRECEDENT_DB_USER"), os.getenv("LAWSYSTEM_DB_USER"), default="root")
DB_NAME = _first(
    os.getenv("PRECEDENT_DB_NAME"),
    os.getenv("LAWSYSTEM_DB_DATABASE"),
    _jdbc_db,
    default="law_system",
)

# 비밀번호는 빈 문자열도 유효하므로 trim/override 판단을 None 여부로만 한다.
_db_password = os.getenv("PRECEDENT_DB_PASSWORD")
if _db_password is None:
    _db_password = os.getenv("LAWSYSTEM_DB_PASSWORD", "")
DB_PASSWORD = _db_password
