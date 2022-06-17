<template>
  <v-main class="banks">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <v-sheet min-height="70vh" elevation="20" rounded="lg">
          <SheetAppBar header="Mock TPP" />
          <v-row>
            <v-col> </v-col>
            <v-col cols="10">
              <div class="pa-md-4 transition-swing text-h6" align="center">
                {{ headerText }}
              </div>
              <v-text-field
                label="Search"
                outlined
                clearable
                v-model="search"
              ></v-text-field>

              <v-skeleton-loader
                class="mx-auto"
                type="list-item-avatar, list-item-avatar, list-item-avatar, list-item-avatar"
                v-if="loadingBanks"
              ></v-skeleton-loader>

              <v-card class="mx-auto" else>
                <v-list-item-group
                  style="max-height: 300px"
                  class="overflow-y-auto"
                >
                  <v-list-item
                    v-for="bank in searchBank"
                    :key="bank.id"
                    class="pa-md-4"
                    @click="selectBank(bank.title)"
                  >
                    <v-list-item-avatar>
                      <v-img contain :src="bank.avatar"></v-img>
                    </v-list-item-avatar>

                    <v-list-item-content>
                      <v-list-item-title
                        class="text-h5"
                        v-text="bank.title"
                      ></v-list-item-title>
                    </v-list-item-content>
                  </v-list-item>
                </v-list-item-group>
              </v-card>
            </v-col>
            <v-col> </v-col>
          </v-row>

          <v-row justify="center" class="mt-12">
            <v-dialog v-model="dialog" persistent max-width="600px">
              <template v-slot:activator="{ on, attrs }">
                <v-btn
                  :disabled="selectedBank ? false : true"
                  color="primary"
                  v-bind="attrs"
                  x-large
                  v-on="on"
                  @click="getClients"
                  class="mb-10"
                >
                  Continue
                </v-btn>
              </template>
              <v-card>
                <v-card-title>
                  <span class="text-h5">Client Options</span>
                </v-card-title>
                <v-card-text>
                  <v-container>
                    <v-form ref="form" v-model="valid" lazy-validation>
                      <v-row>
                        <v-col class="d-flex" cols="12" sm="12">
                          <v-select
                            :items="dcrOptions"
                            label="Client Options"
                            dense
                            outlined
                            v-model="selectedDcrOption"
                          ></v-select>
                        </v-col>

                        <v-col
                          class="d-flex"
                          cols="12"
                          sm="12"
                          v-if="selectedDcrOption === this.dcrOptions[1]"
                        >
                          <v-select
                            :items="clientIds"
                            label="Client Ids"
                            dense
                            outlined
                            v-model="selectedClientId"
                          ></v-select>
                        </v-col>

                        <template
                          v-if="selectedDcrOption === this.dcrOptions[1]"
                        >
                          <v-col cols="12" sm="6">
                            <v-text-field
                              dense
                              outlined
                              label="Client ID"
                              v-model="clientId"
                              required
                              :rules="clientIdRules"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <v-text-field
                              dense
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
                  </v-container>
                </v-card-text>
                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn color="blue darken-1" text @click="dialog = false">
                    Close
                  </v-btn>
                  <v-btn
                    color="blue darken-1"
                    text
                    @click="confirmSelectedBank"
                  >
                    Save
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-row>
        </v-sheet>
      </v-col>
      <v-col cols="12" sm="2">
        <BackButton path="/" />
      </v-col>
    </v-row>
    <v-overlay :value="loading">
      <v-progress-circular indeterminate size="100"></v-progress-circular>
    </v-overlay>
    <v-snackbar v-model="snackbar" :multi-line="multiLine" color="red accent-2">
      {{ text }}

      <template v-slot:action="{ attrs }">
        <v-btn color="white" text v-bind="attrs" @click="snackbar = false">
          Close
        </v-btn>
      </template>
    </v-snackbar>
  </v-main>
</template>

<script>
// @ is an alias to /src

import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import BackButton from "@/components/GeneralAppComponents/BackButton.vue";
import axios from "../util/axios.js";
import { v1 as uuid } from "uuid";
import { mapGetters, mapActions } from "vuex";

export default {
  name: "BankView",
  components: {
    SheetAppBar,
    BackButton,
  },
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
    multiLine: true,
    snackbar: false,
    text: "Please select a bank",
    loading: false,
    selectedBank: "",
    banks: [],
    search: "",
    loadingBanks: true,
    clients: [],
    clientIds: [],
    selectedClientId: "",
    selectedOption: "",
  }),
  methods: {
    ...mapActions(["setScopes"]),
    selectBank(bankTitle) {
      this.selectedBank = bankTitle;
    },
    confirmSelectedBank() {
      if (
        this.selectedDcrOption === this.dcrOptions[1] &&
        (!this.clientId || !this.registrationAccessToken)
      ) {
        this.$refs.form.validate();
        return;
      }
      this.dialog = false;
      axios.defaults.withCredentials = true;
      this.loading = true;
      axios
        .post(
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
        )
        .then((res) => {
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
        })
        .catch((error) => {
          console.log(error);
          this.text = error.response.statusText;
          this.snackbar = true;
          this.loading = false;
        });
    },
    getBanks(data) {
      for (var i = 0; i < data.length; i++) {
        if (data[i].AuthorisationServers) {
          for (let y = 0; y < data[i].AuthorisationServers.length; y++) {
            this.banks.push({
              id: uuid(),
              avatar: data[i].AuthorisationServers[y].CustomerFriendlyLogoUri,
              title: data[i].AuthorisationServers[y].CustomerFriendlyName,
            });
          }
        }
      }
      this.banks.sort((a, b) => a.title.trim().localeCompare(b.title.trim()));
      for (let i = 0; i < this.banks.length; i++) {
        fetch(this.banks[i].avatar, { mode: "no-cors"}).then((response) => {
        }).catch((error) => {
          console.log("error", error);
          this.banks[i].avatar = "https://ui-avatars.com/api/?name=No+Logo";
        });
      }
    },

    getClients() {
      axios.get("clients", { withCredentials: true }).then((response) => {
        this.clients = response.data;
        this.clients.forEach((client) => {
          if (client.bank === this.selectedBank) {
            this.clientIds.push(client.clientId);
          }
        });
      });
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
      return this.banks.filter((bank) => {
        return bank.title.toLowerCase().includes(this.search.toLowerCase());
      });
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

  created() {
    console.log(this.$route.query);

    this.clientId = "";
    this.registrationAccessToken = "";

    this.selectedOption = this.$route.query.option;
    axios
      .get(`/banks/${this.selectedOption}`, { withCredentials: true })
      .then((response) => {
        this.loadingBanks = false;
        this.getBanks(response.data);
      });
  },
};
</script>
