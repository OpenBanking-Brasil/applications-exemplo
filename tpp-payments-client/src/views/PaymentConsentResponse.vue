<template>
  <CardWrapper title="">
    <template v-slot:content>
      <CardCode 
          color="lightblue" 
          title="Consent Request" 
          :code="requestData" />
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Consent Response Payload" 
        :code="consentResponse" />
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
  name: "PaymentConsentResponse",
  components: {
    SheetAppBar,
    CardCode,
    CardWrapper,
  },

  data: () => ({
    multiLine: true,
    requestData: "",
    consentResponse: "",
    primaryResBannerStyle: "white--text cyan darken-4",
    consent: "",
  }),

  methods: {
    ...mapActions(["setError", "setLoading", "setConsent"]),
  },

  async created() {
    try {
      this.setLoading(true);

      const response = await axios.get("/payments/payment-consent-response", { withCredentials: true });
      this.consentResponse = response.data.consentPayload;
      this.requestData = response.data.requestData;
      this.consent = response.data.consentPayload;
      this.setConsent(this.consent);
      
      this.setLoading(false);
    } catch (error) {
      this.setError(error.message);
    }
  },
};
</script>
