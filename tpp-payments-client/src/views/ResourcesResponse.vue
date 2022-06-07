<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Resources Response" />

        <v-sheet min-height="70vh" rounded="lg">
          <v-container class="pa-md-12">
            <div class="pa-2"></div>
            <v-row>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title class="white--text cyan darken-4"
                    >Resources Response</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                        {{ resourcesResponse }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>

            <v-row>
              <v-col cols="4" md="4">
                <v-text-field
                  label="Page Size"
                  placeholder="Page Size"
                  v-model="resourcesQueryParams['page-size']"
                  outlined
                ></v-text-field>
              </v-col>
              <v-col cols="4" md="4">
                <v-text-field
                  label="Page"
                  placeholder="Page"
                  outlined
                  v-model="resourcesQueryParams['page']"
                ></v-text-field>
              </v-col>
              <v-col cols="4" md="4">
                <v-btn
                  depressed
                  height="3.4rem"
                  width="100%"
                  color="primary"
                  @click="getResourcesByQueryParams"
                >
                  Run
                </v-btn>
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
import BackButton from "@/components/GeneralAppComponents/BackButton.vue";
import axios from "../util/axios.js";

export default {
  name: "ResourcesResponse",
  components: {
    SheetAppBar,
    BackButton,
  },
  data() {
    return {
      resourcesResponse: "",
      resourcesQueryParams: {
        "page-size": null,
        "page": null,
        "accountType": ""
      }
    };
  },
  methods: {
    getResourcesByQueryParams(){
      this.accountIDs = [];
      let path = "";
      let isFirstIteration = true;
      for(let queryParam in this.resourcesQueryParams){
        if(this.resourcesQueryParams[queryParam]){
          if(!isFirstIteration){
            path += `&${queryParam}=${this.resourcesQueryParams[queryParam]}`;
          } else {
            isFirstIteration = false;
            path = `?${queryParam}=${this.resourcesQueryParams[queryParam]}`;
          }
        }
      }

      this.getResources(path);
    },

    getResources(path=""){
      axios.get(`/resources${path}`, { withCredentials: true }).then((response) => {
        this.resourcesResponse = response.data;
      });
    }
  },
  created() {
    this.getResources();
  },
};
</script>
