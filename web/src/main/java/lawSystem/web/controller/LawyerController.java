package lawSystem.web.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lawSystem.web.auth.LoginMember;
import lawSystem.web.auth.RoleAllowed;
import lawSystem.web.auth.SessionConst;
import lawSystem.web.dto.CaseDto;
import lawSystem.web.service.CaseService;
import lawSystem.web.service.LawyerService;

/**
 * 변호사 검색 (의뢰인). 키워드/분야/지역 + 저장된 사건 키워드로 검색.
 */
@Controller
@RoleAllowed("CLIENT")
public class LawyerController {

    private final LawyerService lawyerService;
    private final CaseService caseService;

    public LawyerController(LawyerService lawyerService, CaseService caseService) {
        this.lawyerService = lawyerService;
        this.caseService = caseService;
    }

    @GetMapping("/lawyers")
    public String list(@RequestParam(value = "keyword", required = false) String keyword,
                       @RequestParam(value = "region", required = false) String region,
                       @RequestParam(value = "specialty", required = false) String specialty,
                       @RequestParam(value = "caseId", required = false) String caseId,
                       HttpSession session, Model model) {

        // 사건을 선택하면 그 사건에 저장된 키워드를 검색어로 사용한다.
        String effectiveKeyword = keyword;
        if (caseId != null && !caseId.isBlank()) {
            CaseDto c = caseService.get(caseId);
            if (c != null && c.getKeywords() != null && !c.getKeywords().isEmpty()) {
                effectiveKeyword = String.join(" ", c.getKeywords());
            }
        }

        LoginMember m = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        model.addAttribute("lawyers", lawyerService.search(effectiveKeyword, region, specialty));
        model.addAttribute("keyword", effectiveKeyword);
        model.addAttribute("region", region);
        model.addAttribute("specialty", specialty);
        model.addAttribute("cases", caseService.listForUser(m.getMemberId(), "CLIENT"));
        model.addAttribute("selectedCaseId", caseId);
        return "lawyers";
    }
}
