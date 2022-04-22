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
import ConsentResponseMenu from '../views/ConsentResponseMenu.vue'
import AccountsMenu from '../views/AccountsMenu.vue'
import ResourcesResponse from '../views/ResourcesResponse.vue'

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
  },
  {
    path: '/consent-response-menu',
    name: 'consent-response-menu',
    component: ConsentResponseMenu
  },
  {
    path: '/accounts',
    name: 'accounts',
    component: AccountsMenu
  },
  {
    path: '/resources',
    name: 'resources',
    component: ResourcesResponse
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

export default router
