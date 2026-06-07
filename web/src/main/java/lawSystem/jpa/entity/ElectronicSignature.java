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
@Table(name = "electronic_signature")
public class ElectronicSignature {

    @Id
    @Column(name = "signature_id", length = 64, nullable = false, updatable = false)
    private String signatureId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "document_id", nullable = false)
    private CaseDocument document;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verification_id")
    private VerificationResult verification;

    @Column(name = "signed_at", nullable = false)
    private LocalDateTime signedAt;

    @Column(name = "signature_hash", length = 255, nullable = false)
    private String signatureHash;

    protected ElectronicSignature() {}

    public ElectronicSignature(String signatureId, CaseDocument document, Member member,
                               VerificationResult verification, String signatureHash) {
        this.signatureId = signatureId;
        this.document = document;
        this.member = member;
        this.verification = verification;
        this.signedAt = LocalDateTime.now();
        this.signatureHash = signatureHash;
    }

    public String getSignatureId() { return signatureId; }
    public CaseDocument getDocument() { return document; }
    public Member getMember() { return member; }
    public VerificationResult getVerification() { return verification; }
    public LocalDateTime getSignedAt() { return signedAt; }
    public String getSignatureHash() { return signatureHash; }
}
