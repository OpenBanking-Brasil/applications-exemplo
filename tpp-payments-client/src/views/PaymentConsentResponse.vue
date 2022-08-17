<template>
  <v-main class="payment-consent-response">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Payment Consent Response" />
        <v-sheet min-height="70vh" rounded="lg" v-if="!loading">
          <v-container class="pa-md-12">
            <div class="pa-2"></div>
            <v-card elevation="2" outlined color="">
              <v-card-title class="white--text blue darken-4"
                >Consent Request</v-card-title
              >
              <v-card-text>
                <pre class="pt-4" style="overflow: auto">
                  {{ requestData }}
                </pre>
              </v-card-text>
            </v-card>
            <div class="pa-2"></div>
            <v-card elevation="2" outlined color="">
              <v-card-title class="white--text cyan darken-4"
                >Consent Response Payload</v-card-title
              >
              <v-card-text>
                <pre class="pt-4" style="overflow: auto">
                  {{ consentResponse }}
                </pre>
              </v-card-text>
            </v-card>
            <div class="pa-2"></div>
            <v-snackbar v-model="snackbar" :multi-line="multiLine" color="red accent-2">
              {{ errorMessage }}

              <template v-slot:action="{ attrs }">
                <v-btn color="white" text v-bind="attrs" @click="snackbar = false">
                  Close
                </v-btn>
              </template>
            </v-snackbar>
          </v-container>
        </v-sheet>
      </v-col>

      <v-col cols="12" sm="2">
        <BackButton path="payment-menu" />
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
import { mapActions } from "vuex";

export default {
  name: "PaymentConsentResponse",
  components: {
    SheetAppBar,
    BackButton,
  },

  data: () => ({
    multiLine: true,
    snackbar: false,
    errorMessage: "",
    requestData: "",
    consentResponse: "",
    primaryResBannerStyle: "white--text cyan darken-4",
    consent: "",
    loading: true,
  }),

  methods: {
    ...mapActions(["setConsent"]),
  },

  async created() {
    try {
      const response = await axios.get("/payments/payment-consent-response", { withCredentials: true });
      this.consentResponse = response.data.consentPayload;
      this.requestData = response.data.requestData;
      this.consent = response.data.consentPayload;
      this.setConsent(this.consent);
      this.loading = false;
    } catch (error) {
      this.errorMessage = error.message;
      this.snackbar = true;
    }
  },
};
</script>
