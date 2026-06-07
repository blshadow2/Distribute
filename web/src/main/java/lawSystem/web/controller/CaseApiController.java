package lawSystem.web.controller;

import java.util.List;

import jakarta.servlet.http.HttpSession;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lawSystem.web.auth.LoginMember;
import lawSystem.web.auth.SessionConst;
import lawSystem.web.dto.CaseDto;
import lawSystem.web.service.CaseService;

/**
 * AI 도구의 "사건 선택" 드롭다운용 REST.
 *  - GET /api/cases/mine   : 로그인 사용자가 선택할 수 있는 사건 목록
 *  - GET /api/cases/{id}   : 선택한 사건 상세(사실관계 채우기용)
 * 로그인만 되어 있으면 접근 가능(인터셉터가 보장).
 */
@RestController
@RequestMapping("/api/cases")
public class CaseApiController {

    private final CaseService caseService;

    public CaseApiController(CaseService caseService) {
        this.caseService = caseService;
    }

    @GetMapping("/mine")
    public List<CaseDto> mine(HttpSession session) {
        LoginMember m = (LoginMember) session.getAttribute(SessionConst.LOGIN_MEMBER);
        if (m == null) {
            return List.of();
        }
        return caseService.listForUser(m.getMemberId(), m.getViewRole());
    }

    @GetMapping("/{id}")
    public CaseDto one(@PathVariable("id") String id) {
        return caseService.get(id);
    }
}
