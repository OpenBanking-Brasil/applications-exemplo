<template>
  <v-main class="main">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <v-sheet
          min-height="70vh"
          elevation="20"
          rounded="lg"
          class="grey lighten-3"
        >
          <SheetAppBar header="Mock TPP" />
          <v-container class="pa-md-12">
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
            <v-row>
              <v-col cols="12" sm="6" md="3">
                <b>Client ID</b>
                <v-text-field
                  placeholder="123456"
                  outlined
                  filled
                  v-model="clientID"
                ></v-text-field>
              </v-col>
              <v-col> </v-col>
              <v-col cols="12" sm="6" md="3">
                <b>Refresh Token</b>
                <v-text-field
                  placeholder="123456"
                  outlined
                  filled
                  v-model="refreshToken"
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" sm="6" md="3">
                <b>Consent ID</b>
                <v-text-field
                  placeholder="123456"
                  outlined
                  filled
                  v-model="consentID"
                ></v-text-field>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" sm="6" md="3">
                <b>Payment ID</b>
                <v-text-field
                  placeholder="123456"
                  outlined
                  filled
                  v-model="paymentID"
                ></v-text-field>
              </v-col>
            </v-row>
          </v-container>

          <v-row style="margintop: -60px" align="center">
            <v-col align="center">
              <Button
                colour="white--text green lighten-1"
                text="Create Payment"
                icon="mdi-file"
                :func="createPayment"
                :hasIcon="true"
              />
            </v-col>
            <v-col align="center">
              <v-dialog transition="dialog-bottom-transition" max-width="800">
                <template v-slot:activator="{ on, attrs }">
                  <v-btn
                    color="primary"
                    v-bind="attrs"
                    v-on="on"
                    depressed
                    x-large
                    @click="getPayment"
                  >
                    <v-icon left>mdi-refresh</v-icon>
                    Check Status</v-btn
                  >
                </template>
                <template v-slot:default="dialog">
                  <v-card>
                    <v-toolbar class="blue-grey darken-4 font-weight-bold" dark
                      >Payment Status</v-toolbar
                    >
                    <v-card-text>
                      <div>
                        <v-row>
                          <v-col>
                            <v-card>
                              <v-card-title
                                class="subheading font-weight-bold mt-6"
                              >
                                {{ bankName }}
                              </v-card-title>

                              <v-divider></v-divider>

                              <v-list dense>
                                <v-list-item>
                                  <v-list-item-content
                                    >Amount:</v-list-item-content
                                  >
                                  <v-list-item-content class="align-end">
                                    {{ paymentAmount }}
                                  </v-list-item-content>
                                </v-list-item>

                                <v-list-item>
                                  <v-list-item-content
                                    >Status:</v-list-item-content
                                  >
                                  <v-list-item-content class="align-end">
                                    {{ status }}
                                  </v-list-item-content>
                                </v-list-item>

                                <v-list-item>
                                  <v-list-item-content
                                    >Currency:</v-list-item-content
                                  >
                                  <v-list-item-content class="align-end">
                                    {{ currency }}
                                  </v-list-item-content>
                                </v-list-item>

                                <v-list-item>
                                  <v-list-item-content
                                    >Creation Date and
                                    Time:</v-list-item-content
                                  >
                                  <v-list-item-content class="align-end">
                                    {{ creationDateTime }}
                                  </v-list-item-content>
                                </v-list-item>

                                <v-list-item v-if="paymentIsScheduled">
                                  <v-list-item-content
                                    >Scheduled Date :</v-list-item-content
                                  >
                                  <v-list-item-content class="align-end">
                                    {{ scheduledDate }}
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
            </v-col>
            <v-col align="center" v-if="paymentIsScheduled">
              <Button
                colour="white--text light-blue darken-4"
                text="Revoke Payment"
                icon="mdi-cancel"
                :hasIcon="true"
                :func="revokePayment"
              />
            </v-col>
          </v-row>

          <v-row justify="space-around">
            <v-col cols="auto"> </v-col>
          </v-row>
        </v-sheet>
      </v-col>
      <v-col cols="12" sm="2">
        <BackButton />
      </v-col>
    </v-row>
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
import Button from "@/components/Buttons/Button.vue";
import BackButton from "@/components/GeneralAppComponents/BackButton.vue";
import axios from "../util/axios.js";
import { mapGetters } from "vuex";

export default {
  name: "MainMenuView",
  components: {
    SheetAppBar,
    Button,
    BackButton,
  },

  data: () => ({
    dialog: true,
    multiLine: true,
    snackbar: false,
    loading: false,
    snackbarMessage: "",
    bankName: "",
    clientId: "",
    refreshToken: "",
    consentID: "",
    paymentID: "",
    paymentIsScheduled: false,
    paymentAmount: 0,
    status: "",
    currency: "",
    creationDateTime: "",
    scheduledDate: "",
    messageText: "",
  }),

  computed: {
    ...mapGetters(["scopes", "clientID", "registrationAccessToken"]),
  },

  methods: {
    async getPayment() {
      this.loading = true;

      let response;
      try {
        response = await axios.get(`/payments/${this.paymentID}`, {
          withCredentials: true,
        });

        this.paymentAmount = response.data.data.payment.amount;
        this.status = response.data.data.status;
        this.currency = response.data.data.payment.currency;
        this.creationDateTime = response.data.data.creationDateTime;
        this.bankName = response.data.selectedBank;
        if (this.paymentIsScheduled) {
          axios
            .get(`/payments/payment-consent/${this.consentID}`, {
              withCredentials: true,
            })
            .then((response) => {
              this.scheduledDate =
                response.data.data.payment.schedule.single.date;
              this.loading = false;
            })
            .catch((error) => {
              if (error.response.status !== 200) {
                this.snackbarMessage = `Error ${error.response.status} - ${error.message}`;
                this.snackbar = true;
                this.loading = false;
              }
            });
        } else {
          this.loading = false;
        }
      } catch (error) {
        if (error.response.status !== 200) {
          this.snackbarMessage = `Error ${error.response.status} - ${error.message}`;
          this.snackbar = true;
          this.loading = false;
        }
      }
    },
    createPayment() {
      this.$router.push({
        name: "payment-detail",
        params: {
          data: this.bankName,
        },
      });
    },
    revokePayment() {
      this.$router.push("patch-detail");
    },
  },

  async created() {
    const selectedDcrOption = this.$route.params.data?.selectedDcrOption;
    this.messageText =
      selectedDcrOption === "USE_EXISTING_CLIENT"
        ? "Obtained the registered client's details successfully"
        : "Dynamic client registration has been done successfully";
    this.clientId = this.$route.params.data?.clientId;

    let response;
    try {
      response = await axios.get("/payments/payment-response", { withCredentials: true });
      if (response.data.clientId) {
        const res = response.data.payload?.payload?.data || response.data.payload?.data;
        this.clientId = response.data?.clientId;
        this.refreshToken = response.data?.refreshToken;
        this.consentID = res?.consentId;
        this.paymentID = res?.paymentId;
        this.paymentIsScheduled = response?.data.scheduled ? true : false;
      }
    } catch (error) {
      this.snackbarMessage = error.message;
      this.snackbar = true;
    }
  },
};
</script>
