<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Customers" />

        <v-sheet min-height="70vh" rounded="lg">
          <v-container class="pa-md-12">
            <v-row>
              <v-col cols="12" sm="4">
                <CardComponent
                  :fullPath="identificationsFullPath"
                  :title="identificationTitle"
                  :displayTextField="false"
                  btnText="RUN"
                  path="identifications"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="customersQueryParams"
                  flag="CUSTOMERS"
                  :ApiVersion="ApiVersion"
                  @fetch-data="fetchCustomersData"
                />
              </v-col>
              <v-col cols="12" sm="4">
                <CardComponent
                  :fullPath="financialRelationsFullPath"
                  :title="financialRelationTitle"
                  :displayTextField="false"
                  btnText="RUN"
                  path="financial-relations"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="customersQueryParams"
                  flag="CUSTOMERS"
                  :ApiVersion="ApiVersion"
                  @fetch-data="fetchCustomersData"
                />
              </v-col>
              <v-col cols="12" sm="4">
                <CardComponent
                  :fullPath="qualificationFullPath"
                  :title="qualificationTitle"
                  :displayTextField="false"
                  btnText="RUN"
                  path="qualifications"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="customersQueryParams"
                  flag="CUSTOMERS"
                  :ApiVersion="ApiVersion"
                  @fetch-data="fetchCustomersData"
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
                        {{ customersRequestData }}
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
                        {{ customersDataResponse }}
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
import { mapGetters } from "vuex";
import axios from "../util/axios.js";

export default {
  name: "CustomersMenu",
  components: {
    SheetAppBar,
    CardComponent,
    BackButton,
  },
  data() {
    return {
      ApiVersion: "",
      identificationTitle: "",
      financialRelationTitle: "",
      qualificationTitle: "",
      customersDataResponse: "",
      customersRequestData: "",
      apiFamilyType: "",
      resBannerStyle: "white--text cyan darken-4",
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
    getPathWithQueryParams(invoiceFinancingsQueryParams){
      let path = "";
      let isFirstIteration = true;
      for(let queryParam in invoiceFinancingsQueryParams){
        if(invoiceFinancingsQueryParams[queryParam]){
          if(!isFirstIteration){
            path += `&${queryParam}=${invoiceFinancingsQueryParams[queryParam]}`;
          } else {
            isFirstIteration = false;
            path = `?${queryParam}=${invoiceFinancingsQueryParams[queryParam]}`;
          }
        }
      }

      return path;
    },
    async fetchCustomersData(path) {
      let response;
      try {
        response = await axios.get(`${this.apiFamilyType}/${path}`, {
          withCredentials: true,
        });
        if (response.status === 200) {
          this.customersRequestData = response.data.requestData;
          this.customersDataResponse = response.data.responseData;
          this.resBannerStyle = "white--text cyan darken-4";
        }
      } catch (error) {
        if (error.response.status !== 200) {
          this.resBannerStyle = "white--text red darken-1";
          this.customersDataResponse = error.response.data.responseData;
          this.customersRequestData = error.response.data.requestData;
        }
      }
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
