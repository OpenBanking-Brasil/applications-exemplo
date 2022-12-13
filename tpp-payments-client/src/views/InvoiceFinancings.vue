<template>
  <CardWrapper title="Add Query Parameters">
    <template v-slot:card-content>
      <v-col cols="6" sm="12" md="6">
        <v-text-field label="Page Size" v-model="invoiceFinancingsQueryParams['page-size']" outlined dense>
        </v-text-field>
      </v-col>
      <v-col cols="6" sm="12" md="6">
        <v-text-field label="Page" outlined dense v-model="invoiceFinancingsQueryParams['page']"></v-text-field>
      </v-col>
      <v-col cols="12" sm="12" md="12" v-if="ApiVersion === 'v2'">
        <v-text-field label="Pagination Key" outlined dense
          v-model="invoiceFinancingsQueryParams['pagination-key']">
        </v-text-field>
      </v-col>
      <v-col cols="6" sm="12" md="3" class="mx-auto">
        <v-btn depressed height="2.5rem" width="100%" color="primary"
          @click="getInvoiceFinancingsByQueryParams">
          Run
        </v-btn>
      </v-col>
    </template>
    <template v-slot:content>
      <CardCode 
        class="mt-8" 
        color="lightblue"
        title="Invoice Financings API Request"
        :code="invoiceFinancingsRequest"
      />
      <CardCode
        class="mt-10" 
        color="lightgreen" 
        title="Invoice Financings API Response"
        :code="invoiceFinancingsResponse"
        :is-error="isInvoiceFinansingError"
      />
      <v-row>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Invoice Financings Warranties API"
            :fullPath="`/open-banking/invoice-financings/${ApiVersion}/contracts/{contractId}/warranties`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}/warranties`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="invoiceFinancingGenericQueryParams"
            flag="CREDIT_OPERATION" 
            :ApiVersion="ApiVersion"
            @fetch-data="fetchInvoiceFinancingData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Invoice Financings Scheduled Instalments API"
            :fullPath="`/open-banking/invoice-financings/${ApiVersion}/contracts/{contractId}/scheduled-instalments`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}/scheduled-instalments`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="invoiceFinancingGenericQueryParams"
            flag="CREDIT_OPERATION" 
            :ApiVersion="ApiVersion" 
            @fetch-data="fetchInvoiceFinancingData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent title="Invoice Financings Payments API"
            :fullPath="`/open-banking/invoice-financings/${ApiVersion}/contracts/{contractId}/payments`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}/payments`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="invoiceFinancingGenericQueryParams"
            flag="CREDIT_OPERATION" 
            :ApiVersion="ApiVersion" 
            @fetch-data="fetchInvoiceFinancingData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Invoice Financings API"
            :fullPath="`/open-banking/invoice-financings/${ApiVersion}/contracts/{contractId}`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}`"
            @fetch-data="fetchInvoiceFinancingData" 
            @resource-id-change="changeResourceId" />
        </v-col>
      </v-row>
      <div class="pa-2"></div>
      <v-divider class="mt-5 mb-8"></v-divider>

      <v-card v-if="contractIDs.length" elevation="0" class="pa-0">
        <v-card-title class="px-0 pt-0 pb-5">Available Contract IDs</v-card-title>
        <v-list dense max-height="20vh" style="overflow: auto">
          <v-list-item-group color="primary">
            <v-list-item v-for="(contractId, i) in contractIDs" :key="i" @click="setContractId(contractId)">
              <v-list-item-content>
                <v-list-item-title >
                    {{contractId}}
                </v-list-item-title>
              </v-list-item-content>
            </v-list-item>
          </v-list-item-group>
        </v-list>
      </v-card>
      <CardCode
        class="mt-10"
        color="lightblue"
        title="Request"
        :code="invoiceFinancingRequest"
      />
      <CardCode
        class="mt-10"
        color="lightgreen"
        title="Response"
        :code="invoiceFinancingResponse"
        :is-error="isFetchInvoiceFinancingData"
      />
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
  name: "InvoiceFinancings",
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
      invoiceFinancingsResponse: "",
      invoiceFinancingsRequest: "",
      invoiceFinancingRequest: "",
      contractIDs: [],
      selectedContractId: "",
      invoiceFinancingResponse: "",
      isInvoiceFinansingError: false,
      isFetchInvoiceFinancingData: false,
      invoiceFinancingsQueryParams: {
        "page-size": null,
        page: null,
        "pagination-key": null,
      },
      invoiceFinancingGenericQueryParams: {
        "page-size": null,
        page: null,
        "pagination-key": null,
      }
    };
  },
  created() {
    const optionWords = this.ApiOption.split("-");
    this.ApiVersion = optionWords[optionWords.length - 1];
    this.getInvoiceFinancings();
  },
  computed: {
    ...mapGetters(["ApiOption"]),
  },
  methods: {
    ...mapActions(["setError", "setLoading"]),

    getInvoiceFinancingsByQueryParams() {
      this.contractIDs = [];
      const path = this.getPathWithQueryParams(this.invoiceFinancingsQueryParams);

      this.getInvoiceFinancings(path);
    },

    async getInvoiceFinancings(path = "") {
      this.setLoading(true);
      let response;
      try {
        response = await axios.get(`/invoice-financings${path}`, { withCredentials: true });
        this.invoiceFinancingsResponse = response.data.responseData;
        this.invoiceFinancingsRequest = response.data.requestData;
        this.invoiceFinancingsResponse.data.forEach((invoiceFinancing) => {
          this.contractIDs.push(invoiceFinancing.contractId);
        });
        this.isInvoiceFinansingError = false;
      } catch (error) {
        this.setError(error.message);
        this.isInvoiceFinansingError = true;
        this.invoiceFinancingsResponse = error.response.data.responseData;
        this.invoiceFinancingsRequest = error.response.data.requestData;
      }
      this.setLoading(false);
    },

    setContractId(contractId) {
      this.selectedContractId = contractId;
    },

    async fetchInvoiceFinancingData(path) {
      this.setLoading(true);
      let response;
      try {
        response = await axios.get(`invoice-financings/${path}`, { withCredentials: true });
        if (response.status === 200) {
          this.invoiceFinancingResponse = response.data.responseData;
          this.invoiceFinancingRequest = response.data.requestData;
          this.isFetchInvoiceFinancingData = false;
        }
      } catch (error) {
        this.setError(error.message);
        this.isFetchInvoiceFinancingData = true;
        if (error.response.status !== 200) {
          this.invoiceFinancingResponse = error.response.data.responseData;
          this.invoiceFinancingRequest = error.response.data.requestData;
        }
      }
      this.setLoading(false);
    },

    changeResourceId(contractId) {
      this.selectedContractId = contractId;
    },
  },
};
</script>
