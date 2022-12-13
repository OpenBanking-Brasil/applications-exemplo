<template>
  <CardWrapper title="Add Query Parameters">
    <template v-slot:card-content>
      <v-col cols="6" sm="12" md="6">
        <v-text-field label="Page Size" v-model="creditCardAccountsQueryParams['page-size']" outlined dense>
        </v-text-field>
      </v-col>
      <v-col cols="6" sm="12" md="6">
        <v-text-field label="Page" outlined v-model="creditCardAccountsQueryParams['page']" dense>
        </v-text-field>
      </v-col>
      <v-col cols="12" sm="12" md="12" v-if="ApiVersion === 'v2'">
        <v-text-field label="Pagination Key" outlined v-model="creditCardAccountsQueryParams['pagination-key']"
          dense>
        </v-text-field>
      </v-col>

      <v-col cols="6" sm="12" md="3" class="mx-auto">
        <v-btn depressed height="2.5rem" width="100%" color="primary"
          @click="getCreditCardAccountsByQueryParams">
          Run
        </v-btn>
      </v-col>
    </template>
    <template v-slot:content>
      <CardCode 
        class="mt-8" 
        color="lightblue" 
        title="Credit Card Accounts API Request"
        :code="creditCardAccountsRequest" 
        :is-error="isFetchCreditCardAccountsError" />
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Credit Card Accounts API Response"
        :code="creditCardAccountsResponse" 
        :is-error="isFetchCreditCardAccountsError" />

      <v-row class="mt-8" v-if="billIdSelected">
        <v-col cols="12">
          <CardComponent 
            title="Credit Card Bill Transactions API"
            :fullPath="`/open-banking/credit-cards-accounts/${ApiVersion}/accounts/{creditCardAccountId}/bills/{billId}/transactions`"
            :resourceId="selectedBillId" 
            :path="`${selectedCreditCardAccountId}/bills/${selectedBillId}/transactions`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="creditCardAccountTransactionsQueryParams"
            flag="CREDIT_CARD_ACCOUNT_BILLS_TRANSACTIONS" 
            @fetch-data="fetchAccountData" 
            @resource-id-change="
              (billId) => changeResourceId(billId, true)
            " />
        </v-col>
      </v-row>
      <v-row class="mt-8" v-else>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Credit Card Account API"
            :fullPath="`/open-banking/credit-cards-accounts/${ApiVersion}/accounts/{creditCardAccountId}`"
            :resourceId="selectedCreditCardAccountId" 
            :path="`${selectedCreditCardAccountId}`" 
            @fetch-data="fetchAccountData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Credit Card Account Limits API"
            :fullPath="`/open-banking/credit-cards-accounts/${ApiVersion}/accounts/{creditCardAccountId}/limits`"
            :resourceId="selectedCreditCardAccountId" 
            :path="`${selectedCreditCardAccountId}/limits`" 
            @fetch-data="fetchAccountData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Credit Card Account Transactions API"
            :fullPath="`/open-banking/credit-cards-accounts/${ApiVersion}/accounts/{creditCardAccountId}/transactions`"
            :resourceId="selectedCreditCardAccountId" 
            :path="`${selectedCreditCardAccountId}/transactions`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="creditCardAccountTransactionsQueryParams"
            flag="CREDIT_CARD_ACCOUNT_TRANSACTIONS" 
            @fetch-data="fetchAccountData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col v-if="ApiVersion === 'v2'" cols="12" md="6">
          <CardComponent 
            title="Credit Card Account Transactions Current API"
            :fullPath="`/open-banking/credit-cards-accounts/${ApiVersion}/accounts/{creditCardAccountId}/transactions-current`"
            :resourceId="selectedCreditCardAccountId" 
            :path="`${selectedCreditCardAccountId}/transactions-current`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams"
            :queryParams="creditCardAccountTransactionsCurrentQueryParams"
            flag="CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT" 
            @fetch-data="fetchAccountData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Credit Card Account Bills API"
            :fullPath="`/open-banking/credit-cards-accounts/${ApiVersion}/accounts/{creditCardAccountId}/bills`"
            :resourceId="selectedCreditCardAccountId" 
            :path="`${selectedCreditCardAccountId}/bills`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="creditCardAccountBillsQueryParams"
            flag="CREDIT_CARD_ACCOUNT_BILLS" 
            @fetch-data="fetchAccountData" 
            @resource-id-change="changeResourceId" />
        </v-col>
      </v-row>
      <div class="pa-2"></div>
      <v-divider class="mt-5 mb-8"></v-divider>

      <v-card v-if="creditCardAccountIDs.length" elevation="0" class="pa-0">
        <v-card-title class="px-0 pt-0 pb-5">Available Account IDs</v-card-title>
        <v-list dense max-height="20vh" style="overflow: auto">
          <v-list-item-group color="primary" v-model="selectedAccountIdIndex">
            <v-list-item 
              v-for="(creditCardAccountId, i) in creditCardAccountIDs" :key="i" @click="setAccountId(creditCardAccountId)">
              <v-list-item-content>
                <v-list-item-title v-text="creditCardAccountId"></v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-card>
      <v-card v-if="billIDs.length" elevation="0" class="pa-0">
        <v-card-title class="px-0 pt-0 pb-5">Available Bill IDs</v-card-title>
        <v-list dense max-height="20vh" style="overflow: auto">
          <v-list-item-group color="primary" v-model="selectedBillIdIndex">
            <v-list-item v-for="(billId, i) in billIDs" :key="i" @click="setBillId(billId)">
              <v-list-item-content>
                <v-list-item-title v-text="billId"></v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-card>

      <CardCode 
        class="mt-10" 
        color="lightblue" 
        title="Request" 
        :code="creditCardAccountRequest" />
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Response" 
        :code="creditCardAccountResponse" />
    </template>
  </CardWrapper>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import CardComponent from "@/components/Shared/CardComponent.vue";
import CardCode from "@/components/Shared/CardCode.vue";
import CardWrapper from "@/components/Shared/CardWrapper.vue";

import axios from "@/util/axios.js";
import { getPathWithQueryParams } from "@/util/helpers.js";

import { mapGetters, mapActions } from "vuex";

export default {
  name: "CreditCardAccounts",
  components: {
    SheetAppBar,
    CardComponent,
    CardCode,
    CardWrapper,
  },
  data() {
    return {
      getPathWithQueryParams,
      selectedAccountIdIndex: null,
      selectedBillIdIndex: null,
      acccountIdSelected: false,
      billIdSelected: false,
      creditCardAccountsResponse: "",
      creditCardAccountsRequest: "",
      creditCardAccountRequest: "",
      creditCardAccountIDs: [],
      selectedCreditCardAccountId: "",
      selectedBillId: "",
      creditCardAccountResponse: "",
      billIDs: [],
      isFetchCreditCardAccountsError: false,
      isFetchAccountDataError: false,
      creditCardAccountsQueryParams: {
        "page-size": null,
        page: null,
        "pagination-key": null,
      },
      creditCardAccountTransactionsCurrentQueryParams: {
        fromTransactionDateMaxLimited: null,
        toTransactionDateMaxLimited: null,
        "page-size": null,
        page: null,
        creditCardTransactionType: null,
        creditCardPayeeMCC: null,
        "pagination-key": null,
      },
      creditCardAccountTransactionsQueryParams: {
        fromTransactionDate: null,
        toTransactionDate: null,
        "page-size": null,
        page: null,
        transactionType: null,
        payeeMCC: null,
      },
      creditCardAccountBillsQueryParams: {
        fromDueDate: null,
        toDueDate: null,
        "page-size": null,
        page: null,
      },
    };
  },
  created() {
    const optionWords = this.ApiOption.split("-");
    this.ApiVersion = optionWords[optionWords.length - 1];
    this.getCreditCardAccounts();
  },
  computed: {
    ...mapGetters(["ApiOption"]),
  },
  methods: {
    ...mapActions(["setError", "setLoading"]),

    getCreditCardAccountsByQueryParams() {
      this.creditCardAccountIDs = [];
      const path = this.getPathWithQueryParams(
        this.creditCardAccountsQueryParams
      );

      this.getCreditCardAccounts(path);
    },

    async getCreditCardAccounts(path = "") {
      this.setLoading(true);
      try {
        const response = await axios.get(`/credit-cards-accounts${path}`, { withCredentials: true });
        this.creditCardAccountsResponse = response.data.responseData;
        this.creditCardAccountsRequest = response.data.requestData;
        this.creditCardAccountsResponse.data.forEach((creditCardAccount) => {
          this.creditCardAccountIDs.push(
            creditCardAccount.creditCardAccountId
          );
        });
        this.isFetchCreditCardAccountsError = false;
      } catch (error) {
        this.setError(error.message);
        this.isFetchCreditCardAccountsError = true;
        this.creditCardAccountsResponse = error.response.data.responseData;
        this.creditCardAccountsRequest = error.response.data.requestData;
      }
      this.setLoading(false);
    },
    setAccountId(creditCardAccountId) {
      this.creditCardAccountResponse = "";
      this.selectedCreditCardAccountId = creditCardAccountId;
      this.acccountIdSelected = true;
      this.billIdSelected = false;
      this.selectedBillIdIndex = null;
      this.billIDs = [];
    },

    setBillId(selectedBillId) {
      this.creditCardAccountResponse = "";
      this.selectedBillId = selectedBillId;
      this.acccountIdSelected = false;
      this.billIdSelected = true;
      this.selectedAccountIdIndex = null;
    },

    async fetchAccountData(path) {
      this.setLoading(true);
      let response;
      try {
        response = await axios.get(`credit-cards-accounts/${path}`, {
          withCredentials: true,
        });
        if (response.status === 200) {
          this.creditCardAccountResponse = response.data.responseData;
          this.creditCardAccountRequest = response.data.requestData;

          this.isFetchAccountDataError = false;
          if (path.match(`${this.selectedCreditCardAccountId}/bills/*`)) {
            response.data.responseData.data.forEach((bill) => {
              this.billIDs.push(bill.billId);
            });
          }
        }
      } catch (error) {
        if (error.response.status !== 200) {
          this.isFetchAccountDataError = true;
          this.setError(error.message);
          this.creditCardAccountResponse = error.response.data.responseData;
          this.creditCardAccountRequest = error.response.data.requestData;
        }
      }
      this.setLoading(false);
    },

    changeResourceId(resourceId, billdIdProvided = false) {
      if (billdIdProvided) {
        this.selectedBillId = resourceId;
      } else {
        this.selectedCreditCardAccountId = resourceId;
      }
    },
  },
};
</script>
