package lawSystem.precedent;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import lawSystem.db.dao.PrecedentDAO;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * privacy_cases.jsonl 의 판례 원문을 MariaDB precedent 테이블로 적재하는 1회성 도구이다.
 *
 * 실행:
 *   - 기본 경로 사용:  java lawSystem.precedent.PrecedentImporter
 *   - 경로 지정:       java lawSystem.precedent.PrecedentImporter "<jsonl 경로>"
 *
 * precedent_id 는 external_case_id 로부터 결정적으로 생성되므로,
 * 같은 파일을 다시 실행해도 중복 없이 갱신(upsert)된다.
 */
public class PrecedentImporter {

    private static final String DEFAULT_PATH =
            "\\\\192.168.0.17\\Code\\Python\\precedent\\data\\privacy_cases.jsonl";

    private static final DateTimeFormatter YYYYMMDD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ObjectMapper mapper = new ObjectMapper();
    private final PrecedentDAO dao = new PrecedentDAO();

    public static void main(String[] args) {
        String path = args.length > 0 ? args[0] : DEFAULT_PATH;
        new PrecedentImporter().importFromJsonl(Paths.get(path));
    }

    public void importFromJsonl(Path jsonlPath) {
        if (!Files.exists(jsonlPath)) {
            System.err.println("[Importer] 파일을 찾을 수 없습니다: " + jsonlPath);
            return;
        }

        int total = 0;
        int success = 0;
        List<String> failed = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(Files.newInputStream(jsonlPath), StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }
                total++;

                try {
                    Precedent precedent = parseLine(line);
                    if (dao.upsert(precedent)) {
                        success++;
                    } else {
                        failed.add(precedent.getExternalCaseId());
                    }
                } catch (Exception e) {
                    System.err.println("[Importer] " + total + "번째 줄 파싱 실패: " + e.getMessage());
                    failed.add("line-" + total);
                }
            }
        } catch (IOException e) {
            throw new IllegalStateException("JSONL 읽기 실패: " + jsonlPath, e);
        }

        System.out.println("[Importer] 완료 - 전체 " + total
                + " / 성공 " + success
                + " / 실패 " + failed.size());
        if (!failed.isEmpty()) {
            System.out.println("[Importer] 실패 목록: " + failed);
        }
        System.out.println("[Importer] 현재 precedent 행 수: " + dao.count());
    }

    private Precedent parseLine(String line) throws IOException {
        JsonNode node = mapper.readTree(line);

        String externalCaseId = text(node, "case_id");
        if (externalCaseId == null || externalCaseId.isEmpty()) {
            throw new IllegalArgumentException("case_id 없음");
        }

        Precedent precedent = new Precedent(
                Precedent.buildPrecedentId(externalCaseId),
                externalCaseId
        );

        precedent.setCaseName(text(node, "case_name"));
        precedent.setCaseNumber(text(node, "case_number"));
        precedent.setCourtName(text(node, "court"));
        precedent.setCourtTypeCode(text(node, "court_type_code"));
        precedent.setDecisionDate(parseDate(text(node, "decision_date")));
        precedent.setCaseType(text(node, "case_type"));
        precedent.setJudgmentType(text(node, "judgment_type"));
        precedent.setIssues(text(node, "issues"));
        precedent.setSummary(text(node, "summary"));
        precedent.setReferencedStatutes(text(node, "referenced_statutes"));
        precedent.setReferencedCases(text(node, "referenced_cases"));
        precedent.setFullText(text(node, "full_text"));
        precedent.setDomain(text(node, "domain"));
        precedent.setSource(text(node, "source"));
        precedent.setSourceUrl(text(node, "source_url"));

        List<String> keywords = new ArrayList<>();
        JsonNode keywordsNode = node.get("keywords");
        if (keywordsNode != null && keywordsNode.isArray()) {
            for (JsonNode kw : keywordsNode) {
                String value = kw.asText(null);
                if (value != null && !value.trim().isEmpty()) {
                    keywords.add(value.trim());
                }
            }
        }
        precedent.setKeywords(keywords);

        return precedent;
    }

    private String text(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String s = value.asText();
        return (s == null || s.isEmpty()) ? null : s;
    }

    /** "20260312" 형태의 8자리 문자열을 LocalDate 로 변환한다. 파싱 불가 시 null. */
    private LocalDate parseDate(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() != 8) {
            return null;
        }
        try {
            return LocalDate.parse(digits, YYYYMMDD);
        } catch (Exception e) {
            return null;
        }
    }
}
