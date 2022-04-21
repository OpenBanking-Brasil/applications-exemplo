<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Consent Menu" />
        <v-container class="pa-md-12" style="background: #ffffff">
          <v-data-table
            :hide-default-footer="true"
            :items-per-page="11"
            :headers="headers"
            :items="consents"
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
import { mapGetters } from "vuex";

export default {
  name: "ConsentMenu",
  components: {
    SheetAppBar,
  },
  data() {
    return {
      loading: false,

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

  computed: {
    ...mapGetters(["consents"])
  },

  methods: {
    continueConsent() {
      this.loading = true;
      axios.defaults.withCredentials = true;
      let bankConsent = window.open("", "_self");
      const selectedConsents = this.consents.filter(
        (rowData) => rowData.consent === true
      );

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
  },
};
</script>

