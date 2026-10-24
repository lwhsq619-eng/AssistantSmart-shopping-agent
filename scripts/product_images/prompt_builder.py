from __future__ import annotations

import re


NEGATIVE_PROMPT = (
    "illustration, vector art, flat design, icon, cartoon, anime, 3d icon, simple geometric shapes, "
    "infographic, poster, banner, text, letters, numbers, logo, watermark, price tag, product code, "
    "label text, Chinese characters, English words, low resolution, blurry, pixelated, distorted, "
    "deformed, duplicate product, floating objects, incorrect proportions, cropped product, cut off, "
    "overexposed, underexposed, busy background, collage, screenshot, website interface, UI elements, "
    "border, frame"
)

CATEGORY_SCENES = {
    "数码科技": "dark modern studio background, subtle blue ambient light, premium consumer electronics photography, realistic plastic and metal materials",
    "手机通讯": "clean white or light gray studio, premium mobile accessory photography, realistic glass, metal and plastic surfaces",
    "电脑办公": "modern professional desk studio, neutral gray background, realistic computer peripheral product photography",
    "家居生活": "warm minimal home interior background, soft daylight, natural wood and fabric textures",
    "美妆护肤": "clean luxury beauty studio, soft cream background, subtle reflections, realistic glass and cosmetic packaging",
    "食品饮料": "professional food packaging photography, clean tabletop scene, natural warm lighting, appetizing but realistic presentation",
    "运动户外": "outdoor-inspired studio environment, subtle natural landscape background, durable material textures",
    "健身": "modern fitness studio, rubber floor or clean gym background, dramatic but realistic side lighting",
    "宠物用品": "bright clean pet-friendly indoor scene, warm daylight, realistic fabric, plastic or food packaging",
    "办公文具": "minimal organized desktop, soft neutral lighting, realistic paper and stationery materials",
    "图书文具": "clean reading desk or bookshelf environment, soft warm light, realistic book cover without readable text",
    "图书音像": "clean reading desk or bookshelf environment, soft warm light, realistic book cover without readable text",
    "礼品专区": "elegant gift photography, soft studio lighting, ribbon and gift box composition, premium festive background",
    "箱包配饰": "fashion catalog product photography, neutral studio background, realistic leather and fabric texture",
    "服饰鞋包": "fashion e-commerce photography, clean studio background, realistic fabric and stitching details",
    "时尚服饰": "apparel catalog photography, professional softbox lighting, natural fabric draping",
    "母婴用品": "bright soft pastel studio, safe and clean nursery-inspired environment, soft natural lighting",
}

SLUG_BY_CATEGORY = {
    "数码科技": "digital",
    "手机通讯": "mobile",
    "电脑办公": "computer",
    "家居生活": "home",
    "美妆护肤": "beauty",
    "食品饮料": "food",
    "运动户外": "outdoor",
    "健身": "fitness",
    "宠物用品": "pet",
    "办公文具": "office",
    "图书文具": "book",
    "图书音像": "book",
    "礼品专区": "gift",
    "箱包配饰": "bag",
    "服饰鞋包": "clothing",
    "时尚服饰": "fashion",
    "母婴用品": "baby",
}

KEYWORD_DESCRIPTIONS = [
    (("投影仪",), "compact home theater projector with lens and ventilation details"),
    (("运动耳机", "耳机",), "wireless in-ear earbuds with charging case"),
    (("头戴耳机",), "over-ear wireless headphones with padded ear cups"),
    (("键盘",), "mechanical computer keyboard with realistic keycaps and aluminum frame"),
    (("鼠标",), "ergonomic wireless computer mouse"),
    (("显示器",), "slim desktop monitor with narrow bezels and stable stand"),
    (("无线充电器", "充电器",), "compact wireless charging pad or phone charger with realistic plastic surface"),
    (("手机",), "modern smartphone with glass screen and metal frame"),
    (("扩展坞",), "compact USB-C docking station with realistic metal shell and ports"),
    (("奶瓶",), "single baby feeding bottle with translucent plastic and soft silicone nipple"),
    (("摄像头",), "compact smart security camera with realistic lens and plastic shell"),
    (("智能手表",), "modern smartwatch with glass display and silicone strap"),
    (("台灯",), "minimalist desk lamp with adjustable arm"),
    (("保温杯",), "stainless steel insulated travel mug"),
    (("背包",), "urban commuter backpack made of durable woven fabric"),
    (("运动鞋", "跑鞋",), "lightweight running shoes with breathable mesh upper"),
    (("瑜伽垫",), "rolled non-slip yoga mat with realistic textured surface"),
    (("壶铃",), "single coated fitness kettlebell with textured handle"),
    (("哑铃",), "pair of coated fitness dumbbells"),
    (("泡沫轴",), "textured foam roller for fitness recovery"),
    (("宠物外出包",), "ventilated pet travel carrier bag with mesh panels"),
    (("积木",), "colorful children's building block set arranged safely"),
    (("面膜",), "premium skincare sheet mask package and cosmetic presentation"),
    (("香水",), "elegant perfume bottle with transparent glass and soft reflection"),
    (("咖啡",), "premium roasted coffee beans in a sealed kraft package"),
    (("图书", "绘本",), "physical printed educational book with an abstract non-readable cover"),
    (("礼盒",), "premium gift box with ribbon on a clean studio table"),
]

FALLBACK_BY_CATEGORY = {
    "数码科技": "premium compact consumer electronics device with realistic plastic and metal details",
    "手机通讯": "premium mobile accessory product with realistic glass, metal and plastic surfaces",
    "电脑办公": "modern computer office accessory with realistic material details",
    "家居生活": "minimal practical home living product with realistic fabric or wood texture",
    "时尚服饰": "single apparel product displayed as catalog photography with natural fabric texture",
    "服饰鞋包": "single fashion product with realistic stitching and fabric texture",
    "美妆护肤": "premium cosmetic packaging product with realistic glass and cream texture",
    "食品饮料": "single packaged food or beverage product on a clean tabletop",
    "运动户外": "single outdoor sports product with durable realistic material",
    "图书文具": "single book or stationery product with non-readable abstract cover",
    "母婴用品": "single clean baby care product in a soft pastel studio",
    "宠物用品": "single pet product with realistic fabric or plastic material",
    "礼品专区": "single premium gift box product with elegant ribbon",
    "办公文具": "single office stationery product on an organized desktop",
    "箱包配饰": "single backpack or bag product with realistic leather or woven fabric",
    "健身": "single fitness training product in a modern gym studio",
}


def build_product_description(product: dict) -> str:
    text = f"{product.get('name', '')} {product.get('description', '')} {product.get('specs', '')}"
    for keywords, description in KEYWORD_DESCRIPTIONS:
        if any(keyword in text for keyword in keywords):
            if re.search(r"[23456789]件组|套装", text):
                return f"a premium product set featuring {description}, neatly arranged as a cohesive e-commerce bundle"
            return description
    return FALLBACK_BY_CATEGORY.get(product.get("categoryName", ""), "single realistic e-commerce product")


def build_prompt(product: dict) -> str:
    product_description = build_product_description(product)
    scene = CATEGORY_SCENES.get(product.get("categoryName", ""), "clean premium e-commerce studio background")
    return (
        f"Photorealistic commercial product photography of {product_description}, "
        "single product centered in the frame, product occupies about 60 to 75 percent of the image, "
        f"{scene}, professional studio lighting, soft natural shadows, realistic materials and textures, "
        "clean premium e-commerce background, high detail, sharp focus, accurate proportions, catalog photography, "
        "85mm product photography, subtle depth of field, 4:3 composition, no text, no logo, no watermark, no price tag"
    )


def category_slug(category_name: str) -> str:
    return SLUG_BY_CATEGORY.get(category_name, "digital")
