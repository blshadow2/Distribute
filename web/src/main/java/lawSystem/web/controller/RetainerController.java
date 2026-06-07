package lawSystem.web.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import lawSystem.web.auth.LoginMember;
import lawSystem.web.auth.RoleAllowed;
import lawSystem.web.auth.SessionConst;
import lawSystem.web.dto.LawyerDto;
import lawSystem.web.service.CaseService;
import lawSystem.web.service.ConsultationService;
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
    private final ConsultationService consultationService;

    public RetainerController(RetainerService retainerService,
                             CaseService caseService,
                             LawyerService lawyerService,
                             ConsultationService consultationService) {
        this.retainerService = retainerService;
        this.caseService = caseService;
        this.lawyerService = lawyerService;
        this.consultationService = consultationService;
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
        // 유스케이스: 상담이 완료된 변호사에게만 수임 요청 가능 → 드롭다운을 그들로 제한.
        java.util.Set<String> eligible = consultationService.completedConsultationLawyerIds(m.getMemberId());
        List<LawyerDto> lawyers = lawyerService.search(null, null, null).stream()
                .filter(l -> eligible.contains(l.getLawyerId()))
                .toList();
        model.addAttribute("lawyers", lawyers);
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
                          RedirectAttributes ra, HttpSession session) {
        LoginMember m = login(session);
        try {
            retainerService.requestRetainer(m.getMemberId(), caseId, lawyerId, scope, fee, result, content);
        } catch (RuntimeException e) {
            // 상담 미완료 등 전제조건 위반 → 화면에 안내
            ra.addFlashAttribute("error", e.getMessage());
        }
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
