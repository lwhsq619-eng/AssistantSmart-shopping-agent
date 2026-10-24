package com.assistantsmart.shopping.service;

import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class FunctionCallingService {

    public String selectFunction(String message) {
        String normalized = message.toLowerCase(Locale.ROOT);
        if (normalized.contains("compare")) {
            return "compareProducts";
        }
        if (normalized.contains("budget") || normalized.contains("cheap")) {
            return "budgetAdvisor";
        }
        return "generalAdvisor";
    }

    public String execute(String functionName, String message) {
        return switch (functionName) {
            case "compareProducts" -> "Compared key options and prioritized value for money.";
            case "budgetAdvisor" -> "Filtered recommendations to fit your budget constraints.";
            default -> "Generated a general shopping recommendation path.";
        };
    }
}
