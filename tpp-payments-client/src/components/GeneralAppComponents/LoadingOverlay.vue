<template>
  <v-overlay :value="loading">
    <v-progress-circular indeterminate size="100"></v-progress-circular>
  </v-overlay>
</template>

<script>
import { mapGetters } from "vuex";

export default {
  name: "LoadingOverlay",

  computed: {
    ...mapGetters(["loading"]),
  },
};
</script>