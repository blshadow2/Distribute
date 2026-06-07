package lawSystem.jpa.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "case_document")
public class CaseDocument {

    @Id
    @Column(name = "document_id", length = 64, nullable = false, updatable = false)
    private String documentId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "case_id", nullable = false)
    private LegalCase legalCase;

    @Column(name = "document_type", length = 50)
    private String documentType;

    @Column(name = "title", length = 255)
    private String title;

    @Column(name = "content", columnDefinition = "LONGTEXT")
    private String content;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "created_by", length = 64)
    private String createdBy;

    @Column(name = "version", nullable = false)
    private int version = 1;

    @Column(name = "signed", nullable = false)
    private boolean signed = false;

    protected CaseDocument() {}

    public CaseDocument(String documentId, String documentType, String title, String content, String createdBy) {
        this.documentId = documentId;
        this.documentType = documentType;
        this.title = title;
        this.content = content;
        this.createdBy = createdBy;
    }

    public String getDocumentId() { return documentId; }
    public LegalCase getLegalCase() { return legalCase; }
    public String getDocumentType() { return documentType; }
    public String getTitle() { return title; }
    public String getContent() { return content; }
    public String getFilePath() { return filePath; }
    public String getCreatedBy() { return createdBy; }
    public int getVersion() { return version; }
    public boolean isSigned() { return signed; }

    public void setLegalCase(LegalCase legalCase) { this.legalCase = legalCase; }
    public void setContent(String content) { this.content = content; this.version++; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public void setSigned(boolean signed) { this.signed = signed; }
}
