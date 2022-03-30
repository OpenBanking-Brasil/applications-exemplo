<template>
  <v-main class="payment-response">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Payment Response" />
        <v-sheet min-height="70vh" rounded="lg">
          <v-container class="pa-md-12">
            <div class="pa-2"></div>
            <v-card elevation="2" outlined color="">
              <v-card-title style="color: white; background-color: #004c50"
                >Information</v-card-title
              >
              <v-card-text>
                <v-row class="pt-6">
                  <v-col cols="12" sm="4" md="4">
                    <b>Payment Value</b>
                    <v-text-field
                      v-model="amount"
                      class="text-green"
                      placeholder="1335.0"
                      outlined
                      filled
                    ></v-text-field>
                  </v-col>
                  <v-col cols="12" sm="4" md="4">
                    <b>Final Payment Status</b>
                    <v-text-field
                    v-model="status"
                      placeholder="ACCC"
                      outlined
                      filled
                    ></v-text-field>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>
            <div class="pa-2"></div>
            <v-card elevation="2" outlined color="" v-if="consentResponse">
              <v-card-title style="color: white; background-color: #9ccc65"
                >Consent Response Payload</v-card-title
              >
              <v-card-text>
                <pre class="pt-4" style="overflow: auto">
                  {{ consentResponse.stringify }}
                </pre>
              </v-card-text>
            </v-card>
            <div class="pa-2"></div>
            <v-card elevation="2" outlined color="" v-if="paymentResponse">
              <v-card-title style="color: white; background-color: #3949ab"
                >Payment Response Payload</v-card-title
              >
              <v-card-text>
                <pre class="pt-4" style="overflow: auto">
                  {{ paymentResponse.stringify }}
                </pre>
              </v-card-text>
            </v-card>
            <div class="pa-2"></div>
            <v-card elevation="2" outlined color="" v-if="errorResponse">
              <v-card-title style="color: white; background-color: #ff5252"
                >Error Response Payload</v-card-title
              >
              <v-card-text>
                <pre class="pt-4" style="overflow: auto" v-if="errorResponse">
                  {{ errorResponse }}
                </pre>
              </v-card-text>
            </v-card>
          </v-container>
        </v-sheet>
      </v-col>

      <v-col cols="12" sm="2">
        <BackButton />
      </v-col>
    </v-row>
  </v-main>
</template>

<script>
// @ is an alias to /src

import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import BackButton from "@/components/GeneralAppComponents/BackButton.vue";
import axios from "../util/axios.js";

export default {
  name: "PaymentResponseView",
  components: {
    SheetAppBar,
    BackButton,
  },

  data: () => ({
    consentResponse: "",
    paymentResponse: "",
    errorResponse: "",
    amount: null,
    status: ""
  }),

  created(){
        axios
      .get("/payment-response-data", {withCredentials: true})
      .then((response) => {
        if (response.data.consentPayload) {
          this.consentResponse = response.data.consentPayload;
          this.paymentResponse = response.data.payload;
          this.amount = response.data.payload.data.payment.amount;
          this.status = response.data.payload.data.status;

        } else {
          this.errorResponse = response.data.errorPayload;
        }
      });
  },
  mounted(){
    console.log("h", this.amount)
  } 
};
</script>
