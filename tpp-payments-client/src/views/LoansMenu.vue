<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Loans" />

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
                  v-model="loansQueryParams['page-size']"
                  outlined
                ></v-text-field>
              </v-col>
              <v-col cols="4" md="4">
                <v-text-field
                  label="Page"
                  placeholder="Page"
                  outlined
                  v-model="loansQueryParams['page']"
                ></v-text-field>
              </v-col>
              <v-col cols="4" md="4">
                <v-btn
                  depressed
                  height="3.4rem"
                  width="100%"
                  color="primary"
                  @click="getLoansByQueryParams"
                >
                  Run
                </v-btn>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text blue darken-4"
                    >Loans API Request</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                         {{ loansRequest }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text cyan darken-4"
                    >Loans API Response</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                         {{ loansResponse }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Loan API"
                  fullPath="/open-banking/loans/v1/contracts/{contractId}"
                  :resourceId="selectedContractId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedContractId}`"
                  @fetch-data="fetchLoanData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Loan Warranties API"
                  fullPath="/open-banking/loans/v1/contracts/{contractId}/warranties"
                  :resourceId="selectedContractId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedContractId}/warranties`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="loanWarrantiesQueryParams"
                  flag="LOAN"
                  @fetch-data="fetchLoanData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Loan Scheduled Instalments API"
                  fullPath="/open-banking/loans/v1/contracts/{contractId}/scheduled-instalments"
                  :resourceId="selectedContractId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedContractId}/scheduled-instalments`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="loanWarrantiesQueryParams"
                  flag="LOAN"
                  @fetch-data="fetchLoanData"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Loan Payments API"
                  fullPath="/open-banking/loans/v1/contracts/{contractId}/payments"
                  :resourceId="selectedContractId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedContractId}/payments`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="loanWarrantiesQueryParams"
                  flag="LOAN"
                  @fetch-data="fetchLoanData"
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
                        {{ loanRequest }}
                    </pre>
                  </v-card-text>
                </v-card>
                 <v-divider class="mt-4"></v-divider>
                <v-card elevation="2" outlined>
                  <v-card-title :class="resBannerStyle">Response</v-card-title>
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                        {{ loanResponse }}
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
  name: "LoansMenu",
  components: {
    SheetAppBar,
    CardComponent,
    BackButton,
  },
  data() {
    return {
      loansResponse: "",
      loansRequest: "",
      loanRequest: "",
      contractIDs: [],
      selectedContractId: "",
      loanResponse: "",
      resBannerStyle: "white--text cyan darken-4",
      loansQueryParams: {
        "page-size": null,
        page: null,
      },
      loanWarrantiesQueryParams: {
        "page-size": null,
        page: null,
      }
    };
  },
  created() {
    this.getLoans();
  },
  methods: {

    getPathWithQueryParams(loansQueryParams){
      let path = "";
      let isFirstIteration = true;
      for(let queryParam in loansQueryParams){
        if(loansQueryParams[queryParam]){
          if(!isFirstIteration){
            path += `&${queryParam}=${loansQueryParams[queryParam]}`;
          } else {
            isFirstIteration = false;
            path = `?${queryParam}=${loansQueryParams[queryParam]}`;
          }
        }
      }

      return path;
    },

    getLoansByQueryParams(){
      this.contractIDs = [];
      const path = this.getPathWithQueryParams(this.loansQueryParams);

      this.getLoans(path);
    },

    getLoans(path=""){
      axios.get(`/loans${path}`, { withCredentials: true }).then((response) => {
        this.loansResponse = response.data.responseData;
        this.loansRequest = response.data.requestData;
        this.loansResponse.data.forEach((loan) => {
          this.contractIDs.push(loan.contractId);
        });
      }).catch((error) => {
        this.loansResponse = error.response.data.responseData;
        this.loansRequest = error.response.data.requestData;
      });
    },

    setContractId(contractId) {
      this.selectedContractId = contractId;
    },

    fetchLoanData(path) {
      axios
        .get(`loans/${path}`, { withCredentials: true })
        .then((response) => {
          if (response.status === 200) {
            this.loanResponse = response.data.responseData;
            this.loanRequest = response.data.requestData;
            this.resBannerStyle = "white--text cyan darken-4";
          }
        })
        .catch((error) => {
          if (error.response.status !== 200) {
            this.resBannerStyle = "white--text red darken-1";
            this.loanResponse = error.response.data.responseData;
            this.loanRequest = error.response.data.requestData;
          }
        });
    },

    changeResourceId(contractId) {
      this.selectedContractId = contractId;
    },
  },
};
</script>
