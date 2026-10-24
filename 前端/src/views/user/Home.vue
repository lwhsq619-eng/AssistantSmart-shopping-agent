<template>
  <div class="mall-home">
    <el-carousel class="hero-carousel" height="320px" indicator-position="outside">
      <el-carousel-item v-for="banner in banners" :key="banner.title">
        <section class="hero" :style="{ backgroundImage: `linear-gradient(90deg, rgba(15,23,42,.76), rgba(37,99,235,.2)), url(${banner.image})` }">
          <div class="hero-copy">
            <p>{{ banner.eyebrow }}</p>
            <h1>{{ banner.title }}</h1>
            <span>{{ banner.desc }}</span>
          </div>
          <el-button type="warning" size="large" @click="$router.push(banner.link)">立即查看</el-button>
        </section>
      </el-carousel-item>
    </el-carousel>

    <section class="ai-shopping-entry">
      <div>
        <p>AI 智能购物助手</p>
        <h2>描述你的预算、用途和偏好</h2>
        <span>输入自然语言需求，跳转 AI 导购页并自动开始推荐，商品结果来自数据库。</span>
      </div>
      <div class="ai-entry-form">
        <el-input
          v-model="aiQuestion"
          size="large"
          placeholder="例如：500元以内适合跑步的无线耳机"
          clearable
          @keyup.enter="goAiGuide(aiQuestion)"
        >
          <template #append>
            <el-button type="primary" @click="goAiGuide(aiQuestion)">发送</el-button>
          </template>
        </el-input>
        <div class="quick-tags">
          <el-button v-for="item in aiQuickQuestions" :key="item" size="small" round plain @click="goAiGuide(item)">
            {{ item }}
          </el-button>
        </div>
      </div>
    </section>

    <section class="category-section">
      <div class="section-head">
        <div>
          <h2>全品类商城入口</h2>
          <p>16 个商品分类已接入数据库商品列表，点击即可进入对应分类。</p>
        </div>
        <el-button type="primary" plain @click="$router.push('/user/category')">查看全部商品</el-button>
      </div>
      <div class="category-grid">
        <article
          v-for="item in enhancedCategories"
          :key="item.id"
          class="category-card"
          @click="$router.push(`/user/category?categoryId=${item.id}`)"
        >
          <img :src="item.imageUrl || categoryBanner" :alt="item.name" @error="imageError" />
          <div>
            <h3>{{ item.name }}</h3>
            <p>{{ item.description }}</p>
            <el-button size="small" type="primary" plain>进入分类</el-button>
          </div>
        </article>
      </div>
    </section>

    <MallSection title="今日特价" subtitle="数据库 is_special = 1 控制，点击商品可进入详情页">
      <ProductCard v-for="item in special" :key="item.id" :product="item" @add-cart="addCart" />
    </MallSection>

    <MallSection title="热门推荐" subtitle="优先展示真实商品图，再按销量排序">
      <ProductCard v-for="item in hot" :key="item.id" :product="item" @add-cart="addCart" />
    </MallSection>

    <section class="category-showcase">
      <div class="showcase-card" v-for="item in showcaseCategories" :key="item.id" @click="$router.push(`/user/category?categoryId=${item.id}`)">
        <img :src="item.imageUrl" :alt="item.name" @error="imageError" />
        <div>
          <p>分类精选</p>
          <h3>{{ item.name }}</h3>
          <span>{{ item.description }}</span>
        </div>
      </div>
    </section>

    <MallSection title="新品上市" subtitle="按创建时间排序，展示最新上架商品">
      <ProductCard v-for="item in newest" :key="item.id" :product="item" @add-cart="addCart" />
    </MallSection>

    <section class="brand-section">
      <div class="section-head">
        <div>
          <h2>品牌故事</h2>
          <p>品牌卡片与商品列表联动，点击进入品牌商品。</p>
        </div>
      </div>
      <div class="brand-grid">
        <article v-for="brand in brands" :key="brand.id" class="brand-card" @click="$router.push(`/user/search?brandId=${brand.id}`)">
          <img :src="brand.imageUrl || brandFallbackImage" :alt="brand.name" @error="imageError" />
          <div>
            <h3>{{ brand.name }}</h3>
            <b>{{ brand.slogan }}</b>
            <p>{{ brand.description }}</p>
          </div>
        </article>
      </div>
    </section>

    <section class="gift-banner">
      <div>
        <p>礼品卡专区</p>
        <h2>节日礼物、生日礼盒和电子礼品卡一站选购</h2>
        <span>购买后生成兑换码，可在我的礼品卡查看。</span>
      </div>
      <el-button type="warning" size="large" @click="$router.push('/user/gift-card')">购买礼品卡</el-button>
    </section>

    <MallSection title="猜你喜欢" subtitle="高评分商品推荐，支持加入购物车和查看详情">
      <ProductCard v-for="item in guess" :key="item.id" :product="item" @add-cart="addCart" />
    </MallSection>

    <section class="service-row">
      <div><el-icon><Van /></el-icon><b>快速配送</b><span>订单、支付、收货流程完整演示</span></div>
      <div><el-icon><CreditCard /></el-icon><b>模拟支付</b><span>不接入真实支付，适合课程设计演示</span></div>
      <div><el-icon><ChatLineRound /></el-icon><b>AI 服务</b><span>导购、搜索、客服与商品数据联动</span></div>
      <div><el-icon><Lock /></el-icon><b>权限隔离</b><span>用户只看自己的订单和购物车</span></div>
    </section>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import ProductCard from '../../components/ProductCard.vue'
import { addCartApi, getBrandsApi, getCategoriesApi, getRecommendProductsApi, getSpecialProductsApi } from '../../api/mall'
import { categoryBanner, imageError } from '../../utils/images'

const categories = ref([])
const router = useRouter()
const brands = ref([])
const special = ref([])
const hot = ref([])
const newest = ref([])
const guess = ref([])
const aiQuestion = ref('')
const aiQuickQuestions = ['500元以内无线耳机', '宿舍收纳好物', '生日礼物推荐', '办公桌面设备', '宠物清洁用品', '健身入门装备']

const categoryMeta = {
  数码科技: {
    image: 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=900&q=85',
    desc: '投影仪、耳机、智能手表和智能设备'
  },
  时尚服饰: {
    image: 'https://images.unsplash.com/photo-1445205170230-053b83016050?auto=format&fit=crop&w=900&q=85',
    desc: '卫衣、外套、针织衫和日常穿搭'
  },
  家居生活: {
    image: 'https://images.unsplash.com/photo-1513161455079-7dc1de15ef3e?auto=format&fit=crop&w=900&q=85',
    desc: '台灯、收纳、香薰和居家用品'
  },
  美妆护肤: {
    image: 'https://images.unsplash.com/photo-1596462502278-27bfdc403348?auto=format&fit=crop&w=900&q=85',
    desc: '护肤品、香水、彩妆和个人护理'
  },
  食品饮料: {
    image: 'https://images.unsplash.com/photo-1542838132-92c53300491e?auto=format&fit=crop&w=900&q=85',
    desc: '咖啡、零食、饮品与健康食品'
  },
  运动户外: {
    image: 'https://images.unsplash.com/photo-1518611012118-696072aa579a?auto=format&fit=crop&w=900&q=85',
    desc: '户外装备、运动配件和训练用品'
  },
  母婴用品: {
    image: 'https://images.unsplash.com/photo-1515488042361-ee00e0ddd4e4?auto=format&fit=crop&w=900&q=85',
    desc: '母婴护理、儿童玩具和成长用品'
  },
  办公文具: {
    image: 'https://images.unsplash.com/photo-1516321497487-e288fb19713f?auto=format&fit=crop&w=900&q=85',
    desc: '文具、桌面用品和办公设备'
  },
  箱包配饰: {
    image: 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?auto=format&fit=crop&w=900&q=85',
    desc: '背包、钱包、配饰和出行用品'
  },
  图书音像: {
    image: 'https://images.unsplash.com/photo-1512820790803-83ca734da794?auto=format&fit=crop&w=900&q=85',
    desc: '教材、畅销书、影音与学习资料'
  },
  手机通讯: {
    image: 'https://images.unsplash.com/photo-1511707171634-5f897ff02aa9?auto=format&fit=crop&w=900&q=85',
    desc: '手机、充电器、支架和移动配件'
  },
  电脑办公: {
    image: 'https://images.unsplash.com/photo-1496181133206-80ce9b88a853?auto=format&fit=crop&w=900&q=85',
    desc: '显示器、键盘、鼠标和办公外设'
  },
  服饰鞋包: {
    image: 'https://images.unsplash.com/photo-1542291026-7eec264c27ff?auto=format&fit=crop&w=900&q=85',
    desc: '运动鞋、卫衣、背包与潮流配饰'
  },
  宠物用品: {
    image: 'https://images.unsplash.com/photo-1548199973-03cce0bbc87b?auto=format&fit=crop&w=900&q=85',
    desc: '宠物玩具、清洁用品和日常护理'
  },
  礼品专区: {
    image: 'https://images.unsplash.com/photo-1513885535751-8b9238bd345a?auto=format&fit=crop&w=900&q=85',
    desc: '礼品卡、节日礼盒与生日礼物'
  },
  健身: {
    image: 'https://images.unsplash.com/photo-1534258936925-c58bed479fcb?auto=format&fit=crop&w=900&q=85',
    desc: '壶铃、泡沫轴、健身踏板和训练装备'
  }
}

const banners = [
  { eyebrow: '全品类商城升级', title: '16 个分类真实联动，浏览、搜索、下单完整贯通', desc: '分类入口、商品列表、AI 导购和购物车全部读取数据库商品', image: 'https://images.unsplash.com/photo-1607082350899-7e105aa886ae?auto=format&fit=crop&w=1800&q=85', link: '/user/category' },
  { eyebrow: '数码科技焕新季', title: '投影仪、耳机、手机配件和电脑外设', desc: '热门数码商品已替换真实摄影图并前置展示', image: 'https://images.unsplash.com/photo-1516321318423-f06f85e504b3?auto=format&fit=crop&w=1800&q=85', link: '/user/category?categoryId=1' },
  { eyebrow: '礼品与生活专区', title: '礼品卡、咖啡礼盒、家居好物一站选购', desc: '从分类浏览到模拟支付，适合课程设计完整演示', image: 'https://images.unsplash.com/photo-1513885535751-8b9238bd345a?auto=format&fit=crop&w=1800&q=85', link: '/user/gift-card' },
  { eyebrow: '运动健身新品', title: '户外装备与健身器材同步上架', desc: '健身分类已补齐图片和描述，首页可直接进入', image: 'https://images.unsplash.com/photo-1534258936925-c58bed479fcb?auto=format&fit=crop&w=1800&q=85', link: '/user/category?categoryId=16' }
]

const brandFallbackImage = 'https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=900&q=85'

const enhancedCategories = computed(() => categories.value.map((item) => {
  const meta = categoryMeta[item.name] || {}
  return {
    ...item,
    imageUrl: item.imageUrl || meta.image || categoryBanner,
    description: item.description || meta.desc || '精选商城商品分类'
  }
}))

const showcaseCategories = computed(() => enhancedCategories.value.filter((item) =>
  ['数码科技', '家居生活', '食品饮料', '服饰鞋包', '宠物用品', '健身'].includes(item.name)
))

async function load() {
  const [categoryData, brandData, specialData, hotData, newData, guessData] = await Promise.all([
    getCategoriesApi(),
    getBrandsApi(),
    getSpecialProductsApi(),
    getRecommendProductsApi('hot'),
    getRecommendProductsApi('new'),
    getRecommendProductsApi('guess')
  ])
  categories.value = categoryData
  brands.value = brandData
  special.value = specialData.slice(0, 8)
  hot.value = hotData.slice(0, 8)
  newest.value = newData.slice(0, 8)
  guess.value = guessData.slice(0, 8)
}

async function addCart(product) {
  await addCartApi({ productId: product.id, specInfo: '默认规格', quantity: 1 })
  ElMessage.success('已加入购物车')
}

function goAiGuide(question) {
  router.push({ path: '/ai/guide', query: question && question.trim() ? { q: question.trim() } : {} })
}

onMounted(load)
</script>

<script>
export default {
  components: {
    MallSection: {
      props: ['title', 'subtitle'],
      template: `
        <section class="mall-section">
          <div class="section-head">
            <div><h2>{{ title }}</h2><p>{{ subtitle }}</p></div>
          </div>
          <div class="product-grid"><slot /></div>
        </section>
      `
    }
  }
}
</script>

<style scoped>
.mall-home {
  display: flex;
  flex-direction: column;
  gap: 28px;
}

.hero {
  height: 100%;
  border-radius: 14px;
  padding: 42px;
  color: #fff;
  display: flex;
  justify-content: space-between;
  align-items: flex-end;
  background-size: cover;
  background-position: center;
  overflow: hidden;
}

.hero-copy {
  max-width: 680px;
}

.hero p {
  margin: 0 0 10px;
  color: #fbbf24;
  font-weight: 900;
}

.hero h1 {
  margin: 0 0 10px;
  font-size: 40px;
  line-height: 1.18;
}

.hero span {
  color: rgba(255, 255, 255, 0.88);
  line-height: 1.7;
}

.ai-shopping-entry {
  display: grid;
  grid-template-columns: minmax(260px, 430px) minmax(0, 1fr);
  gap: 18px;
  align-items: center;
  padding: 22px;
  border-radius: 14px;
  background:
    linear-gradient(135deg, rgba(239, 246, 255, .98), rgba(255, 247, 237, .88)),
    url('https://images.unsplash.com/photo-1556742049-0cfed4f6a45d?auto=format&fit=crop&w=1200&q=85') right center/cover;
  border: 1px solid #dbeafe;
  box-shadow: 0 10px 28px rgba(15, 23, 42, 0.08);
}

.ai-shopping-entry p,
.gift-banner p,
.showcase-card p {
  margin: 0 0 6px;
  color: #2563eb;
  font-weight: 900;
}

.ai-shopping-entry h2 {
  margin: 0 0 8px;
  color: #111827;
}

.ai-shopping-entry span {
  color: #64748b;
}

.ai-entry-form {
  display: grid;
  gap: 12px;
}

.quick-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
}

.category-section,
.mall-section,
.brand-section {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.section-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 14px;
}

.section-head h2 {
  margin: 0 0 4px;
}

.section-head p {
  margin: 0;
  color: #64748b;
  line-height: 1.5;
}

.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 16px;
}

.brand-grid,
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 18px;
}

.category-card,
.brand-card {
  display: grid;
  grid-template-columns: 104px 1fr;
  gap: 14px;
  align-items: center;
  padding: 14px;
  border-radius: 14px;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.07);
  cursor: pointer;
  transition: transform .2s ease, box-shadow .2s ease;
}

.category-card:hover,
.brand-card:hover,
.showcase-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 20px 42px rgba(15, 23, 42, 0.12);
}

.category-card img,
.brand-card img {
  width: 104px;
  height: 104px;
  border-radius: 12px;
  object-fit: cover;
  background: #f1f5f9;
}

.category-card h3,
.brand-card h3,
.showcase-card h3 {
  margin: 0 0 6px;
}

.category-card p,
.brand-card p,
.showcase-card span {
  margin: 0 0 10px;
  color: #64748b;
  line-height: 1.5;
}

.category-showcase {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(310px, 1fr));
  gap: 18px;
}

.showcase-card {
  min-height: 190px;
  border-radius: 16px;
  overflow: hidden;
  display: grid;
  grid-template-columns: 44% 1fr;
  background: #fff;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.07);
  cursor: pointer;
  transition: transform .2s ease, box-shadow .2s ease;
}

.showcase-card img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.showcase-card div {
  padding: 22px;
}

.gift-banner {
  min-height: 170px;
  border-radius: 16px;
  padding: 30px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
  background:
    linear-gradient(90deg, rgba(15, 23, 42, 0.78), rgba(245, 158, 11, 0.3)),
    url('https://images.unsplash.com/photo-1513201099705-a9746e1e201f?auto=format&fit=crop&w=1600&q=85') center/cover;
}

.gift-banner p {
  color: #fbbf24;
}

.gift-banner h2 {
  margin: 0 0 8px;
}

.gift-banner span {
  color: rgba(255, 255, 255, .86);
}

.service-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.service-row div {
  min-height: 96px;
  padding: 18px;
  border-radius: 14px;
  background: #fff;
  display: flex;
  flex-direction: column;
  gap: 6px;
  box-shadow: 0 12px 28px rgba(15, 23, 42, 0.06);
}

.service-row .el-icon {
  color: #2563eb;
  font-size: 24px;
}

.service-row span {
  color: #64748b;
}

@media (max-width: 900px) {
  .hero,
  .gift-banner {
    padding: 24px;
    align-items: flex-start;
    flex-direction: column;
    gap: 18px;
  }

  .ai-shopping-entry {
    grid-template-columns: 1fr;
  }

  .service-row {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 560px) {
  .category-card,
  .brand-card,
  .showcase-card {
    grid-template-columns: 1fr;
  }

  .category-card img,
  .brand-card img {
    width: 100%;
    height: 160px;
  }

  .service-row {
    grid-template-columns: 1fr;
  }
}
</style>
