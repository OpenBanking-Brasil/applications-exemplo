<template>
  <CardWrapper title="">
    <template v-slot:card-content>
      <v-col cols="12" md="6">
        <CardComponent 
          :fullPath="identificationsFullPath"
          :resourceId="resourceId"
          :title="identificationTitle" 
          path="identifications" 
          :supportsQueryParam="true"
          :getPathWithQueryParams="getPathWithQueryParams" 
          :queryParams="customersQueryParams" 
          flag="CUSTOMERS"
          :ApiVersion="ApiVersion" 
          @fetch-data="fetchCustomersData"
          @resource-id-change="changeResourceId" />
      </v-col>
      <v-col cols="12" md="6">
        <CardComponent 
          :fullPath="financialRelationsFullPath"
          :resourceId="resourceId"
          :title="financialRelationTitle"
          path="financial-relations" 
          :supportsQueryParam="true"
          :getPathWithQueryParams="getPathWithQueryParams" 
          :queryParams="customersQueryParams" 
          flag="CUSTOMERS"
          :ApiVersion="ApiVersion" 
          @fetch-data="fetchCustomersData"
          @resource-id-change="changeResourceId" />
      </v-col>
      <v-col cols="12" md="6">
        <CardComponent 
          :fullPath="qualificationFullPath"
          :resourceId="resourceId"
          :title="qualificationTitle" 
          path="qualifications" 
          :supportsQueryParam="true"
          :getPathWithQueryParams="getPathWithQueryParams" 
          :queryParams="customersQueryParams" 
          flag="CUSTOMERS"
          :ApiVersion="ApiVersion" 
          @fetch-data="fetchCustomersData"
          @resource-id-change="changeResourceId" />
      </v-col>
    </template>
    <template v-slot:content>
      <CardCode 
        class="mt-10" 
        color="lightblue" 
        title="Request" 
        :code="customersRequestData" />
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Response" 
        :code="customersDataResponse"
        :is-error="isFetchCustomersDataError" />
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
  name: "CustomersMenu",
  components: {
    SheetAppBar,
    CardComponent,
    CardCode,
    CardWrapper,
  },
  data() {
    return {
      getPathWithQueryParams,
      resourceId: "",
      ApiVersion: "",
      identificationTitle: "",
      financialRelationTitle: "",
      qualificationTitle: "",
      customersDataResponse: "",
      customersRequestData: "",
      apiFamilyType: "",
      isFetchCustomersDataError: false,
      identificationsFullPath: "",
      financialRelationsFullPath: "",
      qualificationFullPath: "",
      customersQueryParams: {
        "page-size": null,
        page: null,
        "pagination-key": null,
      }
    };
  },
  computed: {
    ...mapGetters(["cadastroOption", "ApiOption"]),
  },
  methods: {
    ...mapActions(["setError", "setLoading"]),

    async fetchCustomersData(path) {
      let response;
      try {
        this.setLoading(true);
        response = await axios.get(`${this.apiFamilyType}/${path}`, {
          withCredentials: true,
        });
        if (response.status === 200) {
          this.customersRequestData = response.data.requestData;
          this.customersDataResponse = response.data.responseData;
          this.isFetchCustomersDataError = false;
        }
        this.setLoading(false);
      } catch (error) {
        this.setError(error.message);
        this.isFetchCustomersDataError = true;
        if (error.response.status !== 200) {
          this.customersDataResponse = error.response.data.responseData;
          this.customersRequestData = error.response.data.requestData;
        }
      }
    },
    changeResourceId(resourceId) {
      this.resourceId = resourceId;
    },
  },

  created() {
    const optionWords = this.ApiOption.split("-");
    this.ApiVersion = optionWords[optionWords.length - 1];
    if (this.cadastroOption === "PF") {
      this.apiFamilyType = "customers-personal";
      this.identificationTitle = "Customers Personal Identifications API";
      this.financialRelationTitle =
        "Customers Personal Financial Relations API";
      this.qualificationTitle = "Customers Personal Qualifications API";

      this.identificationsFullPath =
        `/open-banking/customers/${this.ApiVersion}/personal/identifications`;
      this.financialRelationsFullPath =
        `/open-banking/customers/${this.ApiVersion}/personal/financial-relations`;
      this.qualificationFullPath =
        `/open-banking/customers/${this.ApiVersion}/personal/qualifications`;
    } else {
      this.apiFamilyType = "customers-business";
      this.identificationTitle = "Customers Business Identifications API";
      this.financialRelationTitle =
        "Customers Business Financial Relations API";
      this.qualificationTitle = "Customers Business Qualifications API";
      this.identificationsFullPath =
        `/open-banking/customers/${this.ApiVersion}/business/identifications`;
      this.financialRelationsFullPath =
        `/open-banking/customers/${this.ApiVersion}/business/financial-relations`;
      this.qualificationFullPath =
        `/open-banking/customers/${this.ApiVersion}/business/qualifications`;
    }
  },
};
</script>
