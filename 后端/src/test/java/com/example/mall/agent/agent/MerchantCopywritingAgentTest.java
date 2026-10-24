package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.CopywritingRequest;
import com.example.mall.agent.dto.agent.CopywritingResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.prompt.MerchantCopywritingPrompt;
import com.example.mall.agent.service.DeepSeekService;
import com.example.mall.agent.tool.ProductTool;
import com.example.mall.agent.utils.CopywritingSafetyValidator;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MerchantCopywritingAgentTest {
    @Mock
    private ProductTool productTool;
    @Mock
    private DeepSeekService deepSeekService;
    @Mock
    private MerchantCopywritingPrompt merchantCopywritingPrompt;

    private MerchantCopywritingAgent agent;

    @BeforeEach
    void setUp() {
        agent = new MerchantCopywritingAgent(
                productTool,
                deepSeekService,
                merchantCopywritingPrompt,
                new CopywritingSafetyValidator(),
                new ObjectMapper()
        );
    }

    @Test
    void productIdsEmptyShouldFail() {
        CopywritingResponse response = agent.generate(request(List.of(), List.of("TITLE")));

        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getResults()).isNotNull().isEmpty();
        assertThat(response.getMessage()).contains("productIds");
    }

    @Test
    void productIdsMoreThanTwentyShouldFail() {
        List<Long> ids = new ArrayList<>();
        for (long i = 1; i <= 21; i++) {
            ids.add(i);
        }

        CopywritingResponse response = agent.generate(request(ids, List.of("TITLE")));

        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).contains("20");
    }

    @Test
    void generateTypesEmptyShouldFail() {
        CopywritingResponse response = agent.generate(request(List.of(1L), List.of()));

        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).contains("generateTypes");
    }

    @Test
    void illegalGenerateTypeShouldFail() {
        CopywritingResponse response = agent.generate(request(List.of(1L), List.of("BAD_TYPE")));

        assertThat(response.getSuccess()).isFalse();
        assertThat(response.getMessage()).contains("非法");
    }

    @Test
    void duplicateProductIdsShouldBeDeduplicated() {
        stubProduct(1L, product(1L, "蓝牙耳机", ""));
        stubAi("""
                {"title":"蓝牙耳机日常通勤推荐","sellingPoints":[],"detail":null,"promotion":null,"shortVideoScript":null,"liveScript":null}
                """);

        CopywritingResponse response = agent.generate(request(List.of(1L, 1L), List.of("TITLE")));

        assertThat(response.getTotal()).isEqualTo(1);
        assertThat(response.getResults()).hasSize(1);
        verify(productTool, times(1)).getProductDetail(1L);
    }

    @Test
    void missingProductShouldFailSingleItemAndContinueBatch() {
        when(productTool.getProductDetail(1L)).thenThrow(new RuntimeException("not found"));
        stubProduct(2L, product(2L, "机械键盘", ""));
        stubAi("""
                {"title":"机械键盘办公使用推荐","sellingPoints":[],"detail":null,"promotion":null,"shortVideoScript":null,"liveScript":null}
                """);

        CopywritingResponse response = agent.generate(request(List.of(1L, 2L), List.of("TITLE")));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getSuccessCount()).isEqualTo(1);
        assertThat(response.getFailureCount()).isEqualTo(1);
        assertThat(response.getResults()).extracting("success").containsExactly(false, true);
    }

    @Test
    void legalJsonShouldGenerateContent() {
        stubProduct(1L, product(1L, "蓝牙耳机", ""));
        stubAi("""
                {"title":"蓝牙耳机日常通勤推荐","sellingPoints":["真实商品信息清晰"],"detail":"详情文案","promotion":null,"shortVideoScript":null,"liveScript":null}
                """);

        CopywritingResponse response = agent.generate(request(List.of(1L), List.of("TITLE", "SELLING_POINTS", "DETAIL")));

        assertThat(response.getResults().get(0).getContent().getTitle()).contains("蓝牙耳机");
        assertThat(response.getResults().get(0).getContent().getDetail()).isEqualTo("详情文案");
        assertThat(response.getResults().get(0).getFallback()).isFalse();
    }

    @Test
    void markdownJsonShouldBeParsed() {
        stubProduct(1L, product(1L, "蓝牙耳机", ""));
        stubAi("""
                ```json
                {"title":"蓝牙耳机学生使用推荐","sellingPoints":[],"detail":null,"promotion":null,"shortVideoScript":null,"liveScript":null}
                ```
                """);

        CopywritingResponse response = agent.generate(request(List.of(1L), List.of("TITLE")));

        assertThat(response.getResults().get(0).getContent().getTitle()).contains("蓝牙耳机");
        assertThat(response.getResults().get(0).getFallback()).isFalse();
    }

    @Test
    void invalidJsonShouldFallbackSingleItem() {
        stubProduct(1L, product(1L, "蓝牙耳机", ""));
        stubAi("not json");

        CopywritingResponse response = agent.generate(request(List.of(1L), List.of("TITLE")));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getResults().get(0).getFallback()).isTrue();
        assertThat(response.getResults().get(0).getContent().getTitle()).contains("蓝牙耳机");
    }

    @Test
    void fakePromotionShouldBeFilteredWhenNoDiscountInfo() {
        stubProduct(1L, product(1L, "蓝牙耳机", ""));
        stubAi("""
                {"title":"蓝牙耳机优惠券折扣推荐","sellingPoints":["赠品多多"],"detail":null,"promotion":"领取优惠券享受折扣赠品","shortVideoScript":null,"liveScript":null}
                """);

        CopywritingResponse response = agent.generate(request(List.of(1L), List.of("TITLE", "SELLING_POINTS", "PROMOTION")));
        String text = response.getResults().get(0).getContent().getTitle()
                + response.getResults().get(0).getContent().getSellingPoints()
                + response.getResults().get(0).getContent().getPromotion();

        assertThat(text).doesNotContain("优惠券", "折扣", "赠品");
    }

    @Test
    void absoluteWordsShouldBeReplaced() {
        stubProduct(1L, product(1L, "蓝牙耳机", ""));
        stubAi("""
                {"title":"全网最低蓝牙耳机","sellingPoints":["全国第一音质"],"detail":null,"promotion":null,"shortVideoScript":null,"liveScript":null}
                """);

        CopywritingResponse response = agent.generate(request(List.of(1L), List.of("TITLE", "SELLING_POINTS")));
        String text = response.getResults().get(0).getContent().getTitle()
                + response.getResults().get(0).getContent().getSellingPoints();

        assertThat(text).doesNotContain("全网最低", "全国第一");
    }

    @Test
    void noDiscountInfoShouldNotContainPromotionWords() {
        stubProduct(1L, product(1L, "蓝牙耳机", ""));
        stubAi("""
                {"title":null,"sellingPoints":[],"detail":null,"promotion":"到手价更划算，优惠券可用","shortVideoScript":null,"liveScript":null}
                """);

        CopywritingResponse response = agent.generate(request(List.of(1L), List.of("PROMOTION")));

        assertThat(response.getResults().get(0).getContent().getPromotion()).doesNotContain("到手价", "优惠券");
    }

    @Test
    void unrequestedFieldsShouldBeNull() {
        stubProduct(1L, product(1L, "蓝牙耳机", ""));
        stubAi("""
                {"title":"蓝牙耳机日常推荐","sellingPoints":["不应返回"],"detail":"不应返回","promotion":"不应返回","shortVideoScript":"不应返回","liveScript":"不应返回"}
                """);

        CopywritingResponse response = agent.generate(request(List.of(1L), List.of("TITLE")));

        assertThat(response.getResults().get(0).getContent().getTitle()).isNotNull();
        assertThat(response.getResults().get(0).getContent().getDetail()).isNull();
        assertThat(response.getResults().get(0).getContent().getSellingPoints()).isNull();
    }

    @Test
    void sellingPointsShouldBeAtMostFive() {
        stubProduct(1L, product(1L, "蓝牙耳机", ""));
        stubAi("""
                {"title":null,"sellingPoints":["1","2","3","4","5","6"],"detail":null,"promotion":null,"shortVideoScript":null,"liveScript":null}
                """);

        CopywritingResponse response = agent.generate(request(List.of(1L), List.of("SELLING_POINTS")));

        assertThat(response.getResults().get(0).getContent().getSellingPoints()).hasSize(5);
    }

    @Test
    void singleProductFailureShouldNotAffectOtherProducts() {
        when(productTool.getProductDetail(1L)).thenThrow(new RuntimeException("not found"));
        stubProduct(2L, product(2L, "台灯", ""));
        stubAi("""
                {"title":"台灯日常使用推荐","sellingPoints":[],"detail":null,"promotion":null,"shortVideoScript":null,"liveScript":null}
                """);

        CopywritingResponse response = agent.generate(request(List.of(1L, 2L), List.of("TITLE")));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getResults()).hasSize(2);
        assertThat(response.getResults().get(0).getSuccess()).isFalse();
        assertThat(response.getResults().get(1).getSuccess()).isTrue();
    }

    @Test
    void allSuccessShouldHaveCorrectSuccessCount() {
        stubProduct(1L, product(1L, "台灯", ""));
        stubProduct(2L, product(2L, "耳机", ""));
        stubAi("""
                {"title":"商品日常使用推荐","sellingPoints":[],"detail":null,"promotion":null,"shortVideoScript":null,"liveScript":null}
                """);

        CopywritingResponse response = agent.generate(request(List.of(1L, 2L), List.of("TITLE")));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getSuccessCount()).isEqualTo(2);
        assertThat(response.getFailureCount()).isZero();
    }

    @Test
    void partialFailureShouldHaveCorrectFailureCount() {
        when(productTool.getProductDetail(1L)).thenThrow(new RuntimeException("not found"));
        stubProduct(2L, product(2L, "耳机", ""));
        stubAi("""
                {"title":"商品日常使用推荐","sellingPoints":[],"detail":null,"promotion":null,"shortVideoScript":null,"liveScript":null}
                """);

        CopywritingResponse response = agent.generate(request(List.of(1L, 2L), List.of("TITLE")));

        assertThat(response.getSuccess()).isTrue();
        assertThat(response.getSuccessCount()).isEqualTo(1);
        assertThat(response.getFailureCount()).isEqualTo(1);
    }

    @Test
    void resultsShouldNeverBeNull() {
        CopywritingResponse response = agent.generate(request(List.of(), List.of("TITLE")));

        assertThat(response.getResults()).isNotNull();
    }

    @Test
    void shouldNotCallDeepSeekWhenRequestInvalid() {
        CopywritingResponse response = agent.generate(request(List.of(), List.of("TITLE")));

        assertThat(response.getSuccess()).isFalse();
        verify(deepSeekService, never()).chat(anyString(), anyString());
    }

    private void stubProduct(Long productId, ProductToolResponse product) {
        when(productTool.getProductDetail(productId)).thenReturn(product);
    }

    private void stubAi(String content) {
        when(merchantCopywritingPrompt.systemPrompt()).thenReturn("merchant prompt");
        when(deepSeekService.chat(anyString(), anyString())).thenReturn(AiChatResponse.ok("deepseek-chat", content));
    }

    private CopywritingRequest request(List<Long> productIds, List<String> generateTypes) {
        CopywritingRequest request = new CopywritingRequest();
        request.setProductIds(productIds);
        request.setStyle("年轻活泼");
        request.setTargetAudience("大学生");
        request.setScene("开学季");
        request.setGenerateTypes(generateTypes);
        request.setExtraRequirement("突出日常实用性");
        return request;
    }

    private ProductToolResponse product(Long id, String name, String discountInfo) {
        return ProductToolResponse.builder()
                .productId(id)
                .productName(name)
                .categoryName("数码科技")
                .price(new BigDecimal("199.00"))
                .stock(20)
                .sales(100)
                .description("真实商品描述")
                .imageUrl("https://example.com/product.jpg")
                .discountInfo(discountInfo)
                .build();
    }
}
