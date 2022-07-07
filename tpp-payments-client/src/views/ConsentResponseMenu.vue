<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Consent Response Menu" />

        <v-sheet min-height="70vh" rounded="lg" v-if="!loading">
          <v-container class="pa-md-12">
            <v-dialog transition="dialog-bottom-transition" max-width="800">
              <template v-slot:activator="{ on, attrs }">
                <v-btn
                  color="primary"
                  v-bind="attrs"
                  v-on="on"
                  depressed
                  x-medium
                >
                  Client Details</v-btn
                >
              </template>
              <template v-slot:default="dialog">
                <v-card>
                  <v-toolbar class="blue-grey darken-4 font-weight-bold" dark
                    >Client Details</v-toolbar
                  >
                  <v-card-text>
                    <div>
                      <v-row>
                        <v-col>
                          <v-card class="mt-5">
                            <v-list dense>
                              <v-list-item>
                                <v-list-item-content
                                  >Client ID:</v-list-item-content
                                >
                                <v-list-item-content class="align-end">
                                  {{ clientID }}
                                </v-list-item-content>
                              </v-list-item>

                              <v-list-item>
                                <v-list-item-content
                                  >Registration Access
                                  Token:</v-list-item-content
                                >
                                <v-list-item-content class="align-end">
                                  {{ registrationAccessToken }}
                                </v-list-item-content>
                              </v-list-item>

                              <v-list-item>
                                <v-list-item-content
                                  >Scopes Granted:</v-list-item-content
                                >
                                <v-list-item-content class="align-end">
                                  {{ scopes }}
                                </v-list-item-content>
                              </v-list-item>
                            </v-list>
                            <v-progress-linear
                              v-if="loading"
                              indeterminate
                              color="primary"
                            ></v-progress-linear>
                          </v-card>
                        </v-col>
                      </v-row>
                    </div>
                  </v-card-text>
                  <v-card-actions class="justify-end">
                    <v-btn text @click="dialog.value = false">Close</v-btn>
                  </v-card-actions>
                </v-card>
              </template>
            </v-dialog>

            <div class="pa-2"></div>
            <v-row>
              <v-col cols="12" md="8">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text blue darken-4"
                    >Consent Request</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto"
                      >{{ requestData }}
                    </pre>
                  </v-card-text>
                </v-card>
                <div class="pa-2"></div>
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text cyan darken-4"
                    >Consent Response</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto"
                      >{{ consentPayload }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
              <v-col cols="12" md="3">
                <h3 class="grey--text text--darken-1">Consents Granted</h3>
                <v-dialog transition="dialog-bottom-transition" max-width="800">
                  <template v-slot:activator="{ on, attrs }">
                    <v-btn
                      class="ma-1 mt-4"
                      outlined
                      color="primary"
                      v-bind="attrs"
                      v-on="on"
                      v-for="(consent, index) in grantedConsents"
                      :key="index"
                      @click="
                        () => {
                          getConsentInfo(consent);
                        }
                      "
                    >
                      <v-icon left>mdi-information</v-icon>
                      {{ consent.category }}
                    </v-btn>
                  </template>
                  <template v-slot:default="dialog">
                    <v-card>
                      <v-toolbar
                        class="blue-grey darken-4 font-weight-bold"
                        dark
                        >Permissions</v-toolbar
                      >
                      <v-card-text>
                        <div>
                          <v-row>
                            <v-col>
                              <v-card>
                                <v-card-title
                                  class="subheading font-weight-bold mt-6"
                                >
                                  {{ grantedConsentsCategory }}
                                </v-card-title>

                                <v-divider></v-divider>
                                <v-list-item>
                                  <v-list-item-content>
                                    <strong>Group(s)</strong>
                                  </v-list-item-content>
                                  <v-list-item-content class="align-end">
                                    <strong>Permissions</strong>
                                  </v-list-item-content>
                                </v-list-item>
                                <v-divider></v-divider>

                                <v-list
                                  v-for="(consentObj, index) in consentsArr"
                                  :key="index"
                                  dense
                                >
                                  <v-list-item>
                                    <v-list-item-content>
                                      {{ consentObj.group }}
                                    </v-list-item-content>
                                    <v-list-item-content class="align-end" style="overflow: auto">
                                      {{ consentObj.permissions }}
                                    </v-list-item-content>
                                  </v-list-item>
                                </v-list>
                              </v-card>
                            </v-col>
                          </v-row>
                        </div>
                      </v-card-text>
                      <v-card-actions class="justify-end">
                        <v-btn text @click="dialog.value = false">Close</v-btn>
                      </v-card-actions>
                    </v-card>
                  </template>
                </v-dialog>
              </v-col>
            </v-row>
            <div class="pa-2"></div>
            <v-divider class="mt-5"></v-divider>
            <h3 class="ma-3 mt-5 grey--text text--darken-1">
              Select which API to call with the consents that have been granted:
            </h3>
            <v-btn
              color="primary"
              class="ma-3 mt-5"
              @click="$router.push('customers')"
            >
              1. Personal/Business Info
            </v-btn>
            <v-btn
              color="primary"
              class="ma-3 mt-5"
              @click="$router.push('accounts')"
            >
              2. Accounts
            </v-btn>
            <v-btn
              color="primary"
              class="ma-3 mt-5"
              @click="$router.push('credit-card-accounts')"
            >
              3. Credit Card
            </v-btn>

            <v-dialog v-model="dialog" persistent max-width="600px">
              <template v-slot:activator="{ on, attrs }">
                <v-btn
                  color="primary"
                  v-bind="attrs"
                  v-on="on"
                  class="ma-3 mt-5"
                >
                  4. Credit Operations
                </v-btn>
              </template>
              <v-card>
                <v-card-title>
                  <span class="text-h5">Credit Operation Options</span>
                </v-card-title>
                <v-card-text>
                  <v-container>
                    <v-form ref="form" v-model="valid" lazy-validation>
                      <v-row>
                        <v-col class="d-flex" cols="12" sm="12">
                          <v-select
                            :items="[
                              'Loans',
                              'Financings',
                              'Unarranged Accounts Overdraft',
                              'Invoice Financings',
                            ]"
                            label="Credit Operations"
                            dense
                            outlined
                            v-model="selectedCreditOperation"
                            :rules="creditOperationRules"
                          ></v-select>
                        </v-col>
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
                    @click="selectCreditOperationOption"
                  >
                    Save
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>
            <v-btn
              color="primary"
              class="ma-3 mt-5"
              @click="$router.push('resources')"
            >
              5. Resources
            </v-btn>
          </v-container>
        </v-sheet>
      </v-col>
      <v-col cols="12" sm="2">
        <BackButton path="consent-menu" />
      </v-col>
    </v-row>
    <v-overlay :value="loading">
      <v-progress-circular indeterminate size="100"></v-progress-circular>
    </v-overlay>
        <v-snackbar v-model="snackbar" :multi-line="multiLine" color="red accent-2">
      {{ errorMessage }}

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
import { mapGetters, mapActions } from "vuex";

export default {
  name: "ConsentResponseMenu",
  components: {
    SheetAppBar,
    BackButton,
  },
  data() {
    return {
      multiLine: true,
      snackbar: false,
      errorMessage: "",
      valid: true,
      dialog: false,
      selected: true,
      loading: true,
      consentPayload: "",
      requestData: "",
      grantedConsents: [],
      grantedConsentsCategory: "",
      consentsArr: [],
      selectedCreditOperation: "",
      creditOperationRules: [(v) => !!v || "please select a credit operation"],
    };
  },

  methods: {
    ...mapActions(["setCadastroOption"]),
    getConsentInfo(consentData) {
      this.grantedConsentsCategory = consentData.category;
      this.consentsArr = consentData.permissionsArray;
    },

    async selectCreditOperationOption() {
      this.$refs.form.validate();
      await setTimeout(() => {}, 100);
      if (!this.valid) {
        return;
      }
      this.dialog = false;
      if (this.selectedCreditOperation === "Loans") {
        this.$router.push("loans");
      } else if (this.selectedCreditOperation === "Financings") {
        this.$router.push("financings");
      } else if (this.selectedCreditOperation === "Invoice Financings") {
        this.$router.push("invoice-financings");
      } else if (
        this.selectedCreditOperation === "Unarranged Accounts Overdraft"
      ) {
        this.$router.push("unarranged-accounts-overdraft");
      }
    },

    convertArrayToString(arr) {
      let text = "";
      arr.forEach((item) => {
        text = text + " " + item;
      });

      return text;
    },
  },

  computed: {
    ...mapGetters([
      "consents",
      "scopes",
      "clientID",
      "registrationAccessToken",
      "ApiOption",
    ]),
    creditOperationSelected() {
      if (this.selectedCreditOperation) {
        return false;
      }

      return true;
    },
  },
  async created() {
    try {
      const response = await axios.get("/consent/consent-response", { withCredentials: true });
      this.consentPayload = response.data.consent;
      this.requestData = response.data.requestData;
      this.grantedConsents = response.data.permissionsData;

      let cadastroOption;
      this.grantedConsents.forEach((grantedConsent) => {
        if(grantedConsent.group.includes("PF")){
          cadastroOption = "PF";
        } else if (grantedConsent.group.includes("PJ")){
          cadastroOption = "PJ";
        }
      });

      this.setCadastroOption(cadastroOption);

      let formatedConsents = [];
      for (let consent of this.grantedConsents) {
        const consentObj = {
          category: "",
          permissionsArray: [],
        };

        let itemFound = false;
        const filteredArr = this.grantedConsents.filter((theConsent) => theConsent.category === consent.category);
        for(const cnst of filteredArr){
          for(const formatedConsent of formatedConsents){
            if(formatedConsent["category"] === cnst["category"]){
              itemFound = true;
            }
          }
          if(!itemFound){
            consentObj.category = cnst.category;
            consentObj.permissionsArray.push({
              group: cnst.group,
              permissions: this.convertArrayToString(cnst.permissions.map((permissions) => permissions.permission))
            });
          }
        }

        if(consentObj.category){
          formatedConsents.push(consentObj);
        }
      }

      this.grantedConsents = [...formatedConsents];
      this.loading = false;
    } catch (error){
      this.errorMessage = error.message;
      this.snackbar = true;
    }
  },
};
</script>
