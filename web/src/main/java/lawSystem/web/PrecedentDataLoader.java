package lawSystem.web;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Set;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import lawSystem.jpa.entity.Precedent;
import lawSystem.web.repository.PrecedentRepository;

/**
 * 앱 시작 시 판례 원문(privacy_cases.jsonl)을 JPA(precedent 테이블)로 1회 적재한다.
 * precedent 가 비어 있을 때만 실행(이미 있으면 건너뜀).
 *
 * 적재 후에는 Python 에서 `python -m scripts.build_index` 로 Chroma 인덱스를 만들어야
 * 검색/유사판례 기능이 동작한다. (DB=원본, Chroma=검색 인덱스)
 */
@Component
@Order(2)
public class PrecedentDataLoader implements CommandLineRunner {

    private static final DateTimeFormatter YMD = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final PrecedentRepository precedentRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    @Value("${precedent.data.jsonl:}")
    private String jsonlPath;

    public PrecedentDataLoader(PrecedentRepository precedentRepository) {
        this.precedentRepository = precedentRepository;
    }

    @Override
    public void run(String... args) {
        try {
            long existing = precedentRepository.count();
            if (existing > 0) {
                System.out.println("[Precedent] 이미 적재됨 (" + existing + "건) — 적재 건너뜀");
                return;
            }
            if (jsonlPath == null || jsonlPath.isBlank()) {
                System.err.println("[Precedent] precedent.data.jsonl 경로 미설정 — 적재 건너뜀");
                return;
            }
            Path path = Paths.get(jsonlPath);
            if (!Files.exists(path)) {
                System.err.println("[Precedent] JSONL 파일 없음: " + path);
                return;
            }

            int count = 0;
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty()) {
                        continue;
                    }
                    precedentRepository.save(parse(line));
                    count++;
                }
            }
            System.out.println("[Precedent] JPA 적재 완료: " + count + "건 → 이제 Python build_index 실행하세요.");
        } catch (Exception e) {
            System.err.println("[Precedent] 적재 실패(앱은 계속 기동): " + e.getMessage());
        }
    }

    private Precedent parse(String line) throws Exception {
        JsonNode node = mapper.readTree(line);
        String ext = text(node, "case_id");
        Precedent p = new Precedent("PREC-" + ext, ext);

        p.setCaseName(text(node, "case_name"));
        p.setCaseNumber(text(node, "case_number"));
        p.setCourtName(text(node, "court"));
        p.setCourtTypeCode(text(node, "court_type_code"));
        p.setDecisionDate(parseDate(text(node, "decision_date")));
        p.setCaseType(text(node, "case_type"));
        p.setJudgmentType(text(node, "judgment_type"));
        p.setIssues(text(node, "issues"));
        p.setSummary(text(node, "summary"));
        p.setReferencedStatutes(text(node, "referenced_statutes"));
        p.setReferencedCases(text(node, "referenced_cases"));
        p.setFullText(text(node, "full_text"));
        p.setDomain(text(node, "domain"));
        p.setSource(text(node, "source"));
        p.setSourceUrl(text(node, "source_url"));

        Set<String> keywords = new HashSet<>();
        JsonNode kw = node.get("keywords");
        if (kw != null && kw.isArray()) {
            for (JsonNode k : kw) {
                String v = k.asText(null);
                if (v != null && !v.trim().isEmpty()) {
                    keywords.add(v.trim());
                }
            }
        }
        p.setKeywords(keywords);
        return p;
    }

    private String text(JsonNode node, String field) {
        JsonNode v = node.get(field);
        if (v == null || v.isNull()) {
            return null;
        }
        String s = v.asText();
        return s.isEmpty() ? null : s;
    }

    private LocalDate parseDate(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("[^0-9]", "");
        if (digits.length() != 8) {
            return null;
        }
        try {
            return LocalDate.parse(digits, YMD);
        } catch (Exception e) {
            return null;
        }
    }
}
