# AI 기반 법률 사무 플랫폼

사건 수임·관리 워크플로우와 **판례 RAG · LLM**을 결합한 법률 사무 통합 웹 플랫폼.
분산프로그래밍1 과제 — 웹(Java) · AI(Python) · DB · LLM을 **독립 프로세스**로 구성한 3-tier 시스템.

![시스템 아키텍처](docs/architecture.png)

> 전체 설계는 [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md), RAG 상세는 [`docs/RAG_통합정리.md`](docs/RAG_통합정리.md) 참고.

---

## 📦 저장소 구조

```
law-platform/
├── web/    Spring Boot 웹앱 (사건·증거·변호사검색·상담·수임·진행상황·AI 연동)
├── rag/    Python RAG 서비스 (FastAPI · BGE-M3 · Chroma · BM25/RRF · Ollama 연동)
└── docs/   아키텍처 문서 · 다이어그램 · 발표자료
```

| 구성요소 | 런타임 | 포트 | 역할 |
|---|---|---|---|
| `web/` | Spring Boot 3.3 (Java 17) | `:8080` | 화면(Thymeleaf) + 업무 로직 + JPA |
| `rag/` | FastAPI / uvicorn (Python 3.13) | `:8000` | 판례 검색·요약·키워드·법리 분석 |
| Ollama | 로컬 LLM 서버 | `:11434` | qwen2.5 / gemma 등 |
| MariaDB | `law_system_jpa` | `:3306` | 단일 진실 공급원 (별도 PC 가능) |

세 구성요소는 환경변수 **`LAWSYSTEM_DB_URL`** 로 같은 DB를 공유한다.

---

## 🛠️ 기술 스택

- **웹**: Spring Boot 3.3 · Spring MVC · Spring Data JPA(Hibernate 6) · Thymeleaf
- **인증/권한**: HttpSession + `LoginInterceptor` + `@RoleAllowed` · BCrypt(spring-security-crypto)
- **AI/RAG**: BGE-M3 임베딩 · Chroma 벡터DB · BM25Okapi · RRF 융합 · Ollama
- **연동**: `java.net.http.HttpClient`(HTTP/1.1 고정) + Jackson ↔ FastAPI · pymysql
- **DB**: MariaDB (JPA가 스키마 권한 `ddl-auto=update`)

---

## 🚀 실행 방법 (시작 순서)

> 자세한 안전 시작/종료 절차는 [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) 최상단 참고.

### 0. 사전 준비
- MariaDB에 `law_system_jpa` 스키마 생성 (테이블은 JPA가 자동 생성)
- 시스템 환경변수 설정 (웹·RAG 공통):
  ```
  LAWSYSTEM_DB_URL=jdbc:mariadb://<DB_PC_IP>:3306/law_system_jpa?useUnicode=true&characterEncoding=utf8
  LAWSYSTEM_DB_USER=<user>
  LAWSYSTEM_DB_PASSWORD=<password>
  ```

### 1. Ollama (LLM)
```powershell
ollama pull qwen2.5:7b      # 또는 gemma2 등
ollama serve                 # :11434
```

### 2. Python RAG 서비스 (`rag/`)
```powershell
# 최초 1회: 가상환경 (네트워크 공유가 아닌 로컬 경로 권장)
python -m venv C:\rag-venv
C:\rag-venv\Scripts\Activate.ps1
pip install -r rag/requirements.txt

# 환경설정: .env.example 복사 후 값 조정 (DB는 LAWSYSTEM_DB_URL 자동 재사용)
copy rag\.env.example rag\.env

cd rag
python scripts/build_index.py     # 최초 1회: DB→Chroma 인덱스 빌드
python scripts/serve.py           # FastAPI :8000  (health: /healthz, 문서: /docs)
```

### 3. Spring Boot 웹앱 (`web/`)
```powershell
cd web
mvn spring-boot:run               # http://localhost:8080
```
시작 시 `@Order` 시더가 **회원 → 판례 → 담당사건**을 멱등 생성.

### 🔑 테스트 계정 (시드, 비밀번호 `1234`)
| 역할 | 아이디 |
|---|---|
| 의뢰인 | `seed-client` |
| 대표변호사 | `seed-partner` |
| 소속변호사 | `seed-associate`, `seed-assoc2`, `seed-assoc3` |
| 사무직원 | `seed-staff` |

---

## ✨ 주요 기능

- **인증·역할 권한**: 의뢰인 / 대표·소속 변호사 / 사무직원, 서버단 강제(`@RoleAllowed`)
- **사건 관리**: 사건 등록 · 증거 업로드 · AI 분석 이력
- **변호사 찾기**: 키워드·전문분야·지역 검색 (사건 키워드 재사용)
- **상담/수임**: 상태머신 기반 (수임 조건 전달·조정·확정 → `RETAINED`)
- **진행상황 공유/열람**: 변호사 등록 → 의뢰인 열람
- **AI(요청→호출→저장)**: 사건 요약 · 키워드 추출 · 법리 설명(RAG) · 유사판례 검색

---

## 🧠 RAG 동작 원리 (요약)

1. **DB가 원본**(`precedent` 테이블), **Chroma는 검색 인덱스** — 단일 진실 공급원
2. 판례를 `chunk_type`(판시사항·판결요지·참조조문·전문)으로 분할·임베딩
3. 검색 = **Dense(BGE-M3) + Sparse(BM25)** → **RRF(K=60)** 융합 → `case_id` 그룹핑 → 정규화(0~1)
4. 검색 결과는 **식별자+점수만** 반환, 본문은 Java가 DB에서 조립 (근거가 항상 원본)
5. LLM은 Ollama `/api/chat`(`format="json"`), 실패 시 산문 폴백 — **graceful degradation**

---

## 📄 문서

| 파일 | 내용 |
|---|---|
| [`docs/ARCHITECTURE.md`](docs/ARCHITECTURE.md) | 전체 시스템 설계·데이터 모델·역할 매트릭스·시작/종료 절차 |
| [`docs/RAG_통합정리.md`](docs/RAG_통합정리.md) | RAG 파이프라인·검색 알고리즘·API 명세 |
| [`docs/발표자료_초안.md`](docs/발표자료_초안.md) | 발표 슬라이드 초안(기술 심화판, 21장) |
| [`docs/발표_10분_진행안.md`](docs/발표_10분_진행안.md) | 10분 발표 진행안(데모 + 기술스택 + AI 활용법) |
