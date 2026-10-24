package com.assistantsmart.shopping.service;

import com.assistantsmart.shopping.model.EvaluationResult;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EvaluationService {

    public EvaluationResult evaluate(String message, List<String> sources, String functionName) {
        double relevance = message.length() > 8 ? 0.9 : 0.6;
        double grounded = sources.isEmpty() ? 0.5 : 0.95;
        double toolUsage = "generalAdvisor".equals(functionName) ? 0.8 : 0.95;
        return new EvaluationResult(relevance, grounded, toolUsage);
    }
}
