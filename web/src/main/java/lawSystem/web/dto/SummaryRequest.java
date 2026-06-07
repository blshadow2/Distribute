package lawSystem.web.dto;

/** POST /api/ai/summary 요청 본문. */
public class SummaryRequest {

    private String text;      // 요약할 사건 내용 (필수)
    private String caseId;    // 연결할 사건 ID (선택, legal_case 에 존재할 때만)

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getCaseId() {
        return caseId;
    }

    public void setCaseId(String caseId) {
        this.caseId = caseId;
    }
}
