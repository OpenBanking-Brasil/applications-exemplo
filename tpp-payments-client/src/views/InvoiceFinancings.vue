<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Invoice Financings" />

        <v-sheet min-height="70vh" rounded="lg">
          <v-container class="pa-md-12">
            <h3 class="mb-3 mt-5 grey--text text--darken-1">
              Add Query Parameters
            </h3>

            <v-row>
              <v-col cols="4" md="4">
                <v-text-field
                  label="Page Size"
                  placeholder="Page Size"
                  v-model="invoiceFinancingsQueryParams['page-size']"
                  outlined
                ></v-text-field>
              </v-col>
              <v-col cols="4" md="4">
                <v-text-field
                  label="Page"
                  placeholder="Page"
                  outlined
                  v-model="invoiceFinancingsQueryParams['page']"
                ></v-text-field>
              </v-col>
              <v-col cols="4" md="4">
                <v-btn
                  depressed
                  height="3.4rem"
                  width="100%"
                  color="primary"
                  @click="getInvoiceFinancingsByQueryParams"
                >
                  Run
                </v-btn>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text blue darken-4"
                    >Invoice Financings API Request</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                         {{ invoiceFinancingsRequest }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title :class="primaryResBannerStyle"
                    >Invoice Financings API Response</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                         {{ invoiceFinancingsResponse }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Invoice Financings API"
                  fullPath="/open-banking/invoice-financings/v1/contracts/{contractId}"
                  :resourceId="selectedContractId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedContractId}`"
                  @fetch-data="fetchInvoiceFinancingData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Invoice Financings Warranties API"
                  fullPath="/open-banking/invoice-financings/v1/contracts/{contractId}/warranties"
                  :resourceId="selectedContractId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedContractId}/warranties`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="invoiceFinancingWarrantiesQueryParams"
                  flag="CREDIT_OPERATION"
                  @fetch-data="fetchInvoiceFinancingData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Invoice Financings Scheduled Instalments API"
                  fullPath="/open-banking/invoice-financings/v1/contracts/{contractId}/scheduled-instalments"
                  :resourceId="selectedContractId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedContractId}/scheduled-instalments`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="invoiceFinancingWarrantiesQueryParams"
                  flag="CREDIT_OPERATION"
                  @fetch-data="fetchInvoiceFinancingData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Invoice Financings Payments API"
                  fullPath="/open-banking/invoice-financings/v1/contracts/{contractId}/payments"
                  :resourceId="selectedContractId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedContractId}/payments`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="invoiceFinancingWarrantiesQueryParams"
                  flag="CREDIT_OPERATION"
                  @fetch-data="fetchInvoiceFinancingData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
            </v-row>
            <div class="pa-2"></div>
            <v-divider class="mt-5 mb-8"></v-divider>
            <v-row>
              <v-col cols="12" sm="4">
                <v-card class="mx-auto" max-width="300" tile>
                  <v-subheader>Available Contract IDs</v-subheader>
                  <v-list dense max-height="20vh" style="overflow: auto">
                    <v-list-item-group color="primary">
                      <v-list-item
                        v-for="(contractId, i) in contractIDs"
                        :key="i"
                        @click="
                          () => {
                            setContractId(contractId);
                          }
                        "
                      >
                        <v-list-item-content>
                          <v-list-item-title
                            v-text="contractId"
                          ></v-list-item-title>
                        </v-list-item-content>
                      </v-list-item>
                    </v-list-item-group>
                  </v-list>
                </v-card>
              </v-col>
              <v-col cols="12" sm="8">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text blue darken-4">Request</v-card-title>
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                        {{ invoiceFinancingRequest }}
                    </pre>
                  </v-card-text>
                </v-card>
                 <v-divider class="mt-4"></v-divider>
                <v-card elevation="2" outlined>
                  <v-card-title :class="secondaryResBannerStyle">Response</v-card-title>
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                        {{ invoiceFinancingResponse }}
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

export default {
  name: "InvoiceFinancings",
  components: {
    SheetAppBar,
    CardComponent,
    BackButton,
  },
  data() {
    return {
      invoiceFinancingsResponse: "",
      invoiceFinancingsRequest: "",
      invoiceFinancingRequest: "",
      contractIDs: [],
      selectedContractId: "",
      invoiceFinancingResponse: "",
      primaryResBannerStyle: "white--text cyan darken-4",
      secondaryResBannerStyle: "white--text cyan darken-4",
      invoiceFinancingsQueryParams: {
        "page-size": null,
        page: null,
      },
      invoiceFinancingWarrantiesQueryParams: {
        "page-size": null,
        page: null,
      }
    };
  },
  created() {
    this.getInvoiceFinancings();
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

    getInvoiceFinancingsByQueryParams(){
      this.contractIDs = [];
      const path = this.getPathWithQueryParams(this.invoiceFinancingsQueryParams);

      this.getInvoiceFinancings(path);
    },

    async getInvoiceFinancings(path=""){

      let response;
      try {
        response = await axios.get(`/invoice-financings${path}`, { withCredentials: true });
        this.invoiceFinancingsResponse = response.data.responseData;
        this.invoiceFinancingsRequest = response.data.requestData;
        this.invoiceFinancingsResponse.data.forEach((invoiceFinancing) => {
          this.contractIDs.push(invoiceFinancing.contractId);
        });
        this.primaryResBannerStyle = "white--text cyan darken-4";
      } catch (error) {
        this.invoiceFinancingsResponse = error.response.data.responseData;
        this.invoiceFinancingsRequest = error.response.data.requestData;
        this.primaryResBannerStyle = "white--text red darken-1";
      }
    },

    setContractId(contractId) {
      this.selectedContractId = contractId;
    },

    async fetchInvoiceFinancingData(path) {

      let response;
      try {
        response = await axios.get(`invoice-financings/${path}`, { withCredentials: true });
        if (response.status === 200) {
          this.invoiceFinancingResponse = response.data.responseData;
          this.invoiceFinancingRequest = response.data.requestData;
          this.secondaryResBannerStyle = "white--text cyan darken-4";
        }
      } catch (error) {
        if (error.response.status !== 200) {
          this.secondaryResBannerStyle = "white--text red darken-1";
          this.invoiceFinancingResponse = error.response.data.responseData;
          this.invoiceFinancingRequest = error.response.data.requestData;
        }
      }
    },

    changeResourceId(contractId) {
      this.selectedContractId = contractId;
    },
  },
};
</script>
