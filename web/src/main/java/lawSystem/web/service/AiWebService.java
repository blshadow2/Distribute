package lawSystem.web.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import lawSystem.ai.AnalysisType;
import lawSystem.ai.PrecedentAnalysisResult;
import lawSystem.ai.SimilarPrecedentsAnalysis;
import lawSystem.precedent.LegalRulesDto;
import lawSystem.precedent.PrecedentRagClient;
import lawSystem.precedent.SummaryDto;
import lawSystem.web.dto.AiResultResponse;
import lawSystem.web.dto.KeywordsResponse;
import lawSystem.web.dto.LegalRulesResponse;
import lawSystem.web.dto.PrecedentDto;
import lawSystem.web.dto.SummaryResponse;
import lawSystem.web.repository.AiResultRepository;

/**
 * AI 기능 웹 서비스.
 *
 * 모든 AI 기능(요약/키워드/법리/유사판례)의 "요청 → 결과"를 단일 JPA 경로
 * ({@link AiPersistenceService})로 영속화한다. (기존 JDBC DAO 경로는 웹에서 사용하지 않는다.)
 * 저장 실패는 사용자 응답을 막지 않되 로그로 남겨 원인을 드러낸다.
 */
@Service
public class AiWebService {

    private static final Logger log = LoggerFactory.getLogger(AiWebService.class);

    private final AiResultRepository aiResultRepository;
    private final AiPersistenceService aiPersistenceService;
    private final PrecedentRagClient ragClient = new PrecedentRagClient();

    public AiWebService(AiResultRepository aiResultRepository,
                        AiPersistenceService aiPersistenceService) {
        this.aiResultRepository = aiResultRepository;
        this.aiPersistenceService = aiPersistenceService;
    }

    /** 사건 내용 요약 → RAG/LLM 호출 후 JPA 로 저장(항상). */
    public SummaryResponse summarize(String text, String caseId) {
        SummaryDto dto;
        try {
            dto = ragClient.summarize(text, caseId);
        } catch (Exception e) {
            log.warn("[AI] 요약 호출 실패: {}", e.getMessage());
            dto = new SummaryDto("요약 생성 실패(AI 서비스 오류)", List.of(), "");
        }
        String resultId = persist(caseId, AnalysisType.CASE_SUMMARY, text, buildSummaryText(dto), 0.9);
        return new SummaryResponse(resultId, dto.getSummary(), dto.getMainIssues(), dto.getTimeline(), 0.9);
    }

    /** 키워드 추출 (미리보기 전용 — 저장하지 않는다. 저장은 recordKeywords). */
    public KeywordsResponse extractKeywords(String text, Integer maxKeywords) {
        int max = (maxKeywords == null || maxKeywords < 1) ? 5 : maxKeywords;
        try {
            return new KeywordsResponse(ragClient.extractKeywords(text, max));
        } catch (Exception e) {
            log.warn("[AI] 키워드 추출 실패: {}", e.getMessage());
            return new KeywordsResponse(List.of());
        }
    }

    /** 추출 키워드를 AI 분석 이력(ai_analysis_request/result)에 저장(항상, caseId 선택). */
    public void recordKeywords(String caseId, String text, List<String> keywords) {
        if (keywords == null || keywords.isEmpty()) {
            return;
        }
        persist(caseId, AnalysisType.KEYWORD_EXTRACTION, text,
                "키워드: " + String.join(", ", keywords), 0.9);
    }

    /** 유사 판례 목록 (검색 → DB 본문 조립) + 결과 저장. */
    public List<PrecedentDto> similarPrecedents(String query, String caseId) {
        List<PrecedentDto> out = new ArrayList<>();
        List<PrecedentAnalysisResult> results =
                new SimilarPrecedentsAnalysis().searchSimilarPrecedents(caseId, query);
        for (PrecedentAnalysisResult r : results) {
            out.add(new PrecedentDto(
                    r.getPrecedentId(),
                    r.getPrecedentTitle(),
                    r.getSimilarityScore(),
                    r.getLegalIssue(),
                    r.getPrecedentSummary()));
        }
        persist(caseId, AnalysisType.SIMILAR_PRECEDENTS, query, buildPrecedentText(out), 0.8);
        return out;
    }

    /** 법리 분석 (RAG 결합) + 결과 저장(항상). */
    public LegalRulesResponse analyzeLegalRules(String query, String caseId, Integer topK) {
        int k = (topK == null || topK < 1) ? 5 : topK;
        LegalRulesResponse resp;
        try {
            LegalRulesDto d = ragClient.analyzeLegalRules(query, caseId, k);
            resp = new LegalRulesResponse(
                    d.getIssueSummary(), d.getApplicableLaw(), d.getLegalExplanation(),
                    d.getRelatedStatutes(), d.getCitedCases());
        } catch (Exception e) {
            log.warn("[AI] 법리 분석 실패: {}", e.getMessage());
            resp = new LegalRulesResponse("", "", "법리 분석 실패(AI 서비스 오류)", List.of(), List.of());
        }
        String stored = "[쟁점] " + nz(resp.getIssueSummary())
                + "\n[적용 법리] " + nz(resp.getApplicableLaw())
                + "\n[설명] " + nz(resp.getLegalExplanation());
        persist(caseId, AnalysisType.LEGAL_RULE_ANALYSIS, query, stored, 0.85);
        return resp;
    }

    /** 저장된 AI 분석 결과 단건 조회 (Spring Data JPA). */
    @Transactional(readOnly = true)
    public AiResultResponse getResult(String aiResultId) {
        return aiResultRepository.findById(aiResultId)
                .map(e -> new AiResultResponse(
                        e.getAiResultId(),
                        e.getResultType() != null ? e.getResultType().name() : null,
                        e.getSummaryText(),
                        e.getConfidenceScore(),
                        e.getGeneratedAt(),
                        e.isReviewed()))
                .orElse(null);
    }

    // ── 내부 헬퍼 ────────────────────────────────────────────────
    /** 요청+결과를 JPA 로 저장. 실패해도 응답은 유지하되 로그로 남긴다. */
    private String persist(String caseId, AnalysisType type, String prompt,
                           String resultText, double confidence) {
        try {
            return aiPersistenceService.save(caseId, type, prompt, resultText, confidence);
        } catch (Exception e) {
            log.error("[AI] 분석 결과 저장 실패 (type={}, caseId={}): {}", type, caseId, e.getMessage(), e);
            return null;
        }
    }

    private String buildSummaryText(SummaryDto d) {
        StringBuilder sb = new StringBuilder(nz(d.getSummary()));
        if (d.getMainIssues() != null && !d.getMainIssues().isEmpty()) {
            sb.append("\n[주요 쟁점] ").append(String.join("; ", d.getMainIssues()));
        }
        if (d.getTimeline() != null && !d.getTimeline().isBlank()) {
            sb.append("\n[타임라인] ").append(d.getTimeline());
        }
        return sb.toString();
    }

    private String buildPrecedentText(List<PrecedentDto> list) {
        if (list.isEmpty()) {
            return "유사 판례 없음";
        }
        StringBuilder sb = new StringBuilder("유사 판례 " + list.size() + "건: ");
        for (PrecedentDto p : list) {
            sb.append(p.getTitle()).append("(")
              .append(String.format("%.3f", p.getSimilarityScore())).append("), ");
        }
        return sb.substring(0, sb.length() - 2);
    }

    private String nz(String s) {
        return s == null ? "" : s;
    }
}
