import sys

from rag.service import PrecedentRAGService


def main() -> None:
    if len(sys.argv) < 2:
        print('사용법: python -m scripts.ask "질문 내용"')
        sys.exit(1)

    query = " ".join(sys.argv[1:])
    service = PrecedentRAGService()

    print(f"[질문] {query}\n")
    print("=== 답변 (스트리밍) ===\n")
    result = service.answer(query, stream=True)

    print("\n=== 인용 판례 ===")
    for case in result["cases"]:
        print(
            f"- [{case['case_number']}] {case['case_name']} "
            f"({case['court']}, {case['decision_date']}) score={case['score']:.4f}"
        )


if __name__ == "__main__":
    main()
