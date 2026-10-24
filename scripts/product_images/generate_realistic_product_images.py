from __future__ import annotations

import csv
import hashlib
import json
import subprocess
import sys
from pathlib import Path

from comfyui_provider import ComfyUIProvider
from config import (
    BATCH_SIZE,
    COMFYUI_BASE_URL,
    END_CODE,
    HEIGHT,
    IMAGE_API_BASE_URL,
    IMAGE_API_KEY,
    IMAGE_MODEL,
    MYSQL_BIN,
    MYSQL_DB,
    MYSQL_HOST,
    MYSQL_PASSWORD,
    MYSQL_PORT,
    MYSQL_USER,
    PROGRESS_FILE,
    PROMPT_OUTPUT,
    REAL_IMAGE_ROOT,
    REPORT_OUTPUT,
    START_CODE,
    WIDTH,
)
from prompt_builder import NEGATIVE_PROMPT, build_prompt, category_slug


def stable_seed(product_code: str) -> int:
    return int(hashlib.sha256(product_code.encode("utf-8")).hexdigest()[:12], 16)


def mysql_query(sql: str) -> list[list[str]]:
    args = [
        MYSQL_BIN,
        f"-h{MYSQL_HOST}",
        f"-P{MYSQL_PORT}",
        "--protocol=TCP",
        f"-u{MYSQL_USER}",
        "--default-character-set=utf8mb4",
        "-N",
        "-B",
        "-e",
        f"USE {MYSQL_DB}; {sql}",
    ]
    if MYSQL_PASSWORD:
        args.insert(4, f"-p{MYSQL_PASSWORD}")
    result = subprocess.run(args, capture_output=True, text=True, encoding="utf-8", errors="replace")
    if result.returncode != 0:
        raise RuntimeError(result.stderr.strip())
    return list(csv.reader(result.stdout.splitlines(), delimiter="\t"))


def load_sample_products() -> list[dict]:
    rows = mysql_query(
        "SELECT product_no,name,category_name,IFNULL(brand_name,''),IFNULL(description,''),IFNULL(specs,''),IFNULL(image_url,'') "
        "FROM product "
        f"WHERE product_no BETWEEN '{START_CODE}' AND '{END_CODE}' "
        "ORDER BY category_id, product_no"
    )
    selected: dict[str, dict] = {}
    for row in rows:
        if len(row) < 7:
            continue
        category_name = row[2]
        if category_name in selected:
            continue
        product = {
            "productCode": row[0],
            "name": row[1],
            "categoryName": category_name,
            "brandName": row[3],
            "description": row[4],
            "specs": row[5],
            "currentImageUrl": row[6],
        }
        selected[category_name] = product
        if len(selected) >= 16:
            break
    return list(selected.values())


def image_service_status() -> dict:
    comfy = ComfyUIProvider()
    comfy_ok, comfy_reason = comfy.available()
    api_ok = bool(IMAGE_API_KEY and IMAGE_API_BASE_URL and IMAGE_MODEL)
    return {
        "IMAGE_GENERATOR_AVAILABLE": bool(comfy_ok or api_ok),
        "comfyui": {
            "available": comfy_ok,
            "baseUrl": COMFYUI_BASE_URL,
            "model": comfy.model,
            "reason": comfy_reason,
        },
        "imageApi": {
            "available": api_ok,
            "baseUrlConfigured": bool(IMAGE_API_BASE_URL),
            "model": IMAGE_MODEL,
            "apiKeyConfigured": bool(IMAGE_API_KEY),
        },
    }


def build_prompt_records(products: list[dict]) -> list[dict]:
    records = []
    for product in products:
        slug = category_slug(product["categoryName"])
        product_code = product["productCode"]
        output_path = REAL_IMAGE_ROOT / slug / f"{product_code}-main.webp"
        records.append(
            {
                **product,
                "categoryDirectory": slug,
                "seed": stable_seed(product_code),
                "width": WIDTH,
                "height": HEIGHT,
                "outputPath": str(output_path),
                "dbPath": f"/products/real/{slug}/{product_code}-main.webp",
                "positivePrompt": build_prompt(product),
                "negativePrompt": NEGATIVE_PROMPT,
                "format": "webp",
                "generated": False,
                "reason": "No configured realistic image generation service",
            }
        )
    return records


def write_progress(total: int, success: int, failed: int, skipped: int, last_product_code: str, failures: list[dict]) -> None:
    PROGRESS_FILE.parent.mkdir(parents=True, exist_ok=True)
    PROGRESS_FILE.write_text(
        json.dumps(
            {
                "total": total,
                "success": success,
                "failed": failed,
                "skipped": skipped,
                "lastProductCode": last_product_code,
                "failures": failures,
            },
            ensure_ascii=False,
            indent=2,
        ),
        encoding="utf-8",
    )


def write_report(status: dict, records: list[dict]) -> None:
    REPORT_OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    lines = [
        "# 16 张写实商品摄影样图生成检查报告",
        "",
        f"- IMAGE_GENERATOR_AVAILABLE={str(status['IMAGE_GENERATOR_AVAILABLE']).lower()}",
        f"- ComfyUI：{'可用' if status['comfyui']['available'] else '不可用'}",
        f"- ComfyUI 地址：{status['comfyui']['baseUrl']}",
        f"- ComfyUI 说明：{status['comfyui']['reason']}",
        f"- IMAGE_API：{'可用' if status['imageApi']['available'] else '不可用'}",
        f"- IMAGE_MODEL：{status['imageApi']['model'] or '未配置'}",
        "- 是否生成图片：否",
        "- 是否更新数据库：否",
        "- 是否更新前端路径：否",
        "",
        "## 16 个样本商品与提示词",
        "",
    ]
    for item in records:
        lines.extend(
            [
                f"### {item['productCode']} {item['categoryName']}",
                f"- 商品名称：{item['name']}",
                f"- 目标路径：`{item['dbPath']}`",
                f"- Seed：{item['seed']}",
                f"- Positive Prompt：{item['positivePrompt']}",
                f"- Negative Prompt：{item['negativePrompt']}",
                f"- 验证结果：未生成，等待真实图像服务",
                "",
            ]
        )
    REPORT_OUTPUT.write_text("\n".join(lines), encoding="utf-8")


def main() -> int:
    status = image_service_status()
    products = load_sample_products()
    records = build_prompt_records(products)
    PROMPT_OUTPUT.parent.mkdir(parents=True, exist_ok=True)
    PROMPT_OUTPUT.write_text(json.dumps(records, ensure_ascii=False, indent=2), encoding="utf-8")

    if not status["IMAGE_GENERATOR_AVAILABLE"]:
        write_progress(len(records), 0, 0, len(records), records[-1]["productCode"] if records else "", [])
        write_report(status, records)
        print(json.dumps({"status": status, "promptFile": str(PROMPT_OUTPUT), "reportFile": str(REPORT_OUTPUT)}, ensure_ascii=False, indent=2))
        return 0

    raise SystemExit("IMAGE_GENERATOR_AVAILABLE=true, but generation is intentionally paused until a ComfyUI workflow or IMAGE API provider is configured.")


if __name__ == "__main__":
    try:
        raise SystemExit(main())
    except Exception as exc:
        print(f"realistic image sample preparation failed: {exc}", file=sys.stderr)
        raise SystemExit(1)
