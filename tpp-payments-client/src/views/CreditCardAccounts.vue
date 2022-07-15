<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Credit Card Accounts" />

        <v-sheet min-height="70vh" rounded="lg">
          <v-container class="pa-md-12">
            <h3 class="mb-3 mt-5 grey--text text--darken-1">
              Add Query Parameters
            </h3>

            <v-row>
              <v-col :cols="ApiVersion === 'v2' ? 3 : 4" :md="ApiVersion === 'v2' ? 3 : 4">
                <v-text-field
                  label="Page Size"
                  v-model="creditCardAccountsQueryParams['page-size']"
                  outlined
                ></v-text-field>
              </v-col>
              <v-col :cols="ApiVersion === 'v2' ? 3 : 4" :md="ApiVersion === 'v2' ? 3 : 4">
                <v-text-field
                  label="Page"
                  outlined
                  v-model="creditCardAccountsQueryParams['page']"
                ></v-text-field>
              </v-col>
              <v-col :cols="ApiVersion === 'v2' ? 3 : 4" :md="ApiVersion === 'v2' ? 3 : 4" v-if="ApiVersion === 'v2'">
                <v-text-field
                  label="Pagination Key"
                  outlined
                  v-model="creditCardAccountsQueryParams['pagination-key']"
                ></v-text-field>
              </v-col>
              <v-col :cols="ApiVersion === 'v2' ? 3 : 4" :md="ApiVersion === 'v2' ? 3 : 4">
                <v-btn
                  depressed
                  height="3.4rem"
                  width="100%"
                  color="primary"
                  @click="getCreditCardAccountsByQueryParams"
                >
                  Run
                </v-btn>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text blue darken-4"
                    >Credit Card Accounts API Request</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                         {{ creditCardAccountsRequest }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title :class="primaryResBannerStyle"
                    >Credit Card Accounts API Response</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                         {{ creditCardAccountsResponse }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
            <v-row v-if="billIdSelected">
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Credit Card Bill Transactions API"
                  :fullPath="`/open-banking/credit-cards-accounts/${ApiVersion}/accounts/{creditCardAccountId}/bills/{billId}/transactions`"
                  :resourceId="selectedBillId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedCreditCardAccountId}/bills/${selectedBillId}/transactions`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams"
                  :queryParams="creditCardAccountTransactionsQueryParams"
                  flag="CREDIT_CARD_ACCOUNT_BILLS_TRANSACTIONS"
                  @fetch-data="fetchAccountData"
                  @resource-id-change="
                    (billId) => changeResourceId(billId, true)
                  "
                />
              </v-col>
            </v-row>
            <v-row v-else>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Credit Card Account API"
                  :fullPath="`/open-banking/credit-cards-accounts/${ApiVersion}/accounts/{creditCardAccountId}`"
                  :resourceId="selectedCreditCardAccountId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedCreditCardAccountId}`"
                  @fetch-data="fetchAccountData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Credit Card Account Limits API"
                  :fullPath="`/open-banking/credit-cards-accounts/${ApiVersion}/accounts/{creditCardAccountId}/limits`"
                  :resourceId="selectedCreditCardAccountId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedCreditCardAccountId}/limits`"
                  @fetch-data="fetchAccountData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Credit Card Account Transactions API"
                  :fullPath="`/open-banking/credit-cards-accounts/${ApiVersion}/accounts/{creditCardAccountId}/transactions`"
                  :resourceId="selectedCreditCardAccountId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedCreditCardAccountId}/transactions`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams"
                  :queryParams="creditCardAccountTransactionsQueryParams"
                  flag="CREDIT_CARD_ACCOUNT_TRANSACTIONS"
                  @fetch-data="fetchAccountData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3" v-if="ApiVersion === 'v2'">
                <CardComponent
                  title="Credit Card Account Transactions Current API"
                  :fullPath="`/open-banking/credit-cards-accounts/${ApiVersion}/accounts/{creditCardAccountId}/transactions-current`"
                  :resourceId="selectedCreditCardAccountId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedCreditCardAccountId}/transactions-current`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams"
                  :queryParams="creditCardAccountTransactionsCurrentQueryParams"
                  flag="CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT"
                  @fetch-data="fetchAccountData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Credit Card Account Bills API"
                  :fullPath="`/open-banking/credit-cards-accounts/${ApiVersion}/accounts/{creditCardAccountId}/bills`"
                  :resourceId="selectedCreditCardAccountId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedCreditCardAccountId}/bills`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams"
                  :queryParams="creditCardAccountBillsQueryParams"
                  flag="CREDIT_CARD_ACCOUNT_BILLS"
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
                    <v-list-item-group
                      color="primary"
                      v-model="selectedAccountIdIndex"
                    >
                      <v-list-item
                        v-for="(creditCardAccountId, i) in creditCardAccountIDs"
                        :key="i"
                        @click="
                          () => {
                            setAccountId(creditCardAccountId);
                          }
                        "
                      >
                        <v-list-item-content>
                          <v-list-item-title
                            v-text="creditCardAccountId"
                          ></v-list-item-title>
                        </v-list-item-content>
                      </v-list-item>
                    </v-list-item-group>
                  </v-list>
                </v-card>
                <br />
                <v-card
                  class="mx-auto"
                  max-width="300"
                  tile
                  v-if="billIDs.length > 0"
                >
                  <v-subheader>Available Bill IDs</v-subheader>
                  <v-list dense max-height="20vh" style="overflow: auto">
                    <v-list-item-group
                      color="primary"
                      v-model="selectedBillIdIndex"
                    >
                      <v-list-item
                        v-for="(billId, i) in billIDs"
                        :key="i"
                        @click="
                          () => {
                            setBillId(billId);
                          }
                        "
                      >
                        <v-list-item-content>
                          <v-list-item-title
                            v-text="billId"
                          ></v-list-item-title>
                        </v-list-item-content>
                      </v-list-item>
                    </v-list-item-group>
                  </v-list>
                </v-card>
              </v-col>
              <v-col cols="12" sm="8">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text blue darken-4"
                    >Request</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                        {{ creditCardAccountRequest }}
                    </pre>
                  </v-card-text>
                </v-card>
                <v-divider class="mt-5"></v-divider>
                <v-card elevation="2" outlined>
                  <v-card-title :class="secondaryResBannerStyle"
                    >Response</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                        {{ creditCardAccountResponse }}
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
  name: "CreditCardAccounts",
  components: {
    SheetAppBar,
    CardComponent,
    BackButton,
  },
  data() {
    return {
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
      primaryResBannerStyle: "white--text cyan darken-4",
      secondaryResBannerStyle: "white--text cyan darken-4",
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
    getPathWithQueryParams(queryParams) {
      let path = "";
      let isFirstIteration = true;
      for (let queryParam in queryParams) {
        if (queryParams[queryParam]) {
          if (!isFirstIteration) {
            path += `&${queryParam}=${queryParams[queryParam]}`;
          } else {
            isFirstIteration = false;
            path = `?${queryParam}=${queryParams[queryParam]}`;
          }
        }
      }

      return path;
    },

    getCreditCardAccountsByQueryParams() {
      this.creditCardAccountIDs = [];
      const path = this.getPathWithQueryParams(
        this.creditCardAccountsQueryParams
      );

      this.getCreditCardAccounts(path);
    },

    async getCreditCardAccounts(path = "") {

      try {
        const response = await axios.get(`/credit-cards-accounts${path}`, { withCredentials: true });
        this.creditCardAccountsResponse = response.data.responseData;
        this.creditCardAccountsRequest = response.data.requestData;
        this.creditCardAccountsResponse.data.forEach((creditCardAccount) => {
          this.creditCardAccountIDs.push(
            creditCardAccount.creditCardAccountId
          );
        });
        this.primaryResBannerStyle = "white--text cyan darken-4";
      } catch (error) {
          this.creditCardAccountsResponse = error.response.data.responseData;
          this.creditCardAccountsRequest = error.response.data.requestData;
          this.primaryResBannerStyle = "white--text red darken-1";
      }
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
      let response;
      try {
        response = await axios.get(`credit-cards-accounts/${path}`, {
          withCredentials: true,
        });
        if (response.status === 200) {
          this.creditCardAccountResponse = response.data.responseData;
          this.creditCardAccountRequest = response.data.requestData;
          this.secondaryResBannerStyle = "white--text cyan darken-4";

          if (path.match(`${this.selectedCreditCardAccountId}/bills/*`)) {
            response.data.responseData.data.forEach((bill) => {
              this.billIDs.push(bill.billId);
            });
          }
        }
      } catch (error) {
        if (error.response.status !== 200) {
          this.secondaryResBannerStyle = "white--text red darken-1";
          this.creditCardAccountResponse = error.response.data.responseData;
          this.creditCardAccountRequest = error.response.data.requestData;
        }
      }
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
