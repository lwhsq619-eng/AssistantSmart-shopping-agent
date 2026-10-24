package com.example.mall.agent.dto.agent;

import lombok.Builder;
import lombok.Data;

/**
 * 运营简报段落 DTO。
 */
@Data
@Builder
public class OperationBriefingSection {
    private String sectionType;
    private String title;
    private String content;
}
