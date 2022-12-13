<template>
  <CardWrapper title="Consents Granted">
    <template v-slot:top>
      <v-dialog transition="dialog-bottom-transition" max-width="800">
        <template v-slot:activator="{ on, attrs }">
          <v-btn color="primary" v-bind="attrs" v-on="on" depressed x-medium>
            Client Details
          </v-btn>
        </template>
        <template v-slot:default="dialog">
          <v-card>
            <v-toolbar class="blue-grey darken-4 font-weight-bold" dark>Client Details</v-toolbar>
            <v-card-text>
              <div>
                <v-row>
                  <v-col>
                    <v-card class="mt-5">
                      <v-list dense>
                        <v-list-item>
                          <v-list-item-content>Client ID:</v-list-item-content>
                          <v-list-item-content class="align-end">
                            {{ clientID }}
                          </v-list-item-content>
                        </v-list-item>

                        <v-list-item>
                          <v-list-item-content>Registration Access
                            Token:</v-list-item-content>
                          <v-list-item-content class="align-end">
                            {{ registrationAccessToken }}
                          </v-list-item-content>
                        </v-list-item>

                        <v-list-item>
                          <v-list-item-content>Scopes Granted:</v-list-item-content>
                          <v-list-item-content class="align-end">
                            {{ scopes }}
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
      <v-btn color="primary" class="ma-3" depressed x-medium @click="$router.push('consents')">
        Manage Consents
      </v-btn>
    </template>
    <template v-slot:card-content>
      <v-dialog transition="dialog-bottom-transition" max-width="800">
          <template v-slot:activator="{ on, attrs }">
            <v-btn class="ma-1 mt-4" outlined color="primary" v-bind="attrs" v-on="on"
              v-for="(consent, index) in grantedConsents" :key="index" @click="getConsentInfo(consent)">
              <v-icon left>mdi-information</v-icon>
              {{ consent.category }}
            </v-btn>
          </template>
          <template v-slot:default="dialog">
            <v-card>
              <v-toolbar class="blue-grey darken-4 font-weight-bold" dark>Permissions</v-toolbar>
              <v-card-text>
                <div>
                  <v-row>
                    <v-col>
                      <v-card>
                        <v-card-title class="subheading font-weight-bold mt-6">
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

                        <v-list v-for="(consentObj, index) in consentsArr" :key="index" dense>
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
    </template>
    <template v-slot:content>
      <v-card elevation="0" class="pa-0">
        <v-card-title class="px-0 pt-0 pb-5">Selected Consent ID</v-card-title>
        <span>{{ consentId }}</span>
      </v-card>
      <v-card elevation="0" class="pa-0">
        <v-card-title class="px-0 pt-0 pb-5">Selected Consent Status</v-card-title>
        <span :class="statusColor">{{ consentStatus }}</span>
      </v-card>
      <CardCode 
        class="mt-10" 
        color="lightblue" 
        title="Selected Consent POST Request" 
        :code="consentReqObj" 
        tooltip="POST request to the consents endpoint was made when
        permissions were selected from the permissions
        table." />
      <CardCode 
        class="mt-10" 
        color="lightblue" 
        title="Selected Consent GET Request" 
        :code="requestData" 
        tooltip="GET request to the consents endpoint was made before this page got mounted to the DOM to get the
        created consent." />
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Selected Consent Response" 
        :code="consentPayload" />

      <v-card elevation="0" class="pa-0 mt-10">
        <v-card-title class="px-0 pt-0 pb-5">
          Select which API to call with the consents that have been granted:
        </v-card-title>
        <v-btn color="primary" class="ma-3 mt-5" @click="$router.push('customers')">
          1. Personal/Business Info
        </v-btn>
        <v-btn color="primary" class="ma-3 mt-5" @click="$router.push('accounts')">
          2. Accounts
        </v-btn>
        <v-btn color="primary" class="ma-3 mt-5" @click="$router.push('credit-card-accounts')">
          3. Credit Card
        </v-btn>
        <v-dialog v-model="dialog" persistent max-width="600px">
          <template v-slot:activator="{ on, attrs }">
            <v-btn color="primary" v-bind="attrs" v-on="on" class="ma-3 mt-5">
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
                      <v-select :items="[
                        'Loans',
                        'Financings',
                        'Unarranged Accounts Overdraft',
                        'Invoice Financings',
                      ]" label="Credit Operations" dense outlined v-model="selectedCreditOperation"
                        :rules="creditOperationRules"></v-select>
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
              <v-btn color="blue darken-1" text @click="selectCreditOperationOption">
                Save
              </v-btn>
            </v-card-actions>
          </v-card>
        </v-dialog>
        <v-btn color="primary" class="ma-3 mt-5" @click="$router.push('resources')">
          5. Resources
        </v-btn>
      </v-card>
    </template>
  </CardWrapper>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import CardCode from "@/components/Shared/CardCode.vue";
import CardWrapper from "@/components/Shared/CardWrapper.vue";
import axios from "@/util/axios.js";

import { mapGetters, mapActions } from "vuex";

export default {
  name: "ConsentResponseMenu",
  components: {
    SheetAppBar,
    CardCode,
    CardWrapper,
  },
  data() {
    return {
      valid: true,
      dialog: false,
      selected: true,
      consentPayload: "",
      requestData: "",
      consentReqObj: "",
      grantedConsents: [],
      grantedConsentsCategory: "",
      consentsArr: [],
      selectedCreditOperation: "",
      creditOperationRules: [(v) => !!v || "please select a credit operation"],
      consentId: "",
      consentStatus: "",
      statusColor: "",
    };
  },

  methods: {
    ...mapActions(["setError", "setLoading", "setCadastroOption", "setSelectedConsent", "addToConsentsList"]),

    getConsentInfo(consentData) {
      this.grantedConsentsCategory = consentData.category;
      this.consentsArr = consentData.permissionsArray;
    },

    async selectCreditOperationOption() {
      this.$refs.form.validate();
      await setTimeout(() => { }, 100);
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
      "newConsent",
      "selectedConsent",
    ]),
    creditOperationSelected() {
      return !(this.selectedCreditOperation);
    },
  },
  async created() {
    try {
      this.setLoading(true);
      const response = await axios.get("/consent/consent-response", {
        withCredentials: true,
      });

      if (this.newConsent) {
        this.setSelectedConsent(response.data);
        this.addToConsentsList(response.data);
      }

      if (this.selectedConsent != null) {
        this.consentPayload = this.selectedConsent.consent;
        this.requestData = this.selectedConsent.requestData;
        this.consentReqObj = this.selectedConsent.consentReqObj;
        this.consentId = this.selectedConsent.consent.data.consentId;
        this.grantedConsents = this.selectedConsent.permissionsData;
        this.consentStatus = this.selectedConsent.consent.data.status;
      }

      switch (this.consentStatus) {
        case "AUTHORISED":
          this.statusColor = "green--text";
          break;
        case "AWAITING_AUTHORISATION":
          this.statusColor = "yellow--text";
          break;
        case "REJECTED":
          this.statusColor = "red--text";
          break;
        default:
          this.statusColor = "gray--text";
      }

      let cadastroOption;
      this.grantedConsents.forEach((grantedConsent) => {
        if (grantedConsent.group.includes("PF")) {
          cadastroOption = "PF";
        } else if (grantedConsent.group.includes("PJ")) {
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
        const filteredArr = this.grantedConsents.filter(
          (theConsent) => theConsent.category === consent.category
        );
        for (const cnst of filteredArr) {
          for (const formatedConsent of formatedConsents) {
            if (formatedConsent["category"] === cnst["category"]) {
              itemFound = true;
            }
          }
          if (!itemFound) {
            consentObj.category = cnst.category;
            consentObj.permissionsArray.push({
              group: cnst.group,
              permissions: this.convertArrayToString(
                cnst.permissions.map((permissions) => permissions.permission)
              ),
            });
          }
        }

        if (consentObj.category) {
          formatedConsents.push(consentObj);
        }
      }

      this.grantedConsents = [...formatedConsents];
      this.setLoading(false);
    } catch (error) {
      this.setError(error.message);
    }
  },
};
</script>
