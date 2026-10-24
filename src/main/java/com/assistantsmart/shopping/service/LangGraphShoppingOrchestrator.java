package com.assistantsmart.shopping.service;

import com.assistantsmart.shopping.model.ChatResponse;
import com.assistantsmart.shopping.model.EvaluationResult;
import com.assistantsmart.shopping.model.RetrievalDocument;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LangGraphShoppingOrchestrator {

    private final RagRetriever ragRetriever;
    private final SessionMemoryService sessionMemoryService;
    private final FunctionCallingService functionCallingService;
    private final LogLinkTrackerService logLinkTrackerService;
    private final EvaluationService evaluationService;

    public LangGraphShoppingOrchestrator(
            RagRetriever ragRetriever,
            SessionMemoryService sessionMemoryService,
            FunctionCallingService functionCallingService,
            LogLinkTrackerService logLinkTrackerService,
            EvaluationService evaluationService) {
        this.ragRetriever = ragRetriever;
        this.sessionMemoryService = sessionMemoryService;
        this.functionCallingService = functionCallingService;
        this.logLinkTrackerService = logLinkTrackerService;
        this.evaluationService = evaluationService;
    }

    public ChatResponse guide(String sessionId, String message) {
        List<String> history = sessionMemoryService.load(sessionId);
        List<RetrievalDocument> docs = ragRetriever.retrieve(message);
        String functionName = functionCallingService.selectFunction(message);
        String functionResult = functionCallingService.execute(functionName, message);

        String context = docs.stream().map(RetrievalDocument::content).reduce("", (a, b) -> a + " " + b).trim();
        String historyHint = history.isEmpty() ? "" : " Previous preference: " + history.get(history.size() - 1) + ".";
        String answer = "Recommendation: " + functionResult + " Context: " + context + "." + historyHint;

        List<String> links = docs.stream().map(RetrievalDocument::sourceLink).distinct().toList();
        String traceId = logLinkTrackerService.track(links);
        EvaluationResult evaluation = evaluationService.evaluate(message, links, functionName);

        sessionMemoryService.append(sessionId, message);
        sessionMemoryService.append(sessionId, answer);

        return new ChatResponse(sessionId, traceId, answer, functionName, links, evaluation);
    }
}
