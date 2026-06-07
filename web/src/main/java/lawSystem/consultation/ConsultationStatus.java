package lawSystem.consultation;

public enum ConsultationStatus {
    REQUESTED,
    APPROVED,
    COMPLETED,        // 상담 완료 — 수임(RetainerRequest)의 전제조건
    REJECTED,
    SCHEDULE_CHANGED,
    CANCELED
}
