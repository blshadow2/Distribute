package lawSystem.web.auth;

import java.io.Serializable;

/**
 * 세션에 보관하는 로그인 사용자 정보(최소한).
 * viewRole: CLIENT / PARTNER / ASSOCIATE / STAFF / LAWYER — 화면 분기에 사용.
 */
public class LoginMember implements Serializable {

    private final String memberId;
    private final String name;
    private final String viewRole;

    public LoginMember(String memberId, String name, String viewRole) {
        this.memberId = memberId;
        this.name = name;
        this.viewRole = viewRole;
    }

    public String getMemberId() { return memberId; }
    public String getName() { return name; }
    public String getViewRole() { return viewRole; }
}
