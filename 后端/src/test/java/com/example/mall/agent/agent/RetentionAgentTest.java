package com.example.mall.agent.agent;

import com.example.mall.agent.controller.RetentionController;
import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.RetentionRequest;
import com.example.mall.agent.dto.agent.RetentionResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.dto.tool.UserToolResponse;
import com.example.mall.agent.memory.ChatMemoryService;
import com.example.mall.agent.prompt.RetentionPrompt;
import com.example.mall.agent.service.DeepSeekService;
import com.example.mall.agent.tool.OrderTool;
import com.example.mall.agent.tool.ProductTool;
import com.example.mall.agent.tool.UserTool;
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
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class RetentionAgentTest {
    @Mock
    private UserTool userTool;
    @Mock
    private ProductTool productTool;
    @Mock
    private OrderTool orderTool;
    @Mock
    private DeepSeekService deepSeekService;
    @Mock
    private RetentionPrompt retentionPrompt;

    private RetentionAgent retentionAgent;

    @BeforeEach
    void setUp() {
        retentionAgent = new RetentionAgent(
                userTool,
                productTool,
                orderTool,
                deepSeekService,
                retentionPrompt,
                new ChatMemoryService(),
                new ObjectMapper()
        );
    }

    @Test
    void userIdNullShouldFailValidation() throws Exception {
        MockMvc mockMvc = buildMockMvc();

        mockMvc.perform(post("/api/ai/retention/generate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"eventType":"CART_ABANDON","productId":1}
                                """))
                .andExpect(status().isBadRequest());
    }

    @Test
    void illegalEventTypeShouldFail() {
        RetentionResponse response = retentionAgent.generate(request(1L, "BAD_EVENT", 1L));

        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getProducts()).isNotNull().isEmpty();
        assertThat(response.getErrorMessage()).contains("非法 eventType");
    }

    @Test
    void cartAbandonMissingProductIdShouldFail() {
        RetentionResponse response = retentionAgent.generate(request(1L, "CART_ABANDON", null));

        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("productId");
    }

    @Test
    void compareEventNeedsAtLeastTwoProducts() {
        RetentionRequest request = request(1L, "PRODUCT_COMPARE_NO_BUY", null);
        request.setComparedProductIds(List.of(1L));

        RetentionResponse response = retentionAgent.generate(request);

        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getErrorMessage()).contains("comparedProductIds");
    }

    @Test
    void realCouponShouldSetCouponAvailableTrue() {
        stubUser(1L, List.of("满100减10"));
        stubTargetProduct(product(1L, "耳机", "199", 10));
        stubEmptyCandidates();
        stubAi("""
                {"strategyType":"CART_REMINDER","message":"商品仍有库存，可以继续查看。","recommendedProductIds":[1],"couponAvailable":false}
                """);

        RetentionResponse response = retentionAgent.generate(request(1L, "CART_ABANDON", 1L));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getCouponAvailable()).isTrue();
        assertThat(response.getCouponText()).contains("满100减10");
    }

    @Test
    void noCouponShouldNotGenerateCouponText() {
        stubUser(1L, List.of());
        stubTargetProduct(product(1L, "耳机", "199", 10));
        stubEmptyCandidates();
        stubAi("""
                {"strategyType":"CART_REMINDER","message":"商品仍有库存，可以继续查看。","recommendedProductIds":[1],"couponAvailable":false}
                """);

        RetentionResponse response = retentionAgent.generate(request(1L, "CART_ABANDON", 1L));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getCouponAvailable()).isFalse();
        assertThat(response.getCouponText()).isNull();
        assertThat(response.getMessage()).doesNotContain("优惠券");
    }

    @Test
    void fakeCouponFromDeepSeekShouldBeOverriddenByJava() {
        stubUser(1L, List.of());
        stubTargetProduct(product(1L, "耳机", "199", 10));
        stubEmptyCandidates();
        stubAi("""
                {"strategyType":"CART_REMINDER","message":"你有优惠券可以领取，快来下单。","recommendedProductIds":[1],"couponAvailable":true}
                """);

        RetentionResponse response = retentionAgent.generate(request(1L, "CART_ABANDON", 1L));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getCouponAvailable()).isFalse();
        assertThat(response.getCouponText()).isNull();
        assertThat(response.getMessage()).doesNotContain("优惠券");
    }

    @Test
    void nonexistentProductIdFromDeepSeekShouldBeFiltered() {
        stubUser(1L, List.of());
        stubTargetProduct(product(1L, "耳机", "199", 10));
        stubEmptyCandidates();
        stubAi("""
                {"strategyType":"CART_REMINDER","message":"商品仍有库存，可以继续查看。","recommendedProductIds":[999],"couponAvailable":false}
                """);

        RetentionResponse response = retentionAgent.generate(request(1L, "CART_ABANDON", 1L));

        assertThat(response.getProducts()).extracting("productId").doesNotContain(999L);
        assertThat(response.getProducts()).extracting("productId").containsExactly(1L);
    }

    @Test
    void zeroStockProductShouldNotEnterRecommendationList() {
        stubUser(1L, List.of());
        stubTargetProduct(product(1L, "缺货耳机", "199", 0));
        when(productTool.getDiscountProducts(6)).thenReturn(List.of());
        when(productTool.getHotProducts(6)).thenReturn(List.of(product(2L, "热销耳机", "299", 8)));
        stubAi("""
                {"strategyType":"CART_REMINDER","message":"可以看看其他商品。","recommendedProductIds":[1,2],"couponAvailable":false}
                """);

        RetentionResponse response = retentionAgent.generate(request(1L, "CART_ABANDON", 1L));

        assertThat(response.getProducts()).extracting("productId").containsExactly(2L);
        assertThat(response.getProducts()).extracting("productId").doesNotContain(1L);
    }

    @Test
    void invalidJsonShouldUseFallbackMessage() {
        stubUser(1L, List.of());
        stubTargetProduct(product(1L, "耳机", "199", 10));
        stubEmptyCandidates();
        stubAi("not json");

        RetentionResponse response = retentionAgent.generate(request(1L, "CART_ABANDON", 1L));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getFallback()).isTrue();
        assertThat(response.getMessage()).contains("加入购物车");
    }

    @Test
    void emptyProductToolsShouldReturnEmptyProducts() {
        stubUser(1L, List.of());
        when(productTool.getDiscountProducts(6)).thenReturn(List.of());
        when(productTool.getHotProducts(6)).thenReturn(List.of());
        stubAi("not json");

        RetentionResponse response = retentionAgent.generate(request(1L, "SEARCH_NO_CLICK", null));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getProducts()).isNotNull().isEmpty();
    }

    @Test
    void sameEventWithinTenMinutesShouldBeSuppressed() {
        stubUser(1L, List.of());
        stubTargetProduct(product(1L, "耳机", "199", 10));
        stubEmptyCandidates();
        stubAi("not json");

        RetentionResponse first = retentionAgent.generate(request(1L, "CART_ABANDON", 1L));
        RetentionResponse second = retentionAgent.generate(request(1L, "CART_ABANDON", 1L));

        assertThat(first.getSuccess()).isTrue();
        assertThat(second.getSuccess()).isTrue();
        assertThat(second.getMessage()).isEqualTo("近期已生成过挽留提示，本次不重复推送。");
        assertThat(second.getProducts()).isNotNull().isEmpty();
        verify(userTool).getUserProfile(1L);
    }

    @Test
    void compareEventShouldOnlyReturnRealCandidateProducts() {
        stubUser(1L, List.of());
        when(productTool.getProductDetail(1L)).thenReturn(product(1L, "耳机A", "199", 10));
        when(productTool.getProductDetail(2L)).thenReturn(product(2L, "耳机B", "299", 10));
        stubAi("""
                {"strategyType":"COMPARE_ASSIST","message":"可按价格和库存继续比较。","recommendedProductIds":[1,999],"couponAvailable":false}
                """);
        RetentionRequest request = request(1L, "PRODUCT_COMPARE_NO_BUY", null);
        request.setComparedProductIds(List.of(1L, 2L));

        RetentionResponse response = retentionAgent.generate(request);

        assertThat(response.getProducts()).extracting("productId").containsExactly(1L);
        assertThat(response.getProducts()).extracting("productId").doesNotContain(999L);
        verify(productTool, never()).getHotProducts(6);
    }

    @Test
    void productsShouldNeverBeNull() {
        RetentionResponse response = retentionAgent.generate(request(1L, "BAD_EVENT", 1L));

        assertThat(response.getProducts()).isNotNull();
        assertThat(response.getRecommendedProducts()).isNotNull();
    }

    private MockMvc buildMockMvc() {
        RetentionAgent mockAgent = mock(RetentionAgent.class);
        RetentionController controller = new RetentionController(mockAgent);
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.afterPropertiesSet();
        return MockMvcBuilders.standaloneSetup(controller)
                .setValidator(validator)
                .build();
    }

    private void stubUser(Long userId, List<String> coupons) {
        when(userTool.getUserProfile(userId)).thenReturn(UserToolResponse.builder()
                .userId(userId)
                .username("test-user")
                .historyCategories(List.of())
                .preferenceTags(List.of())
                .recentOrders(List.of())
                .cartItems(List.of())
                .coupons(coupons)
                .build());
        when(userTool.getUserCouponList(userId)).thenReturn(coupons);
        when(userTool.getUserBehaviorSummary(userId)).thenReturn(UserToolResponse.builder()
                .userId(userId)
                .historyCategories(List.of())
                .preferenceTags(List.of())
                .build());
        when(userTool.getUserPreference(userId)).thenReturn(List.of());
        when(orderTool.getUserOrders(userId, 10)).thenReturn(List.of());
    }

    private void stubTargetProduct(ProductToolResponse product) {
        when(productTool.getProductDetail(product.getProductId())).thenReturn(product);
    }

    private void stubEmptyCandidates() {
        when(productTool.getDiscountProducts(6)).thenReturn(List.of());
        when(productTool.getHotProducts(6)).thenReturn(List.of());
    }

    private void stubAi(String content) {
        when(retentionPrompt.systemPrompt()).thenReturn("retention prompt");
        when(deepSeekService.chat(anyString(), anyString())).thenReturn(AiChatResponse.ok("deepseek-chat", content));
    }

    private RetentionRequest request(Long userId, String eventType, Long productId) {
        RetentionRequest request = new RetentionRequest();
        request.setUserId(userId);
        request.setEventType(eventType);
        request.setProductId(productId);
        request.setSessionId("ret-test-" + userId + "-" + eventType + "-" + productId);
        request.setMessageContext("test context");
        return request;
    }

    private ProductToolResponse product(Long id, String name, String price, Integer stock) {
        return ProductToolResponse.builder()
                .productId(id)
                .productName(name)
                .price(new BigDecimal(price))
                .stock(stock)
                .sales(100)
                .imageUrl("https://example.com/product.jpg")
                .discountInfo("")
                .build();
    }
}
