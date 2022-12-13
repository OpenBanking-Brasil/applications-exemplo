<template>
  <CardWrapper title="Add Query Parameters">
    <template v-slot:card-content>
      <v-row class="pa-0 align-end">
        <v-col cols="6" sm="12" md="6">
          <v-text-field label="Page Size" v-model="financingsQueryParams['page-size']" outlined></v-text-field>
        </v-col>
        <v-col cols="6" sm="12" md="6">
          <v-text-field label="Page" outlined v-model="financingsQueryParams['page']"></v-text-field>
        </v-col>
        <v-col cols="12" sm="12" md="12" v-if="ApiVersion === 'v2'">
          <v-text-field label="Pagination Key" outlined v-model="financingsQueryParams['pagination-key']">
          </v-text-field>
        </v-col>
        <v-col cols="6" sm="12" md="3" class="mx-auto">
          <v-btn depressed height="2.5rem" width="100%" color="primary" @click="getFinancingsByQueryParams">
            Run
          </v-btn>
        </v-col>
      </v-row>
    </template>
    <template v-slot:content>
      <CardCode 
        class="mt-8" 
        color="lightblue" 
        title="Financings API Request" 
        :code="financingsRequest" />
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Financings API Response" 
        :code="financingsResponse"
        :is-error="isFinancingError" />
      <v-row class="mt-8">
        <v-col cols="12" md="6">
          <CardComponent 
            title="Financing API"
            :fullPath="`/open-banking/financings/${ApiVersion}/contracts/{contractId}`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}`"
            @fetch-data="fetchFinancingData" 
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Financing Warranties API"
            :fullPath="`/open-banking/financings/${ApiVersion}/contracts/{contractId}/warranties`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}/warranties`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="financingGenericQueryParams"
            flag="CREDIT_OPERATION" 
            :ApiVersion="ApiVersion" 
            @fetch-data="fetchFinancingData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Financings Scheduled Instalments API"
            :fullPath="`/open-banking/financings/${ApiVersion}/contracts/{contractId}/scheduled-instalments`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}/scheduled-instalments`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="financingGenericQueryParams"
            flag="CREDIT_OPERATION" 
            :ApiVersion="ApiVersion" 
            @fetch-data="fetchFinancingData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Financings Payments API"
            :fullPath="`/open-banking/financings/${ApiVersion}/contracts/{contractId}/payments`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}/payments`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="financingGenericQueryParams"
            flag="CREDIT_OPERATION" 
            :ApiVersion="ApiVersion" 
            @fetch-data="fetchFinancingData"
            @resource-id-change="changeResourceId" />
        </v-col>
      </v-row>
      <div class="pa-2"></div>
      <v-divider class="mt-5 mb-8"></v-divider>
      <v-card elevation="0" class="pa-0">
        <v-card-title class="px-0 pt-0 pb-5">Available Contract IDs</v-card-title>
        <v-list dense max-height="20vh" style="overflow: auto">
          <v-list-item-group color="primary">
            <v-list-item v-for="(contractId, i) in contractIDs" :key="i" @click="setContractId(contractId)">
              <v-list-item-content>
                <v-list-item-title v-text="contractId"></v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-card>
      <CardCode 
        class="mt-10" 
        color="lightblue" 
        title="Request" 
        :code="financingRequest" />
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Response" 
        :code="financingResponse"
        :is-error="isFetchFinancingDataError" />
    </template>
  </CardWrapper>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import CardComponent from "@/components/Shared/CardComponent.vue";
import CardCode from "@/components/Shared/CardCode.vue";
import CardWrapper from "@/components/Shared/CardWrapper.vue";

import axios from "@/util/axios.js";
import { getPathWithQueryParams } from "@/util/helpers.js";

import { mapGetters, mapActions } from "vuex";

export default {
  name: "FinancingsMenu",
  components: {
    SheetAppBar,
    CardComponent,
    CardCode,
    CardWrapper,
  },
  data() {
    return {
      getPathWithQueryParams,
      ApiVersion: "",
      financingsResponse: "",
      financingsRequest: "",
      financingRequest: "",
      contractIDs: [],
      selectedContractId: "",
      financingResponse: "",
      isFinancingError: false,
      isFetchFinancingDataError: false,
      financingsQueryParams: {
        "page-size": null,
        page: null,
        "pagination-key": null,
      },
      financingGenericQueryParams: {
        "page-size": null,
        page: null,
        "pagination-key": null,
      },
    };
  },
  created() {
    const optionWords = this.ApiOption.split("-");
    this.ApiVersion = optionWords[optionWords.length - 1];
    this.getFinancings();
  },
  computed: {
    ...mapGetters(["ApiOption"]),
  },
  methods: {
    ...mapActions(["setError", "setLoading"]),

    getFinancingsByQueryParams() {
      this.contractIDs = [];
      const path = this.getPathWithQueryParams(this.financingsQueryParams);

      this.getFinancings(path);
    },

    async getFinancings(path = "") {
      let response;
      try {
        this.setLoading(true);
        response = await axios.get(`/financings${path}`, {
          withCredentials: true,
        });
        this.financingsResponse = response.data.responseData;
        this.financingsRequest = response.data.requestData;
        this.financingsResponse.data.forEach((financing) => {
          this.contractIDs.push(financing.contractId);
        });
        this.isFinancingError = false;
        this.setLoading(false);
      } catch (error) {
        this.setError(error.message);
        this.isFinancingError = true;
        this.financingsResponse = error.response.data.responseData;
        this.financingsRequest = error.response.data.requestData;
      }
    },

    setContractId(contractId) {
      this.selectedContractId = contractId;
    },

    async fetchFinancingData(path) {
      let response;
      try {
        this.setLoading(true);
        response = await axios.get(`financings/${path}`, {
          withCredentials: true,
        });
        if (response.status === 200) {
          this.financingResponse = response.data.responseData;
          this.financingRequest = response.data.requestData;
          this.isFetchFinancingDataError = false;
        }
        this.setLoading(false);
      } catch (error) {
        this.setError(error.message);
        this.isFetchFinancingDataError = true;
        if (error.response.status !== 200) {
          this.financingResponse = error.response.data.responseData;
          this.financingRequest = error.response.data.requestData;
        }
      }
    },

    changeResourceId(contractId) {
      this.selectedContractId = contractId;
    },
  },
};
</script>
