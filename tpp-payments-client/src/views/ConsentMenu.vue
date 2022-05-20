<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Consent Menu" />
        <v-container class="pa-md-12" style="background: #ffffff">
          <v-dialog
            transition="dialog-bottom-transition"
            max-width="800"
            v-model="dialog"
            v-if="$route.params.data"
          >
            <template v-slot:default="dialog">
              <v-card>
                <v-toolbar class="blue-grey darken-4 font-weight-bold" dark
                  >Client Details</v-toolbar
                >
                <v-card-text>
                  <div>
                    <v-row>
                      <v-col>
                        <v-alert dense text type="success" class="mt-5">
                          {{ messageText }}
                        </v-alert>
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
                                >Registration Access Token:</v-list-item-content
                              >
                              <v-list-item-content class="align-end">
                                {{ registrationAccessToken }}
                              </v-list-item-content>
                            </v-list-item>

                            <v-list-item>
                              <v-list-item-content
                                >Granted Scopes:</v-list-item-content
                              >
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
                  <v-btn text @click="dialog.value = false">OK</v-btn>
                </v-card-actions>
              </v-card>
            </template>
          </v-dialog>

          <v-row align="center">
            <v-col class="d-flex" cols="12" sm="4">
              <v-select
                :items="cadastroOptions"
                label="Cadastro Options"
                dense
                outlined
                v-model="selectedOption"
              ></v-select>
            </v-col>

            <v-col cols="12" sm="4">
              <v-text-field
                dense
                outlined
                label="Identification"
                v-model="identification"
                required
                :rules="identificationRules"
              ></v-text-field>
            </v-col>
            <v-col cols="12" sm="4">
              <v-text-field
                dense
                outlined
                label="Rel"
                v-model="rel"
                required
                :rules="relRules"
              ></v-text-field>
            </v-col>
          </v-row>
          <v-data-table
            :hide-default-footer="true"
            :items-per-page="11"
            :headers="headers"
            :items="consentsArr"
            class="elevation-1"
          >
            <template v-slot:[`item.permissions`]="{ item }">
              <li v-for="(i, index) in item.permissions" :key="index">
                {{ i }}
              </li>
            </template>

            <template v-slot:[`item.consent`]="{ item }">
              <v-simple-checkbox
                :ripple="false"
                v-model="item.consent"
              ></v-simple-checkbox>
            </template>
          </v-data-table>
          <v-col class="text-right">
            <v-btn
              class="mt-8 mx-auto"
              depressed
              color="primary"
              @click="continueConsent"
            >
              Continue
            </v-btn>
          </v-col>
        </v-container>
      </v-col>
      <v-col cols="12" sm="2">
        <BackButton path="banks" />
      </v-col>
    </v-row>
    <v-overlay :value="loading">
      <v-progress-circular indeterminate size="100"></v-progress-circular>
    </v-overlay>
    <v-snackbar v-model="snackbar" :multi-line="multiLine">
      {{ snackbarMessage }}

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
  name: "ConsentMenu",
  components: {
    SheetAppBar,
    BackButton,
  },
  data() {
    return {
      dialog: true,
      multiLine: true,
      snackbar: false,
      snackbarMessage: "",
      loading: false,
      cadastroOptions: ["PF", "PJ"],
      selectedOption: "PF",
      consentsArr: [],
      consentsDeepCopy: [],
      identificationRules: [(v) => !!v || "Identification is required"],
      relRules: [(v) => !!v || "Rel is required"],
      rel: "CPF",
      identification: "76109277673",
      headers: [
        {
          text: "CATEGORIA DE DADOS",
          align: "start",
          sortable: false,
          value: "dataCategory",
        },
        { text: "AGRUPAMENTO", value: "group" },
        { text: "PERMISSIONS", value: "permissions" },
        { text: "GIVE CONSENT", value: "consent" },
      ],
      messageText: "",
    };
  },

  watch: {
    selectedOption(val) {
      this.consentsArr = this.consentsDeepCopy.filter((consent) => {
        if (val === "PF") {
          return consent.id !== 3 && consent.id !== 4;
        } else if (val === "PJ") {
          return consent.id !== 1 && consent.id !== 2;
        }
      });
    },
  },

  computed: {
    ...mapGetters([
      "consents",
      "scopes",
      "clientID",
      "registrationAccessToken",
    ]),
  },

  created() {
    const selectedDcrOption = this.$route.params.data?.selectedDcrOption;
    this.messageText =
      selectedDcrOption === "USE_EXISTING_CLIENT"
        ? "Obtained the registered client's details successfully"
        : "Dynamic client registration has been done successfully";
    this.setCadastroOption(this.selectedOption);
    this.consentsDeepCopy = JSON.parse(JSON.stringify(this.consents));
    this.consentsArr = this.consentsDeepCopy.filter(
      (consent) => consent.id !== 3 && consent.id !== 4
    );
  },

  methods: {
    ...mapActions(["setCadastroOption"]),
    continueConsent() {
      this.setCadastroOption(this.selectedOption);
      this.loading = true;
      axios.defaults.withCredentials = true;
      const selectedConsents = this.consentsArr.filter(
        (rowData) => rowData.consent === true
      );

      const bankConsent = window.open("", "_self");
      axios
        .post(
          "/consent",
          {
            permissionsArr: selectedConsents,
            identification: this.identification,
            rel: this.rel,
          },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        )
        .then((res) => {
          if (res.status === 201) {
            bankConsent.location.href = res.data.authUrl;
          }
        })
        .catch((error) => {
          if (error.response.status !== 201) {
            this.snackbarMessage = `Error ${error.response.status} - ${error.message}`;
            this.snackbar = true;
          }
          this.loading = false;
        });
    },
  },
};
</script>
