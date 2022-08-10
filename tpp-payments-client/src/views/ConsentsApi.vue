<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Consents" />
        
        <v-sheet min-height="70vh" rounded="lg">
          <v-container class="pa-md-12">
            <v-row>
              <v-col cols="12" sm="6">
                <CardComponent
                  title="Consents GET"
                  :fullPath="`/open-banking/consents/${ApiVersion}/consents/{consentId}`"
                  :resourceId="consentId"
                  :displayTextField="false"
                  btnText="RUN"
                  @fetch-data="fetchConsentData"
                />
              </v-col>
              <v-col cols="12" sm="6">
                <CardComponent
                  title="Consents DELETE"
                  :fullPath="`/open-banking/consents/${ApiVersion}/consents/{consentId}`"
                  :resourceId="consentId"
                  :displayTextField="false"
                  btnText="RUN"
                  @fetch-data="deleteConsent"
                />
              </v-col>
            </v-row>
            <v-divider class="mt-5 mb-8"></v-divider>
            <v-row>
              <v-col cols="12" sm="12">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text blue darken-4"
                    >Request</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                        {{ consentsRequestData }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" sm="12">
                <v-card elevation="2" outlined>
                  <v-card-title :class="resBannerStyle">Response</v-card-title>
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                        {{ consentsDataResponse }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
          </v-container>
        </v-sheet>
      </v-col>
      <v-col cols="12" sm="2">
        <BackButton path="consent-response-menu" />
      </v-col>
    </v-row>
  </v-main>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import CardComponent from "@/components/GeneralAppComponents/CardComponent.vue";
import BackButton from "@/components/GeneralAppComponents/BackButton.vue";
import axios from "../util/axios.js";
import { mapGetters } from "vuex";

export default {
  name: "ConsentsApiMenu",
  components: {
    SheetAppBar,
    CardComponent,
    BackButton,
  },
  data() {
    return {
      ApiVersion: "",
      resBannerStyle: "white--text cyan darken-4",
      consentsRequestData: "",
      consentsDataResponse: "",
    };
  },
  computed: {
    ...mapGetters(["ApiOption", "consentId"])
  },
  created() {
    const optionWords = this.ApiOption.split("-");
    this.ApiVersion = optionWords[optionWords.length - 1];
  },
  methods: {
    async fetchConsentData() {
      try {
        const response = await axios.get(`consents/${this.consentId}`, { withCredentials: true });
        if (response.status == 200) {
          this.consentsRequestData = response.data.requestData;
          this.consentsDataResponse = response.data.responseData;
          this.resBannerStyle = "white--text cyan darken-4";
        }
      } catch (error) {
        if (error.response.status != 200) {
          this.consentsRequestData = error.response.data.requestData;
          this.consentsDataResponse = error.response.data.responseData;
          this.resBannerStyle = "white--text red darken-1";
        }
      }
    },

    async deleteConsent() {
      try {
        const response = await axios.delete(`consents/${this.consentId}`, { withCredentials: true });
        if (response.status == 204) {
          this.consentsRequestData = `${this.consentId} deleted succesfully`;
          this.consentsDataResponse = "";
          this.resBannerStyle = "white--text cyan darken-4";
        }
      } catch (error) {
        if (error.response.status != 204) {
          this.consentsRequestData = error.response.data.requestData;
          this.consentsDataResponse = error.response.data.responseData;
          this.resBannerStyle = "white--text red darken-1";
        }
      }
    },
  },
};
</script>
