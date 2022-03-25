<template>
  <v-main class="main">
    <v-row>
      <v-col> </v-col>
      <v-col :cols="7">
        <v-sheet
          min-height="70vh"
          elevation="20"
          rounded="lg"
          class="grey lighten-3"
        >
          <SheetAppBar header="Mock TPP" />
          <v-container class="pa-md-12">
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
                                Mock Bank
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
                                    >Creation Date and Time:</v-list-item-content
                                  >
                                  <v-list-item-content class="align-end">
                                   {{ creationDateTime }}
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
            <v-col align="center">
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
      <v-col> </v-col>
    </v-row>
  </v-main>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import Button from "@/components/Buttons/Button.vue";
import axios from "../util/axios.js";

export default {
  name: "MainMenuView",
  components: {
    SheetAppBar,
    Button,
  },

  data: () => ({
    bankName: "",
    clientID: "",
    refreshToken: "",
    consentID: "",
    paymentID: "",

    paymentAmount: 0,
    status: "",
    currency: "",
    creationDateTime: ""
  }),

  methods: {
    getPayment() {
      axios
        .get(`/payment/${this.paymentID}`, {
          withCredentials: true,
        })
        .then((response) => {
          this.paymentAmount = response.data.data.payment.amount;
          this.status = response.data.data.status;
          this.currency = response.data.data.payment.currency;
          this.creationDateTime = response.data.data.creationDateTime;
        });
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

  created() {
    this.bankName = this.$route.params.data;
    axios
      .get("/payment-response-data", {
        withCredentials: true,
      })
      .then((response) => {
        if (response.data.clientId) {
          this.clientID = response.data.clientId;
          this.refreshToken = response.data.refreshToken;
          this.consentID = response.data.payload.data.consentId;
          this.paymentID = response.data.payload.data.paymentId;
        }
      });
  },
};
</script>
