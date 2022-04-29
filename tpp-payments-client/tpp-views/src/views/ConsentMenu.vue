<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Consent Menu" />
        <v-container class="pa-md-12" style="background: #ffffff">
        <v-row align="center">
          <v-col
            class="d-flex"
            cols="12"
            sm="6"
          >
            <v-select
              :items="cadastroOptions"
              label="Cadastro Options"
              dense
              outlined
              v-model="selectedOption"
            ></v-select>
          </v-col>
        </v-row>
          <v-data-table
            :hide-default-footer="true"
            :items-per-page="11"
            :headers="headers"
            :items="consentsArr"
            class="elevation-1"
          >
            <template v-slot:[`item.permissions`]="{ item }">
              <li v-for="(i, index) in item.permissions" :key="index">
                {{ i }}
              </li>
            </template>

            <template v-slot:[`item.consent`]="{ item }">
              <v-simple-checkbox
                :ripple="false"
                v-model="item.consent"
              ></v-simple-checkbox>
            </template>
          </v-data-table>
          <v-col class="text-right">
            <v-btn
              class="mt-8 mx-auto"
              depressed
              color="primary"
              @click="continueConsent"
            >
              Continue
            </v-btn>
          </v-col>
        </v-container>
      </v-col>
      <v-col cols="12" sm="2"> </v-col>
    </v-row>
    <v-overlay :value="loading">
      <v-progress-circular indeterminate size="100"></v-progress-circular>
    </v-overlay>
  </v-main>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import axios from "../util/axios.js";
import { mapGetters, mapActions } from "vuex";

export default {
  name: "ConsentMenu",
  components: {
    SheetAppBar,
  },
  data() {
    return {
      loading: false,
      cadastroOptions: ['PF', 'PJ'],
      selectedOption: "PF",
      consentsArr: [],
      consentsDeepCopy: [],
      headers: [
        {
          text: "CATEGORIA DE DADOS",
          align: "start",
          sortable: false,
          value: "dataCategory",
        },
        { text: "AGRUPAMENTO", value: "group" },
        { text: "PERMISSIONS", value: "permissions" },
        { text: "GIVE CONSENT", value: "consent" },
      ],
    };
  },

  watch: {
    selectedOption(val){
      this.consentsArr = this.consentsDeepCopy.filter((consent) => {
        if(val === "PF"){
          return consent.id !== 3 && consent.id !== 4; 
        } else if(val === "PJ"){
          return consent.id !== 1 && consent.id !== 2;
        }
      });
    }
  },

  computed: {
    ...mapGetters(["consents"])
  },

  created(){
    this.setCadastroOption(this.selectedOption);
    this.consentsDeepCopy = JSON.parse(JSON.stringify(this.consents));
    this.consentsArr = this.consentsDeepCopy.filter((consent) => consent.id !== 3 && consent.id !== 4);
    
  },

  methods: {
    ...mapActions(["setCadastroOption"]),
    continueConsent() {
      this.setCadastroOption(this.selectedOption);
      this.loading = true;
      axios.defaults.withCredentials = true;
      const selectedConsents = this.consentsArr.filter(
        (rowData) => rowData.consent === true
      );

      const bankConsent = window.open("", "_self");
      axios
        .post(
          "/consent",
          { permissionsArr: selectedConsents },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        )
        .then((res) => {
          bankConsent.location.href = res.data.authUrl;
          this.loading = false;
        })
        .catch((err) => {
          console.log(err.message);
          this.loading = false;
        });
    },
  }
};
</script>

