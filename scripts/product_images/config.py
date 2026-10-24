from __future__ import annotations

import os
from pathlib import Path


ROOT = Path(__file__).resolve().parents[2]
FRONTEND_DIR = ROOT / "前端"
REAL_IMAGE_ROOT = FRONTEND_DIR / "public" / "products" / "real"
PROMPT_OUTPUT = ROOT / "docs" / "realistic_product_image_prompts_16.json"
REPORT_OUTPUT = ROOT / "docs" / "REALISTIC_PRODUCT_IMAGE_SAMPLE_REPORT.md"
PROGRESS_FILE = ROOT / "scripts" / "product_images" / "generation_progress.json"

MYSQL_BIN = os.getenv("MYSQL_BIN", "mysql")
MYSQL_HOST = os.getenv("MALL_DB_HOST", "localhost")
MYSQL_PORT = os.getenv("MALL_DB_PORT", "3306")
MYSQL_USER = os.getenv("MALL_DB_USER") or os.getenv("MYSQL_USERNAME", "root")
MYSQL_PASSWORD = os.getenv("MALL_DB_PASSWORD") or os.getenv("MYSQL_PASSWORD", "")
MYSQL_DB = os.getenv("MALL_DB_NAME", "mall_system")

COMFYUI_BASE_URL = os.getenv("COMFYUI_BASE_URL", "http://127.0.0.1:8188").rstrip("/")
IMAGE_API_KEY = os.getenv("IMAGE_API_KEY", "")
IMAGE_API_BASE_URL = os.getenv("IMAGE_API_BASE_URL", "").rstrip("/")
IMAGE_MODEL = os.getenv("IMAGE_MODEL", "")

WIDTH = 1200
HEIGHT = 900
BATCH_SIZE = int(os.getenv("PRODUCT_IMAGE_BATCH_SIZE", "16"))
START_CODE = "SP300001"
END_CODE = "SP301000"

