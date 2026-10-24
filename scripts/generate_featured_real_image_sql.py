#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为每个分类挑选 3 个 SP300001-SP301000 商品绑定真实摄影图 URL。

只更新：
- product.image_url
- product.detail_image_url
- product.is_recommended / product.is_new
- product_image 关联图

不修改价格、库存、销量、分类、订单、用户、AI 逻辑。
"""

from __future__ import annotations

import csv
import subprocess
from collections import defaultdict
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
SQL_DIR = ROOT / "docs" / "sql"
UPDATE_SQL = SQL_DIR / "update_featured_real_product_images.sql"
ROLLBACK_SQL = SQL_DIR / "rollback_featured_real_product_images.sql"

START_CODE = "SP300001"
END_CODE = "SP301000"


def img(photo_id: str) -> str:
    return f"https://images.unsplash.com/{photo_id}?auto=format&fit=crop&w=1200&h=900&q=85"


IMAGE_SETS: dict[str, list[str]] = {
    "数码科技": [
        img("photo-1516321318423-f06f85e504b3"),
        img("photo-1505740420928-5e560c06d30e"),
        img("photo-1517336714731-489689fd1ca8"),
    ],
    "时尚服饰": [
        img("photo-1521572163474-6864f9cf17ab"),
        img("photo-1542291026-7eec264c27ff"),
        img("photo-1489987707025-afc232f7ea0f"),
    ],
    "家居生活": [
        img("photo-1513506003901-1e6a229e2d15"),
        img("photo-1555041469-a586c61ea9bc"),
        img("photo-1505693416388-ac5ce068fe85"),
    ],
    "美妆护肤": [
        img("photo-1596462502278-27bfdc403348"),
        img("photo-1522335789203-aabd1fc54bc9"),
        img("photo-1612817288484-6f916006741a"),
    ],
    "食品饮料": [
        img("photo-1495474472287-4d71bcdd2085"),
        img("photo-1509042239860-f550ce710b93"),
        img("photo-1542838132-92c53300491e"),
    ],
    "运动户外": [
        img("photo-1517649763962-0c623066013b"),
        img("photo-1571019613454-1cb2f99b2d8b"),
        img("photo-1518611012118-696072aa579a"),
    ],
    "母婴用品": [
        img("photo-1519689680058-324335c77eba"),
        img("photo-1515488042361-ee00e0ddd4e4"),
        img("photo-1503454537195-1dcabb73ffb9"),
    ],
    "办公文具": [
        img("photo-1497366754035-f200968a6e72"),
        img("photo-1516321497487-e288fb19713f"),
        img("photo-1515378791036-0648a3ef77b2"),
    ],
    "箱包配饰": [
        img("photo-1553062407-98eeb64c6a62"),
        img("photo-1547949003-9792a18a2601"),
        img("photo-1590874103328-eac38a683ce7"),
    ],
    "图书音像": [
        img("photo-1512820790803-83ca734da794"),
        img("photo-1495446815901-a7297e633e8d"),
        img("photo-1524995997946-a1c2e315a42f"),
    ],
    "手机通讯": [
        img("photo-1511707171634-5f897ff02aa9"),
        img("photo-1512499617640-c2f999098c01"),
        img("photo-1580910051074-3eb694886505"),
    ],
    "电脑办公": [
        img("photo-1496181133206-80ce9b88a853"),
        img("photo-1517336714731-489689fd1ca8"),
        img("photo-1527443224154-c4a3942d3acf"),
    ],
    "服饰鞋包": [
        img("photo-1542291026-7eec264c27ff"),
        img("photo-1483985988355-763728e1935b"),
        img("photo-1523381210434-271e8be1f52b"),
    ],
    "宠物用品": [
        img("photo-1548199973-03cce0bbc87b"),
        img("photo-1534361960057-19889db9621e"),
        img("photo-1518717758536-85ae29035b6d"),
    ],
    "礼品专区": [
        img("photo-1513201099705-a9746e1e201f"),
        img("photo-1549465220-1a8b9238cd48"),
        img("photo-1513885535751-8b9238bd345a"),
    ],
    "健身": [
        img("photo-1571019613454-1cb2f99b2d8b"),
        img("photo-1518611012118-696072aa579a"),
        img("photo-1534258936925-c58bed479fcb"),
    ],
}


def mysql_rows(sql: str) -> list[list[str]]:
    cmd = [
        "mysql",
        "-hlocalhost",
        "-P3306",
        "--protocol=TCP",
        "-uroot",
        "-p123456",
        "--default-character-set=utf8mb4",
        "-N",
        "-B",
        "-e",
        f"USE mall_system; {sql}",
    ]
    result = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip())
    return list(csv.reader(result.stdout.splitlines(), delimiter="\t"))


def quote(value: str) -> str:
    return "'" + (value or "").replace("\\", "\\\\").replace("'", "''") + "'"


def selected_products() -> list[dict[str, str]]:
    rows = mysql_rows(
        "SELECT p.product_no,p.name,p.category_name,IFNULL(p.image_url,''),IFNULL(p.detail_image_url,''),"
        "IFNULL(p.is_recommended,0),IFNULL(p.is_new,0) "
        "FROM product p "
        f"WHERE p.product_no BETWEEN '{START_CODE}' AND '{END_CODE}' "
        "ORDER BY p.category_id,p.product_no"
    )
    grouped: dict[str, list[dict[str, str]]] = defaultdict(list)
    for row in rows:
        if len(row) < 7:
            continue
        item = {
            "product_no": row[0],
            "name": row[1],
            "category_name": row[2],
            "old_image_url": row[3],
            "old_detail_image_url": row[4],
            "old_is_recommended": row[5],
            "old_is_new": row[6],
        }
        if row[2] in IMAGE_SETS and len(grouped[row[2]]) < 3:
            grouped[row[2]].append(item)
    selected: list[dict[str, str]] = []
    for category, items in grouped.items():
        for idx, item in enumerate(items):
            urls = IMAGE_SETS[category]
            item["new_image_url"] = urls[idx % len(urls)]
            item["new_detail_image_url"] = ",".join(urls)
            item["new_images"] = urls
            selected.append(item)
    return selected


def main() -> None:
    SQL_DIR.mkdir(parents=True, exist_ok=True)
    products = selected_products()
    if len(products) != 48:
        raise RuntimeError(f"expected 48 products, got {len(products)}")

    update = [
        "-- Generated by scripts/generate_featured_real_image_sql.py",
        "SET NAMES utf8mb4;",
        "START TRANSACTION;",
    ]
    rollback = [
        "-- Generated by scripts/generate_featured_real_image_sql.py",
        "SET NAMES utf8mb4;",
        "START TRANSACTION;",
    ]
    codes = ",".join(quote(p["product_no"]) for p in products)
    update.append(f"DELETE pi FROM product_image pi JOIN product p ON p.id=pi.product_id WHERE p.product_no IN ({codes});")
    rollback.append(f"DELETE pi FROM product_image pi JOIN product p ON p.id=pi.product_id WHERE p.product_no IN ({codes});")

    for product in products:
        code = product["product_no"]
        update.append(
            "UPDATE product SET "
            f"image_url={quote(product['new_image_url'])}, "
            f"detail_image_url={quote(product['new_detail_image_url'])}, "
            "is_recommended=1, is_new=1, update_time=NOW() "
            f"WHERE product_no={quote(code)} AND product_no BETWEEN '{START_CODE}' AND '{END_CODE}';"
        )
        for sort_no, url in enumerate(product["new_images"], 1):
            update.append(
                "INSERT INTO product_image (product_id,image_url,sort_no) "
                f"SELECT id,{quote(url)},{sort_no} FROM product WHERE product_no={quote(code)};"
            )

        old_images = [product["old_image_url"]]
        old_images.extend([p for p in product["old_detail_image_url"].split(",") if p])
        old_images = old_images[:3] or [product["old_image_url"]]
        rollback.append(
            "UPDATE product SET "
            f"image_url={quote(product['old_image_url'])}, "
            f"detail_image_url={quote(product['old_detail_image_url'])}, "
            f"is_recommended={product['old_is_recommended']}, is_new={product['old_is_new']}, update_time=NOW() "
            f"WHERE product_no={quote(code)} AND product_no BETWEEN '{START_CODE}' AND '{END_CODE}';"
        )
        for sort_no, url in enumerate(old_images, 1):
            rollback.append(
                "INSERT INTO product_image (product_id,image_url,sort_no) "
                f"SELECT id,{quote(url)},{sort_no} FROM product WHERE product_no={quote(code)};"
            )

    update.append("COMMIT;")
    rollback.append("COMMIT;")
    UPDATE_SQL.write_text("\n".join(update) + "\n", encoding="utf-8")
    ROLLBACK_SQL.write_text("\n".join(rollback) + "\n", encoding="utf-8")
    print(f"selected={len(products)}")
    for product in products:
        print(f"{product['product_no']}\t{product['category_name']}\t{product['new_image_url']}")
    print(UPDATE_SQL)
    print(ROLLBACK_SQL)


if __name__ == "__main__":
    main()
