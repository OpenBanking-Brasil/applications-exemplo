import Vue from "vue";
import VueRouter from "vue-router";
import HomeView from "../views/HomeView.vue";
import BanksView from "../views/BanksView.vue";
import MainMenuView from "../views/MainMenuView.vue";
import PaymentDetail from "../views/PaymentDetails.vue";
import PaymentConsent from "../views/PaymentConsent.vue";
import PaymentConsentResponse from "../views/PaymentConsentResponse.vue";
import PatchView from "../views/PatchView.vue";
import PaymentResponseView from "../views/PaymentResponseView.vue";
import PatchResponseView from "../views/PatchResponseView.vue";
import ConsentMenu from "../views/ConsentMenu.vue";
import ConsentResponseMenu from "../views/ConsentResponseMenu.vue";
import AccountsMenu from "../views/AccountsMenu.vue";
import ResourcesResponse from "../views/ResourcesResponse.vue";
import CreditCardAccounts from "../views/CreditCardAccounts.vue";
import CustomersMenu from "../views/Customers.vue";
import LoansMenu from "../views/LoansMenu.vue";
import FinancingsMenu from "../views/FinancingsMenu.vue";
import InvoiceFinancings from "../views/InvoiceFinancings.vue";
import UnarrangedAccountsOverdraft from "../views/UnarrangedAccountsOverdraft.vue";
import ConsentsApi from "../views/ConsentsApi.vue";
import NotFound from "../views/NotFound.vue";

Vue.use(VueRouter);

const routes = [
  {
    path: "/",
    name: "home",
    component: HomeView,
    meta: {
      breadCrumb: [ { text: "Home" } ],
    }
  },
  {
    path: "/banks",
    name: "banks",
    component: BanksView,
    meta: {
      breadCrumb: [ { text: "Banks" } ],
    }
  },
  {
    path: "/payment-menu",
    name: "payment-menu",
    component: MainMenuView,
    meta: {
      breadCrumb: [ { text: "Payments" } ],
    }
  },
  {
    path: "/payment-detail",
    name: "payment-detail",
    component: PaymentDetail,
    meta: {
      breadCrumb: [ { text: "Payment" } ],
    }
  },
  {
    path: "/payment-consent",
    name: "payment-consent",
    component: PaymentConsent,
    meta: {
      breadCrumb: [ { text: "Payment consent" } ],
    }
  },
  {
    path: "/payment-consent-response",
    name: "payment-consent-response",
    component: PaymentConsentResponse,
    meta: {
      breadCrumb: [ { text: "Payment Consent Response" } ],
      backNav: "payment-menu"
    }
  },
  {
    path: "/patch-detail",
    name: "patch-detail",
    component: PatchView,
    meta: {
      breadCrumb: [ { text: "Patch Details" } ],
      backNav: "payment-menu"
    }
  },
  {
    path: "/payment-response",
    name: "payment-response",
    component: PaymentResponseView,
    meta: {
      breadCrumb: [ { text: "Payment Response" } ],
      backNav: "payment-menu"
    }
  },
  {
    path: "/patch-response",
    name: "patch-response",
    component: PatchResponseView,
    meta: {
      breadCrumb: [ { text: "Patch Response" } ],
      backNav: ""
    }
  },
  {
    path: "/consent-menu",
    name: "consent-menu",
    component: ConsentMenu,
    meta: {
      breadCrumb: [ { text: "Consent Menu" } ],
    }
  },
  {
    path: "/consent-response-menu",
    name: "consent-response-menu",
    component: ConsentResponseMenu,
    meta: {
      breadCrumb: [ { text: "Consent Response Menu" } ],
      backNav: "consent-menu"
    }
  },
  {
    path: "/accounts",
    name: "accounts",
    component: AccountsMenu,
    meta: {
      breadCrumb: [ { text: "Accounts" } ],
      backNav: "consent-response-menu"
    }
  },
  {
    path: "/resources",
    name: "resources",
    component: ResourcesResponse,
    meta: {
      breadCrumb: [ { text: "Resources Response" } ],
      backNav: "consent-response-menu"
    }
  },
  {
    path: "/credit-card-accounts",
    name: "credit-card-accounts",
    component: CreditCardAccounts,
    meta: {
      breadCrumb: [ { text: "Credit Card Accounts" } ],
      backNav: "consent-response-menu"
    }
  },
  {
    path: "/customers",
    name: "customers",
    component: CustomersMenu,
    meta: {
      breadCrumb: [ { text: "Customers" } ],
      backNav: "consent-response-menu"
    }
  },
  {
    path: "/loans",
    name: "loans",
    component: LoansMenu,
    meta: {
      breadCrumb: [ { text: "Loans" } ],
      backNav: "consent-response-menu"
    }
  },
  {
    path: "/financings",
    name: "financings",
    component: FinancingsMenu,
    meta: {
      breadCrumb: [ { text: "Financings" } ],
      backNav: "consent-response-menu"
    }
  },
  {
    path: "/invoice-financings",
    name: "invoice-financings",
    component: InvoiceFinancings,
    meta: {
      breadCrumb: [ { text: "Invoice Financings" } ],
      backNav: "consent-response-menu"
    }
  },
  {
    path: "/unarranged-accounts-overdraft",
    name: "unarranged-accounts-overdraft",
    component: UnarrangedAccountsOverdraft,
    meta: {
      breadCrumb: [ { text: "Unarranged Accounts Overdraft" } ],
      backNav: "consent-response-menu"
    }
  },
  {
    path: "/consents",
    name: "consents",
    component: ConsentsApi,
    meta: {
      breadCrumb: [ { text: "Consents" } ],
      backNav: "consent-response-menu"
    }
  },
  {
    path: "*",
    name: "not-found",
    component: NotFound
  }
];

const router = new VueRouter({
  mode: "history",
  base: process.env.BASE_URL,
  routes,
});

export default router;
