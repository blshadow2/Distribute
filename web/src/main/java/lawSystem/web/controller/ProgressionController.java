package lawSystem.web.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lawSystem.web.auth.LoginMember;
import lawSystem.web.auth.RoleAllowed;
import lawSystem.web.auth.SessionConst;
import lawSystem.web.service.CaseService;
import lawSystem.web.service.ProgressionService;

/**
 * 진행상황: 소속변호사 공유(/progress-share) + 의뢰인 열람(/progress).
 */
@Controller
public class ProgressionController {

    private final ProgressionService progressionService;
    private final CaseService caseService;

    public ProgressionController(ProgressionService progressionService, CaseService caseService) {
        this.progressionService = progressionService;
        this.caseService = caseService;
    }

    private LoginMember login(HttpSession session) {
        return (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    }

    // ---------- 소속 변호사: 진행상황 공유 ----------
    @RoleAllowed("ASSOCIATE")
    @GetMapping("/progress-share")
    public String shareForm(HttpSession session, Model model) {
        LoginMember m = login(session);
        model.addAttribute("cases", caseService.listForLawyer(m.getMemberId()));
        model.addAttribute("records", progressionService.listForLawyer(m.getMemberId()));
        return "progress-share";
    }

    @RoleAllowed("ASSOCIATE")
    @PostMapping("/progress-share")
    public String share(@RequestParam("caseId") String caseId,
                        @RequestParam(value = "progressStatus", required = false) String status,
                        @RequestParam(value = "description", required = false) String description,
                        @RequestParam(value = "recentAction", required = false) String recentAction,
                        @RequestParam(value = "requestedMaterial", required = false) String requestedMaterial,
                        HttpSession session) {
        progressionService.addProgress(caseId, login(session).getMemberId(),
                status, description, recentAction, requestedMaterial);
        return "redirect:/progress-share";
    }

    // ---------- 의뢰인: 진행상황 열람 ----------
    @RoleAllowed("CLIENT")
    @GetMapping("/progress")
    public String view(HttpSession session, Model model) {
        LoginMember m = login(session);
        model.addAttribute("records", progressionService.listForClient(m.getMemberId()));
        return "progress";
    }
}
