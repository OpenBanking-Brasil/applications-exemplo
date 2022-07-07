<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Consent Menu" />

        <v-card>
          <v-tabs v-model="tab" background-color="transparent" grow>
            <v-tab v-for="item in items" :key="item">
              {{ item }}
            </v-tab>
          </v-tabs>

          <v-tabs-items v-model="tab">
            <v-tab-item v-for="item in items" :key="item">
              <v-container class="pa-md-12" style="background: #ffffff">
                <template v-if="item === 'Permissions'">
                  <v-dialog
                    transition="dialog-bottom-transition"
                    max-width="800"
                    v-model="dialog"
                    v-if="$route.params.data"
                  >
                    <template v-slot:default="dialog">
                      <v-card>
                        <v-toolbar
                          class="blue-grey darken-4 font-weight-bold"
                          dark
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
                                        >Registration Access
                                        Token:</v-list-item-content
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
                </template>

                <template v-else>
                  <v-row>
                    <v-col cols="12" sm="5" md="5">
                      <v-divider />
                    </v-col>
                    <v-col cols="12" sm="2" md="2" class="text-center mt-n3">
                      <h4>Logged User</h4>
                    </v-col>
                    <v-col cols="12" sm="5" md="5">
                      <v-divider />
                    </v-col>
                  </v-row>
                  <v-row align="center">
                    <v-col cols="12" sm="6">
                      <v-text-field
                        dense
                        outlined
                        label="Identification"
                        v-model="loggedUserId"
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
                        v-model="loggedUserRel"
                      ></v-text-field>
                    </v-col>
                  </v-row>

                  <v-row>
                    <v-col cols="12" sm="5" md="5">
                      <v-divider />
                    </v-col>
                    <v-col cols="12" sm="2" md="2" class="text-center mt-n3">
                      <h4>Business Entity</h4>
                    </v-col>
                    <v-col cols="12" sm="5" md="5">
                      <v-divider />
                    </v-col>
                  </v-row>
                  <v-row align="center">
                    <v-col cols="12" sm="6">
                      <v-text-field
                        dense
                        outlined
                        label="Identification"
                        v-model="businessEntityId"
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
                        v-model="businessEntityRel"
                      ></v-text-field>
                    </v-col>
                  </v-row>
                  <v-row>
                    <v-col cols="12" sm="5" md="5">
                      <v-divider />
                    </v-col>
                    <v-col cols="12" sm="2" md="2" class="text-center mt-n3">
                      <h4>Date and Time</h4>
                    </v-col>
                    <v-col cols="12" sm="5" md="5">
                      <v-divider />
                    </v-col>
                  </v-row>
                  <v-row align="center">
                    <v-col cols="12" sm="3" md="3">
                      <DatePicker
                        :date="transactionFromDate"
                        dateLabel="Transaction From Date"
                        @change-date="changeDate"
                      />
                    </v-col>
                    <v-col cols="12" sm="3" md="3">
                      <v-icon
                        small
                        title="Acceptable time format: hh:mm:ss"
                        class="ml-8 mt-n5"
                        style="position: absolute"
                      >
                        mdi-information
                      </v-icon>
                      <v-text-field
                        outlined
                        dense
                        v-model="transactionFromTime"
                        label="Transaction From Time"
                        prepend-icon="mdi-clock"
                      ></v-text-field>
                    </v-col>

                    <v-col cols="12" sm="3" md="3">
                      <DatePicker
                        :date="transactionToDate"
                        dateLabel="Transaction To Date"
                        @change-date="changeDate"
                      />
                    </v-col>
                    <v-col cols="12" sm="3" md="3">
                      <v-icon
                        small
                        title="Acceptable time format: hh:mm:ss"
                        class="ml-8 mt-n5"
                        style="position: absolute"
                      >
                        mdi-information
                      </v-icon>
                      <v-text-field
                        outlined
                        dense
                        v-model="transactionToTime"
                        label="Transaction To Time"
                        prepend-icon="mdi-clock"
                      ></v-text-field>
                    </v-col>
                  </v-row>

                  <v-row align="center">
                    <v-col cols="12" sm="3" md="3">
                      <DatePicker
                        :date="expirationDate"
                        dateLabel="Expiration Date"
                        @change-date="changeDate"
                      />
                    </v-col>
                    <v-col cols="12" sm="3" md="3">
                      <v-icon
                        small
                        title="Acceptable time format: hh:mm:ss"
                        class="ml-8 mt-n5"
                        style="position: absolute"
                      >
                        mdi-information
                      </v-icon>
                      <v-text-field
                        outlined
                        dense
                        v-model="expirationTime"
                        label="Expiration Time"
                        prepend-icon="mdi-clock"
                      ></v-text-field>
                    </v-col>
                  </v-row>
                </template>
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
            </v-tab-item>
          </v-tabs-items>
        </v-card>
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
import DatePicker from "@/components/GeneralAppComponents/DatePicker.vue";
import axios from "../util/axios.js";
import { mapGetters } from "vuex";

export default {
  name: "ConsentMenu",
  components: {
    SheetAppBar,
    BackButton,
    DatePicker,
  },
  data() {
    return {
      tab: null,
      items: ["Permissions", "Permissions Settings"],
      text: "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",

      dialog: true,
      multiLine: true,
      snackbar: false,
      snackbarMessage: "",
      loading: false,
      modal: false,
      consentsArr: [],
      consentsDeepCopy: [],
      loggedUserId: "76109277673",
      loggedUserRel: "CPF",
      businessEntityId: "",
      businessEntityRel: "",
      transactionFromDate: null,
      transactionFromTime: null,
      transactionToDate: null,
      transactionToTime: null,
      expirationDate: null,
      expirationTime: null,
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
      "ApiOption",
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
    changeDate(newDate, flag) {
      switch (flag) {
        case "TransactionFromDate":
          this.transactionFromDate = newDate;
          break;
        case "TransactionToDate":
          this.transactionToDate = newDate;
          break;
        default:
          this.expirationDate = newDate;
      }
    },
    async continueConsent() {
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
      let res;
      try {
        res = await axios.post(
          "/consent/create-consent",
          {
            permissionsArr: selectedConsents,
            loggedUserId: this.loggedUserId,
            loggedUserRel: this.loggedUserRel,
            businessEntityId: this.businessEntityId,
            businessEntityRel: this.businessEntityRel,
            transactionFromDate: this.transactionFromDate,
            transactionFromTime: this.transactionFromTime,
            transactionToDate: this.transactionToDate,
            transactionToTime: this.transactionToTime,
            expirationDate: this.expirationDate,
            expirationTime: this.expirationTime,
            ApiOption: this.ApiOption,
          },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );
        if (res.status === 201) {
          bankConsent.location.href = res.data.authUrl;
        }
      } catch (error) {
        if (error.response.status !== 201) {
          this.snackbarMessage = `Error ${error.response.status} - ${error.message}`;
          this.snackbar = true;
        }
        this.loading = false;
      }
    },
  },
};
</script>
