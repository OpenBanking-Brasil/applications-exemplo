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
            <v-col cols="12" sm="6">
              <v-text-field
                dense
                outlined
                label="Identification"
                v-model="identification"
                required
                :rules="identificationRules"
                class="mt-6"
              ></v-text-field>
            </v-col>
            <v-col cols="12" sm="6">
              <v-icon
                small
                title="CPF or CNPJ are acceptable"
                class="mb-2"
              >
                mdi-information
              </v-icon>
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
              <div v-for="(i, index) in item.permissions" :key="index">
                <li class="mb-1">
                  {{ i.permission }}
                </li>
              </div>
            </template>

            <template v-slot:[`item.consent`]="{ item }">
              <v-row>
                <v-col cols="6" md="6" class="d-flex align-center">
                  <v-simple-checkbox
                    :ripple="false"
                    v-model="item.consent"
                    style="transform: scale(1.3)"
                  ></v-simple-checkbox>
                </v-col>
                <v-col cols="6" md="6">
                  <v-simple-checkbox
                    v-for="(consentItem, i) in item.permissions"
                    :key="i"
                    :ripple="false"
                    v-model="consentItem.consent"
                  ></v-simple-checkbox>
                </v-col>
              </v-row>
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
    this.consentsDeepCopy = JSON.parse(JSON.stringify(this.consents));
    this.consentsArr = this.consentsDeepCopy;
  },

  methods: {
    continueConsent() {
      this.loading = true;
      axios.defaults.withCredentials = true;
      const selectedConsentsbyGroup = this.consentsArr.filter(
        (rowData) => rowData.consent === true
      );
      const individuallySelectedConsents = this.consentsArr.filter(
        (rowData) => rowData.consent === false
      );
      const filteredConsents = individuallySelectedConsents
        .map((consent) => {
          let consentGranted = false;
          const obj = {
            ...consent,
            permissions: consent.permissions.filter((permission) => {
              if (permission.consent) {
                consentGranted = true;
                return true;
              }
              return false;
            }),
          };

          if (consentGranted) {
            return { ...obj, consent: consentGranted };
          }
        })
        .filter((consent) => consent);

      const selectedConsents = [
        ...selectedConsentsbyGroup,
        ...filteredConsents,
      ];

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
