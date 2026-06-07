package lawSystem.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "evidence")
public class Evidence {

    @Id
    @Column(name = "evidence_id", length = 64, nullable = false, updatable = false)
    private String evidenceId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private LegalCase legalCase;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_type", length = 50)
    private String fileType;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    protected Evidence() {}

    public Evidence(String evidenceId, String fileName, String fileType, String filePath, String description) {
        this.evidenceId = evidenceId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.filePath = filePath;
        this.description = description;
    }

    public String getEvidenceId() { return evidenceId; }
    public LegalCase getLegalCase() { return legalCase; }
    public String getFileName() { return fileName; }
    public String getFileType() { return fileType; }
    public String getFilePath() { return filePath; }
    public String getDescription() { return description; }

    public void setLegalCase(LegalCase legalCase) { this.legalCase = legalCase; }
    public void setDescription(String description) { this.description = description; }
}
