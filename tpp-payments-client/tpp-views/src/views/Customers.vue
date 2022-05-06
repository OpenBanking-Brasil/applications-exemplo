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
                  @fetch-data="fetchCustomersData"
                />
              </v-col>
            </v-row>
            <v-divider class="mt-5 mb-8"></v-divider>
            <v-row>
              <v-col cols="12" sm="12">
                <v-card elevation="2" outlined>
                  <v-card-title :class="resBannerStyle"
                    >Customers Data Response</v-card-title
                  >
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
        <BackButton path="consent-response-menu"/>
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
    BackButton
  },
  data() {
    return {
      identificationTitle: "",
      financialRelationTitle: "",
      qualificationTitle: "",
      customersDataResponse: "",
      apiFamilyType: "",
      resBannerStyle: "white--text cyan darken-4",
      identificationsFullPath: "",
      financialRelationsFullPath: "",
      qualificationFullPath: "",
    };
  },

  computed: {
    ...mapGetters(["cadastroOption"]),
  },

  methods: {
    fetchCustomersData(path) {
      axios
        .get(`${this.apiFamilyType}/${path}`, { withCredentials: true })
        .then((response) => {
          if (response.status === 200) {
            this.customersDataResponse = response.data;
            this.resBannerStyle = "white--text cyan darken-4";
          }
        })
        .catch((error) => {
          if (error.response.status !== 200) {
            this.resBannerStyle = "white--text red darken-1";
            this.customersDataResponse = error.response.data;
          }
        });
    },
  },

  created() {
    if (this.cadastroOption === "PF") {
      this.apiFamilyType = "customers-personal";
      this.identificationTitle = "Customers Personal Identifications API";
      this.financialRelationTitle =
        "Customers Personal Financial Relations API";
      this.qualificationTitle = "Customers Personal Qualifications API";

      this.identificationsFullPath =
        "/open-banking/customers/v1/personal/identifications";
      this.financialRelationsFullPath =
        "/open-banking/customers/v1/personal/financial-relations";
      this.qualificationFullPath =
        "/open-banking/customers/v1/personal/qualifications";
    } else {
      this.apiFamilyType = "customers-business";
      this.identificationTitle = "Customers Business Identifications API";
      this.financialRelationTitle =
        "Customers Business Financial Relations API";
      this.qualificationTitle = "Customers Business Qualifications API";
      this.identificationsFullPath =
        "/open-banking/customers/v1/business/identifications";
      this.financialRelationsFullPath =
        "/open-banking/customers/v1/business/financial-relations";
      this.qualificationFullPath =
        "/open-banking/customers/v1/business/qualifications";
    }
  },
};
</script>

