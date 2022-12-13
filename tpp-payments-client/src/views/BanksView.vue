<template>
  <div class="banks layout-wrapper">
    <div>
      <v-row>
        <v-col cols="12">
          <v-text-field
            clearable
            flat
            class="banks__search"
            color="#338DAD"
            v-model="search"
          >
            <template v-slot:label>
              <v-icon>mdi-magnify</v-icon>
              <span>Please choose a bank</span>
            </template>
          </v-text-field>

          <v-skeleton-loader
            class="mx-auto"
            type="list-item-avatar, list-item-avatar, list-item-avatar, list-item-avatar"
            v-if="loadingBanks"
          ></v-skeleton-loader>

          <v-list-item-group
            v-else
          >
            <v-list-item
              v-for="(bank, i) in searchBank"
              :key="bank.id"
              class=""
              @click="selectBank(bank.title)"
            >
              <v-list-item-avatar width="95px">
                <v-img contain :src="bank.avatar" @error="onImgError(i)"></v-img>
              </v-list-item-avatar>

              <v-list-item-content>
                <v-list-item-title
                  class="text-h5"
                  v-text="bank.title"
                ></v-list-item-title>
              </v-list-item-content>

              <v-list-item-action>
                  <v-checkbox
                    :value="selectedBank === bank.title"
                    color="#007199"
                    on-icon="mdi-circle-slice-8"
                    off-icon="mdi-circle-outline"
                  >
                  </v-checkbox>
                </v-list-item-action>
            </v-list-item>
          </v-list-item-group>
        </v-col>
      </v-row>

      <v-dialog
        v-model="dialog"
        transition="dialog-bottom-transition"
        content-class="dialog-content"
        persistent
        max-width="600px"
      >
        <template v-slot:activator="{ on, attrs }">
          <v-row class="layout-wrapper__bottom-btns mx-0">
            <v-col class="pa-0">
              <v-btn
                :disabled="selectedBank ? false : true"
                depressed
                block
                height="57"
                v-bind="attrs"
                v-on="on"
                class="banks__next-btn"
                @click="getClients"
              >
                <span>Next</span>
                <v-icon>mdi-chevron-right</v-icon>
              </v-btn>
            </v-col>
          </v-row>
        </template>

        <v-card>
          <v-toolbar height="46" dark>
            <span>Client Options</span>

            <v-btn
              icon
              height="36"
              width="36"
              class="mr-0"
              @click="dialog = false"
            >
              <v-icon small>mdi-close</v-icon>
            </v-btn>
          </v-toolbar >

          <v-card-text class="pt-8">
            <v-form ref="form" v-model="valid" lazy-validation>
              <v-row>
                <v-col class="d-flex" cols="12" sm="12">
                  <v-select
                    :items="dcrOptions"
                    label="Client Options"
                    outlined
                    color="#338DAD"
                    v-model="selectedDcrOption"
                  ></v-select>
                </v-col>

                <template
                  v-if="selectedDcrOption === this.dcrOptions[1]"
                >
                  <v-col
                    class="d-flex"
                    cols="12"
                    sm="12"
                  >
                    <v-select
                      :items="clientIds"
                      label="Client Ids"
                      outlined
                      v-model="selectedClientId"
                    ></v-select>
                  </v-col>

                  <v-col cols="12" sm="6">
                    <v-text-field
                      outlined
                      label="Client ID"
                      v-model="clientId"
                      required
                      :rules="clientIdRules"
                    ></v-text-field>
                  </v-col>

                  <v-col cols="12" sm="6">
                    <v-text-field
                      outlined
                      label="Registration Access Token"
                      v-model="registrationAccessToken"
                      required
                      :rules="registrationAccessTokenRules"
                    ></v-text-field>
                  </v-col>
                </template>
              </v-row>
            </v-form>
          </v-card-text>

          <v-card-actions>
            <v-row class="layout-wrapper__bottom-btns">
              <v-col class="col-6 pa-0">
                <v-btn depressed block text height="57" @click="dialog = false">Close</v-btn>
              </v-col>

              <v-col class="col-6 pa-0">
                <v-btn
                  depressed
                  block
                  text
                  height="57"
                  @click="confirmSelectedBank"
                >
                  Save
                </v-btn>
              </v-col>
            </v-row>
          </v-card-actions>
        </v-card>
      </v-dialog>
    </div>
  </div>
</template>

<script>
import axios from "@/util/axios.js";
import { v1 as uuid } from "uuid";
import { mapGetters, mapActions } from "vuex";

export default {
  name: "BankView",

  data: () => ({
    dcrOptions: [
      "Dynamically Register A New Client",
      "Provide An Existing Client Configuration",
    ],
    selectedDcrOption: "Dynamically Register A New Client",
    clientIdRules: [(v) => !!v || "client ID is required"],
    registrationAccessTokenRules: [
      (v) => !!v || "Registration Access Token is required",
    ],
    valid: true,
    dialog: false,
    text: "Please select a bank",
    selectedBank: "",
    banks: [],
    search: "",
    loadingBanks: true,
    clients: [],
    clientIds: [],
    selectedClientId: "",
    selectedOption: "",
    cancelRequests: false,
    ApiVersion: "v1",
    defaultOption: "customer-data-v1",
  }),
  methods: {
    ...mapActions(["setScopes", "setApiOption", "setError", "setLoading"]),

    selectBank(bankTitle) {
      if (this.selectedBank === bankTitle) {
        this.selectedBank = "";
      } else {
        this.selectedBank = bankTitle;
      }
    },
    async confirmSelectedBank() {
      if (
        this.selectedDcrOption === this.dcrOptions[1] &&
        (!this.clientId || !this.registrationAccessToken)
      ) {
        this.$refs.form.validate();
        return;
      }
      this.dialog = false;
      axios.defaults.withCredentials = true;

      try {
        this.setLoading(true);
        const res = await axios.post(
          "/dcr",
          {
            bank: this.selectedBank,
            selectedDcrOption: this.dcrOption,
            clientId: this.clientId,
            registrationAccessToken: this.registrationAccessToken,
          },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );
        this.clientId = res.data.clientId;
        this.registrationAccessToken = res.data.registrationAccessToken;
        this.setScopes(res.data.scope);
        if (this.selectedOption === "payments") {
          this.$router.push({
            name: "payment-menu",
            params: {
              data: {
                selectedDcrOption: this.dcrOption,
                selectedBank: this.selectedBank,
                clientId: res.data.clientId,
              },
            },
          });
        } else {
          this.$router.push({
            name: "consent-menu",
            params: {
              data: {
                selectedDcrOption: this.dcrOption,
              },
            },
          });
        }
        this.setLoading(false);
      } catch (error) {
        this.setError(error.response.statusText);
      }
    },

    async getBanks(data) {
      for (let i = 0; i < data.length; i++) {

        let filteredAuthServers;
        if(this.ApiVersion === "v2"){
          filteredAuthServers = data[i].AuthorisationServers.filter((as) => {
            return as.ApiResources.some((resource) => {
                return resource.ApiDiscoveryEndpoints.some((endpointObj) => {
                  return endpointObj.ApiEndpoint.includes(this.ApiVersion);
                });
            });
          });
        } else {
          filteredAuthServers = data[i].AuthorisationServers;
        }
        
        if (filteredAuthServers) {
          for (let y = 0; y < filteredAuthServers.length; y++) {
            this.banks.push({
              id: uuid(),
              avatar: filteredAuthServers[y].CustomerFriendlyLogoUri,
              title: filteredAuthServers[y].CustomerFriendlyName,
            });
          }
        }
      }
      this.banks.sort((a, b) => a.title.trim().localeCompare(b.title.trim()));
      for (let i = 0; i < this.banks.length; i++) {
        if(this.cancelRequests){
          break;
        }
        try {
          await fetch(this.banks[i].avatar, { mode: "no-cors" });
        } catch (error) {
          this.banks[i].avatar = "https://ui-avatars.com/api/?name=No+Logo";
        }
      }
    },

    onImgError(i){
      this.banks[i].avatar = "https://ui-avatars.com/api/?name=No+Logo";
    },

    async getClients() {
      try {
        const response = await axios.get("/clients", { withCredentials: true });
        this.clients = response.data;
        this.clients.forEach((client) => {
          if (client.bank === this.selectedBank) {
            this.clientIds.push(client.clientId);
          }
        });
      } catch (error){
        this.setError(`Cannot get clients: error ${error.response.status}`);
      }
    },
  },

  computed: {
    dcrOption() {
      if (this.selectedDcrOption === "Dynamically Register A New Client") {
        return "REGISTER_NEW_CLIENT";
      } else {
        return "USE_EXISTING_CLIENT";
      }
    },

    headerText() {
      if (this.selectedOption === "payments") {
        return "Payment Providers Details";
      } else {
        return "Authorisation Servers List";
      }
    },
    searchBank() {
      return this.search ? this.banks.filter((bank) => {
          return bank.title.toLowerCase().includes(this.search.toLowerCase());
        })
      : this.banks;
    },

    clientId: {
      get() {
        return this.$store.state.mockTPP.clientID;
      },
      set(clientId) {
        this.$store.commit("setClientID", clientId);
      },
    },

    registrationAccessToken: {
      get() {
        return this.$store.state.mockTPP.registrationAccessToken;
      },
      set(registrationAccessToken) {
        this.$store.commit(
          "setRegistrationAccessToken",
          registrationAccessToken
        );
      },
    },

    ...mapGetters(["clientID"]),
  },

  watch: {
    selectedClientId(clientId) {
      this.clients.forEach((client) => {
        if (clientId === client.clientId) {
          this.clientId = client.clientId;
          this.registrationAccessToken = client.registrationAccessToken;
        }
      });
    },
  },

  beforeDestroy(){
    this.cancelRequests = true;
  },

  async created() {
    this.clientId = "";
    this.registrationAccessToken = "";

    this.selectedOption = this.$route.query.option || 
      this.$router.push({ path: "banks", query: { option: this.defaultOption }}) && this.defaultOption;

    this.setApiOption(this.selectedOption);

    const optionWords = this.selectedOption.split("-");
    this.ApiVersion = optionWords[optionWords.length - 1];

    try {
      this.setLoading(true);
      const response = await axios.get(`/banks/${this.selectedOption}`, { withCredentials: true });
      this.loadingBanks = false;
      this.getBanks(response.data);
      this.setLoading(false);
    } catch (error){
      this.setError(error.message);
    }
  },
};
</script>
