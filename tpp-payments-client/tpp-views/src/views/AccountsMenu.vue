<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Accounts Menu" />

        <v-sheet min-height="70vh" rounded="lg">
          <v-container class="pa-md-12">
            <div class="pa-2"></div>
            <v-row>
              <v-col cols="12" md="12">
                <v-card elevation="2" outlined>
                  <v-card-title style="color: white; background-color: #004c50"
                    >Account API Response</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                         <!-- {} -->
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
            <v-row>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Accounts API (accounts/{resouceID}"
                  btnText="GO"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Account Overdraft Limits API (accounts/ {resouceID}/ overdraft-limits)"
                  btnText="GO"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Account Balances API (accounts/ {resouceID}/balances)"
                  btnText="GO"
                />
              </v-col>
              <v-col cols="12" sm="3">
                <CardComponent
                  title="Account Transactions API (accounts/ {resouceID}/ transactions)"
                  btnText="GO"
                />
              </v-col>
            </v-row>
            <div class="pa-2"></div>
            <v-divider class="mt-5 mb-8"></v-divider>
            <v-row>
              <v-col cols="12" sm="4">
                <v-card class="mx-auto" max-width="300" tile>
                  <v-subheader>Available Account IDs</v-subheader>
                  <v-list dense max-height="20vh" style="overflow: auto">
                    <v-list-item-group v-model="selectedItem" color="primary">
                      <v-list-item v-for="(item, i) in items" :key="i">
                        <v-list-item-content>
                          <v-list-item-title
                            v-text="item.text"
                          ></v-list-item-title>
                        </v-list-item-content>
                      </v-list-item>
                    </v-list-item-group>
                  </v-list>
                </v-card>
              </v-col>
              <v-col cols="12" sm="8">
                <v-card elevation="2" outlined>
                  <v-card-title style="color: white; background-color: #004c50"
                    >Response</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
                         <!-- {} -->
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
            </v-row>
          </v-container>
        </v-sheet>
      </v-col>
      <v-col cols="12" sm="2"> </v-col>
    </v-row>
  </v-main>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import CardComponent from "@/components/GeneralAppComponents/CardComponent.vue";

export default {
  name: "AccountsMenu",
  components: {
    SheetAppBar,
    CardComponent,
  },
  data() {
    return {
      selected: true,
      selectedItem: 1,
      items: [
        { text: "hf74tj4g3hjg35h43g534h" }
      ],
    };
  },
};
</script>

