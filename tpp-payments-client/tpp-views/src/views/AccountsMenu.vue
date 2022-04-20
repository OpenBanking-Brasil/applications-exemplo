<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Accounts Menu" />

        <v-sheet min-height="70vh" rounded="lg">
          <v-container class="pa-md-12">
            <div class="pa-2"></div>
            <v-row>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title style="color: white; background-color: #004c50"
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
                  title="Accounts API (accounts/{resouceID}"
                  :accountId="selectedAccountId"
                  btnText="GO"
                  :path="`${selectedAccountId}`"
                  @fetch-account-data="fetchAccountData"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Account Overdraft Limits API (accounts/ {resouceID}/ overdraft-limits)"
                  :accountId="selectedAccountId"
                  btnText="GO"
                  :path="`${selectedAccountId}/overdraft-limits`"
                  @fetch-account-data="fetchAccountData"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Account Balances API (accounts/ {resouceID}/balances)"
                  :accountId="selectedAccountId"
                  btnText="GO"
                  :path="`${selectedAccountId}/balances`"
                  @fetch-account-data="fetchAccountData"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Account Transactions API (accounts/ {resouceID}/ transactions)"
                  :accountId="selectedAccountId"
                  btnText="GO"
                  :path="`${selectedAccountId}/transactions`"
                  @fetch-account-data="fetchAccountData"
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
                  <v-card-title style="color: white; background-color: #004c50"
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
      <v-col cols="12" sm="2"> </v-col>
    </v-row>
  </v-main>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import CardComponent from "@/components/GeneralAppComponents/CardComponent.vue";
import axios from "../util/axios.js";

export default {
  name: "AccountsMenu",
  components: {
    SheetAppBar,
    CardComponent,
  },
  data() {
    return {
      accountsResponse: "",
      accountIDs: [],
      selectedAccountId: "",
      accountDataResponse: ""
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
        this.accountDataResponse = response.data;
      });
    }
  }
};
</script>

