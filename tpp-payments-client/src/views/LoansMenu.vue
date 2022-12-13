<template>
  <CardWrapper title="Add Query Parameters">
    <template v-slot:card-content>
      <v-col cols="6" sm="12" md="6">
        <v-text-field label="Page Size" v-model="loansQueryParams['page-size']" outlined dense></v-text-field>
      </v-col>
      <v-col cols="6" sm="12" md="6">
        <v-text-field label="Page" outlined dense v-model="loansQueryParams['page']"></v-text-field>
      </v-col>
      <v-col cols="12" sm="12" md="12" v-if="ApiVersion === 'v2'">
        <v-text-field label="Pagination Key" outlined dense v-model="loansQueryParams['pagination-key']">
        </v-text-field>
      </v-col>
      <v-col cols="6" sm="12" md="3" class="mx-auto">
        <v-btn depressed height="2.5rem" width="100%" color="primary" @click="getLoansByQueryParams">
          Run
        </v-btn>
      </v-col>
    </template>
    <template v-slot:content>
      <CardCode
        class="mt-8"
        color="lightblue"
        title="Loans API Request"
        :code="loansRequest"
      />
      <CardCode
        class="mt-10"
        color="lightgreen"
        title="Loans API Response"
        :code="loansResponse"
      />
      <v-row class="mt-8">
        <v-col cols="12" md="6">
          <CardComponent 
            title="Loan Warranties API"
            :fullPath="`/open-banking/loans/${ApiVersion}/contracts/{contractId}/warranties`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}/warranties`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="loanGenericQueryParams"
            flag="CREDIT_OPERATION" 
            :ApiVersion="ApiVersion" 
            @fetch-data="fetchLoanData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Loan Scheduled Instalments API"
            :fullPath="`/open-banking/loans/${ApiVersion}/contracts/{contractId}/scheduled-instalments`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}/scheduled-instalments`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" :queryParams="loanGenericQueryParams"
            flag="CREDIT_OPERATION" 
            :ApiVersion="ApiVersion"
            @fetch-data="fetchLoanData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Loan Payments API"
            :fullPath="`/open-banking/loans/${ApiVersion}/contracts/{contractId}/payments`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}/payments`" 
            :supportsQueryParam="true"
            :getPathWithQueryParams="getPathWithQueryParams" 
            :queryParams="loanGenericQueryParams"
            flag="CREDIT_OPERATION" 
            :ApiVersion="ApiVersion" 
            @fetch-data="fetchLoanData"
            @resource-id-change="changeResourceId" />
        </v-col>
        <v-col cols="12" md="6">
          <CardComponent 
            title="Loan API" 
            :fullPath="`/open-banking/loans/${ApiVersion}/contracts/{contractId}`"
            :resourceId="selectedContractId" 
            :path="`${selectedContractId}`"
            @fetch-data="fetchLoanData" 
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
        :code="loanRequest"
      />
      <CardCode
        class="mt-10"
        color="lightgreen"
        title="Response"
        :code="loanResponse"
        :is-error="isFetchLoanDataError"
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
  name: "LoansMenu",
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
      loansResponse: "",
      loansRequest: "",
      loanRequest: "",
      contractIDs: [],
      selectedContractId: "",
      loanResponse: "",
      isFetchLoanDataError: false,
      loansQueryParams: {
        "page-size": null,
        page: null,
        "pagination-key": null,
      },
      loanGenericQueryParams: { // covers warranties, scheduled instalments and loan payments
        "page-size": null,
        page: null,
        "pagination-key": null
      }
    };
  },
  created() {
    const optionWords = this.ApiOption.split("-");
    this.ApiVersion = optionWords[optionWords.length - 1];
    this.getLoans();
  },
  computed: {
    ...mapGetters(["ApiOption"]),
  },
  methods: {
    ...mapActions(["setError", "setLoading"]),

    getLoansByQueryParams() {
      this.contractIDs = [];
      const path = this.getPathWithQueryParams(this.loansQueryParams);
      console.log(path);
      this.getLoans(path);
    },

    async getLoans(path = "") {
      this.setLoading(true);
      let response;
      try {
        response = await axios.get(`/loans${path}`, { withCredentials: true });
        this.loansResponse = response.data.responseData;
        this.loansRequest = response.data.requestData;
        this.loansResponse.data.forEach((loan) => {
          this.contractIDs.push(loan.contractId);
        });
      } catch (error) {
        this.setError(error.message);
        this.loansResponse = error.response.data.responseData;
        this.loansRequest = error.response.data.requestData;
      }
      this.setLoading(false);
    },

    setContractId(contractId) {
      this.selectedContractId = contractId;
    },

    async fetchLoanData(path) {
      this.setLoading(true);
      let response;
      try {
        response = await axios.get(`loans/${path}`, { withCredentials: true });
        if (response.status === 200) {
          this.isFetchLoanDataError = false;
          this.loanResponse = response.data.responseData;
          this.loanRequest = response.data.requestData;
        }
      } catch (error) {
        this.setError(error.message);
        this.isFetchLoanDataError = true;
        if (error.response.status !== 200) {
          this.loanResponse = error.response.data.responseData;
          this.loanRequest = error.response.data.requestData;
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
