<template>
  <CardWrapper title="Add Query Parameters">
    <template v-slot:card-content>
      <v-col cols="6" sm="12" md="6">
        <v-text-field label="Page Size" v-model="resourcesQueryParams['page-size']" outlined dense>
        </v-text-field>
      </v-col>
      <v-col cols="6" sm="12" md="6">
        <v-text-field label="Page" outlined dense v-model="resourcesQueryParams['page']"></v-text-field>
      </v-col>
      <v-col cols="12" sm="12" md="12" v-if="ApiVersion === 'v2'">
        <v-text-field label="Pagination Key" outlined dense v-model="resourcesQueryParams['pagination-key']">
        </v-text-field>
      </v-col>
      <v-col cols="6" sm="12" md="3" class="mx-auto">
        <v-btn depressed height="2.5rem" width="100%" color="primary" @click="getResourcesByQueryParams">
          Run
        </v-btn>
      </v-col>
    </template>
    <template v-slot:content>
      <CardCode 
        class="mt-8" 
        color="lightblue" 
        title="Resources Request" 
        :code="resourcesRequest" />
      <CardCode 
        class="mt-10" 
        color="lightgreen" 
        title="Resources Response" 
        :code="resourcesResponse"
        :is-error="isResourcesError" />
    </template>
  </CardWrapper>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import CardCode from "@/components/Shared/CardCode.vue";
import CardWrapper from "@/components/Shared/CardWrapper.vue";

import axios from "@/util/axios.js";
import { getPathWithQueryParams } from "@/util/helpers.js";

import { mapGetters, mapActions } from "vuex";

export default {
  name: "ResourcesResponse",
  components: {
    SheetAppBar,
    CardCode,
    CardWrapper,
  },
  data() {
    return {
      getPathWithQueryParams,
      ApiVersion: "",
      resourcesResponse: "",
      resourcesRequest: "",
      resourcesQueryParams: {
        "page-size": null,
        "page": null,
        "pagination-key": ""
      },
      isResourcesError: false,
    };
  },
  computed: {
    ...mapGetters(["ApiOption"]),
  },
  methods: {
    ...mapActions(["setError", "setLoading"]),

    getResourcesByQueryParams() {
      const path = this.getPathWithQueryParams(this.resourcesQueryParams);
      this.getResources(path);
    },

    async getResources(path = "") {
      let response;
      try {
        this.setLoading(true);
        response = await axios.get(`/resources${path}`, { withCredentials: true });
        this.resourcesResponse = response.data.responseData;
        this.resourcesRequest = response.data.requestData;
        this.isResourcesError = false;
        this.setLoading(false);
      } catch (error) {
        this.setError(error.message);
        this.isResourcesError = true;
        this.resourcesResponse = error.response.data.responseData;
        this.resourcesRequest = error.response.data.requestData;
      }
    }
  },
  created() {
    const optionWords = this.ApiOption.split("-");
    this.ApiVersion = optionWords[optionWords.length - 1];
    this.getResources();
  },
};
</script>
