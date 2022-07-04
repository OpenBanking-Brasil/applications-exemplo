<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header=" Unarranged Accounts Overdraft" />

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
                  v-model="theUAO_QueryParams['page-size']"
                  outlined
                ></v-text-field>
              </v-col>
              <v-col cols="4" md="4">
                <v-text-field
                  label="Page"
                  placeholder="Page"
                  outlined
                  v-model="theUAO_QueryParams['page']"
                ></v-text-field>
              </v-col>
              <v-col cols="4" md="4">
                <v-btn
                  depressed
                  height="3.4rem"
                  width="100%"
                  color="primary"
                  @click="getUAO_ByQueryParams"
                >
                  Run
                </v-btn>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text blue darken-4"
                    >Unarranged Accounts Overdraft API Request</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                         {{ theUAO_Request }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title :class="primaryResBannerStyle"
                    >Unarranged Accounts Overdraft API Response</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                         {{ theUAO_Response }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Unarranged Accounts Overdraft API"
                  fullPath="/open-banking/unarranged-accounts-overdraft/v1/contracts/{contractId}"
                  :resourceId="selectedContractId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedContractId}`"
                  @fetch-data="fetchUAO_Data"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Unarranged Accounts Overdraft Warranties API"
                  fullPath="/open-banking/unarranged-accounts-overdraft/v1/contracts/{contractId}/warranties"
                  :resourceId="selectedContractId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedContractId}/warranties`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="UAO_WarrantiesQueryParams"
                  flag="CREDIT_OPERATION"
                  @fetch-data="fetchUAO_Data"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Unarranged Accounts Overdraft Scheduled Instalments API"
                  fullPath="/open-banking/unarranged-accounts-overdraft/v1/contracts/{contractId}/scheduled-instalments"
                  :resourceId="selectedContractId"
                  btnText="RUN"
                  :displayTextField="true"
                  :path="`${selectedContractId}/scheduled-instalments`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="UAO_WarrantiesQueryParams"
                  flag="CREDIT_OPERATION"
                  @fetch-data="fetchUAO_Data"
                  @resource-id-change="changeResourceId"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Unarranged Accounts Overdraft Payments API"
                  fullPath="/open-banking/unarranged-accounts-overdraft/v1/contracts/{contractId}/payments"
                  :resourceId="selectedContractId"
                  :displayTextField="true"
                  btnText="RUN"
                  :path="`${selectedContractId}/payments`"
                  :supportsQueryParam="true"
                  :getPathWithQueryParams="getPathWithQueryParams" 
                  :queryParams="UAO_WarrantiesQueryParams"
                  flag="CREDIT_OPERATION"
                  @fetch-data="fetchUAO_Data"
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
                        {{ UAO_Request }}
                    </pre>
                  </v-card-text>
                </v-card>
                 <v-divider class="mt-4"></v-divider>
                <v-card elevation="2" outlined>
                  <v-card-title :class="secondaryResBannerStyle">Response</v-card-title>
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                        {{ UAO_Response }}
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
  name: "UnarrangedAccountsOverdraft",
  components: {
    SheetAppBar,
    CardComponent,
    BackButton,
  },
  data() {
    return {
      theUAO_Response: "",
      theUAO_Request: "",
      UAO_Request: "",
      contractIDs: [],
      selectedContractId: "",
      UAO_Response: "",
      primaryResBannerStyle: "white--text cyan darken-4",
      secondaryResBannerStyle: "white--text cyan darken-4",
      theUAO_QueryParams: {
        "page-size": null,
        page: null,
      },
      UAO_WarrantiesQueryParams: {
        "page-size": null,
        page: null,
      }
    };
  },
  created() {
    this.getUAO();
  },
  methods: {

    getPathWithQueryParams(theUAO_QueryParams){
      let path = "";
      let isFirstIteration = true;
      for(let queryParam in theUAO_QueryParams){
        if(theUAO_QueryParams[queryParam]){
          if(!isFirstIteration){
            path += `&${queryParam}=${theUAO_QueryParams[queryParam]}`;
          } else {
            isFirstIteration = false;
            path = `?${queryParam}=${theUAO_QueryParams[queryParam]}`;
          }
        }
      }

      return path;
    },

    getUAO_ByQueryParams(){
      this.contractIDs = [];
      const path = this.getPathWithQueryParams(this.theUAO_QueryParams);

      this.getUAO(path);
    },

    async getUAO(path=""){

      let response;
      try {
        response = await axios.get(`/unarranged-accounts-overdraft${path}`, { withCredentials: true });
        this.theUAO_Response = response.data.responseData;
        this.theUAO_Request = response.data.requestData;
        this.theUAO_Response.data.forEach((UAO) => {
          this.contractIDs.push(UAO.contractId);
        });
        this.primaryResBannerStyle = "white--text cyan darken-4";
      } catch (error){
        this.theUAO_Response = error.response.data.responseData;
        this.theUAO_Request = error.response.data.requestData;
        this.primaryResBannerStyle = "white--text red darken-1";
      }
    },

    setContractId(contractId) {
      this.selectedContractId = contractId;
    },

    async fetchUAO_Data(path) {

      let response;
      try {
        response = await axios.get(`unarranged-accounts-overdraft/${path}`, { withCredentials: true });
        if (response.status === 200) {
          this.UAO_Response = response.data.responseData;
          this.UAO_Request = response.data.requestData;
          this.secondaryResBannerStyle = "white--text cyan darken-4";
        }
      } catch (error){
        if (error.response.status !== 200) {
          this.secondaryResBannerStyle = "white--text red darken-1";
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
