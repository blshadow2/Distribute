package lawSystem.web.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import jakarta.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import lawSystem.legalCase.CaseCategory;
import lawSystem.web.auth.LoginMember;
import lawSystem.web.auth.RoleAllowed;
import lawSystem.web.auth.SessionConst;
import lawSystem.web.dto.CaseForm;
import lawSystem.web.service.CaseService;

/**
 * 사건 등록/목록/상세 + 증거 업로드 (의뢰인 전용).
 */
@Controller
@RoleAllowed("CLIENT")
public class CaseController {

    private final CaseService caseService;

    @Value("${app.upload-dir:./uploads}")
    private String uploadDir;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping("/cases")
    public String list(HttpSession session, Model model) {
        LoginMember m = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        model.addAttribute("cases", caseService.listForUser(m.getMemberId(), m.getViewRole()));
        return "cases";
    }

    @GetMapping("/cases/new")
    public String form(Model model) {
        model.addAttribute("categories", CaseCategory.values());
        return "case-form";
    }

    @PostMapping("/cases")
    public String create(@ModelAttribute CaseForm form, HttpSession session) {
        LoginMember m = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        caseService.create(m.getMemberId(), form);
        return "redirect:/cases";
    }

    /** 사건 상세: 사건 정보 + 증거 목록 + AI 분석 결과 이력. */
    @GetMapping("/cases/{id}")
    public String detail(@PathVariable("id") String id, Model model) {
        model.addAttribute("aCase", caseService.get(id));
        model.addAttribute("evidences", caseService.listEvidence(id));
        model.addAttribute("aiResults", caseService.listAiResults(id));
        return "case-detail";
    }

    /** 증거자료 업로드. */
    @PostMapping("/cases/{id}/evidence")
    public String uploadEvidence(@PathVariable("id") String id,
                                 @RequestParam("file") MultipartFile file,
                                 @RequestParam(value = "description", required = false) String description)
            throws IOException {
        if (file != null && !file.isEmpty()) {
            String original = StringUtils.cleanPath(
                    file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload");
            Path dir = Paths.get(uploadDir, id);
            Files.createDirectories(dir);
            String stored = System.currentTimeMillis() + "_" + original;
            Path target = dir.resolve(stored);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            caseService.addEvidence(id, original, file.getContentType(),
                    target.toAbsolutePath().toString(), description);
        }
        return "redirect:/cases/" + id;
    }
}
