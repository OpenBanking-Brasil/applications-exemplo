<template>
  <CardWrapper>
    <template v-slot:card-content>
      <v-col cols="6" sm="12" md="6">
        <div class="app-label-holder d-flex justify-space-between">
          <span>Payment Value</span>
        </div>
        <v-text-field v-model="amount" class="text-green" placeholder="1335.0" outlined dense></v-text-field>
      </v-col>
      <v-col cols="6" sm="12" md="6">
        <div class="app-label-holder d-flex justify-space-between">
          <span>Final Payment Status</span>
        </div>
        <v-text-field v-model="status" placeholder="ACCC" outlined dense></v-text-field>
      </v-col>
    </template>
    <template v-slot:content>
      <CardCode 
        v-if="consentResponse" 
        class="mt-10" 
        color="lightgreen" 
        title="Consent Response Payload" 
        :code="consentResponse" />
      <CardCode 
        v-if="paymentResponse" 
        class="mt-10" 
        color="lightgreen" 
        title="Payment Response Payload" 
        :code="paymentResponse"/>
      <CardCode 
        v-if="errorResponse" 
        class="mt-10" 
        color="lightred" 
        title="Error Response Payload" 
        :code="errorResponse"/>
    </template>
  </CardWrapper>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import CardCode from "@/components/Shared/CardCode.vue";
import CardWrapper from "@/components/Shared/CardWrapper.vue";

import axios from "@/util/axios.js";

import { mapActions } from "vuex";

export default {
  name: "PaymentResponseView",
  components: {
    SheetAppBar,
    CardCode,
    CardWrapper,
  },

  data: () => ({
    consentResponse: "",
    paymentResponse: "",
    errorResponse: "",
    amount: null,
    status: "",
  }),

  methods: {
    ...mapActions(["setError", "setLoading"])
  },
  async created() {
    let response;
    try {
      this.setLoading(true);
      response = await axios.get("/payments/payment-response", { withCredentials: true });
      const res = response.data.payload?.payload || response.data.payload;
      if (!response.data.errorPayload) {
        this.consentResponse = response.data.consentPayload;
        this.paymentResponse = response.data.payload;
        this.amount = res.data.payment.amount;
        this.status = res.data.status;
        this.setLoading(false);
      } else {
        //payment error response
        this.errorResponse = response.data.errorPayload;
        this.consentResponse = response.data.consentPayload;

        this.amount = response.data.consentPayload.data.payment.amount;
        this.status = response.data.payload.data.status;
      }
    } catch (error) {
      this.setError(error.message);
    }
  },
};
</script>
