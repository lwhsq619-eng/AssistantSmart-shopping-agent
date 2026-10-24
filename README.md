# AssistantSmart-shopping-agent

Multi-agent intelligent shopping guide platform built with **Spring Boot + LangGraph-style orchestration + Milvus-oriented RAG retriever design**, integrating retrieval, function calling, session memory, traceable source links, and response evaluation.

## Implemented modules

- **Spring Boot API**: `POST /api/shopping/guide`
- **LangGraph-style multi-agent orchestration**:
  - retrieval node
  - tool/function-calling node
  - recommendation node
  - evaluation node
- **RAG retrieval module** (`MilvusRagRetriever`): retrieval abstraction designed for Milvus-backed vector search
- **Function calling module** (`FunctionCallingService`): shopping functions such as `compareProducts` and `budgetAdvisor`
- **Session memory module** (`SessionMemoryService`): per-session short-term memory window
- **Log + link tracking module** (`LogLinkTrackerService`): trace ID generation and source-link mapping
- **Evaluation module** (`EvaluationService`): relevance/groundedness/tool-usage scores

## Quick start

```bash
mvn spring-boot:run
```

Example request:

```bash
curl -X POST http://127.0.0.1:8080/api/shopping/guide \
  -H 'Content-Type: application/json' \
  -d '{"sessionId":"s1","message":"compare phone options for my budget"}'
```

## Test

```bash
mvn -Dtest=ShoppingGuideControllerTest test
```
