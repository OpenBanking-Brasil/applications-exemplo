<template>
  <v-container>
    <v-sheet min-height="8vh" max-width="8vh" rounded="lg" fill-height
      ><v-row justify="center"
        ><v-col align="center">
          <v-icon x-large @click="goBack"
            >mdi-arrow-left-circle</v-icon
          ></v-col
        ></v-row
      >
    </v-sheet>
  </v-container>
</template>

<script>
export default {
  name: "BackButton",
  props: {
    path: {
      type: String,
    }
  },

  data: () => ({}),
  methods: {
    goBack(){
      if(this.path){
        this.$router.push(this.path);
      } else {
        window.history.back();
      }
    }
  }
};
</script>