package lawSystem.precedent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * Python RAG 서비스(FastAPI, 기본 http://localhost:8000)와 통신하는 HTTP 클라이언트이다.
 *
 * JDK17 내장 java.net.http.HttpClient 를 사용하므로 별도 HTTP 라이브러리가 필요 없다.
 * RAG 서비스 주소는 환경 변수 PRECEDENT_RAG_URL 로 바꿀 수 있다.
 */
public class PrecedentRagClient {

    private final String baseUrl;
    private final HttpClient httpClient;
    private final ObjectMapper mapper = new ObjectMapper();

    public PrecedentRagClient() {
        this(resolveBaseUrl());
    }

    public PrecedentRagClient(String baseUrl) {
        this.baseUrl = stripTrailingSlash(baseUrl);
        this.httpClient = HttpClient.newBuilder()
                // uvicorn 은 HTTP/1.1 전용이다. 기본값(HTTP/2)으로 두면 평문 h2c
                // 업그레이드 과정에서 POST 본문이 유실되어 서버가 422(body 누락)를 낸다.
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }

    private static String resolveBaseUrl() {
        String fromEnv = System.getenv("PRECEDENT_RAG_URL");
        if (fromEnv != null && !fromEnv.trim().isEmpty()) {
            return fromEnv.trim();
        }
        return "http://localhost:8000";
    }

    private static String stripTrailingSlash(String url) {
        if (url != null && url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    /**
     * 유사 판례를 검색한다.
     *
     * @param query               검색 쿼리(사건 요약 등)
     * @param caseId              요청 사건 ID(참고용, null 가능)
     * @param topK                최대 결과 수
     * @param similarityThreshold 정규화 점수(0~1) 임계값
     * @return external_case_id + 유사도 점수 목록 (본문 없음)
     * @throws RuntimeException RAG 서비스 호출 실패 시 (호출 측에서 graceful degradation 처리)
     */
    public List<PrecedentHit> searchSimilarPrecedents(String query,
                                                      String caseId,
                                                      int topK,
                                                      double similarityThreshold) {
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("query", query == null ? "" : query);
            if (caseId != null) {
                body.put("case_id", caseId);
            }
            body.put("top_k", topK);
            body.put("similarity_threshold", similarityThreshold);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/search/similar-precedents"))
                    .timeout(Duration.ofSeconds(30))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            mapper.writeValueAsString(body), java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                throw new RuntimeException("RAG 서비스 오류 응답: HTTP " + response.statusCode()
                        + " / " + response.body());
            }

            return parseHits(response.body());

        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("RAG 서비스 호출 실패: " + e.getMessage(), e);
        }
    }

    /**
     * 사건 내용 요약. POST /llm/summarize
     */
    public SummaryDto summarize(String text, String caseId) {
        ObjectNode body = mapper.createObjectNode();
        body.put("text", text == null ? "" : text);
        if (caseId != null) {
            body.put("case_id", caseId);
        }
        JsonNode root = post("/llm/summarize", body, 120);
        return new SummaryDto(
                textOrNull(root, "summary"),
                toStringList(root.get("main_issues")),
                textOrNull(root, "timeline")
        );
    }

    /**
     * 키워드 추출. POST /llm/extract-keywords
     */
    public List<String> extractKeywords(String text, int maxKeywords) {
        ObjectNode body = mapper.createObjectNode();
        body.put("text", text == null ? "" : text);
        body.put("max_keywords", maxKeywords);
        JsonNode root = post("/llm/extract-keywords", body, 120);
        return toStringList(root.get("keywords"));
    }

    /**
     * 법리 분석(RAG 결합). POST /rag/analyze-rules
     */
    public LegalRulesDto analyzeLegalRules(String query, String caseId, int topK) {
        ObjectNode body = mapper.createObjectNode();
        body.put("query", query == null ? "" : query);
        if (caseId != null) {
            body.put("case_id", caseId);
        }
        body.put("top_k", topK);
        JsonNode root = post("/rag/analyze-rules", body, 120);
        return new LegalRulesDto(
                textOrNull(root, "issue_summary"),
                textOrNull(root, "applicable_law"),
                textOrNull(root, "legal_explanation"),
                toStringList(root.get("related_statutes")),
                toStringList(root.get("cited_cases"))
        );
    }

    /**
     * 의미 기반 재정렬. POST /embed/rerank
     *
     * query 와 각 문서(text)의 코사인 유사도를 계산해 식별자→점수 맵으로 반환한다.
     * (변호사 검색에서 키워드 ↔ 변호사 프로필 유사도 정렬에 사용)
     *
     * @param query    기준 쿼리(사건 키워드 등)
     * @param idToText 후보 식별자 → 프로필/본문 텍스트
     * @return 식별자 → 유사도 점수 (점수 내림차순). 호출 실패는 RuntimeException.
     */
    public java.util.LinkedHashMap<String, Double> rerank(String query, java.util.Map<String, String> idToText) {
        java.util.LinkedHashMap<String, Double> scores = new java.util.LinkedHashMap<>();
        if (idToText == null || idToText.isEmpty()) {
            return scores;
        }
        ObjectNode body = mapper.createObjectNode();
        body.put("query", query == null ? "" : query);
        ArrayNode docs = body.putArray("documents");
        for (java.util.Map.Entry<String, String> e : idToText.entrySet()) {
            ObjectNode doc = docs.addObject();
            doc.put("id", e.getKey());
            doc.put("text", e.getValue() == null ? "" : e.getValue());
        }
        body.put("top_k", idToText.size());

        JsonNode root = post("/embed/rerank", body, 30);
        JsonNode results = root.get("results");
        if (results != null && results.isArray()) {
            for (JsonNode r : results) {
                String id = textOrNull(r, "id");
                if (id != null) {
                    scores.put(id, r.has("score") ? r.get("score").asDouble() : 0.0);
                }
            }
        }
        return scores;
    }

    /** 공통 POST 헬퍼: JSON 본문 전송 후 응답 루트 노드를 반환한다. */
    private JsonNode post(String path, ObjectNode body, int timeoutSec) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + path))
                    .timeout(Duration.ofSeconds(timeoutSec))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(
                            mapper.writeValueAsString(body),
                            java.nio.charset.StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = httpClient.send(
                    request,
                    HttpResponse.BodyHandlers.ofString(java.nio.charset.StandardCharsets.UTF_8));

            if (response.statusCode() != 200) {
                throw new RuntimeException("RAG 서비스 오류 응답: HTTP " + response.statusCode()
                        + " / " + response.body());
            }
            return mapper.readTree(response.body());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("RAG 서비스 호출 실패(" + path + "): " + e.getMessage(), e);
        }
    }

    private List<String> toStringList(JsonNode arrayNode) {
        List<String> out = new ArrayList<>();
        if (arrayNode != null && arrayNode.isArray()) {
            for (JsonNode item : arrayNode) {
                String v = item.asText(null);
                if (v != null && !v.trim().isEmpty()) {
                    out.add(v.trim());
                }
            }
        }
        return out;
    }

    /** RAG 서비스 헬스 체크. 사용 가능하면 true. */
    public boolean isHealthy() {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(baseUrl + "/healthz"))
                    .timeout(Duration.ofSeconds(3))
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(
                    request, HttpResponse.BodyHandlers.ofString());
            return response.statusCode() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private List<PrecedentHit> parseHits(String json) throws Exception {
        List<PrecedentHit> hits = new ArrayList<>();
        JsonNode root = mapper.readTree(json);
        JsonNode hitsNode = root.get("hits");
        if (hitsNode == null || !hitsNode.isArray()) {
            return hits;
        }

        for (JsonNode hit : hitsNode) {
            String caseId = textOrNull(hit, "case_id");
            if (caseId == null) {
                continue;
            }
            double score = hit.has("similarity_score") ? hit.get("similarity_score").asDouble() : 0.0;

            List<String> chunkTypes = new ArrayList<>();
            JsonNode chunkTypesNode = hit.get("matched_chunk_types");
            if (chunkTypesNode != null && chunkTypesNode.isArray()) {
                for (JsonNode ct : chunkTypesNode) {
                    chunkTypes.add(ct.asText());
                }
            }

            String excerpt = textOrNull(hit, "matched_excerpt");
            hits.add(new PrecedentHit(caseId, score, chunkTypes, excerpt));
        }
        return hits;
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        return value.asText();
    }
}
