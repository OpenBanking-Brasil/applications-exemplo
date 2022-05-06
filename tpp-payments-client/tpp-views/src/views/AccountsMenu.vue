<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Accounts" />

        <v-sheet min-height="70vh" rounded="lg">
          <v-container class="pa-md-12">
            <div class="pa-2"></div>
            <v-row>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text cyan darken-4"
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
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Account Transactions API"
                  fullPath="/open-banking/accounts/v1/accounts/{accountId}/transactions"
                  :resourceId="selectedAccountId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedAccountId}/transactions`"
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
                      <v-list-item v-for="(accountId, i) in accountIDs" :key="i" @click="() => { setAccountId(accountId) }">
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
                  <v-card-title :class="resBannerStyle"
                    >Response</v-card-title
                  >
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
        <BackButton path="consent-response-menu"/>
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
  name: "AccountsMenu",
  components: {
    SheetAppBar,
    CardComponent,
    BackButton
  },
  data() {
    return {
      accountsResponse: "",
      accountIDs: [],
      selectedAccountId: "",
      accountDataResponse: "",
      resBannerStyle: "white--text cyan darken-4"
    };
  },
  created(){
      axios.get("/accounts", {withCredentials: true})
      .then((response) => {
        this.accountsResponse = response.data.data;
        this.accountsResponse.forEach(account => {
          this.accountIDs.push(account.accountId);
        });
      });
  },
  methods: {
    setAccountId(accountId){
      this.selectedAccountId = accountId;
    },

    fetchAccountData(path){
      axios.get(`accounts/${path}`, {withCredentials: true})
      .then((response) => {
        if(response.status === 200){
          this.accountDataResponse = response.data;
          this.resBannerStyle = "white--text cyan darken-4";
        }

      }).catch((error) => {
        if(error.response.status !== 200){
          this.resBannerStyle = "white--text red darken-1";
          this.accountDataResponse = error.response.data;
        }
      });
    },

    changeResourceId(accountId){
      this.selectedAccountId = accountId;
    }
  }
};
</script>

