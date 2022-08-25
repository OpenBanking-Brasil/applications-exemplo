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
                  :resourceId="selectedConsentId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedConsentId}`"
                  @fetch-data="fetchConsentData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="6">
                <CardComponent
                  title="Consents DELETE"
                  :fullPath="`/open-banking/consents/${ApiVersion}/consents/{consentId}`"
                  :resourceId="selectedConsentId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedConsentId}`"
                  @fetch-data="deleteConsent"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
            </v-row>
            <v-divider class="mt-5 mb-8"></v-divider>
            <v-row>
              <v-col cols="12" sm="4">
                <v-row class="mt-2">
                  <v-card class="mx-auto" max-width="300" tile>
                    <v-subheader>Available Consent IDs</v-subheader>
                    <v-list dense max-height="20vh" style="overflow: auto">
                      <v-list-item-group color="primary">
                        <v-list-item
                          v-for="(consent, i) in this.consentsList"
                          :key="i"
                          @click="
                            () => {
                              setConsent(consent);
                            }
                          "
                        >
                          <v-list-item-content>
                            <v-list-item-title
                              v-text="consent.consent.data.consentId"
                            ></v-list-item-title>
                          </v-list-item-content>
                        </v-list-item>
                      </v-list-item-group>
                    </v-list>
                  </v-card>
                </v-row>
                <v-row class="mt-10">
                  <v-card class="mx-auto" max-width="300" tile>
                    <v-subheader>Selected Consent ID</v-subheader>
                    {{ this.selectedConsentText }}
                  </v-card>
                </v-row>
              </v-col>
              <v-col cols="12" sm="8">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text blue darken-4">Request</v-card-title>
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                        {{ consentsRequestData }}
                    </pre>
                  </v-card-text>
                </v-card>
                 <v-divider class="mt-4"></v-divider>
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
            <v-divider class="mt-5 mb-8"></v-divider>
            <v-row style="margin:-40px" align="center">
              <v-col align="center">
                <Button
                  colour="white--text green lighten-1"
                  text="Create New"
                  icon="mdi-file"
                  :func="createConsent"
                  :hasIcon="true"
                />
              </v-col>
              <v-col align="center">
                <Button
                  colour="primary"
                  text="Select"
                  icon="mdi-check"
                  :func="selectConsentFromList"
                  :hasIcon="true"
                />
              </v-col>
              <v-col align="center">
                <Button
                  colour="white--text orange darken-1"
                  text="Remove"
                  icon="mdi-cancel"
                  :func="removeFromList"
                  :hasIcon="true"
                />
              </v-col>
            </v-row>
          </v-container>
        </v-sheet>
      </v-col>
      <v-col cols="12" sm="2">
        <BackButton path="consent-response-menu" />
      </v-col>
    </v-row>
    <v-overlay :value="loading">
      <v-progress-circular indeterminate size="100"></v-progress-circular>
    </v-overlay>
  </v-main>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import Button from "@/components/Buttons/Button.vue";
import CardComponent from "@/components/GeneralAppComponents/CardComponent.vue";
import BackButton from "@/components/GeneralAppComponents/BackButton.vue";
import axios from "../util/axios.js";
import { mapGetters, mapActions } from "vuex";

export default {
  name: "ConsentsApiMenu",
  components: {
    SheetAppBar,
    Button,
    CardComponent,
    BackButton,
  },
  data() {
    return {
      ApiVersion: "",
      selectedConsentId: "",
      resBannerStyle: "white--text cyan darken-4",
      consentsRequestData: "",
      consentsDataResponse: "",
      selectedConsentText: "",
      loading: false,
    };
  },
  computed: {
    ...mapGetters(["ApiOption", "consentId", "consentsList", "selectedConsent"])
  },
  created() {
    const optionWords = this.ApiOption.split("-");
    this.ApiVersion = optionWords[optionWords.length - 1];
    this.selectedConsentText = this.selectedConsent.consent.data.consentId;
  },
  methods: {
    ...mapActions(["setSelectedConsentFromId", "removeFromConsentsList", "setNewConsent", "updateConsentInConsentsList", "setSelectedConsent"]),
    async fetchConsentData(path) {
      this.loading = true;
      try {
        const response = await axios.get(`consents/${path}`, { withCredentials: true });
        if (response.status == 200) {
          this.consentsRequestData = response.data.requestData;
          this.consentsDataResponse = response.data.responseData;
          this.updateConsentInConsentsList(this.consentsDataResponse);
          if (this.selectedConsentId === this.selectedConsent.consent.data.consentId) {
            await this.selectConsentFromList();
          }
          this.resBannerStyle = "white--text cyan darken-4";
        }
      } catch (error) {
        if (error.response.status != 200) {
          this.consentsRequestData = error.response.data.requestData;
          this.consentsDataResponse = error.response.data.responseData;
          this.resBannerStyle = "white--text red darken-1";
        }
      }
      this.loading = false;
    },

    async deleteConsent(path) {
      this.loading = true;
      try {
        const response = await axios.delete(`consents/${path}`, { withCredentials: true });
        if (response.status == 204) {
          await this.fetchConsentData(path);
        }
      } catch (error) {
        if (error.response.status != 204) {
          this.consentsRequestData = error.response.data.requestData;
          this.consentsDataResponse = error.response.data.responseData;
          this.resBannerStyle = "white--text red darken-1";
        }
      }
      this.loading = false;
    },

    createConsent() {
      this.$router.push("consent-menu");
    },

    async selectConsentFromList() {
      if (this.consentsList.length == 0) { return; }
      this.loading = true;
      const consent = this.consentsList.find(item => item.consent.data.consentId === this.selectedConsentId);
      axios.defaults.withCredentials = true;
      try {
        const response = await axios.post(
          "consent/set-consent",
          {
            consent: consent.consent,
            permissionsData: consent.permissionsData,
            requestData: consent.requestData,
            consentReqObj: consent.consentReqObj,
          },
          {
            headers: {
              "Content-Type": "application/json",
            },
          },
        );
        if (response.status == 201) {
          this.setSelectedConsentFromId(this.selectedConsentId);
          this.setNewConsent(false);
          this.selectedConsentText = this.selectedConsentId;
        }
      } catch (error) {
        this.selectedConsentText = "";
      }
      this.loading = false;
    },

    async removeFromList() {
      if (this.consentsList.length == 0) { return; }
      this.loading = true;
      const consent = this.consentsList.find(item => item.consent.data.consentId === this.selectedConsentId);
      if (consent.consent.data.status != "REJECTED") {
        await this.deleteConsent(this.selectedConsentId);
      } else {
        await this.fetchConsentData(this.selectedConsentId);
      }
      this.removeFromConsentsList(this.selectedConsentId);
      if (this.selectedConsent.consent.data.consentId == this.selectedConsentId) {
        // If there still is a consent in the list, choose the next
        if (this.consentsList.length > 0) {
          this.selectedConsentId = this.consentsList[0].consent.data.consentId;
          this.selectedConsentText = this.selectedConsentId;
          await this.selectConsentFromList();
        } else {
          this.setSelectedConsent(null);
          this.setNewConsent(false);
          this.selectedConsentText = "";
          this.selectedConsentId = "";
          axios.defaults.withCredentials = true;
          try {
            await axios.post(
              "consent/set-consent",
              {
                consent: null,
                permissionsData: null,
                requestData: null,
                consentReqObj: null,
              },
              {
                headers: {
                  "Content-Type": "application/json",
                },
              },
            );
          } catch (error) {
            console.log(error);
          }
        }
      }
      this.loading = false;
    },

    setConsent(consent) {
      this.selectedConsentId = consent.consent.data.consentId;
    },

    changeResourceId(consentId) {
      this.selectedConsentId = consentId;
    },
  },
};
</script>
