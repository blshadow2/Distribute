package lawSystem.web.dto;

/** AI 분석 공용 요청 본문 (키워드/유사판례/법리). */
public class AiRequest {

    private String text;          // 분석 대상 텍스트(사건 내용/쿼리)
    private String caseId;        // 선택
    private Integer topK;         // 유사판례/법리: 검색 개수
    private Integer maxKeywords;  // 키워드 개수

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public Integer getTopK() { return topK; }
    public void setTopK(Integer topK) { this.topK = topK; }

    public Integer getMaxKeywords() { return maxKeywords; }
    public void setMaxKeywords(Integer maxKeywords) { this.maxKeywords = maxKeywords; }
}
