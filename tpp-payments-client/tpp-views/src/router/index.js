import Vue from 'vue'
import VueRouter from 'vue-router'
import HomeView from '../views/HomeView.vue'
import BanksView from '../views/BanksView.vue'
import MainMenuView from '../views/MainMenuView.vue'
import PaymentDetail from '../views/PaymentDetails.vue'
import PatchView from '../views/PatchView.vue'
import PaymentResponseView from '../views/PaymentResponseView.vue'
import PatchResponseView from '../views/PatchResponseView.vue'
import ConsentMenu from '../views/ConsentMenu.vue'

Vue.use(VueRouter)

const routes = [
  {
    path: '/',
    name: 'home',
    component: HomeView
  },
  {
    path: '/banks',
    name: 'banks',
    component: BanksView
  },
  {
    path: '/payment-menu',
    name: 'payment-menu',
    component: MainMenuView,
  },
  {
    path: '/payment-detail',
    name: 'payment-detail',
    component: PaymentDetail
  },
  {
    path: '/patch-detail',
    name: 'patch-detail',
    component: PatchView
  },
  {
    path: '/payment-response',
    name: 'payment-response',
    component: PaymentResponseView
  },
  {
    path: '/patch-response',
    name: 'patch-response',
    component: PatchResponseView
  },
  {
    path: '/consent-menu',
    name: 'consent-menu',
    component: ConsentMenu
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
