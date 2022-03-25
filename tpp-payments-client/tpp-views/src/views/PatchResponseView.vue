<template>
  <v-main class="payment-response">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Patch Response" />
        <v-sheet min-height="70vh" rounded="lg">
          <v-container class="pa-md-12">
            <div class="pa-2"></div>
            <v-card elevation="2" outlined color="">
              <v-card-title style="color: white; background-color: #004c50"
                >Information</v-card-title
              >
              <v-card-text>
                <v-row class="pt-6">
                  <v-col cols="12" sm="4" md="4">
                    <b>Payment Value</b>
                    <v-text-field
                      class="text-green"
                      placeholder="1335.0"
                      outlined
                      filled
                    ></v-text-field>
                  </v-col>
                  <v-col cols="12" sm="2" md="2">
                    <b>Revoked By</b>
                    <v-text-field
                      placeholder="USER"
                      outlined
                      filled
                    ></v-text-field>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>
            <div class="pa-2"></div>
            <v-card elevation="2" outlined color="">
              <v-card-title style="color: white; background-color: #9ccc65"
                >Consent Response Payload</v-card-title
              >
              <v-card-text>
                <pre class="pt-4">
                  {{ consentResponse }}
                </pre>
              </v-card-text>
            </v-card>
            <div class="pa-2"></div>
            <v-card elevation="2" outlined color="">
              <v-card-title style="color: white; background-color: #3949ab"
                >Patch Response Payload</v-card-title
              >
              <v-card-text>
                <pre class="pt-4" style="overflow: auto">
                  {{ patchResponse }}
                </pre>
              </v-card-text>
            </v-card>
            <div class="pa-2"></div>
            <v-card elevation="2" outlined color="">
              <v-card-title style="color: white; background-color: #ff5252"
                >Error Response Payload</v-card-title
              >
              <v-card-text>
                <pre class="pt-4">
                  {{ errorResponse }}
                </pre>
              </v-card-text>
            </v-card>
          </v-container>
        </v-sheet>
      </v-col>

      <v-col cols="12" sm="2">
        <BackButton />
      </v-col>
    </v-row>
  </v-main>
</template>

<script>
// @ is an alias to /src

import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import BackButton from "@/components/GeneralAppComponents/BackButton.vue";

export default {
  name: "PatchResponseView",
  components: {
    SheetAppBar,
    BackButton,
  },

  data: () => ({
    consentResponse: {},
    patchResponse: {},
    errorResponse: {},
    amount: null,
  }),

  created(){
    this.patchResponse = this.$route.params.patchResponse;
  }
};
</script>
