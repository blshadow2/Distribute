-- =====================================================================
-- AI 기반 사건 수임 및 관리 플랫폼 (lawSystem)
-- MariaDB 스키마 DDL
-- 중간 레포트의 Class Diagram을 기반으로 한 테이블 정의이다.
-- 모든 식별자(memberId, caseId 등)는 클래스에서 String 으로 정의되어 있으므로
-- VARCHAR 로 매핑한다.
-- =====================================================================

-- ---------- Member 계층 ----------
CREATE TABLE IF NOT EXISTS member (
    member_id     VARCHAR(64)  PRIMARY KEY,
    name          VARCHAR(100) NOT NULL,
    email         VARCHAR(150) NOT NULL UNIQUE,
    password      VARCHAR(255) NOT NULL,
    phone_number  VARCHAR(30),
    role          VARCHAR(20)  NOT NULL,
    created_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS client (
    client_id              VARCHAR(64) PRIMARY KEY,
    member_id              VARCHAR(64) NOT NULL UNIQUE,
    address                VARCHAR(255),
    registered_case_count  INT         NOT NULL DEFAULT 0,
    identity_verified      BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_client_member FOREIGN KEY (member_id)
        REFERENCES member(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS lawyer (
    lawyer_id         VARCHAR(64)  PRIMARY KEY,
    member_id         VARCHAR(64)  NOT NULL UNIQUE,
    license_number    VARCHAR(50)  NOT NULL,
    office_location   VARCHAR(255),
    current_workload  INT          NOT NULL DEFAULT 0,
    introduction      TEXT,
    CONSTRAINT fk_lawyer_member FOREIGN KEY (member_id)
        REFERENCES member(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 변호사의 specialty 는 List<String> 이므로 별도 테이블로 분리한다.
CREATE TABLE IF NOT EXISTS lawyer_specialty (
    lawyer_id  VARCHAR(64)  NOT NULL,
    specialty  VARCHAR(100) NOT NULL,
    PRIMARY KEY (lawyer_id, specialty),
    CONSTRAINT fk_specialty_lawyer FOREIGN KEY (lawyer_id)
        REFERENCES lawyer(lawyer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS partner_lawyer (
    partner_lawyer_id   VARCHAR(64) PRIMARY KEY,
    lawyer_id           VARCHAR(64) NOT NULL UNIQUE,
    managing_lawyer_id  VARCHAR(64),
    CONSTRAINT fk_partner_lawyer FOREIGN KEY (lawyer_id)
        REFERENCES lawyer(lawyer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS associate_lawyer (
    associate_lawyer_id  VARCHAR(64) PRIMARY KEY,
    lawyer_id            VARCHAR(64) NOT NULL UNIQUE,
    CONSTRAINT fk_associate_lawyer FOREIGN KEY (lawyer_id)
        REFERENCES lawyer(lawyer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS staff (
    staff_id   VARCHAR(64)  PRIMARY KEY,
    member_id  VARCHAR(64)  NOT NULL UNIQUE,
    department VARCHAR(100),
    position   VARCHAR(100),
    CONSTRAINT fk_staff_member FOREIGN KEY (member_id)
        REFERENCES member(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Case 계층 ----------
-- 'case' 는 SQL 예약어이므로 legal_case 로 명명한다.
CREATE TABLE IF NOT EXISTS legal_case (
    case_id             VARCHAR(64) PRIMARY KEY,
    client_id           VARCHAR(64) NOT NULL,
    assigned_lawyer_id  VARCHAR(64),
    title               VARCHAR(255),
    category            VARCHAR(30),
    current_stage       VARCHAR(100),
    fact_description    TEXT,
    case_status         VARCHAR(50) NOT NULL,
    created_at          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_case_client FOREIGN KEY (client_id)
        REFERENCES client(client_id) ON DELETE CASCADE,
    CONSTRAINT fk_case_lawyer FOREIGN KEY (assigned_lawyer_id)
        REFERENCES lawyer(lawyer_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS case_keyword (
    case_id VARCHAR(64)  NOT NULL,
    keyword VARCHAR(100) NOT NULL,
    PRIMARY KEY (case_id, keyword),
    CONSTRAINT fk_case_keyword_case FOREIGN KEY (case_id)
        REFERENCES legal_case(case_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS case_info (
    case_info_id     VARCHAR(64) PRIMARY KEY,
    client_id        VARCHAR(64) NOT NULL,
    case_id          VARCHAR(64),
    title            VARCHAR(255),
    category         VARCHAR(30),
    current_stage    VARCHAR(100),
    fact_description TEXT,
    incident_date    DATE,
    region           VARCHAR(100),
    temporary_saved  BOOLEAN     NOT NULL DEFAULT FALSE,
    created_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_case_info_client FOREIGN KEY (client_id)
        REFERENCES client(client_id) ON DELETE CASCADE,
    CONSTRAINT fk_case_info_case FOREIGN KEY (case_id)
        REFERENCES legal_case(case_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS case_info_keyword (
    case_info_id VARCHAR(64)  NOT NULL,
    keyword      VARCHAR(100) NOT NULL,
    PRIMARY KEY (case_info_id, keyword),
    CONSTRAINT fk_case_info_keyword_info FOREIGN KEY (case_info_id)
        REFERENCES case_info(case_info_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS evidence (
    evidence_id VARCHAR(64) PRIMARY KEY,
    case_id     VARCHAR(64) NOT NULL,
    file_name   VARCHAR(255),
    file_type   VARCHAR(50),
    file_path   VARCHAR(500),
    description TEXT,
    CONSTRAINT fk_evidence_case FOREIGN KEY (case_id)
        REFERENCES legal_case(case_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS case_document (
    document_id   VARCHAR(64) PRIMARY KEY,
    case_id       VARCHAR(64) NOT NULL,
    document_type VARCHAR(50),
    title         VARCHAR(255),
    content       LONGTEXT,
    file_path     VARCHAR(500),
    created_by    VARCHAR(64),
    version       INT         NOT NULL DEFAULT 1,
    signed        BOOLEAN     NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_document_case FOREIGN KEY (case_id)
        REFERENCES legal_case(case_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS progression_record (
    progress_id        VARCHAR(64) PRIMARY KEY,
    case_id            VARCHAR(64) NOT NULL,
    writer_id          VARCHAR(64),
    progress_status    VARCHAR(100),
    description        TEXT,
    recent_action      TEXT,
    requested_material VARCHAR(255),
    CONSTRAINT fk_progress_case FOREIGN KEY (case_id)
        REFERENCES legal_case(case_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS similar_precedent (
    precedent_id        VARCHAR(64) PRIMARY KEY,
    case_id             VARCHAR(64) NOT NULL,
    precedent_title     VARCHAR(255),
    court_name          VARCHAR(150),
    case_number         VARCHAR(100),
    decision_date       DATE,
    precedent_summary   TEXT,
    legal_issue         TEXT,
    applied_legal_rule  TEXT,
    similarity_score    DOUBLE,
    source_url          VARCHAR(500),
    selected            BOOLEAN     NOT NULL DEFAULT FALSE,
    registered_by       VARCHAR(64),
    registered_at       DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_precedent_case FOREIGN KEY (case_id)
        REFERENCES legal_case(case_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Consultation ----------
CREATE TABLE IF NOT EXISTS consultation_schedule (
    schedule_id      VARCHAR(64) PRIMARY KEY,
    lawyer_id        VARCHAR(64) NOT NULL,
    date_time        DATETIME    NOT NULL,
    duration         INT         NOT NULL,
    available_status VARCHAR(20) NOT NULL,
    CONSTRAINT fk_schedule_lawyer FOREIGN KEY (lawyer_id)
        REFERENCES lawyer(lawyer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS consultation_request (
    consultation_request_id VARCHAR(64) PRIMARY KEY,
    case_id                 VARCHAR(64),
    client_id               VARCHAR(64),
    lawyer_id               VARCHAR(64) NOT NULL,
    schedule_id             VARCHAR(64),
    request_status          VARCHAR(30) NOT NULL,
    request_memo            TEXT,
    requested_at            DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_consult_case FOREIGN KEY (case_id)
        REFERENCES legal_case(case_id) ON DELETE SET NULL,
    CONSTRAINT fk_consult_client FOREIGN KEY (client_id)
        REFERENCES client(client_id) ON DELETE SET NULL,
    CONSTRAINT fk_consult_lawyer FOREIGN KEY (lawyer_id)
        REFERENCES lawyer(lawyer_id) ON DELETE CASCADE,
    CONSTRAINT fk_consult_schedule FOREIGN KEY (schedule_id)
        REFERENCES consultation_schedule(schedule_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Retainer ----------
CREATE TABLE IF NOT EXISTS retainer_request (
    retainer_request_id VARCHAR(64) PRIMARY KEY,
    case_id             VARCHAR(64) NOT NULL,
    client_id           VARCHAR(64),
    lawyer_id           VARCHAR(64) NOT NULL,
    request_content     TEXT,
    desired_scope       VARCHAR(255),
    desired_fee         INT,
    desired_result      TEXT,
    request_status      VARCHAR(40) NOT NULL,
    requested_at        DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_retainer_case FOREIGN KEY (case_id)
        REFERENCES legal_case(case_id) ON DELETE CASCADE,
    CONSTRAINT fk_retainer_client FOREIGN KEY (client_id)
        REFERENCES client(client_id) ON DELETE SET NULL,
    CONSTRAINT fk_retainer_lawyer FOREIGN KEY (lawyer_id)
        REFERENCES lawyer(lawyer_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS retainer_condition (
    condition_id        VARCHAR(64) PRIMARY KEY,
    retainer_request_id VARCHAR(64) NOT NULL,
    fee                 INT         NOT NULL,
    scope               VARCHAR(255),
    additional_terms    TEXT,
    revision_no         INT         NOT NULL DEFAULT 1,
    condition_status    VARCHAR(30) NOT NULL,
    created_at          DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_condition_request FOREIGN KEY (retainer_request_id)
        REFERENCES retainer_request(retainer_request_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Verification ----------
CREATE TABLE IF NOT EXISTS verification_result (
    verification_id      VARCHAR(64) PRIMARY KEY,
    member_id            VARCHAR(64) NOT NULL,
    verification_method  VARCHAR(50),
    verified             BOOLEAN     NOT NULL DEFAULT FALSE,
    verified_at          DATETIME,
    personal_identifier  VARCHAR(100),
    CONSTRAINT fk_verification_member FOREIGN KEY (member_id)
        REFERENCES member(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS electronic_signature (
    signature_id     VARCHAR(64)  PRIMARY KEY,
    document_id      VARCHAR(64)  NOT NULL,
    member_id        VARCHAR(64)  NOT NULL,
    verification_id  VARCHAR(64),
    signed_at        DATETIME     NOT NULL,
    signature_hash   VARCHAR(255) NOT NULL,
    CONSTRAINT fk_signature_document FOREIGN KEY (document_id)
        REFERENCES case_document(document_id) ON DELETE CASCADE,
    CONSTRAINT fk_signature_member FOREIGN KEY (member_id)
        REFERENCES member(member_id) ON DELETE CASCADE,
    CONSTRAINT fk_signature_verification FOREIGN KEY (verification_id)
        REFERENCES verification_result(verification_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- AI ----------
CREATE TABLE IF NOT EXISTS ai_analysis_function (
    function_id      VARCHAR(64) PRIMARY KEY,
    function_name    VARCHAR(150),
    model_version    VARCHAR(50),
    function_status  VARCHAR(30)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_analysis_request (
    ai_analysis_request_id VARCHAR(64) PRIMARY KEY,
    requester_id           VARCHAR(64),
    target_type            VARCHAR(50),
    target_id              VARCHAR(64),
    analysis_type          VARCHAR(40) NOT NULL,
    prompt                 TEXT,
    request_status         VARCHAR(20) NOT NULL,
    requested_at           DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fail_reason            TEXT,
    CONSTRAINT fk_ai_request_member FOREIGN KEY (requester_id)
        REFERENCES member(member_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS ai_analysis_result (
    ai_result_id      VARCHAR(64) PRIMARY KEY,
    ai_request_id     VARCHAR(64) NOT NULL,
    case_id           VARCHAR(64),
    result_type       VARCHAR(40) NOT NULL,
    summary_text      LONGTEXT,
    confidence_score  DOUBLE,
    generated_at      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    reviewed          BOOLEAN     NOT NULL DEFAULT FALSE,
    reviewer_id       VARCHAR(64),
    CONSTRAINT fk_ai_result_request FOREIGN KEY (ai_request_id)
        REFERENCES ai_analysis_request(ai_analysis_request_id) ON DELETE CASCADE,
    CONSTRAINT fk_ai_result_case FOREIGN KEY (case_id)
        REFERENCES legal_case(case_id) ON DELETE SET NULL,
    CONSTRAINT fk_ai_result_reviewer FOREIGN KEY (reviewer_id)
        REFERENCES member(member_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- ---------- Precedent (RAG 판례 마스터 카탈로그) ----------
-- 외부에서 수집한 판례 원문을 보관하는 단일 진실 공급원(single source of truth).
-- RAG 색인/검색은 이 테이블을 읽어 수행하며, 검색 결과는 external_case_id 로 역참조한다.
CREATE TABLE IF NOT EXISTS precedent (
    precedent_id        VARCHAR(64) PRIMARY KEY,
    external_case_id    VARCHAR(64) NOT NULL UNIQUE,
    case_name           VARCHAR(255),
    case_number         VARCHAR(100),
    court_name          VARCHAR(150),
    court_type_code     VARCHAR(20),
    decision_date       DATE,
    case_type           VARCHAR(50),
    judgment_type       VARCHAR(50),
    issues              TEXT,
    summary             TEXT,
    referenced_statutes TEXT,
    referenced_cases    TEXT,
    full_text           LONGTEXT,
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

-- 사건에 등록된 유사판례(similar_precedent)가 어떤 마스터 판례에서 왔는지 추적용(선택).
ALTER TABLE similar_precedent
    ADD COLUMN IF NOT EXISTS precedent_ref_id VARCHAR(64) NULL AFTER case_id;
