<template>
  <CardWrapper>
    <template v-slot:card-content>
      <v-col cols="6" sm="12" md="6">
        <div class="app-label-holder d-flex justify-space-between">
          <span>Payment Value</span>
        </div>
        <v-text-field class="text-green" placeholder="1335.0" outlined dense></v-text-field>
      </v-col>
      <v-col cols="6" sm="12" md="6">
        <div class="app-label-holder d-flex justify-space-between">
          <span>Revoked By</span>
        </div>
        <v-text-field placeholder="USER" outlined dense></v-text-field>
      </v-col>
    </template>
    <template v-slot:content>
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Patch Response Payload" 
        :code="patchResponse"
        :is-error="isPatchResponseError" />
    </template>
  </CardWrapper>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import CardCode from "@/components/Shared/CardCode.vue";
import CardWrapper from "@/components/Shared/CardWrapper.vue";

export default {
  name: "PatchResponseView",
  components: {
    SheetAppBar,
    CardCode,
    CardWrapper,
  },

  data: () => ({
    patchResponse: {},
    isPatchResponseError: false,
    amount: null,
  }),

  created() {
    if (this.$route.params.status === 200) {
      this.patchResponse = this.$route.params.patchResponse;
      this.isPatchResponseError = true;
    } else {
      this.patchResponse = this.$route.params.patchErrorResponse;
      this.isPatchResponseError = false;
    }
  },
};
</script>
