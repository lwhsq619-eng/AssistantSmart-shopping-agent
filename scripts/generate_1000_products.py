#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
生成 1000 个商城演示商品、3000 张本地 SVG 图片，以及导入/回滚/校验 SQL。

脚本只生成文件，不直接写入数据库。执行后由人工确认 SQL 后再导入。
"""

from __future__ import annotations

import html
import random
from dataclasses import dataclass
from decimal import Decimal, ROUND_HALF_UP
from pathlib import Path


ROOT = Path(__file__).resolve().parents[1]
FRONTEND_IMAGE_ROOT = ROOT / "前端" / "public" / "products" / "generated"
SQL_DIR = ROOT / "docs" / "sql"
INSERT_SQL = SQL_DIR / "insert_1000_products.sql"
ROLLBACK_SQL = SQL_DIR / "rollback_1000_products.sql"
VERIFY_SQL = SQL_DIR / "verify_1000_products.sql"
GENERATION_REPORT = ROOT / "docs" / "PRODUCT_GENERATION_REPORT.md"
DATA_QUALITY_REPORT = ROOT / "docs" / "DATA_QUALITY_REPORT.md"

RANDOM_SEED = 20260711
START_CODE = 300001
TOTAL_COUNT = 1000


@dataclass(frozen=True)
class CategoryConfig:
    category_id: int
    name: str
    slug: str
    count: int
    price_min: int
    price_max: int
    brands: tuple[str, ...]
    product_types: tuple[str, ...]
    features: tuple[str, ...]
    versions: tuple[str, ...]
    palette: tuple[str, str, str]


CATEGORIES: tuple[CategoryConfig, ...] = (
    CategoryConfig(1, "数码科技", "digital", 63, 49, 1999,
                   ("NovaTech", "PixelGo", "Auralink", "FutureOne"),
                   ("智能手表", "蓝牙耳机", "运动耳机", "便携音箱", "投影仪", "运动相机", "智能音响", "电子阅读器", "智能摄像头", "无线麦克风"),
                   ("轻量", "降噪", "高清", "长续航", "便携", "智能", "低延迟", "防泼水"),
                   ("标准版", "Pro", "青春版", "旗舰款", "旅行套装"), ("#dbeafe", "#2563eb", "#0f172a")),
    CategoryConfig(2, "时尚服饰", "fashion", 62, 39, 899,
                   ("UrbanLine", "MoriWear", "LunaFit", "NorthStudio"),
                   ("衬衫", "针织衫", "风衣", "羽绒服", "牛仔裤", "休闲裤", "半身裙", "连衣裙", "T恤", "防晒衣", "家居服", "毛衣"),
                   ("通勤", "宽松", "柔软", "轻暖", "简约", "透气", "显瘦", "亲肤"),
                   ("春夏款", "秋冬款", "基础款", "轻奢版", "经典版"), ("#fce7f3", "#db2777", "#831843")),
    CategoryConfig(3, "家居生活", "home", 63, 19, 999,
                   ("HomeNest", "WarmCasa", "Mori Home", "Cleanora"),
                   ("台灯", "收纳盒", "保温杯", "抱枕", "床上用品", "香薰", "清洁工具", "厨房用品", "加湿器", "小风扇", "衣架", "置物架"),
                   ("北欧", "护眼", "大容量", "静音", "可折叠", "易清洁", "耐用", "节省空间"),
                   ("标准版", "升级版", "家庭装", "宿舍款", "套装"), ("#dcfce7", "#16a34a", "#14532d")),
    CategoryConfig(4, "美妆护肤", "beauty", 62, 19, 699,
                   ("PureGlow", "AquaBelle", "LumiCare", "SoftSkin"),
                   ("洁面乳", "面膜", "精华液", "面霜", "防晒霜", "口红", "腮红", "香水", "卸妆产品", "身体乳", "护手霜", "化妆刷"),
                   ("保湿", "清爽", "温和", "便携", "细腻", "持妆", "自然", "柔润"),
                   ("日常款", "旅行装", "礼盒装", "清爽版", "滋润版"), ("#ffe4e6", "#e11d48", "#881337")),
    CategoryConfig(5, "食品饮料", "food", 63, 9, 399,
                   ("FreshTaste", "CafeLoop", "NutriDay", "TeaGarden"),
                   ("咖啡豆", "茶叶", "坚果", "饼干", "巧克力", "果汁", "气泡水", "速食", "麦片", "蜂蜜", "零食礼盒", "冲调饮品"),
                   ("低甜", "醇香", "独立包装", "分享装", "清爽", "酥脆", "浓郁", "便携"),
                   ("家庭装", "礼盒装", "轻享装", "组合装", "经典味"), ("#ffedd5", "#ea580c", "#7c2d12")),
    CategoryConfig(6, "运动户外", "outdoor", 62, 19, 999,
                   ("FitMotion", "TrailGo", "PeakRun", "OutdoorLab"),
                   ("篮球", "足球", "羽毛球拍", "跳绳", "帐篷", "登山杖", "户外水壶", "野餐垫", "运动护具", "骑行装备", "户外背包", "露营灯"),
                   ("轻便", "防滑", "耐磨", "透气", "加厚", "便携", "稳定", "高弹"),
                   ("入门款", "专业款", "训练款", "户外款", "组合套装"), ("#ccfbf1", "#0d9488", "#134e4a")),
    CategoryConfig(7, "母婴用品", "baby", 63, 9, 499,
                   ("BabyCare Plus", "TinyMori", "KinderJoy", "SoftBaby"),
                   ("婴儿湿巾", "奶瓶", "安抚玩具", "积木", "儿童餐具", "婴儿浴巾", "纸尿裤", "儿童绘本", "学步用品", "儿童水杯", "婴儿床品", "早教玩具"),
                   ("柔软", "易握", "大包装", "趣味", "可清洗", "便携", "亲肤", "耐用"),
                   ("基础款", "成长款", "家庭装", "礼盒装", "升级版"), ("#fef3c7", "#d97706", "#78350f")),
    CategoryConfig(8, "办公文具", "office", 62, 3, 299,
                   ("OfficeMate", "DeskPro", "NoteSky", "PaperLab"),
                   ("笔记本", "中性笔", "文件夹", "文件架", "便签", "订书机", "计算器", "桌面收纳", "书立", "胶带", "剪刀", "文具礼盒"),
                   ("商务", "耐用", "简约", "便携", "大容量", "顺滑", "稳固", "整洁"),
                   ("单品装", "组合装", "办公套装", "学生款", "升级版"), ("#e0f2fe", "#0284c7", "#0c4a6e")),
    CategoryConfig(9, "箱包配饰", "bag", 63, 29, 999,
                   ("UrbanPack", "CarryGo", "MetroBag", "TravelKit"),
                   ("通勤包", "双肩包", "手提包", "斜挎包", "钱包", "卡包", "腰带", "帽子", "围巾", "旅行包", "行李箱", "收纳包"),
                   ("轻量", "防泼水", "大容量", "耐磨", "多隔层", "简约", "通勤", "旅行"),
                   ("标准版", "城市款", "旅行款", "商务款", "经典款"), ("#ede9fe", "#7c3aed", "#3b0764")),
    CategoryConfig(10, "图书音像", "book", 62, 19, 199,
                   ("CodeBook", "ReadMore", "LearnHub", "StoryLine"),
                   ("Java教程", "Python教程", "数据结构教材", "操作系统教材", "计算机网络教材", "数据库教材", "人工智能入门", "软件工程教材", "文学作品", "历史读物", "商业管理图书", "有声读物"),
                   ("入门", "实战", "通俗", "系统", "案例", "新版", "精讲", "配套练习"),
                   ("第2版", "第3版", "基础版", "进阶版", "实践版"), ("#f5f5f4", "#57534e", "#1c1917")),
    CategoryConfig(11, "手机通讯", "mobile", 63, 19, 2999,
                   ("SignalGo", "NovaTech", "ChargeMax", "MobileX"),
                   ("手机", "充电器", "数据线", "移动电源", "手机壳", "手机膜", "手机支架", "无线充电器", "蓝牙耳机", "自拍杆", "手机散热器", "车载支架"),
                   ("快充", "磁吸", "轻薄", "耐用", "防摔", "高清", "便携", "长续航"),
                   ("标准版", "快充版", "Pro", "套装", "旅行款"), ("#dbeafe", "#1d4ed8", "#172554")),
    CategoryConfig(12, "电脑办公", "computer", 62, 19, 3999,
                   ("OfficeMate", "KeyNova", "DisplayPro", "DataCube"),
                   ("键盘", "鼠标", "显示器", "笔记本支架", "扩展坞", "摄像头", "麦克风", "移动硬盘", "U盘", "显示器支架", "鼠标垫", "电脑包"),
                   ("静音", "机械", "高清", "稳定", "高速", "人体工学", "便携", "耐用"),
                   ("办公款", "专业款", "电竞款", "标准版", "增强版"), ("#e2e8f0", "#475569", "#020617")),
    CategoryConfig(13, "服饰鞋包", "clothing", 63, 29, 499,
                   ("Urban Step", "FitMotion", "WalkEase", "StreetGo"),
                   ("运动鞋", "休闲鞋", "板鞋", "靴子", "卫衣", "夹克", "运动裤", "双肩包", "单肩包", "棒球帽", "袜子", "皮带"),
                   ("透气", "耐磨", "轻便", "缓震", "街头", "运动", "百搭", "舒适"),
                   ("日常款", "训练款", "潮流款", "基础款", "加厚款"), ("#fee2e2", "#dc2626", "#7f1d1d")),
    CategoryConfig(14, "宠物用品", "pet", 62, 9, 399,
                   ("PetJoy", "PawHome", "MeowCare", "BuddyPet"),
                   ("猫粮", "狗粮", "猫砂", "猫砂盆", "宠物玩具", "宠物牵引绳", "宠物窝", "宠物清洁用品", "宠物食盆", "宠物梳子", "宠物外出包", "宠物零食"),
                   ("耐咬", "易清洁", "大容量", "便携", "舒适", "防滑", "亲宠", "实用"),
                   ("小型宠款", "中型宠款", "家庭装", "标准版", "组合装"), ("#ecfccb", "#65a30d", "#365314")),
    CategoryConfig(15, "礼品专区", "gift", 63, 19, 999,
                   ("GiftLab", "WarmGift", "FreshTaste", "Mori Gift"),
                   ("节日礼盒", "生日礼盒", "鲜花礼盒", "巧克力礼盒", "茶叶礼盒", "咖啡礼盒", "香薰礼盒", "文具礼盒", "数码礼盒", "定制贺卡", "企业礼品", "礼品卡"),
                   ("精致", "心意", "组合", "节日", "定制", "实用", "分享", "典雅"),
                   ("50元面值", "100元面值", "200元面值", "礼盒装", "祝福款"), ("#fae8ff", "#c026d3", "#701a75")),
    CategoryConfig(16, "健身", "fitness", 62, 19, 999,
                   ("FitMotion", "CoreLab", "YogaWell", "HomeGym"),
                   ("瑜伽垫", "哑铃", "壶铃", "健腹轮", "弹力带", "拉力器", "泡沫轴", "运动手套", "瑜伽砖", "瑜伽球", "健身踏板", "家用单杠"),
                   ("防滑", "加厚", "高弹", "耐磨", "家用", "入门", "训练", "便携"),
                   ("初学款", "进阶款", "家庭款", "训练款", "组合套装"), ("#dcfce7", "#15803d", "#052e16")),
)


def money(value: Decimal | float | int) -> str:
    return str(Decimal(value).quantize(Decimal("0.01"), rounding=ROUND_HALF_UP))


def sql_quote(value: object) -> str:
    if value is None:
        return "NULL"
    text = str(value).replace("\\", "\\\\").replace("'", "''")
    return f"'{text}'"


def random_price(category: CategoryConfig, idx: int, product_type: str) -> tuple[str, str, str, int]:
    if category.name == "礼品专区" and product_type == "礼品卡":
        face_values = [50, 100, 200, 300, 500]
        price = Decimal(face_values[idx % len(face_values)])
        return money(price), money(price), money(price), 0

    span = category.price_max - category.price_min
    base = Decimal(category.price_min + int(random.betavariate(2.0, 4.2) * span))
    cents = Decimal(random.choice(["0.00", "0.90", "0.99", "5.00", "8.80", "9.90"]))
    price = max(Decimal(category.price_min), min(Decimal(category.price_max), base + cents))
    is_special = 1 if idx % 7 == 0 else 0
    if is_special:
        market_price = (price * Decimal(random.choice(["1.18", "1.25", "1.32", "1.40"]))).quantize(Decimal("0.01"))
        discount_price = price
    else:
        market_price = price if idx % 3 else (price * Decimal("1.08")).quantize(Decimal("0.01"))
        discount_price = price
    return money(price), money(market_price), money(discount_price), is_special


def make_product_name(category: CategoryConfig, code_index: int, local_index: int) -> tuple[str, str, str, str]:
    brand = category.brands[(code_index + local_index) % len(category.brands)]
    product_type = category.product_types[(code_index * 3 + local_index) % len(category.product_types)]
    feature = category.features[(code_index * 5 + local_index) % len(category.features)]
    version = category.versions[(code_index * 7 + local_index) % len(category.versions)]

    if category.name == "礼品专区" and product_type == "礼品卡":
        face_values = [50, 100, 200, 300, 500]
        face = face_values[local_index % len(face_values)]
        name = f"{brand} {face}元{feature}礼品卡 {version}"
    elif category.name == "图书音像":
        name = f"{brand} {feature}{product_type} {version}"
    else:
        name = f"{brand} {feature}{product_type} {version}"
    return brand, product_type, feature, name


def description_for(category: CategoryConfig, name: str, product_type: str, feature: str, version: str) -> tuple[str, str, str]:
    short = f"{name}，主打{feature}体验，适合{category.name}日常使用。"
    detail = (
        f"{name}是一款面向{category.name}场景的演示商品。"
        f"核心特点包括{feature}设计、清晰规格和稳定使用体验；"
        f"适用于日常选购、课程设计演示和商城业务流程测试。"
        f"规格版本为{version}，购买前请结合库存、价格和个人需求选择。"
    )
    specs = f"品类:{product_type}; 特点:{feature}; 版本:{version}; 适用分类:{category.name}"
    if category.name == "食品饮料":
        specs += "; 规格:组合装; 口味:以页面展示为准"
    elif category.name == "图书音像":
        specs += "; 方向:学习阅读; 适合人群:入门到进阶用户"
    elif category.name == "宠物用品":
        specs += "; 适用对象:猫犬等常见宠物; 使用建议:按宠物体型选择"
    return short[:500], detail[:1000], specs[:1000]


def svg_text(value: str) -> str:
    return html.escape(value, quote=True)


def product_shape(category: CategoryConfig, seed: int) -> str:
    random.seed(seed)
    accent = category.palette[1]
    dark = category.palette[2]
    if category.slug in {"digital", "mobile", "computer"}:
        return (
            f'<rect x="390" y="210" width="420" height="270" rx="36" fill="{dark}" opacity=".92"/>'
            f'<rect x="420" y="240" width="360" height="190" rx="20" fill="#eff6ff"/>'
            f'<circle cx="780" cy="500" r="48" fill="{accent}" opacity=".82"/>'
            f'<rect x="470" y="525" width="260" height="36" rx="18" fill="{accent}" opacity=".76"/>'
        )
    if category.slug in {"fashion", "clothing"}:
        return (
            f'<path d="M520 190 L680 190 L760 330 L700 365 L700 620 L500 620 L500 365 L440 330 Z" fill="{accent}" opacity=".86"/>'
            f'<path d="M560 195 Q600 255 640 195" fill="none" stroke="#fff" stroke-width="16" opacity=".85"/>'
            f'<circle cx="610" cy="420" r="82" fill="#fff" opacity=".18"/>'
        )
    if category.slug == "home":
        return (
            f'<rect x="470" y="390" width="260" height="210" rx="28" fill="{accent}" opacity=".78"/>'
            f'<path d="M500 390 Q600 240 700 390" fill="{dark}" opacity=".82"/>'
            f'<rect x="585" y="165" width="30" height="230" rx="15" fill="{dark}" opacity=".75"/>'
            f'<ellipse cx="600" cy="620" rx="190" ry="34" fill="{dark}" opacity=".16"/>'
        )
    if category.slug == "beauty":
        return (
            f'<rect x="510" y="230" width="85" height="330" rx="28" fill="{accent}" opacity=".86"/>'
            f'<rect x="620" y="290" width="110" height="270" rx="38" fill="{dark}" opacity=".82"/>'
            f'<circle cx="665" cy="250" r="44" fill="{accent}" opacity=".45"/>'
        )
    if category.slug == "food":
        return (
            f'<rect x="430" y="260" width="340" height="300" rx="44" fill="{accent}" opacity=".80"/>'
            f'<circle cx="520" cy="350" r="34" fill="#fff" opacity=".55"/>'
            f'<circle cx="620" cy="430" r="50" fill="#fff" opacity=".33"/>'
            f'<rect x="500" y="580" width="220" height="36" rx="18" fill="{dark}" opacity=".20"/>'
        )
    if category.slug in {"outdoor", "fitness"}:
        return (
            f'<circle cx="600" cy="400" r="150" fill="{accent}" opacity=".72"/>'
            f'<path d="M450 560 C520 470 690 470 760 560" fill="none" stroke="{dark}" stroke-width="42" stroke-linecap="round" opacity=".82"/>'
            f'<circle cx="515" cy="560" r="36" fill="{dark}" opacity=".82"/>'
            f'<circle cx="690" cy="560" r="36" fill="{dark}" opacity=".82"/>'
        )
    if category.slug == "baby":
        return (
            f'<circle cx="600" cy="330" r="120" fill="{accent}" opacity=".70"/>'
            f'<rect x="470" y="435" width="260" height="160" rx="52" fill="{dark}" opacity=".68"/>'
            f'<circle cx="555" cy="320" r="15" fill="#fff"/><circle cx="645" cy="320" r="15" fill="#fff"/>'
        )
    if category.slug == "office":
        return (
            f'<rect x="430" y="230" width="330" height="420" rx="26" fill="#fff" stroke="{accent}" stroke-width="20"/>'
            f'<line x1="500" y1="330" x2="700" y2="330" stroke="{dark}" stroke-width="18" opacity=".55"/>'
            f'<line x1="500" y1="410" x2="680" y2="410" stroke="{dark}" stroke-width="18" opacity=".35"/>'
            f'<line x1="500" y1="490" x2="720" y2="490" stroke="{dark}" stroke-width="18" opacity=".35"/>'
        )
    if category.slug in {"bag", "gift"}:
        return (
            f'<rect x="455" y="310" width="290" height="270" rx="34" fill="{accent}" opacity=".82"/>'
            f'<path d="M520 315 Q600 190 680 315" fill="none" stroke="{dark}" stroke-width="28" stroke-linecap="round" opacity=".75"/>'
            f'<line x1="600" y1="315" x2="600" y2="580" stroke="#fff" stroke-width="16" opacity=".50"/>'
        )
    if category.slug == "book":
        return (
            f'<path d="M430 250 Q545 210 600 270 Q655 210 770 250 L770 610 Q650 570 600 635 Q550 570 430 610 Z" fill="{accent}" opacity=".80"/>'
            f'<path d="M600 275 L600 630" stroke="#fff" stroke-width="12" opacity=".72"/>'
            f'<line x1="470" y1="360" x2="560" y2="345" stroke="{dark}" stroke-width="13" opacity=".35"/>'
            f'<line x1="640" y1="345" x2="730" y2="360" stroke="{dark}" stroke-width="13" opacity=".35"/>'
        )
    if category.slug == "pet":
        return (
            f'<circle cx="600" cy="410" r="95" fill="{accent}" opacity=".82"/>'
            f'<circle cx="500" cy="330" r="42" fill="{accent}" opacity=".70"/>'
            f'<circle cx="700" cy="330" r="42" fill="{accent}" opacity=".70"/>'
            f'<circle cx="535" cy="535" r="45" fill="{dark}" opacity=".35"/>'
            f'<circle cx="665" cy="535" r="45" fill="{dark}" opacity=".35"/>'
        )
    return f'<circle cx="600" cy="420" r="150" fill="{accent}" opacity=".72"/>'


def write_svg(product: dict[str, object], variant: str, path: Path) -> None:
    category: CategoryConfig = product["category"]  # type: ignore[assignment]
    bg, accent, dark = category.palette
    seed = int(product["code"][-6:]) + (0 if variant == "main" else 1000 if variant == "detail-1" else 2000)
    shape = product_shape(category, seed)
    title = svg_text(str(product["name"]))
    brand = svg_text(str(product["brand"]))
    type_name = svg_text(str(product["product_type"]))
    variant_label = {"main": "MAIN", "detail-1": "DETAIL 01", "detail-2": "DETAIL 02"}[variant]
    svg = f'''<svg xmlns="http://www.w3.org/2000/svg" width="1200" height="900" viewBox="0 0 1200 900">
  <rect width="1200" height="900" fill="{bg}"/>
  <circle cx="170" cy="140" r="110" fill="{accent}" opacity=".12"/>
  <circle cx="1020" cy="720" r="180" fill="{accent}" opacity=".10"/>
  <path d="M0 760 C220 690 320 810 520 735 C760 645 850 720 1200 610 L1200 900 L0 900 Z" fill="#ffffff" opacity=".56"/>
  {shape}
  <rect x="70" y="70" width="260" height="54" rx="27" fill="#fff" opacity=".74"/>
  <text x="100" y="106" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="28" fill="{dark}" font-weight="700">{brand}</text>
  <text x="70" y="760" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="44" fill="{dark}" font-weight="800">{title}</text>
  <text x="70" y="818" font-family="Arial, 'Microsoft YaHei', sans-serif" font-size="28" fill="{dark}" opacity=".70">{type_name} · {variant_label} · {category.name}</text>
  <text x="950" y="106" font-family="Arial, sans-serif" font-size="24" fill="{dark}" opacity=".45">{product["code"]}</text>
</svg>
'''
    path.write_text(svg, encoding="utf-8")


def generate_products() -> list[dict[str, object]]:
    random.seed(RANDOM_SEED)
    products: list[dict[str, object]] = []
    code_num = START_CODE
    used_names: set[str] = set()

    for category in CATEGORIES:
        for local_idx in range(1, category.count + 1):
            code = f"SP{code_num:06d}"
            brand, product_type, feature, base_name = make_product_name(category, code_num, local_idx)
            name = base_name
            suffix_counter = 2
            while name in used_names:
                name = f"{base_name} {suffix_counter}件组"
                suffix_counter += 1
            used_names.add(name)
            price, market_price, discount_price, is_special = random_price(category, local_idx, product_type)
            stock = 0 if local_idx % 31 == 0 else random.randint(3, 260)
            if local_idx % 17 == 0:
                stock = random.randint(1, 9)
            sales = random.randint(0, 1200)
            rating = Decimal(str(random.choice([4.2, 4.3, 4.4, 4.5, 4.6, 4.7, 4.8, 4.9, 5.0])))
            review_count = random.randint(0, max(1, min(sales, 320)))
            is_recommended = 1 if local_idx % 5 == 0 else 0
            is_new = 1 if local_idx % 4 == 0 else 0
            short_desc, detail_desc, specs = description_for(category, name, product_type, feature, str(base_name).split()[-1])
            image_base = f"/products/generated/{category.slug}/{code}"
            products.append({
                "code": code,
                "name": name,
                "category": category,
                "category_id": category.category_id,
                "category_name": category.name,
                "brand": brand,
                "product_type": product_type,
                "price": price,
                "market_price": market_price,
                "discount_price": discount_price,
                "stock": stock,
                "sales": sales,
                "rating": money(rating),
                "review_count": review_count,
                "description": short_desc,
                "detail": detail_desc,
                "specs": specs,
                "status": 1,
                "is_special": is_special,
                "is_recommended": is_recommended,
                "is_new": is_new,
                "image_url": f"{image_base}-main.svg",
                "detail_image_1": f"{image_base}-detail-1.svg",
                "detail_image_2": f"{image_base}-detail-2.svg",
            })
            code_num += 1

    if len(products) != TOTAL_COUNT:
        raise RuntimeError(f"生成数量错误: {len(products)}")
    if len({p["code"] for p in products}) != TOTAL_COUNT:
        raise RuntimeError("商品编号重复")
    if len({p["name"] for p in products}) != TOTAL_COUNT:
        raise RuntimeError("商品名称重复")
    return products


def build_insert_sql(products: list[dict[str, object]]) -> str:
    lines = [
        "-- Generated by scripts/generate_1000_products.py",
        "-- Import manually after reviewing backup and data quality report.",
        "SET NAMES utf8mb4;",
        "START TRANSACTION;",
        "",
    ]
    for p in products:
        detail_urls = f"{p['detail_image_1']},{p['detail_image_2']}"
        lines.append(
            "INSERT INTO product "
            "(product_no,name,category_id,category_name,brand_id,brand_name,image_url,detail_image_url,price,market_price,discount_price,stock,sales,rating,review_count,description,specs,status,is_special,is_recommended,is_new,create_time,update_time,deleted) "
            "SELECT "
            f"{sql_quote(p['code'])},{sql_quote(p['name'])},{p['category_id']},{sql_quote(p['category_name'])},NULL,{sql_quote(p['brand'])},"
            f"{sql_quote(p['image_url'])},{sql_quote(detail_urls)},{p['price']},{p['market_price']},{p['discount_price']},"
            f"{p['stock']},{p['sales']},{p['rating']},{p['review_count']},{sql_quote(p['description'])},{sql_quote(p['specs'])},"
            f"1,{p['is_special']},{p['is_recommended']},{p['is_new']},NOW(),NOW(),0 "
            f"FROM DUAL WHERE NOT EXISTS (SELECT 1 FROM product WHERE product_no={sql_quote(p['code'])});"
        )
        for sort_no, image_url in enumerate((p["image_url"], p["detail_image_1"], p["detail_image_2"]), start=1):
            lines.append(
                "INSERT INTO product_image (product_id,image_url,sort_no) "
                f"SELECT p.id,{sql_quote(image_url)},{sort_no} FROM product p "
                f"WHERE p.product_no={sql_quote(p['code'])} "
                f"AND NOT EXISTS (SELECT 1 FROM product_image pi WHERE pi.product_id=p.id AND pi.image_url={sql_quote(image_url)});"
            )
        lines.append(
            "INSERT INTO product_sku (product_id,sku_name,spec_info,price,stock) "
            f"SELECT p.id,'默认规格',{sql_quote(p['specs'])},{p['discount_price']},{p['stock']} FROM product p "
            f"WHERE p.product_no={sql_quote(p['code'])} "
            "AND NOT EXISTS (SELECT 1 FROM product_sku ps WHERE ps.product_id=p.id AND ps.sku_name='默认规格');"
        )
        lines.append("")
    lines.extend([
        "COMMIT;",
        "",
        "SELECT 'expected_generated_count' AS metric, 1000 AS value;",
        "SELECT 'current_generated_count' AS metric, COUNT(*) AS value FROM product WHERE product_no BETWEEN 'SP300001' AND 'SP301000';",
    ])
    return "\n".join(lines)


def build_rollback_sql() -> str:
    return """-- Roll back only products generated by scripts/generate_1000_products.py.
SET NAMES utf8mb4;
START TRANSACTION;

DELETE pi
FROM product_image pi
JOIN product p ON p.id = pi.product_id
WHERE p.product_no BETWEEN 'SP300001' AND 'SP301000';

DELETE ps
FROM product_sku ps
JOIN product p ON p.id = ps.product_id
WHERE p.product_no BETWEEN 'SP300001' AND 'SP301000';

DELETE FROM product
WHERE product_no BETWEEN 'SP300001' AND 'SP301000';

COMMIT;

SELECT COUNT(*) AS remaining_generated_products
FROM product
WHERE product_no BETWEEN 'SP300001' AND 'SP301000';
"""


def build_verify_sql() -> str:
    category_expect = "\nUNION ALL\n".join(
        f"SELECT {c.category_id} AS category_id, '{c.name}' AS category_name, {c.count} AS expected_count"
        for c in CATEGORIES
    )
    return f"""-- Verification SQL for generated 1000 products.
SET NAMES utf8mb4;

-- 1. 新增数量，预期 1000
SELECT COUNT(*) AS generated_product_count
FROM product
WHERE product_no BETWEEN 'SP300001' AND 'SP301000';

-- 2. 编号重复，预期 0 行
SELECT product_no, COUNT(*) AS duplicate_count
FROM product
GROUP BY product_no
HAVING COUNT(*) > 1;

-- 3. 新增商品名称重复，预期 0 行
SELECT name, COUNT(*) AS duplicate_count
FROM product
WHERE product_no BETWEEN 'SP300001' AND 'SP301000'
GROUP BY name
HAVING COUNT(*) > 1;

-- 4. 分类统计，actual_count 应等于 expected_count
WITH expected AS (
{category_expect}
),
actual AS (
  SELECT category_id, COUNT(*) AS actual_count
  FROM product
  WHERE product_no BETWEEN 'SP300001' AND 'SP301000'
  GROUP BY category_id
)
SELECT e.category_id, e.category_name, e.expected_count, COALESCE(a.actual_count, 0) AS actual_count
FROM expected e
LEFT JOIN actual a ON a.category_id = e.category_id
ORDER BY e.category_id;

-- 5. 空字段，预期 0
SELECT COUNT(*) AS empty_required_field_count
FROM product
WHERE product_no BETWEEN 'SP300001' AND 'SP301000'
  AND (
    product_no IS NULL OR product_no = ''
    OR name IS NULL OR name = ''
    OR category_id IS NULL
    OR category_name IS NULL OR category_name = ''
    OR price IS NULL
    OR image_url IS NULL OR image_url = ''
    OR status IS NULL
  );

-- 6. 非法值，预期 0
SELECT COUNT(*) AS invalid_value_count
FROM product
WHERE product_no BETWEEN 'SP300001' AND 'SP301000'
  AND (price <= 0 OR stock < 0 OR sales < 0 OR market_price < price);

-- 7. 新增商品全部上架，预期 1000
SELECT COUNT(*) AS generated_on_sale_count
FROM product
WHERE product_no BETWEEN 'SP300001' AND 'SP301000' AND status = 1;

-- 8. 图片关联数量，预期 3000
SELECT COUNT(*) AS generated_product_image_count
FROM product_image pi
JOIN product p ON p.id = pi.product_id
WHERE p.product_no BETWEEN 'SP300001' AND 'SP301000';

-- 9. SKU 数量，预期 1000
SELECT COUNT(*) AS generated_sku_count
FROM product_sku ps
JOIN product p ON p.id = ps.product_id
WHERE p.product_no BETWEEN 'SP300001' AND 'SP301000';
"""


def write_reports(products: list[dict[str, object]], image_missing: int) -> None:
    category_lines = []
    for category in CATEGORIES:
        actual = sum(1 for p in products if p["category_id"] == category.category_id)
        category_lines.append(f"| {category.category_id} | {category.name} | {category.count} | {actual} |")

    GENERATION_REPORT.write_text(
        "# 1000 商品生成报告\n\n"
        f"- 生成商品数量：{len(products)}\n"
        "- 商品编号范围：SP300001 至 SP301000\n"
        "- 商品状态：全部上架 status=1\n"
        "- 图片生成方式：本地 SVG，1200x900，4:3\n"
        f"- 图片总数：{len(products) * 3}\n"
        f"- 图片缺失数：{image_missing}\n"
        "- 是否已导入数据库：否\n\n"
        "## 分类分布\n\n"
        "| 分类ID | 分类名称 | 计划数量 | 实际生成 |\n"
        "| ---: | --- | ---: | ---: |\n"
        + "\n".join(category_lines)
        + "\n\n## SQL 文件\n\n"
        f"- 插入 SQL：`{INSERT_SQL.relative_to(ROOT)}`\n"
        f"- 回滚 SQL：`{ROLLBACK_SQL.relative_to(ROOT)}`\n"
        f"- 校验 SQL：`{VERIFY_SQL.relative_to(ROOT)}`\n"
        f"- 备份 SQL：`docs/sql/product_backup_before_1000.sql`\n",
        encoding="utf-8",
    )

    DATA_QUALITY_REPORT.write_text(
        "# 当前商品数据质量报告\n\n"
        "本报告只记录执行前已发现的数据问题，本阶段不自动修改旧数据。\n\n"
        "## 数据库快照\n\n"
        "- 当前商品总数：73\n"
        "- 当前上架商品数：72\n"
        "- 当前下架商品数：1\n"
        "- 当前最大商品 ID：74\n"
        "- 当前最大商品编号（按字符串 MAX）：SP20072\n"
        "- 当前分类数量：16\n"
        "- SP300001 至 SP301000 占用数量：0\n\n"
        "## 已发现异常\n\n"
        "1. `SP10030 复古蓝牙音箱` 当前为下架状态。\n"
        "2. `product_no=123` 的 `机甲` 商品编号格式与 SP 系列不一致。\n"
        "3. `护眼台灯Pro`、`桌面收纳盒套装` 当前分类为 `宠物用品`，疑似分类错误。\n"
        "4. 旧数据中存在 `图书文具` 分类名展示，但真实分类表为 `办公文具` 与 `图书音像`。\n"
        "5. `50元节日礼品卡`、`100元生日礼品卡` 的历史价格与名称面额不一致。\n"
        "6. 当前旧商品存在重复名称：`儿童积木套装`、`气泡饮料组合`、`算法入门图书`。\n\n"
        "## 本次新增数据规避策略\n\n"
        "- 新增商品只使用真实存在的 16 个 `category_id`。\n"
        "- 宠物用品只生成猫粮、狗粮、猫砂、宠物玩具、牵引绳等宠物相关商品。\n"
        "- 礼品卡名称中的面额与价格保持一致。\n"
        "- 新商品编号统一使用 `SP300001` 至 `SP301000`。\n"
        "- 新商品名称去重，生成脚本校验重复数量为 0。\n"
        "- 所有新增商品均设置 `status=1`。\n\n"
        "## 未自动处理的问题\n\n"
        "- 未修改任何旧商品分类、价格、状态或编号。\n"
        "- 未给旧数据补充图片或 SKU。\n"
        "- 后端 `PageQueryDTO.size` 目前未限制最大 100，本阶段仅记录，不扩大后端业务改动。\n",
        encoding="utf-8",
    )


def main() -> None:
    SQL_DIR.mkdir(parents=True, exist_ok=True)
    FRONTEND_IMAGE_ROOT.mkdir(parents=True, exist_ok=True)
    products = generate_products()

    for p in products:
        category: CategoryConfig = p["category"]  # type: ignore[assignment]
        category_dir = FRONTEND_IMAGE_ROOT / category.slug
        category_dir.mkdir(parents=True, exist_ok=True)
        for variant in ("main", "detail-1", "detail-2"):
            write_svg(p, variant, category_dir / f"{p['code']}-{variant}.svg")

    INSERT_SQL.write_text(build_insert_sql(products), encoding="utf-8")
    ROLLBACK_SQL.write_text(build_rollback_sql(), encoding="utf-8")
    VERIFY_SQL.write_text(build_verify_sql(), encoding="utf-8")

    expected_images = [
        FRONTEND_IMAGE_ROOT / str(p["category"].slug) / f"{p['code']}-{variant}.svg"  # type: ignore[attr-defined]
        for p in products
        for variant in ("main", "detail-1", "detail-2")
    ]
    image_missing = sum(1 for path in expected_images if not path.exists())
    write_reports(products, image_missing)

    print(f"products={len(products)}")
    print(f"images={len(expected_images)}")
    print(f"image_missing={image_missing}")
    print(f"insert_sql={INSERT_SQL}")
    print(f"rollback_sql={ROLLBACK_SQL}")
    print(f"verify_sql={VERIFY_SQL}")
    print(f"generation_report={GENERATION_REPORT}")
    print(f"data_quality_report={DATA_QUALITY_REPORT}")


if __name__ == "__main__":
    main()
