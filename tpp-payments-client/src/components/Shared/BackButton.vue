<template>
  <div v-if="path !== undefined">
    <v-btn v-if="text" text color="primary" @click="goBack">
      <v-icon medium>mdi-chevron-left</v-icon>
      <span>GO BACK</span>
    </v-btn>
    <v-btn v-if="!text" class="mr-1 round-button" rounded text icon color="black" @click="goBack">
      <v-icon small>mdi-chevron-left</v-icon>
    </v-btn>
  </div>
</template>

<script>
export default {
  name: "BackButton",
  props: {
    text: {
      type: Boolean,
      default: false,
    }
  },
  methods: {
    goBack() {
      if (this.path) {
        this.$router.push(this.path);
      } else {
        window.history.back();
      }
      window.scrollTo(0, 0);
    },
  },
  computed: {
    path() {
      return this.$route.meta.backNav;
    },
  },
};
</script>

<style scoped>
.round-button i {
  background-color: white;
  border-radius: 50%;
}
</style>
