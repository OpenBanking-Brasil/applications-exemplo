<template>
  <v-snackbar
    v-model="snackbar"
    :timeout="timeout"
    :multi-line="multiLine"
    :color="statusColour"
  >
    {{ messageText }}

    <template v-slot:action="{ attrs }">
      <v-btn color="white" text v-bind="attrs" @click="snackbar = false">
        Close
      </v-btn>
    </template>
  </v-snackbar>
</template>

<script>
import { mapActions, mapGetters } from "vuex";

export default {
  name: "SnackBar",

  data() {
    return {
      timeout: 5000,
      multiLine: true,
      snackbar: false,
      statusColour: "red accent-2",
    };
  },

  computed: {
    ...mapGetters(["error", "info"]),

    messageText() {
      if (this.error) {
        this.statusColour = "red accent-2";
        this.snackbar = true;
      } else if (this.info) {
        this.statusColour = "success";
        this.snackbar = true;
      }

      return this.error || this.info;
    },
  },

  watch: {
    snackbar: {
      deep: true,
      handler(v) {
        if(!v) {
          this.setError("");
          this.setInfo("");
        }
      },
    }
  },

  methods: {
    ...mapActions(["setError", "setInfo"]),
  },
};
</script>