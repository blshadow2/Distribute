# 판례 RAG 검색 시스템 — 통합 정리 문서

> **목적**: 기존 법률관리 시스템(`lawSystem`, Java)에 **RAG 기반 유사 판례 검색**을 붙이고,
> 판례 원문을 **MariaDB가 단일 진실 공급원(Single Source of Truth)** 이 되도록 구성한다.
>
> **현재 상태**: **Phase 3 Step 1 + Step 2 완료.**
> Step 1(DB 기반 RAG): 검색(`/search`)·생성(`/answer`)·Java 연동 검증됨.
> Step 2(AI 3종 + 영속화): 요약·키워드·법리(RAG 결합) 구현 + 요청→호출→저장 흐름 연결.

---

## 0. 프로세스 시작 / 종료 (Quick Reference)

### ▶ 시작 (매 작업 시작 시)
보통 ①②는 상시 실행 상태이므로 **③부터** 하면 된다.

```
① MariaDB (DB PC)   : Windows 서비스로 상시 실행
                       확인) Test-NetConnection 192.168.0.A -Port 3306   → True
② Ollama (코드 PC)   : 설치 시 백그라운드 자동 실행
                       확인) ollama list
③ RAG 서비스 (코드 PC):
     Set-Location \\192.168.0.17\Code\Python\precedent
     C:\rag-venv\Scripts\Activate.ps1
     python -m scripts.serve          # ← 이 창은 켜둔 채 유지
④ 헬스 체크 (새 창)  : Invoke-RestMethod http://localhost:8000/healthz   → status: ok
⑤ Java 앱 실행
```

### ⏹ 안전 종료 (시작의 역순)
```
① Java 앱 정상 종료
② RAG 서비스 종료    : serve 실행 중인 창에서  Ctrl + C   (uvicorn graceful shutdown, 1회면 충분)
     - 창을 이미 닫았거나 백그라운드로 띄운 경우 포트로 종료:
       Get-NetTCPConnection -LocalPort 8000 -State Listen |
           ForEach-Object { Stop-Process -Id $_.OwningProcess -Force }
③ (선택) venv 비활성화        : deactivate
④ (선택) LLM 메모리 해제      : ollama stop qwen2.5:14b   (Ollama 서비스 자체는 보통 켜둠)
⑤ MariaDB / Ollama 서비스    : 공유 인프라이므로 그대로 둔다 (끄지 않음)
```

### ⚠ 데이터 안전 / 주의
- **Chroma는 별도 프로세스가 아니다**: serve 프로세스 안의 라이브러리이며, 색인은 `chroma_db/` 폴더에 디스크로 저장된다. → serve를 꺼도 **색인은 보존**되고 재색인 불필요.
- **Ctrl+C 1회 = graceful shutdown**. 두 번 누르면 강제 종료.
- **DB는 끄지 않는다**: 다른 기능도 쓰는 공유 자원이라 RAG 종료와 무관하게 유지.
- **재색인이 필요한 경우만** `python -m scripts.build_index` 재실행(= 판례 데이터가 바뀐 경우).
- 포트가 안 닫히거나 "이미 사용 중(8000)" 오류 시: 위 ②의 포트 종료 명령으로 잔여 프로세스 정리 후 재기동.

---

## 목차
0. [프로세스 시작 / 종료](#0-프로세스-시작--종료-quick-reference)
1. [전체 아키텍처](#1-전체-아키텍처)
2. [기술 스택](#2-기술-스택)
3. [데이터 흐름](#3-데이터-흐름)
4. [핵심 설계 결정](#4-핵심-설계-결정)
5. [Python RAG 서비스 — 파일별 정리](#5-python-rag-서비스--파일별-정리)
6. [Java lawSystem — 파일별 정리](#6-java-lawsystem--파일별-정리)
7. [DB 스키마](#7-db-스키마)
8. [REST API 명세](#8-rest-api-명세)
9. [환경 설정(환경변수)](#9-환경-설정환경변수)
10. [셋업 & 실행 절차](#10-셋업--실행-절차)
11. [트러블슈팅 기록](#11-트러블슈팅-기록)
12. [Phase 3 Step 2 — AI 3종 (요약·키워드·법리)](#12-phase-3-step-2--ai-3종-요약키워드법리)
13. [AI 분석 영속화 프로세스 (요청 → 호출 → 저장)](#13-ai-분석-영속화-프로세스-요청--호출--저장)
14. [다음 단계 / 남은 항목](#14-다음-단계--남은-항목)

---

## 1. 전체 아키텍처

머신은 2대로 분리되어 있다.

```
[DB PC]  192.168.0.A                     [코드 PC]  192.168.0.B
┌────────────────────┐                   ┌────────────────────────────────────┐
│  MariaDB :3306      │ ◀── LAN(JDBC) ────│  Java lawSystem                     │
│  - precedent        │ ◀── LAN(pymysql)──│  Python RAG 서비스 :8000            │
│  - precedent_keyword│                   │   ├ BGE-M3 (임베딩, sentence-transf) │
│  - similar_precedent│                   │   ├ Chroma (로컬 벡터DB, 파일)       │
│  - ai_analysis_*    │                   │   └ Ollama :11434 (로컬 LLM)         │
└────────────────────┘                   │  Java ──HTTP/1.1──▶ localhost:8000  │
                                          └────────────────────────────────────┘
```

- **MariaDB**: 판례 원문 보관소(단일 진실 공급원).
- **Python RAG 서비스**: DB를 읽어 벡터 색인 → 검색/생성 API 제공.
- **Java lawSystem**: 기존 시스템. RAG 서비스를 HTTP로 호출하고, 결과 `case_id`로 DB에서 본문을 조립.

---

## 2. 기술 스택

| 구분 | 기술 | 비고 |
|---|---|---|
| 벡터 DB | **Chroma** (PersistentClient) | 로컬 파일 저장, `hnsw:space=cosine` |
| 임베딩 | **BGE-M3** (`BAAI/bge-m3`) | 한국어 강함, sentence-transformers, 정규화 임베딩 |
| 키워드 검색 | **BM25Okapi** (rank-bm25) | 한글 단순 토크나이저 |
| 랭킹 융합 | **RRF** (Reciprocal Rank Fusion, K=60) | dense + BM25 결합 |
| LLM | **Ollama** (`qwen2.5:14b` 기본) | 로컬, `/api/chat` |
| API 서버 | **FastAPI + uvicorn** | HTTP/1.1, 포트 8000 |
| Java HTTP | **java.net.http.HttpClient** (JDK17 내장) | **HTTP/1.1 고정** |
| Java JSON | **Jackson** (databind + jsr310) | JSONL 파싱, RAG 통신 |
| DB | **MariaDB + Hibernate 6** | JDBC DAO + JPA 엔티티 병행 |
| Python 런타임 | **Python 3.13** (표준 빌드) | 3.8 불가(posthog 호환) |

---

## 3. 데이터 흐름

```
privacy_cases.jsonl (75건)
   │ ① Java PrecedentImporter (1회)  ─ JSONL → DB upsert
   ▼
MariaDB  precedent / precedent_keyword          ← 단일 진실 공급원
   │ ② Python db_loader → indexer (재색인)
   ▼
Chroma 벡터 인덱스 (chunk_type 단위: 판시사항/판결요지/참조조문/전문)
   │ ③ POST /search/similar-precedents
   │    → { external_case_id, similarity_score, matched_chunk_types, matched_excerpt }
   ▼
Java SimilarPrecedentsAnalysis
   │ ④ PrecedentDAO.findByExternalId(case_id) → DB에서 본문 조립
   ▼
List<PrecedentAnalysisResult>  (제목/요지/쟁점/유사도)
```

**핵심**: RAG는 "어떤 판례가 / 얼마나 유사한지"만 책임지고(본문 미포함), **본문은 항상 DB에서** 가져온다.
`external_case_id`(원천 case_id)가 Python ↔ Java의 계약 키이다.

---

## 4. 핵심 설계 결정

| 결정 | 내용 | 이유 |
|---|---|---|
| **마스터/등록 분리** | `precedent`(판례 카탈로그 마스터) vs 기존 `similar_precedent`(사건별 등록 인스턴스) | 카탈로그와 사건별 등록의 책임 분리 |
| **경량 응답** | RAG는 `case_id+score`만 반환, 본문은 Java가 DB 조회 | 본문 중복 제거, 단일 진실 공급원 유지 |
| **HTTP 마이크로서비스 분리** | BGE-M3/Chroma/Ollama는 Python 전용 → 독립 서비스 | JVM 임베딩 불가, 모델 재로딩 방지 |
| **결정적 PK** | `precedent_id = "PREC-" + external_case_id` | 재적재 시 idempotent upsert |
| **DB 설정 공유** | Python이 Java의 `LAWSYSTEM_DB_*` 환경변수 자동 재사용 (JDBC URL 파싱) | DB 접속정보 단일 관리 |
| **HTTP/1.1 고정** | Java HttpClient를 HTTP/1.1로 | uvicorn HTTP/2 미지원 → POST 본문 유실 방지 |
| **Graceful degradation** | RAG 장애 시 빈 목록 반환 | 기능 장애가 전체를 죽이지 않도록 |
| **chunk_type 가중치** | issues 1.15 / summary 1.10 / referenced_statutes 1.05 / full_text 1.00 | 판시사항·요지가 더 강한 검색 신호 |

---

## 5. Python RAG 서비스 — 파일별 정리

경로: `\\192.168.0.17\Code\Python\precedent\`

```
precedent/
├─ requirements.txt          # 의존성 (+ PyMySQL)
├─ .env.example              # 환경변수 템플릿
├─ rag/
│  ├─ config.py              # 설정 + DB접속 자동 해석
│  ├─ db_loader.py           # ★신규: precedent 테이블 → 청크
│  ├─ indexer.py             # 색인 (DB/JSONL 선택)
│  ├─ retriever.py           # dense + BM25 + RRF 검색
│  ├─ generator.py           # Ollama 답변 생성
│  ├─ service.py             # 검색+생성 묶음
│  └─ api.py                 # FastAPI 엔드포인트
└─ scripts/
   ├─ build_index.py         # 색인 실행
   ├─ ask.py                 # CLI 테스트
   └─ serve.py               # 서비스 기동
```

| 파일 | 역할 | 핵심 포인트 |
|---|---|---|
| `rag/config.py` | 모든 설정 상수 | `INDEX_SOURCE`(db/jsonl), `LAWSYSTEM_DB_URL`을 파싱해 `DB_HOST/PORT/NAME` 자동 도출, `PRECEDENT_DB_*`로 override 가능 |
| `rag/db_loader.py` | **신규.** precedent 테이블의 각 판례를 `chunk_type`(issues/summary/referenced_statutes/full_text) 단위로 쪼개 청크 리스트 생성. 반환 형태는 기존 JSONL 청크와 동일 | pymysql DictCursor, 키워드 join |
| `rag/indexer.py` | `build_index(reset, source)` — DB 또는 JSONL에서 청크 로드 → BGE-M3 임베딩 → Chroma 적재 | batch=8, 정규화 임베딩 |
| `rag/retriever.py` | `Retriever.search()`(dense+BM25+RRF+가중치), `search_grouped()`(case_id 단위 묶음) | 내부적으론 full text 보유 |
| `rag/generator.py` | `generate_answer()` — Ollama `/api/chat`, 한국어 법률 전문가 시스템 프롬프트, 인용 형식 `[사건번호(사건명)]` | temperature 0.2 |
| `rag/service.py` | `PrecedentRAGService.search()/answer()` | retriever+generator 묶음 |
| `rag/api.py` | FastAPI 앱. `/healthz`, `/search/similar-precedents`, `/answer`. `Hit`을 **경량화**(case_id+score+matched 정보) | min-max 점수 정규화 |
| `scripts/build_index.py` | `build_index(reset=True)` 실행 | 데이터 변경 시 재실행 |
| `scripts/serve.py` | `uvicorn.run(... :8000)` | 서비스 상시 기동 |
| `scripts/ask.py` | CLI 스트리밍 질의 테스트 | |

---

## 6. Java lawSystem — 파일별 정리

경로: `\\192.168.0.17\Code\java\Distribute\`

### 신규 파일
| 파일 | 역할 |
|---|---|
| `lawSystem/precedent/Precedent.java` | 판례 마스터 **도메인** 객체. `buildPrecedentId()`로 결정적 PK 생성 |
| `lawSystem/jpa/entity/Precedent.java` | 판례 마스터 **JPA 엔티티**. keywords = `@ElementCollection`(precedent_keyword) |
| `lawSystem/db/dao/PrecedentDAO.java` | JDBC DAO. `upsert()`(ON DUPLICATE KEY UPDATE), `findByExternalId()`, `findAllExternalIds()`, `count()` |
| `lawSystem/precedent/PrecedentImporter.java` | **JSONL → DB 1회성 적재기**. Jackson 파싱, `yyyyMMdd` 날짜 처리, upsert |
| `lawSystem/precedent/PrecedentRagClient.java` | RAG 서비스 **HTTP 클라이언트**. `searchSimilarPrecedents()`, `isHealthy()`. **HTTP/1.1 고정** |
| `lawSystem/precedent/PrecedentHit.java` | RAG 검색 결과 DTO (caseId, score, matchedChunkTypes, matchedExcerpt) |
| `lawSystem/precedent/RagConnectionTest.java` | 연결 검증용 임시 실행 클래스(삭제 가능) |

### 수정 파일
| 파일 | 변경 내용 |
|---|---|
| `lawSystem/ai/SimilarPrecedentsAnalysis.java` | **더미 → 실제 RAG**. `searchSimilarPrecedents(caseId, summary)` 시그니처 유지. RAG 호출 → `PrecedentDAO`로 본문 조립. 예외 시 빈 목록(graceful) |
| `pom.xml` | `jackson-databind`, `jackson-datatype-jsr310` 2.17.2 추가 |
| `src/main/resources/schema.sql` | `precedent`, `precedent_keyword` 테이블 + `similar_precedent.precedent_ref_id` 컬럼 |
| `src/main/resources/META-INF/persistence.xml` | `Precedent` 엔티티 등록 |
| `lawSystem/db/DBInitializer.java` | `dropAllTables()` 목록에 precedent 추가 |

### 기존 (변경 없음, 연계됨)
- `lawSystem/ai/PrecedentAnalysisResult.java` — 결과 객체 (생성자 9인자)
- `lawSystem/ai/AIAnalysisFunction.java`, `AnalysisType.java`
- `lawSystem/legalCase/SimilarPrecedent.java` (도메인), 동명 JPA 엔티티, `SimilarPrecedentDAO.java`
- `lawSystem/db/DBA.java` — JDBC 연결 (`LAWSYSTEM_DB_*` 환경변수 우선)

---

## 7. DB 스키마

```sql
-- 판례 마스터 카탈로그 (단일 진실 공급원)
CREATE TABLE IF NOT EXISTS precedent (
    precedent_id        VARCHAR(64) PRIMARY KEY,          -- "PREC-<external_case_id>"
    external_case_id    VARCHAR(64) NOT NULL UNIQUE,      -- 원천 case_id (RAG 역참조 키)
    case_name           VARCHAR(255),
    case_number         VARCHAR(100),
    court_name          VARCHAR(150),
    court_type_code     VARCHAR(20),
    decision_date       DATE,
    case_type           VARCHAR(50),
    judgment_type       VARCHAR(50),
    issues              TEXT,        -- 판시사항
    summary             TEXT,        -- 판결요지
    referenced_statutes TEXT,        -- 참조조문
    referenced_cases    TEXT,        -- 참조판례
    full_text           LONGTEXT,    -- 전문
    domain              VARCHAR(50),
    source              VARCHAR(100),
    source_url          VARCHAR(500),
    imported_at         DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_precedent_domain (domain)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS precedent_keyword (
    precedent_id VARCHAR(64)  NOT NULL,
    keyword      VARCHAR(100) NOT NULL,
    PRIMARY KEY (precedent_id, keyword),
    CONSTRAINT fk_precedent_keyword FOREIGN KEY (precedent_id)
        REFERENCES precedent(precedent_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 사건별 등록 판례가 어느 마스터에서 왔는지 추적(선택)
ALTER TABLE similar_precedent
    ADD COLUMN IF NOT EXISTS precedent_ref_id VARCHAR(64) NULL AFTER case_id;
```

> `precedent`(마스터) 와 기존 `similar_precedent`(사건별 인스턴스)는 별개 테이블이다.

---

## 8. REST API 명세

베이스 URL: `http://localhost:8000`

### `GET /healthz`
```json
{ "status": "ok", "collection": "privacy_cases" }
```

### `POST /search/similar-precedents`
**요청**
```json
{
  "query": "공개된 개인정보를 동의 없이 이용하면 위법인가?",
  "case_id": "선택(참고용)",
  "top_k": 5,
  "similarity_threshold": 0.0
}
```
**응답**
```json
{
  "hits": [
    {
      "case_id": "240901",
      "similarity_score": 1.0,
      "matched_chunk_types": ["summary", "issues", "referenced_statutes", "full_text"],
      "matched_excerpt": "[원본 정보]\n사건명: ...\n[판결요지]\n..."
    }
  ]
}
```
> `similarity_score`는 **min-max 정규화**(이 결과셋 내 1등=1.0, 꼴등=0). 절대 유사도가 아니라 **상대 순위**.

### `POST /answer`
**요청** `{ "query": "...", "top_k": 5 }`
**응답** `{ "answer": "LLM 생성 답변", "hits": [ ...위와 동일... ] }`
> Ollama가 실제 생성하므로 수 초~수십 초 소요.

---

## 9. 환경 설정(환경변수)

### Python `.env` (코드 PC)
```ini
INDEX_SOURCE=db                  # db(precedent 테이블) | jsonl
EMBED_MODEL=BAAI/bge-m3
EMBED_DEVICE=cpu                 # NVIDIA GPU면 cuda (RTX 50번대는 torch cu128 필요)
OLLAMA_HOST=http://localhost:11434
OLLAMA_MODEL=qwen2.5:14b         # ollama list 모델명과 일치

# DB 는 아래 PRECEDENT_DB_* 를 적지 않으면 LAWSYSTEM_DB_* 를 자동 재사용한다.
# PRECEDENT_DB_HOST / PORT / USER / PASSWORD / NAME  (override 시에만)
```

### 시스템 환경변수 (Java·Python 공용)
```
LAWSYSTEM_DB_URL      = jdbc:mariadb://192.168.0.A:3306/law_system?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul
LAWSYSTEM_DB_USER     = lawuser
LAWSYSTEM_DB_PASSWORD = ********
LAWSYSTEM_DB_DATABASE = law_system
PRECEDENT_RAG_URL     = http://localhost:8000   (Java, 기본값이라 보통 생략)
```
> Python `config.py`가 `LAWSYSTEM_DB_URL`(JDBC URL)을 파싱해 host/port/db를 자동 도출한다.

### DB PC 원격 허용 (1회)
1. `my.ini` → `bind-address = 0.0.0.0` 후 재시작
2. `CREATE USER 'lawuser'@'192.168.0.B' ...; GRANT ALL ON law_system.* ...; FLUSH PRIVILEGES;`
3. 방화벽 인바운드 TCP **3306** 허용

---

## 10. 셋업 & 실행 절차

### 최초 1회
1. **DB PC**: 원격 허용 (위 9절)
2. **코드 PC**: `setx LAWSYSTEM_DB_*` (새 터미널/IDE 재시작)
3. **Ollama**: 설치 → `ollama pull qwen2.5:14b`
4. **Python**: 로컬 디스크에 venv
   ```powershell
   Set-Location \\192.168.0.17\Code\Python\precedent
   py -3.13 -m venv C:\rag-venv
   C:\rag-venv\Scripts\Activate.ps1
   pip install -r requirements.txt
   Copy-Item .env.example .env   # 편집
   ```
5. **DB 적재 (Java)**: `lawSystem.precedent.PrecedentImporter` 실행 → 75건
6. **색인 (Python)**: `python -m scripts.build_index` (첫 실행 시 BGE-M3 2GB 다운로드)

### 매번
```powershell
# RAG 서비스
Set-Location \\192.168.0.17\Code\Python\precedent
C:\rag-venv\Scripts\Activate.ps1
python -m scripts.serve         # http://localhost:8000
# 그 후 Java 앱 실행
```
> 데이터 변경 시에만 5(적재)+6(색인) 재수행.

### 검증
- `GET /healthz` → ok
- `/docs`에서 `POST /search/similar-precedents` Try it out
- Java `RagConnectionTest` 실행 → `결과 수: 5` + 판례 본문

---

## 11. 트러블슈팅 기록

실제로 겪고 해결한 이슈들.

| 증상 | 원인 | 해결 |
|---|---|---|
| `Torch not compiled with CUDA enabled` | `.env` `EMBED_DEVICE=cuda` + CPU torch | cpu로 변경 또는 cu128 torch 설치 |
| `sm_120 is not compatible` (RTX 5070 Ti) | Blackwell는 CUDA 12.8 필요 | `pip install torch --index-url .../cu128` |
| `TypeError: 'type' object is not subscriptable` (posthog) | **Python 3.8** (`dict[...]`은 3.9+) | Python 3.13으로 venv 재생성 |
| PowerShell 한글 깨짐 (`ìë³¸`) | Win PowerShell 5.1이 응답을 ISO-8859-1로 디코딩 | pwsh 7 사용 / `/docs` / 파일은 UTF-8 정상 |
| `422 Field required (body)` (PowerShell) | `$body` 미정의/`-Method Post` 누락 | body 정의+호출 같은 창, `-Method Post` |
| `422 Field required (body)` (Java) | **HttpClient 기본 HTTP/2** ↔ uvicorn HTTP/1.1, POST 본문 유실 | `.version(HTTP_1_1)` 고정 ✅ |
| `405 Method Not Allowed` | POST 엔드포인트를 브라우저 주소창(GET)으로 접근 | `/docs`에서 호출 |

---

## 12. Phase 3 Step 2 — AI 3종 (요약·키워드·법리)

웹서비스용 AI 3종을 Step 1과 동일 패턴(Python 작성 → Java 연결)으로 추가했다.

| 기능 | 방식 | 엔드포인트 | Java 기능 클래스 | Java 클라이언트 |
|---|---|---|---|---|
| 내용 요약 | LLM 단순 | `POST /llm/summarize` | `CaseSummary` | `PrecedentRagClient.summarize()` |
| 키워드 추출 | LLM 단순 | `POST /llm/extract-keywords` | `CaseKeywordsExtract` | `.extractKeywords()` |
| 법리 설명 | **RAG 결합** | `POST /rag/analyze-rules` | `LegalRuleAnalysis` | `.analyzeLegalRules()` |

**Python**: `rag/llm.py`(JSON 강제 호출 헬퍼) + `rag/tasks/{summarize,keywords,legal_rules}.py`.
LLM 출력은 Ollama `format:"json"` 으로 강제하고 파싱 폴백을 둔다. `legal_rules` 는 `retriever.search_grouped()` 로 판례를 검색해 컨텍스트로 주는 RAG 결합 방식이며 `cited_cases`(external_case_id)를 함께 반환한다.

**Java**: `SummaryDto`, `LegalRulesDto`. 각 기능 클래스는 더미를 제거하고 클라이언트를 호출하며, **AI 서비스 장애 시 빈/폴백 결과**(graceful degradation)를 낸다. 검증은 `lawSystem.precedent.Step2Test`.

- 제외: 문서 초안(`DocumentDraft`), 변호사 추천(`RecommendLawyers`).
- 응답 지연: 요약/법리는 LLM 생성이라 수 초~수십 초 → 클라이언트 타임아웃 120초.

---

## 13. AI 분석 영속화 프로세스 (요청 → 호출 → 저장)

> "기존 설계는 **분석 요청 → 분석 기능 호출 → 분석 결과 저장** 프로세스인데, 이를 위해 무엇을 어떻게 고쳤는가"에 대한 답.

### 설계 의도
```
AIAnalysisRequest ──sendToAIService──▶ AIAnalysisFunction.execute() ──▶ AIAnalysisResult
  (ai_analysis_request 테이블)                                          (ai_analysis_result 테이블)
   DAO: AIAnalysisRequestDAO                          DAO: AIAnalysisResultDAO
   FK: ai_analysis_result.ai_request_id → ai_analysis_request (NOT NULL)
```

### 고치기 전(문제)
1. `AIAnalysisResult.saveResult()` 가 **스텁**(null 체크만, DB 미저장).
2. `execute()` 의 switch 가 **베이스 더미** 메서드를 호출 → 서브클래스의 실제 RAG/LLM 로직이 무시됨(이름·시그니처가 달라 override 가 아니었음).
3. 결과의 `aiRequestId` 가 **가짜 ID** → 저장 시 FK 위반 위험.
→ 결국 `ai_analysis_request`/`ai_analysis_result` 에 **아무것도 저장되지 않았음**.

### 고친 부분 (3곳)
| # | 파일 | 변경 |
|---|---|---|
| A | `AIAnalysisFunction.java` | `createResult` private→**protected** (서브클래스가 재사용, `aiRequestId = request.getAiAnalysisRequestId()`) |
| A | `CaseSummary`/`CaseKeywordsExtract`/`LegalRuleAnalysis`/`SimilarPrecedentsAnalysis` | 베이스 switch 대상 메서드를 **override** 하여 실제 RAG/LLM 실행. 결과는 **실제 요청 ID** 로 생성 |
| B | **신규** `AIAnalysisService.java` | 오케스트레이터: ① 요청 생성+`requestDAO.insert` → ② `request.sendToAIService(function)` → ③ `requestDAO.updateStatus` + `resultDAO.insert` |
| C | 호출부(웹서비스 계층) | `aiService.analyze(requesterId, caseId, type, prompt, function)` 한 번으로 요청–호출–저장 완결 |

### 고친 후(정상 흐름)
```java
AIAnalysisResult saved = new AIAnalysisService().analyze(
        requesterId,                 // member_id 또는 null
        caseId,                      // legal_case 에 존재해야 함(FK)
        AnalysisType.SIMILAR_PRECEDENTS,
        query,                       // prompt = 입력 텍스트/검색 쿼리
        new SimilarPrecedentsAnalysis());
// → ai_analysis_request 1행, ai_analysis_result 1행 저장 (FK 정합)
```

### FK 전제
- `ai_analysis_request.requester_id` → `member` (nullable, SET NULL): 실제 member 또는 null
- `ai_analysis_result.case_id` → `legal_case` (nullable, SET NULL): 실제 case 또는 null
- `ai_analysis_result.ai_request_id` → `ai_analysis_request` (**NOT NULL**): 먼저 insert 된 요청 ID

### 참고
- `Main.java` 는 인메모리 데모(엔티티 DB 미적재)라 영속화 경로를 타지 않는다. 실제 저장은 사건이 DB에 있는 웹서비스 흐름에서 `AIAnalysisService` 로 수행한다.
- 구조화 데이터(쟁점/법령/키워드)는 `summary_text`(대표 텍스트)로 저장된다. 전체 구조 보존이 필요하면 `summary_text` 에 JSON 병기로 확장.

---

## 14. 다음 단계 / 남은 항목

- **검색 품질**: `full_text` 가 한 청크로 임베딩되어 BGE-M3 최대 토큰에서 잘릴 수 있음 → 전문 슬라이딩 분할.
- **구조화 저장**: 키워드/법령/쟁점을 `summary_text` 에 JSON 병기 또는 전용 컬럼/테이블.
- **웹 계층 연결**: 실제 화면/REST 컨트롤러에서 `AIAnalysisService.analyze(...)` 호출로 통일.
- **JPA `Precedent` 엔티티**: 등록만 됨(실사용은 JDBC DAO). 필요 시 활성화.
- **임시 클래스 정리**: `RagConnectionTest`, `Step2Test` 는 검증용 — 유지/삭제 선택.

---

*문서 갱신: Phase 3 Step 1 + Step 2 완료 시점 기준.*
