-- Verification SQL for generated 1000 products.
SET NAMES utf8mb4;

-- 1. 新增数量，预期 1000
SELECT COUNT(*) AS generated_product_count
FROM product
WHERE product_no BETWEEN 'SP300001' AND 'SP301000';

-- 2. 编号重复，预期 0 行
SELECT product_no, COUNT(*) AS duplicate_count
FROM product
GROUP BY product_no
HAVING COUNT(*) > 1;

-- 3. 新增商品名称重复，预期 0 行
SELECT name, COUNT(*) AS duplicate_count
FROM product
WHERE product_no BETWEEN 'SP300001' AND 'SP301000'
GROUP BY name
HAVING COUNT(*) > 1;

-- 4. 分类统计，actual_count 应等于 expected_count
WITH expected AS (
SELECT 1 AS category_id, '数码科技' AS category_name, 63 AS expected_count
UNION ALL
SELECT 2 AS category_id, '时尚服饰' AS category_name, 62 AS expected_count
UNION ALL
SELECT 3 AS category_id, '家居生活' AS category_name, 63 AS expected_count
UNION ALL
SELECT 4 AS category_id, '美妆护肤' AS category_name, 62 AS expected_count
UNION ALL
SELECT 5 AS category_id, '食品饮料' AS category_name, 63 AS expected_count
UNION ALL
SELECT 6 AS category_id, '运动户外' AS category_name, 62 AS expected_count
UNION ALL
SELECT 7 AS category_id, '母婴用品' AS category_name, 63 AS expected_count
UNION ALL
SELECT 8 AS category_id, '办公文具' AS category_name, 62 AS expected_count
UNION ALL
SELECT 9 AS category_id, '箱包配饰' AS category_name, 63 AS expected_count
UNION ALL
SELECT 10 AS category_id, '图书音像' AS category_name, 62 AS expected_count
UNION ALL
SELECT 11 AS category_id, '手机通讯' AS category_name, 63 AS expected_count
UNION ALL
SELECT 12 AS category_id, '电脑办公' AS category_name, 62 AS expected_count
UNION ALL
SELECT 13 AS category_id, '服饰鞋包' AS category_name, 63 AS expected_count
UNION ALL
SELECT 14 AS category_id, '宠物用品' AS category_name, 62 AS expected_count
UNION ALL
SELECT 15 AS category_id, '礼品专区' AS category_name, 63 AS expected_count
UNION ALL
SELECT 16 AS category_id, '健身' AS category_name, 62 AS expected_count
),
actual AS (
  SELECT category_id, COUNT(*) AS actual_count
  FROM product
  WHERE product_no BETWEEN 'SP300001' AND 'SP301000'
  GROUP BY category_id
)
SELECT e.category_id, e.category_name, e.expected_count, COALESCE(a.actual_count, 0) AS actual_count
FROM expected e
LEFT JOIN actual a ON a.category_id = e.category_id
ORDER BY e.category_id;

-- 5. 空字段，预期 0
SELECT COUNT(*) AS empty_required_field_count
FROM product
WHERE product_no BETWEEN 'SP300001' AND 'SP301000'
  AND (
    product_no IS NULL OR product_no = ''
    OR name IS NULL OR name = ''
    OR category_id IS NULL
    OR category_name IS NULL OR category_name = ''
    OR price IS NULL
    OR image_url IS NULL OR image_url = ''
    OR status IS NULL
  );

-- 6. 非法值，预期 0
SELECT COUNT(*) AS invalid_value_count
FROM product
WHERE product_no BETWEEN 'SP300001' AND 'SP301000'
  AND (price <= 0 OR stock < 0 OR sales < 0 OR market_price < price);

-- 7. 新增商品全部上架，预期 1000
SELECT COUNT(*) AS generated_on_sale_count
FROM product
WHERE product_no BETWEEN 'SP300001' AND 'SP301000' AND status = 1;

-- 8. 图片关联数量，预期 3000
SELECT COUNT(*) AS generated_product_image_count
FROM product_image pi
JOIN product p ON p.id = pi.product_id
WHERE p.product_no BETWEEN 'SP300001' AND 'SP301000';

-- 9. SKU 数量，预期 1000
SELECT COUNT(*) AS generated_sku_count
FROM product_sku ps
JOIN product p ON p.id = ps.product_id
WHERE p.product_no BETWEEN 'SP300001' AND 'SP301000';
