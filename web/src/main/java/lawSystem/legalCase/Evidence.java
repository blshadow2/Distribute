package lawSystem.legalCase;

import java.io.File;

public class Evidence {
    private String evidenceId;
    private String caseId;
    private String fileName;
    private String fileType;
    private String filePath;
    private String description;

    public Evidence(
            String evidenceId,
            String caseId,
            String fileName,
            String fileType,
            String filePath,
            String description
    ) {
        this.evidenceId = evidenceId;
        this.caseId = caseId;
        this.fileName = fileName;
        this.fileType = fileType;
        this.filePath = filePath;
        this.description = description;
    }

    public boolean uploadEvidence(File file) {
        if (file == null) {
            return false;
        }

        this.fileName = file.getName();
        this.filePath = file.getPath();

        int dotIndex = fileName.lastIndexOf(".");
        if (dotIndex != -1) {
            this.fileType = fileName.substring(dotIndex + 1);
        }

        return true;
    }

    public void updateDescription(String description) {
        this.description = description;
    }

    public boolean checkDuplicateFileName(String fileName) {
        if (fileName == null) {
            return false;
        }

        return fileName.equals(this.fileName);
    }

    public boolean deleteEvidence() {
        this.fileName = null;
        this.fileType = null;
        this.filePath = null;
        this.description = null;
        return true;
    }

    public File downloadEvidence() {
        if (filePath == null) {
            return null;
        }

        return new File(filePath);
    }

    public String getEvidenceId() {
        return evidenceId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getFileName() {
        return fileName;
    }

    public String getDescription() {
        return description;
    }

    public String getFileType() {
        return fileType;
    }

    public String getFilePath() {
        return filePath;
    }
}