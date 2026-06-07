package lawSystem.web.dto;

import java.util.List;

/** AI 분석 공용 요청 본문 (키워드/유사판례/법리). */
public class AiRequest {

    private String text;            // 분석 대상 텍스트(사건 내용/쿼리)
    private String caseId;          // 선택
    private Integer topK;           // 유사판례/법리: 검색 개수
    private Integer maxKeywords;    // 키워드 개수
    private List<String> keywords;  // 키워드 저장 시 클라이언트가 보낸 키워드 목록

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public List<String> getKeywords() { return keywords; }
    public void setKeywords(List<String> keywords) { this.keywords = keywords; }

    public String getCaseId() { return caseId; }
    public void setCaseId(String caseId) { this.caseId = caseId; }

    public Integer getTopK() { return topK; }
    public void setTopK(Integer topK) { this.topK = topK; }

    public Integer getMaxKeywords() { return maxKeywords; }
    public void setMaxKeywords(Integer maxKeywords) { this.maxKeywords = maxKeywords; }
}
