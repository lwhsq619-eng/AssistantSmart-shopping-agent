package com.example.mall.agent.agent;

import com.example.mall.agent.dto.AiChatResponse;
import com.example.mall.agent.dto.agent.AfterSaleAdvice;
import com.example.mall.agent.dto.agent.AfterSaleClassifyResult;
import com.example.mall.agent.dto.agent.AfterSaleTicketRequest;
import com.example.mall.agent.dto.agent.AfterSaleTicketResponse;
import com.example.mall.agent.dto.tool.OrderItemToolResponse;
import com.example.mall.agent.dto.tool.OrderToolResponse;
import com.example.mall.agent.dto.tool.ProductToolResponse;
import com.example.mall.agent.dto.tool.UserToolResponse;
import com.example.mall.agent.prompt.AfterSaleTicketPrompt;
import com.example.mall.agent.service.AiService;
import com.example.mall.agent.tool.OrderTool;
import com.example.mall.agent.tool.ProductTool;
import com.example.mall.agent.tool.UserTool;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * 售后工单智能分拣 Agent
 * 业务定位：电商售后垂直AI智能体，自动分析用户售后诉求，完成工单分类、分级、生成处理建议
 * 核心约束：仅做分拣分析，不自动审批订单、不修改订单数据、不创建真实售后工单，所有流程强制人工复核
 */
@Service
public class AfterSaleTicketAgent {
    // 日志对象，记录运行日志、异常、耗时，用于线上排查问题
    private static final Logger log = LoggerFactory.getLogger(AfterSaleTicketAgent.class);

    // 售后工单全部合法类型枚举集合，限制AI输出只能从该集合取值
    private static final Set<String> TICKET_TYPES = Set.of(
            "ONLY_REFUND",        // 仅退款（不退货）
            "RETURN_REFUND",      // 退货退款
            "EXCHANGE",           // 换货
            "DAMAGE_RESEND",      // 商品破损补发
            "QUALITY_COMPLAINT",  // 质量问题投诉
            "LOGISTICS_PROBLEM",  // 物流快递问题
            "RIGHTS_COMPLAINT",   // 维权/平台投诉
            "OTHER"               // 其他未知类型
    );

    // 工单优先级合法集合：低、中、高、紧急
    private static final Set<String> PRIORITIES = Set.of("LOW", "MEDIUM", "HIGH", "URGENT");
    // 工单风险等级合法集合：低、中、高风险
    private static final Set<String> RISK_LEVELS = Set.of("LOW", "MEDIUM", "HIGH");

    // 订单工具：查询订单详情、校验订单退款资格
    private final OrderTool orderTool;
    // 用户工具：查询用户基础信息
    private final UserTool userTool;
    // 商品工具：根据商品ID查询商品详情
    private final ProductTool productTool;
    // AI大模型服务：封装DeepSeek兼容API，提供普通对话/深度推理两种调用方式
    private final AiService aiService;
    // 售后专用提示词管理类，存储固定系统prompt
    private final AfterSaleTicketPrompt afterSaleTicketPrompt;
    // JSON序列化工具，解析大模型返回的JSON文本
    private final ObjectMapper objectMapper;

    /**
     * 构造函数，Spring自动注入所有依赖工具/服务
     */
    public AfterSaleTicketAgent(OrderTool orderTool,
                                UserTool userTool,
                                ProductTool productTool,
                                AiService aiService,
                                AfterSaleTicketPrompt afterSaleTicketPrompt,
                                ObjectMapper objectMapper) {
        this.orderTool = orderTool;
        this.userTool = userTool;
        this.productTool = productTool;
        this.aiService = aiService;
        this.afterSaleTicketPrompt = afterSaleTicketPrompt;
        this.objectMapper = objectMapper;
    }

    /**
     * 对外暴露的唯一入口方法：售后工单分拣主流程
     * @param request 前端传入售后工单请求参数（用户ID、订单ID、用户诉求、上传图片）
     * @return 标准化分拣结果响应（工单类型、优先级、风险、处理建议/失败提示）
     */
    public AfterSaleTicketResponse classify(AfterSaleTicketRequest request) {
        // 1. 校验入参完整性，参数缺失直接返回失败
        String validationError = validateRequest(request);
        if (validationError != null) {
            return AfterSaleTicketResponse.fail(validationError);
        }

        // 记录方法开始时间，用于统计接口耗时
        long start = System.currentTimeMillis();
        try {
            // 查询订单完整信息
            OrderToolResponse order = orderTool.getOrderDetail(request.getOrderId());
            // 越权拦截：校验订单归属人，非订单本人禁止操作
            if (order.getUserId() == null || !order.getUserId().equals(request.getUserId())) {
                return AfterSaleTicketResponse.fail("订单不属于当前用户，禁止越权查询。");
            }
            // 查询该订单是否支持退款
            Boolean refundAvailable = orderTool.checkRefundAvailable(request.getOrderId());
            // 查询提交工单的用户信息
            UserToolResponse user = userTool.getUserProfile(request.getUserId());
            // 根据订单商品ID批量拉取商品详情，拼接上下文给AI
            List<ProductToolResponse> products = loadOrderProducts(order);

            // 核心逻辑：优先调用AI分析，AI异常则降级本地关键词规则兜底
            AfterSaleClassifyResult result = callAiOrFallback(request, order, refundAvailable, user, products);
            // 清洗、校验、修正AI返回的结果，强制符合业务规则
            result = sanitizeResult(result, request.getMessage(), order, refundAvailable);

            // 打印成功日志：用户ID、订单ID、工单类型、接口耗时
            log.info("AfterSaleTicketAgent success, userId={}, orderId={}, ticketType={}, elapsedMs={}",
                    request.getUserId(), request.getOrderId(), result.getTicketType(), System.currentTimeMillis() - start);
            // 封装成功响应返回
            return AfterSaleTicketResponse.builder()
                    .success(true)
                    .ticketType(result.getTicketType())
                    .priority(result.getPriority())
                    .riskLevel(result.getRiskLevel())
                    .reason(result.getReason())
                    .advice(result.getAdvice())
                    .build();
        } catch (Exception e) {
            // 全局异常捕获，记录错误堆栈，返回统一失败信息
            log.error("AfterSaleTicketAgent failed, userId={}, orderId={}", request.getUserId(), request.getOrderId(), e);
            return AfterSaleTicketResponse.fail("售后工单分拣失败：" + e.getMessage());
        }
    }

    /**
     * AI调用容错方法：优先大模型语义分析，AI报错/无返回则使用本地关键词兜底规则
     * @param request 用户工单请求
     * @param order 订单数据
     * @param refundAvailable 订单是否可退款
     * @param user 用户信息
     * @param products 订单商品列表
     * @return 工单分拣分类结果
     */
    private AfterSaleClassifyResult callAiOrFallback(AfterSaleTicketRequest request,
                                                     OrderToolResponse order,
                                                     Boolean refundAvailable,
                                                     UserToolResponse user,
                                                     List<ProductToolResponse> products) {
        // 提前生成兜底结果，AI失效时直接使用
        AfterSaleClassifyResult fallback = fallbackClassify(request.getMessage(), order, refundAvailable);
        try {
            // 拼接完整上下文Prompt（用户诉求+订单+商品+业务约束）
            String prompt = buildPrompt(request, order, refundAvailable, user, products);
            // 判断是否高风险诉求，高风险启用深度推理模型，普通诉求使用轻量化对话模型
            AiChatResponse aiResponse = shouldUseReasoner(request.getMessage())
                    ? aiService.reason(afterSaleTicketPrompt.systemPrompt(), prompt)
                    : aiService.chat(afterSaleTicketPrompt.systemPrompt(), prompt);
            // AI调用失败/返回空内容，直接返回兜底结果
            if (!Boolean.TRUE.equals(aiResponse.getSuccess()) || !StringUtils.hasText(aiResponse.getContent())) {
                return fallback;
            }
            // 解析AI返回的JSON文本，转为业务实体
            return parseAiResult(aiResponse.getContent(), fallback);
        } catch (Exception e) {
            // AI接口异常，打印警告日志，降级本地规则
            log.warn("AfterSaleTicketAgent AI fallback, orderId={}, error={}", request.getOrderId(), e.getMessage());
            return fallback;
        }
    }

    /**
     * 构建发给大模型的完整提示词Prompt
     * 包含输出格式强制约束、全量业务上下文、平台风控限制
     */
    private String buildPrompt(AfterSaleTicketRequest request,
                               OrderToolResponse order,
                               Boolean refundAvailable,
                               UserToolResponse user,
                               List<ProductToolResponse> products) {
        return "请只返回 JSON，格式：{\"ticketType\":\"EXCHANGE\",\"priority\":\"HIGH\",\"riskLevel\":\"MEDIUM\","
                + "\"reason\":\"\",\"advice\":{\"handlerRole\":\"\",\"suggestedAction\":\"\",\"needManualReview\":true}}\n"
                + "用户描述：" + request.getMessage() + "\n"
                + "图片数量：" + (request.getImages() == null ? 0 : request.getImages().size()) + "\n"
                + "订单信息：" + order + "\n"
                + "是否可退款：" + refundAvailable + "\n"
                + "用户信息：" + user + "\n"
                + "订单商品真实数据：" + products + "\n"
                + "要求：不能自动审批；不能承诺赔付、退款到账、补发结果；需要人工时明确 needManualReview=true。";
    }

    /**
     * 解析大模型返回的文本，提取JSON并映射为分类结果实体
     * @param content AI返回原始文本（可能带markdown代码块）
     * @param fallback AI解析失败时使用的兜底数据
     * @return 标准化分类结果
     * @throws Exception JSON解析异常，上层捕获后自动降级兜底
     */
    private AfterSaleClassifyResult parseAiResult(String content, AfterSaleClassifyResult fallback) throws Exception {
        // 提取纯JSON字符串，去除```json标记
        JsonNode root = objectMapper.readTree(extractJson(content));
        // 读取处理建议节点
        JsonNode adviceNode = root.get("advice");
        AfterSaleAdvice advice = adviceNode == null || adviceNode.isNull()
                ? fallback.getAdvice()
                // 读取建议字段，缺失则使用兜底默认值
                : AfterSaleAdvice.builder()
                .handlerRole(textOrDefault(adviceNode, "handlerRole", fallback.getAdvice().getHandlerRole()))
                .suggestedAction(textOrDefault(adviceNode, "suggestedAction", fallback.getAdvice().getSuggestedAction()))
                .needManualReview(!adviceNode.hasNonNull("needManualReview") || adviceNode.get("needManualReview").asBoolean(true))
                .build();
        // 组装完整分类结果，字段缺失自动填充兜底数据
        return AfterSaleClassifyResult.builder()
                .ticketType(textOrDefault(root, "ticketType", fallback.getTicketType()))
                .priority(textOrDefault(root, "priority", fallback.getPriority()))
                .riskLevel(textOrDefault(root, "riskLevel", fallback.getRiskLevel()))
                .reason(textOrDefault(root, "reason", fallback.getReason()))
                .advice(advice)
                .build();
    }

    /**
     * 结果清洗校验：强制修正AI不规范输出，过滤违规承诺话术、校验字段合法性
     * @param result AI原始输出结果
     * @param message 用户原始售后描述
     * @param order 订单数据
     * @param refundAvailable 订单退款权限
     * @return 合规、标准化后的分拣结果
     */
    private AfterSaleClassifyResult sanitizeResult(AfterSaleClassifyResult result,
                                                   String message,
                                                   OrderToolResponse order,
                                                   Boolean refundAvailable) {
        // 校验工单类型，不在合法集合则使用关键词兜底分类
        String ticketType = TICKET_TYPES.contains(safe(result.getTicketType())) ? result.getTicketType() : fallbackTicketType(message);
        // 校验优先级，非法值替换为该工单类型默认优先级
        String priority = PRIORITIES.contains(safe(result.getPriority())) ? result.getPriority() : defaultPriority(ticketType);
        // 校验风险等级，非法值替换为默认风险等级
        String riskLevel = RISK_LEVELS.contains(safe(result.getRiskLevel())) ? result.getRiskLevel() : defaultRisk(ticketType);
        // 处理建议为空则生成默认处理方案
        AfterSaleAdvice advice = result.getAdvice() == null ? defaultAdvice(ticketType, order, refundAvailable) : result.getAdvice();
        // 判断是否强制人工复核：除OTHER类型全部强制人工审核
        boolean manualReview = Boolean.TRUE.equals(advice.getNeedManualReview()) || requiresManualReview(ticketType);

        return AfterSaleClassifyResult.builder()
                .ticketType(ticketType)
                .priority(priority)
                .riskLevel(riskLevel)
                // 清洗问题原因文本，空值填充兜底描述
                .reason(StringUtils.hasText(result.getReason()) ? sanitizeText(result.getReason()) : fallbackReason(ticketType, message))
                .advice(AfterSaleAdvice.builder()
                        // 处理角色为空默认售后管理员
                        .handlerRole(StringUtils.hasText(advice.getHandlerRole()) ? advice.getHandlerRole() : "售后管理员")
                        // 清洗操作建议话术，替换违规承诺词汇
                        .suggestedAction(sanitizeText(StringUtils.hasText(advice.getSuggestedAction())
                                ? advice.getSuggestedAction()
                                : defaultAdvice(ticketType, order, refundAvailable).getSuggestedAction()))
                        .needManualReview(manualReview)
                        .build())
                .build();
    }

    /**
     * 本地兜底分类总方法：纯关键词匹配生成完整分拣结果（AI服务不可用时降级）
     */
    private AfterSaleClassifyResult fallbackClassify(String message, OrderToolResponse order, Boolean refundAvailable) {
        // 根据用户文本关键词匹配工单类型
        String ticketType = fallbackTicketType(message);
        return AfterSaleClassifyResult.builder()
                .ticketType(ticketType)
                .priority(defaultPriority(ticketType))
                .riskLevel(defaultRisk(ticketType))
                .reason(fallbackReason(ticketType, message))
                .advice(defaultAdvice(ticketType, order, refundAvailable))
                .build();
    }

    /**
     * 关键词匹配工单类型核心方法，无AI时靠文本关键字自动分类售后类型
     */
    private String fallbackTicketType(String message) {
        String text = safe(message);
        // 维权投诉关键词
        if (containsAny(text, "投诉", "维权", "12315", "平台介入")) {
            return "RIGHTS_COMPLAINT";
        }
        // 物流问题关键词
        if (containsAny(text, "物流", "快递", "没收到", "一直不动", "丢了")) {
            return "LOGISTICS_PROBLEM";
        }
        // 质量问题关键词
        if (containsAny(text, "质量", "假货", "不能用", "质量差")) {
            return "QUALITY_COMPLAINT";
        }
        // 破损场景区分补发/换货
        if (containsAny(text, "破损", "包装破", "漏液", "碎了", "摔坏", "划痕")) {
            if (containsAny(text, "换一个", "换货")) {
                return "EXCHANGE";
            }
            return "DAMAGE_RESEND";
        }
        // 换货关键词
        if (containsAny(text, "换货", "换一个", "尺码不合适换")) {
            return "EXCHANGE";
        }
        // 退货退款关键词
        if (containsAny(text, "退货退款", "寄回", "退货")) {
            return "RETURN_REFUND";
        }
        // 仅退款关键词
        if (containsAny(text, "退款", "不想要了", "没发货", "不退货")) {
            return "ONLY_REFUND";
        }
        // 无匹配关键字
        return "OTHER";
    }

    /**
     * 根据工单类型返回标准化人工处理方案：处理人角色、操作步骤、强制人工复核标记
     */
    private AfterSaleAdvice defaultAdvice(String ticketType, OrderToolResponse order, Boolean refundAvailable) {
        return switch (ticketType) {
            case "ONLY_REFUND" -> AfterSaleAdvice.builder()
                    .handlerRole("售后管理员")
                    .suggestedAction(Boolean.TRUE.equals(refundAvailable)
                            ? "检查订单支付和发货状态，未发货可进入退款审核，已发货需人工确认。"
                            : "当前订单状态不满足自动判断条件，需人工核实后处理。")
                    .needManualReview(true)
                    .build();
            case "RETURN_REFUND" -> AfterSaleAdvice.builder()
                    .handlerRole("售后管理员")
                    .suggestedAction("检查订单是否签收，退货地址和商品完整性需人工确认。")
                    .needManualReview(true)
                    .build();
            case "EXCHANGE" -> AfterSaleAdvice.builder()
                    .handlerRole("售后管理员")
                    .suggestedAction("核实订单状态、商品库存和换货凭证后进入换货流程。")
                    .needManualReview(true)
                    .build();
            case "DAMAGE_RESEND" -> AfterSaleAdvice.builder()
                    .handlerRole("售后管理员")
                    .suggestedAction("要求上传破损凭证，核实物流签收和商品状态后人工确认是否补发。")
                    .needManualReview(true)
                    .build();
            case "QUALITY_COMPLAINT" -> AfterSaleAdvice.builder()
                    .handlerRole("售后主管")
                    .suggestedAction("需要售后人员判断质量问题，必要时升级人工复核。")
                    .needManualReview(true)
                    .build();
            case "LOGISTICS_PROBLEM" -> AfterSaleAdvice.builder()
                    .handlerRole("物流客服")
                    .suggestedAction("当前无物流接口，建议人工核实物流状态并同步用户。")
                    .needManualReview(true)
                    .build();
            case "RIGHTS_COMPLAINT" -> AfterSaleAdvice.builder()
                    .handlerRole("售后主管")
                    .suggestedAction("维权投诉需优先人工介入，核实订单、沟通记录和用户诉求后处理。")
                    .needManualReview(true)
                    .build();
            default -> AfterSaleAdvice.builder()
                    .handlerRole("售后管理员")
                    .suggestedAction("问题类型不明确，建议人工查看订单和用户描述后分配处理。")
                    .needManualReview(true)
                    .build();
        };
    }

    /**
     * 根据工单类型匹配默认紧急优先级
     */
    private String defaultPriority(String ticketType) {
        return switch (ticketType) {
            case "RIGHTS_COMPLAINT" -> "URGENT";          // 维权投诉 紧急
            case "QUALITY_COMPLAINT", "DAMAGE_RESEND", "EXCHANGE" -> "HIGH"; // 质量/破损/换货 高优先级
            case "LOGISTICS_PROBLEM", "RETURN_REFUND", "ONLY_REFUND" -> "MEDIUM"; // 物流/退款 中等
            default -> "LOW"; // 其他 低优先级
        };
    }

    /**
     * 根据工单类型匹配默认风险等级
     */
    private String defaultRisk(String ticketType) {
        return switch (ticketType) {
            case "RIGHTS_COMPLAINT", "QUALITY_COMPLAINT" -> "HIGH"; // 维权、质量 高风险
            case "DAMAGE_RESEND", "EXCHANGE", "RETURN_REFUND" -> "MEDIUM"; // 破损、换货、退货 中风险
            default -> "LOW"; // 仅退款、物流、其他 低风险
        };
    }

    /**
     * 兜底模式下，自动生成工单问题原因描述
     */
    private String fallbackReason(String ticketType, String message) {
        return switch (ticketType) {
            case "ONLY_REFUND" -> "用户描述中包含退款、未发货或不想要等仅退款相关表达。";
            case "RETURN_REFUND" -> "用户描述中包含退货退款、寄回或退货相关表达。";
            case "EXCHANGE" -> "用户描述中包含换货、换一个或规格不合适相关表达。";
            case "DAMAGE_RESEND" -> "用户描述中包含破损、包装破、漏液、碎了等破损相关表达。";
            case "QUALITY_COMPLAINT" -> "用户描述中包含质量差、不能用或假货等质量问题表达。";
            case "LOGISTICS_PROBLEM" -> "用户描述中包含物流、快递、没收到或一直不动等物流问题表达。";
            case "RIGHTS_COMPLAINT" -> "用户描述中包含投诉、维权或平台介入等高风险表达。";
            default -> "用户描述暂无法明确归类：" + safe(message);
        };
    }

    /**
     * 判断当前工单是否必须人工复核
     * @return true=强制人工，false=无需人工（仅OTHER类型返回false）
     */
    private boolean requiresManualReview(String ticketType) {
        return !"OTHER".equals(ticketType);
    }

    /**
     * 判断用户诉求是否属于高风险内容，高风险启用深度推理AI模型提升精度
     */
    private boolean shouldUseReasoner(String message) {
        return containsAny(safe(message), "投诉", "维权", "12315", "平台介入", "质量", "假货", "赔偿", "责任");
    }

    /**
     * 根据订单内商品ID，批量查询商品详情，拼接商品数据给AI做分析上下文
     * 查询失败的商品跳过，打印警告日志不阻断整体流程
     */
    private List<ProductToolResponse> loadOrderProducts(OrderToolResponse order) {
        List<ProductToolResponse> products = new ArrayList<>();
        // 订单无商品直接返回空列表
        if (order.getItems() == null) {
            return products;
        }
        // 遍历订单明细
        for (OrderItemToolResponse item : order.getItems()) {
            if (item.getProductId() == null) {
                continue;
            }
            try {
                // 根据商品ID查询商品完整信息
                products.add(productTool.getProductDetail(item.getProductId()));
            } catch (Exception e) {
                // 单个商品查询失败仅打印警告，不中断整个流程
                log.warn("AfterSaleTicketAgent product skipped, productId={}, error={}", item.getProductId(), e.getMessage());
            }
        }
        return products;
    }

    /**
     * 入参校验：校验工单请求必填字段是否存在
     * @return 校验失败返回错误文本，校验通过返回null
     */
    private String validateRequest(AfterSaleTicketRequest request) {
        if (request == null || request.getUserId() == null) {
            return "userId 不能为空。";
        }
        if (request.getOrderId() == null) {
            return "orderId 不能为空。";
        }
        if (!StringUtils.hasText(request.getMessage())) {
            return "message 不能为空。";
        }
        // 全部参数合法，无错误
        return null;
    }

    /**
     * 文本安全过滤：替换AI输出中容易引发客诉的承诺类话术，规避平台纠纷
     */
    private String sanitizeText(String text) {
        if (!StringUtils.hasText(text)) {
            return text;
        }
        return text.replace("自动退款", "进入退款审核")
                .replace("直接退款", "建议退款审核")
                .replace("马上到账", "处理结果以审核为准")
                .replace("一定补发", "人工确认是否补发")
                .replace("赔付", "协助处理");
    }

    /**
     * 从AI返回的混合文本中提取纯JSON字符串，去除markdown ```json 代码块标记
     */
    private String extractJson(String content) {
        String text = content.trim();
        // 移除代码块标记
        if (text.startsWith("```")) {
            text = text.replaceFirst("^```json", "").replaceFirst("^```", "").replaceFirst("```$", "").trim();
        }
        // 截取第一个{到最后一个}之间的JSON内容
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        return text;
    }

    /**
     * JSON节点取值工具：字段为空/缺失时返回默认值，避免空指针
     * @param node JSON节点对象
     * @param field 需要读取的字段名
     * @param defaultValue 字段缺失时使用的默认值
     * @return 字段值 / 默认值
     */
    private String textOrDefault(JsonNode node, String field, String defaultValue) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull() || !StringUtils.hasText(value.asText())) {
            return defaultValue;
        }
        return value.asText();
    }

    /**
     * 多关键词匹配工具：判断文本是否包含任意一个传入关键字
     */
    private boolean containsAny(String text, String... words) {
        for (String word : words) {
            if (text.contains(word)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 空值兼容工具：null字符串转为空串，防止空指针异常
     */
    private String safe(String value) {
        return value == null ? "" : value;
    }
}