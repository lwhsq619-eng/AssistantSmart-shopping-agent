# 部署说明

## 1. 环境要求

| 环境 | 版本 |
| --- | --- |
| JDK | 17 |
| Maven | 3.8+ |
| Node.js | 18+ |
| MySQL | 8.x |
| Redis | 可选，本项目默认使用本地缓存 |

## 2. 本地开发部署

### 2.1 配置环境变量

Windows PowerShell：

```powershell
$env:DEEPSEEK_API_KEY="你的 DeepSeek Key"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="你的 MySQL 密码"
```

Linux：

```bash
export DEEPSEEK_API_KEY="你的 DeepSeek Key"
export MYSQL_USERNAME="root"
export MYSQL_PASSWORD="你的 MySQL 密码"
```

### 2.2 初始化 MySQL

```bash
mysql --default-character-set=utf8mb4 -u root -p
```

```sql
source D:/javaee/网上商城购物系统/data.sql;
```

如需追加 AI 演示数据：

```sql
source D:/javaee/网上商城购物系统/docs/demo-data.sql;
```

### 2.3 启动后端

```powershell
cd D:\javaee\网上商城购物系统\后端
mvn spring-boot:run
```

### 2.4 启动前端

```powershell
cd D:\javaee\网上商城购物系统\前端
npm install
npm run dev
```

## 3. 后端 Maven 打包

```powershell
cd D:\javaee\网上商城购物系统\后端
mvn clean package -DskipTests
```

打包结果：

```text
后端/target/*.jar
```

## 4. 前端生产构建

```powershell
cd D:\javaee\网上商城购物系统\前端
npm run build
```

构建结果：

```text
前端/dist/
```

## 5. Windows 运行命令

```powershell
cd D:\javaee\网上商城购物系统\后端
$env:DEEPSEEK_API_KEY="你的 DeepSeek Key"
$env:MYSQL_USERNAME="root"
$env:MYSQL_PASSWORD="你的 MySQL 密码"
java -jar target\mall-system-1.0.0.jar
```

## 6. Linux 运行命令

```bash
cd /opt/mall/后端
export DEEPSEEK_API_KEY="你的 DeepSeek Key"
export MYSQL_USERNAME="root"
export MYSQL_PASSWORD="你的 MySQL 密码"
nohup java -jar target/mall-system-1.0.0.jar > mall.log 2>&1 &
```

## 7. Redis 可选配置

当前验证码和会话默认可用本地缓存。若后续改造 Redis，可新增：

```yaml
spring:
  data:
    redis:
      host: localhost
      port: 6379
```

## 8. 常见部署错误

| 错误 | 原因 | 处理 |
| --- | --- | --- |
| `Access denied for user root` | MySQL 用户名或密码错误 | 检查 `MYSQL_USERNAME`、`MYSQL_PASSWORD` |
| `Unknown database mall_system` | 未导入 SQL | 执行 `data.sql` |
| AI Key 为空 | 启动进程未读取环境变量 | 在启动后端前设置 `DEEPSEEK_API_KEY` |
| 前端接口跨域 | 后端未启动或端口错误 | 确认后端 `8080` 正常运行 |
| `npm install` 找不到 package.json | 当前目录错误 | 进入 `前端` 目录执行 |
| Java 版本错误 | 使用了 JDK 8 | 切换到 JDK 17 |
