<template>
  <v-main class="banks">
    <v-row>
      <v-col> </v-col>
      <v-col :cols="7">
        <v-sheet min-height="70vh" elevation="20" rounded="lg">
          <SheetAppBar header="Mock TPP" />
          <v-row>
            <v-col> </v-col>
            <v-col cols="10">
              <div class="pa-md-4 transition-swing text-h6" align="center">
                Payment Provider Details
              </div>
              <v-text-field
                label="Search"
                outlined
                clearable
                v-model="search"
              ></v-text-field>

              <v-card class="mx-auto">
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
                >
                  Continue
                </v-btn>
              </template>
              <v-card>
                <v-card-title>
                  <span class="text-h5">DCR Options</span>
                </v-card-title>
                <v-card-text>
                  <v-container>
                    <v-form ref="form" v-model="valid" lazy-validation>
                      <v-row>
                        <v-col class="d-flex" cols="12" sm="12">
                          <v-select
                            :items="dcrOptions"
                            label="DCR Options"
                            dense
                            outlined
                            v-model="selectedDcrOption"
                          ></v-select>
                        </v-col>

                        <template
                          v-if="selectedDcrOption === 'Use Existing DCR'"
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
      <v-col> </v-col>
    </v-row>
    <v-overlay :value="loading">
      <v-progress-circular indeterminate size="100"></v-progress-circular>
    </v-overlay>
    <v-snackbar v-model="snackbar" :multi-line="multiLine">
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
import axios from "../util/axios.js";
import { v1 as uuid } from "uuid";
import { mapGetters } from "vuex";

export default {
  name: "BankView",
  components: {
    SheetAppBar,
  },
  data: () => ({
    dcrOptions: ["Perform New DCR", "Use Existing DCR"],
    selectedDcrOption: "Perform New DCR",
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
    selectedBank: "Mock Bank",
    banks: [],
    search: "",
  }),
  methods: {
    selectBank(bankTitle) {
      this.selectedBank = bankTitle;
    },
    confirmSelectedBank() {
      if ((this.selectedDcrOption === "Use Existing DCR") && (!this.clientId || !this.registrationAccessToken)) {
        this.$refs.form.validate();
        return;
      }
      this.dialog = false;
      axios.defaults.withCredentials = true;
      this.loading = true;
      axios
        .post(
          "/dcr",
          { bank: this.selectedBank, selectedDcrOption: this.selectedDcrOption, clientId: this.clientId, registrationAccessToken: this.registrationAccessToken},
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        )
        .then((res) => {
          this.clientId = res.data.clientId;
          this.registrationAccessToken = res.data.registrationAccessToken;
          if (this.selectedOption === "payments") {
            this.$router.push({
              name: "payment-menu",
              params: {
                data: {
                  selectedBank: this.selectedBank,
                  clientId: res.data.clientId,
                },
              },
            });
          } else {
            this.$router.push({
              name: "consent-menu",
            });
          }
        })
        .catch((err) => {
          console.log(err);
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
    },
  },

  computed: {
    searchBank() {
      return this.banks.filter((bank) => {
        return bank.title.toLowerCase().includes(this.search.toLowerCase());
      });
    },

    clientId: {
      get () {
        return this.$store.state.mockTPP.clientID
      },
      set (clientId) {
        this.$store.commit('setClientID', clientId)
      }
    },

    registrationAccessToken: {
      get () {
        return this.$store.state.mockTPP.registrationAccessToken
      },
      set (registrationAccessToken) {
        this.$store.commit('setRegistrationAccessToken', registrationAccessToken)
      }
    },

    ...mapGetters(["selectedOption", "clientID"]),
  },

  created() {
    axios
      .get(`/banks/${this.selectedOption}`, { withCredentials: true })
      .then((response) => {
        this.getBanks(response.data);
      });
  },
};
</script>
