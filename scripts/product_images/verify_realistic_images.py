from __future__ import annotations

import hashlib
import json
from pathlib import Path

from config import HEIGHT, PROMPT_OUTPUT, WIDTH


def verify_webp_header(path: Path) -> bool:
    data = path.read_bytes()
    return len(data) > 12 and data[:4] == b"RIFF" and data[8:12] == b"WEBP"


def verify_jpeg_header(path: Path) -> bool:
    data = path.read_bytes()
    return len(data) > 4 and data[:2] == b"\xff\xd8" and data[-2:] == b"\xff\xd9"


def file_hash(path: Path) -> str:
    return hashlib.sha256(path.read_bytes()).hexdigest()


def verify_prompt_list() -> dict:
    if not PROMPT_OUTPUT.exists():
        return {"success": False, "error": f"missing prompt file: {PROMPT_OUTPUT}"}
    records = json.loads(PROMPT_OUTPUT.read_text(encoding="utf-8"))
    return {
        "success": True,
        "sampleCount": len(records),
        "expectedWidth": WIDTH,
        "expectedHeight": HEIGHT,
        "note": "No bitmap verification was run because realistic images have not been generated.",
    }


if __name__ == "__main__":
    print(json.dumps(verify_prompt_list(), ensure_ascii=False, indent=2))
