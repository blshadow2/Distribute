# lawSystem DB 계층

중간 레포트의 Class Diagram을 기반으로 한 JDBC + MariaDB DAO/DBA 구조이다.

## 디렉터리 구성

```
src/lawSystem/db/
├── DBA.java             - JDBC 연결 관리 (Database Access)
├── DBInitializer.java   - 데이터베이스/테이블 생성
├── DBDemo.java          - DB에 연결해 콘솔 입력/출력으로 DAO를 테스트하는 데모
├── db.properties        - 접속 정보
├── schema.sql           - 전체 DDL
└── dao/                 - 각 엔티티별 DAO
    ├── MemberDAO.java
    ├── ClientDAO.java
    ├── LawyerDAO.java
    ├── PartnerLawyerDAO.java
    ├── AssociateLawyerDAO.java
    ├── StaffDAO.java
    ├── CaseDAO.java
    ├── CaseInfoDAO.java
    ├── EvidenceDAO.java
    ├── CaseDocumentDAO.java
    ├── ProgressionRecordDAO.java
    ├── SimilarPrecedentDAO.java
    ├── ConsultationRequestDAO.java
    ├── ConsultationScheduleDAO.java
    ├── RetainerRequestDAO.java
    ├── RetainerConditionDAO.java
    ├── VerificationResultDAO.java
    ├── ElectronicSignatureDAO.java
    ├── AIAnalysisFunctionDAO.java
    ├── AIAnalysisRequestDAO.java
    └── AIAnalysisResultDAO.java
```

## 사용 흐름

1. **MariaDB 설치 및 사용자 준비**
   - MariaDB 서버가 실행 중이어야 한다.
   - `db.properties` 의 `jdbc.username`, `jdbc.password`, 포트(기본 3306)를 환경에 맞게 수정한다.

2. **JDBC 드라이버 추가**
   - `mariadb-java-client-x.x.x.jar` 를 IntelliJ IDEA 의 `File > Project Structure > Modules > Dependencies` 또는
     클래스패스에 추가한다.
   - 드라이버 클래스명: `org.mariadb.jdbc.Driver`

3. **스키마 생성**
   ```java
   lawSystem.db.DBInitializer.initialize();
   ```
   - 호출 시 `law_system` 데이터베이스가 없으면 생성하고,
     `schema.sql` 의 모든 CREATE TABLE 을 실행한다.

4. **DAO 사용**
   ```java
   ClientDAO clientDAO = new ClientDAO();
   clientDAO.insert(new Client(...));

   Client found = clientDAO.findById("client-c1");
   ```

5. **DB 연결 텍스트 데모 실행**
   - `DBDemo.java` 의 `main` 은 웹 화면 없이 콘솔 입력/출력으로 실제 DAO 저장/조회 흐름을 테스트한다.
   - 실행하면 `DBInitializer.initialize()` 로 MariaDB 데이터베이스와 테이블을 준비한 뒤 메뉴를 출력한다.
   - `--sample` 인자를 주면 의뢰인 → 변호사 → 사건 → 증거 → 상담 → 수임 요청 → 인증/전자서명 → AI 분석 데이터를 DB에 자동 저장하고 다시 조회해 출력한다.
   - 예: `java -cp <classes>:<mariadb-java-client.jar> lawSystem.db.DBDemo --sample`

## 계층 구조

```
Application / Member 메서드  ──▶  DAO (lawSystem.db.dao.*)  ──▶  DBA  ──▶  MariaDB JDBC
```

- 도메인 객체는 직접 JDBC API 를 호출하지 않는다.
- DAO 만이 `DBA.getConnection()` 을 통해 `Connection` 을 획득한다.
- DBA 는 `db.properties` 의 설정을 기반으로 `DriverManager` 에서 `Connection` 을 만든다.

## 테이블 매핑 요약

| 클래스                  | 테이블                     |
|------------------------|---------------------------|
| Member                 | member                    |
| Client                 | client (+ member)         |
| Lawyer                 | lawyer (+ member)         |
| (Lawyer.specialty)     | lawyer_specialty          |
| PartnerLawyer          | partner_lawyer            |
| AssociateLawyer        | associate_lawyer          |
| Staff                  | staff                     |
| Case                   | legal_case (case 는 예약어) |
| (Case.keywords)        | case_keyword              |
| CaseInfo               | case_info                 |
| (CaseInfo.keywords)    | case_info_keyword         |
| Evidence               | evidence                  |
| CaseDocument           | case_document             |
| ProgressionRecord      | progression_record        |
| SimilarPrecedent       | similar_precedent         |
| ConsultationRequest    | consultation_request      |
| ConsultationSchedule   | consultation_schedule     |
| RetainerRequest        | retainer_request          |
| RetainerCondition      | retainer_condition        |
| VerificationResult     | verification_result       |
| ElectronicSignature    | electronic_signature      |
| AIAnalysisFunction     | ai_analysis_function      |
| AIAnalysisRequest      | ai_analysis_request       |
| AIAnalysisResult       | ai_analysis_result        |

## 비고

- 모든 ID 컬럼은 `VARCHAR(64)` 이다. 도메인 클래스가 String 으로 정의되어 있어 그에 맞췄다.
- `List<String>` 필드(`Lawyer.specialty`, `Case.keywords`, `CaseInfo.keywords`)는 보조 테이블로 분리되어 있다.
- `enum` 값은 모두 `VARCHAR` 컬럼에 `name()` 으로 저장된다.
- 외래 키는 부모 삭제 시의 의도(`ON DELETE CASCADE` 또는 `SET NULL`)에 따라 다르게 설정되어 있다.
