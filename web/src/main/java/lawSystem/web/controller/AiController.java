package lawSystem.web.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import jakarta.servlet.http.HttpSession;

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
    public SummaryResponse summarize(@RequestBody SummaryRequest request) {
        return aiWebService.summarize(request.getText(), request.getCaseId());
    }

    @PostMapping("/keywords")
    public KeywordsResponse keywords(@RequestBody AiRequest request, HttpSession session) {
        KeywordsResponse resp = aiWebService.extractKeywords(
                request.getText(), request.getMaxKeywords(), request.getCaseId());

        if (!resp.getKeywords().isEmpty()) {
            // ① 추출 키워드를 사건에 저장 → 변호사 검색 등에 그대로 사용
            if (request.getCaseId() != null && !request.getCaseId().isBlank()) {
                caseService.saveKeywords(request.getCaseId(), resp.getKeywords());
            }
            // ② 변호사가 실행한 경우 → 본인 전문분야(검색 대상)에 키워드 누적
            LoginMember m = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
            if (m != null && ("PARTNER".equals(m.getViewRole()) || "ASSOCIATE".equals(m.getViewRole()))) {
                lawyerService.addKeywords(m.getMemberId(), resp.getKeywords());
            }
        }
        return resp;
    }

    @RoleAllowed({"ASSOCIATE", "PARTNER"})   // 의뢰인 차단 — 변호사 전용
    @PostMapping("/similar-precedents")
    public List<PrecedentDto> similarPrecedents(@RequestBody AiRequest request) {
        return aiWebService.similarPrecedents(request.getText(), request.getCaseId());
    }

    @PostMapping("/legal-rules")
    public LegalRulesResponse legalRules(@RequestBody AiRequest request) {
        return aiWebService.analyzeLegalRules(request.getText(), request.getCaseId(), request.getTopK());
    }

    @GetMapping("/results/{id}")
    public AiResultResponse getResult(@PathVariable("id") String id) {
        return aiWebService.getResult(id);
    }
}
