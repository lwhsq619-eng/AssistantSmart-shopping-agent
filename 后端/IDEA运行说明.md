# IDEA 后端运行说明

## 结论

后端是 Spring Boot 3 项目，必须使用 JDK 17 或更高版本。使用 JDK 8 会报错，例如：

```text
无效的标记: --release
Unsupported class file major version
class file has wrong version
```

## IDEA 必改配置

### 1. Project SDK

路径：

```text
File -> Project Structure -> Project
```

设置：

```text
SDK: JDK 17
Language level: 17
```

### 2. Module SDK

路径：

```text
File -> Project Structure -> Modules -> 后端
```

设置：

```text
Module SDK: JDK 17
```

### 3. Maven JRE

路径：

```text
Settings -> Build, Execution, Deployment -> Build Tools -> Maven -> Runner
```

设置：

```text
JRE: JDK 17
```

不要选择 Java 8。

### 3.1 Maven 下载依赖失败

如果出现类似错误：

```text
Could not transfer artifact ...
.tmp -> ...pom
```

这是 Maven 本地缓存或中央仓库下载问题，不是启动类代码错误。处理方式：

```bash
rmdir /s /q %USERPROFILE%\.m2\repository\org\jetbrains\kotlinx\kotlinx-serialization-bom
cd 后端
mvn -U clean package -DskipTests
```

本项目已在 `.mvn/settings.xml` 配置阿里云 Maven 镜像，优先避免直接访问 Maven Central。

### 4. 启动类

运行这个类：

```text
com.example.mall.MallApplication
```

### 5. 数据库

先导入根目录的 `data.sql`：

```bash
mysql -uroot -p < data.sql
```

默认数据库连接：

```yaml
url: jdbc:mysql://localhost:3306/mall_system
username: root
password: 123456
```

如果你的 MySQL 密码不同，修改：

```text
src/main/resources/application.yml
```

## 本机验证命令

```bash
cd 后端
mvn -DskipTests package
mvn spring-boot:run
```

启动成功后访问：

```text
http://localhost:8080/doc.html
```
