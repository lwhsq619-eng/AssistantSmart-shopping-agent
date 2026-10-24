package com.assistantsmart.shopping.model;

public record EvaluationResult(double relevanceScore, double groundedScore, double toolUsageScore) {
}
