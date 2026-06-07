package lawSystem.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import lawSystem.web.auth.RoleAllowed;

/**
 * 대시보드 메뉴의 역할별 페이지(현재는 placeholder).
 * {@link RoleAllowed} 로 역할 접근을 제한한다 — 다른 역할이 접근하면 /access-denied.
 *
 * Phase 2 에서 각 메서드를 실제 기능(목록/폼)으로 대체한다.
 */
@Controller
public class PlaceholderPageController {

    // ---------- 의뢰인 ----------
    // /cases 는 CaseController(실제 등록/목록)가 처리한다.

    // /lawyers 는 LawyerController(실제 검색)가 처리한다.

    // /consultations 는 ConsultationController 가 처리한다.
    // /progress 는 ProgressionController(진행상황 열람)가 처리한다.

    // ---------- 대표 변호사 ----------
    // /retainer-mgmt 는 RetainerController 가 처리한다.

    @RoleAllowed("PARTNER")
    @GetMapping("/assign")
    public String assign(Model model) { return page(model, "사건 배당", "소속 변호사 배당 + AI 추천"); }

    // ---------- 소속 변호사 ----------
    // /my-cases 는 LawyerCaseController(담당 사건 목록)가 처리한다.
    // /progress-share 는 ProgressionController(진행상황 공유)가 처리한다.

    // ---------- 사무 직원 ----------
    // /schedules 는 ConsultationController 가 처리한다.

    private String page(Model model, String title, String desc) {
        model.addAttribute("title", title);
        model.addAttribute("desc", desc);
        return "placeholder";
    }
}
