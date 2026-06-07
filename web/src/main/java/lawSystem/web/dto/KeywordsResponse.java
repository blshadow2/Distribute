package lawSystem.web.dto;

import java.util.List;

/** POST /api/ai/keywords 응답. */
public class KeywordsResponse {

    private List<String> keywords;

    public KeywordsResponse(List<String> keywords) {
        this.keywords = keywords;
    }

    public List<String> getKeywords() {
        return keywords;
    }
}
