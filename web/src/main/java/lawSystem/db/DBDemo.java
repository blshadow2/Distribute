package lawSystem.db;

import lawSystem.ai.AIAnalysisFunction;
import lawSystem.ai.AIAnalysisRequest;
import lawSystem.ai.AIAnalysisResult;
import lawSystem.ai.AIRequestStatus;
import lawSystem.ai.AnalysisType;
import lawSystem.consultation.ConsultationRequest;
import lawSystem.consultation.ConsultationSchedule;
import lawSystem.consultation.ConsultationStatus;
import lawSystem.db.dao.AIAnalysisFunctionDAO;
import lawSystem.db.dao.AIAnalysisRequestDAO;
import lawSystem.db.dao.AIAnalysisResultDAO;
import lawSystem.db.dao.CaseDAO;
import lawSystem.db.dao.CaseDocumentDAO;
import lawSystem.db.dao.CaseInfoDAO;
import lawSystem.db.dao.ClientDAO;
import lawSystem.db.dao.ConsultationRequestDAO;
import lawSystem.db.dao.ConsultationScheduleDAO;
import lawSystem.db.dao.ElectronicSignatureDAO;
import lawSystem.db.dao.EvidenceDAO;
import lawSystem.db.dao.PartnerLawyerDAO;
import lawSystem.db.dao.ProgressionRecordDAO;
import lawSystem.db.dao.RetainerConditionDAO;
import lawSystem.db.dao.RetainerRequestDAO;
import lawSystem.db.dao.SimilarPrecedentDAO;
import lawSystem.db.dao.VerificationResultDAO;
import lawSystem.legalCase.Case;
import lawSystem.legalCase.CaseCategory;
import lawSystem.legalCase.CaseDocument;
import lawSystem.legalCase.CaseInfo;
import lawSystem.legalCase.CaseStatus;
import lawSystem.legalCase.Evidence;
import lawSystem.legalCase.ProgressionRecord;
import lawSystem.legalCase.SimilarPrecedent;
import lawSystem.member.Client;
import lawSystem.member.PartnerLawyer;
import lawSystem.retainer.RetainerCondition;
import lawSystem.retainer.RetainerRequest;
import lawSystem.retainer.RetainerStatus;
import lawSystem.verification.ElectronicSignature;
import lawSystem.verification.VerificationResult;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * MariaDB 에 연결한 상태에서 콘솔 입력/출력으로 DAO 흐름을 확인하는 텍스트 기반 DB 데모이다.
 *
 * <p>웹 화면 없이 Scanner 로 값을 입력받고, 각 DAO 를 통해 실제 DB에 저장한 뒤
 * 다시 조회 결과를 콘솔에 출력한다. 실행 전 db.properties 와 MariaDB JDBC 드라이버가
 * 준비되어 있어야 한다.</p>
 */
public class DBDemo {

    private final Scanner scanner;
    private final ClientDAO clientDAO = new ClientDAO();
    private final PartnerLawyerDAO partnerLawyerDAO = new PartnerLawyerDAO();
    private final CaseInfoDAO caseInfoDAO = new CaseInfoDAO();
    private final CaseDAO caseDAO = new CaseDAO();
    private final EvidenceDAO evidenceDAO = new EvidenceDAO();
    private final ProgressionRecordDAO progressionRecordDAO = new ProgressionRecordDAO();
    private final SimilarPrecedentDAO similarPrecedentDAO = new SimilarPrecedentDAO();
    private final CaseDocumentDAO caseDocumentDAO = new CaseDocumentDAO();
    private final ConsultationScheduleDAO scheduleDAO = new ConsultationScheduleDAO();
    private final ConsultationRequestDAO consultationRequestDAO = new ConsultationRequestDAO();
    private final RetainerRequestDAO retainerRequestDAO = new RetainerRequestDAO();
    private final RetainerConditionDAO retainerConditionDAO = new RetainerConditionDAO();
    private final VerificationResultDAO verificationResultDAO = new VerificationResultDAO();
    private final ElectronicSignatureDAO electronicSignatureDAO = new ElectronicSignatureDAO();
    private final AIAnalysisFunctionDAO aiFunctionDAO = new AIAnalysisFunctionDAO();
    private final AIAnalysisRequestDAO aiRequestDAO = new AIAnalysisRequestDAO();
    private final AIAnalysisResultDAO aiResultDAO = new AIAnalysisResultDAO();

    public DBDemo(Scanner scanner) {
        this.scanner = scanner;
    }

    public static void main(String[] args) {
        printTitle("LawSystem DB 연결 텍스트 입출력 데모");
        printStep("0. 데이터베이스와 테이블을 초기화한다");
        DBInitializer.initialize();

        DBDemo demo = new DBDemo(new Scanner(System.in));
        if (args.length > 0 && "--sample".equals(args[0])) {
            demo.runSampleScenario();
            return;
        }

        demo.runMenu();
    }

    private void runMenu() {
        while (true) {
            printMenu();
            String command = prompt("메뉴 번호", "1");

            switch (command) {
                case "1":
                    runSampleScenario();
                    break;
                case "2":
                    createClientFromInput();
                    break;
                case "3":
                    createPartnerLawyerFromInput();
                    break;
                case "4":
                    createCaseFromInput();
                    break;
                case "5":
                    createEvidenceFromInput();
                    break;
                case "6":
                    listCasesByClientFromInput();
                    break;
                case "7":
                    listEvidenceByCaseFromInput();
                    break;
                case "8":
                    listConsultationsByLawyerFromInput();
                    break;
                case "0":
                    printTitle("DB 연결 텍스트 데모를 종료한다");
                    return;
                default:
                    System.out.println("알 수 없는 메뉴이다. 다시 입력한다.");
            }
        }
    }

    private static void printMenu() {
        System.out.println("\n=== DB 텍스트 테스트 메뉴 ===");
        System.out.println("1. 샘플 시나리오 자동 저장/조회");
        System.out.println("2. 의뢰인 직접 입력 후 저장");
        System.out.println("3. 대표 변호사 직접 입력 후 저장");
        System.out.println("4. 사건 정보 직접 입력 후 사건 저장");
        System.out.println("5. 증거 자료 직접 입력 후 저장");
        System.out.println("6. 의뢰인 ID로 사건 목록 조회");
        System.out.println("7. 사건 ID로 증거 목록 조회");
        System.out.println("8. 변호사 ID로 상담 신청 목록 조회");
        System.out.println("0. 종료");
    }

    /**
     * 콘솔 입력 없이도 DB 연결과 DAO 저장/조회 흐름을 빠르게 확인할 수 있는 샘플 시나리오이다.
     */
    private void runSampleScenario() {
        String runId = "demo-" + System.currentTimeMillis();
        printTitle("샘플 시나리오 시작: " + runId);

        Client client = new Client(
                id(runId, "member-client"),
                id(runId, "client"),
                "홍길동",
                runId + "+client@example.com",
                "pw1234",
                "010-1111-2222",
                "서울특별시 종로구"
        );
        require(clientDAO.insert(client), "의뢰인 저장");

        PartnerLawyer partner = new PartnerLawyer(
                id(runId, "member-partner"),
                id(runId, "lawyer-partner"),
                id(runId, "login-partner"),
                "김변호",
                runId + "+partner@example.com",
                "pw1234",
                "010-3333-4444",
                "LAW-" + runId,
                "서울특별시 서초구",
                Arrays.asList("민사", "노동"),
                "10년차 민사/노동 전문 대표 변호사"
        );
        require(partnerLawyerDAO.insert(partner), "대표 변호사 저장");

        CaseInfo caseInfo = new CaseInfo(
                id(runId, "case-info"),
                client.getClientId(),
                "근로계약 미체결 임금체불 사건",
                CaseCategory.LABOR,
                "초기 상담",
                "2026년 1월부터 3개월 동안 임금이 지급되지 않았고, 근로계약서 교부도 지연되었다."
        );
        caseInfo.setIncidentDate(LocalDate.of(2026, 1, 15));
        caseInfo.setRegion("서울특별시");
        caseInfo.addKeywords(Arrays.asList("임금체불", "근로계약", "노동"));
        require(caseInfoDAO.insert(caseInfo), "사건 정보 저장");

        Case targetCase = Case.createCase(caseInfo);
        require(targetCase != null, "사건 객체 생성");
        targetCase.assignLawyer(partner.getLawyerId());
        require(caseDAO.insert(targetCase), "사건 저장");
        caseInfo.setCaseId(targetCase.getCaseId());
        require(caseInfoDAO.update(caseInfo), "사건 정보와 사건 연결");

        Evidence evidence = new Evidence(
                id(runId, "evidence"),
                targetCase.getCaseId(),
                "급여명세서.pdf",
                "pdf",
                "/data/evidence/급여명세서.pdf",
                "임금 미지급 사실을 확인할 수 있는 급여명세서"
        );
        require(evidenceDAO.insert(evidence), "증거 저장");

        ProgressionRecord progressionRecord = new ProgressionRecord(
                id(runId, "progress"),
                targetCase.getCaseId(),
                partner.getMemberId(),
                "상담 준비",
                "의뢰인이 제출한 급여명세서와 근무 기록을 검토했다.",
                "근로계약서 사본 추가 요청"
        );
        progressionRecord.requestAdditionalMaterial("출퇴근 기록");
        require(progressionRecordDAO.insert(progressionRecord), "진행 기록 저장");

        SimilarPrecedent precedent = new SimilarPrecedent(
                id(runId, "precedent"),
                targetCase.getCaseId(),
                "임금체불 및 근로계약서 미교부 손해배상 사건",
                "2024가단12345"
        );
        precedent.setCourtName("서울중앙지방법원");
        precedent.setDecisionDate(LocalDate.of(2024, 6, 12));
        precedent.setPrecedentSummary("근로계약서 미교부와 반복 임금체불이 인정된 사례");
        precedent.setLegalIssue("임금 지급 의무 및 근로계약서 교부 의무 위반");
        precedent.setAppliedLegalRule("근로기준법 제17조, 제36조");
        precedent.setSimilarityScore(0.82);
        precedent.setSelected(true);
        precedent.setRegisteredBy(partner.getMemberId());
        require(similarPrecedentDAO.insert(precedent), "유사 판례 저장");

        CaseDocument document = new CaseDocument(
                id(runId, "document"),
                targetCase.getCaseId(),
                "내용증명",
                "임금 지급 요청 내용증명 초안",
                "미지급 임금 및 지연이자 지급을 요청하는 내용증명 초안입니다.",
                partner.getMemberId()
        );
        document.setFilePath("/data/documents/임금지급요청_내용증명.docx");
        require(caseDocumentDAO.insert(document), "사건 문서 저장");

        ConsultationSchedule schedule = new ConsultationSchedule(
                id(runId, "schedule"),
                partner.getLawyerId(),
                LocalDateTime.of(2026, 5, 20, 10, 0),
                30
        );
        require(scheduleDAO.insert(schedule), "상담 일정 저장");

        ConsultationRequest consultationRequest = new ConsultationRequest(
                id(runId, "consultation"),
                targetCase.getCaseId(),
                client.getClientId(),
                partner.getLawyerId(),
                schedule.getScheduleId(),
                "임금체불 관련 상담 부탁드립니다."
        );
        require(consultationRequestDAO.insert(consultationRequest), "상담 신청 저장");
        require(consultationRequestDAO.updateStatus(
                consultationRequest.getConsultationRequestId(),
                ConsultationStatus.APPROVED
        ), "상담 신청 승인");
        schedule.reserveSchedule();
        require(scheduleDAO.update(schedule), "상담 일정 예약 상태 반영");
        targetCase.changeCaseStatus(CaseStatus.CONSULTATION_REQUESTED);
        require(caseDAO.updateStatus(targetCase.getCaseId(), targetCase.getCaseStatus()), "사건 상태 상담 신청으로 변경");

        RetainerRequest retainerRequest = new RetainerRequest(
                id(runId, "retainer"),
                targetCase.getCaseId(),
                client.getClientId(),
                partner.getLawyerId(),
                "임금 청구 소송 진행을 요청합니다.",
                "임금체불에 대한 내용증명 발송부터 민사소송 1심까지",
                3_000_000,
                "미지급 임금 전액 회수"
        );
        retainerRequest.sendRequest();
        require(retainerRequestDAO.insert(retainerRequest), "수임 요청 저장");

        RetainerCondition retainerCondition = new RetainerCondition(
                id(runId, "condition"),
                retainerRequest.getRetainerRequestId(),
                3_200_000,
                "내용증명, 지급명령, 1심 소송 대리",
                "인지대와 송달료 등 실비는 별도 정산"
        );
        retainerCondition.sendCondition();
        require(retainerConditionDAO.insert(retainerCondition), "수임 조건 저장");
        require(retainerRequestDAO.updateStatus(retainerRequest.getRetainerRequestId(), RetainerStatus.CONDITION_SENT),
                "수임 요청 상태 조건 발송으로 변경");

        VerificationResult verificationResult = new VerificationResult(
                id(runId, "verification"),
                client.getMemberId(),
                "휴대폰 인증"
        );
        require(verificationResult.checkVerificationCode("123456"), "본인 인증 코드 확인");
        require(verificationResultDAO.insert(verificationResult), "본인 인증 결과 저장");

        ElectronicSignature signature = new ElectronicSignature(
                id(runId, "signature"),
                document.getDocumentId(),
                client.getMemberId(),
                verificationResult.getVerificationId(),
                LocalDateTime.now(),
                id(runId, "signature-hash")
        );
        require(signature.validateSignature(), "전자 서명 유효성 검사");
        require(electronicSignatureDAO.insert(signature), "전자 서명 저장");
        document.signDocument(client.getMemberId());
        require(caseDocumentDAO.markSigned(document.getDocumentId()), "문서 서명 상태 반영");

        AIAnalysisFunction aiFunction = new AIAnalysisFunction(
                id(runId, "ai-function"),
                "사건 요약",
                "text-db-demo-model-v1",
                "ACTIVE"
        );
        require(aiFunctionDAO.insert(aiFunction), "AI 기능 저장");

        AIAnalysisRequest aiRequest = new AIAnalysisRequest(
                id(runId, "ai-request"),
                partner.getMemberId(),
                "CASE",
                targetCase.getCaseId(),
                AnalysisType.CASE_SUMMARY,
                "임금체불 사건의 핵심 쟁점과 필요한 증거를 요약해줘."
        );
        require(aiRequestDAO.insert(aiRequest), "AI 분석 요청 저장");
        require(aiRequestDAO.updateStatus(aiRequest.getAiAnalysisRequestId(), AIRequestStatus.PROCESSING, null),
                "AI 분석 요청 처리 중 상태 반영");
        AIAnalysisResult aiResult = aiFunction.execute(aiRequest);
        require(aiResult != null && aiResult.saveResult(), "AI 분석 결과 객체 생성");
        aiResult.markReviewed(partner.getMemberId());
        require(aiResultDAO.insert(aiResult), "AI 분석 결과 저장");
        require(aiRequestDAO.updateStatus(aiRequest.getAiAnalysisRequestId(), AIRequestStatus.COMPLETED, null),
                "AI 분석 요청 완료 상태 반영");

        printStep("저장된 데이터를 DB에서 다시 조회한다");
        printCaseSummary(caseDAO.findById(targetCase.getCaseId()));
        printList("의뢰인별 사건 목록", caseDAO.findByClientId(client.getClientId()));
        printList("담당 변호사별 사건 목록", caseDAO.findByAssignedLawyerId(partner.getLawyerId()));
        printList("사건 증거 자료", evidenceDAO.findByCaseId(targetCase.getCaseId()));
        printList("사건 진행 기록", progressionRecordDAO.findByCaseId(targetCase.getCaseId()));
        printList("사건 유사 판례", similarPrecedentDAO.findByCaseId(targetCase.getCaseId()));
        printList("사건 문서", caseDocumentDAO.findByCaseId(targetCase.getCaseId()));
        printList("변호사 상담 신청 목록", consultationRequestDAO.findByLawyerId(partner.getLawyerId()));
        printList("의뢰인 수임 요청 목록", retainerRequestDAO.findByClientId(client.getClientId()));
        printList("수임 조건 목록", retainerConditionDAO.findByRequestId(retainerRequest.getRetainerRequestId()));
        printList("문서 전자 서명 목록", electronicSignatureDAO.findByDocumentId(document.getDocumentId()));
        printList("사건 AI 분석 결과", aiResultDAO.findByCaseId(targetCase.getCaseId()));

        printTitle("샘플 시나리오 종료: " + runId);
    }

    private void createClientFromInput() {
        printStep("의뢰인 직접 입력");
        String runId = "input-" + System.currentTimeMillis();
        Client client = new Client(
                prompt("회원 ID", id(runId, "member-client")),
                prompt("의뢰인 ID", id(runId, "client")),
                prompt("이름", "의뢰인A"),
                prompt("이메일", runId + "+client@example.com"),
                prompt("비밀번호", "pw1234"),
                prompt("전화번호", "010-0000-0000"),
                prompt("주소", "서울특별시")
        );
        require(clientDAO.insert(client), "입력한 의뢰인 저장");
        System.out.println(formatRow(clientDAO.findById(client.getClientId())));
    }

    private void createPartnerLawyerFromInput() {
        printStep("대표 변호사 직접 입력");
        String runId = "input-" + System.currentTimeMillis();
        PartnerLawyer partner = new PartnerLawyer(
                prompt("회원 ID", id(runId, "member-partner")),
                prompt("변호사 ID", id(runId, "lawyer-partner")),
                prompt("관리 변호사 ID", id(runId, "login-partner")),
                prompt("이름", "대표변호사A"),
                prompt("이메일", runId + "+partner@example.com"),
                prompt("비밀번호", "pw1234"),
                prompt("전화번호", "010-1111-1111"),
                prompt("변호사 등록번호", "LAW-" + runId),
                prompt("사무실 위치", "서울특별시 서초구"),
                readCsv("전문 분야 CSV", "민사,노동"),
                prompt("소개", "텍스트 DB 데모용 대표 변호사")
        );
        require(partnerLawyerDAO.insert(partner), "입력한 대표 변호사 저장");
        System.out.println(formatRow(partnerLawyerDAO.findByLawyerId(partner.getLawyerId())));
    }

    private void createCaseFromInput() {
        printStep("사건 정보와 사건 직접 입력");
        String runId = "input-" + System.currentTimeMillis();
        String clientId = prompt("기존 의뢰인 ID", "client-c1");
        String lawyerId = prompt("담당 변호사 ID(없으면 Enter)", "");
        CaseInfo caseInfo = new CaseInfo(
                prompt("사건 정보 ID", id(runId, "case-info")),
                clientId,
                prompt("사건 제목", "계약 위반 손해배상 사건"),
                readCaseCategory(),
                prompt("현재 단계", "초기 상담"),
                prompt("사실관계", "상대방이 계약상 의무를 이행하지 않아 손해가 발생했다.")
        );
        caseInfo.setRegion(prompt("지역", "서울특별시"));
        caseInfo.addKeywords(readCsv("키워드 CSV", "계약,손해배상"));
        require(caseInfoDAO.insert(caseInfo), "입력한 사건 정보 저장");

        Case targetCase = Case.createCase(caseInfo);
        require(targetCase != null, "입력 사건 객체 생성");
        if (!lawyerId.trim().isEmpty()) {
            targetCase.assignLawyer(lawyerId);
        }
        require(caseDAO.insert(targetCase), "입력한 사건 저장");
        caseInfo.setCaseId(targetCase.getCaseId());
        require(caseInfoDAO.update(caseInfo), "입력 사건 정보와 사건 연결");
        printCaseSummary(caseDAO.findById(targetCase.getCaseId()));
    }

    private void createEvidenceFromInput() {
        printStep("증거 자료 직접 입력");
        String runId = "input-" + System.currentTimeMillis();
        Evidence evidence = new Evidence(
                prompt("증거 ID", id(runId, "evidence")),
                prompt("사건 ID", "case-"),
                prompt("파일명", "evidence.pdf"),
                prompt("파일 유형", "pdf"),
                prompt("파일 경로", "/data/evidence/evidence.pdf"),
                prompt("설명", "텍스트 입력으로 등록한 증거")
        );
        require(evidenceDAO.insert(evidence), "입력한 증거 저장");
        System.out.println(formatRow(evidenceDAO.findById(evidence.getEvidenceId())));
    }

    private void listCasesByClientFromInput() {
        printList("의뢰인별 사건 목록", caseDAO.findByClientId(prompt("의뢰인 ID", "client-c1")));
    }

    private void listEvidenceByCaseFromInput() {
        printList("사건 증거 자료", evidenceDAO.findByCaseId(prompt("사건 ID", "case-")));
    }

    private void listConsultationsByLawyerFromInput() {
        printList("변호사 상담 신청 목록", consultationRequestDAO.findByLawyerId(prompt("변호사 ID", "lawyer-p1")));
    }

    private String prompt(String label, String defaultValue) {
        System.out.print(label + " [기본값: " + defaultValue + "]: ");
        String input = scanner.nextLine().trim();
        if (input.isEmpty()) {
            return defaultValue;
        }
        return input;
    }

    private List<String> readCsv(String label, String defaultValue) {
        return Arrays.stream(prompt(label, defaultValue).split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .collect(Collectors.toList());
    }

    private CaseCategory readCaseCategory() {
        String names = Arrays.stream(CaseCategory.values())
                .map(Enum::name)
                .collect(Collectors.joining(","));
        while (true) {
            String value = prompt("사건 카테고리(" + names + ")", CaseCategory.CIVIL.name());
            try {
                return CaseCategory.valueOf(value.toUpperCase());
            } catch (IllegalArgumentException e) {
                System.out.println("지원하지 않는 카테고리이다. 다시 입력한다.");
            }
        }
    }

    private static String id(String runId, String suffix) {
        return runId + "-" + suffix;
    }

    private static void require(boolean success, String action) {
        if (!success) {
            throw new IllegalStateException("[DBDemo] 실패: " + action);
        }
        System.out.println("[OK] " + action);
    }

    private static void printCaseSummary(Case targetCase) {
        System.out.println("\n=== 단건 사건 조회 ===");
        if (targetCase == null) {
            System.out.println("조회된 사건이 없습니다.");
            return;
        }
        System.out.println("사건 ID: " + targetCase.getCaseId());
        System.out.println("제목: " + targetCase.getTitle());
        System.out.println("담당 변호사 ID: " + targetCase.getAssignedLawyerId());
        System.out.println("상태: " + targetCase.getCaseStatus());
        System.out.println("키워드: " + targetCase.getKeywords());
    }

    private static void printList(String title, List<?> rows) {
        System.out.println("\n=== " + title + " (" + rows.size() + "건) ===");
        if (rows.isEmpty()) {
            System.out.println("조회 결과가 없습니다.");
            return;
        }

        for (Object row : rows) {
            System.out.println(formatRow(row));
            System.out.println("-----------------------------");
        }
    }

    private static String formatRow(Object row) {
        if (row == null) {
            return "조회 결과가 없습니다.";
        }

        if (row instanceof Client) {
            Client client = (Client) row;
            return "의뢰인 ID: " + client.getClientId()
                    + "\n회원 ID: " + client.getMemberId()
                    + "\n이름: " + client.getName()
                    + "\n이메일: " + client.getEmail()
                    + "\n전화번호: " + client.getPhoneNumber()
                    + "\n주소: " + client.getAddress();
        }

        if (row instanceof PartnerLawyer) {
            PartnerLawyer partner = (PartnerLawyer) row;
            return "대표 변호사 ID: " + partner.getLawyerId()
                    + "\n회원 ID: " + partner.getMemberId()
                    + "\n이름: " + partner.getName()
                    + "\n이메일: " + partner.getEmail()
                    + "\n전화번호: " + partner.getPhoneNumber()
                    + "\n등록번호: " + partner.getLicenseNumber()
                    + "\n사무실: " + partner.getOfficeLocation()
                    + "\n전문 분야: " + partner.getSpecialty()
                    + "\n소개: " + partner.getIntroduction();
        }

        if (row instanceof Evidence) {
            Evidence evidence = (Evidence) row;
            return "증거 ID: " + evidence.getEvidenceId()
                    + "\n사건 ID: " + evidence.getCaseId()
                    + "\n파일명: " + evidence.getFileName()
                    + "\n파일 유형: " + evidence.getFileType()
                    + "\n파일 경로: " + evidence.getFilePath()
                    + "\n설명: " + evidence.getDescription();
        }

        if (row instanceof ProgressionRecord) {
            ProgressionRecord record = (ProgressionRecord) row;
            return "진행 기록 ID: " + record.getProgressId()
                    + "\n사건 ID: " + record.getCaseId()
                    + "\n작성자 ID: " + record.getWriterId()
                    + "\n진행 상태: " + record.getProgressStatus()
                    + "\n설명: " + record.getDescription()
                    + "\n최근 조치: " + record.getRecentAction()
                    + "\n요청 자료: " + record.getRequestedMaterial();
        }

        if (row instanceof SimilarPrecedent) {
            SimilarPrecedent precedent = (SimilarPrecedent) row;
            return "판례 ID: " + precedent.getPrecedentId()
                    + "\n사건 ID: " + precedent.getCaseId()
                    + "\n판례명: " + precedent.getPrecedentTitle()
                    + "\n법원: " + precedent.getCourtName()
                    + "\n사건번호: " + precedent.getCaseNumber()
                    + "\n선고일: " + precedent.getDecisionDate()
                    + "\n쟁점: " + precedent.getLegalIssue()
                    + "\n유사도: " + precedent.getSimilarityScore()
                    + "\n선택 여부: " + precedent.isSelected();
        }

        if (row instanceof CaseDocument) {
            CaseDocument document = (CaseDocument) row;
            return "문서 ID: " + document.getDocumentId()
                    + "\n사건 ID: " + document.getCaseId()
                    + "\n문서 유형: " + document.getDocumentType()
                    + "\n제목: " + document.getTitle()
                    + "\n버전: " + document.getVersion()
                    + "\n서명 여부: " + document.isSigned()
                    + "\n파일 경로: " + document.getFilePath()
                    + "\n내용: " + document.getContent();
        }

        return String.valueOf(row);
    }

    private static void printTitle(String title) {
        System.out.println("\n==================================================");
        System.out.println(title);
        System.out.println("==================================================");
    }

    private static void printStep(String title) {
        System.out.println("\n>>> " + title);
    }
}
