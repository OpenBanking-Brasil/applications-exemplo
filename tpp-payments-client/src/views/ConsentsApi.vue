<template>
  <CardWrapper title="">
    <template v-slot:card-content>
      <v-col cols="12" md="6">
        <CardComponent 
          title="Consents GET" 
          :fullPath="`/open-banking/consents/${ApiVersion}/consents/{consentId}`"
          :resourceId="selectedConsentId" 
          :path="`${selectedConsentId}`"
          @fetch-data="fetchConsentData" 
          @resource-id-change="changeResourceId" />
      </v-col>
      <v-col cols="12" md="6">
        <CardComponent 
          title="Consents DELETE"
          :fullPath="`/open-banking/consents/${ApiVersion}/consents/{consentId}`" 
          :resourceId="selectedConsentId"
          :path="`${selectedConsentId}`" 
          @fetch-data="deleteConsent"
          @resource-id-change="changeResourceId" />
      </v-col>
    </template>
    <template v-slot:content>
      <v-card elevation="0" class="pa-0">
        <v-card-title class="px-0 pt-0 pb-5">Available Consent IDs</v-card-title>
        <v-list dense max-height="20vh" style="overflow: auto">
          <v-list-item-group color="primary">
            <v-list-item v-for="(consent, i) in consentsList" :key="i" @click="setConsent(consent)">
              <v-list-item-content>
                <v-list-item-title v-text="consent.consent.data.consentId"></v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-card>

      <v-card elevation="0" class="pa-0 mt-2">
        <v-card-title class="px-0 pt-0 pb-5">Selected Consent ID</v-card-title>
        {{ selectedConsentText }}
      </v-card>
      <CardCode 
        class="mt-10" 
        color="lightblue" 
        title="Request" 
        :code="consentsRequestData" />
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Response" 
        :code="consentsDataResponse" 
        :is-error="isFetchConsentDataError"/>
      <v-divider class="mt-5 mb-8"></v-divider>
      <v-row align="center" >
        <v-col align="center" sm="12" md="4">
          <Button colour="white--text green lighten-1" text="Create New" icon="mdi-file" :func="createConsent"
            :hasIcon="true" />
        </v-col>
        <v-col align="center" sm="12" md="4">
          <Button colour="primary" text="Select" icon="mdi-check" :func="selectConsentFromList" :hasIcon="true" />
        </v-col>
        <v-col align="center" sm="12" md="4">
          <Button colour="white--text orange darken-1" text="Delete" icon="mdi-cancel" :func="removeFromList"
            :hasIcon="true" />
        </v-col>
      </v-row>
    </template>
  </CardWrapper>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import Button from "@/components/Buttons/Button.vue";
import CardCode from "@/components/Shared/CardCode.vue";
import CardComponent from "@/components/Shared/CardComponent.vue";
import CardWrapper from "@/components/Shared/CardWrapper.vue";

import axios from "@/util/axios.js";

import { mapGetters, mapActions } from "vuex";

export default {
  name: "ConsentsApiMenu",
  components: {
    SheetAppBar,
    Button,
    CardComponent,
    CardCode,
    CardWrapper,
  },
  data() {
    return {
      ApiVersion: "",
      selectedConsentId: "",
      isFetchConsentDataError: false,
      consentsRequestData: "",
      consentsDataResponse: "",
      selectedConsentText: "",
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
    ...mapActions(["setLoading", "setError", "setSelectedConsentFromId", "removeFromConsentsList", "setNewConsent", "updateConsentInConsentsList", "setSelectedConsent"]),
    async fetchConsentData(path) {
      this.setLoading(true);
      try {
        const response = await axios.get(`consents/${path}`, { withCredentials: true });
        if (response.status == 200) {
          this.consentsRequestData = response.data.requestData;
          this.consentsDataResponse = response.data.responseData;
          this.updateConsentInConsentsList(this.consentsDataResponse);
          if (this.selectedConsentId === this.selectedConsent.consent.data.consentId) {
            await this.selectConsentFromList();
          }
          this.isFetchConsentDataError = false;
        }
      } catch (error) {
        this.setError(error.message);
        this.isFetchConsentDataError = true;
        if (error.response.status != 200) {
          this.consentsRequestData = error.response.data.requestData;
          this.consentsDataResponse = error.response.data.responseData;
        }
      }
      this.setLoading(false);
    },

    async deleteConsent(path) {
      this.setLoading(true);
      try {
        const response = await axios.delete(`consents/${path}`, { withCredentials: true });
        if (response.status == 204) {
          await this.fetchConsentData(path);
        }
      } catch (error) {
        this.setError(error.message);
        if (error.response.status != 204) {
          this.consentsRequestData = error.response.data.requestData;
          this.consentsDataResponse = error.response.data.responseData;
        }
      }
      this.setLoading(false);
    },

    createConsent() {
      this.$router.push("consent-menu");
    },

    async selectConsentFromList() {
      if (this.consentsList.length == 0) { return; }
      this.setLoading(true);
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
        this.setError(error.message);
        this.selectedConsentText = "";
      }
      this.setLoading(false);
    },

    async removeFromList() {
      if (this.consentsList.length == 0) { return; }
      this.setLoading(true);
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
            this.setError(error.message);
          }
        }
      }
      this.setLoading(false);
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
