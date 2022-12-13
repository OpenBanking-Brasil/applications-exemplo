<template>
  <CardWrapper title="Add Query Parameters">
    <template v-slot:card-content>
      <v-col cols="6" sm="12" md="6"> 
        <v-text-field label="Page Size" v-model="accountsQueryParams['page-size']" outlined dense>
        </v-text-field>
      </v-col>
      <v-col cols="6" sm="12" md="6">
        <v-text-field label="Page" v-model="accountsQueryParams['page']" outlined dense></v-text-field>
      </v-col>
      <v-col cols="12" sm="12" md="12" v-if="ApiVersion === 'v2'">
        <v-text-field label="Pagination Key" v-model="accountsQueryParams['pagination-key']" outlined dense>
        </v-text-field>
      </v-col>
      <v-col cols="12" sm="12" md="12">
        <v-select :items="['VIEW_DEPOSIT_ACCOUNT', 'SAVINGS_ACCOUNT', 'ACCOUNT_PAGAMENTO_PRE_PAGA']"
          label="Account Type" outlined dense v-model="accountsQueryParams['accountType']"></v-select>
      </v-col>
      <v-col cols="6" sm="12" md="3" class="mx-auto">
        <v-btn depressed height="2.5rem" width="100%" color="primary" @click="getAccountsByQueryParams">
          Run
        </v-btn>
      </v-col>
    </template>
    <template v-slot:content>
      <CardCode 
        class="mt-8" 
        color="lightblue" 
        title="Account API Request" 
        :code="accountsRequest"
        :is-error="isFetchError" />
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Account API Response" 
        :code="accountsResponse"
        :is-error="isFetchError" />
      <v-row class="mt-8">
        <v-col sm="12" md="6">
          <CardComponent 
            title="Account API" 
            :fullPath="`/open-banking/accounts/${ApiVersion}/accounts/{accountId}`"
            :resourceId="selectedAccountId" 
            :path="`${selectedAccountId}`"
            @fetch-data="fetchAccountData" 
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col sm="12" md="6">
          <CardComponent 
            title="Account Balances API"
            :fullPath="`/open-banking/accounts/${ApiVersion}/accounts/{accountId}/balances`"
            :resourceId="selectedAccountId" 
            :path="`${selectedAccountId}/balances`" 
            @fetch-data="fetchAccountData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col sm="12" md="6">
          <CardComponent 
            title="Account Transactions API"
            :fullPath="`/open-banking/accounts/${ApiVersion}/accounts/{accountId}/transactions`"
            :resourceId="selectedAccountId" 
            :path="`${selectedAccountId}/transactions`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="accountTransactionsQueryParams"
            flag="ACCOUNT_TRANSACTIONS" 
            @fetch-data="fetchAccountData" 
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col sm="12" md="6">
          <CardComponent 
            title="Account Overdraft Limits API"
            :fullPath="`/open-banking/accounts/${ApiVersion}/accounts/{accountId}/overdraft-limits`"
            :resourceId="selectedAccountId" 
            :path="`${selectedAccountId}/overdraft-limits`" 
            @fetch-data="fetchAccountData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col sm="12" md="6" v-if="ApiVersion === 'v2'">
          <CardComponent 
            title="Account Transactions Current API"
            :fullPath="`/open-banking/accounts/${ApiVersion}/accounts/{accountId}/transactions-current`"
            :resourceId="selectedAccountId" 
            :path="`${selectedAccountId}/transactions-current`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="accountTransactionsCurrentQueryParams"
            flag="ACCOUNT_TRANSACTIONS_CURRENT" 
            @fetch-data="fetchAccountData"
            @resource-id-change="changeResourceId" />
        </v-col>
      </v-row>
      <div class="pa-2"></div>
      <v-divider class="mt-5 mb-8"></v-divider>

      <v-card v-if="accountIDs.length" elevation="0" class="pa-0">
        <v-card-title class="px-0 pt-0 pb-5">Available Account IDs</v-card-title>
        <v-list dense max-height="20vh" style="overflow: auto">
          <v-list-item-group color="primary">
            <v-list-item v-for="(accountId, i) in accountIDs" :key="i" @click="setAccountId(accountId)">
              <v-list-item-content>
                <v-list-item-title v-text="accountId"></v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-card>

      <CardCode 
        class="mt-10" 
        color="lightblue" 
        title="Request" 
        :code="accountRequest" />
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Response" 
        :code="accountDataResponse" />
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
  name: "AccountsMenu",
  components: {
    SheetAppBar,
    CardComponent,
    CardCode,
    CardWrapper,
  },
  data() {
    return {
      getPathWithQueryParams,
      isFetchError: false,
      ApiVersion: "",
      accountsResponse: "",
      accountsRequest: "",
      accountRequest: "",
      accountIDs: [],
      selectedAccountId: "",
      accountDataResponse: "",
      secondaryResBannerStyle: "white--text cyan darken-4",
      accountsQueryParams: {
        "page-size": null,
        page: null,
        accountType: "",
        "pagination-key": null,
      },
      accountTransactionsCurrentQueryParams: {
        fromBookingDateMaxLimited: null,
        toBookingDateMaxLimited: null,
        "page-size": null,
        page: null,
        creditDebitIndicator: null,
        "pagination-key": null,
      },
      accountTransactionsQueryParams: {
        fromBookingDate: null,
        toBookingDate: null,
        "page-size": null,
        page: null,
        creditDebitIndicator: null,
      }
    };
  },
  computed: {
    ...mapGetters(["ApiOption"]),
  },
  created() {
    const optionWords = this.ApiOption.split("-");
    this.ApiVersion = optionWords[optionWords.length - 1];
    this.getAccounts();
  },
  methods: {
    ...mapActions(["setError", "setLoading"]),

    getAccountsByQueryParams() {
      this.accountIDs = [];
      const path = this.getPathWithQueryParams(this.accountsQueryParams);

      this.getAccounts(path);
    },

    async getAccounts(path = "") {
      try {
        this.setLoading(true);
        const response = await axios.get(`/accounts${path}`, { withCredentials: true });
        this.accountsResponse = response.data.responseData;
        this.accountsRequest = response.data.requestData;
        this.accountsResponse.data.forEach((account) => {
          this.accountIDs.push(account.accountId);
        });
        this.isFetchError = false;
        this.setLoading(false);
      } catch (error) {
        this.setError(error.message);
        this.isFetchError = true;
        this.accountsResponse = error.response.data.responseData;
        this.accountsRequest = error.response.data.requestData;
      }
    },

    setAccountId(accountId) {
      this.selectedAccountId = accountId;
    },

    async fetchAccountData(path) {
      try {
        this.setLoading(true);
        const response = await axios.get(`accounts/${path}`, { withCredentials: true });
        if (response.status === 200) {
          this.accountDataResponse = response.data.responseData;
          this.accountRequest = response.data.requestData;
          this.isFetchError = false;
        }
        this.setLoading(false);
      } catch (error) {
        this.setError(error.message);
        if (error.response.status !== 200) {
          this.isFetchError = true;
          this.accountDataResponse = error.response.data.responseData;
          this.accountRequest = error.response.data.requestData;
        }
      }
    },

    changeResourceId(accountId) {
      this.selectedAccountId = accountId;
    },
  },
};
</script>
