<template>
  <div class="home layout-wrapper">
    <v-row>
      <v-col cols="12">
        <p class="home__title mb-0">👋 Hello! Choose next step</p>
      </v-col>
    </v-row>

    <v-row class="mt-12">
      <v-col cols="12" sm="12" md="6">
        <v-card flat class="mx-auto home__card home__card__first">
          <v-list-item three-line>
            <v-list-item-content>
              <div class="mb-4">Phase 2</div>

              <v-list-item-title class="mb-3">
                Customer Data
              </v-list-item-title>

              <v-list-item-subtitle>
                Customer Data (call phase 2 APIs)
              </v-list-item-subtitle>
            </v-list-item-content>

            <v-list-item-avatar tile size="36" class="my-0">
              <v-icon small>
                mdi-swap-horizontal
              </v-icon>
            </v-list-item-avatar>
          </v-list-item>

          <v-divider class="mt-12" />

          <v-card-actions class="justify-space-between">
            <v-select
              class="mt-3"
              :items="apiVersions"
              label="API Version"
              dense
              hide-details
              color="#338DAD"
              v-model="selectedApiVersion"
            />

            <v-btn
              rounded
              text
              @click="optionChoice(`customer-data-${selectedApiVersion}`)"
            >
              <span class="mr-2">Go</span>
              
              <v-icon small>
                mdi-arrow-right
              </v-icon>
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>

      <v-col cols="12" sm="12" md="6">
        <v-card flat class="mx-auto home__card">
          <v-list-item three-line>
            <v-list-item-content>
              <div class="mb-4">Phase 3</div>
              
              <v-list-item-title class="mb-3">
                Payments
              </v-list-item-title>

              <v-list-item-subtitle>
                Use the Mock TPP for Phase 3
              </v-list-item-subtitle>
            </v-list-item-content>

            <v-list-item-avatar tile size="36" class="my-0">
              <v-icon small>
                mdi-credit-card-outline
              </v-icon>
            </v-list-item-avatar>
          </v-list-item>

          <v-divider class="mt-12" />

          <v-card-actions class="justify-end">
            <v-btn
              rounded
              text
              @click="optionChoice('payments')"
            >
              <span class="mr-2">Go</span>
              
              <v-icon small>
                mdi-arrow-right
              </v-icon>
            </v-btn>
          </v-card-actions>
        </v-card>
      </v-col>
    </v-row>
  </div>
</template>

<script>
import { mapActions } from "vuex";
import axios from "@/util/axios.js";
import ConfigurationsDialog from "@/components/Dialogs/ConfigurationsDialog.vue";

export default {
  name: "HomeView",
  components: {
    ConfigurationsDialog,
  },

  data: () => {
    return {
      dialog: false,
      statusColour: "red accent-2",
      apiVersions: ["v1", "v2"],
      selectedApiVersion: "v1",
      items: [
        "Authorization and Message Settings",
        "Software Statement Settings",
        "Payments Polling Settings",
      ],
      text: "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat.",
    };
  },

  methods: {
    ...mapActions(["setError"]),

    optionChoice(option) {
      this.$router.push({
        name: "banks",
        query: { option: option },
      });
    },
  },

  async created() {
    try {
      await axios.get("/", { withCredentials: true });
    } catch (error) {
      this.setError(error.message);
    }
  },
};
</script>
