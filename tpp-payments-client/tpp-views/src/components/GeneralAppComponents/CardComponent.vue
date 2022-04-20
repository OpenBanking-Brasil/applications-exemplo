<template>
  <v-container>
    <v-card class="text-center" max-width="344" outlined>
      <v-list-item>
        <v-list-item-content>
          <div class="text-overline mb-2">
            {{ title }}
          </div>
          <v-text-field dense outlined placeholder="Resource ID" :value="accountId"></v-text-field>
        </v-list-item-content>
      </v-list-item>

      <v-card-actions class="mt-n5">
        <v-btn outlined rounded text @click="onClickAccount"> {{ btnText }} </v-btn>
      </v-card-actions>
    </v-card>
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
    accountId: {
      type: String
    },
    path: {
      type: String
    }
  },
  name: "CardComponent",

  data: () => ({}),
  methods: {
    onClickAccount(){
      this.$emit("fetch-account-data", this.path)
    }
  }
};
</script>