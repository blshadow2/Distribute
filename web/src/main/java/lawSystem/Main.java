package lawSystem;

import lawSystem.ai.AIAnalysisResult;
import lawSystem.ai.CaseAnalysisReport;
import lawSystem.ai.CaseKeywordsExtract;
import lawSystem.ai.CaseSummary;
import lawSystem.ai.DocumentDraft;
import lawSystem.ai.DocumentDraftResult;
import lawSystem.ai.LegalRuleAnalysis;
import lawSystem.ai.LegalRuleAnalysisResult;
import lawSystem.ai.PrecedentAnalysisResult;
import lawSystem.ai.RecommendLawyers;
import lawSystem.ai.RecommendedLawyer;
import lawSystem.ai.SimilarPrecedentsAnalysis;

import lawSystem.consultation.ConsultationRequest;
import lawSystem.consultation.ConsultationSchedule;

import lawSystem.legalCase.Case;
import lawSystem.legalCase.CaseCategory;
import lawSystem.legalCase.CaseDocument;
import lawSystem.legalCase.CaseInfo;
import lawSystem.legalCase.Evidence;
import lawSystem.legalCase.ProgressionRecord;

import lawSystem.member.AssociateLawyer;
import lawSystem.member.Client;
import lawSystem.member.PartnerLawyer;
import lawSystem.member.Staff;

import lawSystem.retainer.RetainerCondition;
import lawSystem.retainer.RetainerRequest;

import lawSystem.verification.ElectronicSignature;
import lawSystem.verification.VerificationResult;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        printTitle("AI 기반 법률 사무 플랫폼 전체 시나리오 텍스트 테스트");

        /*
         * 1. 사용자 객체 생성
         */
        Client client = new Client(
                "member-client-001",
                "client001",
                "의뢰인A",
                "client@test.com",
                "1234",
                "010-1111-2222",
                "서울시"
        );

        PartnerLawyer partnerLawyer = new PartnerLawyer(
                "member-partner-001",
                "lawyer-partner-001",
                "partner001",
                "대표변호사A",
                "partner@test.com",
                "1234",
                "010-3333-4444",
                "LAW-1001",
                "서울사무소",
                Arrays.asList("민사", "손해배상"),
                "민사 사건 전문 대표 변호사입니다."
        );

        AssociateLawyer associateLawyer = new AssociateLawyer(
                "member-associate-001",
                "lawyer-associate-001",
                "associate001",
                "소속변호사A",
                "associate@test.com",
                "1234",
                "010-5555-6666",
                "LAW-2001",
                "서울사무소",
                Arrays.asList("민사", "계약분쟁"),
                "계약 분쟁과 손해배상 사건을 담당합니다."
        );

        Staff staff = new Staff(
                "member-staff-001",
                "staff001",
                "사무직원A",
                "staff@test.com",
                "1234",
                "010-7777-8888",
                "상담관리팀",
                "사무직원"
        );

        /*
         * 2. 로그인
         */
        printStep("1. 로그인");

        client.login("client@test.com", "1234");
        partnerLawyer.login("partner@test.com", "1234");
        associateLawyer.login("associate@test.com", "1234");
        staff.login("staff@test.com", "1234");

        System.out.println("의뢰인 로그인 상태: " + client.isLoggedIn());
        System.out.println("대표 변호사 로그인 상태: " + partnerLawyer.isLoggedIn());
        System.out.println("소속 변호사 로그인 상태: " + associateLawyer.isLoggedIn());
        System.out.println("사무 직원 로그인 상태: " + staff.isLoggedIn());

        /*
         * 3. 사건 정보를 입력한다
         */
        printStep("2. 사건 정보를 입력한다");

        CaseInfo caseInfo = CaseInfo.createCaseInfo(
                client.getClientId(),
                "계약 위반에 따른 손해배상 사건",
                CaseCategory.CIVIL,
                "소송 준비",
                "상대방이 계약상 의무를 이행하지 않아 금전적 손해가 발생하였다."
        );

        Case createdCase = client.inputCaseInfo(caseInfo);

        if (createdCase == null) {
            System.out.println("사건 정보 입력에 실패했습니다. 테스트를 종료합니다.");
            return;
        }

        System.out.println("사건 정보가 저장되었습니다.");
        System.out.println(createdCase);

        /*
         * 4. 증거자료를 등록한다
         */
        printStep("3. 증거자료를 등록한다");

        Evidence evidence = new Evidence(
                "evidence001",
                createdCase.getCaseId(),
                "contract.pdf",
                "문서",
                "/test/contract.pdf",
                "계약 체결 사실을 증명하는 계약서 파일"
        );

        boolean evidenceUploaded = client.uploadEvidence(createdCase.getCaseId(), evidence);

        if (evidenceUploaded) {
            createdCase.addEvidence(evidence);
            System.out.println("증거자료가 사건에 등록되었습니다.");
        }

        /*
         * 5. AI - 사건 키워드를 추출한다
         */
        printStep("4. AI 기능 - 사건 키워드를 추출한다");

        CaseKeywordsExtract keywordAI = new CaseKeywordsExtract();

        AIAnalysisResult keywordResult = keywordAI.extractKeywords(
                createdCase.getCaseId(),
                "계약 위반에 따른 손해배상 사건",
                "상대방이 계약상 의무를 이행하지 않아 손해가 발생하였다."
        );

        System.out.println(keywordResult);

        createdCase.saveKeywords(
                Arrays.asList("계약 위반", "손해배상", "민사소송", "증거자료")
        );

        /*
         * 6. 상담 일정을 관리한다
         */
        printStep("5. 상담 일정을 관리한다");

        ConsultationSchedule schedule = new ConsultationSchedule(
                "schedule001",
                partnerLawyer.getLawyerId(),
                LocalDateTime.now().plusDays(3),
                60
        );

        staff.registerConsultationSchedule(schedule);

        System.out.println(schedule);

        /*
         * 7. 상담을 신청한다
         */
        printStep("6. 상담을 신청한다");

        ConsultationRequest consultationRequest =
                ConsultationRequest.createConsultationRequest(
                        createdCase.getCaseId(),
                        client.getClientId(),
                        partnerLawyer.getLawyerId(),
                        schedule.getScheduleId(),
                        "계약 위반 사건에 대해 상담을 신청합니다."
                );

        staff.addConsultationRequest(consultationRequest);
        partnerLawyer.addConsultationRequest(consultationRequest);

        System.out.println("상담 신청이 생성되었습니다.");
        System.out.println(consultationRequest);

        /*
         * 8. 상담 신청에 응답한다
         */
        printStep("7. 사무 직원이 상담 신청에 응답한다");

        boolean consultationApproved =
                staff.approveConsultationRequest(
                        consultationRequest.getConsultationRequestId()
                );

        System.out.println("상담 신청 승인 결과: " + consultationApproved);
        System.out.println("상담 신청 상태: " + consultationRequest.getRequestStatus());
        System.out.println("상담 일정 상태: " + schedule.getAvailableStatus());

        /*
         * 9. 사건 수임을 요청한다
         */
        printStep("8. 사건 수임을 요청한다");

        RetainerRequest retainerRequest =
                RetainerRequest.createRetainerRequest(
                        createdCase.getCaseId(),
                        client.getClientId(),
                        partnerLawyer.getLawyerId(),
                        "계약 위반 사건에 대한 소송 대리를 요청합니다.",
                        "소장 작성 및 1심 소송 대리",
                        3000000,
                        "손해배상 청구 인용"
                );

        boolean retainerRequestSent = retainerRequest.sendRequest();

        if (retainerRequestSent) {
            partnerLawyer.addRetainerRequest(retainerRequest);
            System.out.println("수임 요청이 대표 변호사에게 전달되었습니다.");
        }

        System.out.println(retainerRequest);

        /*
         * 10. 수임 조건을 전달한다
         */
        printStep("9. 대표 변호사가 수임 조건을 전달한다");

        RetainerCondition condition =
                RetainerCondition.createCondition(
                        retainerRequest.getRetainerRequestId(),
                        3500000,
                        "소장 작성, 준비서면 작성, 1심 소송 대리",
                        "인지대 및 송달료는 별도 부담"
                );

        boolean conditionSent =
                partnerLawyer.sendRetainerCondition(
                        retainerRequest.getRetainerRequestId(),
                        condition
                );

        System.out.println("수임 조건 전달 결과: " + conditionSent);
        System.out.println("수임 요청 상태: " + retainerRequest.getRequestStatus());

        /*
         * 11. 수임 조건에 응답한다
         */
        printStep("10. 의뢰인이 수임 조건에 응답한다");

        boolean conditionAccepted =
                retainerRequest.acceptCondition(condition.getConditionId());

        if (conditionAccepted) {
            condition.acceptCondition();
            System.out.println("의뢰인이 수임 조건에 동의했습니다.");
        }

        System.out.println("수임 조건 상태: " + condition.getConditionStatus());
        System.out.println("수임 요청 상태: " + retainerRequest.getRequestStatus());

        /*
         * 12. 사건을 수임한다
         */
        printStep("11. 대표 변호사가 사건을 수임한다");

        boolean retained =
                partnerLawyer.acceptRetainer(
                        retainerRequest.getRetainerRequestId()
                );

        System.out.println("사건 수임 처리 결과: " + retained);
        System.out.println("수임 요청 상태: " + retainerRequest.getRequestStatus());

        /*
         * 13. 본인 인증을 한다
         */
        printStep("12. 본인 인증을 한다");

        VerificationResult verificationResult =
                VerificationResult.requestVerification(
                        client.getMemberId(),
                        "PHONE"
                );

        boolean verified = verificationResult.checkVerificationCode("123456");

        System.out.println("본인 인증 결과: " + verified);
        System.out.println(verificationResult);

        /*
         * 14. 수임 문서에 전자 서명한다
         */
        printStep("13. 사건 수임 문서에 서명한다");

        CaseDocument retainerDocument = new CaseDocument(
                "document-retainer-001",
                createdCase.getCaseId(),
                "수임계약서",
                "수임 계약서",
                "의뢰인과 대표 변호사 사이의 수임 계약 내용입니다.",
                partnerLawyer.getLawyerId()
        );

        createdCase.addCaseDocument(retainerDocument);

        ElectronicSignature signature =
                ElectronicSignature.createSignature(
                        retainerDocument.getDocumentId(),
                        client.getMemberId(),
                        verificationResult
                );

        if (signature != null && signature.validateSignature()) {
            retainerDocument.signDocument(client.getMemberId());
            System.out.println("전자 서명이 완료되었습니다.");
            System.out.println(signature);
        } else {
            System.out.println("전자 서명에 실패했습니다.");
        }

        /*
         * 15. 사건을 배당한다
         */
        printStep("14. 사건을 배당한다");

        boolean assignedByPartner =
                partnerLawyer.assignCase(
                        createdCase.getCaseId(),
                        associateLawyer.getLawyerId()
                );

        boolean assignedToAssociate =
                associateLawyer.addAssignedCase(createdCase);

        createdCase.assignLawyer(associateLawyer.getLawyerId());

        System.out.println("대표 변호사의 사건 배당 결과: " + assignedByPartner);
        System.out.println("소속 변호사의 담당 사건 등록 결과: " + assignedToAssociate);
        System.out.println("소속 변호사 현재 업무량: " + associateLawyer.getCurrentWorkload());

        /*
         * 16. AI - 사건 배당 변호사를 추천한다
         */
        printStep("15. AI 기능 - 사건 배당 변호사를 추천한다");

        RecommendLawyers recommendLawyersAI = new RecommendLawyers();

        List<RecommendedLawyer> recommendedLawyers =
                recommendLawyersAI.recommendLawyers(
                        createdCase.getCaseId(),
                        Arrays.asList(
                                associateLawyer.getLawyerId(),
                                "lawyer-associate-002"
                        )
                );

        for (RecommendedLawyer recommendedLawyer : recommendedLawyers) {
            System.out.println(recommendedLawyer);
            System.out.println();
        }

        /*
         * 17. AI - 사건 내용을 요약한다
         */
        printStep("16. AI 기능 - 사건 내용을 요약한다");

        CaseSummary caseSummaryAI = new CaseSummary();

        CaseAnalysisReport caseAnalysisReport =
                caseSummaryAI.summarizeCase(
                        createdCase.getCaseId(),
                        caseInfo.getFactDescription()
                );

        System.out.println(caseAnalysisReport);
        System.out.println("요약 내용: " + caseAnalysisReport.getSummary());
        System.out.println("주요 쟁점: " + caseAnalysisReport.getMainIssues());
        System.out.println("타임라인: " + caseAnalysisReport.getTimeline());

        /*
         * 18. AI - 유사한 판례를 정리한다
         */
        printStep("17. AI 기능 - 유사한 판례를 정리한다");

        SimilarPrecedentsAnalysis precedentAI = new SimilarPrecedentsAnalysis();

        List<PrecedentAnalysisResult> precedentResults =
                precedentAI.searchSimilarPrecedents(
                        createdCase.getCaseId(),
                        caseAnalysisReport.getSummary()
                );

        for (PrecedentAnalysisResult precedentResult : precedentResults) {
            System.out.println(precedentResult);
            System.out.println();
        }

        /*
         * 19. AI - 서면 초안을 작성한다
         */
        printStep("18. AI 기능 - 서면의 초안을 작성한다");

        DocumentDraft documentDraftAI = new DocumentDraft();

        DocumentDraftResult draftResult =
                documentDraftAI.generateDraft(
                        createdCase.getCaseId(),
                        "소장",
                        Arrays.asList("계약 위반", "손해배상 책임", "입증 책임")
                );

        System.out.println(draftResult);
        System.out.println("초안 ID: " + draftResult.getDraftResultId());
        System.out.println("초안 내용: " + draftResult.getDraftContent());

        /*
         * 20. AI 초안을 수정한다
         */
        printStep("19. AI 초안을 수정한다");

        DocumentDraftResult revisedDraftResult =
                documentDraftAI.reviseDraft(
                        draftResult,
                        "피고의 계약 위반 사실과 손해 발생 사이의 인과관계를 더 명확히 작성한다."
                );

        System.out.println(revisedDraftResult);
        System.out.println("수정 초안 ID: " + revisedDraftResult.getDraftResultId());

        /*
         * 21. 사건 자료를 정리한다
         */
        printStep("20. 소속 변호사가 사건 자료를 정리하고 문서를 작성한다");

        CaseDocument caseDocument =
                associateLawyer.writeCaseDocument(
                        createdCase.getCaseId(),
                        "소장"
                );

        if (caseDocument != null) {
            caseDocument.updateDocument(revisedDraftResult.getDraftContent());
            System.out.println("사건 문서가 작성되었습니다.");
            System.out.println("문서 ID: " + caseDocument.getDocumentId());
            System.out.println("문서 유형: " + caseDocument.getDocumentType());
        }

        /*
         * 22. AI - 사건 관련 문서의 법리를 설명한다
         */
        printStep("21. AI 기능 - 사건 관련 문서의 법리를 설명한다");

        LegalRuleAnalysis legalRuleAI = new LegalRuleAnalysis();

        LegalRuleAnalysisResult legalRuleResult =
                legalRuleAI.analyzeLegalIssue(
                        caseDocument.getDocumentId(),
                        createdCase.getCaseId()
                );

        System.out.println(legalRuleResult.renderForClient());

        /*
         * 23. 사건 진행상황을 공유한다
         */
        printStep("22. 소속 변호사가 사건 진행상황을 공유한다");

        ProgressionRecord progressionRecord =
                ProgressionRecord.createProgressRecord(
                        createdCase.getCaseId(),
                        associateLawyer.getLawyerId(),
                        "소장 초안 작성 완료",
                        "AI 초안을 바탕으로 소장 초안을 작성하고 법리 검토를 진행했습니다.",
                        "소장 초안 작성 및 법리 검토"
                );

        boolean progressShared =
                associateLawyer.shareCaseProgress(
                        createdCase.getCaseId(),
                        progressionRecord
                );

        System.out.println("진행상황 공유 결과: " + progressShared);

        /*
         * 24. 필요한 자료를 요청한다
         */
        printStep("23. 소속 변호사가 의뢰인에게 추가 자료를 요청한다");

        boolean materialRequested =
                associateLawyer.requestClientMaterial(
                        createdCase.getCaseId(),
                        "계약 체결 당시 주고받은 문자 내역과 입금 내역을 추가로 제출해 주세요."
                );

        System.out.println("자료 요청 결과: " + materialRequested);

        /*
         * 25. 의뢰인이 사건 진행상황을 열람한다
         */
        printStep("24. 의뢰인이 사건 진행상황을 열람한다");

        System.out.println("의뢰인이 사건 진행 정보를 확인합니다.");
        System.out.println(createdCase);

        /*
         * 26. 테스트 종료
         */
        printTitle("전체 시나리오 테스트 종료");

        client.logout();
        partnerLawyer.logout();
        associateLawyer.logout();
        staff.logout();

        System.out.println("의뢰인 등록 사건 수: " + client.getRegisteredCaseCount());
        System.out.println("테스트가 정상적으로 종료되었습니다.");
    }

    private static void printTitle(String title) {
        System.out.println();
        System.out.println("==================================================");
        System.out.println(title);
        System.out.println("==================================================");
    }

    private static void printStep(String stepTitle) {
        System.out.println();
        System.out.println("--------------------------------------------------");
        System.out.println(stepTitle);
        System.out.println("--------------------------------------------------");
    }
}