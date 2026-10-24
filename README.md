# 网上商城购物系统

基于 Spring Boot 3 + Vue 3 的前后端分离网上商城系统，包含商城前台、后台管理、购物车、订单、评价、库存、RAG 知识库和 DeepSeek AI Agent 工作台，适合作为 Java Web / Spring Boot + Vue 课程设计提交与答辩展示。

## 1. 技术栈

| 模块 | 技术 |
| --- | --- |
| 后端 | Java 17、Spring Boot 3、Spring MVC、MyBatis-Plus、Maven、Lombok |
| 前端 | Vue 3、Vite、Vue Router、Pinia、Axios、Element Plus、ECharts |
| 数据库 | MySQL 8 |
| AI | DeepSeek OpenAI-Compatible API、deepseek-chat、deepseek-reasoner |
| 文档 | Knife4j / OpenAPI、Postman Collection |

## 2. 目录结构

```text
网上商城购物系统/
├── 前端/
├── 后端/
├── docs/
│   ├── AI_AGENT_API.md
│   ├── DEPLOYMENT.md
│   ├── DEMO_SCRIPT.md
│   ├── DEFENSE_OUTLINE.md
│   ├── RESUME_PROJECT.md
│   ├── demo-data.sql
│   └── postman/
├── data.sql
├── .env.example
├── README.md
└── README_AGENT.md
```

## 3. 系统架构

```text
Vue 3 前端
  -> Axios REST API
  -> Spring Boot Controller
  -> Service 业务层
  -> MyBatis-Plus Mapper
  -> MySQL

AI 页面 / Postman
  -> /api/ai/*
  -> Agent Controller
  -> Agent Service / Tool / RAG / Memory / Prompt
  -> DeepSeek API + 商城已有 Service
```

AI 模块采用新增包集成，不删除、不重写原商城业务。所有业务数据查询优先通过已有 Service 和 Tool 封装完成。

## 4. AI Agent 功能列表

| 分类 | 功能 |
| --- | --- |
| 用户端 | AI 智能导购、智能客服、语义搜索、个性化推荐、场景化搭配、流失挽留 |
| 商家端 | 商品文案生成、营销素材生成、评论智能分析、订单数据分析、运营简报 |
| 仓储端 | 库存销售分析、售后工单分拣、补货建议 |
| 通用端 | RAG 知识库问答、内容安全审核、多 Agent 工作台、健康检查 |

## 5. 环境变量

复制 `.env.example` 的变量名，在系统环境变量或启动终端中配置。不要把真实 Key 写入代码或提交到 Git。

Windows PowerShell：

```powershell
$env:DEEPSEEK_API_KEY="你的 DeepSeek API Key"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="你的 MySQL 密码"
```

Linux / macOS：

```bash
export DEEPSEEK_API_KEY="你的 DeepSeek API Key"
export MYSQL_USERNAME="root"
export MYSQL_PASSWORD="你的 MySQL 密码"
```

## 6. 数据库初始化

前提：已安装 MySQL 8。

```bash
mysql --default-character-set=utf8mb4 -u root -p
```

进入 MySQL 后执行：

```sql
source D:/javaee/网上商城购物系统/data.sql;
```

`data.sql` 会初始化 `mall_system` 数据库、商城基础表、商品、分类、用户、订单、购物车、评价、AI 演示数据等。

## 7. 后端启动

```powershell
cd D:\javaee\网上商城购物系统\后端
mvn spring-boot:run
```

后端地址：

```text
http://localhost:8080
```

接口文档：

```text
http://localhost:8080/doc.html
http://localhost:8080/swagger-ui.html
```

## 8. 前端启动

```powershell
cd D:\javaee\网上商城购物系统\前端
npm install
npm run dev
```

前端地址：

```text
http://localhost:5173
```

## 9. 登录入口

```text
管理员登录：http://localhost:5173/login/admin
普通用户登录：http://localhost:5173/login/user
普通用户注册：http://localhost:5173/register
用户商城首页：http://localhost:5173/user/home
AI 导购页：http://localhost:5173/ai/guide
智能客服页：http://localhost:5173/ai/customer-service
商家 AI 工作台：http://localhost:5173/admin/ai/workbench
仓储 AI 工作台：http://localhost:5173/admin/ai/inventory
```

默认管理员账号：

```text
账号：admin
密码：123456
角色：ADMIN
```

普通用户可使用 `zhangsan / 123456` 等初始化账号，角色为 `USER`。

## 10. 接口测试

AI 健康检查：

```text
GET http://localhost:8080/api/ai/health
```

统一工作台：

```text
POST http://localhost:8080/api/ai/workbench/chat
```

请求示例：

```json
{
  "userId": 1,
  "role": "USER",
  "sessionId": "demo-001",
  "message": "推荐500元以内的无线耳机"
}
```

完整接口说明见：[docs/AI_AGENT_API.md](docs/AI_AGENT_API.md)。

## 11. 打包构建

后端：

```powershell
cd D:\javaee\网上商城购物系统\后端
mvn clean package -DskipTests
```

前端：

```powershell
cd D:\javaee\网上商城购物系统\前端
npm run build
```

## 12. 常见问题

| 问题 | 处理 |
| --- | --- |
| AI 返回服务不可用 | 检查 `DEEPSEEK_API_KEY` 是否配置到后端启动进程 |
| MySQL 连接失败 | 检查 `MYSQL_URL`、`MYSQL_USERNAME`、`MYSQL_PASSWORD` |
| 前端 npm install 报 package.json 不存在 | 进入 `前端` 目录执行，不要在项目根目录直接安装 |
| 中文乱码 | MySQL 使用 `utf8mb4`，终端和 IDE 文件编码使用 UTF-8 |
| RAG 无依据 | 先上传知识文档，或使用内置默认规则文档测试 |

## 13. 相关文档

- AI 模块说明：[README_AGENT.md](README_AGENT.md)
- 部署说明：[docs/DEPLOYMENT.md](docs/DEPLOYMENT.md)
- 接口文档：[docs/AI_AGENT_API.md](docs/AI_AGENT_API.md)
- 演示脚本：[docs/DEMO_SCRIPT.md](docs/DEMO_SCRIPT.md)
- 答辩大纲：[docs/DEFENSE_OUTLINE.md](docs/DEFENSE_OUTLINE.md)
- 简历包装：[docs/RESUME_PROJECT.md](docs/RESUME_PROJECT.md)
