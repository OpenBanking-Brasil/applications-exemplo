<template>
  <div class="payment-details layout-wrapper">
    <v-form @submit.prevent="createPayment">
      <v-sheet>
        <v-card elevation="0" class="pa-0">
          <v-card-text class="pa-0">
            <v-row class="pa-0">
              <v-col>
                <div  class="app-label-holder d-flex justify-space-between">
                  <span>Available Consent IDs</span>
                </div>
                <v-select
                  :items="consents"
                  placeholder="Choose consent ID"
                  dense
                  outlined
                  return-object
                  item-text="consentId"
                  :value="selectedConsent"
                  @input="setConsent"
                />
              </v-col>
            </v-row>

            <v-row>
              <v-col
                v-for="item in formItems"
                cols="6"
              >
                <div class="app-label-holder d-flex justify-space-between">
                  <span>{{ item.label }}</span>

                  <v-icon
                    small
                    :title="item.iconTitle"
                    color="rgba(57, 75, 101, 0.2)"
                  >mdi-help-circle</v-icon>
                </div>

                <v-text-field
                  :placeholder="item.placeholder"
                  outlined
                  dense
                  v-model="formDataObj[item.value]"
                  :name="item.name"
                  :id="item.id"
                />
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>

        <v-row class="layout-wrapper__bottom-btns mt-4">
          <v-col class="pa-0">
            <v-btn depressed block text height="57" class="consent-create-btn" type="submit">
              <v-icon left>mdi-file</v-icon>
              <span>Create Payment</span>
            </v-btn>
          </v-col>
        </v-row>
      </v-sheet>
    </v-form>
  </div>
</template>

<script>
import axios from "@/util/axios.js";
import { formItems } from "@/config/PaymentDetails.js";

import { mapGetters, mapActions } from "vuex";

export default {
  name: "PaymentDetail",
  components: {
  },
  data: () => ({
    formItems,
    bankName: "",
    consents: [],
    selectedConsent: null,
    formDataObj: {
      payment_amount: "",
      payment_type: "",
      payment_details_proxy: "",
      payment_details_localInstrument: "",
      payment_details_creditAccount_number: "",
      payment_details_creditAccount_accountType: "",
      payment_details_creditAccount_ispb: "",
      payment_details_creditAccount_issuer: "",
    },
  }),

  computed: {
    ...mapGetters(["consent"])
  },

  created() {
    this.bankName = this.$route.params.data;

    if(this.consent) {
      this.consents.push(this.consent.data.consentId);
    }
  },

  methods: {
    ...mapActions(["setError", "setLoading"]),

    async createPayment() {
      axios.defaults.withCredentials = true;
      try {
        this.setLoading(true);
        await axios.post (
          "/payments/make-payment",
          { 
            ...this.formDataObj,
            consentPayload: this.selectedConsent,
            bank: this.bankName,
          },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );
        this.setLoading(false);
        this.$router.push("payment-response");
      } catch(error) {
        this.setError(error);
      }
    },

    setConsent() {
      if(!this.consent.data.payment) return;
      this.formDataObj = {
        payment_amount: this.consent.data.payment.amount,
        payment_type: this.consent.data.payment.type,
        payment_details_proxy: this.consent.data.payment.details.proxy,
        payment_details_localInstrument: this.consent.data.payment.details.localInstrument,
        payment_details_creditAccount_number: this.consent.data.payment.details.creditorAccount.number,
        payment_details_creditAccount_accountType: this.consent.data.payment.details.creditorAccount.accountType,
        payment_details_creditAccount_ispb: this.consent.data.payment.details.creditorAccount.ispb,
        payment_details_creditAccount_issuer: this.consent.data.payment.details.creditorAccount.issuer,
      };
    },
  },
};
</script>
