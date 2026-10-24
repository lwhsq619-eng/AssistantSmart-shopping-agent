#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""校验 1000 商品生成文件，不连接数据库。"""

from __future__ import annotations

import json
import re
from collections import Counter
from pathlib import Path

ROOT = Path(__file__).resolve().parents[1]
INSERT_SQL = ROOT / "docs" / "sql" / "insert_1000_products.sql"
ROLLBACK_SQL = ROOT / "docs" / "sql" / "rollback_1000_products.sql"
IMAGE_ROOT = ROOT / "前端" / "public" / "products" / "generated"


def main() -> None:
    sql = INSERT_SQL.read_text(encoding="utf-8")
    rollback = ROLLBACK_SQL.read_text(encoding="utf-8")
    product_rows = re.findall(
        r"SELECT '([^']+)','([^']+)',(\d+),'([^']+)',NULL,'([^']+)',"
        r"'([^']+)','([^']+)',([0-9.]+),([0-9.]+),([0-9.]+),(\d+),(\d+),",
        sql,
    )
    codes = [row[0] for row in product_rows]
    names = [row[1] for row in product_rows]
    category_ids = [int(row[2]) for row in product_rows]
    image_urls = [row[5] for row in product_rows]
    prices = [float(row[7]) for row in product_rows]
    market_prices = [float(row[8]) for row in product_rows]
    stocks = [int(row[10]) for row in product_rows]
    sales = [int(row[11]) for row in product_rows]

    image_paths = list(IMAGE_ROOT.rglob("SP*.svg"))
    expected_image_files = []
    for code, category_id in zip(codes, category_ids):
        slug = {
            1: "digital", 2: "fashion", 3: "home", 4: "beauty",
            5: "food", 6: "outdoor", 7: "baby", 8: "office",
            9: "bag", 10: "book", 11: "mobile", 12: "computer",
            13: "clothing", 14: "pet", 15: "gift", 16: "fitness",
        }[category_id]
        expected_image_files.extend([
            IMAGE_ROOT / slug / f"{code}-main.svg",
            IMAGE_ROOT / slug / f"{code}-detail-1.svg",
            IMAGE_ROOT / slug / f"{code}-detail-2.svg",
        ])

    result = {
        "generated_products": len(product_rows),
        "category_count": len(set(category_ids)),
        "category_stats": dict(sorted(Counter(category_ids).items())),
        "duplicate_code_count": sum(1 for count in Counter(codes).values() if count > 1),
        "duplicate_name_count": sum(1 for count in Counter(names).values() if count > 1),
        "image_file_count": len(image_paths),
        "main_image_count": sum(1 for p in image_paths if p.name.endswith("-main.svg")),
        "detail_image_count": sum(1 for p in image_paths if "-detail-" in p.name),
        "missing_image_count": sum(1 for p in expected_image_files if not p.exists()),
        "duplicate_image_path_count": len(image_urls) - len(set(image_urls)),
        "invalid_price_count": sum(1 for price in prices if price <= 0),
        "invalid_stock_count": sum(1 for stock in stocks if stock < 0),
        "invalid_sales_count": sum(1 for sale in sales if sale < 0),
        "invalid_market_price_count": sum(1 for price, market in zip(prices, market_prices) if market < price),
        "invalid_category_count": sum(1 for cid in category_ids if cid not in set(range(1, 17))),
        "sql_has_transaction": "START TRANSACTION;" in sql and "COMMIT;" in sql,
        "rollback_scoped": "product_no BETWEEN 'SP300001' AND 'SP301000'" in rollback,
        "rollback_has_transaction": "START TRANSACTION;" in rollback and "COMMIT;" in rollback,
        "rollback_deletes_images": "DELETE pi" in rollback and "product_image" in rollback,
        "rollback_deletes_skus": "DELETE ps" in rollback and "product_sku" in rollback,
    }
    print(json.dumps(result, ensure_ascii=False, indent=2))


if __name__ == "__main__":
    main()
