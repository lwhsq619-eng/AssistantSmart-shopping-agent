from __future__ import annotations

from abc import ABC, abstractmethod
from pathlib import Path


class ImageProvider(ABC):
    """真实位图图片生成供应商统一接口。"""

    name = "base"
    model = ""

    @abstractmethod
    def available(self) -> tuple[bool, str]:
        """返回供应商是否可用及原因。"""

    @abstractmethod
    def generate_image(
        self,
        prompt: str,
        negative_prompt: str,
        width: int,
        height: int,
        seed: int,
        output_path: Path,
    ) -> None:
        """生成真实商品摄影图。禁止在实现中生成 SVG 或插画占位图。"""

