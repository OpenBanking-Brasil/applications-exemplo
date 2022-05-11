<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Credit Card Accounts" />

        <v-sheet min-height="70vh" rounded="lg">
          <v-container class="pa-md-12">
            <div class="pa-2"></div>
            <v-row>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text cyan darken-4"
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
                  fullPath="/open-banking/credit-cards-accounts/v1/accounts/{creditCardAccountId}/bills/{billId}/transactions"
                  :resourceId="selectedBillId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedCreditCardAccountId}/bills/${selectedBillId}/transactions`"
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
                  fullPath="/open-banking/credit-cards-accounts/v1/accounts/{creditCardAccountId}"
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
                  fullPath="/open-banking/credit-cards-accounts/v1/accounts/{creditCardAccountId}/limits"
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
                  fullPath="/open-banking/credit-cards-accounts/v1/accounts/{creditCardAccountId}/transactions"
                  :resourceId="selectedCreditCardAccountId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedCreditCardAccountId}/transactions`"
                  @fetch-data="fetchAccountData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Credit Card Account Bills API"
                  fullPath="/open-banking/credit-cards-accounts/v1/accounts/{creditCardAccountId}/bills"
                  :resourceId="selectedCreditCardAccountId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedCreditCardAccountId}/bills`"
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
                  <v-card-title :class="resBannerStyle">Response</v-card-title>
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
      creditCardAccountIDs: [],
      selectedCreditCardAccountId: "",
      selectedBillId: "",
      creditCardAccountResponse: "",
      billIDs: [],
      resBannerStyle: "white--text cyan darken-4",
    };
  },
  created() {
    axios
      .get("/credit-cards-accounts", { withCredentials: true })
      .then((response) => {
        this.creditCardAccountsResponse = response.data.data;
        this.creditCardAccountsResponse.forEach((creditCardAccount) => {
          this.creditCardAccountIDs.push(creditCardAccount.creditCardAccountId);
        });
      });
  },
  methods: {
    setAccountId(creditCardAccountId) {
      console.log("hello", creditCardAccountId);
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

    fetchAccountData(path) {
      axios
        .get(`credit-cards-accounts/${path}`, { withCredentials: true })
        .then((response) => {
          if (response.status === 200) {
            this.creditCardAccountResponse = response.data;
            this.resBannerStyle = "white--text cyan darken-4";

            if (path === `${this.selectedCreditCardAccountId}/bills`) {
              response.data.data.forEach((bill) => {
                this.billIDs.push(bill.billId);
              });
            }
          }
        })
        .catch((error) => {
          if (error.response.status !== 200) {
            this.resBannerStyle = "white--text red darken-1";
            this.creditCardAccountResponse = error.response.data;
          }
        });
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
