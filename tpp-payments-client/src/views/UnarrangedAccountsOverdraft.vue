<template>
  <CardWrapper title="Add Query Parameters">
    <template v-slot:card-content>
      <v-col cols="6" sm="12" md="6">
        <v-text-field label="Page Size" v-model="theUAO_QueryParams['page-size']" outlined dense></v-text-field>
      </v-col>
      <v-col cols="6" sm="12" md="6">
        <v-text-field label="Page" outlined dense v-model="theUAO_QueryParams['page']"></v-text-field>
      </v-col>
      <v-col cols="12" sm="12" md="12" v-if="ApiVersion === 'v2'">
        <v-text-field label="Pagination Key" outlined dense v-model="theUAO_QueryParams['pagination-key']">
        </v-text-field>
      </v-col>
      <v-col cols="6" sm="12" md="3" class="mx-auto">
        <v-btn depressed height="2.5rem" width="100%" color="primary" @click="getUAO_ByQueryParams">
          Run
        </v-btn>
      </v-col>
    </template>
    <template v-slot:content>
      <CardCode 
        class="mt-8" 
        color="lightblue" 
        title="Unarranged Accounts Overdraft API Request"
        :code="theUAO_Request" />
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Unarranged Accounts Overdraft API Response"
        :code="theUAO_Response" :is-error="isGetUAOError" />
      <v-row class="mt-2">
        <v-col cols="12" md="6">
          <CardComponent 
            title="Unarranged Accounts Overdraft API"
            :fullPath="`/open-banking/unarranged-accounts-overdraft/${ApiVersion}/contracts/{contractId}`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}`"
            @fetch-data="fetchUAO_Data" 
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Unarranged Accounts Overdraft Warranties API"
            :fullPath="`/open-banking/unarranged-accounts-overdraft/${ApiVersion}/contracts/{contractId}/warranties`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}/warranties`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="UAO_GenericQueryParams"
            flag="CREDIT_OPERATION" 
            :ApiVersion="ApiVersion"
            @fetch-data="fetchUAO_Data"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Unarranged Accounts Overdraft Scheduled Instalments API"
            :fullPath="`/open-banking/unarranged-accounts-overdraft/${ApiVersion}/contracts/{contractId}/scheduled-instalments`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}/scheduled-instalments`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="UAO_GenericQueryParams"
            flag="CREDIT_OPERATION" 
            :ApiVersion="ApiVersion" 
            @fetch-data="fetchUAO_Data"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Unarranged Accounts Overdraft Payments API"
            :fullPath="`/open-banking/unarranged-accounts-overdraft/${ApiVersion}/contracts/{contractId}/payments`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}/payments`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="UAO_GenericQueryParams"
            flag="CREDIT_OPERATION" 
            :ApiVersion="ApiVersion" 
            @fetch-data="fetchUAO_Data"
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
        :code="UAO_Request" />
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Response" 
        :code="UAO_Response"
        :is-error="isFetchUAO_DataError" />
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
  name: "UnarrangedAccountsOverdraft",
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
      theUAO_Response: "",
      theUAO_Request: "",
      UAO_Request: "",
      contractIDs: [],
      selectedContractId: "",
      UAO_Response: "",
      isGetUAOError: false,
      isFetchUAO_DataError: false,
      theUAO_QueryParams: {
        "page-size": null,
        page: null,
        "pagination-key": null,
      },
      UAO_GenericQueryParams: {
        "page-size": null,
        page: null,
        "pagination-key": null,
      }
    };
  },
  created() {
    const optionWords = this.ApiOption.split("-");
    this.ApiVersion = optionWords[optionWords.length - 1];
    this.getUAO();
  },
  computed: {
    ...mapGetters(["ApiOption"]),
  },
  methods: {
    ...mapActions(["setError", "setLoading"]),

    getUAO_ByQueryParams() {
      this.contractIDs = [];
      const path = this.getPathWithQueryParams(this.theUAO_QueryParams);

      this.getUAO(path);
    },

    async getUAO(path = "") {
      let response;
      try {
        this.setLoading(true);
        response = await axios.get(`/unarranged-accounts-overdraft${path}`, { withCredentials: true });
        this.theUAO_Response = response.data.responseData;
        this.theUAO_Request = response.data.requestData;
        this.theUAO_Response.data.forEach((UAO) => {
          this.contractIDs.push(UAO.contractId);
        });
        this.isGetUAOError = false;
        this.setLoading(false);
      } catch (error) {
        this.setError(error.message);
        this.isGetUAOError = true;
        this.theUAO_Response = error.response.data.responseData;
        this.theUAO_Request = error.response.data.requestData;
      }
    },

    setContractId(contractId) {
      this.selectedContractId = contractId;
    },

    async fetchUAO_Data(path) {
      let response;
      try {
        this.setLoading(true);
        response = await axios.get(`unarranged-accounts-overdraft/${path}`, { withCredentials: true });
        if (response.status === 200) {
          this.UAO_Response = response.data.responseData;
          this.UAO_Request = response.data.requestData;
          this.isFetchUAO_DataError = false;
        }
        this.setLoading(false);
      } catch (error) {
        this.setError(error.message);
        this.isFetchUAO_DataError = true;
        if (error.response.status !== 200) {
          this.UAO_Response = error.response.data.responseData;
          this.UAO_Request = error.response.data.requestData;
        }
      }
    },

    changeResourceId(contractId) {
      this.selectedContractId = contractId;
    },
  },
};
</script>
