<template>
  <div class="consent-menu layout-wrapper">
    <v-sheet>
      <v-tabs v-model="tab" height="46px" color="#007199" background-color="transparent" grow>
        <v-tab v-for="item in items" :key="item">
          {{ item }}
        </v-tab>
      </v-tabs>

      <v-tabs-items v-model="tab">
        <v-tab-item v-for="item in items" :key="item">
          <template v-if="item === 'Permissions'">
            <client-details-dialog
              v-if="$route.params.data"
              :messageText="messageText"
            />

            <v-data-table
              :hide-default-footer="true"
              :items-per-page="11"
              :headers="headers"
              :items="consentsArr"
              class="elevation-0"
            >
              <template v-slot:[`item.group`]="{ item }" class="d-flex">
                <div class="d-flex">
                  <v-simple-checkbox
                    color="#007199"
                    on-icon="mdi-circle-slice-8"
                    off-icon="mdi-circle-outline"
                    :ripple="false"
                    v-model="item.consent"
                  />

                  <span>{{ item.group }}</span>
                </div>
              </template>

              <template v-slot:[`item.permissions`]="{ item }">
                <ul>
                  <li v-for="(i, index) in item.permissions" :key="index">
                    <v-simple-checkbox
                      :ripple="false"
                      color="#007199"
                      on-icon="mdi-circle-slice-8"
                      off-icon="mdi-circle-outline"
                      v-model="i.consent"
                      style="transform: scale(0.7)"
                    />

                    <span>{{ i.permission }}</span>
                  </li>
                </ul>
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

            <v-row class="px-7">
              <v-col cols="12" sm="6">
                <v-text-field
                  dense
                  outlined
                  label="Identification"
                  v-model="loggedUserId"
                  class="mt-4"
                ></v-text-field>
              </v-col>

              <v-col cols="12" sm="6">
                <div class="app-label-holder d-flex justify-end">
                  <v-icon
                    small
                    title="CPF or CNPJ are acceptable"
                    color="rgba(57, 75, 101, 0.2)"
                  >mdi-help-circle</v-icon>
                </div>

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

            <v-row class="px-7">
              <v-col cols="12" sm="6">
                <v-text-field
                  dense
                  outlined
                  label="Identification"
                  v-model="businessEntityId"
                  class="mt-4"
                ></v-text-field>
              </v-col>

              <v-col cols="12" sm="6">
                <div class="app-label-holder d-flex justify-end">
                  <v-icon
                    small
                    title="CPF or CNPJ are acceptable"
                    color="rgba(57, 75, 101, 0.2)"
                  >mdi-help-circle</v-icon>
                </div>

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
            
            <v-row class="px-7">
              <v-col cols="12" sm="6" md="6" class="mt-4">
                <DatePicker
                  :date="transactionFromDate"
                  dateLabel="Transaction From Date"
                  @change-date="changeDate"
                />
              </v-col>

              <v-col cols="12" sm="6" md="6">
                <div class="app-label-holder d-flex justify-end">
                  <v-icon
                    small
                    title="Acceptable time format: hh:mm:ss"
                    color="rgba(57, 75, 101, 0.2)"
                  >mdi-help-circle</v-icon>
                </div>

                <v-text-field
                  outlined
                  dense
                  v-model="transactionFromTime"
                  label="Transaction From Time"
                  append-icon="mdi-clock"
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row class="px-7">
              <v-col cols="12" sm="6" md="6" class="mt-4">
                <DatePicker
                  :date="transactionToDate"
                  dateLabel="Transaction To Date"
                  @change-date="changeDate"
                />
              </v-col>
              
              <v-col cols="12" sm="6" md="6">
                <div class="app-label-holder d-flex justify-end">
                  <v-icon
                    small
                    title="Acceptable time format: hh:mm:ss"
                    color="rgba(57, 75, 101, 0.2)"
                  >mdi-help-circle</v-icon>
                </div>

                <v-text-field
                  outlined
                  dense
                  v-model="transactionToTime"
                  label="Transaction To Time"
                  append-icon="mdi-clock"
                ></v-text-field>
              </v-col>
            </v-row>

            <v-row class="px-7 mb-2">
              <v-col cols="12" sm="6" md="6" class="mt-4">
                <DatePicker
                  :date="expirationDate"
                  dateLabel="Expiration Date"
                  @change-date="changeDate"
                />
              </v-col>

              <v-col cols="12" sm="6" md="6">
                <div class="app-label-holder d-flex justify-end">
                  <v-icon
                    small
                    title="Acceptable time format: hh:mm:ss"
                    color="rgba(57, 75, 101, 0.2)"
                  >mdi-help-circle</v-icon>
                </div>

                <v-text-field
                  outlined
                  dense
                  v-model="expirationTime"
                  label="Expiration Time"
                  append-icon="mdi-clock"
                ></v-text-field>
              </v-col>
            </v-row>
          </template>
        </v-tab-item>
      </v-tabs-items>

      <v-row class="layout-wrapper__bottom-btns">
        <v-col cols="12" class="pa-0">
          <v-btn
            depressed
            block 
            text
            height="57"
            @click="continueConsent"
          >
            Continue
          </v-btn>
        </v-col>
      </v-row>
    </v-sheet>
  </div>
</template>

<script>
import DatePicker from "@/components/Shared/DatePicker.vue";
import ClientDetailsDialog from "@/components/Dialogs/ClientDetailsDialog";
import axios from "@/util/axios.js";
import { mapActions, mapGetters } from "vuex";

export default {
  name: "ConsentMenu",
  components: {
    DatePicker,
    ClientDetailsDialog,
  },
  data() {
    return {
      tab: null,
      items: ["Permissions", "Permissions Settings"],
      text: "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",

      dialog: true,
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
    ...mapActions(["setNewConsent", "setError", "setLoading"]),
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
      this.setLoading(true);
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
          this.setNewConsent(true);
          bankConsent.location.href = res.data.authUrl;
        }
        this.setLoading(false);
      } catch (error) {
        if (error.response.status !== 201) {
          this.setError(`Error ${error.response.status} - ${error.message}`);
        } else {
          this.setError(error.message);
        }
      }
    },
  },
};
</script>
