from __future__ import annotations

import json
import urllib.error
import urllib.request
from pathlib import Path

from config import COMFYUI_BASE_URL, IMAGE_MODEL
from image_provider import ImageProvider


class ComfyUIProvider(ImageProvider):
    """本地 ComfyUI 供应商探测与调用占位。

    当前项目不内置具体工作流 JSON，因为不同 ComfyUI 节点和模型命名差异很大。
    配置好本地工作流后，可在 generate_image 中提交 /prompt 并下载 /view 输出。
    """

    name = "ComfyUI"
    model = IMAGE_MODEL or "local ComfyUI model"

    def available(self) -> tuple[bool, str]:
        try:
            with urllib.request.urlopen(f"{COMFYUI_BASE_URL}/system_stats", timeout=3) as response:
                if response.status == 200:
                    return True, f"ComfyUI reachable at {COMFYUI_BASE_URL}"
                return False, f"ComfyUI status={response.status}"
        except urllib.error.URLError as exc:
            return False, f"ComfyUI unavailable: {exc.reason}"
        except Exception as exc:  # noqa: BLE001
            return False, f"ComfyUI unavailable: {exc}"

    def generate_image(
        self,
        prompt: str,
        negative_prompt: str,
        width: int,
        height: int,
        seed: int,
        output_path: Path,
    ) -> None:
        raise NotImplementedError(
            "ComfyUI is reachable, but no project workflow JSON is configured. "
            "Add a ComfyUI workflow before enabling generation."
        )


def dump_provider_status() -> str:
    provider = ComfyUIProvider()
    ok, reason = provider.available()
    return json.dumps(
        {"provider": provider.name, "model": provider.model, "available": ok, "reason": reason},
        ensure_ascii=False,
    )

