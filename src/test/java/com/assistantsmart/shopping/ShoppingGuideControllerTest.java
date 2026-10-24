package com.assistantsmart.shopping;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ShoppingGuideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturnGuideResponseWithRagFunctionAndEvaluation() throws Exception {
        mockMvc.perform(post("/api/shopping/guide")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  \"sessionId\": \"s1\",
                                  \"message\": \"compare phone options for my budget\"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessionId").value("s1"))
                .andExpect(jsonPath("$.traceId").isNotEmpty())
                .andExpect(jsonPath("$.functionCalled").value("compareProducts"))
                .andExpect(jsonPath("$.sourceLinks[0]").exists())
                .andExpect(jsonPath("$.evaluation.relevanceScore").isNumber())
                .andExpect(jsonPath("$.answer").value(org.hamcrest.Matchers.containsString("Recommendation:")));
    }
}
