<template>
  <v-container>
    <v-card class="text-center" max-width="344" outlined>
      <v-list-item>
        <v-list-item-content>
          <v-row>
            <v-col class="d-flex" cols="12" sm="12" right>
              <v-icon :title="fullPath" medium color="darken-2" class="ml-17">
                mdi-information
              </v-icon>
            </v-col>
          </v-row>
          <h1 class="text-overline mb-5 ml-3">
            {{ title }}
          </h1>
          <v-text-field
            v-if="displayTextField"
            dense
            outlined
            placeholder="Resource ID"
            v-model="theResourceId"
          ></v-text-field>
        </v-list-item-content>
      </v-list-item>

      <v-card-actions class="justify-center mt-n5 mb-3">
        <v-btn outlined rounded text @click="onClickAccount">
          {{ btnText }}
        </v-btn>
      </v-card-actions>
    </v-card>
    <v-snackbar v-model="snackbar" :multi-line="multiLine">
      {{ text }}

      <template v-slot:action="{ attrs }">
        <v-btn color="white" text v-bind="attrs" @click="snackbar = false">
          Close
        </v-btn>
      </template>
    </v-snackbar>
  </v-container>
</template>

<script>
export default {
  props: {
    title: {
      type: String,
    },
    btnText: {
      type: String,
    },
    resourceId: {
      type: String,
    },
    path: {
      type: String,
    },
    displayTextField: {
      type: Boolean,
    },
    fullPath: {
      type: String,
    },
  },
  name: "CardComponent",
  emits: ["fetch-data", "resource-id-change"],

  data() {
    return {
      multiLine: true,
      snackbar: false,
      text: "You must provide resource ID",
      theResourceId: ""
    };
  },
  methods: {
    onClickAccount() {
      if (this.displayTextField && !this.resourceId) {
        this.snackbar = true;
        return;
      }
      this.$emit("fetch-data", this.path);
    },
  },

  watch: {
    resourceId(resourceId){
      this.theResourceId = resourceId;
    },
    theResourceId(resourceId){
      this.$emit("resource-id-change", resourceId);
    }
  },
  
  created(){
    this.theResourceId = this.resourceId;
  }

};
</script>