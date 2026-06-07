"use strict";

const resultEl = document.getElementById("result");
const statusEl = document.getElementById("status");

function val(id) { return document.getElementById(id).value.trim(); }
function intVal(id) { const v = parseInt(document.getElementById(id).value, 10); return isNaN(v) ? null : v; }

function setStatus(text) { statusEl.textContent = text || ""; }
function setButtonsDisabled(disabled) {
    document.querySelectorAll(".actions button").forEach(b => b.disabled = disabled);
}
function escapeHtml(s) {
    return (s == null ? "" : String(s))
        .replace(/&/g, "&amp;").replace(/</g, "&lt;").replace(/>/g, "&gt;");
}

async function postJson(path, body) {
    const res = await fetch(path, {
        method: "POST",
        headers: { "Content-Type": "application/json; charset=utf-8" },
        body: JSON.stringify(body),
    });
    if (!res.ok) {
        const txt = await res.text();
        throw new Error("HTTP " + res.status + " — " + txt);
    }
    return res.json();
}

function renderError(e) {
    resultEl.innerHTML = '<p class="err">오류: ' + escapeHtml(e.message) + "</p>";
}

function renderSummary(d) {
    const issues = (d.mainIssues || []).map(i => "<li>" + escapeHtml(i) + "</li>").join("");
    resultEl.innerHTML =
        '<div class="block"><h3>요약</h3><pre class="text">' + escapeHtml(d.summary) + "</pre></div>" +
        '<div class="block"><h3>주요 쟁점</h3><ul>' + issues + "</ul></div>" +
        '<div class="block"><h3>타임라인</h3><pre class="text">' + escapeHtml(d.timeline) + "</pre></div>" +
        '<div class="block"><h3>저장된 결과 ID</h3><code>' + escapeHtml(d.aiResultId) +
        "</code> · 신뢰도 " + (d.confidenceScore ?? 0) + "</div>";
}

function renderKeywords(d) {
    const tags = (d.keywords || []).map(k => '<span class="tag">' + escapeHtml(k) + "</span>").join("");
    resultEl.innerHTML = '<div class="block"><h3>추출 키워드</h3>' +
        (tags || '<span class="placeholder">키워드 없음</span>') + "</div>";
}

function renderPrecedents(list) {
    if (!list || list.length === 0) {
        resultEl.innerHTML = '<p class="placeholder">유사 판례를 찾지 못했습니다.</p>';
        return;
    }
    resultEl.innerHTML = list.map(p =>
        '<div class="prec">' +
        '<span class="score">유사도 ' + Number(p.similarityScore).toFixed(4) + "</span>" +
        '<div class="title">' + escapeHtml(p.title) + "</div>" +
        '<div class="meta">쟁점: ' + escapeHtml((p.legalIssue || "").slice(0, 200)) + "</div>" +
        '<div class="meta">요지: ' + escapeHtml((p.summary || "").slice(0, 300)) + "</div>" +
        "</div>"
    ).join("");
}

function renderLegal(d) {
    const statutes = (d.relatedStatutes || []).map(s => '<span class="tag">' + escapeHtml(s) + "</span>").join("");
    const cited = (d.citedCases || []).map(c => '<span class="tag">' + escapeHtml(c) + "</span>").join("");
    resultEl.innerHTML =
        '<div class="block"><h3>핵심 쟁점</h3><pre class="text">' + escapeHtml(d.issueSummary) + "</pre></div>" +
        '<div class="block"><h3>적용 법리</h3><pre class="text">' + escapeHtml(d.applicableLaw) + "</pre></div>" +
        '<div class="block"><h3>법리 설명</h3><pre class="text">' + escapeHtml(d.legalExplanation) + "</pre></div>" +
        '<div class="block"><h3>관련 법령</h3>' + (statutes || '<span class="placeholder">없음</span>') + "</div>" +
        '<div class="block"><h3>인용 판례(case_id)</h3>' + (cited || '<span class="placeholder">없음</span>') + "</div>";
}

const handlers = {
    summary: async () => renderSummary(await postJson("/api/ai/summary",
        { text: val("caseText"), caseId: val("caseId") || null })),
    keywords: async () => renderKeywords(await postJson("/api/ai/keywords",
        { text: val("caseText"), maxKeywords: intVal("maxKeywords") })),
    precedents: async () => renderPrecedents(await postJson("/api/ai/similar-precedents",
        { text: val("caseText"), caseId: val("caseId") || null, topK: intVal("topK") })),
    legal: async () => renderLegal(await postJson("/api/ai/legal-rules",
        { text: val("caseText"), caseId: val("caseId") || null, topK: intVal("topK") })),
};

const labels = { summary: "요약", keywords: "키워드 추출", precedents: "유사 판례 검색", legal: "법리 분석" };

document.querySelectorAll(".actions button").forEach(btn => {
    btn.addEventListener("click", async () => {
        const action = btn.dataset.action;
        if (!val("caseText")) {
            resultEl.innerHTML = '<p class="err">사건 내용을 입력하세요.</p>';
            return;
        }
        setButtonsDisabled(true);
        setStatus("· " + labels[action] + " 실행 중… (LLM 호출은 수 초~수십 초)");
        resultEl.innerHTML = '<p class="placeholder">처리 중…</p>';
        try {
            await handlers[action]();
        } catch (e) {
            renderError(e);
        } finally {
            setButtonsDisabled(false);
            setStatus("");
        }
    });
});
