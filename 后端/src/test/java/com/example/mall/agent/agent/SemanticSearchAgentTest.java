package com.example.mall.agent.agent;

import com.example.mall.agent.controller.SemanticSearchController;
import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.SemanticSearchRequest;
import com.example.mall.agent.dto.agent.SemanticSearchResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.prompt.SemanticSearchPrompt;
import com.example.mall.agent.service.DeepSeekService;
import com.example.mall.agent.tool.ProductTool;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SemanticSearchAgentTest {
    @Mock
    private ProductTool productTool;
    @Mock
    private DeepSeekService deepSeekService;
    @Mock
    private SemanticSearchPrompt semanticSearchPrompt;

    private SemanticSearchAgent semanticSearchAgent;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        semanticSearchAgent = new SemanticSearchAgent(productTool, deepSeekService, semanticSearchPrompt, objectMapper);
    }

    @Test
    void keywordBlankShouldFailValidation() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(post("/api/ai/search/semantic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keyword":"","page":1,"size":10}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void pageLessThanOneShouldFailValidation() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(post("/api/ai/search/semantic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keyword":"耳机","page":0,"size":10}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void sizeGreaterThanFiftyShouldFailValidation() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(post("/api/ai/search/semantic")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"keyword":"耳机","page":1,"size":51}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void invalidDeepSeekJsonShouldFallbackToRuleIntent() {
        mockPromptAndAi("这不是合法 JSON");
        when(productTool.searchProducts(anyString(), nullable(String.class), nullable(BigDecimal.class), nullable(BigDecimal.class)))
                .thenReturn(List.of(product(1L, "无线耳机", "数码科技", "适合日常使用", "199", 20, 100)));

        SemanticSearchResponse response = semanticSearchAgent.search(request("推荐无线耳机", 1, 10));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getFallback()).isTrue();
        assertThat(response.getProducts()).hasSize(1);
        assertThat(response.getNormalizedKeyword()).isEqualTo("无线耳机");
    }

    @Test
    void exactEmptyShouldUseSynonymExpansion() {
        mockPromptAndAi("非法 JSON");
        when(productTool.searchProducts(anyString(), nullable(String.class), nullable(BigDecimal.class), nullable(BigDecimal.class)))
                .thenReturn(List.of());
        when(productTool.searchProducts(eq("静音"), nullable(String.class), nullable(BigDecimal.class), nullable(BigDecimal.class)))
                .thenReturn(List.of(product(2L, "静音小风扇", "家居生活", "宿舍静音便携", "59", 30, 80)));

        SemanticSearchResponse response = semanticSearchAgent.search(request("适合宿舍用的安静小风扇", 1, 10));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getProducts()).extracting("productId").containsExactly(2L);
        verify(productTool).searchProducts(eq("小风扇"), eq("家居生活"), nullable(BigDecimal.class), nullable(BigDecimal.class));
        verify(productTool, atLeastOnce()).searchProducts(eq("静音"), nullable(String.class), nullable(BigDecimal.class), nullable(BigDecimal.class));
    }

    @Test
    void expansionEmptyShouldUseHotProductsFallback() {
        mockPromptAndAi("非法 JSON");
        when(productTool.searchProducts(anyString(), nullable(String.class), nullable(BigDecimal.class), nullable(BigDecimal.class)))
                .thenReturn(List.of());
        when(productTool.getHotProducts(10))
                .thenReturn(List.of(product(3L, "热销耳机", "数码科技", "热销商品", "299", 10, 500)));

        SemanticSearchResponse response = semanticSearchAgent.search(request("找一个不存在的商品", 1, 10));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getFallback()).isTrue();
        assertThat(response.getProducts()).extracting("productId").containsExactly(3L);
        verify(productTool).getHotProducts(10);
    }

    @Test
    void zeroStockProductShouldBeDownRanked() {
        mockPromptAndAi("""
                {"normalizedKeyword":"耳机","categoryName":"数码科技","sortBy":"RELEVANCE"}
                """);
        when(productTool.searchProducts(anyString(), nullable(String.class), nullable(BigDecimal.class), nullable(BigDecimal.class)))
                .thenReturn(List.of(
                        product(4L, "耳机A", "数码科技", "耳机", "199", 0, 100),
                        product(5L, "耳机B", "数码科技", "耳机", "199", 10, 100)
                ));

        SemanticSearchResponse response = semanticSearchAgent.search(request("耳机", 1, 10));

        assertThat(response.getProducts()).extracting("productId").containsExactly(5L, 4L);
        assertThat(response.getProducts().get(0).getMatchScore()).isGreaterThan(response.getProducts().get(1).getMatchScore());
    }

    @Test
    void productOverBudgetShouldBeDownRanked() {
        mockPromptAndAi("""
                {"normalizedKeyword":"耳机","categoryName":"数码科技","maxPrice":100,"sortBy":"RELEVANCE"}
                """);
        when(productTool.searchProducts(anyString(), nullable(String.class), nullable(BigDecimal.class), nullable(BigDecimal.class)))
                .thenReturn(List.of(
                        product(6L, "耳机低价款", "数码科技", "耳机", "80", 10, 100),
                        product(7L, "耳机高价款", "数码科技", "耳机", "200", 10, 100)
                ));

        SemanticSearchResponse response = semanticSearchAgent.search(request("100元以内耳机", 1, 10));

        assertThat(response.getProducts()).extracting("productId").containsExactly(6L, 7L);
        assertThat(response.getProducts().get(0).getMatchScore()).isGreaterThan(response.getProducts().get(1).getMatchScore());
    }

    @Test
    void paginationShouldNotOutOfBounds() {
        mockPromptAndAi("""
                {"normalizedKeyword":"耳机","categoryName":"数码科技","sortBy":"RELEVANCE"}
                """);
        when(productTool.searchProducts(anyString(), nullable(String.class), nullable(BigDecimal.class), nullable(BigDecimal.class)))
                .thenReturn(List.of(product(8L, "耳机", "数码科技", "耳机", "99", 10, 10)));

        SemanticSearchResponse response = semanticSearchAgent.search(request("耳机", 99, 10));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getProducts()).isEmpty();
    }

    @Test
    void emptyResultShouldReturnEmptyProductsNotNull() {
        mockPromptAndAi(null);
        when(productTool.searchProducts(anyString(), nullable(String.class), nullable(BigDecimal.class), nullable(BigDecimal.class)))
                .thenReturn(List.of());
        when(productTool.getHotProducts(10)).thenReturn(List.of());

        SemanticSearchResponse response = semanticSearchAgent.search(request("完全不存在的商品", 1, 10));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getProducts()).isNotNull().isEmpty();
        assertThat(response.getTotal()).isZero();
    }

    private MockMvc buildMockMvc() {
        SemanticSearchAgent mockAgent = mock(SemanticSearchAgent.class);
        SemanticSearchController controller = new SemanticSearchController(mockAgent);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    private void mockPromptAndAi(String content) {
        when(semanticSearchPrompt.systemPrompt()).thenReturn("semantic search prompt");
        when(deepSeekService.chat(anyString(), anyString())).thenReturn(AiChatResponse.ok("deepseek-chat", content));
    }

    private SemanticSearchRequest request(String keyword, int page, int size) {
        SemanticSearchRequest request = new SemanticSearchRequest();
        request.setKeyword(keyword);
        request.setPage(page);
        request.setSize(size);
        return request;
    }

    private ProductToolResponse product(Long id,
                                        String name,
                                        String categoryName,
                                        String description,
                                        String price,
                                        Integer stock,
                                        Integer sales) {
        return ProductToolResponse.builder()
                .productId(id)
                .productName(name)
                .categoryName(categoryName)
                .description(description)
                .price(new BigDecimal(price))
                .stock(stock)
                .sales(sales)
                .imageUrl("https://example.com/product.jpg")
                .discountInfo("")
                .build();
    }
}
