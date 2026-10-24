package com.example.mall.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 兼容旧版课程设计数据库的轻量结构修复器。
 * 只在表或字段缺失时补齐结构，不删除数据、不覆盖业务数据。
 */
@Component
public class DatabaseSchemaRepair implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaRepair.class);

    private final JdbcTemplate jdbcTemplate;

    public DatabaseSchemaRepair(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        try {
            repairCoreColumns();
            createExtensionTables();
            repairExtensionColumns();
            normalizeDefaultValues();
        } catch (Exception e) {
            log.warn("Database schema repair skipped: {}", e.getMessage());
        }
    }

    private void repairCoreColumns() {
        addColumn("sys_user", "role", "VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色'");
        addColumn("sys_user", "avatar", "VARCHAR(500) NULL COMMENT '头像'");
        addColumn("sys_user", "avatar_url", "VARCHAR(500) NULL COMMENT '头像'");
        addColumn("sys_user", "deleted", "TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'");

        addColumn("product_category", "image_url", "VARCHAR(500) NULL COMMENT '分类封面'");
        addColumn("product_category", "deleted", "TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'");

        addColumn("product", "brand_id", "BIGINT NULL COMMENT '品牌编号'");
        addColumn("product", "brand_name", "VARCHAR(100) NULL COMMENT '品牌名称'");
        addColumn("product", "detail_image_url", "VARCHAR(1000) NULL COMMENT '商品详情图'");
        addColumn("product", "market_price", "DECIMAL(10,2) NULL COMMENT '市场原价'");
        addColumn("product", "discount_price", "DECIMAL(10,2) NULL COMMENT '折扣价格'");
        addColumn("product", "sales", "INT NOT NULL DEFAULT 0 COMMENT '销量'");
        addColumn("product", "rating", "DECIMAL(3,1) NOT NULL DEFAULT 5.0 COMMENT '评分'");
        addColumn("product", "review_count", "INT NOT NULL DEFAULT 0 COMMENT '评价数量'");
        addColumn("product", "specs", "VARCHAR(1000) NULL COMMENT '商品规格'");
        addColumn("product", "is_special", "TINYINT NOT NULL DEFAULT 0 COMMENT '是否今日特价'");
        addColumn("product", "is_recommended", "TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐商品'");
        addColumn("product", "is_new", "TINYINT NOT NULL DEFAULT 0 COMMENT '是否新品'");
        addColumn("product", "deleted", "TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'");

        addColumn("orders", "discount_amount", "DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额'");
        addColumn("orders", "freight_amount", "DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费'");
        addColumn("orders", "pay_amount", "DECIMAL(10,2) NULL COMMENT '应付金额'");
        addColumn("orders", "payment_method", "VARCHAR(50) NULL COMMENT '支付方式'");
        addColumn("orders", "paid_time", "DATETIME NULL COMMENT '支付时间'");
        addColumn("orders", "deleted", "TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'");

        addColumn("order_item", "spec_info", "VARCHAR(255) NULL COMMENT '规格信息'");
        addColumn("order_item", "reviewed", "TINYINT NOT NULL DEFAULT 0 COMMENT '是否评价'");
        addColumn("order_item", "deleted", "TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'");
    }

    private void createExtensionTables() {
        createTableIfMissing("brand", """
                CREATE TABLE brand (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  name VARCHAR(100) NOT NULL,
                  slogan VARCHAR(255),
                  description TEXT,
                  image_url VARCHAR(500),
                  status TINYINT NOT NULL DEFAULT 1,
                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌表'
                """);

        createTableIfMissing("product_image", """
                CREATE TABLE product_image (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  product_id BIGINT NOT NULL,
                  image_url VARCHAR(1000) NOT NULL,
                  sort_no INT DEFAULT 0
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表'
                """);

        createTableIfMissing("product_sku", """
                CREATE TABLE product_sku (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  product_id BIGINT NOT NULL,
                  sku_name VARCHAR(100),
                  spec_info VARCHAR(255),
                  price DECIMAL(10,2),
                  stock INT DEFAULT 0
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格表'
                """);

        createTableIfMissing("shopping_cart", """
                CREATE TABLE shopping_cart (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  user_id BIGINT NOT NULL,
                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表'
                """);

        createTableIfMissing("shopping_cart_item", """
                CREATE TABLE shopping_cart_item (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  cart_id BIGINT NOT NULL,
                  user_id BIGINT NOT NULL,
                  product_id BIGINT NOT NULL,
                  product_name VARCHAR(120),
                  product_image VARCHAR(1000),
                  spec_info VARCHAR(255),
                  price DECIMAL(10,2),
                  quantity INT DEFAULT 1,
                  selected TINYINT DEFAULT 1,
                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车商品表'
                """);

        createTableIfMissing("user_address", """
                CREATE TABLE user_address (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  user_id BIGINT NOT NULL,
                  receiver_name VARCHAR(50),
                  receiver_phone VARCHAR(20),
                  province VARCHAR(50),
                  city VARCHAR(50),
                  detail_address VARCHAR(255),
                  is_default TINYINT DEFAULT 0,
                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
                  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表'
                """);

        createTableIfMissing("payment_record", """
                CREATE TABLE payment_record (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  order_no VARCHAR(50),
                  user_id BIGINT,
                  amount DECIMAL(10,2),
                  payment_method VARCHAR(50),
                  status VARCHAR(30),
                  pay_time DATETIME,
                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表'
                """);

        createTableIfMissing("product_review", """
                CREATE TABLE product_review (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  product_id BIGINT NOT NULL,
                  order_id BIGINT,
                  order_item_id BIGINT,
                  user_id BIGINT,
                  username VARCHAR(50),
                  avatar VARCHAR(500),
                  rating INT,
                  content VARCHAR(1000),
                  image_url VARCHAR(1000),
                  anonymous TINYINT DEFAULT 0,
                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表'
                """);

        createTableIfMissing("gift_card", """
                CREATE TABLE gift_card (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  name VARCHAR(100),
                  amount DECIMAL(10,2),
                  image_url VARCHAR(500),
                  description VARCHAR(255),
                  status TINYINT DEFAULT 1
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='礼品卡表'
                """);

        createTableIfMissing("user_gift_card", """
                CREATE TABLE user_gift_card (
                  id BIGINT PRIMARY KEY AUTO_INCREMENT,
                  user_id BIGINT,
                  gift_card_id BIGINT,
                  gift_card_name VARCHAR(100),
                  amount DECIMAL(10,2),
                  recipient VARCHAR(80),
                  blessing VARCHAR(255),
                  redeem_code VARCHAR(50),
                  status VARCHAR(30),
                  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
                ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户礼品卡表'
                """);
    }

    private void repairExtensionColumns() {
        addColumn("brand", "name", "VARCHAR(100) NULL COMMENT '品牌名称'");
        addColumn("brand", "slogan", "VARCHAR(255) NULL COMMENT '品牌理念'");
        addColumn("brand", "description", "TEXT NULL COMMENT '品牌简介'");
        addColumn("brand", "image_url", "VARCHAR(500) NULL COMMENT '品牌图片'");
        addColumn("brand", "status", "TINYINT NOT NULL DEFAULT 1 COMMENT '状态'");
        addColumn("brand", "create_time", "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'");
        addColumn("brand", "update_time", "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'");

        addColumn("product_image", "product_id", "BIGINT NULL COMMENT '商品编号'");
        addColumn("product_image", "image_url", "VARCHAR(1000) NULL COMMENT '图片地址'");
        addColumn("product_image", "sort_no", "INT DEFAULT 0 COMMENT '排序号'");

        addColumn("product_sku", "product_id", "BIGINT NULL COMMENT '商品编号'");
        addColumn("product_sku", "sku_name", "VARCHAR(100) NULL COMMENT '规格名称'");
        addColumn("product_sku", "spec_info", "VARCHAR(255) NULL COMMENT '规格信息'");
        addColumn("product_sku", "price", "DECIMAL(10,2) NULL COMMENT '价格'");
        addColumn("product_sku", "stock", "INT DEFAULT 0 COMMENT '库存'");

        addColumn("shopping_cart", "user_id", "BIGINT NULL COMMENT '用户编号'");
        addColumn("shopping_cart", "create_time", "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'");
        addColumn("shopping_cart", "update_time", "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'");

        addColumn("shopping_cart_item", "cart_id", "BIGINT NULL COMMENT '购物车编号'");
        addColumn("shopping_cart_item", "user_id", "BIGINT NULL COMMENT '用户编号'");
        addColumn("shopping_cart_item", "product_id", "BIGINT NULL COMMENT '商品编号'");
        addColumn("shopping_cart_item", "product_name", "VARCHAR(120) NULL COMMENT '商品名称'");
        addColumn("shopping_cart_item", "product_image", "VARCHAR(1000) NULL COMMENT '商品图片'");
        addColumn("shopping_cart_item", "spec_info", "VARCHAR(255) NULL COMMENT '规格信息'");
        addColumn("shopping_cart_item", "price", "DECIMAL(10,2) NULL COMMENT '价格'");
        addColumn("shopping_cart_item", "quantity", "INT DEFAULT 1 COMMENT '数量'");
        addColumn("shopping_cart_item", "selected", "TINYINT DEFAULT 1 COMMENT '是否选中'");
        addColumn("shopping_cart_item", "create_time", "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'");
        addColumn("shopping_cart_item", "update_time", "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'");

        addColumn("user_address", "user_id", "BIGINT NULL COMMENT '用户编号'");
        addColumn("user_address", "receiver_name", "VARCHAR(50) NULL COMMENT '收货人'");
        addColumn("user_address", "receiver_phone", "VARCHAR(20) NULL COMMENT '手机号'");
        addColumn("user_address", "province", "VARCHAR(50) NULL COMMENT '省份'");
        addColumn("user_address", "city", "VARCHAR(50) NULL COMMENT '城市'");
        addColumn("user_address", "detail_address", "VARCHAR(255) NULL COMMENT '详细地址'");
        addColumn("user_address", "is_default", "TINYINT DEFAULT 0 COMMENT '默认地址'");
        addColumn("user_address", "create_time", "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'");
        addColumn("user_address", "update_time", "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'");

        addColumn("payment_record", "order_no", "VARCHAR(50) NULL COMMENT '订单号'");
        addColumn("payment_record", "user_id", "BIGINT NULL COMMENT '用户编号'");
        addColumn("payment_record", "amount", "DECIMAL(10,2) NULL COMMENT '金额'");
        addColumn("payment_record", "payment_method", "VARCHAR(50) NULL COMMENT '支付方式'");
        addColumn("payment_record", "status", "VARCHAR(30) NULL COMMENT '状态'");
        addColumn("payment_record", "pay_time", "DATETIME NULL COMMENT '支付时间'");
        addColumn("payment_record", "create_time", "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'");

        addColumn("product_review", "product_id", "BIGINT NULL COMMENT '商品编号'");
        addColumn("product_review", "order_id", "BIGINT NULL COMMENT '订单编号'");
        addColumn("product_review", "order_item_id", "BIGINT NULL COMMENT '订单明细编号'");
        addColumn("product_review", "user_id", "BIGINT NULL COMMENT '用户编号'");
        addColumn("product_review", "username", "VARCHAR(50) NULL COMMENT '用户名'");
        addColumn("product_review", "avatar", "VARCHAR(500) NULL COMMENT '头像'");
        addColumn("product_review", "rating", "INT NULL COMMENT '评分'");
        addColumn("product_review", "content", "VARCHAR(1000) NULL COMMENT '评价内容'");
        addColumn("product_review", "image_url", "VARCHAR(1000) NULL COMMENT '评价图片'");
        addColumn("product_review", "anonymous", "TINYINT DEFAULT 0 COMMENT '匿名'");
        addColumn("product_review", "create_time", "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'");

        addColumn("gift_card", "name", "VARCHAR(100) NULL COMMENT '礼品卡名称'");
        addColumn("gift_card", "amount", "DECIMAL(10,2) NULL COMMENT '金额'");
        addColumn("gift_card", "image_url", "VARCHAR(500) NULL COMMENT '图片'");
        addColumn("gift_card", "description", "VARCHAR(255) NULL COMMENT '描述'");
        addColumn("gift_card", "status", "TINYINT DEFAULT 1 COMMENT '状态'");

        addColumn("user_gift_card", "user_id", "BIGINT NULL COMMENT '用户编号'");
        addColumn("user_gift_card", "gift_card_id", "BIGINT NULL COMMENT '礼品卡编号'");
        addColumn("user_gift_card", "gift_card_name", "VARCHAR(100) NULL COMMENT '礼品卡名称'");
        addColumn("user_gift_card", "amount", "DECIMAL(10,2) NULL COMMENT '金额'");
        addColumn("user_gift_card", "recipient", "VARCHAR(80) NULL COMMENT '赠送对象'");
        addColumn("user_gift_card", "blessing", "VARCHAR(255) NULL COMMENT '祝福语'");
        addColumn("user_gift_card", "redeem_code", "VARCHAR(50) NULL COMMENT '兑换码'");
        addColumn("user_gift_card", "status", "VARCHAR(30) NULL COMMENT '状态'");
        addColumn("user_gift_card", "create_time", "DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'");
    }

    private void normalizeDefaultValues() {
        executeSafely("UPDATE sys_user SET role = 'ADMIN' WHERE username = 'admin' AND (role IS NULL OR role = '')");
        executeSafely("UPDATE sys_user SET role = 'USER' WHERE username <> 'admin' AND (role IS NULL OR role = '')");
        executeSafely("UPDATE sys_user SET avatar = avatar_url WHERE (avatar IS NULL OR avatar = '') AND avatar_url IS NOT NULL");
        executeSafely("UPDATE product SET detail_image_url = image_url WHERE detail_image_url IS NULL");
        executeSafely("UPDATE product SET market_price = price WHERE market_price IS NULL");
        executeSafely("UPDATE product SET discount_price = price WHERE discount_price IS NULL");
        executeSafely("UPDATE orders SET pay_amount = total_amount WHERE pay_amount IS NULL");
        seedBrandsIfEmpty();
    }

    private void seedBrandsIfEmpty() {
        if (!tableExists("brand")) {
            return;
        }
        Integer count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM brand", Integer.class);
        if (count != null && count > 0) {
            return;
        }
        List<String> sqlList = List.of(
                "INSERT INTO brand (id,name,slogan,description,image_url,status) VALUES (1,'NovaTech 数码生活','让科技融入日常','专注数码配件与智能设备，提供稳定易用的科技产品。','https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=800&q=80',1)",
                "INSERT INTO brand (id,name,slogan,description,image_url,status) VALUES (2,'Mori Home 家居美学','把舒适带回家','以简洁家居、收纳和灯具提升日常生活质感。','https://images.unsplash.com/photo-1513161455079-7dc1de15ef3e?auto=format&fit=crop&w=800&q=80',1)",
                "INSERT INTO brand (id,name,slogan,description,image_url,status) VALUES (3,'Urban Step 潮流鞋服','轻松穿出城市节奏','覆盖运动鞋、卫衣、背包等年轻化出行单品。','https://images.unsplash.com/photo-1445205170230-053b83016050?auto=format&fit=crop&w=800&q=80',1)",
                "INSERT INTO brand (id,name,slogan,description,image_url,status) VALUES (4,'Pure Glow 美妆护肤','清爽自然的日常护理','提供护肤、美妆和香氛产品，强调温和与高质感。','https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=800&q=80',1)"
        );
        sqlList.forEach(this::executeSafely);
    }

    private void addColumn(String tableName, String columnName, String definition) {
        if (!tableExists(tableName) || columnExists(tableName, columnName)) {
            return;
        }
        executeSafely("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + definition);
    }

    private void createTableIfMissing(String tableName, String ddl) {
        if (tableExists(tableName)) {
            return;
        }
        executeSafely(ddl);
    }

    private boolean tableExists(String tableName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.tables
                WHERE table_schema = DATABASE() AND table_name = ?
                """, Integer.class, tableName);
        return count != null && count > 0;
    }

    private boolean columnExists(String tableName, String columnName) {
        Integer count = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = DATABASE() AND table_name = ? AND column_name = ?
                """, Integer.class, tableName, columnName);
        return count != null && count > 0;
    }

    private void executeSafely(String sql) {
        try {
            jdbcTemplate.execute(sql);
            log.info("Database schema repair executed: {}", firstLine(sql));
        } catch (Exception e) {
            log.warn("Database schema repair SQL skipped: {}, reason={}", firstLine(sql), e.getMessage());
        }
    }

    private String firstLine(String sql) {
        String normalized = sql == null ? "" : sql.strip().replaceAll("\\s+", " ");
        return normalized.length() > 140 ? normalized.substring(0, 140) + "..." : normalized;
    }
}

