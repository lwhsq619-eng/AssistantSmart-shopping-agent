# 网上商城购物系统 AI Agent 模块说明

## 1. 模块定位

本模块在原有网上商城购物系统基础上，以新增代码方式接入 DeepSeek API，形成覆盖用户端、商家端、仓储端和通用治理端的电商 AI Agent 系统。

核心原则：

- 不删除原有 Controller、Service、Mapper。
- 不在 Controller 中直接调用 DeepSeek。
- AI 查询商城数据必须经过 Tool，Tool 再调用已有业务 Service。
- Prompt 统一放在 `后端/src/main/resources/prompt/`。
- API Key 只通过环境变量 `DEEPSEEK_API_KEY` 注入。

## 2. 技术栈

| 模块 | 技术 |
| --- | --- |
| AI 模型 | DeepSeek API |
| 普通聊天模型 | `deepseek-chat` |
| 复杂推理模型 | `deepseek-reasoner` |
| HTTP 调用 | Java HttpClient / Jackson |
| 业务工具 | ProductTool、OrderTool、UserTool、CommentTool、InventoryTool |
| RAG | 本地文档切片、关键词检索、JSON 持久化 |
| 多轮会话 | 内存 Map，按 sessionId 保存最近上下文 |

## 3. Agent 功能清单

| Agent | 接口 | 说明 |
| --- | --- | --- |
| DeepSeekService | `/api/ai/chat` | DeepSeek 最小聊天接口 |
| ShoppingGuideAgent | `/api/ai/guide/chat` | AI 智能导购 |
| CustomerServiceAgent | `/api/ai/customer-service/chat` | 查单、售后、优惠券咨询 |
| SemanticSearchAgent | `/api/ai/search/semantic` | 自然语言商品搜索 |
| RetentionAgent | `/api/ai/retention/generate` | 流失用户挽留话术 |
| MerchantCopywritingAgent | `/api/ai/merchant/copywriting/generate` | 商品文案批量生成 |
| CommentManagementAgent | `/api/ai/merchant/comment/analyze` | 评论情感和回复建议 |
| MarketingMaterialAgent | `/api/ai/merchant/marketing/generate` | 营销素材生成 |
| OrderDataAnalysisAgent | `/api/ai/merchant/order-analysis` | 用户订单经营分析 |
| AfterSaleTicketAgent | `/api/ai/after-sale/classify` | 售后工单智能分拣 |
| InventorySalesAnalysisAgent | `/api/ai/inventory/analysis` | 库存销售分析 |
| OperationBriefingAgent | `/api/ai/operation/briefing` | 自动化运营简报 |
| PersonalizedRecommendationAgent | `/api/ai/recommend/personalized` | 千人千面推荐 |
| SceneMatchAgent | `/api/ai/match/scene` | 场景化搭配推荐 |
| ContentSafetyAgent | `/api/ai/safety/review` | 内容安全审核 |
| RagKnowledgeAgent | `/api/ai/rag/ask` | 知识库问答 |
| MallAgentWorkflow | `/api/ai/workbench/chat` | 多 Agent 统一工作台 |

## 4. 架构链路

```text
前端 AI 页面 / Postman
  -> /api/ai/*
  -> Controller
  -> Agent
  -> Prompt + Memory + Tool + RAG
  -> DeepSeekService
  -> DeepSeek API
```

多 Agent 工作台链路：

```text
MallAgentWorkflow
  -> ContentSafetyAgent 初筛
  -> AgentIntentClassifier 意图识别
  -> RolePermissionService 权限校验
  -> AgentRouter 路由目标 Agent
  -> 目标 Agent 执行
  -> AgentWorkflowResult 统一响应
```

## 5. 配置方式

`后端/src/main/resources/application-ai.yml`：

```yaml
deepseek:
  api-key: ${DEEPSEEK_API_KEY:}
  base-url: https://api.deepseek.com
  chat-model: deepseek-chat
  reasoner-model: deepseek-reasoner
  timeout-seconds: 60
  max-retries: 2
```

Windows PowerShell 临时配置：

```powershell
$env:DEEPSEEK_API_KEY="你的 DeepSeek Key"
```

永久配置：

```powershell
[Environment]::SetEnvironmentVariable("DEEPSEEK_API_KEY", "你的 DeepSeek Key", "User")
```

## 6. 数据库和业务数据

AI 模块不直接操作 Mapper，不直接拼 SQL。核心数据来源：

- 商品：ProductTool -> ProductService
- 订单：OrderTool -> OrderService
- 用户：UserTool -> UserService / OrderService / CartService
- 评论：CommentTool -> ReviewService
- 库存：InventoryTool -> ProductService / OrderService

缺失业务表时，Tool 返回空集合或降级结果，不抛出无意义异常。

## 7. Prompt 管理

Prompt 文件目录：

```text
后端/src/main/resources/prompt/
```

设计原因：

- Controller 不硬编码 Prompt。
- Agent 行为可审计、可维护。
- 答辩时可以直接展示 Prompt 工程设计。

## 8. RAG 知识库

支持：

- 上传 `.txt`、`.md` 文档。
- 文档切片。
- 关键词相似度检索。
- 本地 JSON 持久化。
- 无依据时明确返回“知识库中暂无相关依据”。

测试接口：

```text
POST /api/ai/rag/document/upload
POST /api/ai/rag/ask
```

## 9. 健康检查

```text
GET /api/ai/health
```

检查项包括：

- DeepSeek Key 是否可用。
- Prompt 文件是否存在。
- RAG 文档数量。
- Tool 状态。
- 已启用 Agent。

## 10. 常见问题

| 问题 | 原因 | 处理 |
| --- | --- | --- |
| `deepseekAvailable=false` | 后端进程没有读取到 Key | 在启动后端前设置 `DEEPSEEK_API_KEY` |
| AI 调用失败但接口仍返回 | Agent 触发降级策略 | 查看响应中的 fallback 信息和后端日志 |
| RAG 回答“暂无依据” | 检索分数低或未上传文档 | 上传规则文档后重试 |
| USER 无法访问运营分析 | 权限控制正常 | 使用 MERCHANT / ADMIN / WAREHOUSE 角色测试 |
| 结果没有编造商品 | 系统限制 Agent 只基于 Tool 返回商品 | 属于设计要求 |

## 11. 答辩展示重点

1. 传统商城业务完整。
2. AI Agent 以低侵入方式新增。
3. Tool 封装避免 AI 直接访问数据库。
4. Prompt 文件化，便于审计和维护。
5. RAG 避免售后政策编造。
6. 内容安全先审再路由。
7. 多 Agent 工作台统一入口便于演示。
