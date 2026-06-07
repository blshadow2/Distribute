package lawSystem.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import lawSystem.web.auth.AccessDeniedException;
import lawSystem.web.auth.LoginMember;
import lawSystem.web.auth.RoleAllowed;
import lawSystem.web.auth.SessionConst;
import lawSystem.web.dto.AiRequest;
import lawSystem.web.dto.AiResultResponse;
import lawSystem.web.dto.KeywordsResponse;
import lawSystem.web.dto.LegalRulesResponse;
import lawSystem.web.dto.PrecedentDto;
import lawSystem.web.dto.SummaryRequest;
import lawSystem.web.dto.SummaryResponse;
import lawSystem.web.service.AiWebService;
import lawSystem.web.service.CaseService;
import lawSystem.web.service.LawyerService;

/**
 * AI 기능 REST 컨트롤러 (첫 수직 슬라이스: 사건 요약).
 *
 *   POST /api/ai/summary        — 텍스트 요약 후 저장 (AIAnalysisService 재사용)
 *   GET  /api/ai/results/{id}   — 저장된 결과 조회 (Spring Data JPA)
 */
@RestController
@RequestMapping("/api/ai")
public class AiController {

    private final AiWebService aiWebService;
    private final CaseService caseService;
    private final LawyerService lawyerService;

    public AiController(AiWebService aiWebService, CaseService caseService, LawyerService lawyerService) {
        this.aiWebService = aiWebService;
        this.caseService = caseService;
        this.lawyerService = lawyerService;
    }

    @RoleAllowed({"ASSOCIATE", "PARTNER"})   // 의뢰인 차단 — 변호사 전용
    @PostMapping("/summary")
    public SummaryResponse summarize(@RequestBody SummaryRequest request, HttpSession session) {
        authorizeCase(request.getCaseId(), session);
        return aiWebService.summarize(request.getText(), request.getCaseId());
    }

    /** 키워드 추출 (미리보기). 저장은 /keywords/save 에서 명시적으로 수행한다. */
    @PostMapping("/keywords")
    public KeywordsResponse keywords(@RequestBody AiRequest request) {
        return aiWebService.extractKeywords(request.getText(), request.getMaxKeywords());
    }

    /**
     * 추출한 키워드 저장. (사용자가 "키워드 저장" 버튼을 눌렀을 때)
     *  ① 사건(legal_case)에 키워드 저장 → 사건 상세/변호사 검색에 반영
     *  ② 사건의 AI 분석 이력(ai_analysis_result)에 기록
     *  ③ 변호사가 실행한 경우 → 본인 전문분야(검색 대상)에 누적
     */
    @PostMapping("/keywords/save")
    public KeywordsResponse saveKeywords(@RequestBody AiRequest request, HttpSession session) {
        List<String> keywords = request.getKeywords();
        if (keywords == null || keywords.isEmpty()) {
            return new KeywordsResponse(List.of());
        }
        // 소유권 확인: 남의 사건에 키워드를 덮어쓰지 못하게 막는다.
        authorizeCase(request.getCaseId(), session);
        // ① 사건 키워드 필드 저장 (사건이 지정된 경우)
        if (request.getCaseId() != null && !request.getCaseId().isBlank()) {
            caseService.saveKeywords(request.getCaseId(), keywords);
        }
        // ② AI 분석 이력 저장 (항상 — 사건 미지정 시 case_id=null 로 기록)
        aiWebService.recordKeywords(request.getCaseId(), request.getText(), keywords);
        LoginMember m = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);   // ③
        if (m != null && ("PARTNER".equals(m.getViewRole()) || "ASSOCIATE".equals(m.getViewRole()))) {
            lawyerService.addKeywords(m.getMemberId(), keywords);
        }
        return new KeywordsResponse(keywords);
    }

    @RoleAllowed({"ASSOCIATE", "PARTNER"})   // 의뢰인 차단 — 변호사 전용
    @PostMapping("/similar-precedents")
    public List<PrecedentDto> similarPrecedents(@RequestBody AiRequest request, HttpSession session) {
        authorizeCase(request.getCaseId(), session);
        return aiWebService.similarPrecedents(request.getText(), request.getCaseId());
    }

    @PostMapping("/legal-rules")
    public LegalRulesResponse legalRules(@RequestBody AiRequest request, HttpSession session) {
        authorizeCase(request.getCaseId(), session);
        return aiWebService.analyzeLegalRules(request.getText(), request.getCaseId(), request.getTopK());
    }

    @GetMapping("/results/{id}")
    public AiResultResponse getResult(@PathVariable("id") String id) {
        return aiWebService.getResult(id);
    }

    /** caseId 가 지정된 경우, 로그인 사용자가 그 사건에 접근 권한이 있는지 확인한다. */
    private void authorizeCase(String caseId, HttpSession session) {
        if (caseId == null || caseId.isBlank()) {
            return;
        }
        LoginMember m = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (m == null) {
            throw new AccessDeniedException("로그인이 필요합니다.");
        }
        caseService.checkAccess(caseId, m.getMemberId(), m.getViewRole());
    }
}
