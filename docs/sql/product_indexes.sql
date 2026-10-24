-- Optional performance indexes for product list after importing 1000 products.
-- Existing indexes before this script:
--   product_no unique index: exists
--   idx_product_category(category_id): exists
-- This script only adds status index when it is missing.

SET @index_exists := (
  SELECT COUNT(1)
  FROM information_schema.statistics
  WHERE table_schema = DATABASE()
    AND table_name = 'product'
    AND index_name = 'idx_product_status'
);

SET @ddl := IF(
  @index_exists = 0,
  'ALTER TABLE product ADD INDEX idx_product_status (status)',
  'SELECT ''idx_product_status already exists'' AS message'
);

PREPARE stmt FROM @ddl;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;
