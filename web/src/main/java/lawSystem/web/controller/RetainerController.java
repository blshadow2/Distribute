package lawSystem.web.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lawSystem.web.auth.LoginMember;
import lawSystem.web.auth.RoleAllowed;
import lawSystem.web.auth.SessionConst;
import lawSystem.web.service.CaseService;
import lawSystem.web.service.LawyerService;
import lawSystem.web.service.RetainerService;

/**
 * 수임: 의뢰인 요청/응답(/retainers) + 대표변호사 관리(/retainer-mgmt).
 */
@Controller
public class RetainerController {

    private final RetainerService retainerService;
    private final CaseService caseService;
    private final LawyerService lawyerService;

    public RetainerController(RetainerService retainerService,
                             CaseService caseService,
                             LawyerService lawyerService) {
        this.retainerService = retainerService;
        this.caseService = caseService;
        this.lawyerService = lawyerService;
    }

    private LoginMember login(HttpSession session) {
        return (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    }

    // ---------- 의뢰인 ----------
    @RoleAllowed("CLIENT")
    @GetMapping("/retainers")
    public String myRetainers(HttpSession session, Model model) {
        LoginMember m = login(session);
        model.addAttribute("requests", retainerService.listForClient(m.getMemberId()));
        model.addAttribute("cases", caseService.listForUser(m.getMemberId(), "CLIENT"));
        model.addAttribute("lawyers", lawyerService.search(null, null, null));
        return "retainers";
    }

    @RoleAllowed("CLIENT")
    @PostMapping("/retainers")
    public String request(@RequestParam("caseId") String caseId,
                          @RequestParam("lawyerId") String lawyerId,
                          @RequestParam(value = "desiredScope", required = false) String scope,
                          @RequestParam(value = "desiredFee", required = false, defaultValue = "0") int fee,
                          @RequestParam(value = "desiredResult", required = false) String result,
                          @RequestParam(value = "requestContent", required = false) String content,
                          HttpSession session) {
        LoginMember m = login(session);
        retainerService.requestRetainer(m.getMemberId(), caseId, lawyerId, scope, fee, result, content);
        return "redirect:/retainers";
    }

    @RoleAllowed("CLIENT")
    @PostMapping("/retainers/{id}/accept")
    public String accept(@PathVariable("id") String id) {
        retainerService.acceptCondition(id);
        return "redirect:/retainers";
    }

    @RoleAllowed("CLIENT")
    @PostMapping("/retainers/{id}/reject")
    public String reject(@PathVariable("id") String id) {
        retainerService.rejectCondition(id);
        return "redirect:/retainers";
    }

    @RoleAllowed("CLIENT")
    @PostMapping("/retainers/{id}/adjust")
    public String adjust(@PathVariable("id") String id,
                         @RequestParam(value = "message", required = false) String message) {
        retainerService.requestAdjustment(id, message);
        return "redirect:/retainers";
    }

    @RoleAllowed("CLIENT")
    @PostMapping("/retainers/{id}/delete")
    public String delete(@PathVariable("id") String id, HttpSession session) {
        retainerService.deleteByClient(id, login(session).getMemberId());
        return "redirect:/retainers";
    }

    // ---------- 대표 변호사 ----------
    @RoleAllowed("PARTNER")
    @GetMapping("/retainer-mgmt")
    public String manage(Model model) {
        model.addAttribute("requests", retainerService.listAll());
        return "retainer-mgmt";
    }

    @RoleAllowed("PARTNER")
    @PostMapping("/retainers/{id}/condition")
    public String sendCondition(@PathVariable("id") String id,
                                @RequestParam("fee") int fee,
                                @RequestParam(value = "scope", required = false) String scope,
                                @RequestParam(value = "terms", required = false) String terms) {
        retainerService.sendCondition(id, fee, scope, terms);
        return "redirect:/retainer-mgmt";
    }

    @RoleAllowed("PARTNER")
    @PostMapping("/retainers/{id}/retain")
    public String retain(@PathVariable("id") String id) {
        retainerService.retain(id);
        return "redirect:/retainer-mgmt";
    }

    @RoleAllowed("PARTNER")
    @PostMapping("/retainers/{id}/decline")
    public String decline(@PathVariable("id") String id) {
        retainerService.rejectByPartner(id);
        return "redirect:/retainer-mgmt";
    }
}
