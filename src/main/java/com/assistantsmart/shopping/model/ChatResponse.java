package com.assistantsmart.shopping.model;

import java.util.List;

public record ChatResponse(
        String sessionId,
        String traceId,
        String answer,
        String functionCalled,
        List<String> sourceLinks,
        EvaluationResult evaluation
) {
}
