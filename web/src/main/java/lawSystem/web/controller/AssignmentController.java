package lawSystem.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import lawSystem.web.auth.RoleAllowed;
import lawSystem.web.service.AssignmentService;

/**
 * 사건 배당 (대표변호사 전용).
 *  GET  /assign            — 수임 사건 목록 + 변호사 선택
 *  POST /assign/{caseId}   — 사건을 변호사에게 배당
 */
@Controller
@RoleAllowed("PARTNER")
public class AssignmentController {

    private final AssignmentService assignmentService;

    public AssignmentController(AssignmentService assignmentService) {
        this.assignmentService = assignmentService;
    }

    @GetMapping("/assign")
    public String list(Model model) {
        model.addAttribute("cases", assignmentService.listAssignableCases());
        model.addAttribute("lawyers", assignmentService.listLawyers());
        return "assignments";
    }

    @PostMapping("/assign/{caseId}")
    public String assign(@PathVariable("caseId") String caseId,
                         @RequestParam("lawyerId") String lawyerId) {
        assignmentService.assign(caseId, lawyerId);
        return "redirect:/assign";
    }
}
