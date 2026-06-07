# lawSystem JPA 계층

기존 `lawSystem.db` (JDBC + DAO) 와 평행하게 동작하는 **JPA 기반 영속성 계층**이다.
같은 도메인을 JPA 엔티티 + Repository 패턴으로 다시 모델링했다.

## 디렉터리 구성

```
src/
├── META-INF/persistence.xml      - JPA 영속성 단위(Persistence Unit) 설정
└── lawSystem/jpa/
    ├── JpaManager.java           - EntityManagerFactory 라이프사이클 관리
    ├── JpaDemo.java              - 전체 흐름 시연 main
    ├── entity/                   - JPA 엔티티 21개
    │   ├── Member.java           - 추상 베이스 (@Inheritance(JOINED))
    │   ├── Client.java / Lawyer.java(추상)
    │   ├── PartnerLawyer.java / AssociateLawyer.java / Staff.java
    │   ├── LegalCase.java        - 사건 (case 가 예약어라 테이블명 legal_case)
    │   ├── CaseInfo.java / Evidence.java / CaseDocument.java
    │   ├── ProgressionRecord.java / SimilarPrecedent.java
    │   ├── ConsultationRequest.java / ConsultationSchedule.java
    │   ├── RetainerRequest.java / RetainerCondition.java
    │   ├── VerificationResult.java / ElectronicSignature.java
    │   └── AIAnalysisFunction.java / AIAnalysisRequest.java / AIAnalysisResult.java
    └── repository/
        ├── BaseRepository.java   - 제네릭 CRUD 베이스
        ├── ClientRepository.java
        ├── PartnerLawyerRepository.java
        ├── LegalCaseRepository.java
        ├── ConsultationRequestRepository.java
        └── RetainerRequestRepository.java
```

## DAO 와의 차이점

| 항목 | DAO 계층 (lawSystem.db) | JPA 계층 (lawSystem.jpa) |
|---|---|---|
| API | `Connection`, `PreparedStatement`, `ResultSet` 직접 사용 | `EntityManager`, JPQL |
| 매핑 | 수동 (rs.getString → 필드) | 어노테이션 자동 (`@Entity`, `@Column`) |
| 트랜잭션 | autoCommit 또는 명시적 commit/rollback | `EntityTransaction` (또는 `@Transactional` 풍의 `JpaManager.execute`) |
| 관계 | FK 컬럼을 String 으로 직접 다룸 | 객체 참조 (`@ManyToOne Client`) |
| List<String> 필드 | 보조 테이블 + 별도 DAO 메서드 | `@ElementCollection` 자동 |
| 상속 | 별도 테이블 + 1:1 join, ID 두 개 | `@Inheritance(JOINED)` + `@PrimaryKeyJoinColumn` |
| 스키마 | `schema.sql` 수동 관리 | `hbm2ddl.auto=update` 자동 |
| 데이터베이스 | `law_system` | `law_system_jpa` (분리) |

## 필요한 라이브러리

`hibernate-orm-6.x.x.Final.tar.gz` (또는 zip) 을 https://hibernate.org/orm/releases/ 에서 다운로드한 후,
압축 안의 `lib/required/` 의 모든 jar 를 IntelliJ 프로젝트 의존성에 추가한다.
대표적으로 필요한 jar 들:

- `hibernate-core-6.x.x.Final.jar`
- `jakarta.persistence-api-3.1.x.jar`
- `jakarta.transaction-api-2.0.x.jar`
- `jakarta.xml.bind-api-4.0.x.jar`
- `jboss-logging-3.5.x.jar`
- `byte-buddy-1.x.x.jar`
- `antlr-4.x.x.jar`
- `classmate-1.x.x.jar`
- `jandex-3.x.x.jar`
- `jakarta.inject-api-2.0.x.jar`

기존에 가지고 있던 `mariadb-java-client-x.x.x.jar` 는 그대로 사용한다.

## 설정 흐름

### 1) DB 준비

MariaDB 에 root 로 접속하여 JPA 전용 데이터베이스를 만든다:

```sql
CREATE DATABASE IF NOT EXISTS law_system_jpa
    DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

GRANT ALL PRIVILEGES ON law_system_jpa.* TO 'blshadow2'@'%';
FLUSH PRIVILEGES;
```

테이블은 `hbm2ddl.auto=update` 가 엔티티 어노테이션을 보고 자동으로 만든다.

### 2) 환경 변수

기존 DAO 와 같은 환경 변수를 재사용한다 — URL 만 새 DB 를 가리키게 바꾼다:

```
LAWSYSTEM_DB_URL=jdbc:mariadb://192.168.0.17:3306/law_system_jpa?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Seoul
LAWSYSTEM_DB_USER=blshadow2
LAWSYSTEM_DB_PASSWORD=********
```

IntelliJ Run Configuration 의 Environment variables 에 위 세 줄 추가.

### 3) 실행

`JpaDemo.main` 을 실행하면 다음 흐름이 진행된다:

1. `JpaManager.factory()` 가 `persistence.xml` + 환경 변수를 합쳐 `EntityManagerFactory` 생성
2. `hbm2ddl.auto=update` 로 누락된 테이블 자동 생성
3. 의뢰인 → 변호사 → 사건정보/사건 → 증거/문서/판례/진행기록 → 상담 → 수임 → 인증/서명 → AI 분석까지 한 번에 저장
4. Repository 의 다양한 조회 메서드로 결과 재확인
5. `JpaManager.shutdown()` 으로 EMF 종료

콘솔에 Hibernate 가 실행한 SQL 이 보기 좋게 포맷되어 출력된다 (`hibernate.show_sql=true`, `hibernate.format_sql=true`).

## 핵심 개념 설명

### 1. `@Inheritance(JOINED)` 와 `@PrimaryKeyJoinColumn`

```java
@Entity @Inheritance(JOINED)
public abstract class Member { @Id String memberId; ... }

@Entity @PrimaryKeyJoinColumn(name="member_id")
public class Client extends Member { String address; ... }
```

- 부모 `Member` 와 자식 `Client` 가 **각자의 테이블**을 가지고
- 자식의 PK 가 부모의 PK 와 같은 컬럼명(`member_id`) 으로 1:1 join
- DAO 에서는 `client.client_id` PK + `client.member_id` FK 의 **두 ID** 를 다뤘지만, JPA 에서는 **하나의 ID(`memberId`)** 만 다룬다 — 더 단순함

### 2. `@ElementCollection` (List<String> 매핑)

```java
@ElementCollection
@CollectionTable(name="lawyer_specialty", joinColumns=@JoinColumn(name="member_id"))
@Column(name="specialty")
List<String> specialty;
```

- DAO 에서는 `lawyer_specialty` 테이블 + `LawyerDAO.insertSpecialties()` 수동 관리
- JPA 에서는 어노테이션 한 줄이면 자동. insert/update/delete 모두 자동 동기화

### 3. `@ManyToOne` / `@OneToMany` 양방향

```java
@Entity
public class LegalCase {
    @OneToMany(mappedBy="legalCase", cascade=ALL, orphanRemoval=true)
    List<Evidence> evidences;
}

@Entity
public class Evidence {
    @ManyToOne @JoinColumn(name="case_id")
    LegalCase legalCase;
}
```

- DAO: `EvidenceDAO.findByCaseId(caseId)` 로 따로 조회
- JPA: `legalCase.getEvidences()` 하나로 자동 로딩 (LAZY 인 경우 트랜잭션 안에서 호출)
- `cascade=ALL`: 부모 저장 시 자식도 함께 INSERT
- `orphanRemoval=true`: 컬렉션에서 빼면 자동 DELETE

### 4. `@Enumerated(EnumType.STRING)`

```java
@Enumerated(EnumType.STRING)
@Column(name="case_status")
CaseStatus caseStatus;
```

- enum 값을 `name()` 문자열로 저장 (`ORDINAL` 대신 권장 — 순서 바뀌어도 안전)
- DAO 에서는 `status.name()`/`CaseStatus.valueOf(...)` 를 수동 호출했음

### 5. `JpaManager.execute()` / `query()` 패턴

```java
JpaManager.execute(em -> em.persist(entity));            // 쓰기, 반환 없음
Client c = JpaManager.query(em -> em.find(Client.class, id));   // 읽기/계산, 반환
```

- `EntityManager` 의 생성/트랜잭션 시작/커밋/롤백/닫기 를 한꺼번에 처리
- DAO 의 `try-with-resources Connection` 패턴과 정확히 같은 역할
- Repository 가 이 헬퍼를 통해 트랜잭션 안에서 동작

### 6. `BaseRepository<T, ID>` 제네릭 패턴

```java
public class ClientRepository extends BaseRepository<Client, String> {
    public ClientRepository() { super(Client.class); }
    // save/findById/findAll/update/deleteById 자동 상속
    // 도메인 특화 메서드만 추가하면 됨
    public Optional<Client> findByEmail(String email) { ... }
}
```

Spring Data JPA 의 `JpaRepository<T, ID>` 인터페이스 패턴의 단순화 버전.

## 코드 흐름 예시

### 새 사건 저장

DAO 버전:
```java
Case c = new Case(...);
caseDAO.insert(c);          // SQL INSERT 직접
List<String> kws = ...;
for (String kw : kws) {
    caseKeywordDAO.insert(c.getCaseId(), kw);   // 보조 테이블 별도
}
```

JPA 버전:
```java
LegalCase c = new LegalCase(...);
c.setKeywords(kws);         // List 그대로 세팅
caseRepo.save(c);           // INSERT + case_keyword INSERT 자동
```

### 사건과 의뢰인 연결

DAO: FK 컬럼을 String 으로 다룸
```java
case.setClientId(client.getClientId());
caseDAO.update(case);
```

JPA: 객체 참조
```java
case.setClient(client);       // managed 상태면 dirty checking 으로 자동 UPDATE
```

### 사건의 증거 목록 조회

DAO:
```java
List<Evidence> evidences = evidenceDAO.findByCaseId(caseId);
```

JPA (이미 사건이 로딩됐다면):
```java
List<Evidence> evidences = legalCase.getEvidences();    // LAZY 이면 트랜잭션 안에서
```

## 자주 마주치는 함정

1. **LazyInitializationException** — `em.close()` 후 컬렉션을 만지면 발생. 항상 `JpaManager.execute/query` 블록 안에서 컬렉션을 다루거나, JPQL `JOIN FETCH` 로 미리 로딩.
2. **Detached entity passed to persist** — 이미 ID 가 있는 영속화 안 된 객체를 `persist()` 하면 발생. 대신 `merge()` 사용.
3. **EntityExistsException** — 같은 PK 가 이미 있는 상태에서 `persist` 시도. runId 패턴으로 회피.
4. **`hbm2ddl.auto=update` 의 한계** — 컬럼 타입 변경, NOT NULL 추가 같은 변경은 무시. 큰 스키마 변경은 직접 DDL 작성 또는 `create-drop` 으로 한 번 재생성.
5. **이름 충돌** — `lawSystem.legalCase.Case` 와 JPA 의 `LegalCase` 가 완전 별개 클래스이다. JPA 시연에는 항상 `lawSystem.jpa.entity.*` 만 import.

## DAO vs JPA — 어느 쪽을 쓸지

| 시나리오 | 추천 |
|---|---|
| SQL 을 직접 통제하고 싶음 | DAO |
| 학습/구조 이해 목적 | DAO (어떻게 동작하는지 노출됨) |
| 도메인 객체로 자연스럽게 작업 | **JPA** |
| 관계 그래프 자주 탐색 | **JPA** |
| 대량 INSERT/UPDATE 성능 | DAO (또는 JPA + JDBC 배치) |
| 학교 과제 코드 가독성 | **JPA** (현장 표준) |

이 프로젝트는 두 가지를 모두 가지고 있으므로, 같은 도메인을 두 방식으로 어떻게 구현하는지 비교 학습에 좋다.
