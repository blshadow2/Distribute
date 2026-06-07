package lawSystem.web.controller;

import org.springframework.stereotype.Controller;

/**
 * (구) 역할별 placeholder 컨트롤러.
 *
 * 모든 메뉴가 실제 기능 컨트롤러로 대체되어, 현재 이 클래스가 직접 처리하는 라우트는 없다.
 *  - /cases·/lawyers·/consultations·/progress      → 의뢰인 기능 컨트롤러
 *  - /retainer-mgmt                                 → RetainerController
 *  - /assign (사건 배당)                            → AssignmentController
 *  - /my-cases·/progress-share                      → 변호사 기능 컨트롤러
 *  - /schedules                                     → ConsultationController
 */
@Controller
public class PlaceholderPageController {
}
