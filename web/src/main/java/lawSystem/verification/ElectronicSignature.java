package lawSystem.verification;

import java.time.LocalDateTime;

public class ElectronicSignature {
    private String signatureId;
    private String documentId;
    private String memberId;
    private String verificationId;
    private LocalDateTime signedAt;
    private String signatureHash;

    public ElectronicSignature(
            String signatureId,
            String documentId,
            String memberId,
            String verificationId,
            LocalDateTime signedAt,
            String signatureHash
    ) {
        this.signatureId = signatureId;
        this.documentId = documentId;
        this.memberId = memberId;
        this.verificationId = verificationId;
        this.signedAt = signedAt;
        this.signatureHash = signatureHash;
    }

    public static ElectronicSignature createSignature(
            String documentId,
            String memberId
    ) {
        if (documentId == null || documentId.trim().isEmpty()) {
            return null;
        }

        if (memberId == null || memberId.trim().isEmpty()) {
            return null;
        }

        LocalDateTime signedAt = LocalDateTime.now();

        String signatureHash = generateSignatureHash(
                documentId,
                memberId,
                null,
                signedAt
        );

        return new ElectronicSignature(
                "signature-" + System.currentTimeMillis(),
                documentId,
                memberId,
                null,
                signedAt,
                signatureHash
        );
    }

    public static ElectronicSignature createSignature(
            String documentId,
            String memberId,
            VerificationResult verificationResult
    ) {
        if (documentId == null || documentId.trim().isEmpty()) {
            return null;
        }

        if (memberId == null || memberId.trim().isEmpty()) {
            return null;
        }

        if (verificationResult == null || !verificationResult.isVerified()) {
            return null;
        }

        LocalDateTime signedAt = LocalDateTime.now();

        String signatureHash = generateSignatureHash(
                documentId,
                memberId,
                verificationResult.getVerificationId(),
                signedAt
        );

        return new ElectronicSignature(
                "signature-" + System.currentTimeMillis(),
                documentId,
                memberId,
                verificationResult.getVerificationId(),
                signedAt,
                signatureHash
        );
    }

    public boolean validateSignature() {
        return signatureId != null && !signatureId.trim().isEmpty()
                && documentId != null && !documentId.trim().isEmpty()
                && memberId != null && !memberId.trim().isEmpty()
                && signedAt != null
                && signatureHash != null && !signatureHash.trim().isEmpty();
    }

    public boolean attachToDocument(String documentId) {
        if (documentId == null || documentId.trim().isEmpty()) {
            return false;
        }

        if (!validateSignature()) {
            return false;
        }

        this.documentId = documentId;
        return true;
    }

    private static String generateSignatureHash(
            String documentId,
            String memberId,
            String verificationId,
            LocalDateTime signedAt
    ) {
        String baseText = documentId + "|"
                + memberId + "|"
                + verificationId + "|"
                + signedAt;

        return Integer.toHexString(baseText.hashCode());
    }

    public String getSignatureId() {
        return signatureId;
    }

    public String getDocumentId() {
        return documentId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getVerificationId() {
        return verificationId;
    }

    public LocalDateTime getSignedAt() {
        return signedAt;
    }

    public String getSignatureHash() {
        return signatureHash;
    }

    @Override
    public String toString() {
        return "전자 서명 ID: " + signatureId +
                "\n문서 ID: " + documentId +
                "\n회원 ID: " + memberId +
                "\n본인 인증 ID: " + verificationId +
                "\n서명 시간: " + signedAt +
                "\n서명 해시: " + signatureHash;
    }
}