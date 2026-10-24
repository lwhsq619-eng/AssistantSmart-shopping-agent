import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import AdminLayout from '../layout/AdminLayout.vue'
import UserLayout from '../layout/UserLayout.vue'
import AdminLogin from '../views/admin/AdminLogin.vue'
import UserLogin from '../views/user/UserLogin.vue'
import Register from '../views/user/Register.vue'
import UserHome from '../views/user/Home.vue'
import ProductList from '../views/user/ProductList.vue'
import ProductDetail from '../views/user/ProductDetail.vue'
import Cart from '../views/user/Cart.vue'
import Checkout from '../views/user/Checkout.vue'
import Payment from '../views/user/Payment.vue'
import Orders from '../views/user/Orders.vue'
import OrderDetail from '../views/user/OrderDetail.vue'
import Reviews from '../views/user/Reviews.vue'
import GiftCard from '../views/user/GiftCard.vue'
import Profile from '../views/user/Profile.vue'
import DashboardView from '../views/DashboardView.vue'
import ProductsView from '../views/ProductsView.vue'
import CategoriesView from '../views/CategoriesView.vue'
import UsersView from '../views/UsersView.vue'
import OrdersView from '../views/OrdersView.vue'
import Forbidden from '../views/error/Forbidden.vue'
import AiGuide from '../views/ai/AiGuide.vue'
import AiCustomerService from '../views/ai/AiCustomerService.vue'
import AiSearch from '../views/ai/AiSearch.vue'
import AiWorkbench from '../views/admin/AiWorkbench.vue'
import AiInventory from '../views/admin/AiInventory.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/', redirect: () => redirectHome() },
    { path: '/login', redirect: '/login/user' },
    { path: '/login/admin', component: AdminLogin, meta: { public: true } },
    { path: '/login/user', component: UserLogin, meta: { public: true } },
    { path: '/register', component: Register, meta: { public: true } },
    {
      path: '/admin',
      component: AdminLayout,
      redirect: '/admin/dashboard',
      meta: { role: 'ADMIN' },
      children: [
        { path: 'dashboard', component: DashboardView, meta: { title: '首页仪表盘', role: 'ADMIN' } },
        { path: 'products', component: ProductsView, meta: { title: '商品管理', role: 'ADMIN' } },
        { path: 'categories', component: CategoriesView, meta: { title: '分类管理', role: 'ADMIN' } },
        { path: 'users', component: UsersView, meta: { title: '用户管理', role: 'ADMIN' } },
        { path: 'orders', component: OrdersView, meta: { title: '订单管理', role: 'ADMIN' } },
        { path: 'ai/workbench', component: AiWorkbench, meta: { title: '商家 AI 工作台', role: 'ADMIN' } },
        { path: 'ai/copywriting', redirect: '/admin/ai/workbench?tab=copy' },
        { path: 'ai/comment', redirect: '/admin/ai/workbench?tab=comment' },
        { path: 'ai/marketing', redirect: '/admin/ai/workbench?tab=marketing' },
        { path: 'ai/order-analysis', redirect: '/admin/ai/workbench?tab=order' },
        { path: 'ai/briefing', redirect: '/admin/ai/workbench?tab=briefing' },
        { path: 'ai/inventory', component: AiInventory, meta: { title: '仓储 AI 工作台', role: 'ADMIN' } }
      ]
    },
    {
      path: '/user',
      component: UserLayout,
      redirect: '/user/home',
      meta: { role: 'USER' },
      children: [
        { path: 'home', component: UserHome, meta: { title: '商城首页', role: 'USER' } },
        { path: 'category', component: ProductList, meta: { title: '商品分类', role: 'USER' } },
        { path: 'search', component: ProductList, meta: { title: '搜索商品', role: 'USER' } },
        { path: 'product/:id', component: ProductDetail, meta: { title: '商品详情', role: 'USER' } },
        { path: 'cart', component: Cart, meta: { title: '购物车', role: 'USER' } },
        { path: 'checkout', component: Checkout, meta: { title: '订单结算', role: 'USER' } },
        { path: 'payment/:orderNo', component: Payment, meta: { title: '模拟支付', role: 'USER' } },
        { path: 'orders', component: Orders, meta: { title: '我的订单', role: 'USER' } },
        { path: 'order/:id', component: OrderDetail, meta: { title: '订单详情', role: 'USER' } },
        { path: 'reviews', component: Reviews, meta: { title: '我的评价', role: 'USER' } },
        { path: 'gift-card', component: GiftCard, meta: { title: '礼品卡', role: 'USER' } },
        { path: 'profile', component: Profile, meta: { title: '个人中心', role: 'USER' } },
        { path: '/ai/guide', component: AiGuide, meta: { title: 'AI 导购', role: 'USER' } },
        { path: '/ai/customer-service', component: AiCustomerService, meta: { title: '智能客服', role: 'USER' } },
        { path: '/ai/search', component: AiSearch, meta: { title: '语义搜索', role: 'USER' } }
      ]
    },
    { path: '/403', component: Forbidden, meta: { public: true } },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

function redirectHome() {
  const auth = useAuthStore()
  if (!auth.isLogin) return '/login/user'
  return auth.role === 'ADMIN' ? '/admin/dashboard' : '/user/home'
}

router.beforeEach((to) => {
  const auth = useAuthStore()
  if (to.meta.public) {
    if (auth.isLogin && (to.path.startsWith('/login') || to.path === '/register')) {
      return auth.role === 'ADMIN' ? '/admin/dashboard' : '/user/home'
    }
    return true
  }

  if (!auth.isLogin) {
    return to.path.startsWith('/admin') ? '/login/admin' : '/login/user'
  }

  const role = to.meta.role || to.matched.find((item) => item.meta.role)?.meta.role
  if (role && auth.role !== role) {
    return auth.role === 'ADMIN' ? '/admin/dashboard' : '/user/home'
  }

  return true
})

export default router
