<template>
  <div class="payment-consent layout-wrapper">
    <v-form @submit.prevent="createConsent">
      <v-sheet min-height="70vh">
        <v-card
          elevation="0"
          class="pa-0"
          v-for="card in formItems"
        >
          <v-card-title class="px-0 pt-0 pb-5">
            {{ card.title }}
          </v-card-title>

          <v-card-text class="px-0">
            <v-row class="pa-0">
              <v-col
                v-for="item in card.items"
                :cols="item.cols"
                :md="item.cols"
                sm="12"
                :class="{ 'scheduled-switcher': item.type === 'select' }"
              >
                <div
                  v-if="[undefined, 'select'].includes(item.type) || (formDataObj.selected === 'Yes' && item.type === 'dataPicker')"
                  class="app-label-holder d-flex justify-space-between"
                  :style="{ 'flex-direction': item.type === 'select' ? 'row-reverse' : 'row'}"
                >
                  <span>{{ item.label }}</span>

                  <v-icon
                    small
                    :title="item.iconTitle"
                    color="secondary"
                  >mdi-help-circle</v-icon>
                </div>

                <v-text-field
                  v-if="item.type === undefined"
                  :placeholder="item.placeholder"
                  outlined
                  dense
                  v-model="formDataObj[item.value]"
                  :name="item.name"
                  :id="item.id"
                />

                <v-switch
                  v-else-if="item.type === 'select'"
                  v-model="formDataObj.selected"
                  inset
                  :false-value="item.items[1]"
                  :true-value="item.items[0]"
                  :name="item.name"
                  :id="item.id"
                  :label="formDataObj.selected"
                  class="mt-0 pt-0"
                />

                <date-picker
                  v-else-if="formDataObj.selected === 'Yes' && item.type === 'dataPicker'"
                  v-model="formDataObj.date"
                />
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>

        <v-row class="layout-wrapper__bottom-btns">
          <v-col class="pa-0">
            <v-btn depressed block text height="57" class="consent-create-btn" type="submit">
              <v-icon left> mdi-file-document-plus-outline </v-icon>
              <span>Create Consent</span>
            </v-btn>
          </v-col>
        </v-row>
      </v-sheet>
    </v-form>
  </div>
</template>

<script>
import axios from "@/util/axios.js";
import { mapActions } from "vuex";
import DatePicker from "@/components/Shared/DatePicker.vue";

import { formItems } from "@/config/paymentConsent.js";


export default {
  name: "PaymentConsent",

  components: {
    DatePicker,
  },

  data() {
    return {
      formItems,
      messageText: "Payment schedule date must be in the future",
      modal: false,
      bankName: "",
      today: null,
      formDataObj: {
        selected: "No",
        date: null,
        debtorAccount_number: "94088392",
        debtorAccount_accountType: "CACC",
        debtorAccount_ispb: "12345678",
        debtorAccount_issuer: "6272",
        loggedUser_document_identification: "76109277673",
        loggedUser_document_rel: "CPF",
        creditor_name: "Marco Antonio de Brito",
        creditor_cpfCnpj: "48847377765",
        creditor_personType: "PESSOA_NATURAL",
        payment_amount: "1335.00",
        payment_type: "PIX",
        payment_details_proxy: "12345678901",
        payment_details_localInstrument: "DICT",
        payment_details_creditAccount_number: "1234567890",
        payment_details_creditAccount_accountType: "CACC",
        payment_details_creditAccount_ispb: "12345678",
        payment_details_creditAccount_issuer: "1774",
      },
    };
  },

  created() {
    this.today = this.getTodaysDate;
    this.formDataObj.date = this.getTodaysDate;
    this.bankName = this.$route.params.data;
  },

  computed: {
    getTodaysDate(){
      const todaysDate = new Date(Date.now() - new Date().getTimezoneOffset() * 60000)
      .toISOString()
      .substr(0, 10);

      return todaysDate;
    }
  },

  methods: {
    ...mapActions(["setError", "setInfo", "setLoading"]),

    async createConsent() {
      this.setLoading(true);
      if (
        this.formDataObj.selected === "Yes" &&
        this.formDataObj.date.toString() === this.today.toString()
      ) {
        this.setInfo(this.messageText);
        return;
      }

      let formBody = [];
      for (let property in this.formDataObj) {
        const encodedKey = encodeURIComponent(property);
        const encodedValue = encodeURIComponent(this.formDataObj[property]);
        formBody.push(encodedKey + "=" + encodedValue);
      }
      formBody = formBody.join("&");
      axios.defaults.withCredentials = true;

      let bankConsent = window.open("", "_self");

      let response;
      try {
        response = await axios.post("/payments/payment-consent", formBody, {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
            "Access-Control-Allow-Origin": "*",

          },
        });

        if (response.status === 201) {
          bankConsent.location.href = response.data.authUrl;
        }
      } catch (error) {
        this.setLoading(false);
        this.setError(`Error ${error?.response.status} - ${error.message}`);
      }
    },
  },
};
</script>