#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
为 SP300001-SP301000 批量商品生成本地 SVG 图片，并生成图片绑定 SQL。

脚本只处理本批次商品图片：
- 不新增商品；
- 不修改价格、库存、销量、分类等业务字段；
- 生成 main / scene / detail 三张图；
- SQL 限定 product_no BETWEEN 'SP300001' AND 'SP301000'。
"""

from __future__ import annotations

import csv
import hashlib
import html
import os
import re
import shutil
import subprocess
import sys
from dataclasses import dataclass
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
FRONTEND_IMAGE_ROOT = ROOT / "前端" / "public" / "products" / "generated"
PLACEHOLDER = ROOT / "前端" / "public" / "products" / "placeholder.svg"
SQL_DIR = ROOT / "docs" / "sql"
INSERT_1000_SQL = SQL_DIR / "insert_1000_products.sql"
UPDATE_SQL = SQL_DIR / "update_1000_product_images.sql"
ROLLBACK_SQL = SQL_DIR / "rollback_1000_product_images.sql"

START_CODE = "SP300001"
END_CODE = "SP301000"

CATEGORY_SLUGS: dict[str, str] = {
    "数码科技": "digital",
    "手机通讯": "mobile",
    "电脑办公": "computer",
    "家居生活": "home",
    "服饰鞋包": "fashion",
    "美妆护肤": "beauty",
    "食品饮料": "food",
    "运动户外": "outdoor",
    "图书文具": "book",
    "母婴用品": "baby",
    "宠物用品": "pet",
    "礼品专区": "gift",
    "办公文具": "office",
    "箱包配饰": "bag",
    "服装鞋靴": "clothing",
    "健身器材": "fitness",
}

SLUG_PALETTES: dict[str, tuple[str, str, str, str]] = {
    "digital": ("#dbeafe", "#2563eb", "#0f172a", "#60a5fa"),
    "mobile": ("#e0f2fe", "#0284c7", "#0c4a6e", "#38bdf8"),
    "computer": ("#ede9fe", "#7c3aed", "#312e81", "#a78bfa"),
    "home": ("#dcfce7", "#16a34a", "#14532d", "#86efac"),
    "fashion": ("#fce7f3", "#db2777", "#831843", "#f9a8d4"),
    "clothing": ("#ffe4e6", "#e11d48", "#881337", "#fda4af"),
    "beauty": ("#fae8ff", "#c026d3", "#701a75", "#e879f9"),
    "food": ("#ffedd5", "#ea580c", "#7c2d12", "#fdba74"),
    "outdoor": ("#ccfbf1", "#0d9488", "#134e4a", "#5eead4"),
    "fitness": ("#dcfce7", "#22c55e", "#14532d", "#86efac"),
    "book": ("#fef3c7", "#d97706", "#78350f", "#fcd34d"),
    "baby": ("#fef9c3", "#ca8a04", "#713f12", "#fde047"),
    "pet": ("#ede9fe", "#8b5cf6", "#4c1d95", "#c4b5fd"),
    "gift": ("#fee2e2", "#ef4444", "#7f1d1d", "#fca5a5"),
    "office": ("#e2e8f0", "#475569", "#0f172a", "#94a3b8"),
    "bag": ("#f3e8ff", "#9333ea", "#581c87", "#d8b4fe"),
}

SCENE_TEXT: dict[str, tuple[str, str, str]] = {
    "digital": ("智能桌面", "高清体验", "高效连接"),
    "mobile": ("通勤随身", "轻巧便携", "稳定续航"),
    "computer": ("办公桌面", "高效输入", "清晰显示"),
    "home": ("舒适居家", "整洁收纳", "温暖空间"),
    "fashion": ("日常穿搭", "简洁有型", "轻松出行"),
    "clothing": ("通勤休闲", "舒适面料", "自然版型"),
    "beauty": ("清爽护理", "日常精致", "温和体验"),
    "food": ("休闲分享", "独立包装", "轻松补给"),
    "outdoor": ("户外训练", "稳定耐用", "轻便随行"),
    "fitness": ("居家健身", "耐用防滑", "科学训练"),
    "book": ("学习办公", "清晰记录", "专注阅读"),
    "baby": ("安心成长", "柔和亲肤", "便捷照护"),
    "pet": ("陪伴互动", "耐咬耐玩", "日常清洁"),
    "gift": ("节日心意", "精致礼赠", "祝福传递"),
    "office": ("高效办公", "桌面整洁", "耐用实用"),
    "bag": ("通勤出行", "容量充足", "轻装有序"),
}


@dataclass(frozen=True)
class ProductRow:
    product_no: str
    name: str
    category_id: str
    category_name: str
    brand_name: str
    price: str
    description: str
    slug: str


def run_mysql_query() -> list[ProductRow]:
    mysql_bin = os.environ.get("MYSQL_BIN", "mysql")
    db_name = os.environ.get("MALL_DB_NAME", "mall_system")
    user = os.environ.get("MALL_DB_USER") or os.environ.get("MYSQL_USERNAME", "root")
    password = os.environ.get("MALL_DB_PASSWORD") or os.environ.get("MYSQL_PASSWORD", "")
    host = os.environ.get("MALL_DB_HOST", "localhost")
    port = os.environ.get("MALL_DB_PORT", "3306")
    sql = (
        "SELECT product_no,name,category_id,category_name,IFNULL(brand_name,''),"
        "price,IFNULL(description,''),IFNULL(image_url,'') "
        "FROM product "
        f"WHERE product_no BETWEEN '{START_CODE}' AND '{END_CODE}' "
        "ORDER BY product_no"
    )
    cmd = [
        mysql_bin,
        f"-h{host}",
        f"-P{port}",
        "--protocol=TCP",
        f"-u{user}",
        "--default-character-set=utf8mb4",
        "-N",
        "-B",
        "-e",
        f"USE {db_name}; {sql}",
    ]
    if password:
        cmd.insert(4, f"-p{password}")
    result = subprocess.run(cmd, capture_output=True, text=True, encoding="utf-8", errors="replace")
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip() or "mysql query failed")
    rows: list[ProductRow] = []
    for parts in csv.reader(result.stdout.splitlines(), delimiter="\t"):
        if len(parts) < 8:
            continue
        rows.append(ProductRow(parts[0], parts[1], parts[2], parts[3], parts[4], parts[5], parts[6], slug_from_path_or_category(parts[7], parts[3])))
    return rows


def parse_insert_sql() -> list[ProductRow]:
    if not INSERT_1000_SQL.exists():
        raise FileNotFoundError(f"missing fallback sql: {INSERT_1000_SQL}")
    rows: list[ProductRow] = []
    pattern = re.compile(r"INSERT INTO product \((.*?)\) SELECT (.*?) FROM DUAL", re.IGNORECASE)
    for line in INSERT_1000_SQL.read_text(encoding="utf-8", errors="replace").splitlines():
        match = pattern.search(line)
        if not match:
            continue
        columns = [c.strip() for c in match.group(1).split(",")]
        values = split_sql_values(match.group(2))
        data = dict(zip(columns, values))
        product_no = unquote_sql(data.get("product_no", ""))
        if not (START_CODE <= product_no <= END_CODE):
            continue
        image_url = unquote_sql(data.get("image_url", ""))
        category_name = unquote_sql(data.get("category_name", ""))
        rows.append(
            ProductRow(
                product_no=product_no,
                name=unquote_sql(data.get("name", "")),
                category_id=data.get("category_id", "").strip(),
                category_name=category_name,
                brand_name=unquote_sql(data.get("brand_name", "")),
                price=data.get("price", "0").strip(),
                description=unquote_sql(data.get("description", "")),
                slug=slug_from_path_or_category(image_url, category_name),
            )
        )
    rows.sort(key=lambda item: item.product_no)
    return rows


def split_sql_values(text: str) -> list[str]:
    values: list[str] = []
    current: list[str] = []
    in_quote = False
    i = 0
    while i < len(text):
        ch = text[i]
        if ch == "'":
            current.append(ch)
            if in_quote and i + 1 < len(text) and text[i + 1] == "'":
                current.append("'")
                i += 2
                continue
            in_quote = not in_quote
        elif ch == "," and not in_quote:
            values.append("".join(current).strip())
            current = []
        else:
            current.append(ch)
        i += 1
    if current:
        values.append("".join(current).strip())
    return values


def unquote_sql(value: str) -> str:
    value = value.strip()
    if value.upper() == "NULL":
        return ""
    if len(value) >= 2 and value[0] == "'" and value[-1] == "'":
        return value[1:-1].replace("''", "'")
    return value


def slug_from_path_or_category(image_url: str, category_name: str) -> str:
    match = re.search(r"/products/generated/([^/]+)/", image_url or "")
    if match:
        return match.group(1)
    return CATEGORY_SLUGS.get(category_name, "digital")


def load_products() -> list[ProductRow]:
    try:
        rows = run_mysql_query()
        if rows:
            print(f"loaded {len(rows)} products from MySQL")
            return rows
    except Exception as exc:
        print(f"MySQL unavailable, fallback to insert SQL: {exc}")
    rows = parse_insert_sql()
    if not rows:
        raise RuntimeError("no SP300001-SP301000 products found")
    print(f"loaded {len(rows)} products from fallback SQL")
    return rows


def escape(value: str) -> str:
    return html.escape(str(value or ""), quote=True)


def digest_int(product_no: str, mod: int) -> int:
    return int(hashlib.sha1(product_no.encode("utf-8")).hexdigest()[:8], 16) % mod


def cleanup_batch_images() -> int:
    removed = 0
    for file in FRONTEND_IMAGE_ROOT.rglob("SP30*.svg"):
        if START_CODE <= file.stem.split("-")[0] <= END_CODE:
            file.unlink()
            removed += 1
    return removed


def write_placeholder() -> None:
    PLACEHOLDER.parent.mkdir(parents=True, exist_ok=True)
    PLACEHOLDER.write_text(
        """<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="900" viewBox="0 0 1200 900" role="img" aria-label="商品图片占位图">
  <rect width="1200" height="900" fill="#f1f5f9"/>
  <rect x="300" y="245" width="600" height="410" rx="34" fill="#ffffff" stroke="#cbd5e1" stroke-width="8"/>
  <circle cx="500" cy="390" r="72" fill="#e2e8f0"/>
  <path d="M345 610 L545 470 L665 565 L760 500 L855 610 Z" fill="#cbd5e1"/>
  <text x="600" y="715" text-anchor="middle" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="46" font-weight="700" fill="#64748b">暂无商品图片</text>
</svg>
""",
        encoding="utf-8",
    )


def product_paths(row: ProductRow) -> tuple[str, str, str]:
    base = f"/products/generated/{row.slug}/{row.product_no}"
    return f"{base}-main.svg", f"{base}-scene.svg", f"{base}-detail.svg"


def render_svg(row: ProductRow, image_type: str) -> str:
    bg, primary, dark, accent = SLUG_PALETTES.get(row.slug, SLUG_PALETTES["digital"])
    seed = digest_int(f"{row.product_no}-{image_type}", 1000)
    title = escape(row.name[:42])
    brand = escape(row.brand_name or "精选商城")
    category = escape(row.category_name)
    price = escape(row.price)
    desc = escape((row.description or row.name)[:58])
    scene_words = SCENE_TEXT.get(row.slug, SCENE_TEXT["digital"])
    word1, word2, word3 = [escape(w) for w in scene_words]
    item_x = 420 + seed % 80
    item_y = 260 + seed % 40
    if image_type == "main":
        headline = "商品主图"
        center = f"""
  <ellipse cx="610" cy="620" rx="270" ry="46" fill="{dark}" opacity="0.10"/>
  <rect x="{item_x}" y="{item_y}" width="360" height="300" rx="36" fill="#ffffff" stroke="{primary}" stroke-width="10"/>
  <rect x="{item_x + 42}" y="{item_y + 48}" width="276" height="160" rx="24" fill="{bg}"/>
  <circle cx="{item_x + 125}" cy="{item_y + 136}" r="50" fill="{accent}" opacity="0.85"/>
  <rect x="{item_x + 205}" y="{item_y + 96}" width="72" height="72" rx="18" fill="{primary}" opacity="0.82"/>
  <path d="M{item_x + 72} {item_y + 246} H{item_x + 288}" stroke="{dark}" stroke-width="18" stroke-linecap="round" opacity="0.35"/>
"""
    elif image_type == "scene":
        headline = "场景展示"
        center = f"""
  <rect x="120" y="555" width="960" height="100" rx="24" fill="#ffffff" opacity="0.86"/>
  <rect x="190" y="230" width="250" height="320" rx="30" fill="#ffffff" opacity="0.78"/>
  <rect x="480" y="190" width="300" height="370" rx="36" fill="#ffffff" stroke="{primary}" stroke-width="8"/>
  <rect x="822" y="260" width="190" height="280" rx="26" fill="#ffffff" opacity="0.72"/>
  <circle cx="626" cy="370" r="98" fill="{accent}" opacity="0.72"/>
  <text x="630" y="610" text-anchor="middle" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="34" font-weight="700" fill="{dark}">{word1} · {word2}</text>
"""
    else:
        headline = "细节亮点"
        center = f"""
  <rect x="155" y="235" width="890" height="390" rx="34" fill="#ffffff" opacity="0.92"/>
  <circle cx="305" cy="370" r="80" fill="{bg}" stroke="{primary}" stroke-width="8"/>
  <circle cx="600" cy="370" r="80" fill="{bg}" stroke="{primary}" stroke-width="8"/>
  <circle cx="895" cy="370" r="80" fill="{bg}" stroke="{primary}" stroke-width="8"/>
  <text x="305" y="383" text-anchor="middle" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="30" font-weight="700" fill="{dark}">{word1}</text>
  <text x="600" y="383" text-anchor="middle" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="30" font-weight="700" fill="{dark}">{word2}</text>
  <text x="895" y="383" text-anchor="middle" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="30" font-weight="700" fill="{dark}">{word3}</text>
  <text x="600" y="535" text-anchor="middle" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="36" fill="#475569">{desc}</text>
"""
    return f"""<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="900" viewBox="0 0 1200 900" role="img" aria-label="{title} {headline}">
  <defs>
    <linearGradient id="bg" x1="0" y1="0" x2="1" y2="1">
      <stop offset="0%" stop-color="{bg}"/>
      <stop offset="100%" stop-color="#ffffff"/>
    </linearGradient>
    <filter id="softShadow" x="-20%" y="-20%" width="140%" height="140%">
      <feDropShadow dx="0" dy="18" stdDeviation="18" flood-color="{dark}" flood-opacity="0.18"/>
    </filter>
  </defs>
  <rect width="1200" height="900" fill="url(#bg)"/>
  <circle cx="{140 + seed % 140}" cy="{140 + seed % 90}" r="120" fill="{accent}" opacity="0.22"/>
  <circle cx="{935 + seed % 90}" cy="{150 + seed % 120}" r="170" fill="{primary}" opacity="0.13"/>
  <rect x="86" y="86" width="1028" height="728" rx="48" fill="#ffffff" opacity="0.66" filter="url(#softShadow)"/>
  <text x="120" y="156" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="34" font-weight="700" fill="{primary}">{headline}</text>
  <text x="120" y="212" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="28" fill="{dark}">{brand} · {category}</text>
  {center}
  <text x="120" y="735" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="42" font-weight="800" fill="{dark}">{title}</text>
  <text x="120" y="790" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="34" font-weight="800" fill="#f97316">¥ {price}</text>
  <text x="1080" y="790" text-anchor="end" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="24" fill="#64748b">{escape(row.product_no)}</text>
</svg>
"""


def write_images(rows: list[ProductRow]) -> None:
    FRONTEND_IMAGE_ROOT.mkdir(parents=True, exist_ok=True)
    write_placeholder()
    removed = cleanup_batch_images()
    for row in rows:
        target_dir = FRONTEND_IMAGE_ROOT / row.slug
        target_dir.mkdir(parents=True, exist_ok=True)
        for image_type in ("main", "scene", "detail"):
            (target_dir / f"{row.product_no}-{image_type}.svg").write_text(render_svg(row, image_type), encoding="utf-8")
    print(f"removed old batch svgs: {removed}")
    print(f"generated svg files: {len(rows) * 3}")


def sql_quote(value: str) -> str:
    return "'" + (value or "").replace("\\", "\\\\").replace("'", "''") + "'"


def write_sql(rows: list[ProductRow]) -> None:
    SQL_DIR.mkdir(parents=True, exist_ok=True)
    update_lines = [
        "-- Generated by scripts/generate_product_images.py",
        "SET NAMES utf8mb4;",
        "START TRANSACTION;",
        f"DELETE pi FROM product_image pi JOIN product p ON p.id = pi.product_id WHERE p.product_no BETWEEN '{START_CODE}' AND '{END_CODE}';",
    ]
    rollback_lines = [
        "-- Generated by scripts/generate_product_images.py",
        "SET NAMES utf8mb4;",
        "START TRANSACTION;",
        f"DELETE pi FROM product_image pi JOIN product p ON p.id = pi.product_id WHERE p.product_no BETWEEN '{START_CODE}' AND '{END_CODE}';",
    ]
    for row in rows:
        main, scene, detail = product_paths(row)
        update_lines.append(
            "UPDATE product SET "
            f"image_url={sql_quote(main)}, "
            f"detail_image_url={sql_quote(scene + ',' + detail)}, "
            "update_time=NOW() "
            f"WHERE product_no={sql_quote(row.product_no)} AND product_no BETWEEN '{START_CODE}' AND '{END_CODE}';"
        )
        for sort_no, path in enumerate((main, scene, detail), start=1):
            update_lines.append(
                "INSERT INTO product_image (product_id,image_url,sort_no) "
                f"SELECT id,{sql_quote(path)},{sort_no} FROM product WHERE product_no={sql_quote(row.product_no)};"
            )
        old_main = f"/products/generated/{row.slug}/{row.product_no}-main.svg"
        old_detail_1 = f"/products/generated/{row.slug}/{row.product_no}-detail-1.svg"
        old_detail_2 = f"/products/generated/{row.slug}/{row.product_no}-detail-2.svg"
        rollback_lines.append(
            "UPDATE product SET "
            f"image_url={sql_quote(old_main)}, "
            f"detail_image_url={sql_quote(old_detail_1 + ',' + old_detail_2)}, "
            "update_time=NOW() "
            f"WHERE product_no={sql_quote(row.product_no)} AND product_no BETWEEN '{START_CODE}' AND '{END_CODE}';"
        )
        for sort_no, path in enumerate((old_main, old_detail_1, old_detail_2), start=1):
            rollback_lines.append(
                "INSERT INTO product_image (product_id,image_url,sort_no) "
                f"SELECT id,{sql_quote(path)},{sort_no} FROM product WHERE product_no={sql_quote(row.product_no)};"
            )
    update_lines.append("COMMIT;")
    rollback_lines.append("COMMIT;")
    UPDATE_SQL.write_text("\n".join(update_lines) + "\n", encoding="utf-8")
    ROLLBACK_SQL.write_text("\n".join(rollback_lines) + "\n", encoding="utf-8")
    print(f"wrote {UPDATE_SQL}")
    print(f"wrote {ROLLBACK_SQL}")


def main() -> int:
    rows = load_products()
    if len(rows) != 1000:
        raise RuntimeError(f"expected 1000 products, got {len(rows)}")
    missing_slugs = sorted({row.slug for row in rows if row.slug not in SLUG_PALETTES})
    if missing_slugs:
        raise RuntimeError(f"unsupported category slugs: {missing_slugs}")
    write_images(rows)
    write_sql(rows)
    return 0


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"generate product images failed: {exc}", file=sys.stderr)
        raise SystemExit(1)
