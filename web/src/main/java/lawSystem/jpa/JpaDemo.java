package lawSystem.jpa;

import lawSystem.ai.AIRequestStatus;
import lawSystem.ai.AnalysisType;
import lawSystem.consultation.ConsultationStatus;
import lawSystem.consultation.ScheduleStatus;
import lawSystem.jpa.entity.AIAnalysisFunction;
import lawSystem.jpa.entity.AIAnalysisRequest;
import lawSystem.jpa.entity.AIAnalysisResult;
import lawSystem.jpa.entity.CaseDocument;
import lawSystem.jpa.entity.CaseInfo;
import lawSystem.jpa.entity.Client;
import lawSystem.jpa.entity.ConsultationRequest;
import lawSystem.jpa.entity.ConsultationSchedule;
import lawSystem.jpa.entity.ElectronicSignature;
import lawSystem.jpa.entity.Evidence;
import lawSystem.jpa.entity.LegalCase;
import lawSystem.jpa.entity.PartnerLawyer;
import lawSystem.jpa.entity.ProgressionRecord;
import lawSystem.jpa.entity.RetainerCondition;
import lawSystem.jpa.entity.RetainerRequest;
import lawSystem.jpa.entity.SimilarPrecedent;
import lawSystem.jpa.entity.VerificationResult;
import lawSystem.jpa.repository.ClientRepository;
import lawSystem.jpa.repository.ConsultationRequestRepository;
import lawSystem.jpa.repository.LegalCaseRepository;
import lawSystem.jpa.repository.PartnerLawyerRepository;
import lawSystem.jpa.repository.RetainerRequestRepository;
import lawSystem.legalCase.CaseCategory;
import lawSystem.legalCase.CaseStatus;
import lawSystem.retainer.RetainerStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

/**
 * JPA 기반 데이터 흐름을 검증하는 데모이다.
 *
 * <p>DAO 기반 DBDemo 와 동일한 시나리오를 JPA 엔티티/리포지토리만으로 수행한다.
 *    - 의뢰인/대표변호사 생성
 *    - 사건정보 → 사건 변환
 *    - 증거/문서/진행기록/유사판례 추가
 *    - 상담 일정/신청
 *    - 수임 요청/조건
 *    - 본인 인증/전자 서명
 *    - AI 분석 요청/결과
 *    - 저장된 데이터를 다시 조회</p>
 *
 * <p>매 실행마다 고유한 runId 를 prefix 로 붙여 PK 중복을 회피한다.</p>
 */
public class JpaDemo {

    private final ClientRepository clientRepo = new ClientRepository();
    private final PartnerLawyerRepository partnerRepo = new PartnerLawyerRepository();
    private final LegalCaseRepository caseRepo = new LegalCaseRepository();
    private final ConsultationRequestRepository consultRepo = new ConsultationRequestRepository();
    private final RetainerRequestRepository retainerRepo = new RetainerRequestRepository();

    public static void main(String[] args) {
        try {
            new JpaDemo().run();
        } finally {
            JpaManager.shutdown();
        }
    }

    public void run() {
        String runId = "jpa-" + System.currentTimeMillis();
        printTitle("JPA 기반 시연 시작: " + runId);

        // --- 1) 의뢰인 / 대표 변호사 저장 -----------------------------
        Client client = new Client(
                id(runId, "client"),
                "홍길동",
                runId + "+client@example.com",
                "pw1234",
                "010-1111-2222",
                "서울특별시 종로구"
        );
        clientRepo.save(client);
        log("의뢰인 저장: " + client.getMemberId());

        PartnerLawyer partner = new PartnerLawyer(
                id(runId, "partner"),
                "LAW-" + runId,
                "김변호",
                runId + "+partner@example.com",
                "pw1234",
                "010-3333-4444",
                "LAW-LICENSE-" + runId,
                "서울특별시 서초구",
                Arrays.asList("민사", "노동"),
                "10년차 민사/노동 전문 대표 변호사"
        );
        partnerRepo.save(partner);
        log("대표 변호사 저장: " + partner.getMemberId());

        // --- 2) 사건 정보 → 사건 ------------------------------------
        CaseInfo caseInfo = new CaseInfo(
                id(runId, "case-info"),
                client,
                "근로계약 미체결 임금체불 사건",
                CaseCategory.LABOR,
                "초기 상담",
                "2026년 1월부터 3개월 동안 임금이 지급되지 않았다."
        );
        caseInfo.setIncidentDate(LocalDate.of(2026, 1, 15));
        caseInfo.setRegion("서울특별시");
        caseInfo.setKeywords(Arrays.asList("임금체불", "근로계약", "노동"));

        LegalCase legalCase = new LegalCase(
                id(runId, "case"),
                client,
                caseInfo.getTitle(),
                caseInfo.getCategory(),
                caseInfo.getCurrentStage(),
                caseInfo.getFactDescription(),
                CaseStatus.INFO_REGISTERED
        );
        legalCase.setKeywords(caseInfo.getKeywords());
        legalCase.setAssignedLawyer(partner);

        // CaseInfo 와 Case 양방향 연결 후 단일 트랜잭션에 저장
        JpaManager.execute(em -> {
            em.persist(legalCase);
            caseInfo.setLegalCase(legalCase);
            em.persist(caseInfo);
        });
        log("사건 정보 + 사건 저장: " + legalCase.getCaseId());

        // --- 3) 증거 / 진행기록 / 유사 판례 / 사건 문서 ---------------
        JpaManager.execute(em -> {
            LegalCase managedCase = em.find(LegalCase.class, legalCase.getCaseId());

            Evidence evidence = new Evidence(
                    id(runId, "evidence"),
                    "급여명세서.pdf",
                    "pdf",
                    "/data/evidence/급여명세서.pdf",
                    "임금 미지급 사실 확인용 급여명세서"
            );
            managedCase.addEvidence(evidence);

            ProgressionRecord progress = new ProgressionRecord(
                    id(runId, "progress"),
                    partner.getMemberId(),
                    "상담 준비",
                    "급여명세서 검토 완료",
                    "근로계약서 사본 요청"
            );
            progress.setRequestedMaterial("출퇴근 기록");
            managedCase.addProgressionRecord(progress);

            SimilarPrecedent precedent = new SimilarPrecedent(
                    id(runId, "precedent"),
                    "임금체불 손해배상 사건",
                    "2024가단12345"
            );
            precedent.setCourtName("서울중앙지방법원");
            precedent.setDecisionDate(LocalDate.of(2024, 6, 12));
            precedent.setPrecedentSummary("근로계약서 미교부와 반복 임금체불이 인정된 사례");
            precedent.setLegalIssue("임금 지급 의무 위반");
            precedent.setAppliedLegalRule("근로기준법 제17조, 제36조");
            precedent.setSimilarityScore(0.82);
            precedent.setSelected(true);
            precedent.setRegisteredBy(partner.getMemberId());
            managedCase.addSimilarPrecedent(precedent);

            CaseDocument document = new CaseDocument(
                    id(runId, "document"),
                    "내용증명",
                    "임금 지급 요청 내용증명 초안",
                    "미지급 임금 및 지연이자 지급 요청",
                    partner.getMemberId()
            );
            document.setFilePath("/data/documents/임금지급요청.docx");
            managedCase.addDocument(document);
            // cascade=ALL 이므로 별도 persist 불필요
        });
        log("증거/진행기록/유사판례/문서 저장 완료");

        // --- 4) 상담 일정 + 상담 신청 -------------------------------
        ConsultationSchedule schedule = new ConsultationSchedule(
                id(runId, "schedule"),
                partner,
                LocalDateTime.of(2026, 5, 20, 10, 0),
                30
        );

        ConsultationRequest consultationRequest = new ConsultationRequest(
                id(runId, "consultation"),
                legalCase,
                client,
                partner,
                schedule,
                "임금체불 관련 상담 부탁드립니다."
        );

        JpaManager.execute(em -> {
            em.persist(schedule);
            em.persist(consultationRequest);
            // 상담 승인 → 일정 예약 → 사건 상태 변경
            ConsultationRequest managed = em.find(ConsultationRequest.class, consultationRequest.getConsultationRequestId());
            managed.setRequestStatus(ConsultationStatus.APPROVED);
            ConsultationSchedule managedSchedule = em.find(ConsultationSchedule.class, schedule.getScheduleId());
            managedSchedule.setAvailableStatus(ScheduleStatus.RESERVED);
            LegalCase managedCase = em.find(LegalCase.class, legalCase.getCaseId());
            managedCase.setCaseStatus(CaseStatus.CONSULTATION_REQUESTED);
        });
        log("상담 일정/신청 저장 및 상태 변경 완료");

        // --- 5) 수임 요청 + 수임 조건 -------------------------------
        RetainerRequest retainerRequest = new RetainerRequest(
                id(runId, "retainer"),
                legalCase,
                client,
                partner,
                "임금 청구 소송 진행 요청",
                "내용증명 발송부터 1심까지",
                3_000_000,
                "미지급 임금 전액 회수"
        );

        RetainerCondition condition = new RetainerCondition(
                id(runId, "condition"),
                3_200_000,
                "내용증명, 지급명령, 1심 소송 대리",
                "실비는 별도 정산"
        );
        retainerRequest.addCondition(condition);

        JpaManager.execute(em -> {
            em.persist(retainerRequest);   // condition 은 cascade=ALL 로 함께 저장
            RetainerRequest managed = em.find(RetainerRequest.class, retainerRequest.getRetainerRequestId());
            managed.setRequestStatus(RetainerStatus.CONDITION_SENT);
        });
        log("수임 요청/조건 저장 완료");

        // --- 6) 본인 인증 + 전자 서명 --------------------------------
        VerificationResult verification = new VerificationResult(
                id(runId, "verification"),
                client,
                "휴대폰 인증"
        );
        verification.markVerified("PID-" + runId);

        String documentId = id(runId, "document");
        ElectronicSignature signature = JpaManager.query(em -> {
            em.persist(verification);
            CaseDocument managedDoc = em.find(CaseDocument.class, documentId);
            Client managedClient = em.find(Client.class, client.getMemberId());
            ElectronicSignature sig = new ElectronicSignature(
                    id(runId, "signature"),
                    managedDoc,
                    managedClient,
                    verification,
                    Integer.toHexString((documentId + runId).hashCode())
            );
            em.persist(sig);
            managedDoc.setSigned(true);
            return sig;
        });
        log("본인 인증/전자 서명 저장 완료: " + signature.getSignatureId());

        // --- 7) AI 분석 요청 + 결과 ----------------------------------
        AIAnalysisFunction aiFunction = new AIAnalysisFunction(
                id(runId, "ai-function"),
                "사건 요약",
                "model-v1",
                "ACTIVE"
        );

        JpaManager.execute(em -> em.persist(aiFunction));

        AIAnalysisRequest aiRequest = new AIAnalysisRequest(
                id(runId, "ai-request"),
                partner,
                "CASE",
                legalCase.getCaseId(),
                AnalysisType.CASE_SUMMARY,
                "임금체불 사건의 핵심 쟁점과 필요한 증거를 요약해줘."
        );

        AIAnalysisResult aiResult = new AIAnalysisResult(
                id(runId, "ai-result"),
                aiRequest,
                legalCase,
                AnalysisType.CASE_SUMMARY,
                "사건 ID [" + legalCase.getCaseId() + "] 의 사실관계, 증거자료, 주요 쟁점을 요약했습니다.",
                0.85
        );
        aiResult.markReviewed(partner.getMemberId());

        JpaManager.execute(em -> {
            em.persist(aiRequest);
            AIAnalysisRequest managedReq = em.find(AIAnalysisRequest.class, aiRequest.getAiAnalysisRequestId());
            managedReq.markProcessing();
            em.persist(aiResult);
            managedReq.markCompleted();
        });
        log("AI 분석 요청/결과 저장 완료");

        // --- 8) 저장된 데이터를 다시 조회 -----------------------------
        printTitle("DB에서 다시 조회");
        clientRepo.findById(client.getMemberId()).ifPresent(c -> log("의뢰인 단건 조회: " + c.getName()));

        List<LegalCase> casesByClient = caseRepo.findByClient(client.getMemberId());
        log("의뢰인별 사건 " + casesByClient.size() + "건");

        List<LegalCase> casesByLawyer = caseRepo.findByAssignedLawyer(partner.getMemberId());
        log("담당 변호사별 사건 " + casesByLawyer.size() + "건");

        // 연관 그래프 탐색: 사건 → 증거/문서/유사판례 (LAZY 이므로 트랜잭션 안에서)
        JpaManager.execute(em -> {
            LegalCase found = em.find(LegalCase.class, legalCase.getCaseId());
            log("사건 키워드: " + found.getKeywords());
            log("사건 증거 수: " + found.getEvidences().size());
            log("사건 문서 수: " + found.getDocuments().size());
            log("유사 판례 수: " + found.getSimilarPrecedents().size());
            log("진행 기록 수: " + found.getProgressionRecords().size());
        });

        List<ConsultationRequest> consults = consultRepo.findByLawyer(partner.getMemberId());
        log("변호사 상담 신청 " + consults.size() + "건");

        List<RetainerRequest> retainers = retainerRepo.findByClient(client.getMemberId());
        log("의뢰인 수임 요청 " + retainers.size() + "건");

        printTitle("JPA 시연 종료: " + runId);
    }

    private static String id(String runId, String suffix) {
        return runId + "-" + suffix;
    }

    private static void log(String message) {
        System.out.println("[JpaDemo] " + message);
    }

    private static void printTitle(String title) {
        System.out.println("\n==================================================");
        System.out.println(title);
        System.out.println("==================================================");
    }
}
