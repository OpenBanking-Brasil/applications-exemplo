<template>
  <div class="patch layout-wrapper">
    <v-form @submit.prevent="revokePayment">
      <v-sheet>
        <v-card elevation="0" class="pa-0">
          <v-card-title class="px-0 pt-0 pb-5">Logged User</v-card-title>
          <v-card-text class="px-0">
            <v-row class="pa-0 align-end">
              <v-col cols="6" md="6" sm="12">
                <div class="app-label-holder d-flex justify-space-between">
                  <span>Document Identification</span>
                  <v-icon small color="secondary" :title="documentIdentificationTitle">
                    mdi-information
                  </v-icon>
                </div>
                <v-text-field class="text-green" placeholder="76109277673" outlined dense
                  id="loggedUser_document_identification" v-model="formData.document_identification"></v-text-field>
              </v-col>
              <v-col cols="6" md="6" sm="12">
                <div class="app-label-holder d-flex justify-space-between">
                  <span>Document Rel</span>
                  <v-icon small color="secondary" :title="documentRelTitle">
                    mdi-information
                  </v-icon>
                </div>
                <v-text-field placeholder="CPF" outlined dense id="loggedUser_document_rel"
                  v-model="formData.document_rel"></v-text-field>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
        <v-card elevation="0" class="pa-0">
          <v-card-title class="px-0 pt-0 pb-5">Information</v-card-title>
          <v-card-text class="px-0">
            <v-row class="pa-0">
              <v-col cols="6" md="6" sm="12">
                <div class="app-label-holder d-flex justify-space-between">
                  <span>Revoked By </span>
                  <v-icon small color="secondary" :title="revokedByTitle">
                    mdi-information
                  </v-icon>
                </div>
                <v-text-field placeholder="USER" outlined dense id="revokedBy" v-model="formData.revoked_by">
                </v-text-field>
              </v-col>
              <v-col cols="6" md="6" sm="12">
                <div class="app-label-holder d-flex justify-space-between">
                  <span>Code </span>
                  <v-icon small color="secondary" :title="codeTitle">
                    mdi-information
                  </v-icon>
                </div>
                <v-text-field placeholder="OTHER" outlined dense id="revokedCode" v-model="formData.code">
                </v-text-field>
              </v-col>
              <v-col cols="12">
                <div class="app-label-holder d-flex justify-space-between">
                  <span>Additional Information</span>
                  <v-icon small color="secondary" :title="additionalInformationTitle">
                    mdi-information
                  </v-icon>
                </div>
                <v-text-field placeholder="Não quero mais o serviço" outlined dense id="revokedAdditionalInfo"
                  v-model="formData.additional_info"></v-text-field>
              </v-col>
            </v-row>
          </v-card-text>
        </v-card>
        <v-row class="layout-wrapper__bottom-btns">
          <v-col class="pa-0">
            <v-btn block depressed height="57" color="primary" class="consent-create-btn" type="submit"
              :loading="loading">
              <v-icon left>mdi-file </v-icon>
              <span>Confirm Patch</span>
            </v-btn>
          </v-col>
        </v-row>
      </v-sheet>
    </v-form>
  </div>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import axios from "@/util/axios.js";

import { documentIdentificationTitle, documentRelTitle, revokedByTitle, codeTitle, additionalInformationTitle } from "@/config/patchView.js";

export default {
  name: "PatchView",
  components: {
    SheetAppBar,
  },

  data: () => ({
    documentIdentificationTitle,
    documentRelTitle,
    revokedByTitle,
    codeTitle,
    additionalInformationTitle,
    loading: false,
    formData: {
      document_identification: 76109277673,
      document_rel: "CPF",
      revoked_by: "USER",
      code: "OTHER",
      additional_info: "Não quero mais o serviço",
    },
  }),

  methods: {
    async revokePayment() {
      this.loading = true;
      let formBody = [];
      for (let property in this.formData) {
        const encodedKey = encodeURIComponent(property);
        const encodedValue = encodeURIComponent(this.formData[property]);
        formBody.push(encodedKey + "=" + encodedValue);
      }
      formBody = formBody.join("&");

      axios.defaults.withCredentials = true;

      let response;
      try {
        response = await axios.patch("payments/revoke-payment", formBody, {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
        });

        this.$router.push({
          name: "patch-response",
          params: {
            patchResponse: response.data,
            status: response.status,
          },
        });
      } catch (error) {
        this.loading = false;
        this.$router.push({
          name: "patch-response",
          params: {
            patchErrorResponse: error.response.data,
            status: error.response.status,
          },
        });
      }
    },
  },
};
</script>
<style scoped>
.layout-wrapper {
  padding: 40px 40px 0 40px;
}
</style>