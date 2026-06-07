package lawSystem.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "progression_record")
public class ProgressionRecord {

    @Id
    @Column(name = "progress_id", length = 64, nullable = false, updatable = false)
    private String progressId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private LegalCase legalCase;

    @Column(name = "writer_id", length = 64)
    private String writerId;

    @Column(name = "progress_status", length = 100)
    private String progressStatus;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "recent_action", columnDefinition = "TEXT")
    private String recentAction;

    @Column(name = "requested_material", length = 255)
    private String requestedMaterial;

    protected ProgressionRecord() {}

    public ProgressionRecord(String progressId, String writerId, String progressStatus,
                             String description, String recentAction) {
        this.progressId = progressId;
        this.writerId = writerId;
        this.progressStatus = progressStatus;
        this.description = description;
        this.recentAction = recentAction;
    }

    public String getProgressId() { return progressId; }
    public LegalCase getLegalCase() { return legalCase; }
    public String getWriterId() { return writerId; }
    public String getProgressStatus() { return progressStatus; }
    public String getDescription() { return description; }
    public String getRecentAction() { return recentAction; }
    public String getRequestedMaterial() { return requestedMaterial; }

    public void setLegalCase(LegalCase legalCase) { this.legalCase = legalCase; }
    public void setRequestedMaterial(String requestedMaterial) { this.requestedMaterial = requestedMaterial; }
}
