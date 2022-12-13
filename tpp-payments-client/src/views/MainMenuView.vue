<template>
  <div class="main-menu layout-wrapper">
    <v-form>
      <v-sheet>
        <v-card elevation="0" class="pa-0">
          <client-details-dialog
            v-if="$route.params.data"
            :messageText="messageText"
          />

          <v-row>
            <v-col cols="6" >
              <b class="app-label-holder">Client ID</b>
              <v-text-field
                placeholder="123456"
                outlined
                v-model="clientID"
              ></v-text-field>
            </v-col>

            <v-col cols="6" >
              <b class="app-label-holder">Refresh Token</b>
              <v-text-field
                placeholder="123456"
                outlined
                v-model="refreshToken"
              ></v-text-field>
            </v-col>
          </v-row>

          <v-row>
            <v-col cols="6" >
              <b class="app-label-holder">Consent ID</b>
              <v-text-field
                placeholder="123456"
                outlined
                v-model="consentID"
              ></v-text-field>
            </v-col>

            <v-col cols="6" >
              <b class="app-label-holder">Payment ID</b>
              <v-text-field
                placeholder="123456"
                outlined
                v-model="paymentID"
              ></v-text-field>
            </v-col>
          </v-row>
        </v-card>

        <v-row class="layout-wrapper__bottom-btns mt-4">
          <v-col cols="6" class="pa-0">
            <v-btn
              depressed
              block
              text
              height="57"
              class="rounded-0"
              @click="createConsent"
            >
              <v-icon left> mdi-file-document-plus-outline </v-icon>
              <span>Create Consent</span>
            </v-btn>
          </v-col>

          <v-col cols="6" class="pa-0">
            <v-btn
              depressed
              block
              text
              height="57"
              class="rounded-0"
              :disabled="!paymentIsScheduled"
              @click="revokePayment"
            >
              <v-icon left> mdi-cancel </v-icon>
              <span>Revoke Payment</span>
            </v-btn>
          </v-col>
        </v-row>

        <v-row class="layout-wrapper__bottom-btns mt-0">
          <v-col cols="6" class="pa-0">
            <payment-status-dialog
              :loading="loading"
              :scheduledDate="scheduledDate"
              :creationDateTime="creationDateTime"
              :currency="currency"
              :status="status"
              :paymentAmount="paymentAmount"
              :bankName="bankName"
              :paymentIsScheduled="paymentIsScheduled"
              @getPayment="getPayment"
            />
          </v-col>
          
          <v-col cols="6" class="pa-0">
            <v-btn depressed block text height="57" @click="createPayment">
              <v-icon left> mdi-file-outline </v-icon>
              <span>Create Payment</span>
            </v-btn>
          </v-col>
        </v-row>
      </v-sheet>
    </v-form>
  </div>
</template>

<script>
import Button from "@/components/Buttons/Button.vue";
import PaymentStatusDialog from "@/components/Dialogs/PaymentStatusDialog";
import ClientDetailsDialog from "@/components/Dialogs/ClientDetailsDialog";
import axios from "@/util/axios.js";
import { mapGetters, mapActions} from "vuex";

export default {
  name: "MainMenuView",
  components: {
    Button,
    PaymentStatusDialog,
    ClientDetailsDialog,
  },

  data: () => ({
    loading: false,
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
    ...mapActions(["setError", "setInfo"]),

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
                this.setError(`Error ${error.response.status} - ${error.message}`);
                this.loading = false;
              }
            });
        } else {
          this.loading = false;
        }
      } catch (error) {
        if (error.response.status !== 200) {
          this.setError(`Error ${error.response.status} - ${error.message}`);
          this.loading = false;
        }
      }
    },
    createConsent() {
      this.$router.push({
        name: "payment-consent",
        params: {
          data: this.bankName,
        },
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
      this.setError(error.message);
    }
  },
};
</script>
