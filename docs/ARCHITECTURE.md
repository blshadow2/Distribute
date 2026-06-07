# AI 기반 법률 사무 플랫폼 — 전체 구조 및 작동 원리

> 의뢰인·변호사 간 **사건 수임/관리** 워크플로우에 **판례 RAG 검색 + LLM(요약·키워드·법리)** AI를 결합한 웹 플랫폼.
> Java(Spring Boot) 웹앱 · Python(FastAPI) AI 서비스 · MariaDB(JPA 스키마) 3개 런타임으로 구성된다.

---

## 1. 전체 아키텍처

```
┌──────────────────────────── 코드 PC (localhost) ────────────────────────────┐
│                                                                              │
│  [브라우저] ──HTTP──▶ [Spring Boot 웹앱 :8080]                                │
│                         │  - Thymeleaf 화면(서버 렌더링)                       │
│                         │  - @RestController (AI JSON API)                    │
│                         │  - 세션 인증 + 역할 권한(@RoleAllowed)               │
│                         │                                                     │
│                         ├── JPA(Spring Data) ──▶ MariaDB(law_system_jpa) ◀────┼─┐
│                         │                          (회원·사건·상담·수임·AI결과)   │ │
│                         │                                                     │ │
│                         └── HTTP/1.1 ──▶ [Python RAG :8000]                   │ │
│                                            ├ BGE-M3(임베딩) + Chroma(벡터DB)   │ │
│                                            ├ Ollama :11434 (LLM)              │ │
│                                            └ pymysql ──▶ MariaDB ─────────────┼─┘
└──────────────────────────────────────────────────────────────────────────────┘
         DB(MariaDB)는 별도 PC일 수 있음(LAWSYSTEM_DB_URL 로 원격 지정).
```

- **Java**: 사용자 진입점. 화면·인증·사건/상담/수임 로직 + AI 호출 오케스트레이션.
- **Python**: 판례 검색(RAG)·LLM 생성 전담. Java가 HTTP로 호출.
- **MariaDB**: 단일 진실 공급원. Java(JPA)·Python(pymysql) 모두 같은 DB를 본다.

---

## 2. 기술 스택

| 구분 | 기술 |
|---|---|
| 웹 프레임워크 | Spring Boot 3.3 (Spring MVC + Spring Data JPA + Thymeleaf) |
| 인증 | 경량 **HttpSession** + `LoginInterceptor` + `@RoleAllowed` (Spring Security 미사용), 비밀번호 **BCrypt**(spring-security-crypto) |
| ORM/DB | Hibernate 6(jakarta), MariaDB, `ddl-auto=update` |
| Java↔Python | `java.net.http.HttpClient` (**HTTP/1.1 고정**) + Jackson |
| 임베딩 | **BGE-M3** (sentence-transformers) |
| 벡터DB | **Chroma** (로컬, cosine) |
| 키워드검색 | BM25 + **RRF** 융합 |
| LLM | **Ollama** (qwen2.5 / gemma 등) |
| AI API | FastAPI + uvicorn (:8000) |
| Python런타임 | Python 3.13 |

---

## 3. 실행 구성

| 프로세스 | 포트 | 역할 | 기동 |
|---|---|---|---|
| MariaDB | 3306 | DB(law_system_jpa) | 상시(별도 PC 가능) |
| Ollama | 11434 | 로컬 LLM | 백그라운드 |
| Python RAG | 8000 | 검색·LLM API | `python -m scripts.serve` |
| Spring Boot | 8080 | 웹앱 | `LawSystemWebApplication` |

**공유 환경변수** (Java·Python 공통, DB 단일화 핵심)
```
LAWSYSTEM_DB_URL = jdbc:mariadb://<DB_IP>:3306/law_system_jpa?...
LAWSYSTEM_DB_USER / LAWSYSTEM_DB_PASSWORD / LAWSYSTEM_DB_DATABASE
PRECEDENT_RAG_URL = http://localhost:8000   (Java→Python)
OLLAMA_MODEL / EMBED_DEVICE 등             (Python .env)
```
- Spring Boot `application.yml`이 `LAWSYSTEM_DB_URL`을, Python `config.py`가 같은 URL을 파싱 → **세 구성요소가 같은 DB**.

---

## 4. 작동 원리 (핵심 흐름)

### 4.1 인증 · 세션
```
요청 → LoginInterceptor.preHandle
  ① 세션에 LoginMember 없음 → /login 리다이렉트
로그인: AuthController → AuthService.login(email, pw)
  → MemberRepository.findByEmail → BCrypt 대조
  → 세션에 LoginMember(memberId, name, viewRole) 저장
```
- `viewRole`은 회원 구체 타입으로 결정: Client→CLIENT, PartnerLawyer→PARTNER, AssociateLawyer→ASSOCIATE, Staff→STAFF.
- 가입은 **의뢰인(Client)** 만 폼 제공. 변호사/직원은 `DataSeeder`가 시드.

### 4.2 역할 권한
```
@RoleAllowed("CLIENT") 등을 컨트롤러/메서드에 부착
 → LoginInterceptor 가 세션 viewRole 과 비교
    페이지: 불일치 → /access-denied(403)
    /api/** : 불일치 → 403 JSON
```
- 화면은 `th:if`로 메뉴 숨김(UX) + 인터셉터로 실제 차단(보안) = 2중 방어.

### 4.3 사건 관리
```
의뢰인: /cases 등록(client_id=세션) → /cases/{id} 상세
        ├ 증거 업로드(MultipartFile → ./uploads/{caseId}/, 경로만 DB)
        └ 사건 키워드(AI 추출 결과) 표시
변호사: /my-cases (배당된 담당 사건)
```

### 4.4 AI 기능 (RAG + LLM)
**판례 데이터 파이프라인(원본=DB, 인덱스=Chroma)**
```
privacy_cases.jsonl ──PrecedentDataLoader(JPA)──▶ DB.precedent
DB.precedent ──Python build_index──▶ Chroma 벡터 인덱스
```
**검색·생성 호출**
```
[브라우저] /api/ai/* → AiController → AiWebService
  요약(/summary)      : AIAnalysisService → CaseSummary → Python /llm/summarize
  키워드(/keywords)   : Python /llm/extract-keywords → 사건/변호사 specialty에 저장
  유사판례(/similar..): Python /search → case_id+score → DB에서 본문 조립
  법리(/legal-rules)  : Python /rag/analyze-rules (판례 검색+LLM, JSON 실패 시 산문 폴백)
```
**요청–호출–저장(영속화)**
```
AIAnalysisService.analyze / AiPersistenceService.save
  ① ai_analysis_request 저장  ② 기능 실행  ③ ai_analysis_result 저장(case_id 연결)
→ 사건 상세 "AI 결과 이력"에 누적 표시
```
> 역할 제한: 요약·유사판례 = 변호사 전용, 키워드·법리 = 의뢰인 포함 전체.

### 4.5 변호사 검색 (키워드)
```
/lawyers?keyword=&specialty=&region=  또는  caseId(저장된 사건 키워드)
 → LawyerService.search: 변호사 specialty/이름/소개/사무소에 토큰 매칭(OR), 업무량 적은 순
사건 키워드 흐름: AI 키워드 추출 → 사건에 저장 → 변호사 찾기에서 그 키워드로 검색
```

### 4.6 상담 상태머신
```
사무직원: 상담 일정 등록(ScheduleStatus.AVAILABLE)
의뢰인: 상담 신청(REQUESTED, 일정 RESERVED)
사무직원: 승인(APPROVED) / 거절(REJECTED, 일정 AVAILABLE 복원)
의뢰인: 거절/취소 건 삭제
```

### 4.7 수임 상태머신
```
REQUEST_SENT ──(대표 조건 전달)──▶ CONDITION_SENT
   ├─(의뢰인 수락)──▶ CONDITION_ACCEPTED ──(대표 수임확정)──▶ RETAINED(사건 RETAINED+담당변호사 지정)
   ├─(의뢰인 거절)──▶ REJECTED
   ├─(대표 거절)───▶ REJECTED
   └─(의뢰인 조정요청+메시지)──▶ CONDITION_ADJUSTMENT_REQUESTED ──(대표 재제안)──▶ CONDITION_SENT (반복)
의뢰인: 거절된 요청 삭제(조건 cascade)
```

### 4.8 진행상황 공유/열람
```
소속변호사: /progress-share → 담당 사건 선택 + 진행상태/설명/최근조치/요청자료 입력 → ProgressionRecord 저장
의뢰인:     /progress → 본인 사건들의 진행상황 열람(요청 자료 포함)
```

---

## 5. Java 구조 (Spring Boot)

패키지 루트: `lawSystem`

```
web/                         ← 웹 계층(Spring Boot)
 ├ LawSystemWebApplication   @SpringBootApplication, @EntityScan(jpa.entity), @EnableJpaRepositories
 ├ controller/  AuthController, AiController, CaseController, CaseApiController,
 │              LawyerController, LawyerCaseController, ConsultationController,
 │              RetainerController, PlaceholderPageController
 ├ service/     AuthService, AiWebService, AiPersistenceService, CaseService,
 │              LawyerService, ConsultationService, RetainerService
 ├ repository/  (Spring Data JpaRepository) Member/Case/Lawyer/Evidence/
 │              AiResult/AiRequest/Precedent/ConsultationSchedule/ConsultationRequest/RetainerRequest
 ├ dto/         요청·응답 DTO (엔티티 직접 노출 금지)
 ├ auth/        SessionConst, LoginMember, RoleAllowed, LoginInterceptor
 ├ config/      WebConfig(인터셉터 등록)
 └ DataSeeder / PrecedentDataLoader / CaseSeeder   (@Order 1·2·3 시작 시드)

jpa/entity/                  ← JPA 엔티티(테이블 매핑, 스키마 원본)
 Member(추상,JOINED)→Client/Lawyer(추상)→Partner/Associate, Staff,
 LegalCase, Evidence, CaseDocument, ProgressionRecord, CaseInfo,
 ConsultationRequest/Schedule, RetainerRequest/Condition,
 VerificationResult, ElectronicSignature,
 AIAnalysisRequest/Result/Function, Precedent

ai/                          ← AI 도메인(기능 객체 + 결과)
 AIAnalysisFunction(베이스), AnalysisType, AIRequestStatus, AIAnalysisService,
 CaseSummary, CaseKeywordsExtract, LegalRuleAnalysis, SimilarPrecedentsAnalysis,
 (DocumentDraft, RecommendLawyers), 결과: CaseAnalysisReport/LegalRuleAnalysisResult/
 PrecedentAnalysisResult ...

precedent/                   ← 판례 + RAG 연동
 Precedent(도메인), PrecedentDAO(JDBC), PrecedentImporter,
 PrecedentRagClient(HTTP/1.1), PrecedentHit, SummaryDto, LegalRulesDto,
 RagConnectionTest, Step2Test

legalCase/, consultation/, retainer/   ← 도메인 enum/클래스 (CaseCategory/Status 등)
db/ , db.dao/                ← JDBC(DBA, DAO), DBInitializer  ※콘솔/AI 영속화 일부에서 사용(레거시)
jpa/ JpaManager, BaseRepository           ※콘솔 데모용 수제 JPA (웹은 Spring Data 사용)
Main.java                    ← 콘솔 데모(인메모리, 웹과 독립)
```

**계층 패턴(웹)**: `@Controller/@RestController → @Service(@Transactional, DTO 매핑) → JpaRepository → @Entity`.

---

## 6. Python 구조 (RAG 서비스)

경로: `\\192.168.0.17\Code\Python\precedent\`
```
rag/
 ├ config.py      설정 + LAWSYSTEM_DB_URL 파싱(DB 접속 자동)
 ├ db_loader.py   precedent 테이블 → chunk_type 단위 청크
 ├ indexer.py     build_index: DB→임베딩→Chroma
 ├ retriever.py   dense(BGE-M3)+BM25+RRF, case_id 그룹핑
 ├ generator.py   Ollama /api/chat (RAG 답변)
 ├ llm.py         Ollama JSON 강제 호출 + 폴백 파서
 ├ tasks/         summarize.py / keywords.py / legal_rules.py(RAG결합, 산문 폴백)
 ├ service.py     검색+생성 묶음
 └ api.py         FastAPI: /healthz /search/similar-precedents /answer
                  /llm/summarize /llm/extract-keywords /rag/analyze-rules
scripts/ build_index.py · serve.py · ask.py
```

**REST 계약(요지)**
| 엔드포인트 | 입력 | 출력 |
|---|---|---|
| `POST /search/similar-precedents` | query, top_k | hits[{case_id, similarity_score, matched_chunk_types, excerpt}] |
| `POST /llm/summarize` | text | {summary, main_issues[], timeline} |
| `POST /llm/extract-keywords` | text, max_keywords | {keywords[]} |
| `POST /rag/analyze-rules` | query, top_k | {issue_summary, applicable_law, legal_explanation, related_statutes[], cited_cases[]} |

---

## 7. DB 스키마 (JPA 엔티티 = 스키마 원본)

DB: **law_system_jpa** (Hibernate `ddl-auto=update` 가 엔티티 기준 생성)

**회원(JOINED 상속)**: `member`(PK member_id) ← `client`/`lawyer`(←`partner_lawyer`/`associate_lawyer`)/`staff` (모두 member_id 공유 PK). `lawyer_specialty`(전문분야).

**사건**: `legal_case`(client_id FK, assigned_lawyer_id FK, case_status), `case_keyword`, `evidence`, `case_document`, `progression_record`, `case_info`.

**상담/수임**: `consultation_schedule`, `consultation_request`, `retainer_request`, `retainer_condition`.

**인증/서명**: `verification_result`, `electronic_signature`.

**AI**: `ai_analysis_request` ──FK──▶ `ai_analysis_result`(case_id→legal_case), `ai_analysis_function`.

**판례(RAG 원본)**: `precedent`(external_case_id UNIQUE), `precedent_keyword`.

주요 관계
```
member 1─1 client/lawyer/staff (JOINED)
client 1─N legal_case ─N─1 lawyer(assigned)
legal_case 1─N evidence / case_document / progression_record / similar_precedent
client/lawyer ─N consultation_request ─N─1 consultation_schedule
client/lawyer ─N retainer_request 1─N retainer_condition
ai_analysis_request 1─N ai_analysis_result ─N─1 legal_case
```

> ⚠️ 콘솔용 `schema.sql`(JDBC)과 JPA 스키마는 상속 테이블 구조가 달라 호환되지 않는다. **웹은 JPA 스키마(law_system_jpa)가 단일 권한**이며, 콘솔 `DBInitializer`를 이 DB에 실행하지 않는다.

---

## 8. 역할별 기능

| 기능 | 의뢰인 | 대표변호사 | 소속변호사 | 사무직원 |
|---|:--:|:--:|:--:|:--:|
| 로그인/대시보드 | ● | ● | ● | ● |
| 사건 등록/상세/증거 | ● | | | |
| 변호사 검색(키워드) | ● | | | |
| 상담 신청 | ● | | | |
| 상담 일정/응답 | | | | ● |
| 수임 요청/조건 수락·거절·**조정요청** | ● | | | |
| 수임 조건전달/재제안/확정/거절 | | ● | | |
| 진행상황 공유 | | | ● | |
| 진행상황 열람 | ● | | | |
| 담당 사건 목록 | | ●(배당분) | ● | |
| AI 요약·유사판례 | | ● | ● | |
| AI 키워드·법리 | ● | ● | ● | |

---

## 9. 상태 enum

- **CaseStatus**: INFO_REGISTERED → … → RETAINED / CLOSED
- **ConsultationStatus**: REQUESTED / APPROVED / REJECTED / SCHEDULE_CHANGED / CANCELED
- **ScheduleStatus**: AVAILABLE / RESERVED / …
- **RetainerStatus**: REQUEST_SENT / CONDITION_SENT / CONDITION_ACCEPTED / RETAINED / REJECTED
- **ConditionStatus**: CREATED / SENT / ACCEPTED / REJECTED

---

## 10. 핵심 설계 결정

| 결정 | 이유 |
|---|---|
| Java/Python **HTTP 분리** | BGE-M3/Chroma/Ollama는 Python 전용, 모델 상주 필요 |
| **단일 DB(law_system_jpa)** | 웹·AI·Python이 같은 데이터 — `LAWSYSTEM_DB_URL` 하나로 통일 |
| **JPA가 스키마 권한** | schema.sql(JDBC)과 상속 테이블 비호환 → JPA로 일원화 |
| 판례 **원본=DB / 인덱스=Chroma** | 단일 진실 공급원 유지, 검색은 case_id로 역참조 |
| **HTTP/1.1 고정** | uvicorn HTTP/2 미지원 → POST 본문 유실 방지 |
| 경량 세션 인증 | Spring Security 학습곡선 회피, 과제 규모에 적합 |
| **요청–호출–저장** | 설계의 AI 분석 프로세스를 DB까지 완결 |
| LLM **JSON 강제 + 산문 폴백** | gemma 등 약한 모델에서도 법리 설명 보장 |

---

## 11. 시작 시 자동 시드(@Order)

```
@Order(1) DataSeeder        : 역할별 계정 (client/partner/associate×3/staff, 비번 pw1234)
@Order(2) PrecedentDataLoader: privacy_cases.jsonl → precedent 테이블(JPA)
@Order(3) CaseSeeder        : 판례 → 각 변호사 담당 사건 배치(라운드로빈)
```
모두 멱등(이미 있으면 건너뜀). DB가 비어 있어도 1회 기동으로 테스트 데이터 완비.

---

## 12. 실행/셋업 절차

```
1) DB PC: law_system_jpa 생성 + 권한 + 방화벽(3306)
2) 코드 PC 환경변수: setx LAWSYSTEM_DB_URL ".../law_system_jpa..."  (+ USER/PW/DB)
3) Ollama: 모델 pull(qwen2.5:7b 권장)
4) Python: venv(C:\rag-venv, py3.13) + pip install -r requirements.txt + .env
5) Spring Boot 1회 기동 → Hibernate 테이블 생성 + 시드(@Order)
6) python -m scripts.build_index  (precedent → Chroma)
7) python -m scripts.serve        (:8000)
8) Spring Boot 실행(:8080) → 로그인(pw1234)
```

---

## 13. 구현 현황

**완료**: 인증·역할권한·디자인(Cohere)·사건(등록/상세/증거/AI이력)·변호사 검색(키워드)·AI 4종(요약/키워드/유사판례/법리)+영속화·상담(신청/일정/승인/삭제)·수임(요청/조건/수락/거절/**조정요청·재제안**/확정/삭제)·**진행상황 공유·열람**·담당 사건·판례 기반 시드.

**미구현/단순화**: 사건 배당 AI 추천(RecommendLawyers), 서면 초안(DocumentDraft), 본인인증/전자서명.

**기술 부채**: 일부 JDBC DAO(요약 영속화의 AIAnalysisService, PrecedentDAO/Importer)와 JPA 혼재 → 추후 JPA 일원화. 전역 예외처리(@ControllerAdvice)·입력검증(@Valid)·사건 소유권 체크 보강 여지.

---
*문서 기준: 인증~사건~AI~상담~수임~담당사건/시드 구현 시점.*
