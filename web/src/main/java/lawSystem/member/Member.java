package lawSystem.member;

import java.time.LocalDateTime;

public class Member {
    protected String memberId;
    protected String name;
    protected String email;
    protected String password;
    protected String phoneNumber;
    protected String role;
    protected LocalDateTime createdAt;
    protected boolean loggedIn;

    public Member(
            String memberId,
            String name,
            String email,
            String password,
            String phoneNumber,
            String role
    ) {
        this.memberId = memberId;
        this.name = name;
        this.email = email;
        this.password = password;
        this.phoneNumber = phoneNumber;
        this.role = role;
        this.createdAt = LocalDateTime.now();
        this.loggedIn = false;
    }

    public boolean login(String id, String pw) {
        if (id == null || pw == null) {
            return false;
        }

        boolean idMatched = id.equals(this.memberId) || id.equals(this.email);
        boolean passwordMatched = pw.equals(this.password);

        if (idMatched && passwordMatched) {
            this.loggedIn = true;
            return true;
        }

        return false;
    }

    public void logout() {
        this.loggedIn = false;
    }

    public boolean verifyIdentity(String authId) {
        return authId != null && !authId.trim().isEmpty();
    }

    public boolean isLoggedIn() {
        return loggedIn;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getRole() {
        return role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}