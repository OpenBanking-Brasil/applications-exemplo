<template>
  <v-main class="home">
    <v-row>
      <v-col> </v-col>
      <v-col :cols="7">
        <v-sheet min-height="70vh" elevation="20" rounded="lg">
          <v-app-bar class="blue darken-4 rounded-lg rounded-b-0">
            <v-spacer />
            <div class="white--text transition-swing text-h3">Home</div>
            <v-spacer />
          </v-app-bar>
          <v-row class="pt-12"> </v-row>
          <v-row class="pt-12"> </v-row>
          <v-row class="pt-12">
            <v-col>
              <v-card class="mx-auto" max-width="344" outlined>
                <v-list-item three-line>
                  <v-list-item-content>
                    <div class="text-overline mb-4">Phase 2</div>
                    <v-list-item-title class="text-h5 mb-1">
                      Customer Data
                    </v-list-item-title>
                    <v-list-item-subtitle
                      >This is still a work-in-progress</v-list-item-subtitle
                    >
                  </v-list-item-content>

                  <v-list-item-avatar tile size="40"
                    ><v-icon large color="grey darken-2">
                      mdi-swap-horizontal
                    </v-icon>
                  </v-list-item-avatar>
                </v-list-item>
                <v-card-actions>
                  <v-btn outlined rounded text @click="optionChoice('customer-data')">
                    Go
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-col>

            <v-col>
              <v-card class="mx-auto" max-width="344">
                <v-list-item three-line>
                  <v-list-item-content>
                    <div class="text-overline mb-4">Phase 3</div>
                    <v-list-item-title class="text-h5 mb-1">
                      Payments
                    </v-list-item-title>
                    <v-list-item-subtitle
                      >Use the Mock TPP for Phase 3
                      Payments</v-list-item-subtitle
                    >
                  </v-list-item-content>

                  <v-list-item-avatar tile size="40"
                    ><v-icon large color="green darken-2">
                      mdi-cash-fast
                    </v-icon>
                  </v-list-item-avatar>
                </v-list-item>

                <v-card-actions>
                  <v-btn outlined rounded text @click="optionChoice('payments')">
                    Go
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-col>
          </v-row>
        </v-sheet>
      </v-col>
      <v-col> </v-col>
    </v-row>
  </v-main>
</template>

<script>
import { mapActions } from "vuex";

export default {
  name: "HomeView",
  components: {},

  methods: {
    ...mapActions(["setOption"]),
    optionChoice(option) {
      this.setOption(option);
      this.$router.push({
        name: "banks",
        params: {
          data: {
            selectedOption: option,
          },
        },
      });
    },
  },
};
</script>
