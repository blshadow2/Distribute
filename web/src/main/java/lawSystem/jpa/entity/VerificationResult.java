package lawSystem.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

@Entity
@Table(name = "verification_result")
public class VerificationResult {

    @Id
    @Column(name = "verification_id", length = 64, nullable = false, updatable = false)
    private String verificationId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "verification_method", length = 50)
    private String verificationMethod;

    @Column(name = "verified", nullable = false)
    private boolean verified = false;

    @Column(name = "verified_at")
    private LocalDateTime verifiedAt;

    @Column(name = "personal_identifier", length = 100)
    private String personalIdentifier;

    protected VerificationResult() {}

    public VerificationResult(String verificationId, Member member, String verificationMethod) {
        this.verificationId = verificationId;
        this.member = member;
        this.verificationMethod = verificationMethod;
    }

    public String getVerificationId() { return verificationId; }
    public Member getMember() { return member; }
    public String getVerificationMethod() { return verificationMethod; }
    public boolean isVerified() { return verified; }
    public LocalDateTime getVerifiedAt() { return verifiedAt; }
    public String getPersonalIdentifier() { return personalIdentifier; }

    public void markVerified(String personalIdentifier) {
        this.verified = true;
        this.verifiedAt = LocalDateTime.now();
        this.personalIdentifier = personalIdentifier;
    }
}
