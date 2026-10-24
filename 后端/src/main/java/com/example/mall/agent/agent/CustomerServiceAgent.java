package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.AfterSaleAdviceResult;
import com.example.mall.agent.dto.agent.CustomerServiceRequest;
import com.example.mall.agent.dto.agent.CustomerServiceResponse;
import com.example.mall.agent.dto.agent.ServiceIntentResult;
import com.example.mall.agent.dto.tool.OrderToolResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.exception.AiException;
import com.example.mall.agent.memory.ChatMemoryService;
import com.example.mall.agent.prompt.CustomerServicePrompt;
import com.example.mall.agent.service.DeepSeekService;
import com.example.mall.agent.tool.OrderTool;
import com.example.mall.agent.tool.ProductTool;
import com.example.mall.agent.tool.UserTool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 全天候智能客服 Agent。
 * 订单、商品、优惠券数据必须来自 Tool，DeepSeek 只负责组织客服话术。
 */
@Service
public class CustomerServiceAgent {
    private static final Logger log = LoggerFactory.getLogger(CustomerServiceAgent.class);

    private static final String ORDER_STATUS = "ORDER_STATUS";
    private static final String REFUND_REQUEST = "REFUND_REQUEST";
    private static final String RETURN_REQUEST = "RETURN_REQUEST";
    private static final String EXCHANGE_REQUEST = "EXCHANGE_REQUEST";
    private static final String COUPON_CONSULT = "COUPON_CONSULT";
    private static final String PRE_SALE_CONSULT = "PRE_SALE_CONSULT";
    private static final String UNKNOWN = "UNKNOWN";

    private final OrderTool orderTool;
    private final UserTool userTool;
    private final ProductTool productTool;
    private final DeepSeekService deepSeekService;
    private final ChatMemoryService chatMemoryService;
    private final CustomerServicePrompt customerServicePrompt;

    public CustomerServiceAgent(OrderTool orderTool,
                                UserTool userTool,
                                ProductTool productTool,
                                DeepSeekService deepSeekService,
                                ChatMemoryService chatMemoryService,
                                CustomerServicePrompt customerServicePrompt) {
        this.orderTool = orderTool;
        this.userTool = userTool;
        this.productTool = productTool;
        this.deepSeekService = deepSeekService;
        this.chatMemoryService = chatMemoryService;
        this.customerServicePrompt = customerServicePrompt;
    }

    public CustomerServiceResponse chat(CustomerServiceRequest request) {
        String sessionId = chatMemoryService.normalizeSessionId(request == null ? null : request.getSessionId(), "cs-");
        if (request == null || request.getUserId() == null) {
            return CustomerServiceResponse.fail(sessionId, "userId 不能为空");
        }
        if (!StringUtils.hasText(request.getMessage())) {
            return CustomerServiceResponse.fail(sessionId, "message 不能为空");
        }

        long start = System.currentTimeMillis();
        String message = request.getMessage().trim();
        String context = chatMemoryService.buildUserContext(sessionId, message);
        ServiceIntentResult intent = recognizeIntent(context);

        try {
            CustomerServiceResponse response = switch (intent.getIntent()) {
                case ORDER_STATUS -> handleOrderStatus(request.getUserId(), sessionId, message, intent);
                case REFUND_REQUEST -> handleRefund(request.getUserId(), sessionId, message, intent);
                case RETURN_REQUEST -> handleReturnOrExchange(request.getUserId(), sessionId, message, intent, "退货");
                case EXCHANGE_REQUEST -> handleReturnOrExchange(request.getUserId(), sessionId, message, intent, "换货");
                case COUPON_CONSULT -> handleCoupon(request.getUserId(), sessionId, message, intent);
                case PRE_SALE_CONSULT -> handlePreSale(sessionId, message, intent);
                default -> handleUnknown(sessionId, message, intent);
            };
            log.info("CustomerServiceAgent success, sessionId={}, userId={}, intent={}, elapsedMs={}",
                    sessionId, request.getUserId(), intent.getIntent(), System.currentTimeMillis() - start);
            return response;
        } catch (AiException e) {
            String reply = e.getCode() == 404 ? "未查询到该用户下的订单信息。" : "客服处理失败：" + e.getMessage();
            chatMemoryService.append(sessionId, message, reply);
            log.warn("CustomerServiceAgent handled AiException, sessionId={}, code={}, message={}",
                    sessionId, e.getCode(), e.getMessage());
            return CustomerServiceResponse.builder()
                    .success(true)
                    .sessionId(sessionId)
                    .intent(intent.getIntent())
                    .intentResult(intent)
                    .reply(reply)
                    .build();
        } catch (Exception e) {
            log.error("CustomerServiceAgent failed, sessionId={}, message={}", sessionId, message, e);
            return CustomerServiceResponse.fail(sessionId, "智能客服处理失败，请转人工客服。");
        }
    }

    private CustomerServiceResponse handleOrderStatus(Long userId, String sessionId, String message, ServiceIntentResult intent) {
        OrderToolResponse order = resolveUserOrder(userId, intent);
        String fallback = "订单号：" + order.getOrderNo()
                + "，当前状态：" + order.getStatusText()
                + "，物流信息：" + order.getLogisticsInfo()
                + "。如状态长时间未变化，建议转人工客服确认。";
        String reply = generateAiReply(intent, message, buildOrderData(order), fallback);
        chatMemoryService.append(sessionId, message, reply);
        return CustomerServiceResponse.builder()
                .success(true)
                .sessionId(sessionId)
                .intent(ORDER_STATUS)
                .intentResult(intent)
                .reply(reply)
                .order(order)
                .build();
    }

    private CustomerServiceResponse handleRefund(Long userId, String sessionId, String message, ServiceIntentResult intent) {
        OrderToolResponse order = resolveUserOrder(userId, intent);
        Boolean refundable = orderTool.checkRefundAvailable(order.getOrderId());
        AfterSaleAdviceResult advice = buildAfterSaleAdvice(order, refundable, "退款");
        String reply = generateAiReply(intent, message, buildAfterSaleData(order, advice), advice.getAdvice());
        chatMemoryService.append(sessionId, message, reply);
        return CustomerServiceResponse.builder()
                .success(true)
                .sessionId(sessionId)
                .intent(REFUND_REQUEST)
                .intentResult(intent)
                .reply(reply)
                .order(order)
                .afterSaleAdvice(advice)
                .build();
    }

    private CustomerServiceResponse handleReturnOrExchange(Long userId,
                                                           String sessionId,
                                                           String message,
                                                           ServiceIntentResult intent,
                                                           String actionName) {
        OrderToolResponse order = resolveUserOrder(userId, intent);
        boolean actionable = isAfterSaleCandidate(order);
        AfterSaleAdviceResult advice = buildAfterSaleAdvice(order, actionable, actionName);
        String reply = generateAiReply(intent, message, buildAfterSaleData(order, advice), advice.getAdvice());
        chatMemoryService.append(sessionId, message, reply);
        return CustomerServiceResponse.builder()
                .success(true)
                .sessionId(sessionId)
                .intent(intent.getIntent())
                .intentResult(intent)
                .reply(reply)
                .order(order)
                .afterSaleAdvice(advice)
                .build();
    }

    private CustomerServiceResponse handleCoupon(Long userId, String sessionId, String message, ServiceIntentResult intent) {
        List<String> coupons = userTool.getUserCouponList(userId);
        String fallback = coupons.isEmpty() ? "暂无可用优惠券。" : "当前可用优惠券：" + String.join("、", coupons);
        String reply = generateAiReply(intent, message, "优惠券列表：" + coupons, fallback);
        chatMemoryService.append(sessionId, message, reply);
        return CustomerServiceResponse.builder()
                .success(true)
                .sessionId(sessionId)
                .intent(COUPON_CONSULT)
                .intentResult(intent)
                .reply(reply)
                .coupons(coupons)
                .build();
    }

    private CustomerServiceResponse handlePreSale(String sessionId, String message, ServiceIntentResult intent) {
        List<ProductToolResponse> products = productTool.searchProducts(intent.getKeyword(), null, null, null)
                .stream()
                .limit(5)
                .toList();
        String fallback = products.isEmpty() ? "暂未查询到匹配商品，请提供更明确的商品名称或品类。" : buildProductFallback(products);
        String reply = generateAiReply(intent, message, buildProductData(products), fallback);
        chatMemoryService.append(sessionId, message, reply);
        return CustomerServiceResponse.builder()
                .success(true)
                .sessionId(sessionId)
                .intent(PRE_SALE_CONSULT)
                .intentResult(intent)
                .reply(reply)
                .products(products)
                .build();
    }

    private CustomerServiceResponse handleUnknown(String sessionId, String message, ServiceIntentResult intent) {
        String reply = "该问题需要人工客服进一步确认。你也可以补充订单号、商品名称，或说明是查单、退款、退货、换货、优惠券还是售前咨询。";
        chatMemoryService.append(sessionId, message, reply);
        return CustomerServiceResponse.builder()
                .success(true)
                .sessionId(sessionId)
                .intent(UNKNOWN)
                .intentResult(intent)
                .reply(reply)
                .build();
    }

    /**
     * 订单权限校验：先从 OrderTool 查询当前用户订单，再按订单号或订单ID过滤。
     * 找不到即视为无权限或订单不存在，统一返回“未查询到该用户下的订单信息”。
     */
    private OrderToolResponse resolveUserOrder(Long userId, ServiceIntentResult intent) {
        if (StringUtils.hasText(intent.getOrderNo())) {
            return orderTool.getUserOrderDetail(userId, intent.getOrderNo());
        }
        List<OrderToolResponse> orders = orderTool.getUserOrders(userId, 50);
        if (intent.getOrderId() != null) {
            return orders.stream()
                    .filter(order -> intent.getOrderId().equals(order.getOrderId()))
                    .findFirst()
                    .orElseThrow(() -> new AiException(404, "未查询到该用户下的订单信息。"));
        }
        return orders.stream()
                .findFirst()
                .orElseThrow(() -> new AiException(404, "未查询到该用户下的订单信息。"));
    }

    private ServiceIntentResult recognizeIntent(String context) {
        String text = context == null ? "" : context;
        String orderNo = extractOrderNo(text);
        Long orderId = extractOrderId(text);
        String keyword = extractProductKeyword(text);

        if (containsAny(text, "优惠券", "优惠劵", "券", "满减", "红包")) {
            return intent(COUPON_CONSULT, orderNo, orderId, keyword, false, false, "优惠券咨询");
        }
        if (containsAny(text, "退款", "退钱", "退差价")) {
            return intent(REFUND_REQUEST, orderNo, orderId, keyword, false, false, "退款咨询");
        }
        if (containsAny(text, "换货", "换一个", "换新")) {
            return intent(EXCHANGE_REQUEST, orderNo, orderId, keyword, false, true, "换货咨询");
        }
        if (containsAny(text, "退货", "退掉", "不想要了")) {
            return intent(RETURN_REQUEST, orderNo, orderId, keyword, false, true, "退货咨询");
        }
        if (containsAny(text, "订单", "单号", "查单", "发货", "物流", "快递", "到哪", "状态", "没收到")) {
            return intent(ORDER_STATUS, orderNo, orderId, keyword, false, false, "订单状态/物流查询");
        }
        if (StringUtils.hasText(keyword) || containsAny(text, "支持", "参数", "适合", "有没有", "多少钱", "推荐", "能不能")) {
            return intent(PRE_SALE_CONSULT, orderNo, orderId, keyword, false, false, "售前商品咨询");
        }
        return intent(UNKNOWN, orderNo, orderId, keyword, false, true, "未知意图");
    }

    private ServiceIntentResult intent(String intent,
                                       String orderNo,
                                       Long orderId,
                                       String keyword,
                                       boolean useReasoner,
                                       boolean needHuman,
                                       String description) {
        return ServiceIntentResult.builder()
                .intent(intent)
                .orderNo(orderNo)
                .orderId(orderId)
                .keyword(keyword)
                .useReasoner(useReasoner)
                .needHuman(needHuman)
                .description(description)
                .build();
    }

    private String generateAiReply(ServiceIntentResult intent, String message, String data, String fallback) {
        try {
            String prompt = "用户问题：" + message + "\n"
                    + "识别意图：" + intent.getIntent() + "\n"
                    + "系统真实数据：" + data + "\n"
                    + "请只基于系统真实数据回答；如果数据不足，提示转人工客服。";
            AiChatResponse aiResponse = Boolean.TRUE.equals(intent.getUseReasoner())
                    ? deepSeekService.reason(customerServicePrompt.systemPrompt(), prompt)
                    : deepSeekService.chat(customerServicePrompt.systemPrompt(), prompt);
            if (Boolean.TRUE.equals(aiResponse.getSuccess()) && StringUtils.hasText(aiResponse.getContent())) {
                return aiResponse.getContent();
            }
            return fallback;
        } catch (Exception e) {
            log.warn("CustomerServiceAgent AI fallback, intent={}, error={}", intent.getIntent(), e.getMessage());
            return fallback;
        }
    }

    private AfterSaleAdviceResult buildAfterSaleAdvice(OrderToolResponse order, Boolean actionable, String actionName) {
        boolean canOperate = Boolean.TRUE.equals(actionable);
        String advice;
        if (canOperate) {
            advice = "订单当前状态为 " + order.getStatusText() + "，可提交" + actionName + "申请，具体处理结果需要人工客服或后台审核确认。";
        } else if (statusContains(order.getStatus(), "待付款")) {
            advice = "订单尚未支付，建议直接取消订单，无需发起" + actionName + "。";
        } else if (statusContains(order.getStatus(), "已取消")) {
            advice = "订单已取消，不能再次发起" + actionName + "。";
        } else {
            advice = "订单当前状态为 " + order.getStatusText() + "，是否支持" + actionName + "需要人工客服进一步确认。";
        }
        return AfterSaleAdviceResult.builder()
                .orderId(order.getOrderId())
                .orderNo(order.getOrderNo())
                .status(order.getStatusText())
                .refundAvailable(canOperate)
                .actionable(canOperate)
                .needHuman(!canOperate)
                .advice(advice)
                .build();
    }

    private boolean isAfterSaleCandidate(OrderToolResponse order) {
        return statusContains(order.getStatus(), "待发货", "待收货", "已完成");
    }

    private boolean statusContains(String status, String... expected) {
        if (!StringUtils.hasText(status)) {
            return false;
        }
        for (String item : expected) {
            if (status.contains(item)) {
                return true;
            }
        }
        return false;
    }

    private String buildOrderData(OrderToolResponse order) {
        return "订单ID=" + order.getOrderId()
                + "，订单号=" + order.getOrderNo()
                + "，用户ID=" + order.getUserId()
                + "，状态=" + order.getStatusText()
                + "，金额=" + order.getTotalAmount()
                + "，下单时间=" + order.getCreateTime()
                + "，支付时间=" + order.getPayTime()
                + "，物流=" + order.getLogisticsInfo()
                + "，明细=" + order.getItems();
    }

    private String buildAfterSaleData(OrderToolResponse order, AfterSaleAdviceResult advice) {
        return buildOrderData(order)
                + "，售后建议=" + advice.getAdvice()
                + "，是否可操作=" + advice.getActionable();
    }

    private String buildProductData(List<ProductToolResponse> products) {
        if (products == null || products.isEmpty()) {
            return "商品查询结果为空。";
        }
        StringBuilder builder = new StringBuilder();
        for (ProductToolResponse product : products) {
            builder.append("商品ID=").append(product.getProductId())
                    .append("，名称=").append(product.getProductName())
                    .append("，分类=").append(product.getCategoryName())
                    .append("，价格=").append(product.getPrice())
                    .append("，库存=").append(product.getStock())
                    .append("，销量=").append(product.getSales())
                    .append("，描述=").append(product.getDescription())
                    .append('\n');
        }
        return builder.toString();
    }

    private String buildProductFallback(List<ProductToolResponse> products) {
        StringBuilder builder = new StringBuilder("查询到以下相关商品：");
        for (ProductToolResponse product : products) {
            builder.append("\n")
                    .append(product.getProductName())
                    .append("，价格：").append(product.getPrice())
                    .append("，库存：").append(product.getStock())
                    .append("。");
        }
        return builder.toString();
    }

    private String extractOrderNo(String text) {
        Matcher namedMatcher = Pattern.compile("(?:订单号|单号)[:：\\s]*([A-Za-z0-9_-]{6,32})").matcher(text);
        if (namedMatcher.find()) {
            return namedMatcher.group(1);
        }
        Matcher digitMatcher = Pattern.compile("(?<!\\d)(\\d{10,32})(?!\\d)").matcher(text);
        if (digitMatcher.find()) {
            return digitMatcher.group(1);
        }
        return null;
    }

    private Long extractOrderId(String text) {
        Matcher matcher = Pattern.compile("订单ID[:：\\s]*(\\d{1,9})").matcher(text);
        if (matcher.find()) {
            return Long.valueOf(matcher.group(1));
        }
        return null;
    }

    private String extractProductKeyword(String text) {
        String[] keywords = {
                "无线耳机", "蓝牙耳机", "耳机", "机械键盘", "键盘", "显示器", "智能手表", "手机支架",
                "运动鞋", "卫衣", "双肩包", "香水", "护肤套装", "咖啡", "零食礼盒", "保温杯",
                "台灯", "收纳盒", "瑜伽垫", "篮球", "图书", "文具", "母婴", "宠物玩具", "礼品卡"
        };
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return keyword;
            }
        }
        return null;
    }

    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }
}
