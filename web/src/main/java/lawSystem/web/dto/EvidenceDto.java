package lawSystem.web.dto;

/** 증거자료 표시용. */
public class EvidenceDto {

    private final String evidenceId;
    private final String fileName;
    private final String fileType;
    private final String description;

    public EvidenceDto(String evidenceId, String fileName, String fileType, String description) {
        this.evidenceId = evidenceId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.description = description;
    }

    public String getEvidenceId() { return evidenceId; }
    public String getFileName() { return fileName; }
    public String getFileType() { return fileType; }
    public String getDescription() { return description; }
}
