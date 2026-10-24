#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
校验 SP300001-SP301000 本地商品图片与数据库绑定。

数据库不可连接时仍校验本地文件，并在报告中标记 DB_UNAVAILABLE。
"""

from __future__ import annotations

import csv
import json
import os
import random
import re
import subprocess
import sys
from collections import Counter, defaultdict
from dataclasses import dataclass, field
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
FRONTEND_IMAGE_ROOT = ROOT / "前端" / "public" / "products" / "generated"
REPORT = ROOT / "docs" / "PRODUCT_IMAGE_REPORT.md"
START_CODE = "SP300001"
END_CODE = "SP301000"
EXPECTED_PER_PRODUCT = ("main", "scene", "detail")
EXPECTED_EXT = os.environ.get("PRODUCT_IMAGE_EXT", "png").lower().lstrip(".")


@dataclass
class DbProduct:
    product_no: str
    image_url: str
    detail_image_url: str


@dataclass
class VerifySummary:
    product_count: int = 0
    main_count: int = 0
    scene_count: int = 0
    detail_count: int = 0
    total_expected_images: int = 0
    total_existing_images: int = 0
    missing_files: list[str] = field(default_factory=list)
    invalid_svg: list[str] = field(default_factory=list)
    duplicate_paths: list[str] = field(default_factory=list)
    db_mismatches: list[str] = field(default_factory=list)
    db_available: bool = False
    db_error: str = ""
    sampled_48_ok: bool = False
    random_100_ok: bool = False


def run_mysql(sql: str) -> list[list[str]]:
    mysql_bin = os.environ.get("MYSQL_BIN", "mysql")
    db_name = os.environ.get("MALL_DB_NAME", "mall_system")
    user = os.environ.get("MALL_DB_USER") or os.environ.get("MYSQL_USERNAME", "root")
    password = os.environ.get("MALL_DB_PASSWORD") or os.environ.get("MYSQL_PASSWORD", "")
    host = os.environ.get("MALL_DB_HOST", "localhost")
    port = os.environ.get("MALL_DB_PORT", "3306")
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
    return list(csv.reader(result.stdout.splitlines(), delimiter="\t"))


def load_db_products() -> tuple[dict[str, DbProduct], dict[str, list[tuple[str, str]]]]:
    product_rows = run_mysql(
        "SELECT product_no,IFNULL(image_url,''),IFNULL(detail_image_url,'') "
        "FROM product "
        f"WHERE product_no BETWEEN '{START_CODE}' AND '{END_CODE}' ORDER BY product_no"
    )
    image_rows = run_mysql(
        "SELECT p.product_no,pi.image_url,pi.sort_no "
        "FROM product_image pi JOIN product p ON p.id=pi.product_id "
        f"WHERE p.product_no BETWEEN '{START_CODE}' AND '{END_CODE}' ORDER BY p.product_no,pi.sort_no"
    )
    products = {row[0]: DbProduct(row[0], row[1], row[2]) for row in product_rows if len(row) >= 3}
    images: dict[str, list[tuple[str, str]]] = defaultdict(list)
    for row in image_rows:
        if len(row) >= 3:
            images[row[0]].append((row[1], row[2]))
    return products, images


def all_batch_files() -> list[Path]:
    return sorted(
        file
        for file in FRONTEND_IMAGE_ROOT.rglob(f"SP30*.{EXPECTED_EXT}")
        if START_CODE <= file.stem.split("-")[0] <= END_CODE
    )


def expected_paths_from_files() -> dict[str, dict[str, str]]:
    mapping: dict[str, dict[str, str]] = defaultdict(dict)
    for file in all_batch_files():
        product_no = file.stem.split("-")[0]
        kind = file.stem.removeprefix(product_no + "-")
        if kind in EXPECTED_PER_PRODUCT:
            rel = "/" + file.relative_to(ROOT / "前端" / "public").as_posix()
            mapping[product_no][kind] = rel
    return mapping


def validate_file(file: Path) -> bool:
    if file.suffix.lower() == ".png":
        try:
            data = file.read_bytes()
        except OSError:
            return False
        if len(data) < 33 or data[:8] != b"\x89PNG\r\n\x1a\n":
            return False
        width = int.from_bytes(data[16:20], "big")
        height = int.from_bytes(data[20:24], "big")
        return width == 1200 and height == 900
    try:
        text = file.read_text(encoding="utf-8", errors="replace")
    except OSError:
        return False
    return (
        "<svg" in text[:300]
        and "</svg>" in text[-200:]
        and ('width="1200"' in text or "width='1200'" in text)
        and ('height="900"' in text or "height='900'" in text)
        and not re.search(r"\b(?:href|src|xlink:href)=['\"]https?://", text, re.IGNORECASE)
    )


def sample_48(mapping: dict[str, dict[str, str]]) -> bool:
    by_slug: dict[str, list[str]] = defaultdict(list)
    for product_no, kinds in mapping.items():
        path = kinds.get("main", "")
        match = re.search(r"/products/generated/([^/]+)/", path)
        if match:
            by_slug[match.group(1)].append(product_no)
    checked = []
    for values in by_slug.values():
        checked.extend(sorted(values)[:3])
    if len(checked) < 48:
        return False
    return all(all(kind in mapping.get(product_no, {}) for kind in EXPECTED_PER_PRODUCT) for product_no in checked[:48])


def random_100(mapping: dict[str, dict[str, str]]) -> bool:
    product_nos = sorted(mapping)
    random.seed(20260711)
    sample = random.sample(product_nos, min(100, len(product_nos)))
    return len(sample) == 100 and all(all(kind in mapping.get(product_no, {}) for kind in EXPECTED_PER_PRODUCT) for product_no in sample)


def verify() -> VerifySummary:
    summary = VerifySummary()
    files = all_batch_files()
    mapping = expected_paths_from_files()
    summary.product_count = len(mapping)
    summary.main_count = sum(1 for kinds in mapping.values() if "main" in kinds)
    summary.scene_count = sum(1 for kinds in mapping.values() if "scene" in kinds)
    summary.detail_count = sum(1 for kinds in mapping.values() if "detail" in kinds)
    summary.total_expected_images = summary.product_count * 3
    summary.total_existing_images = len(files)
    for product_no in [f"SP{num:06d}" for num in range(300001, 301001)]:
        for kind in EXPECTED_PER_PRODUCT:
            if kind not in mapping.get(product_no, {}):
                summary.missing_files.append(f"{product_no}-{kind}.{EXPECTED_EXT}")
    summary.invalid_svg = [str(file.relative_to(ROOT)) for file in files if not validate_file(file)]
    path_counter = Counter(path for kinds in mapping.values() for path in kinds.values())
    summary.duplicate_paths = [path for path, count in path_counter.items() if count > 1]
    summary.sampled_48_ok = sample_48(mapping)
    summary.random_100_ok = random_100(mapping)
    try:
        products, product_images = load_db_products()
        summary.db_available = True
        for product_no in [f"SP{num:06d}" for num in range(300001, 301001)]:
            expected = mapping.get(product_no, {})
            product = products.get(product_no)
            if product is None:
                summary.db_mismatches.append(f"{product_no}: missing product row")
                continue
            if product.image_url != expected.get("main"):
                summary.db_mismatches.append(f"{product_no}: image_url mismatch")
            expected_detail = ",".join([expected.get("scene", ""), expected.get("detail", "")])
            if product.detail_image_url != expected_detail:
                summary.db_mismatches.append(f"{product_no}: detail_image_url mismatch")
            actual_images = product_images.get(product_no, [])
            expected_images = [(expected.get("main", ""), "1"), (expected.get("scene", ""), "2"), (expected.get("detail", ""), "3")]
            if actual_images != expected_images:
                summary.db_mismatches.append(f"{product_no}: product_image mismatch")
    except Exception as exc:
        summary.db_available = False
        summary.db_error = str(exc)
    return summary


def write_report(summary: VerifySummary) -> None:
    REPORT.parent.mkdir(parents=True, exist_ok=True)
    status = "PASS" if not summary.missing_files and not summary.invalid_svg and not summary.duplicate_paths and not summary.db_mismatches and summary.db_available else "CHECK_REQUIRED"
    lines = [
        "# 1000 商品本地图片生成与绑定校验报告",
        "",
        f"- 校验状态：{status}",
        f"- 商品范围：{START_CODE} 至 {END_CODE}",
        f"- 商品数量：{summary.product_count}",
        f"- 主图数量：{summary.main_count}",
        f"- 场景图数量：{summary.scene_count}",
        f"- 详情图数量：{summary.detail_count}",
        f"- 图片格式：{EXPECTED_EXT.upper()}",
        f"- 期望图片总数：{summary.total_expected_images}",
        f"- 实际批次图片总数：{summary.total_existing_images}",
        f"- 缺失图片数：{len(summary.missing_files)}",
        f"- 无效 SVG 数：{len(summary.invalid_svg)}",
        f"- 重复路径数：{len(summary.duplicate_paths)}",
        f"- DB 可连接：{'是' if summary.db_available else '否'}",
        f"- DB 路径不一致数：{len(summary.db_mismatches)}",
        f"- 每分类抽样 48 个商品：{'通过' if summary.sampled_48_ok else '未通过'}",
        f"- 随机抽样 100 个文件：{'通过' if summary.random_100_ok else '未通过'}",
        "",
        "## 明细",
        "",
        f"- DB 错误：{summary.db_error or '无'}",
        f"- 缺失图片示例：{summary.missing_files[:20]}",
        f"- 无效 SVG 示例：{summary.invalid_svg[:20]}",
        f"- 重复路径示例：{summary.duplicate_paths[:20]}",
        f"- DB 不一致示例：{summary.db_mismatches[:20]}",
    ]
    REPORT.write_text("\n".join(lines) + "\n", encoding="utf-8")


def main() -> int:
    summary = verify()
    write_report(summary)
    print(json.dumps(summary.__dict__, ensure_ascii=False, indent=2))
    if summary.missing_files or summary.invalid_svg or summary.duplicate_paths:
        return 1
    if summary.db_available and summary.db_mismatches:
        return 1
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
