<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Accounts" />

        <v-sheet min-height="70vh" rounded="lg">
          <v-container class="pa-md-12">
            <h3 class="mb-3 mt-5 grey--text text--darken-1">
              Add Query Parameters
            </h3>

            <v-row>
              <v-col cols="3" md="3">
                <v-text-field
                  label="Page Size"
                  placeholder="Page Size"
                  v-model="accountsQueryParams['page-size']"
                  outlined
                ></v-text-field>
              </v-col>
              <v-col cols="3" md="3">
                <v-text-field
                  label="Page"
                  placeholder="Page"
                  outlined
                  v-model="accountsQueryParams['page']"
                ></v-text-field>
              </v-col>
              <v-col cols="3" md="3">
                <v-select
                  :items="['VIEW_DEPOSIT_ACCOUNT', 'SAVINGS_ACCOUNT', 'ACCOUNT_PAGAMENTO_PRE_PAGA']"
                  label="Account Type"
                  outlined
                  v-model="accountsQueryParams['accountType']"
                ></v-select>
              </v-col>
              <v-col cols="3" md="3">
                <v-btn
                  depressed
                  height="3.4rem"
                  width="100%"
                  color="primary"
                  @click="getAccountsByQueryParams"
                >
                  Run
                </v-btn>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text blue darken-4"
                    >Account API Request</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                         {{ accountsRequest }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title :class="primaryResBannerStyle"
                    >Account API Response</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                         {{ accountsResponse }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Account API"
                  fullPath="/open-banking/accounts/v1/accounts/{accountId}"
                  :resourceId="selectedAccountId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedAccountId}`"
                  @fetch-data="fetchAccountData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Account Overdraft Limits API"
                  fullPath="/open-banking/accounts/v1/accounts/{accountId}/overdraft-limits"
                  :resourceId="selectedAccountId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedAccountId}/overdraft-limits`"
                  @fetch-data="fetchAccountData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Account Balances API"
                  fullPath="/open-banking/accounts/v1/accounts/{accountId}/balances"
                  :resourceId="selectedAccountId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedAccountId}/balances`"
                  @fetch-data="fetchAccountData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3" v-if="ApiVersion === 'v2'">
                <CardComponent
                  title="Account Transactions Current API"
                  fullPath="/open-banking/accounts/v1/accounts/{accountId}/transactions-current"
                  :resourceId="selectedAccountId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedAccountId}/transactions-current`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="accountTransactionsCurrentQueryParams"
                  flag="ACCOUNT_TRANSACTIONS_CURRENT"
                  @fetch-data="fetchAccountData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Account Transactions API"
                  fullPath="/open-banking/accounts/v1/accounts/{accountId}/transactions"
                  :resourceId="selectedAccountId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedAccountId}/transactions`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="accountTransactionsQueryParams"
                  flag="ACCOUNT_TRANSACTIONS"
                  @fetch-data="fetchAccountData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
            </v-row>
            <div class="pa-2"></div>
            <v-divider class="mt-5 mb-8"></v-divider>
            <v-row>
              <v-col cols="12" sm="4">
                <v-card class="mx-auto" max-width="300" tile>
                  <v-subheader>Available Account IDs</v-subheader>
                  <v-list dense max-height="20vh" style="overflow: auto">
                    <v-list-item-group color="primary">
                      <v-list-item
                        v-for="(accountId, i) in accountIDs"
                        :key="i"
                        @click="
                          () => {
                            setAccountId(accountId);
                          }
                        "
                      >
                        <v-list-item-content>
                          <v-list-item-title
                            v-text="accountId"
                          ></v-list-item-title>
                        </v-list-item-content>
                      </v-list-item>
                    </v-list-item-group>
                  </v-list>
                </v-card>
              </v-col>
              <v-col cols="12" sm="8">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text blue darken-4">Request</v-card-title>
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                        {{ accountRequest }}
                    </pre>
                  </v-card-text>
                </v-card>
                 <v-divider class="mt-4"></v-divider>
                <v-card elevation="2" outlined>
                  <v-card-title :class="secondaryResBannerStyle">Response</v-card-title>
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                        {{ accountDataResponse }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
          </v-container>
        </v-sheet>
      </v-col>
      <v-col cols="12" sm="2">
        <BackButton path="consent-response-menu" />
      </v-col>
    </v-row>
  </v-main>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import CardComponent from "@/components/GeneralAppComponents/CardComponent.vue";
import BackButton from "@/components/GeneralAppComponents/BackButton.vue";
import axios from "../util/axios.js";
import { mapGetters } from "vuex";

export default {
  name: "AccountsMenu",
  components: {
    SheetAppBar,
    CardComponent,
    BackButton,
  },
  data() {
    return {
      ApiVersion: "",
      accountsResponse: "",
      accountsRequest: "",
      accountRequest: "",
      accountIDs: [],
      selectedAccountId: "",
      accountDataResponse: "",
      primaryResBannerStyle: "white--text cyan darken-4",
      secondaryResBannerStyle: "white--text cyan darken-4",
      accountsQueryParams: {
        "page-size": null,
        page: null,
        accountType: ""
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

    getPathWithQueryParams(accountsQueryParams){
      let path = "";
      let isFirstIteration = true;
      for(let queryParam in accountsQueryParams){
        if(accountsQueryParams[queryParam]){
          if(!isFirstIteration){
            path += `&${queryParam}=${accountsQueryParams[queryParam]}`;
          } else {
            isFirstIteration = false;
            path = `?${queryParam}=${accountsQueryParams[queryParam]}`;
          }
        }
      }

      return path;
    },

    getAccountsByQueryParams(){
      this.accountIDs = [];
      const path = this.getPathWithQueryParams(this.accountsQueryParams);

      this.getAccounts(path);
    },

    async getAccounts(path=""){
      
      try {
        const response = await axios.get(`/accounts${path}`, { withCredentials: true });
        this.accountsResponse = response.data.responseData;
        this.accountsRequest = response.data.requestData;
        this.accountsResponse.data.forEach((account) => {
          this.accountIDs.push(account.accountId);
        });
        this.primaryResBannerStyle = "white--text cyan darken-4";
      } catch (error){
        this.accountsResponse = error.response.data.responseData;
        this.accountsRequest = error.response.data.requestData;
        this.primaryResBannerStyle = "white--text red darken-1";
      }
    },

    setAccountId(accountId) {
      this.selectedAccountId = accountId;
    },

    async fetchAccountData(path) {
      try {
        const response = await axios.get(`accounts/${path}`, { withCredentials: true });
        if (response.status === 200) {
          this.accountDataResponse = response.data.responseData;
          this.accountRequest = response.data.requestData;
          this.secondaryResBannerStyle = "white--text cyan darken-4";
        }
      } catch(error) {
        if (error.response.status !== 200) {
          this.secondaryResBannerStyle = "white--text red darken-1";
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
