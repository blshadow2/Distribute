# -*- coding: utf-8 -*-
import matplotlib
matplotlib.use("Agg")
import matplotlib.pyplot as plt
from matplotlib.patches import Rectangle

plt.rcParams["font.family"] = "Malgun Gothic"
plt.rcParams["axes.unicode_minus"] = False

fig, ax = plt.subplots(figsize=(10, 7.2), dpi=120)
ax.set_xlim(0, 1000)
ax.set_ylim(0, 720)
ax.invert_yaxis()
ax.axis("off")


def box(x, y, w, h, fc, ec="#d9d9dd"):
    ax.add_patch(Rectangle((x, y), w, h, linewidth=1.4, edgecolor=ec, facecolor=fc))


def t(x, y, s, size=11, color="#374151", weight="normal"):
    ax.text(x, y, s, fontsize=size, color=color, weight=weight, ha="center", va="center")


def arrow(x1, y1, x2, y2, label="", lx=None, ly=None):
    ax.annotate("", xy=(x2, y2), xytext=(x1, y1),
                arrowprops=dict(arrowstyle="-|>", color="#17171c", lw=1.7))
    if label:
        ax.text(lx if lx is not None else (x1 + x2) / 2,
                ly if ly is not None else (y1 + y2) / 2,
                label, fontsize=10.5, color="#1863dc", weight="bold", ha="center", va="center")


# title
t(500, 28, "AI 기반 법률 사무 플랫폼 — 시스템 아키텍처", 18, "#003c33", "bold")
t(500, 52, "JAVA(Spring Boot)  ·  PYTHON(RAG)  ·  MariaDB(JPA)", 10, "#75758a")

# Browser
box(375, 72, 250, 58, "#eeece7")
t(500, 92, "브라우저", 14, "#17171c", "bold")
t(500, 114, "의뢰인 · 대표/소속 변호사 · 사무직원", 10.5)

# Spring Boot
box(300, 178, 400, 158, "#f1f5ff")
t(500, 200, "Spring Boot 웹앱  ·  :8080", 14, "#17171c", "bold")
t(500, 226, "Thymeleaf 화면 + @RestController(AI JSON)", 10.5)
t(500, 248, "Controller → Service(@Transactional) → JPA Repository", 10.5)
t(500, 270, "인증: 세션 · 권한: @RoleAllowed · 사건 소유권", 10.5)
t(500, 292, "사건·증거·검색·상담·수임·배당·진행·AI", 10.5)
t(500, 316, "시작 시드: 회원 → 판례 → 담당사건", 9.5, "#75758a")

# MariaDB
box(70, 430, 330, 150, "#edfce9")
t(235, 452, "MariaDB  ·  law_system_jpa", 13, "#17171c", "bold")
t(235, 476, "단일 진실 공급원 (Hibernate)", 10.5)
t(235, 497, "member·client·lawyer·legal_case", 10.5)
t(235, 517, "consultation·retainer·progression", 10.5)
t(235, 537, "ai_analysis_request/result", 10.5)
t(235, 559, "precedent · precedent_keyword", 10.5)

# Python RAG
box(600, 430, 340, 150, "#eeece7")
t(770, 452, "Python RAG 서비스  ·  :8000", 13, "#17171c", "bold")
t(770, 476, "FastAPI · 검색/생성 API", 10.5)
t(770, 497, "BGE-M3 임베딩 + Chroma + BM25/RRF", 10.5)
t(770, 517, "tasks: 요약·키워드·법리 + rerank(변호사)", 10.5)
t(770, 537, "검색결과 = external_case_id + score", 10.5)
t(770, 559, "JSONL→DB→build_index→Chroma", 9.5, "#75758a")

# Ollama
box(615, 636, 310, 58, "#003c33", "#003c33")
t(770, 657, "Ollama  ·  :11434", 13, "#ffffff", "bold")
t(770, 679, "로컬 LLM (qwen2.5 / gemma)", 10, "#edfce9")

# arrows
arrow(500, 130, 500, 176, "HTTP", 540, 153)
arrow(395, 336, 258, 428, "JPA", 300, 388)
arrow(605, 336, 742, 428, "HTTP/1.1", 690, 388)
arrow(598, 510, 404, 510, "pymysql(판례)", 500, 495)
arrow(770, 582, 770, 634, "LLM", 815, 608)

t(500, 712, "세 구성요소는 LAWSYSTEM_DB_URL 로 같은 DB(law_system_jpa)를 공유한다", 9.5, "#75758a")

out = r"\\192.168.0.17\Code\java\Distribute\architecture.png"
plt.savefig(out, dpi=120, facecolor="white")
print("saved:", out)
