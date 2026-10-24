# 答辩大纲

## 1. 研究背景

传统网上商城系统通常只提供商品、订单、用户、库存等基础 CRUD 能力，缺少智能导购、语义搜索、智能客服和运营分析能力。引入 AI Agent 可以提升用户体验和后台运营效率。

## 2. 传统商城痛点

1. 商品搜索依赖关键词，无法理解模糊需求。
2. 客服重复问题多，人工成本高。
3. 运营文案、评论回复、数据分析依赖人工。
4. 售后政策问答容易出现口径不一致。
5. 库存和订单数据缺少自动分析。

## 3. Agent 技术引入原因

Agent 适合处理“理解意图 -> 调用工具 -> 生成结果”的流程。本项目将商品、订单、用户、评论、库存能力封装为 Tool，让 AI 基于真实业务数据生成建议，降低编造风险。

## 4. 系统总体架构

```text
Vue 前端
  -> REST API
  -> Spring Boot 后端
  -> 原商城业务模块
  -> AI Agent 模块
  -> DeepSeek API / RAG / Tool
  -> MySQL
```

## 5. 核心模块设计

| 模块 | 说明 |
| --- | --- |
| 商品模块 | 商品浏览、分类、详情、搜索、后台管理 |
| 订单模块 | 下单、支付模拟、订单状态、我的订单 |
| 用户模块 | 管理员登录、用户登录、注册、权限控制 |
| 评论模块 | 商品评价、评论分析 |
| AI 模块 | 导购、客服、搜索、运营、仓储、RAG、工作台 |

## 6. DeepSeek 接入设计

- 使用 OpenAI-Compatible API。
- API Key 从环境变量读取。
- `deepseek-chat` 用于普通对话。
- `deepseek-reasoner` 用于复杂分析。
- Service 封装模型调用，Controller 不直接访问 DeepSeek。

## 7. Tool Calling 设计

Tool 负责连接 AI 和商城业务：

```text
Agent -> ProductTool / OrderTool / UserTool / CommentTool / InventoryTool -> 已有 Service -> MySQL
```

优点：

1. 避免 AI 直接写 SQL。
2. 复用原业务校验。
3. 控制返回字段，降低数据泄露。
4. 便于后续扩展更多 Agent。

## 8. RAG 知识库设计

流程：

```text
上传文档 -> 文本读取 -> Chunk 切分 -> 关键词检索 -> TopK 片段 -> DeepSeek 生成答案
```

设计约束：

- 无依据时直接返回“知识库中暂无相关依据”。
- 回答必须附带来源片段。
- 第一版使用本地关键词检索，后续可替换向量数据库。

## 9. 多 Agent 编排设计

统一入口：

```text
POST /api/ai/workbench/chat
```

编排步骤：

1. 内容安全审核。
2. 意图识别。
3. 角色权限校验。
4. 路由目标 Agent。
5. 统一响应和日志。

## 10. 数据库设计说明

核心表：

- `sys_user`
- `product_category`
- `product`
- `orders`
- `order_item`
- `shopping_cart`
- `product_review`
- `brand`
- `gift_card`
- `user_address`

数据覆盖商品、用户、订单、评论、库存和 AI 演示场景。

## 11. 系统测试

测试类型：

1. 后端编译：`mvn clean package -DskipTests`
2. 前端构建：`npm run build`
3. AI 健康检查：`/api/ai/health`
4. Postman 接口测试。
5. 页面演示测试。

## 12. 项目创新点

1. 传统商城业务与 AI Agent 低侵入集成。
2. Tool 封装保证 AI 基于真实数据回答。
3. RAG 知识库降低售后政策编造。
4. 多 Agent 工作台统一编排。
5. 内容安全审核前置。
6. 支持用户端、商家端、仓储端多场景智能化。

## 13. 不足与展望

不足：

1. RAG 第一版使用关键词检索，语义召回能力有限。
2. 多轮记忆采用内存 Map，重启后丢失。
3. 没有接入真实支付、物流和消息推送。

展望：

1. 接入向量模型和 Milvus / PGVector。
2. Agent 执行日志落库和可视化。
3. 接入真实物流、支付和优惠券系统。
4. 增加细粒度 RBAC 权限控制。
