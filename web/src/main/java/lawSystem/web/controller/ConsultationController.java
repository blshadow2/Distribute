package lawSystem.web.controller;

import java.time.LocalDateTime;

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
import lawSystem.web.service.ConsultationService;
import lawSystem.web.service.LawyerService;

/**
 * 상담: 의뢰인 신청(/consultations) + 사무직원 일정/응답(/schedules).
 */
@Controller
public class ConsultationController {

    private final ConsultationService consultationService;
    private final CaseService caseService;
    private final LawyerService lawyerService;

    public ConsultationController(ConsultationService consultationService,
                                  CaseService caseService,
                                  LawyerService lawyerService) {
        this.consultationService = consultationService;
        this.caseService = caseService;
        this.lawyerService = lawyerService;
    }

    private LoginMember login(HttpSession session) {
        return (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
    }

    // ---------- 의뢰인 ----------
    @RoleAllowed("CLIENT")
    @GetMapping("/consultations")
    public String myConsultations(@RequestParam(value = "lawyer", required = false) String lawyerId,
                                  HttpSession session, Model model) {
        LoginMember m = login(session);
        model.addAttribute("requests", consultationService.listForClient(m.getMemberId()));
        model.addAttribute("cases", caseService.listForUser(m.getMemberId(), "CLIENT"));
        model.addAttribute("lawyers", lawyerService.search(null, null, null));
        model.addAttribute("schedules", consultationService.availableSchedules());
        model.addAttribute("selectedLawyer", lawyerId);
        return "consultations";
    }

    @RoleAllowed("CLIENT")
    @PostMapping("/consultations")
    public String request(@RequestParam(value = "caseId", required = false) String caseId,
                          @RequestParam("lawyerId") String lawyerId,
                          @RequestParam(value = "scheduleId", required = false) String scheduleId,
                          @RequestParam(value = "memo", required = false) String memo,
                          HttpSession session) {
        LoginMember m = login(session);
        consultationService.requestConsultation(m.getMemberId(), caseId, lawyerId, scheduleId, memo);
        return "redirect:/consultations";
    }

    @RoleAllowed("CLIENT")
    @PostMapping("/consultations/{id}/delete")
    public String delete(@PathVariable("id") String id, HttpSession session) {
        consultationService.deleteByClient(id, login(session).getMemberId());
        return "redirect:/consultations";
    }

    // ---------- 사무직원 ----------
    @RoleAllowed("STAFF")
    @GetMapping("/schedules")
    public String schedules(Model model) {
        model.addAttribute("schedules", consultationService.listSchedules());
        model.addAttribute("lawyers", lawyerService.search(null, null, null));
        model.addAttribute("requests", consultationService.listAll());
        return "schedules";
    }

    @RoleAllowed("STAFF")
    @PostMapping("/schedules")
    public String addSchedule(@RequestParam("lawyerId") String lawyerId,
                              @RequestParam("dateTime") String dateTime,
                              @RequestParam("duration") int duration) {
        consultationService.registerSchedule(lawyerId, LocalDateTime.parse(dateTime), duration);
        return "redirect:/schedules";
    }

    @RoleAllowed("STAFF")
    @PostMapping("/consultations/{id}/approve")
    public String approve(@PathVariable("id") String id) {
        consultationService.approve(id);
        return "redirect:/schedules";
    }

    @RoleAllowed("STAFF")
    @PostMapping("/consultations/{id}/reject")
    public String reject(@PathVariable("id") String id) {
        consultationService.reject(id);
        return "redirect:/schedules";
    }
}
