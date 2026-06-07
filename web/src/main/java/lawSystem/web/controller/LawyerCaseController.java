package lawSystem.web.controller;

import jakarta.servlet.http.HttpSession;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lawSystem.web.auth.LoginMember;
import lawSystem.web.auth.RoleAllowed;
import lawSystem.web.auth.SessionConst;
import lawSystem.web.service.CaseService;

/**
 * 변호사 담당 사건 목록(/my-cases). 본인에게 배당된 사건을 보여준다.
 */
@Controller
public class LawyerCaseController {

    private final CaseService caseService;

    public LawyerCaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @RoleAllowed({"ASSOCIATE", "PARTNER"})
    @GetMapping("/my-cases")
    public String myCases(HttpSession session, Model model) {
        LoginMember m = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        model.addAttribute("cases", caseService.listForLawyer(m.getMemberId()));
        return "my-cases";
    }
}
