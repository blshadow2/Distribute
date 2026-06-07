package lawSystem.verification;

import java.time.LocalDateTime;

public class VerificationResult {
    private String verificationId;
    private String memberId;
    private String verificationMethod;
    private boolean verified;
    private LocalDateTime verifiedAt;
    private String personalIdentifier;

    public VerificationResult(
            String verificationId,
            String memberId,
            String verificationMethod
    ) {
        this.verificationId = verificationId;
        this.memberId = memberId;
        this.verificationMethod = verificationMethod;
        this.verified = false;
        this.verifiedAt = null;
        this.personalIdentifier = null;
    }

    public static VerificationResult requestVerification(
            String memberId,
            String method
    ) {
        if (memberId == null || memberId.trim().isEmpty()) {
            return null;
        }

        if (method == null || method.trim().isEmpty()) {
            return null;
        }

        return new VerificationResult(
                "lawSystem.verification-" + System.currentTimeMillis(),
                memberId,
                method
        );
    }

    public boolean checkVerificationCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        /*
         * 텍스트 기반 테스트용 인증 코드이다.
         * 실제 시스템에서는 민간 인증서 API 또는 휴대폰 인증 API의 결과를 확인해야 한다.
         */
        if (code.equals("123456")) {
            this.verified = true;
            this.verifiedAt = LocalDateTime.now();
            this.personalIdentifier = "PID-" + memberId;
            return true;
        }

        this.verified = false;
        return false;
    }

    public boolean isVerified() {
        return verified;
    }

    public void expireVerification() {
        this.verified = false;
    }

    public String getVerificationId() {
        return verificationId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getVerificationMethod() {
        return verificationMethod;
    }

    public LocalDateTime getVerifiedAt() {
        return verifiedAt;
    }

    public String getPersonalIdentifier() {
        return personalIdentifier;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public void setVerifiedAt(LocalDateTime verifiedAt) {
        this.verifiedAt = verifiedAt;
    }

    public void setPersonalIdentifier(String personalIdentifier) {
        this.personalIdentifier = personalIdentifier;
    }

    @Override
    public String toString() {
        return "본인 인증 ID: " + verificationId +
                "\n회원 ID: " + memberId +
                "\n인증 방식: " + verificationMethod +
                "\n인증 성공 여부: " + verified +
                "\n인증 시간: " + verifiedAt +
                "\n개인 식별 값: " + personalIdentifier;
    }
}