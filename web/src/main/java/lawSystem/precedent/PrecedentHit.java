package lawSystem.precedent;

import java.util.ArrayList;
import java.util.List;

/**
 * RAG 유사 판례 검색 결과 한 건이다.
 *
 * 본문은 담지 않는다. {@code caseId}(= precedent.external_case_id) 로
 * {@link lawSystem.db.dao.PrecedentDAO#findByExternalId(String)} 를 호출해 DB 에서 본문을 가져온다.
 */
public class PrecedentHit {

    private final String caseId;
    private final double similarityScore;
    private final List<String> matchedChunkTypes;
    private final String matchedExcerpt;

    public PrecedentHit(String caseId,
                        double similarityScore,
                        List<String> matchedChunkTypes,
                        String matchedExcerpt) {
        this.caseId = caseId;
        this.similarityScore = similarityScore;
        this.matchedChunkTypes = matchedChunkTypes != null ? matchedChunkTypes : new ArrayList<>();
        this.matchedExcerpt = matchedExcerpt;
    }

    public String getCaseId() {
        return caseId;
    }

    public double getSimilarityScore() {
        return similarityScore;
    }

    public List<String> getMatchedChunkTypes() {
        return matchedChunkTypes;
    }

    public String getMatchedExcerpt() {
        return matchedExcerpt;
    }
}
