-- MySQL dump 10.13  Distrib 8.0.17, for Win64 (x86_64)
--
-- Host: localhost    Database: mall_system
-- ------------------------------------------------------
-- Server version	8.0.17

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_UNIQUE_CHECKS=@@UNIQUE_CHECKS, UNIQUE_CHECKS=0 */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;

--
-- Table structure for table `brand`
--

DROP TABLE IF EXISTS `brand`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `brand` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) NOT NULL,
  `slogan` varchar(255) DEFAULT NULL,
  `description` text,
  `image_url` varchar(500) DEFAULT NULL,
  `status` tinyint(4) NOT NULL DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=5 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='品牌表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `brand`
--

LOCK TABLES `brand` WRITE;
/*!40000 ALTER TABLE `brand` DISABLE KEYS */;
INSERT INTO `brand` VALUES (1,'NovaTech 数码生活','让科技融入日常','专注数码配件与智能设备，提供稳定易用的科技产品。','https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=800&q=80',1,'2026-06-23 16:34:29','2026-06-23 16:34:29'),(2,'Mori Home 家居美学','把舒适带回家','以简洁家居、收纳和灯具提升日常生活质感。','https://images.unsplash.com/photo-1513161455079-7dc1de15ef3e?auto=format&fit=crop&w=800&q=80',1,'2026-06-23 16:34:29','2026-06-23 16:34:29'),(3,'Urban Step 潮流鞋服','轻松穿出城市节奏','覆盖运动鞋、卫衣、背包等年轻化出行单品。','https://images.unsplash.com/photo-1445205170230-053b83016050?auto=format&fit=crop&w=800&q=80',1,'2026-06-23 16:34:29','2026-06-23 16:34:29'),(4,'Pure Glow 美妆护肤','清爽自然的日常护理','提供护肤、美妆和香氛产品，强调温和与高质感。','https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=800&q=80',1,'2026-06-23 16:34:29','2026-06-23 16:34:29');
/*!40000 ALTER TABLE `brand` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `gift_card`
--

DROP TABLE IF EXISTS `gift_card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `gift_card` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `name` varchar(100) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `image_url` varchar(500) DEFAULT NULL,
  `description` varchar(255) DEFAULT NULL,
  `status` tinyint(4) DEFAULT '1',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='礼品卡表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `gift_card`
--

LOCK TABLES `gift_card` WRITE;
/*!40000 ALTER TABLE `gift_card` DISABLE KEYS */;
INSERT INTO `gift_card` VALUES (1,'50元礼品卡',50.00,'https://images.unsplash.com/photo-1513201099705-a9746e1e201f?auto=format&fit=crop&w=600&q=80','适合日常小礼物',1),(2,'100元礼品卡',100.00,'https://images.unsplash.com/photo-1512909006721-3d6018887383?auto=format&fit=crop&w=600&q=80','热门礼品卡面额',1),(3,'200元礼品卡',200.00,'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?auto=format&fit=crop&w=600&q=80','适合节日赠礼',1),(4,'500元礼品卡',500.00,'https://images.unsplash.com/photo-1513885535751-8b9238bd345a?auto=format&fit=crop&w=600&q=80','高价值礼品卡',1),(5,'节日礼品卡',299.00,'https://images.unsplash.com/photo-1482517967863-00e15c9b44be?auto=format&fit=crop&w=600&q=80','节日祝福专属',1),(6,'生日礼品卡',199.00,'https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?auto=format&fit=crop&w=600&q=80','生日礼物推荐',1);
/*!40000 ALTER TABLE `gift_card` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `order_item`
--

DROP TABLE IF EXISTS `order_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `order_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '明细编号',
  `order_id` bigint(20) NOT NULL COMMENT '订单编号',
  `product_id` bigint(20) NOT NULL COMMENT '商品编号',
  `product_name` varchar(100) NOT NULL COMMENT '商品名称',
  `product_image` varchar(500) DEFAULT NULL COMMENT '商品图片',
  `quantity` int(11) NOT NULL COMMENT '商品数量',
  `unit_price` decimal(10,2) NOT NULL COMMENT '商品单价',
  `subtotal` decimal(10,2) NOT NULL COMMENT '小计',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `spec_info` varchar(255) DEFAULT NULL COMMENT '规格信息',
  `reviewed` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否评价',
  PRIMARY KEY (`id`),
  KEY `idx_item_order` (`order_id`),
  KEY `idx_item_product` (`product_id`)
) ENGINE=InnoDB AUTO_INCREMENT=33 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单明细表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `order_item`
--

LOCK TABLES `order_item` WRITE;
/*!40000 ALTER TABLE `order_item` DISABLE KEYS */;
INSERT INTO `order_item` VALUES (1,1,1,'蓝牙降噪耳机','https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=500&q=80',1,399.00,399.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(2,2,2,'机械键盘','https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500&q=80',2,289.00,578.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(3,3,5,'简约连帽卫衣','https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=500&q=80',1,169.00,169.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(4,4,6,'轻量运动鞋','https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80',2,329.00,658.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(5,5,7,'城市通勤背包','https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80',1,219.00,219.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(6,6,8,'皮革钱包','https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=500&q=80',2,129.00,258.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(7,7,9,'北欧台灯','https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500&q=80',1,149.00,149.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(8,8,10,'人体工学椅','https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=500&q=80',2,899.00,1798.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(9,9,13,'保湿精华液','https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=500&q=80',1,199.00,199.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(10,10,14,'淡香水','https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=500&q=80',1,269.00,269.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(11,11,15,'润色口红','https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=500&q=80',2,99.00,198.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(12,12,16,'精品咖啡豆','https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=500&q=80',2,88.00,176.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(13,13,17,'坚果礼盒','https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&w=500&q=80',1,128.00,128.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(14,14,19,'瑜伽垫','https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?auto=format&fit=crop&w=500&q=80',2,119.00,238.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(15,15,20,'运动水杯','https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=500&q=80',1,59.00,59.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(16,16,21,'户外双肩包','https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=500&q=80',2,239.00,478.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(17,17,22,'婴儿湿巾','https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=500&q=80',2,39.00,78.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(18,18,23,'儿童积木套装','https://images.unsplash.com/photo-1560961911-ba7ef651a56c?auto=format&fit=crop&w=500&q=80',2,159.00,318.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(19,19,25,'无线鼠标','https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=500&q=80',1,79.00,79.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(20,20,29,'数据库系统教材','https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=500&q=80',2,89.00,178.00,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,NULL,0),(21,21,31,'轻薄智能手机','https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=500&q=80',1,45.00,45.00,'2026-06-23 16:35:07','2026-06-23 16:35:07',0,'????',0),(22,22,71,'高清投影仪','https://images.unsplash.com/photo-1601944179066-29786cb9d32a?auto=format&fit=crop&w=500&q=80',1,513.00,513.00,'2026-06-23 16:38:36','2026-06-23 16:38:36',0,'默认规格',1),(23,23,72,'家用咖啡机','https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=500&q=80',1,524.70,524.70,'2026-06-23 16:40:59','2026-06-23 16:40:59',0,'默认规格',0),(24,23,64,'宠物磨牙玩具','https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=500&q=80',1,431.10,431.10,'2026-06-23 16:40:59','2026-06-23 16:40:59',0,'默认规格',0),(25,23,42,'桌面收纳盒套装','https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=500&q=80',1,173.70,173.70,'2026-06-23 16:40:59','2026-06-23 16:40:59',0,'默认规格',0),(26,24,71,'高清投影仪','https://images.unsplash.com/photo-1601944179066-29786cb9d32a?auto=format&fit=crop&w=500&q=80',1,513.00,513.00,'2026-06-30 20:23:16','2026-06-30 20:23:16',0,'默认规格',1),(27,24,70,'智能手表青春版','https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=500&q=80',1,501.30,501.30,'2026-06-30 20:23:16','2026-06-30 20:23:16',0,'默认规格',0),(28,24,72,'家用咖啡机','https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=500&q=80',2,524.70,1049.40,'2026-06-30 20:23:16','2026-06-30 20:23:16',0,'默认规格',0),(29,25,72,'家用咖啡机','https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=500&q=80',1,524.70,524.70,'2026-06-30 20:33:34','2026-06-30 20:33:34',0,'默认规格',1),(30,25,71,'高清投影仪','https://images.unsplash.com/photo-1601944179066-29786cb9d32a?auto=format&fit=crop&w=500&q=80',1,513.00,513.00,'2026-06-30 20:33:34','2026-06-30 20:33:34',0,'默认规格',0),(31,25,70,'智能手表青春版','https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=500&q=80',1,501.30,501.30,'2026-06-30 20:33:34','2026-06-30 20:33:34',0,'默认规格',0),(32,25,69,'节日零食礼盒','https://images.unsplash.com/photo-1549465220-1a8b9238cd48?auto=format&fit=crop&w=500&q=80',1,489.60,489.60,'2026-06-30 20:33:34','2026-06-30 20:33:34',0,'默认规格',0);
/*!40000 ALTER TABLE `order_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `orders`
--

DROP TABLE IF EXISTS `orders`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `orders` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '订单编号',
  `order_no` varchar(50) NOT NULL COMMENT '订单号',
  `user_id` bigint(20) NOT NULL COMMENT '用户编号',
  `user_name` varchar(50) NOT NULL COMMENT '用户名称',
  `total_amount` decimal(10,2) NOT NULL COMMENT '订单总金额',
  `shipping_address` varchar(255) NOT NULL COMMENT '收货地址',
  `status` varchar(20) NOT NULL COMMENT '订单状态',
  `order_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `discount_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '优惠金额',
  `freight_amount` decimal(10,2) NOT NULL DEFAULT '0.00' COMMENT '运费',
  `pay_amount` decimal(10,2) DEFAULT NULL COMMENT '应付金额',
  `payment_method` varchar(50) DEFAULT NULL COMMENT '支付方式',
  `paid_time` datetime DEFAULT NULL COMMENT '支付时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `order_no` (`order_no`),
  KEY `idx_order_no` (`order_no`),
  KEY `idx_order_user` (`user_id`),
  KEY `idx_order_status` (`status`)
) ENGINE=InnoDB AUTO_INCREMENT=26 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='订单表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `orders`
--

LOCK TABLES `orders` WRITE;
/*!40000 ALTER TABLE `orders` DISABLE KEYS */;
INSERT INTO `orders` VALUES (1,'SO202606160001',2,'zhangsan',399.00,'上海市浦东新区世纪大道100号','已完成','2026-06-17 10:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,399.00,NULL,NULL),(2,'SO202606160002',3,'lisi',578.00,'广州市天河区体育西路88号','已完成','2026-06-17 15:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,578.00,NULL,NULL),(3,'SO202606170001',4,'wangwu',169.00,'深圳市南山区科技园9栋','待收货','2026-06-18 11:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,169.00,NULL,NULL),(4,'SO202606170002',5,'zhaoliu',658.00,'杭州市西湖区文三路66号','已完成','2026-06-18 18:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,658.00,NULL,NULL),(5,'SO202606180001',6,'sunqi',219.00,'成都市高新区天府三街12号','待发货','2026-06-19 09:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,219.00,NULL,NULL),(6,'SO202606180002',7,'zhouba',258.00,'南京市鼓楼区中山路28号','已取消','2026-06-19 16:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,258.00,NULL,NULL),(7,'SO202606190001',8,'wujie',149.00,'武汉市洪山区光谷大道77号','已完成','2026-06-20 10:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,149.00,NULL,NULL),(8,'SO202606190002',9,'chenchen',1798.00,'西安市雁塔区科技路18号','待收货','2026-06-20 20:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,1798.00,NULL,NULL),(9,'SO202606200001',2,'zhangsan',199.00,'上海市浦东新区世纪大道100号','待付款','2026-06-21 12:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,199.00,NULL,NULL),(10,'SO202606200002',3,'lisi',269.00,'广州市天河区体育西路88号','已完成','2026-06-21 19:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,269.00,NULL,NULL),(11,'SO202606210001',4,'wangwu',198.00,'深圳市南山区科技园9栋','待发货','2026-06-22 08:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,198.00,NULL,NULL),(12,'SO202606210002',5,'zhaoliu',176.00,'杭州市西湖区文三路66号','已完成','2026-06-22 15:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,176.00,NULL,NULL),(13,'SO202606210003',6,'sunqi',128.00,'成都市高新区天府三街12号','待收货','2026-06-22 21:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,128.00,NULL,NULL),(14,'SO202606220001',7,'zhouba',238.00,'南京市鼓楼区中山路28号','待付款','2026-06-23 09:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,238.00,NULL,NULL),(15,'SO202606220002',8,'wujie',59.00,'武汉市洪山区光谷大道77号','待发货','2026-06-23 10:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,59.00,NULL,NULL),(16,'SO202606220003',9,'chenchen',478.00,'西安市雁塔区科技路18号','已完成','2026-06-23 11:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,478.00,NULL,NULL),(17,'SO202606220004',2,'zhangsan',78.00,'上海市浦东新区世纪大道100号','待收货','2026-06-23 12:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,78.00,NULL,NULL),(18,'SO202606220005',3,'lisi',318.00,'广州市天河区体育西路88号','已完成','2026-06-23 13:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,318.00,NULL,NULL),(19,'SO202606220006',4,'wangwu',79.00,'深圳市南山区科技园9栋','待发货','2026-06-23 14:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,79.00,NULL,NULL),(20,'SO202606220007',5,'zhaoliu',178.00,'杭州市西湖区文三路66号','已取消','2026-06-23 15:00:00','2026-06-23 16:34:29','2026-06-23 16:34:29',0,0.00,0.00,178.00,NULL,NULL),(21,'SO202606231635061352',2,'用户2',45.00,'张三 13800000001 上海市浦东新区世纪大道100号','待发货','2026-06-23 16:35:07','2026-06-23 16:35:07','2026-06-23 16:35:26',0,0.00,8.00,53.00,'余额模拟支付','2026-06-23 16:35:26'),(22,'SO202606231638356331',11,'用户11',513.00,'23 15846852356 广东省广州市li-wenhao','已完成','2026-06-23 16:38:36','2026-06-23 16:38:36','2026-06-23 16:38:54',0,10.00,0.00,503.00,'余额模拟支付','2026-06-23 16:38:38'),(23,'SO202606231640594280',11,'用户11',1129.50,'23 15846852356 广东省广州市li-wenhao','已完成','2026-06-23 16:40:59','2026-06-23 16:40:59','2026-06-30 20:23:32',0,10.00,0.00,1119.50,'余额模拟支付','2026-06-23 16:41:01'),(24,'SO202606302023168285',11,'用户11',2063.70,'123 15384567512 广东省广州市1600 Amphitheatre Pkwy','已完成','2026-06-30 20:23:16','2026-06-30 20:23:16','2026-06-30 20:23:30',0,10.00,0.00,2053.70,'余额模拟支付','2026-06-30 20:23:22'),(25,'SO202606302033337318',12,'用户12',2028.60,'123 15368954682 广东省广州市li-wenhao','已完成','2026-06-30 20:33:34','2026-06-30 20:33:34','2026-06-30 20:33:54',0,10.00,0.00,2018.60,'余额模拟支付','2026-06-30 20:33:37');
/*!40000 ALTER TABLE `orders` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `payment_record`
--

DROP TABLE IF EXISTS `payment_record`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `payment_record` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `order_no` varchar(50) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `payment_method` varchar(50) DEFAULT NULL,
  `status` varchar(30) DEFAULT NULL,
  `pay_time` datetime DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=6 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='支付记录表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `payment_record`
--

LOCK TABLES `payment_record` WRITE;
/*!40000 ALTER TABLE `payment_record` DISABLE KEYS */;
INSERT INTO `payment_record` VALUES (1,'SO202606231635061352',2,53.00,'余额模拟支付','支付成功','2026-06-23 16:35:26','2026-06-23 16:35:26'),(2,'SO202606231638356331',11,503.00,'余额模拟支付','支付成功','2026-06-23 16:38:38','2026-06-23 16:38:38'),(3,'SO202606231640594280',11,1119.50,'余额模拟支付','支付成功','2026-06-23 16:41:01','2026-06-23 16:41:01'),(4,'SO202606302023168285',11,2053.70,'余额模拟支付','支付成功','2026-06-30 20:23:22','2026-06-30 20:23:22'),(5,'SO202606302033337318',12,2018.60,'余额模拟支付','支付成功','2026-06-30 20:33:37','2026-06-30 20:33:37');
/*!40000 ALTER TABLE `payment_record` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product`
--

DROP TABLE IF EXISTS `product`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '商品编号',
  `product_no` varchar(50) NOT NULL COMMENT '商品编码',
  `name` varchar(100) NOT NULL COMMENT '商品名称',
  `category_id` bigint(20) NOT NULL COMMENT '分类编号',
  `category_name` varchar(80) NOT NULL COMMENT '分类名称',
  `image_url` varchar(500) DEFAULT NULL COMMENT '商品图片',
  `price` decimal(10,2) NOT NULL COMMENT '商品价格',
  `stock` int(11) NOT NULL DEFAULT '0' COMMENT '库存数量',
  `description` varchar(500) DEFAULT NULL COMMENT '商品描述',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '上架状态：1上架 0下架',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  `brand_id` bigint(20) DEFAULT NULL COMMENT '品牌编号',
  `brand_name` varchar(100) DEFAULT NULL COMMENT '品牌名称',
  `detail_image_url` varchar(1000) DEFAULT NULL COMMENT '商品详情图',
  `market_price` decimal(10,2) DEFAULT NULL COMMENT '市场原价',
  `discount_price` decimal(10,2) DEFAULT NULL COMMENT '折扣价格',
  `sales` int(11) NOT NULL DEFAULT '0' COMMENT '销量',
  `rating` decimal(3,1) NOT NULL DEFAULT '5.0' COMMENT '评分',
  `review_count` int(11) NOT NULL DEFAULT '0' COMMENT '评价数量',
  `specs` varchar(1000) DEFAULT NULL COMMENT '商品规格',
  `is_special` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否今日特价',
  `is_recommended` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否推荐商品',
  `is_new` tinyint(4) NOT NULL DEFAULT '0' COMMENT '是否新品',
  PRIMARY KEY (`id`),
  UNIQUE KEY `product_no` (`product_no`),
  KEY `idx_product_category` (`category_id`),
  KEY `idx_product_name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=75 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product`
--

LOCK TABLES `product` WRITE;
/*!40000 ALTER TABLE `product` DISABLE KEYS */;
INSERT INTO `product` VALUES (1,'SP10001','蓝牙降噪耳机',1,'数码科技','https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=500&q=80',399.00,26,'主动降噪，长续航，适合通勤与学习。',1,'2026-06-23 16:34:29','2026-06-30 20:25:12',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=500&q=80',498.75,399.00,31,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(2,'SP10002','机械键盘',1,'数码科技','https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500&q=80',289.00,8,'青轴手感，白色背光，桌面办公游戏两用。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500&q=80',361.25,289.00,32,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(3,'SP10003','27英寸显示器',1,'数码科技','https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=500&q=80',1299.00,15,'2K 高清屏幕，低蓝光护眼。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=500&q=80',1623.75,1299.00,33,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,1,0),(4,'SP10004','智能手机支架',1,'数码科技','https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=500&q=80',59.00,42,'铝合金可折叠支架，多角度调节。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=500&q=80',73.75,59.00,34,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(5,'SP10005','简约连帽卫衣',13,'服饰鞋包','https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=500&q=80',169.00,35,'柔软亲肤面料，适合春秋日常穿搭。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=500&q=80',211.25,169.00,35,4.6,12,'颜色: 黑色/白色; 版本: 标准版',1,0,0),(6,'SP10006','轻量运动鞋',13,'服饰鞋包','https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80',329.00,18,'缓震鞋底，轻量透气。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80',411.25,329.00,36,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,1,0),(7,'SP10007','城市通勤背包',13,'服饰鞋包','https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80',219.00,22,'多隔层设计，可容纳 15 英寸电脑。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80',273.75,219.00,37,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(8,'SP10008','皮革钱包',13,'服饰鞋包','https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=500&q=80',129.00,9,'简洁卡位设计，手感细腻。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=500&q=80',161.25,129.00,38,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(9,'SP10009','北欧台灯',3,'家居生活','https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500&q=80',149.00,14,'暖光护眼，适合卧室与书桌。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500&q=80',186.25,149.00,39,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,1,0),(10,'SP10010','人体工学椅',3,'家居生活','https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=500&q=80',899.00,7,'腰托可调，长时间办公更舒适。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=500&q=80',1123.75,899.00,40,4.6,12,'颜色: 黑色/白色; 版本: 标准版',1,0,0),(11,'SP10011','桌面收纳盒',3,'家居生活','https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=500&q=80',49.00,56,'分区收纳，保持桌面整洁。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=500&q=80',61.25,49.00,41,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(12,'SP10012','香薰蜡烛',3,'家居生活','https://images.unsplash.com/photo-1602874801006-e26c8d89ba20?auto=format&fit=crop&w=500&q=80',79.00,31,'清新木质香调，营造放松氛围。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1602874801006-e26c8d89ba20?auto=format&fit=crop&w=500&q=80',98.75,79.00,42,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,1,0),(13,'SP10013','保湿精华液',4,'美妆护肤','https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=500&q=80',199.00,25,'补水保湿，质地清爽。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=500&q=80',248.75,199.00,43,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(14,'SP10014','淡香水',4,'美妆护肤','https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=500&q=80',269.00,12,'清新花果香，适合日常通勤。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=500&q=80',336.25,269.00,44,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(15,'SP10015','润色口红',4,'美妆护肤','https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=500&q=80',99.00,40,'显色自然，滋润不拔干。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=500&q=80',123.75,99.00,45,4.6,12,'颜色: 黑色/白色; 版本: 标准版',1,1,0),(16,'SP10016','精品咖啡豆',5,'食品饮料','https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=500&q=80',88.00,33,'中深烘焙，坚果可可风味。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=500&q=80',110.00,88.00,46,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(17,'SP10017','坚果礼盒',5,'食品饮料','https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&w=500&q=80',128.00,20,'多种坚果组合，独立小包装。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&w=500&q=80',160.00,128.00,47,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(18,'SP10018','气泡饮料组合',5,'食品饮料','https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=500&q=80',69.00,6,'低糖清爽口味，适合聚会囤货。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=500&q=80',86.25,69.00,48,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,1,0),(19,'SP10019','瑜伽垫',6,'运动户外','https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?auto=format&fit=crop&w=500&q=80',119.00,28,'防滑加厚，适合居家训练。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?auto=format&fit=crop&w=500&q=80',148.75,119.00,49,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(20,'SP10020','运动水杯',6,'运动户外','https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=500&q=80',59.00,45,'大容量便携，户外健身适用。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=500&q=80',73.75,59.00,50,4.6,12,'颜色: 黑色/白色; 版本: 标准版',1,0,1),(21,'SP10021','户外双肩包',6,'运动户外','https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=500&q=80',239.00,16,'轻量防泼水，多场景出行。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=500&q=80',298.75,239.00,51,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,1,1),(22,'SP10022','婴儿湿巾',7,'母婴用品','https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=500&q=80',39.00,60,'柔软亲肤，无香型。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=500&q=80',48.75,39.00,52,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,1),(23,'SP10023','儿童积木套装',7,'母婴用品','https://images.unsplash.com/photo-1560961911-ba7ef651a56c?auto=format&fit=crop&w=500&q=80',159.00,21,'启蒙拼搭，安全圆角。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1560961911-ba7ef651a56c?auto=format&fit=crop&w=500&q=80',198.75,159.00,53,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,1),(24,'SP10024','保温奶瓶',7,'母婴用品','https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=500&q=80',89.00,13,'宽口径设计，便于清洗。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=500&q=80',111.25,89.00,54,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,1,1),(25,'SP10025','无线鼠标',9,'图书文具','https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=500&q=80',79.00,38,'静音按键，办公学习适用。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=500&q=80',98.75,79.00,55,4.6,12,'颜色: 黑色/白色; 版本: 标准版',1,0,1),(26,'SP10026','商务笔记本',9,'图书文具','https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=500&q=80',29.00,80,'加厚纸张，书写顺滑。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=500&q=80',36.25,29.00,56,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,1),(27,'SP10027','桌面文件架',9,'图书文具','https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=500&q=80',66.00,17,'多层分类，提升办公效率。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=500&q=80',82.50,66.00,57,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,1,1),(28,'SP10028','算法入门图书',9,'图书文具','https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=500&q=80',75.00,32,'适合计算机专业学生入门算法。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=500&q=80',93.75,75.00,58,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,1),(29,'SP10029','数据库系统教材',9,'图书文具','https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=500&q=80',89.00,24,'覆盖关系模型、SQL、事务与索引。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=500&q=80',111.25,89.00,59,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,1),(30,'SP10030','复古蓝牙音箱',1,'数码科技','https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=500&q=80',259.00,5,'复古外观，蓝牙连接，低音增强。',0,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=500&q=80',323.75,259.00,60,4.6,12,'颜色: 黑色/白色; 版本: 标准版',1,1,1),(31,'SP20031','轻薄智能手机',11,'手机通讯','https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=500&q=80',50.00,49,'轻薄智能手机，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:35:07',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=500&q=80',62.50,45.00,101,4.6,10,'颜色: 黑色/白色; 版本: 标准版',1,1,1),(32,'SP20032','快充移动电源',11,'手机通讯','https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?auto=format&fit=crop&w=500&q=80',63.00,51,'快充移动电源，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,2,'Mori Home 家居美学','https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?auto=format&fit=crop&w=500&q=80',78.75,56.70,107,4.6,11,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(33,'SP20033','磁吸手机支架',11,'手机通讯','https://images.unsplash.com/photo-1512499617640-c2f999098c01?auto=format&fit=crop&w=500&q=80',76.00,52,'磁吸手机支架，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1512499617640-c2f999098c01?auto=format&fit=crop&w=500&q=80',95.00,68.40,114,4.6,12,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(34,'SP20034','无线充电底座',11,'手机通讯','https://images.unsplash.com/photo-1615529182904-14819c35db37?auto=format&fit=crop&w=500&q=80',89.00,53,'无线充电底座，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1615529182904-14819c35db37?auto=format&fit=crop&w=500&q=80',111.25,80.10,121,4.6,13,'颜色: 黑色/白色; 版本: 标准版',0,1,0),(35,'SP20035','降噪通话耳机',11,'手机通讯','https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=500&q=80',102.00,54,'降噪通话耳机，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=500&q=80',127.50,91.80,128,4.6,14,'颜色: 黑色/白色; 版本: 标准版',1,0,0),(36,'SP20036','便携蓝牙音箱',12,'电脑办公','https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=500&q=80',115.00,55,'便携蓝牙音箱，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,2,'Mori Home 家居美学','https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=500&q=80',143.75,103.50,135,4.6,15,'颜色: 黑色/白色; 版本: 标准版',0,0,1),(37,'SP20037','电竞显示器',12,'电脑办公','https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=500&q=80',128.00,56,'电竞显示器，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=500&q=80',160.00,115.20,142,4.6,16,'颜色: 黑色/白色; 版本: 标准版',0,1,0),(38,'SP20038','人体工学键盘',12,'电脑办公','https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500&q=80',141.00,57,'人体工学键盘，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500&q=80',176.25,126.90,149,4.6,17,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(39,'SP20039','静音无线鼠标',12,'电脑办公','https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=500&q=80',154.00,58,'静音无线鼠标，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=500&q=80',192.50,138.60,156,4.6,18,'颜色: 黑色/白色; 版本: 标准版',1,0,0),(40,'SP20040','办公笔记本支架',12,'电脑办公','https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=500&q=80',167.00,59,'办公笔记本支架，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,2,'Mori Home 家居美学','https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=500&q=80',208.75,150.30,163,4.6,19,'颜色: 黑色/白色; 版本: 标准版',0,1,0),(41,'SP20041','护眼台灯Pro',14,'宠物用品','https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500&q=80',180.00,60,'护眼台灯Pro，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500&q=80',225.00,162.00,170,4.6,20,'颜色: 黑色/白色; 版本: 标准版',0,0,1),(42,'SP20042','桌面收纳盒套装',14,'宠物用品','https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=500&q=80',193.00,60,'桌面收纳盒套装，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:40:59',0,4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=500&q=80',241.25,173.70,178,4.6,21,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(43,'SP20043','陶瓷保温杯',15,'礼品专区','https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=500&q=80',206.00,62,'陶瓷保温杯，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=500&q=80',257.50,185.40,184,4.6,22,'颜色: 黑色/白色; 版本: 标准版',1,1,0),(44,'SP20044','香薰蜡烛礼盒',15,'礼品专区','https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?auto=format&fit=crop&w=500&q=80',219.00,63,'香薰蜡烛礼盒，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,2,'Mori Home 家居美学','https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?auto=format&fit=crop&w=500&q=80',273.75,197.10,191,4.6,23,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(45,'SP20045','轻跑运动鞋',13,'服饰鞋包','https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80',232.00,64,'轻跑运动鞋，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80',290.00,208.80,198,4.6,24,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(46,'SP20046','宽松连帽卫衣',13,'服饰鞋包','https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=500&q=80',245.00,65,'宽松连帽卫衣，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=500&q=80',306.25,220.50,205,4.6,25,'颜色: 黑色/白色; 版本: 标准版',0,1,1),(47,'SP20047','城市双肩包',13,'服饰鞋包','https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80',258.00,66,'城市双肩包，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80',322.50,232.20,212,4.6,26,'颜色: 黑色/白色; 版本: 标准版',1,0,0),(48,'SP20048','棒球帽',13,'服饰鞋包','https://images.unsplash.com/photo-1521369909029-2afed882baee?auto=format&fit=crop&w=500&q=80',271.00,67,'棒球帽，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,2,'Mori Home 家居美学','https://images.unsplash.com/photo-1521369909029-2afed882baee?auto=format&fit=crop&w=500&q=80',338.75,243.90,219,4.6,27,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(49,'SP20049','护肤套装',4,'美妆护肤','https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=500&q=80',284.00,68,'护肤套装，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=500&q=80',355.00,255.60,226,4.6,28,'颜色: 黑色/白色; 版本: 标准版',0,1,0),(50,'SP20050','清新淡香水',4,'美妆护肤','https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=500&q=80',297.00,69,'清新淡香水，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=500&q=80',371.25,267.30,233,4.6,29,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(51,'SP20051','保湿面膜礼盒',4,'美妆护肤','https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=500&q=80',310.00,70,'保湿面膜礼盒，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=500&q=80',387.50,279.00,240,4.6,30,'颜色: 黑色/白色; 版本: 标准版',1,0,1),(52,'SP20052','咖啡豆礼盒',5,'食品饮料','https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=500&q=80',323.00,71,'咖啡豆礼盒，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,2,'Mori Home 家居美学','https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=500&q=80',403.75,290.70,247,4.6,31,'颜色: 黑色/白色; 版本: 标准版',0,1,0),(53,'SP20053','零食大礼包',5,'食品饮料','https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&w=500&q=80',336.00,72,'零食大礼包，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&w=500&q=80',420.00,302.40,254,4.6,32,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(54,'SP20054','气泡饮料组合',5,'食品饮料','https://images.unsplash.com/photo-1563227812-0ea4c22e6cc8?auto=format&fit=crop&w=500&q=80',349.00,73,'气泡饮料组合，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1563227812-0ea4c22e6cc8?auto=format&fit=crop&w=500&q=80',436.25,314.10,261,4.6,33,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(55,'SP20055','专业瑜伽垫',6,'运动户外','https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?auto=format&fit=crop&w=500&q=80',362.00,74,'专业瑜伽垫，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?auto=format&fit=crop&w=500&q=80',452.50,325.80,268,4.6,34,'颜色: 黑色/白色; 版本: 标准版',1,1,0),(56,'SP20056','室内篮球',6,'运动户外','https://images.unsplash.com/photo-1546519638-68e109498ffc?auto=format&fit=crop&w=500&q=80',375.00,75,'室内篮球，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,2,'Mori Home 家居美学','https://images.unsplash.com/photo-1546519638-68e109498ffc?auto=format&fit=crop&w=500&q=80',468.75,337.50,275,4.6,35,'颜色: 黑色/白色; 版本: 标准版',0,0,1),(57,'SP20057','户外保温壶',6,'运动户外','https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=500&q=80',388.00,76,'户外保温壶，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=500&q=80',485.00,349.20,282,4.6,36,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(58,'SP20058','算法入门图书',9,'图书文具','https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=500&q=80',401.00,77,'算法入门图书，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=500&q=80',501.25,360.90,289,4.6,37,'颜色: 黑色/白色; 版本: 标准版',0,1,0),(59,'SP20059','文具礼盒',9,'图书文具','https://images.unsplash.com/photo-1456735190827-d1262f71b8a3?auto=format&fit=crop&w=500&q=80',414.00,78,'文具礼盒，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1456735190827-d1262f71b8a3?auto=format&fit=crop&w=500&q=80',517.50,372.60,296,4.6,38,'颜色: 黑色/白色; 版本: 标准版',1,0,0),(60,'SP20060','效率笔记本',9,'图书文具','https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=500&q=80',427.00,79,'效率笔记本，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,2,'Mori Home 家居美学','https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=500&q=80',533.75,384.30,303,4.6,39,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(61,'SP20061','婴儿湿巾箱装',7,'母婴用品','https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=500&q=80',440.00,80,'婴儿湿巾箱装，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=500&q=80',550.00,396.00,310,4.6,40,'颜色: 黑色/白色; 版本: 标准版',0,1,1),(62,'SP20062','儿童积木套装',7,'母婴用品','https://images.unsplash.com/photo-1560961911-ba7ef651a56c?auto=format&fit=crop&w=500&q=80',453.00,81,'儿童积木套装，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1560961911-ba7ef651a56c?auto=format&fit=crop&w=500&q=80',566.25,407.70,317,4.6,41,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(63,'SP20063','婴儿安抚玩具',7,'母婴用品','https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=500&q=80',466.00,82,'婴儿安抚玩具，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=500&q=80',582.50,419.40,324,4.6,42,'颜色: 黑色/白色; 版本: 标准版',1,0,0),(64,'SP20064','宠物磨牙玩具',14,'宠物用品','https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=500&q=80',479.00,82,'宠物磨牙玩具，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:40:59',0,2,'Mori Home 家居美学','https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=500&q=80',598.75,431.10,332,4.6,43,'颜色: 黑色/白色; 版本: 标准版',0,1,0),(65,'SP20065','宠物外出包',14,'宠物用品','https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?auto=format&fit=crop&w=500&q=80',492.00,84,'宠物外出包，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?auto=format&fit=crop&w=500&q=80',615.00,442.80,338,4.6,44,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(66,'SP20066','宠物清洁套装',14,'宠物用品','https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=500&q=80',505.00,85,'宠物清洁套装，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=500&q=80',631.25,454.50,345,4.6,45,'颜色: 黑色/白色; 版本: 标准版',0,0,1),(67,'SP20067','50元节日礼品卡',15,'礼品专区','https://images.unsplash.com/photo-1513201099705-a9746e1e201f?auto=format&fit=crop&w=500&q=80',518.00,86,'50元节日礼品卡，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1513201099705-a9746e1e201f?auto=format&fit=crop&w=500&q=80',647.50,466.20,352,4.6,46,'颜色: 黑色/白色; 版本: 标准版',1,1,0),(68,'SP20068','100元生日礼品卡',15,'礼品专区','https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?auto=format&fit=crop&w=500&q=80',531.00,87,'100元生日礼品卡，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-23 16:34:29',0,2,'Mori Home 家居美学','https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?auto=format&fit=crop&w=500&q=80',663.75,477.90,359,4.6,47,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(69,'SP20069','节日零食礼盒',15,'礼品专区','https://images.unsplash.com/photo-1549465220-1a8b9238cd48?auto=format&fit=crop&w=500&q=80',544.00,87,'节日零食礼盒，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-30 20:33:34',0,3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1549465220-1a8b9238cd48?auto=format&fit=crop&w=500&q=80',680.00,489.60,367,4.6,48,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(70,'SP20070','智能手表青春版',1,'数码科技','https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=500&q=80',557.00,87,'智能手表青春版，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-30 20:33:34',0,4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=500&q=80',696.25,501.30,375,4.6,49,'颜色: 黑色/白色; 版本: 标准版',0,1,0),(71,'SP20071','高清投影仪',1,'数码科技','https://images.unsplash.com/photo-1601944179066-29786cb9d32a?auto=format&fit=crop&w=500&q=80',570.00,87,'高清投影仪，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-30 20:33:34',0,1,'NovaTech 数码生活','https://images.unsplash.com/photo-1601944179066-29786cb9d32a?auto=format&fit=crop&w=500&q=80',712.50,513.00,383,5.0,2,'颜色: 黑色/白色; 版本: 标准版',1,0,1),(72,'SP20072','家用咖啡机',3,'家居生活','https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=500&q=80',583.00,87,'家用咖啡机，现代电商精选商品，适合课程设计演示。',1,'2026-06-23 16:34:29','2026-06-30 20:33:34',0,2,'Mori Home 家居美学','https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=500&q=80',728.75,524.70,391,4.5,2,'颜色: 黑色/白色; 版本: 标准版',0,0,0),(73,'011','哑铃',16,'健身','',900.00,0,'',1,'2026-06-30 20:17:08','2026-07-11 10:47:13',1,NULL,NULL,'',900.00,900.00,0,5.0,0,NULL,0,0,0),(74,'123','机甲',1,'数码科技','',100.00,1,'',1,'2026-06-30 20:26:13','2026-07-11 10:47:13',0,NULL,NULL,'',100.00,100.00,0,5.0,0,NULL,0,0,0);
/*!40000 ALTER TABLE `product` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_category`
--

DROP TABLE IF EXISTS `product_category`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_category` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '分类编号',
  `name` varchar(80) NOT NULL COMMENT '分类名称',
  `description` varchar(255) DEFAULT NULL COMMENT '分类描述',
  `sort_no` int(11) NOT NULL DEFAULT '0' COMMENT '排序号',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '状态：1启用 0禁用',
  `image_url` varchar(500) DEFAULT NULL COMMENT '分类封面',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除',
  PRIMARY KEY (`id`),
  UNIQUE KEY `name` (`name`)
) ENGINE=InnoDB AUTO_INCREMENT=17 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品分类表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_category`
--

LOCK TABLES `product_category` WRITE;
/*!40000 ALTER TABLE `product_category` DISABLE KEYS */;
INSERT INTO `product_category` VALUES (1,'数码科技','手机、耳机、键盘、显示器等数码设备',1,1,'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-30 20:15:53',0),(2,'时尚服饰','卫衣、外套、运动鞋与日常穿搭',2,1,'https://images.unsplash.com/photo-1445205170230-053b83016050?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(3,'家居生活','台灯、椅子、香薰、收纳等家居用品',3,1,'https://images.unsplash.com/photo-1513161455079-7dc1de15ef3e?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(4,'美妆护肤','护肤品、香水、彩妆和个人护理',4,1,'https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(5,'食品饮料','咖啡、零食、饮品与健康食品',5,1,'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(6,'运动户外','健身装备、户外用品和运动配件',6,1,'https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(7,'母婴用品','母婴护理、玩具和儿童用品',7,1,'https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(8,'办公文具','办公设备、笔记本和桌面用品',8,1,'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(9,'箱包配饰','背包、钱包、配饰和出行用品',9,1,'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(10,'图书音像','教材、畅销书、影音与学习资料',10,1,'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(11,'手机通讯','手机、充电器、支架和移动配件',11,1,'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(12,'电脑办公','显示器、键盘、鼠标和办公设备',12,1,'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(13,'服饰鞋包','运动鞋、卫衣、背包与潮流配饰',13,1,'https://images.unsplash.com/photo-1445205170230-053b83016050?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(14,'宠物用品','宠物玩具、清洁和日常护理用品',14,1,'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(15,'礼品专区','礼品卡、节日礼盒与生日礼品',15,1,'https://images.unsplash.com/photo-1513885535751-8b9238bd345a?auto=format&fit=crop&w=600&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(16,'健身','',0,1,'','2026-06-30 20:16:08','2026-06-30 20:16:08',0);
/*!40000 ALTER TABLE `product_category` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_image`
--

DROP TABLE IF EXISTS `product_image`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_image` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `image_url` varchar(1000) NOT NULL,
  `sort_no` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=200 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品图片表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_image`
--

LOCK TABLES `product_image` WRITE;
/*!40000 ALTER TABLE `product_image` DISABLE KEYS */;
INSERT INTO `product_image` VALUES (1,1,'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=500&q=80',1),(2,2,'https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500&q=80',1),(3,3,'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=500&q=80',1),(4,4,'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=500&q=80',1),(5,5,'https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=500&q=80',1),(6,6,'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80',1),(7,7,'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80',1),(8,8,'https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=500&q=80',1),(9,9,'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500&q=80',1),(10,10,'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=500&q=80',1),(11,11,'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=500&q=80',1),(12,12,'https://images.unsplash.com/photo-1602874801006-e26c8d89ba20?auto=format&fit=crop&w=500&q=80',1),(13,13,'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=500&q=80',1),(14,14,'https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=500&q=80',1),(15,15,'https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=500&q=80',1),(16,16,'https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=500&q=80',1),(17,17,'https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&w=500&q=80',1),(18,18,'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=500&q=80',1),(19,19,'https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?auto=format&fit=crop&w=500&q=80',1),(20,20,'https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=500&q=80',1),(21,21,'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=500&q=80',1),(22,22,'https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=500&q=80',1),(23,23,'https://images.unsplash.com/photo-1560961911-ba7ef651a56c?auto=format&fit=crop&w=500&q=80',1),(24,24,'https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=500&q=80',1),(25,25,'https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=500&q=80',1),(26,26,'https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=500&q=80',1),(27,27,'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=500&q=80',1),(28,28,'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=500&q=80',1),(29,29,'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=500&q=80',1),(30,30,'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=500&q=80',1),(31,31,'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=500&q=80',1),(32,32,'https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?auto=format&fit=crop&w=500&q=80',1),(33,33,'https://images.unsplash.com/photo-1512499617640-c2f999098c01?auto=format&fit=crop&w=500&q=80',1),(34,34,'https://images.unsplash.com/photo-1615529182904-14819c35db37?auto=format&fit=crop&w=500&q=80',1),(35,35,'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=500&q=80',1),(36,36,'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=500&q=80',1),(37,37,'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=500&q=80',1),(38,38,'https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500&q=80',1),(39,39,'https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=500&q=80',1),(40,40,'https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=500&q=80',1),(41,41,'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500&q=80',1),(42,42,'https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=500&q=80',1),(43,43,'https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=500&q=80',1),(44,44,'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?auto=format&fit=crop&w=500&q=80',1),(45,45,'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80',1),(46,46,'https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=500&q=80',1),(47,47,'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80',1),(48,48,'https://images.unsplash.com/photo-1521369909029-2afed882baee?auto=format&fit=crop&w=500&q=80',1),(49,49,'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=500&q=80',1),(50,50,'https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=500&q=80',1),(51,51,'https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=500&q=80',1),(52,52,'https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=500&q=80',1),(53,53,'https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&w=500&q=80',1),(54,54,'https://images.unsplash.com/photo-1563227812-0ea4c22e6cc8?auto=format&fit=crop&w=500&q=80',1),(55,55,'https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?auto=format&fit=crop&w=500&q=80',1),(56,56,'https://images.unsplash.com/photo-1546519638-68e109498ffc?auto=format&fit=crop&w=500&q=80',1),(57,57,'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=500&q=80',1),(58,58,'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=500&q=80',1),(59,59,'https://images.unsplash.com/photo-1456735190827-d1262f71b8a3?auto=format&fit=crop&w=500&q=80',1),(60,60,'https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=500&q=80',1),(61,61,'https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=500&q=80',1),(62,62,'https://images.unsplash.com/photo-1560961911-ba7ef651a56c?auto=format&fit=crop&w=500&q=80',1),(63,63,'https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=500&q=80',1),(64,64,'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=500&q=80',1),(65,65,'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?auto=format&fit=crop&w=500&q=80',1),(66,66,'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=500&q=80',1),(67,67,'https://images.unsplash.com/photo-1513201099705-a9746e1e201f?auto=format&fit=crop&w=500&q=80',1),(68,68,'https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?auto=format&fit=crop&w=500&q=80',1),(69,69,'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?auto=format&fit=crop&w=500&q=80',1),(70,70,'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=500&q=80',1),(71,71,'https://images.unsplash.com/photo-1601944179066-29786cb9d32a?auto=format&fit=crop&w=500&q=80',1),(72,72,'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=500&q=80',1),(128,1,'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=500&q=80',2),(129,2,'https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500&q=80',2),(130,3,'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=500&q=80',2),(131,4,'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=500&q=80',2),(132,5,'https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=500&q=80',2),(133,6,'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80',2),(134,7,'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80',2),(135,8,'https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=500&q=80',2),(136,9,'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500&q=80',2),(137,10,'https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=500&q=80',2),(138,11,'https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=500&q=80',2),(139,12,'https://images.unsplash.com/photo-1602874801006-e26c8d89ba20?auto=format&fit=crop&w=500&q=80',2),(140,13,'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=500&q=80',2),(141,14,'https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=500&q=80',2),(142,15,'https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=500&q=80',2),(143,16,'https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=500&q=80',2),(144,17,'https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&w=500&q=80',2),(145,18,'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=500&q=80',2),(146,19,'https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?auto=format&fit=crop&w=500&q=80',2),(147,20,'https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=500&q=80',2),(148,21,'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=500&q=80',2),(149,22,'https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=500&q=80',2),(150,23,'https://images.unsplash.com/photo-1560961911-ba7ef651a56c?auto=format&fit=crop&w=500&q=80',2),(151,24,'https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=500&q=80',2),(152,25,'https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=500&q=80',2),(153,26,'https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=500&q=80',2),(154,27,'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=500&q=80',2),(155,28,'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=500&q=80',2),(156,29,'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=500&q=80',2),(157,30,'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=500&q=80',2),(158,31,'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=500&q=80',2),(159,32,'https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?auto=format&fit=crop&w=500&q=80',2),(160,33,'https://images.unsplash.com/photo-1512499617640-c2f999098c01?auto=format&fit=crop&w=500&q=80',2),(161,34,'https://images.unsplash.com/photo-1615529182904-14819c35db37?auto=format&fit=crop&w=500&q=80',2),(162,35,'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=500&q=80',2),(163,36,'https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=500&q=80',2),(164,37,'https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=500&q=80',2),(165,38,'https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500&q=80',2),(166,39,'https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=500&q=80',2),(167,40,'https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=500&q=80',2),(168,41,'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500&q=80',2),(169,42,'https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=500&q=80',2),(170,43,'https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=500&q=80',2),(171,44,'https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?auto=format&fit=crop&w=500&q=80',2),(172,45,'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80',2),(173,46,'https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=500&q=80',2),(174,47,'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80',2),(175,48,'https://images.unsplash.com/photo-1521369909029-2afed882baee?auto=format&fit=crop&w=500&q=80',2),(176,49,'https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=500&q=80',2),(177,50,'https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=500&q=80',2),(178,51,'https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=500&q=80',2),(179,52,'https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=500&q=80',2),(180,53,'https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&w=500&q=80',2),(181,54,'https://images.unsplash.com/photo-1563227812-0ea4c22e6cc8?auto=format&fit=crop&w=500&q=80',2),(182,55,'https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?auto=format&fit=crop&w=500&q=80',2),(183,56,'https://images.unsplash.com/photo-1546519638-68e109498ffc?auto=format&fit=crop&w=500&q=80',2),(184,57,'https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=500&q=80',2),(185,58,'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=500&q=80',2),(186,59,'https://images.unsplash.com/photo-1456735190827-d1262f71b8a3?auto=format&fit=crop&w=500&q=80',2),(187,60,'https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=500&q=80',2),(188,61,'https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=500&q=80',2),(189,62,'https://images.unsplash.com/photo-1560961911-ba7ef651a56c?auto=format&fit=crop&w=500&q=80',2),(190,63,'https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=500&q=80',2),(191,64,'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=500&q=80',2),(192,65,'https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?auto=format&fit=crop&w=500&q=80',2),(193,66,'https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=500&q=80',2),(194,67,'https://images.unsplash.com/photo-1513201099705-a9746e1e201f?auto=format&fit=crop&w=500&q=80',2),(195,68,'https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?auto=format&fit=crop&w=500&q=80',2),(196,69,'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?auto=format&fit=crop&w=500&q=80',2),(197,70,'https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=500&q=80',2),(198,71,'https://images.unsplash.com/photo-1601944179066-29786cb9d32a?auto=format&fit=crop&w=500&q=80',2),(199,72,'https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=500&q=80',2);
/*!40000 ALTER TABLE `product_image` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_review`
--

DROP TABLE IF EXISTS `product_review`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_review` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `order_id` bigint(20) DEFAULT NULL,
  `order_item_id` bigint(20) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  `username` varchar(50) DEFAULT NULL,
  `avatar` varchar(500) DEFAULT NULL,
  `rating` int(11) DEFAULT NULL,
  `content` varchar(1000) DEFAULT NULL,
  `image_url` varchar(1000) DEFAULT NULL,
  `anonymous` tinyint(4) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=19 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品评价表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_review`
--

LOCK TABLES `product_review` WRITE;
/*!40000 ALTER TABLE `product_review` DISABLE KEYS */;
INSERT INTO `product_review` VALUES (1,1,1,1,2,'zhangsan',NULL,5,'耳机降噪效果不错，通勤很实用。',NULL,0,'2026-06-23 16:34:29'),(2,2,2,2,3,'lisi',NULL,5,'键盘手感清晰，适合写代码。',NULL,0,'2026-06-23 16:34:29'),(3,35,3,3,4,'匿名用户',NULL,4,'包装完整，音质符合预期。',NULL,1,'2026-06-23 16:34:29'),(4,37,4,4,5,'zhaoliu',NULL,5,'显示器色彩不错，刷新率够用。',NULL,0,'2026-06-23 16:34:29'),(5,41,5,5,6,'sunqi',NULL,5,'台灯很亮，学习使用舒服。',NULL,0,'2026-06-23 16:34:29'),(6,45,6,6,7,'zhouba',NULL,5,'运动鞋轻便，尺码标准。',NULL,0,'2026-06-23 16:34:29'),(7,49,7,7,8,'wujie',NULL,5,'护肤套装清爽不油腻。',NULL,0,'2026-06-23 16:34:29'),(8,52,8,8,9,'chenchen',NULL,4,'咖啡香气不错，礼盒很适合送人。',NULL,0,'2026-06-23 16:34:29'),(9,55,9,9,2,'zhangsan',NULL,5,'瑜伽垫防滑，厚度刚好。',NULL,0,'2026-06-23 16:34:29'),(10,58,10,10,3,'lisi',NULL,5,'图书内容适合入门复习。',NULL,0,'2026-06-23 16:34:29'),(11,61,11,11,4,'wangwu',NULL,4,'湿巾温和，家庭使用方便。',NULL,0,'2026-06-23 16:34:29'),(12,64,12,12,5,'zhaoliu',NULL,5,'宠物玩具很耐咬。',NULL,0,'2026-06-23 16:34:29'),(13,67,13,13,6,'sunqi',NULL,5,'礼品卡兑换码生成很快。',NULL,0,'2026-06-23 16:34:29'),(14,70,14,14,7,'zhouba',NULL,5,'智能手表功能够用，续航不错。',NULL,0,'2026-06-23 16:34:29'),(15,72,15,15,8,'wujie',NULL,4,'咖啡机操作简单。',NULL,0,'2026-06-23 16:34:29'),(16,71,22,22,11,'匿名用户','https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=240&q=80',5,'czsc',NULL,1,'2026-06-23 16:44:44'),(17,71,24,26,11,'匿名用户','https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=240&q=80',5,'666',NULL,1,'2026-06-30 20:23:44'),(18,72,25,29,12,'匿名用户','https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=240&q=80',5,'112',NULL,1,'2026-07-11 11:17:56');
/*!40000 ALTER TABLE `product_review` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `product_sku`
--

DROP TABLE IF EXISTS `product_sku`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `product_sku` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `product_id` bigint(20) NOT NULL,
  `sku_name` varchar(100) DEFAULT NULL,
  `spec_info` varchar(255) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `stock` int(11) DEFAULT '0',
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=73 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='商品规格表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `product_sku`
--

LOCK TABLES `product_sku` WRITE;
/*!40000 ALTER TABLE `product_sku` DISABLE KEYS */;
INSERT INTO `product_sku` VALUES (1,1,'标准版','默认规格; 颜色: 黑色/白色',399.00,26),(2,2,'标准版','默认规格; 颜色: 黑色/白色',289.00,8),(3,3,'标准版','默认规格; 颜色: 黑色/白色',1299.00,15),(4,4,'标准版','默认规格; 颜色: 黑色/白色',59.00,42),(5,5,'标准版','默认规格; 颜色: 黑色/白色',169.00,35),(6,6,'标准版','默认规格; 颜色: 黑色/白色',329.00,18),(7,7,'标准版','默认规格; 颜色: 黑色/白色',219.00,22),(8,8,'标准版','默认规格; 颜色: 黑色/白色',129.00,9),(9,9,'标准版','默认规格; 颜色: 黑色/白色',149.00,14),(10,10,'标准版','默认规格; 颜色: 黑色/白色',899.00,7),(11,11,'标准版','默认规格; 颜色: 黑色/白色',49.00,56),(12,12,'标准版','默认规格; 颜色: 黑色/白色',79.00,31),(13,13,'标准版','默认规格; 颜色: 黑色/白色',199.00,25),(14,14,'标准版','默认规格; 颜色: 黑色/白色',269.00,12),(15,15,'标准版','默认规格; 颜色: 黑色/白色',99.00,40),(16,16,'标准版','默认规格; 颜色: 黑色/白色',88.00,33),(17,17,'标准版','默认规格; 颜色: 黑色/白色',128.00,20),(18,18,'标准版','默认规格; 颜色: 黑色/白色',69.00,6),(19,19,'标准版','默认规格; 颜色: 黑色/白色',119.00,28),(20,20,'标准版','默认规格; 颜色: 黑色/白色',59.00,45),(21,21,'标准版','默认规格; 颜色: 黑色/白色',239.00,16),(22,22,'标准版','默认规格; 颜色: 黑色/白色',39.00,60),(23,23,'标准版','默认规格; 颜色: 黑色/白色',159.00,21),(24,24,'标准版','默认规格; 颜色: 黑色/白色',89.00,13),(25,25,'标准版','默认规格; 颜色: 黑色/白色',79.00,38),(26,26,'标准版','默认规格; 颜色: 黑色/白色',29.00,80),(27,27,'标准版','默认规格; 颜色: 黑色/白色',66.00,17),(28,28,'标准版','默认规格; 颜色: 黑色/白色',75.00,32),(29,29,'标准版','默认规格; 颜色: 黑色/白色',89.00,24),(30,30,'标准版','默认规格; 颜色: 黑色/白色',259.00,5),(31,31,'标准版','默认规格; 颜色: 黑色/白色',45.00,50),(32,32,'标准版','默认规格; 颜色: 黑色/白色',56.70,51),(33,33,'标准版','默认规格; 颜色: 黑色/白色',68.40,52),(34,34,'标准版','默认规格; 颜色: 黑色/白色',80.10,53),(35,35,'标准版','默认规格; 颜色: 黑色/白色',91.80,54),(36,36,'标准版','默认规格; 颜色: 黑色/白色',103.50,55),(37,37,'标准版','默认规格; 颜色: 黑色/白色',115.20,56),(38,38,'标准版','默认规格; 颜色: 黑色/白色',126.90,57),(39,39,'标准版','默认规格; 颜色: 黑色/白色',138.60,58),(40,40,'标准版','默认规格; 颜色: 黑色/白色',150.30,59),(41,41,'标准版','默认规格; 颜色: 黑色/白色',162.00,60),(42,42,'标准版','默认规格; 颜色: 黑色/白色',173.70,61),(43,43,'标准版','默认规格; 颜色: 黑色/白色',185.40,62),(44,44,'标准版','默认规格; 颜色: 黑色/白色',197.10,63),(45,45,'标准版','默认规格; 颜色: 黑色/白色',208.80,64),(46,46,'标准版','默认规格; 颜色: 黑色/白色',220.50,65),(47,47,'标准版','默认规格; 颜色: 黑色/白色',232.20,66),(48,48,'标准版','默认规格; 颜色: 黑色/白色',243.90,67),(49,49,'标准版','默认规格; 颜色: 黑色/白色',255.60,68),(50,50,'标准版','默认规格; 颜色: 黑色/白色',267.30,69),(51,51,'标准版','默认规格; 颜色: 黑色/白色',279.00,70),(52,52,'标准版','默认规格; 颜色: 黑色/白色',290.70,71),(53,53,'标准版','默认规格; 颜色: 黑色/白色',302.40,72),(54,54,'标准版','默认规格; 颜色: 黑色/白色',314.10,73),(55,55,'标准版','默认规格; 颜色: 黑色/白色',325.80,74),(56,56,'标准版','默认规格; 颜色: 黑色/白色',337.50,75),(57,57,'标准版','默认规格; 颜色: 黑色/白色',349.20,76),(58,58,'标准版','默认规格; 颜色: 黑色/白色',360.90,77),(59,59,'标准版','默认规格; 颜色: 黑色/白色',372.60,78),(60,60,'标准版','默认规格; 颜色: 黑色/白色',384.30,79),(61,61,'标准版','默认规格; 颜色: 黑色/白色',396.00,80),(62,62,'标准版','默认规格; 颜色: 黑色/白色',407.70,81),(63,63,'标准版','默认规格; 颜色: 黑色/白色',419.40,82),(64,64,'标准版','默认规格; 颜色: 黑色/白色',431.10,83),(65,65,'标准版','默认规格; 颜色: 黑色/白色',442.80,84),(66,66,'标准版','默认规格; 颜色: 黑色/白色',454.50,85),(67,67,'标准版','默认规格; 颜色: 黑色/白色',466.20,86),(68,68,'标准版','默认规格; 颜色: 黑色/白色',477.90,87),(69,69,'标准版','默认规格; 颜色: 黑色/白色',489.60,88),(70,70,'标准版','默认规格; 颜色: 黑色/白色',501.30,89),(71,71,'标准版','默认规格; 颜色: 黑色/白色',513.00,90),(72,72,'标准版','默认规格; 颜色: 黑色/白色',524.70,91);
/*!40000 ALTER TABLE `product_sku` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_cart`
--

DROP TABLE IF EXISTS `shopping_cart`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_cart` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=4 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_cart`
--

LOCK TABLES `shopping_cart` WRITE;
/*!40000 ALTER TABLE `shopping_cart` DISABLE KEYS */;
INSERT INTO `shopping_cart` VALUES (1,2,'2026-06-23 16:35:06','2026-06-23 16:35:06'),(2,11,'2026-06-23 16:37:44','2026-06-23 16:37:44'),(3,12,'2026-06-30 20:32:48','2026-06-30 20:32:48');
/*!40000 ALTER TABLE `shopping_cart` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `shopping_cart_item`
--

DROP TABLE IF EXISTS `shopping_cart_item`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `shopping_cart_item` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `cart_id` bigint(20) NOT NULL,
  `user_id` bigint(20) NOT NULL,
  `product_id` bigint(20) NOT NULL,
  `product_name` varchar(120) DEFAULT NULL,
  `product_image` varchar(1000) DEFAULT NULL,
  `spec_info` varchar(255) DEFAULT NULL,
  `price` decimal(10,2) DEFAULT NULL,
  `quantity` int(11) DEFAULT '1',
  `selected` tinyint(4) DEFAULT '1',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=14 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='购物车商品表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `shopping_cart_item`
--

LOCK TABLES `shopping_cart_item` WRITE;
/*!40000 ALTER TABLE `shopping_cart_item` DISABLE KEYS */;
INSERT INTO `shopping_cart_item` VALUES (9,2,11,70,'智能手表青春版','https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=500&q=80','默认规格; 颜色: 黑色/白色',501.30,1,1,'2026-06-30 20:24:31','2026-06-30 20:24:31');
/*!40000 ALTER TABLE `shopping_cart_item` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `sys_user`
--

DROP TABLE IF EXISTS `sys_user`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `sys_user` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户编号',
  `username` varchar(50) NOT NULL COMMENT '用户名',
  `password` varchar(100) NOT NULL COMMENT '密码',
  `phone` varchar(20) DEFAULT NULL COMMENT '手机号',
  `email` varchar(100) DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) DEFAULT NULL COMMENT '收货地址',
  `status` tinyint(4) NOT NULL DEFAULT '1' COMMENT '用户状态：1启用 0禁用',
  `role` varchar(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN管理员 USER普通用户',
  `avatar` varchar(500) DEFAULT NULL COMMENT '头像',
  `avatar_url` varchar(500) DEFAULT NULL COMMENT '头像',
  `register_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  `deleted` tinyint(4) NOT NULL DEFAULT '0' COMMENT '逻辑删除：0未删 1已删',
  PRIMARY KEY (`id`),
  UNIQUE KEY `username` (`username`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `sys_user`
--

LOCK TABLES `sys_user` WRITE;
/*!40000 ALTER TABLE `sys_user` DISABLE KEYS */;
INSERT INTO `sys_user` VALUES (1,'admin','123456','13800000000','admin@mall.com','北京市朝阳区电商中心1号',1,'ADMIN','https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=240&q=80','https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=240&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29','2026-06-30 20:25:25',0),(2,'zhangsan','123456','13800000001','zhangsan@example.com','上海市浦东新区世纪大道100号',1,'USER','https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=240&q=80','https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=240&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(3,'lisi','123456','13800000002','lisi@example.com','广州市天河区体育西路88号',1,'USER','https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=240&q=80','https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=240&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(4,'wangwu','123456','13800000003','wangwu@example.com','深圳市南山区科技园9栋',1,'USER','https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=240&q=80','https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=240&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(5,'zhaoliu','123456','13800000004','zhaoliu@example.com','杭州市西湖区文三路66号',1,'USER','https://images.unsplash.com/photo-1527980965255-d3b416303d12?auto=format&fit=crop&w=240&q=80','https://images.unsplash.com/photo-1527980965255-d3b416303d12?auto=format&fit=crop&w=240&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(6,'sunqi','123456','13800000005','sunqi@example.com','成都市高新区天府三街12号',1,'USER','https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=240&q=80','https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=240&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(7,'zhouba','123456','13800000006','zhouba@example.com','南京市鼓楼区中山路28号',1,'USER','https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=240&q=80','https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=240&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(8,'wujie','123456','13800000007','wujie@example.com','武汉市洪山区光谷大道77号',1,'USER','https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=240&q=80','https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=240&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(9,'chenchen','123456','13800000008','chenchen@example.com','西安市雁塔区科技路18号',1,'USER','https://images.unsplash.com/photo-1547425260-76bcadfb4f2c?auto=format&fit=crop&w=240&q=80','https://images.unsplash.com/photo-1547425260-76bcadfb4f2c?auto=format&fit=crop&w=240&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(10,'liuming','123456','13800000009','liuming@example.com','苏州市工业园区金鸡湖大道8号',0,'USER','https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=240&q=80','https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=240&q=80','2026-06-23 16:34:29','2026-06-23 16:34:29','2026-06-23 16:34:29',0),(11,'123456','li123456789','15389108683','lwh619@qq.com','jia',1,'USER','https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=240&q=80','https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=240&q=80','2026-06-23 16:44:20','2026-06-23 16:44:20','2026-06-30 20:25:28',1),(12,'15389108683','li123456789','15389108683','','',1,'USER','https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=240&q=80','https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=240&q=80','2026-06-30 20:31:53','2026-06-30 20:31:53','2026-06-30 20:31:53',0);
/*!40000 ALTER TABLE `sys_user` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_address`
--

DROP TABLE IF EXISTS `user_address`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_address` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `receiver_name` varchar(50) DEFAULT NULL,
  `receiver_phone` varchar(20) DEFAULT NULL,
  `province` varchar(50) DEFAULT NULL,
  `city` varchar(50) DEFAULT NULL,
  `detail_address` varchar(255) DEFAULT NULL,
  `is_default` tinyint(4) DEFAULT '0',
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  `update_time` datetime DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=7 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户地址表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_address`
--

LOCK TABLES `user_address` WRITE;
/*!40000 ALTER TABLE `user_address` DISABLE KEYS */;
INSERT INTO `user_address` VALUES (1,2,'张三','13800000001','上海市','浦东新区','世纪大道100号',1,'2026-06-23 16:34:29','2026-06-23 16:34:29'),(2,3,'李四','13800000002','广东省','广州市','天河区体育西路88号',1,'2026-06-23 16:34:29','2026-06-23 16:34:29'),(3,4,'王五','13800000003','广东省','深圳市','南山区科技园9栋',1,'2026-06-23 16:34:29','2026-06-23 16:34:29'),(4,11,'23','15846852356','广东省','广州市','li-wenhao',0,'2026-06-23 16:38:34','2026-06-23 16:38:34'),(5,11,'123','15384567512','广东省','广州市','1600 Amphitheatre Pkwy',0,'2026-06-30 20:23:14','2026-06-30 20:23:14'),(6,12,'123','15368954682','广东省','广州市','li-wenhao',0,'2026-06-30 20:33:27','2026-06-30 20:33:27');
/*!40000 ALTER TABLE `user_address` ENABLE KEYS */;
UNLOCK TABLES;

--
-- Table structure for table `user_gift_card`
--

DROP TABLE IF EXISTS `user_gift_card`;
/*!40101 SET @saved_cs_client     = @@character_set_client */;
/*!50503 SET character_set_client = utf8mb4 */;
CREATE TABLE `user_gift_card` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `gift_card_id` bigint(20) DEFAULT NULL,
  `gift_card_name` varchar(100) DEFAULT NULL,
  `amount` decimal(10,2) DEFAULT NULL,
  `recipient` varchar(80) DEFAULT NULL,
  `blessing` varchar(255) DEFAULT NULL,
  `redeem_code` varchar(50) DEFAULT NULL,
  `status` varchar(30) DEFAULT NULL,
  `create_time` datetime DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='用户礼品卡表';
/*!40101 SET character_set_client = @saved_cs_client */;

--
-- Dumping data for table `user_gift_card`
--

LOCK TABLES `user_gift_card` WRITE;
/*!40000 ALTER TABLE `user_gift_card` DISABLE KEYS */;
INSERT INTO `user_gift_card` VALUES (1,11,2,'100元礼品卡',100.00,'','愿你每天都有好心情','GC-D4A44FBF','未使用','2026-06-30 20:22:50');
/*!40000 ALTER TABLE `user_gift_card` ENABLE KEYS */;
UNLOCK TABLES;
/*!40103 SET TIME_ZONE=@OLD_TIME_ZONE */;

/*!40101 SET SQL_MODE=@OLD_SQL_MODE */;
/*!40014 SET FOREIGN_KEY_CHECKS=@OLD_FOREIGN_KEY_CHECKS */;
/*!40014 SET UNIQUE_CHECKS=@OLD_UNIQUE_CHECKS */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
/*!40111 SET SQL_NOTES=@OLD_SQL_NOTES */;

-- Dump completed on 2026-07-11 13:44:36
