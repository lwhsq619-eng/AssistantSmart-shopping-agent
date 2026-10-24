USE mall_system;

-- AI 演示数据：只追加，不覆盖已有数据。

INSERT IGNORE INTO sys_user (id, username, password, phone, email, address, status, role, avatar, avatar_url, register_time, create_time, update_time)
VALUES
(9001, 'demo_user1', '123456', '13800009001', 'demo1@example.com', '广东省广州市天河区演示路1号', 1, 'USER', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=240&q=80', 'https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=240&q=80', NOW(), NOW(), NOW()),
(9002, 'demo_user2', '123456', '13800009002', 'demo2@example.com', '广东省深圳市南山区演示路2号', 1, 'USER', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=240&q=80', 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?auto=format&fit=crop&w=240&q=80', NOW(), NOW(), NOW()),
(9003, 'demo_admin_ai', '123456', '13800009003', 'adminai@example.com', '运营中心', 1, 'ADMIN', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=240&q=80', 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=240&q=80', NOW(), NOW(), NOW());

INSERT IGNORE INTO product_category (id, name, description, sort_no, status, image_url, create_time, update_time)
VALUES
(901, 'AI演示数码', 'AI 导购与语义搜索演示分类', 901, 1, 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=800&q=80', NOW(), NOW()),
(902, 'AI演示家居', '场景搭配与宿舍用品演示分类', 902, 1, 'https://images.unsplash.com/photo-1555041469-a586c61ea9bc?auto=format&fit=crop&w=800&q=80', NOW(), NOW());

INSERT IGNORE INTO product (id, product_no, name, category_id, category_name, brand_id, brand_name, image_url, detail_image_url, price, market_price, discount_price, stock, sales, rating, review_count, description, specs, status, is_special, is_recommended, is_new, create_time, update_time)
VALUES
(9101, 'AI-DEMO-001', '运动蓝牙耳机', 901, 'AI演示数码', 1, 'NovaTech 数码生活', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=1200&q=80', 199.00, 299.00, 179.00, 80, 320, 4.8, 80, '轻便防水，适合跑步、通勤和运动场景。', '颜色: 黑色/白色; 连接: 蓝牙; 防水: 生活防水', 1, 1, 1, 1, NOW(), NOW()),
(9102, 'AI-DEMO-002', '机械键盘', 901, 'AI演示数码', 1, 'NovaTech 数码生活', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=800&q=80', 'https://images.unsplash.com/photo-1587829741301-dc798b83add3?auto=format&fit=crop&w=1200&q=80', 269.00, 399.00, 239.00, 35, 210, 4.7, 62, '适合办公、宿舍学习和游戏输入。', '轴体: 青轴/茶轴; 布局: 87键', 1, 1, 1, 1, NOW(), NOW()),
(9103, 'AI-DEMO-003', '手机支架', 901, 'AI演示数码', 1, 'NovaTech 数码生活', 'https://images.unsplash.com/photo-1512428559087-560fa5ceab42?auto=format&fit=crop&w=800&q=80', 'https://images.unsplash.com/photo-1512428559087-560fa5ceab42?auto=format&fit=crop&w=1200&q=80', 39.90, 69.00, 29.90, 120, 500, 4.6, 120, '桌面可折叠支架，适合网课、追剧和办公。', '材质: 铝合金; 角度: 可调节', 1, 1, 1, 0, NOW(), NOW()),
(9104, 'AI-DEMO-004', '宿舍护眼台灯', 902, 'AI演示家居', 2, 'Mori Home 家居美学', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=800&q=80', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=1200&q=80', 89.00, 129.00, 79.00, 66, 260, 4.7, 70, '三档调光，适合宿舍夜间学习。', '色温: 三档; 供电: USB', 1, 1, 1, 1, NOW(), NOW()),
(9105, 'AI-DEMO-005', '桌面收纳盒', 902, 'AI演示家居', 2, 'Mori Home 家居美学', 'https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=800&q=80', 'https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=1200&q=80', 49.00, 79.00, 39.00, 95, 180, 4.5, 34, '适合宿舍桌面、办公桌和化妆台收纳。', '规格: 三层; 材质: PP', 1, 0, 1, 0, NOW(), NOW()),
(9106, 'AI-DEMO-006', '零食礼盒', 902, 'AI演示家居', 4, 'Pure Glow 美妆护肤', 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=800&q=80', 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=1200&q=80', 99.00, 139.00, 89.00, 55, 230, 4.6, 40, '适合生日、节日和宿舍分享。', '口味: 混合装; 包装: 礼盒', 1, 1, 1, 1, NOW(), NOW()),
(9107, 'AI-DEMO-007', '咖啡礼盒', 902, 'AI演示家居', 4, 'Pure Glow 美妆护肤', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=800&q=80', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?auto=format&fit=crop&w=1200&q=80', 129.00, 199.00, 109.00, 40, 150, 4.7, 38, '适合作为生日礼物和办公室分享。', '规格: 挂耳咖啡; 包装: 礼盒', 1, 1, 0, 1, NOW(), NOW()),
(9108, 'AI-DEMO-008', '运动鞋', 902, 'AI演示家居', 3, 'Urban Step 潮流鞋服', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=1200&q=80', 299.00, 399.00, 269.00, 28, 300, 4.8, 86, '轻量缓震，适合跑步和日常通勤。', '尺码: 39-44; 颜色: 黑/白', 1, 1, 1, 0, NOW(), NOW()),
(9109, 'AI-DEMO-009', '保温杯', 902, 'AI演示家居', 2, 'Mori Home 家居美学', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=800&q=80', 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?auto=format&fit=crop&w=1200&q=80', 59.00, 99.00, 49.00, 88, 176, 4.5, 31, '适合学生、上班族和户外携带。', '容量: 500ml; 材质: 不锈钢', 1, 0, 1, 0, NOW(), NOW()),
(9110, 'AI-DEMO-010', '手机快充套装', 901, 'AI演示数码', 1, 'NovaTech 数码生活', 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?auto=format&fit=crop&w=800&q=80', 'https://images.unsplash.com/photo-1583863788434-e58a36330cf0?auto=format&fit=crop&w=1200&q=80', 79.00, 119.00, 69.00, 72, 260, 4.6, 58, '包含充电器和数据线，适合手机配件组合。', '功率: 30W; 接口: Type-C', 1, 1, 1, 1, NOW(), NOW());

INSERT IGNORE INTO orders (id, order_no, user_id, user_name, total_amount, pay_amount, shipping_address, status, order_time, create_time, update_time)
VALUES
(9901, 'AI202607090001', 9001, 'demo_user1', 179.00, 179.00, '广东省广州市天河区演示路1号', '待发货', NOW(), NOW(), NOW()),
(9902, 'AI202607090002', 9001, 'demo_user1', 118.00, 118.00, '广东省广州市天河区演示路1号', '待收货', NOW(), NOW(), NOW()),
(9903, 'AI202607090003', 9002, 'demo_user2', 269.00, 269.00, '广东省深圳市南山区演示路2号', '已完成', NOW(), NOW(), NOW()),
(9904, 'AI202607090004', 9002, 'demo_user2', 89.00, 89.00, '广东省深圳市南山区演示路2号', '已完成', NOW(), NOW(), NOW()),
(9905, 'AI202607090005', 9001, 'demo_user1', 99.00, 0.00, '广东省广州市天河区演示路1号', '待支付', NOW(), NOW(), NOW());

INSERT IGNORE INTO order_item (id, order_id, product_id, product_name, product_image, quantity, unit_price, subtotal, create_time, update_time)
VALUES
(9951, 9901, 9101, '运动蓝牙耳机', 'https://images.unsplash.com/photo-1505740420928-5e560c06d30e?auto=format&fit=crop&w=800&q=80', 1, 179.00, 179.00, NOW(), NOW()),
(9952, 9902, 9104, '宿舍护眼台灯', 'https://images.unsplash.com/photo-1507473885765-e6ed057f782c?auto=format&fit=crop&w=800&q=80', 1, 79.00, 79.00, NOW(), NOW()),
(9953, 9902, 9105, '桌面收纳盒', 'https://images.unsplash.com/photo-1513519245088-0e12902e5a38?auto=format&fit=crop&w=800&q=80', 1, 39.00, 39.00, NOW(), NOW()),
(9954, 9903, 9108, '运动鞋', 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=800&q=80', 1, 269.00, 269.00, NOW(), NOW()),
(9955, 9904, 9106, '零食礼盒', 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=800&q=80', 1, 89.00, 89.00, NOW(), NOW());

INSERT INTO product_review (product_id, order_id, order_item_id, user_id, username, rating, content, anonymous, create_time)
VALUES
(9101, 9901, 9951, 9001, 'demo_user1', 5, '耳机很轻，跑步佩戴稳定，音质也不错。', 0, NOW()),
(9101, 9901, 9951, 9001, 'demo_user1', 4, '续航可以，适合通勤。', 0, NOW()),
(9102, 9903, 9954, 9002, 'demo_user2', 5, '键盘手感不错，宿舍学习很合适。', 0, NOW()),
(9104, 9902, 9952, 9001, 'demo_user1', 5, '台灯亮度够用，不刺眼。', 0, NOW()),
(9105, 9902, 9953, 9001, 'demo_user1', 4, '收纳盒容量还行，桌面整洁很多。', 0, NOW()),
(9106, 9904, 9955, 9002, 'demo_user2', 5, '礼盒包装好看，送朋友很合适。', 0, NOW()),
(9108, 9903, 9954, 9002, 'demo_user2', 5, '运动鞋轻便，跑步舒服。', 0, NOW()),
(9109, 9904, 9955, 9002, 'demo_user2', 3, '保温杯还可以，物流稍慢。', 0, NOW()),
(9110, 9901, 9951, 9001, 'demo_user1', 4, '快充套装很实用。', 0, NOW()),
(9107, 9904, 9955, 9002, 'demo_user2', 2, '包装有点压坏，希望改进。', 0, NOW());

INSERT IGNORE INTO gift_card (id, name, amount, image_url, description, status)
VALUES
(9901, 'AI演示100元礼品卡', 100.00, 'https://images.unsplash.com/photo-1513201099705-a9746e1e201f?auto=format&fit=crop&w=800&q=80', '用于 AI 礼品卡演示', 1);
