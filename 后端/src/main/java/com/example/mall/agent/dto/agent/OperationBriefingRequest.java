package com.example.mall.agent.dto.agent;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

/**
 * 自动化运营简报请求 DTO。
 */
@Data
public class OperationBriefingRequest {
    private LocalDate startDate;
    private LocalDate endDate;
    private String briefingType;
    private List<String> sections;
}
