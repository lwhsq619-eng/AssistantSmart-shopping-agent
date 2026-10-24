DROP DATABASE IF EXISTS mall_system;
CREATE DATABASE mall_system DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE mall_system;

CREATE TABLE sys_user (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '用户编号',
  username VARCHAR(50) NOT NULL UNIQUE COMMENT '用户名',
  password VARCHAR(100) NOT NULL COMMENT '密码',
  phone VARCHAR(20) COMMENT '手机号',
  email VARCHAR(100) COMMENT '邮箱',
  address VARCHAR(255) COMMENT '收货地址',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '用户状态：1启用 0禁用',
  role VARCHAR(20) NOT NULL DEFAULT 'USER' COMMENT '角色：ADMIN管理员 USER普通用户',
  avatar VARCHAR(500) COMMENT '头像',
  avatar_url VARCHAR(500) COMMENT '头像',
  register_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '注册时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除：0未删 1已删'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户表';

CREATE TABLE product_category (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '分类编号',
  name VARCHAR(80) NOT NULL UNIQUE COMMENT '分类名称',
  description VARCHAR(255) COMMENT '分类描述',
  sort_no INT NOT NULL DEFAULT 0 COMMENT '排序号',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '状态：1启用 0禁用',
  image_url VARCHAR(500) COMMENT '分类封面',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品分类表';

CREATE TABLE product (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '商品编号',
  product_no VARCHAR(50) NOT NULL UNIQUE COMMENT '商品编码',
  name VARCHAR(100) NOT NULL COMMENT '商品名称',
  category_id BIGINT NOT NULL COMMENT '分类编号',
  category_name VARCHAR(80) NOT NULL COMMENT '分类名称',
  image_url VARCHAR(500) COMMENT '商品图片',
  price DECIMAL(10,2) NOT NULL COMMENT '商品价格',
  stock INT NOT NULL DEFAULT 0 COMMENT '库存数量',
  description VARCHAR(500) COMMENT '商品描述',
  status TINYINT NOT NULL DEFAULT 1 COMMENT '上架状态：1上架 0下架',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  INDEX idx_product_category (category_id),
  INDEX idx_product_name (name)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品表';

CREATE TABLE orders (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '订单编号',
  order_no VARCHAR(50) NOT NULL UNIQUE COMMENT '订单号',
  user_id BIGINT NOT NULL COMMENT '用户编号',
  user_name VARCHAR(50) NOT NULL COMMENT '用户名称',
  total_amount DECIMAL(10,2) NOT NULL COMMENT '订单总金额',
  shipping_address VARCHAR(255) NOT NULL COMMENT '收货地址',
  status VARCHAR(20) NOT NULL COMMENT '订单状态',
  order_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '下单时间',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  INDEX idx_order_no (order_no),
  INDEX idx_order_user (user_id),
  INDEX idx_order_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单表';

CREATE TABLE order_item (
  id BIGINT PRIMARY KEY AUTO_INCREMENT COMMENT '明细编号',
  order_id BIGINT NOT NULL COMMENT '订单编号',
  product_id BIGINT NOT NULL COMMENT '商品编号',
  product_name VARCHAR(100) NOT NULL COMMENT '商品名称',
  product_image VARCHAR(500) COMMENT '商品图片',
  quantity INT NOT NULL COMMENT '商品数量',
  unit_price DECIMAL(10,2) NOT NULL COMMENT '商品单价',
  subtotal DECIMAL(10,2) NOT NULL COMMENT '小计',
  create_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  update_time DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  deleted TINYINT NOT NULL DEFAULT 0 COMMENT '逻辑删除',
  INDEX idx_item_order (order_id),
  INDEX idx_item_product (product_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='订单明细表';

INSERT INTO sys_user (username,password,phone,email,address,status,avatar_url,register_time,create_time,update_time) VALUES
('admin','123456','13800000000','admin@mall.com','北京市朝阳区电商中心1号',1,'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=240&q=80',NOW(),NOW(),NOW()),
('zhangsan','123456','13800000001','zhangsan@example.com','上海市浦东新区世纪大道100号',1,'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=240&q=80',NOW(),NOW(),NOW()),
('lisi','123456','13800000002','lisi@example.com','广州市天河区体育西路88号',1,'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=240&q=80',NOW(),NOW(),NOW()),
('wangwu','123456','13800000003','wangwu@example.com','深圳市南山区科技园9栋',1,'https://images.unsplash.com/photo-1438761681033-6461ffad8d80?auto=format&fit=crop&w=240&q=80',NOW(),NOW(),NOW()),
('zhaoliu','123456','13800000004','zhaoliu@example.com','杭州市西湖区文三路66号',1,'https://images.unsplash.com/photo-1527980965255-d3b416303d12?auto=format&fit=crop&w=240&q=80',NOW(),NOW(),NOW()),
('sunqi','123456','13800000005','sunqi@example.com','成都市高新区天府三街12号',1,'https://images.unsplash.com/photo-1544005313-94ddf0286df2?auto=format&fit=crop&w=240&q=80',NOW(),NOW(),NOW()),
('zhouba','123456','13800000006','zhouba@example.com','南京市鼓楼区中山路28号',1,'https://images.unsplash.com/photo-1524504388940-b1c1722653e1?auto=format&fit=crop&w=240&q=80',NOW(),NOW(),NOW()),
('wujie','123456','13800000007','wujie@example.com','武汉市洪山区光谷大道77号',1,'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?auto=format&fit=crop&w=240&q=80',NOW(),NOW(),NOW()),
('chenchen','123456','13800000008','chenchen@example.com','西安市雁塔区科技路18号',1,'https://images.unsplash.com/photo-1547425260-76bcadfb4f2c?auto=format&fit=crop&w=240&q=80',NOW(),NOW(),NOW()),
('liuming','123456','13800000009','liuming@example.com','苏州市工业园区金鸡湖大道8号',0,'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=240&q=80',NOW(),NOW(),NOW());

UPDATE sys_user SET role = 'ADMIN' WHERE username = 'admin';
UPDATE sys_user SET role = 'USER' WHERE username <> 'admin';
UPDATE sys_user SET avatar = avatar_url;

INSERT INTO product_category (name,description,sort_no,status,image_url,create_time,update_time) VALUES
('数码科技','手机、耳机、键盘、显示器等数码设备',1,1,'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=600&q=80',NOW(),NOW()),
('时尚服饰','卫衣、外套、运动鞋与日常穿搭',2,1,'https://images.unsplash.com/photo-1445205170230-053b83016050?auto=format&fit=crop&w=600&q=80',NOW(),NOW()),
('家居生活','台灯、椅子、香薰、收纳等家居用品',3,1,'https://images.unsplash.com/photo-1513161455079-7dc1de15ef3e?auto=format&fit=crop&w=600&q=80',NOW(),NOW()),
('美妆护肤','护肤品、香水、彩妆和个人护理',4,1,'https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=600&q=80',NOW(),NOW()),
('食品饮料','咖啡、零食、饮品与健康食品',5,1,'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=600&q=80',NOW(),NOW()),
('运动户外','健身装备、户外用品和运动配件',6,1,'https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=600&q=80',NOW(),NOW()),
('母婴用品','母婴护理、玩具和儿童用品',7,1,'https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=600&q=80',NOW(),NOW()),
('办公文具','办公设备、笔记本和桌面用品',8,1,'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=600&q=80',NOW(),NOW()),
('箱包配饰','背包、钱包、配饰和出行用品',9,1,'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=600&q=80',NOW(),NOW()),
('图书音像','教材、畅销书、影音与学习资料',10,1,'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=600&q=80',NOW(),NOW());

INSERT INTO product (product_no,name,category_id,category_name,image_url,price,stock,description,status,create_time,update_time) VALUES
('SP10001','蓝牙降噪耳机',1,'数码科技','https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=500&q=80',399.00,26,'主动降噪，长续航，适合通勤与学习。',1,NOW(),NOW()),
('SP10002','机械键盘',1,'数码科技','https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500&q=80',289.00,8,'青轴手感，白色背光，桌面办公游戏两用。',1,NOW(),NOW()),
('SP10003','27英寸显示器',1,'数码科技','https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=500&q=80',1299.00,15,'2K 高清屏幕，低蓝光护眼。',1,NOW(),NOW()),
('SP10004','智能手机支架',1,'数码科技','https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=500&q=80',59.00,42,'铝合金可折叠支架，多角度调节。',1,NOW(),NOW()),
('SP10005','简约连帽卫衣',2,'时尚服饰','https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=500&q=80',169.00,35,'柔软亲肤面料，适合春秋日常穿搭。',1,NOW(),NOW()),
('SP10006','轻量运动鞋',2,'时尚服饰','https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80',329.00,18,'缓震鞋底，轻量透气。',1,NOW(),NOW()),
('SP10007','城市通勤背包',9,'箱包配饰','https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80',219.00,22,'多隔层设计，可容纳 15 英寸电脑。',1,NOW(),NOW()),
('SP10008','皮革钱包',9,'箱包配饰','https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=500&q=80',129.00,9,'简洁卡位设计，手感细腻。',1,NOW(),NOW()),
('SP10009','北欧台灯',3,'家居生活','https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500&q=80',149.00,14,'暖光护眼，适合卧室与书桌。',1,NOW(),NOW()),
('SP10010','人体工学椅',3,'家居生活','https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=500&q=80',899.00,7,'腰托可调，长时间办公更舒适。',1,NOW(),NOW()),
('SP10011','桌面收纳盒',3,'家居生活','https://images.unsplash.com/photo-1518455027359-f3f8164ba6bd?auto=format&fit=crop&w=500&q=80',49.00,56,'分区收纳，保持桌面整洁。',1,NOW(),NOW()),
('SP10012','香薰蜡烛',3,'家居生活','https://images.unsplash.com/photo-1602874801006-e26c8d89ba20?auto=format&fit=crop&w=500&q=80',79.00,31,'清新木质香调，营造放松氛围。',1,NOW(),NOW()),
('SP10013','保湿精华液',4,'美妆护肤','https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=500&q=80',199.00,25,'补水保湿，质地清爽。',1,NOW(),NOW()),
('SP10014','淡香水',4,'美妆护肤','https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=500&q=80',269.00,12,'清新花果香，适合日常通勤。',1,NOW(),NOW()),
('SP10015','润色口红',4,'美妆护肤','https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=500&q=80',99.00,40,'显色自然，滋润不拔干。',1,NOW(),NOW()),
('SP10016','精品咖啡豆',5,'食品饮料','https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=500&q=80',88.00,33,'中深烘焙，坚果可可风味。',1,NOW(),NOW()),
('SP10017','坚果礼盒',5,'食品饮料','https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&w=500&q=80',128.00,20,'多种坚果组合，独立小包装。',1,NOW(),NOW()),
('SP10018','气泡饮料组合',5,'食品饮料','https://images.unsplash.com/photo-1622483767028-3f66f32aef97?auto=format&fit=crop&w=500&q=80',69.00,6,'低糖清爽口味，适合聚会囤货。',1,NOW(),NOW()),
('SP10019','瑜伽垫',6,'运动户外','https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?auto=format&fit=crop&w=500&q=80',119.00,28,'防滑加厚，适合居家训练。',1,NOW(),NOW()),
('SP10020','运动水杯',6,'运动户外','https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=500&q=80',59.00,45,'大容量便携，户外健身适用。',1,NOW(),NOW()),
('SP10021','户外双肩包',6,'运动户外','https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=500&q=80',239.00,16,'轻量防泼水，多场景出行。',1,NOW(),NOW()),
('SP10022','婴儿湿巾',7,'母婴用品','https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=500&q=80',39.00,60,'柔软亲肤，无香型。',1,NOW(),NOW()),
('SP10023','儿童积木套装',7,'母婴用品','https://images.unsplash.com/photo-1560961911-ba7ef651a56c?auto=format&fit=crop&w=500&q=80',159.00,21,'启蒙拼搭，安全圆角。',1,NOW(),NOW()),
('SP10024','保温奶瓶',7,'母婴用品','https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=500&q=80',89.00,13,'宽口径设计，便于清洗。',1,NOW(),NOW()),
('SP10025','无线鼠标',8,'办公文具','https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=500&q=80',79.00,38,'静音按键，办公学习适用。',1,NOW(),NOW()),
('SP10026','商务笔记本',8,'办公文具','https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=500&q=80',29.00,80,'加厚纸张，书写顺滑。',1,NOW(),NOW()),
('SP10027','桌面文件架',8,'办公文具','https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=500&q=80',66.00,17,'多层分类，提升办公效率。',1,NOW(),NOW()),
('SP10028','算法入门图书',10,'图书音像','https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=500&q=80',75.00,32,'适合计算机专业学生入门算法。',1,NOW(),NOW()),
('SP10029','数据库系统教材',10,'图书音像','https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=500&q=80',89.00,24,'覆盖关系模型、SQL、事务与索引。',1,NOW(),NOW()),
('SP10030','复古蓝牙音箱',1,'数码科技','https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=500&q=80',259.00,5,'复古外观，蓝牙连接，低音增强。',0,NOW(),NOW());

INSERT INTO orders (order_no,user_id,user_name,total_amount,shipping_address,status,order_time,create_time,update_time) VALUES
('SO202606160001',2,'zhangsan',399.00,'上海市浦东新区世纪大道100号','已完成',DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 10 HOUR,NOW(),NOW()),
('SO202606160002',3,'lisi',578.00,'广州市天河区体育西路88号','已完成',DATE_SUB(CURDATE(), INTERVAL 6 DAY) + INTERVAL 15 HOUR,NOW(),NOW()),
('SO202606170001',4,'wangwu',169.00,'深圳市南山区科技园9栋','待收货',DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 11 HOUR,NOW(),NOW()),
('SO202606170002',5,'zhaoliu',658.00,'杭州市西湖区文三路66号','已完成',DATE_SUB(CURDATE(), INTERVAL 5 DAY) + INTERVAL 18 HOUR,NOW(),NOW()),
('SO202606180001',6,'sunqi',219.00,'成都市高新区天府三街12号','待发货',DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 9 HOUR,NOW(),NOW()),
('SO202606180002',7,'zhouba',258.00,'南京市鼓楼区中山路28号','已取消',DATE_SUB(CURDATE(), INTERVAL 4 DAY) + INTERVAL 16 HOUR,NOW(),NOW()),
('SO202606190001',8,'wujie',149.00,'武汉市洪山区光谷大道77号','已完成',DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 10 HOUR,NOW(),NOW()),
('SO202606190002',9,'chenchen',1798.00,'西安市雁塔区科技路18号','待收货',DATE_SUB(CURDATE(), INTERVAL 3 DAY) + INTERVAL 20 HOUR,NOW(),NOW()),
('SO202606200001',2,'zhangsan',199.00,'上海市浦东新区世纪大道100号','待付款',DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 12 HOUR,NOW(),NOW()),
('SO202606200002',3,'lisi',269.00,'广州市天河区体育西路88号','已完成',DATE_SUB(CURDATE(), INTERVAL 2 DAY) + INTERVAL 19 HOUR,NOW(),NOW()),
('SO202606210001',4,'wangwu',198.00,'深圳市南山区科技园9栋','待发货',DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 8 HOUR,NOW(),NOW()),
('SO202606210002',5,'zhaoliu',176.00,'杭州市西湖区文三路66号','已完成',DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 15 HOUR,NOW(),NOW()),
('SO202606210003',6,'sunqi',128.00,'成都市高新区天府三街12号','待收货',DATE_SUB(CURDATE(), INTERVAL 1 DAY) + INTERVAL 21 HOUR,NOW(),NOW()),
('SO202606220001',7,'zhouba',238.00,'南京市鼓楼区中山路28号','待付款',CURDATE() + INTERVAL 9 HOUR,NOW(),NOW()),
('SO202606220002',8,'wujie',59.00,'武汉市洪山区光谷大道77号','待发货',CURDATE() + INTERVAL 10 HOUR,NOW(),NOW()),
('SO202606220003',9,'chenchen',478.00,'西安市雁塔区科技路18号','已完成',CURDATE() + INTERVAL 11 HOUR,NOW(),NOW()),
('SO202606220004',2,'zhangsan',78.00,'上海市浦东新区世纪大道100号','待收货',CURDATE() + INTERVAL 12 HOUR,NOW(),NOW()),
('SO202606220005',3,'lisi',318.00,'广州市天河区体育西路88号','已完成',CURDATE() + INTERVAL 13 HOUR,NOW(),NOW()),
('SO202606220006',4,'wangwu',79.00,'深圳市南山区科技园9栋','待发货',CURDATE() + INTERVAL 14 HOUR,NOW(),NOW()),
('SO202606220007',5,'zhaoliu',178.00,'杭州市西湖区文三路66号','已取消',CURDATE() + INTERVAL 15 HOUR,NOW(),NOW());

INSERT INTO order_item (order_id,product_id,product_name,product_image,quantity,unit_price,subtotal,create_time,update_time) VALUES
(1,1,'蓝牙降噪耳机','https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=500&q=80',1,399.00,399.00,NOW(),NOW()),
(2,2,'机械键盘','https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500&q=80',2,289.00,578.00,NOW(),NOW()),
(3,5,'简约连帽卫衣','https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=500&q=80',1,169.00,169.00,NOW(),NOW()),
(4,6,'轻量运动鞋','https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80',2,329.00,658.00,NOW(),NOW()),
(5,7,'城市通勤背包','https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80',1,219.00,219.00,NOW(),NOW()),
(6,8,'皮革钱包','https://images.unsplash.com/photo-1627123424574-724758594e93?auto=format&fit=crop&w=500&q=80',2,129.00,258.00,NOW(),NOW()),
(7,9,'北欧台灯','https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500&q=80',1,149.00,149.00,NOW(),NOW()),
(8,10,'人体工学椅','https://images.unsplash.com/photo-1586023492125-27b2c045efd7?auto=format&fit=crop&w=500&q=80',2,899.00,1798.00,NOW(),NOW()),
(9,13,'保湿精华液','https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=500&q=80',1,199.00,199.00,NOW(),NOW()),
(10,14,'淡香水','https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=500&q=80',1,269.00,269.00,NOW(),NOW()),
(11,15,'润色口红','https://images.unsplash.com/photo-1586495777744-4413f21062fa?auto=format&fit=crop&w=500&q=80',2,99.00,198.00,NOW(),NOW()),
(12,16,'精品咖啡豆','https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=500&q=80',2,88.00,176.00,NOW(),NOW()),
(13,17,'坚果礼盒','https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&w=500&q=80',1,128.00,128.00,NOW(),NOW()),
(14,19,'瑜伽垫','https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?auto=format&fit=crop&w=500&q=80',2,119.00,238.00,NOW(),NOW()),
(15,20,'运动水杯','https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=500&q=80',1,59.00,59.00,NOW(),NOW()),
(16,21,'户外双肩包','https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=500&q=80',2,239.00,478.00,NOW(),NOW()),
(17,22,'婴儿湿巾','https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=500&q=80',2,39.00,78.00,NOW(),NOW()),
(18,23,'儿童积木套装','https://images.unsplash.com/photo-1560961911-ba7ef651a56c?auto=format&fit=crop&w=500&q=80',2,159.00,318.00,NOW(),NOW()),
(19,25,'无线鼠标','https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=500&q=80',1,79.00,79.00,NOW(),NOW()),
(20,29,'数据库系统教材','https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=500&q=80',2,89.00,178.00,NOW(),NOW());

-- Mall frontend extension: schema fields and storefront business tables.
ALTER TABLE product
  ADD COLUMN brand_id BIGINT NULL COMMENT '品牌编号',
  ADD COLUMN brand_name VARCHAR(100) NULL COMMENT '品牌名称',
  ADD COLUMN detail_image_url VARCHAR(1000) NULL COMMENT '商品详情图',
  ADD COLUMN market_price DECIMAL(10,2) NULL COMMENT '市场原价',
  ADD COLUMN discount_price DECIMAL(10,2) NULL COMMENT '折扣价格',
  ADD COLUMN sales INT NOT NULL DEFAULT 0 COMMENT '销量',
  ADD COLUMN rating DECIMAL(3,1) NOT NULL DEFAULT 5.0 COMMENT '评分',
  ADD COLUMN review_count INT NOT NULL DEFAULT 0 COMMENT '评价数量',
  ADD COLUMN specs VARCHAR(1000) NULL COMMENT '商品规格',
  ADD COLUMN is_special TINYINT NOT NULL DEFAULT 0 COMMENT '是否今日特价',
  ADD COLUMN is_recommended TINYINT NOT NULL DEFAULT 0 COMMENT '是否推荐商品',
  ADD COLUMN is_new TINYINT NOT NULL DEFAULT 0 COMMENT '是否新品';

ALTER TABLE orders
  ADD COLUMN discount_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '优惠金额',
  ADD COLUMN freight_amount DECIMAL(10,2) NOT NULL DEFAULT 0.00 COMMENT '运费',
  ADD COLUMN pay_amount DECIMAL(10,2) NULL COMMENT '应付金额',
  ADD COLUMN payment_method VARCHAR(50) NULL COMMENT '支付方式',
  ADD COLUMN paid_time DATETIME NULL COMMENT '支付时间';

ALTER TABLE order_item
  ADD COLUMN spec_info VARCHAR(255) NULL COMMENT '规格信息',
  ADD COLUMN reviewed TINYINT NOT NULL DEFAULT 0 COMMENT '是否评价';

CREATE TABLE brand (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100) NOT NULL,
  slogan VARCHAR(255),
  description TEXT,
  image_url VARCHAR(500),
  status TINYINT NOT NULL DEFAULT 1,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='品牌表';

CREATE TABLE product_image (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  image_url VARCHAR(1000) NOT NULL,
  sort_no INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品图片表';

CREATE TABLE product_sku (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  product_id BIGINT NOT NULL,
  sku_name VARCHAR(100),
  spec_info VARCHAR(255),
  price DECIMAL(10,2),
  stock INT DEFAULT 0
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品规格表';

CREATE TABLE shopping_cart (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  user_id BIGINT NOT NULL,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP,
  update_time DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='购物车商品表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户地址表';

CREATE TABLE payment_record (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  order_no VARCHAR(50),
  user_id BIGINT,
  amount DECIMAL(10,2),
  payment_method VARCHAR(50),
  status VARCHAR(30),
  pay_time DATETIME,
  create_time DATETIME DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='支付记录表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='商品评价表';

CREATE TABLE gift_card (
  id BIGINT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(100),
  amount DECIMAL(10,2),
  image_url VARCHAR(500),
  description VARCHAR(255),
  status TINYINT DEFAULT 1
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='礼品卡表';

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
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户礼品卡表';

INSERT INTO brand (id,name,slogan,description,image_url,status) VALUES
(1,'NovaTech 数码生活','让科技融入日常','专注数码配件与智能设备，提供稳定易用的科技产品。','https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=800&q=80',1),
(2,'Mori Home 家居美学','把舒适带回家','以简洁家居、收纳和灯具提升日常生活质感。','https://images.unsplash.com/photo-1513161455079-7dc1de15ef3e?auto=format&fit=crop&w=800&q=80',1),
(3,'Urban Step 潮流鞋服','轻松穿出城市节奏','覆盖运动鞋、卫衣、背包等年轻化出行单品。','https://images.unsplash.com/photo-1445205170230-053b83016050?auto=format&fit=crop&w=800&q=80',1),
(4,'Pure Glow 美妆护肤','清爽自然的日常护理','提供护肤、美妆和香氛产品，强调温和与高质感。','https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=800&q=80',1);

INSERT INTO product_category (id,name,description,sort_no,status,image_url,create_time,update_time) VALUES
(11,'手机通讯','手机、充电器、支架和移动配件',11,1,'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=600&q=80',NOW(),NOW()),
(12,'电脑办公','显示器、键盘、鼠标和办公设备',12,1,'https://images.unsplash.com/photo-1497366754035-f200968a6e72?auto=format&fit=crop&w=600&q=80',NOW(),NOW()),
(13,'服饰鞋包','运动鞋、卫衣、背包与潮流配饰',13,1,'https://images.unsplash.com/photo-1445205170230-053b83016050?auto=format&fit=crop&w=600&q=80',NOW(),NOW()),
(14,'宠物用品','宠物玩具、清洁和日常护理用品',14,1,'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=600&q=80',NOW(),NOW()),
(15,'礼品专区','礼品卡、节日礼盒与生日礼品',15,1,'https://images.unsplash.com/photo-1513885535751-8b9238bd345a?auto=format&fit=crop&w=600&q=80',NOW(),NOW());

UPDATE product SET brand_id = 1, brand_name = 'NovaTech 数码生活', detail_image_url = image_url, market_price = ROUND(price * 1.25, 2), discount_price = price, sales = 30 + id, rating = 4.6, review_count = 12, specs = '颜色: 黑色/白色; 版本: 标准版', is_special = IF(MOD(id,5)=0,1,0), is_recommended = IF(MOD(id,3)=0,1,0), is_new = IF(id >= 20,1,0);
UPDATE orders SET pay_amount = total_amount WHERE pay_amount IS NULL;

-- Extra 42 mall products generated for user frontend.
INSERT IGNORE INTO product (product_no,name,category_id,category_name,brand_id,brand_name,image_url,detail_image_url,price,market_price,discount_price,stock,sales,rating,review_count,description,specs,status,is_special,is_recommended,is_new,create_time,update_time) VALUES
('SP20031','轻薄智能手机',11,'手机通讯',1,'NovaTech 数码生活','https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=500&q=80',50,62.5,45,50,100,4.6,10,'轻薄智能手机，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,1,1,1,NOW(),NOW()),
('SP20032','快充移动电源',11,'手机通讯',2,'Mori Home 家居美学','https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1609091839311-d5365f9ff1c5?auto=format&fit=crop&w=500&q=80',63,78.75,56.7,51,107,4.6,11,'快充移动电源，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20033','磁吸手机支架',11,'手机通讯',3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1512499617640-c2f999098c01?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1512499617640-c2f999098c01?auto=format&fit=crop&w=500&q=80',76,95,68.4,52,114,4.6,12,'磁吸手机支架，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20034','无线充电底座',11,'手机通讯',4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1615529182904-14819c35db37?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1615529182904-14819c35db37?auto=format&fit=crop&w=500&q=80',89,111.25,80.1,53,121,4.6,13,'无线充电底座，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,1,0,NOW(),NOW()),
('SP20035','降噪通话耳机',1,'数码科技',1,'NovaTech 数码生活','https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=500&q=80',102,127.5,91.8,54,128,4.6,14,'降噪通话耳机，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,1,0,0,NOW(),NOW()),
('SP20036','便携蓝牙音箱',1,'数码科技',2,'Mori Home 家居美学','https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1608043152269-423dbba4e7e1?auto=format&fit=crop&w=500&q=80',115,143.75,103.5,55,135,4.6,15,'便携蓝牙音箱，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,1,NOW(),NOW()),
('SP20037','电竞显示器',12,'电脑办公',3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1527443224154-c4a3942d3acf?auto=format&fit=crop&w=500&q=80',128,160,115.2,56,142,4.6,16,'电竞显示器，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,1,0,NOW(),NOW()),
('SP20038','人体工学键盘',12,'电脑办公',4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1595225476474-87563907a212?auto=format&fit=crop&w=500&q=80',141,176.25,126.9,57,149,4.6,17,'人体工学键盘，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20039','静音无线鼠标',12,'电脑办公',1,'NovaTech 数码生活','https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1527814050087-3793815479db?auto=format&fit=crop&w=500&q=80',154,192.5,138.6,58,156,4.6,18,'静音无线鼠标，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,1,0,0,NOW(),NOW()),
('SP20040','办公笔记本支架',12,'电脑办公',2,'Mori Home 家居美学','https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1497366811353-6870744d04b2?auto=format&fit=crop&w=500&q=80',167,208.75,150.3,59,163,4.6,19,'办公笔记本支架，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,1,0,NOW(),NOW()),
('SP20041','护眼台灯Pro',3,'家居生活',3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=500&q=80',180,225,162,60,170,4.6,20,'护眼台灯Pro，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,1,NOW(),NOW()),
('SP20042','桌面收纳盒套装',3,'家居生活',4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=500&q=80',193,241.25,173.7,61,177,4.6,21,'桌面收纳盒套装，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20043','陶瓷保温杯',3,'家居生活',1,'NovaTech 数码生活','https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1523362628745-0c100150b504?auto=format&fit=crop&w=500&q=80',206,257.5,185.4,62,184,4.6,22,'陶瓷保温杯，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,1,1,0,NOW(),NOW()),
('SP20044','香薰蜡烛礼盒',3,'家居生活',2,'Mori Home 家居美学','https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1608571423902-eed4a5ad8108?auto=format&fit=crop&w=500&q=80',219,273.75,197.1,63,191,4.6,23,'香薰蜡烛礼盒，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20045','轻跑运动鞋',13,'服饰鞋包',3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=500&q=80',232,290,208.8,64,198,4.6,24,'轻跑运动鞋，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20046','宽松连帽卫衣',13,'服饰鞋包',4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1556821840-3a63f95609a7?auto=format&fit=crop&w=500&q=80',245,306.25,220.5,65,205,4.6,25,'宽松连帽卫衣，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,1,1,NOW(),NOW()),
('SP20047','城市双肩包',13,'服饰鞋包',1,'NovaTech 数码生活','https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=500&q=80',258,322.5,232.2,66,212,4.6,26,'城市双肩包，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,1,0,0,NOW(),NOW()),
('SP20048','棒球帽',13,'服饰鞋包',2,'Mori Home 家居美学','https://images.unsplash.com/photo-1521369909029-2afed882baee?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1521369909029-2afed882baee?auto=format&fit=crop&w=500&q=80',271,338.75,243.9,67,219,4.6,27,'棒球帽，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20049','护肤套装',4,'美妆护肤',3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1620916566398-39f1143ab7be?auto=format&fit=crop&w=500&q=80',284,355,255.6,68,226,4.6,28,'护肤套装，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,1,0,NOW(),NOW()),
('SP20050','清新淡香水',4,'美妆护肤',4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1592945403244-b3fbafd7f539?auto=format&fit=crop&w=500&q=80',297,371.25,267.3,69,233,4.6,29,'清新淡香水，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20051','保湿面膜礼盒',4,'美妆护肤',1,'NovaTech 数码生活','https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1556228578-8c89e6adf883?auto=format&fit=crop&w=500&q=80',310,387.5,279,70,240,4.6,30,'保湿面膜礼盒，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,1,0,1,NOW(),NOW()),
('SP20052','咖啡豆礼盒',5,'食品饮料',2,'Mori Home 家居美学','https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1447933601403-0c6688de566e?auto=format&fit=crop&w=500&q=80',323,403.75,290.7,71,247,4.6,31,'咖啡豆礼盒，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,1,0,NOW(),NOW()),
('SP20053','零食大礼包',5,'食品饮料',3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1599599810769-bcde5a160d32?auto=format&fit=crop&w=500&q=80',336,420,302.4,72,254,4.6,32,'零食大礼包，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20054','气泡饮料组合',5,'食品饮料',4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1563227812-0ea4c22e6cc8?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1563227812-0ea4c22e6cc8?auto=format&fit=crop&w=500&q=80',349,436.25,314.1,73,261,4.6,33,'气泡饮料组合，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20055','专业瑜伽垫',6,'运动户外',1,'NovaTech 数码生活','https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1599901860904-17e6ed7083a0?auto=format&fit=crop&w=500&q=80',362,452.5,325.8,74,268,4.6,34,'专业瑜伽垫，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,1,1,0,NOW(),NOW()),
('SP20056','室内篮球',6,'运动户外',2,'Mori Home 家居美学','https://images.unsplash.com/photo-1546519638-68e109498ffc?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1546519638-68e109498ffc?auto=format&fit=crop&w=500&q=80',375,468.75,337.5,75,275,4.6,35,'室内篮球，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,1,NOW(),NOW()),
('SP20057','户外保温壶',6,'运动户外',3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1500530855697-b586d89ba3ee?auto=format&fit=crop&w=500&q=80',388,485,349.2,76,282,4.6,36,'户外保温壶，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20058','算法入门图书',9,'图书文具',4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1495446815901-a7297e633e8d?auto=format&fit=crop&w=500&q=80',401,501.25,360.9,77,289,4.6,37,'算法入门图书，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,1,0,NOW(),NOW()),
('SP20059','文具礼盒',9,'图书文具',1,'NovaTech 数码生活','https://images.unsplash.com/photo-1456735190827-d1262f71b8a3?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1456735190827-d1262f71b8a3?auto=format&fit=crop&w=500&q=80',414,517.5,372.6,78,296,4.6,38,'文具礼盒，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,1,0,0,NOW(),NOW()),
('SP20060','效率笔记本',9,'图书文具',2,'Mori Home 家居美学','https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1517842645767-c639042777db?auto=format&fit=crop&w=500&q=80',427,533.75,384.3,79,303,4.6,39,'效率笔记本，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20061','婴儿湿巾箱装',7,'母婴用品',3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1584515933487-779824d29309?auto=format&fit=crop&w=500&q=80',440,550,396,80,310,4.6,40,'婴儿湿巾箱装，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,1,1,NOW(),NOW()),
('SP20062','儿童积木套装',7,'母婴用品',4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1560961911-ba7ef651a56c?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1560961911-ba7ef651a56c?auto=format&fit=crop&w=500&q=80',453,566.25,407.7,81,317,4.6,41,'儿童积木套装，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20063','婴儿安抚玩具',7,'母婴用品',1,'NovaTech 数码生活','https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=500&q=80',466,582.5,419.4,82,324,4.6,42,'婴儿安抚玩具，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,1,0,0,NOW(),NOW()),
('SP20064','宠物磨牙玩具',14,'宠物用品',2,'Mori Home 家居美学','https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=500&q=80',479,598.75,431.1,83,331,4.6,43,'宠物磨牙玩具，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,1,0,NOW(),NOW()),
('SP20065','宠物外出包',14,'宠物用品',3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1516734212186-a967f81ad0d7?auto=format&fit=crop&w=500&q=80',492,615,442.8,84,338,4.6,44,'宠物外出包，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20066','宠物清洁套装',14,'宠物用品',4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1583337130417-3346a1be7dee?auto=format&fit=crop&w=500&q=80',505,631.25,454.5,85,345,4.6,45,'宠物清洁套装，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,1,NOW(),NOW()),
('SP20067','50元节日礼品卡',15,'礼品专区',1,'NovaTech 数码生活','https://images.unsplash.com/photo-1513201099705-a9746e1e201f?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1513201099705-a9746e1e201f?auto=format&fit=crop&w=500&q=80',518,647.5,466.2,86,352,4.6,46,'50元节日礼品卡，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,1,1,0,NOW(),NOW()),
('SP20068','100元生日礼品卡',15,'礼品专区',2,'Mori Home 家居美学','https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?auto=format&fit=crop&w=500&q=80',531,663.75,477.9,87,359,4.6,47,'100元生日礼品卡，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20069','节日零食礼盒',15,'礼品专区',3,'Urban Step 潮流鞋服','https://images.unsplash.com/photo-1549465220-1a8b9238cd48?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1549465220-1a8b9238cd48?auto=format&fit=crop&w=500&q=80',544,680,489.6,88,366,4.6,48,'节日零食礼盒，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW()),
('SP20070','智能手表青春版',1,'数码科技',4,'Pure Glow 美妆护肤','https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1523275335684-37898b6baf30?auto=format&fit=crop&w=500&q=80',557,696.25,501.3,89,373,4.6,49,'智能手表青春版，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,1,0,NOW(),NOW()),
('SP20071','高清投影仪',1,'数码科技',1,'NovaTech 数码生活','https://images.unsplash.com/photo-1601944179066-29786cb9d32a?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1601944179066-29786cb9d32a?auto=format&fit=crop&w=500&q=80',570,712.5,513,90,380,4.6,50,'高清投影仪，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,1,0,1,NOW(),NOW()),
('SP20072','家用咖啡机',3,'家居生活',2,'Mori Home 家居美学','https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=500&q=80','https://images.unsplash.com/photo-1517668808822-9ebb02f2a0e6?auto=format&fit=crop&w=500&q=80',583,728.75,524.7,91,387,4.6,51,'家用咖啡机，现代电商精选商品，适合课程设计演示。','颜色: 黑色/白色; 版本: 标准版',1,0,0,0,NOW(),NOW());

-- Normalize generated products into the 12 required storefront categories.
UPDATE product SET category_id=13, category_name='服饰鞋包' WHERE category_name IN ('时尚服饰','箱包配饰');
UPDATE product SET category_id=9, category_name='图书文具' WHERE category_name IN ('办公文具','图书音像');
UPDATE product SET category_id=11, category_name='手机通讯' WHERE product_no='SP20035';
UPDATE product SET category_id=12, category_name='电脑办公' WHERE product_no='SP20036';
UPDATE product SET category_id=14, category_name='宠物用品' WHERE product_no IN ('SP20041','SP20042');
UPDATE product SET category_id=15, category_name='礼品专区' WHERE product_no IN ('SP20043','SP20044');

-- Storefront related seed data.
INSERT INTO product_image (product_id,image_url,sort_no) SELECT id,image_url,1 FROM product;
INSERT INTO product_image (product_id,image_url,sort_no) SELECT id,detail_image_url,2 FROM product;
INSERT INTO product_sku (product_id,sku_name,spec_info,price,stock) SELECT id,'标准版','默认规格; 颜色: 黑色/白色',discount_price,stock FROM product;

INSERT INTO gift_card (id,name,amount,image_url,description,status) VALUES
(1,'50元礼品卡',50,'https://images.unsplash.com/photo-1513201099705-a9746e1e201f?auto=format&fit=crop&w=600&q=80','适合日常小礼物',1),
(2,'100元礼品卡',100,'https://images.unsplash.com/photo-1512909006721-3d6018887383?auto=format&fit=crop&w=600&q=80','热门礼品卡面额',1),
(3,'200元礼品卡',200,'https://images.unsplash.com/photo-1549465220-1a8b9238cd48?auto=format&fit=crop&w=600&q=80','适合节日赠礼',1),
(4,'500元礼品卡',500,'https://images.unsplash.com/photo-1513885535751-8b9238bd345a?auto=format&fit=crop&w=600&q=80','高价值礼品卡',1),
(5,'节日礼品卡',299,'https://images.unsplash.com/photo-1482517967863-00e15c9b44be?auto=format&fit=crop&w=600&q=80','节日祝福专属',1),
(6,'生日礼品卡',199,'https://images.unsplash.com/photo-1464349095431-e9a21285b5f3?auto=format&fit=crop&w=600&q=80','生日礼物推荐',1);

INSERT INTO user_address (user_id,receiver_name,receiver_phone,province,city,detail_address,is_default) VALUES
(2,'张三','13800000001','上海市','浦东新区','世纪大道100号',1),
(3,'李四','13800000002','广东省','广州市','天河区体育西路88号',1),
(4,'王五','13800000003','广东省','深圳市','南山区科技园9栋',1);

INSERT INTO product_review (product_id,order_id,order_item_id,user_id,username,avatar,rating,content,anonymous,create_time) VALUES
(1,1,1,2,'zhangsan',NULL,5,'耳机降噪效果不错，通勤很实用。',0,NOW()),
(2,2,2,3,'lisi',NULL,5,'键盘手感清晰，适合写代码。',0,NOW()),
(35,3,3,4,'匿名用户',NULL,4,'包装完整，音质符合预期。',1,NOW()),
(37,4,4,5,'zhaoliu',NULL,5,'显示器色彩不错，刷新率够用。',0,NOW()),
(41,5,5,6,'sunqi',NULL,5,'台灯很亮，学习使用舒服。',0,NOW()),
(45,6,6,7,'zhouba',NULL,5,'运动鞋轻便，尺码标准。',0,NOW()),
(49,7,7,8,'wujie',NULL,5,'护肤套装清爽不油腻。',0,NOW()),
(52,8,8,9,'chenchen',NULL,4,'咖啡香气不错，礼盒很适合送人。',0,NOW()),
(55,9,9,2,'zhangsan',NULL,5,'瑜伽垫防滑，厚度刚好。',0,NOW()),
(58,10,10,3,'lisi',NULL,5,'图书内容适合入门复习。',0,NOW()),
(61,11,11,4,'wangwu',NULL,4,'湿巾温和，家庭使用方便。',0,NOW()),
(64,12,12,5,'zhaoliu',NULL,5,'宠物玩具很耐咬。',0,NOW()),
(67,13,13,6,'sunqi',NULL,5,'礼品卡兑换码生成很快。',0,NOW()),
(70,14,14,7,'zhouba',NULL,5,'智能手表功能够用，续航不错。',0,NOW()),
(72,15,15,8,'wujie',NULL,4,'咖啡机操作简单。',0,NOW());
