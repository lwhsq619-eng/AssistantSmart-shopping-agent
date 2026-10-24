# AI Agent API 文档

基础地址：

```text
http://localhost:8080
```

统一返回字段说明：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| success | boolean | 是否成功 |
| message | string | 错误或提示信息 |
| data | object | 业务数据 |
| errorCode | string | 统一错误码 |

## 1. 通用端接口

### 1.1 健康检查

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/health` |
| Method | GET |
| 说明 | 检查 DeepSeek、Prompt、RAG、Tool 状态 |

返回示例：

```json
{
  "success": true,
  "deepseekAvailable": true,
  "ragDocumentCount": 2,
  "toolStatus": {
    "productTool": true,
    "orderTool": true
  }
}
```

### 1.2 最小聊天接口

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/chat` |
| Method | POST |

请求：

```json
{
  "message": "你好，帮我推荐商品"
}
```

返回：

```json
{
  "success": true,
  "model": "deepseek-chat",
  "content": "AI 回复内容"
}
```

### 1.3 多 Agent 工作台

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/workbench/chat` |
| Method | POST |
| 说明 | 统一入口，自动完成安全审核、意图识别、权限校验和 Agent 路由 |

请求：

```json
{
  "userId": 1,
  "role": "USER",
  "sessionId": "wb-demo-001",
  "message": "推荐500元以内的无线耳机"
}
```

返回：

```json
{
  "success": true,
  "sessionId": "wb-demo-001",
  "intent": "SHOPPING_GUIDE",
  "agentName": "ShoppingGuideAgent",
  "reply": "推荐结果...",
  "durationMs": 1200
}
```

## 2. 用户端接口

### 2.1 AI 智能导购

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/guide/chat` |
| Method | POST |

请求：

```json
{
  "userId": 1,
  "sessionId": "guide-001",
  "message": "我想买500元以内适合跑步的无线耳机"
}
```

返回：

```json
{
  "success": true,
  "sessionId": "guide-001",
  "intent": "商品推荐",
  "reply": "推荐说明",
  "products": [
    {
      "productId": 1,
      "productName": "运动蓝牙耳机",
      "price": 199.00,
      "stock": 50,
      "reason": "适合运动，价格符合预算"
    }
  ],
  "needMoreInfo": false
}
```

### 2.2 智能客服

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/customer-service/chat` |
| Method | POST |

请求：

```json
{
  "userId": 1,
  "sessionId": "cs-001",
  "message": "我的订单怎么还没发货？订单号是202401010001"
}
```

返回：

```json
{
  "success": true,
  "sessionId": "cs-001",
  "intent": "ORDER_STATUS",
  "reply": "你的订单当前状态为待发货。"
}
```

### 2.3 语义化商品搜索

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/search/semantic` |
| Method | POST |

请求：

```json
{
  "userId": 1,
  "keyword": "适合宿舍用的安静小风扇，价格别太贵",
  "page": 1,
  "size": 10
}
```

返回：

```json
{
  "success": true,
  "query": "适合宿舍用的安静小风扇，价格别太贵",
  "normalizedKeyword": "小风扇",
  "tags": ["宿舍", "静音", "低价"],
  "products": []
}
```

### 2.4 个性化推荐

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/recommend/personalized` |
| Method | POST |

请求：

```json
{
  "userId": 1,
  "scene": "HOME_PAGE",
  "limit": 10
}
```

### 2.5 场景化搭配

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/match/scene` |
| Method | POST |

请求：

```json
{
  "userId": 1,
  "sceneType": "DORMITORY",
  "budget": 500,
  "message": "帮我搭配一套适合大学宿舍用的生活用品",
  "limit": 5
}
```

### 2.6 RAG 知识库问答

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/rag/ask` |
| Method | POST |

请求：

```json
{
  "userId": 1,
  "question": "商品签收后几天内可以无理由退货？",
  "documentType": "RETURN_RULE",
  "topK": 5
}
```

返回：

```json
{
  "success": true,
  "answer": "根据知识库规则，商品签收后七日内...",
  "sources": [
    {
      "documentId": "doc-default-return-rule",
      "chunkId": "chunk-001",
      "score": 0.87,
      "content": "七天无理由退货规则..."
    }
  ],
  "noEvidence": false
}
```

## 3. 商家端接口

### 3.1 商品文案生成

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/merchant/copywriting/generate` |
| Method | POST |

请求：

```json
{
  "productIds": [1001],
  "style": "年轻活泼",
  "targetAudience": "大学生",
  "scene": "开学季",
  "generateTypes": ["TITLE", "SELLING_POINTS", "PROMOTION"]
}
```

### 3.2 营销素材生成

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/merchant/marketing/generate` |
| Method | POST |

请求：

```json
{
  "productIds": [1001],
  "activityName": "开学季好物",
  "targetAudience": "大学生",
  "style": "年轻活泼",
  "channels": ["COMMUNITY", "PUSH", "SLOGAN"],
  "extraRequirement": "突出宿舍实用性"
}
```

### 3.3 评论智能分析

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/merchant/comment/analyze` |
| Method | POST |

请求：

```json
{
  "productId": 1001,
  "limit": 20,
  "analysisTypes": ["SENTIMENT", "REPLY"]
}
```

### 3.4 订单数据分析

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/merchant/order-analysis` |
| Method | POST |

请求：

```json
{
  "startDate": "2024-06-01",
  "endDate": "2024-06-30",
  "analysisTypes": ["FULL_REPORT"],
  "categoryId": null
}
```

### 3.5 自动化运营简报

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/operation/briefing` |
| Method | POST |

请求：

```json
{
  "startDate": "2024-06-01",
  "endDate": "2024-06-30",
  "briefingType": "MONTHLY",
  "sections": ["FULL"]
}
```

## 4. 仓储端接口

### 4.1 库存销售分析

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/inventory/analysis` |
| Method | POST |

请求：

```json
{
  "startDate": "2024-06-01",
  "endDate": "2024-06-30",
  "productIds": [1001, 1002],
  "categoryId": null,
  "analysisTypes": ["REPLENISHMENT", "LOW_STOCK"]
}
```

### 4.2 售后工单分拣

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/after-sale/classify` |
| Method | POST |

请求：

```json
{
  "userId": 1,
  "orderId": 10001,
  "message": "收到商品以后发现包装破了，里面也有划痕，想换一个",
  "images": []
}
```

返回：

```json
{
  "success": true,
  "ticketType": "EXCHANGE",
  "priority": "HIGH",
  "riskLevel": "MEDIUM",
  "reason": "用户描述商品破损且要求换一个",
  "advice": {
    "handlerRole": "售后管理员",
    "suggestedAction": "核实订单状态和破损凭证后进入换货流程",
    "needManualReview": true
  }
}
```

## 5. 内容安全接口

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/safety/review` |
| Method | POST |

请求：

```json
{
  "userId": 1,
  "contentType": "MARKETING_COPY",
  "content": "全网最低价，100%有效，永久有效",
  "contextId": 1001
}
```

返回：

```json
{
  "success": true,
  "contentType": "MARKETING_COPY",
  "safe": false,
  "riskLevel": "MEDIUM",
  "categories": ["EXTREME_CLAIM"],
  "suggestion": "MANUAL_REVIEW",
  "reason": "包含绝对化营销表达"
}
```

## 6. RAG 文档上传

| 项 | 内容 |
| --- | --- |
| URL | `/api/ai/rag/document/upload` |
| Method | POST |
| Content-Type | multipart/form-data |

参数：

| 参数 | 类型 | 必填 | 说明 |
| --- | --- | --- | --- |
| file | file | 是 | txt / md 文档 |
| documentType | string | 是 | `PRODUCT_MANUAL`、`AFTER_SALE_POLICY`、`RETURN_RULE`、`PROMOTION_RULE`、`PLATFORM_HELP`、`OTHER` |

## 7. 统一错误码

| 错误码 | 含义 |
| --- | --- |
| `PARAM_ERROR` | 参数错误 |
| `AI_API_ERROR` | DeepSeek API 调用失败 |
| `AI_TIMEOUT` | AI 请求超时 |
| `PROMPT_NOT_FOUND` | Prompt 文件不存在 |
| `TOOL_EXECUTION_ERROR` | Tool 或 Agent 执行失败 |
| `INTENT_UNKNOWN` | 意图无法识别 |
| `PERMISSION_DENIED` | 角色权限不足 |
| `SAFETY_BLOCKED` | 内容安全拦截 |
| `RAG_NO_EVIDENCE` | 知识库无检索依据 |
| `BUSINESS_DATA_EMPTY` | 业务数据为空 |

## 8. Postman 集合

```text
docs/postman/AI商城Agent系统.postman_collection.json
```
