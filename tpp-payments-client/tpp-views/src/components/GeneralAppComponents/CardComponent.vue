<template>
  <v-container>
    <v-card class="text-center" max-width="344" outlined>
      <v-list-item>
        <v-list-item-content>
          <div class="text-overline mb-2">
            {{ title }}
          </div>
          <v-text-field dense outlined placeholder="Resource ID" :value="resourceId"></v-text-field>
        </v-list-item-content>
      </v-list-item>

      <v-card-actions class="mt-n5">
        <v-btn outlined rounded text @click="onClickAccount"> {{ btnText }} </v-btn>
      </v-card-actions>
    </v-card>
    <v-snackbar
        v-model="snackbar"
        :multi-line="multiLine"

      >
        {{ text }}
  
        <template v-slot:action="{ attrs }">
          <v-btn
            color="white"
            text
            v-bind="attrs"
            @click="snackbar = false"
          >
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
      type: String
    },
    path: {
      type: String
    }
  },
  name: "CardComponent",

  data(){
    return {
      multiLine: true,
      snackbar: false,
      text: "You must provide resource ID"
    }
  },
  methods: {
    onClickAccount(){
      if(!this.resourceId){
        this.snackbar = true;
        return;
      }
      this.$emit("fetch-account-data", this.path)
    }
  }
};
</script>