<template>
  <v-main class="banks">
    <v-row>
      <v-col> </v-col>
      <v-col :cols="7">
        <v-sheet min-height="70vh" elevation="20" rounded="lg">
          <SheetAppBar header="Mock TPP" />
          <v-row>
            <v-col> </v-col>
            <v-col cols="10">
              <div class="pa-md-4 transition-swing text-h6" align="center">
                Payment Provider Details
              </div>
              <v-text-field
                label="Search"
                outlined
                clearable
                v-model="search"
              ></v-text-field>

              <v-card class="mx-auto">
                <v-list-item-group
                  style="max-height: 300px"
                  class="overflow-y-auto"
                >
                  <v-list-item
                    v-for="bank in searchBank"
                    :key="bank.id"
                    class="pa-md-4"
                    @click="selectBank(bank.title)"
                  >
                    <v-list-item-avatar>
                      <v-img
                        contain
                        :src="
                          bank.avatar ||
                          'https://ui-avatars.com/api/?name=John+Doe'
                        "
                      ></v-img>
                    </v-list-item-avatar>

                    <v-list-item-content>
                      <v-list-item-title
                        class="text-h5"
                        v-text="bank.title"
                      ></v-list-item-title>
                    </v-list-item-content>
                  </v-list-item>
                </v-list-item-group>
              </v-card>
            </v-col>
            <v-col> </v-col>
          </v-row>
          <Button colour="primary" text="Select" :func="confirmSelectedBank" />
        </v-sheet>
      </v-col>
      <v-col> </v-col>
    </v-row>
    <v-overlay :value="loading">
      <v-progress-circular indeterminate size="100"></v-progress-circular>
    </v-overlay>
  </v-main>
</template>

<script>
// @ is an alias to /src

import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import Button from "@/components/Buttons/Button.vue";
import axios from "../util/axios.js";
import { v1 as uuid } from "uuid";

export default {
  name: "BankView",
  components: {
    SheetAppBar,
    Button,
  },
  data: () => ({
    loading: false,
    selectedBank: "",
    banks: [],
    search: "",
  }),
  methods: {
    selectBank(bankTitle) {
      this.selectedBank = bankTitle;
    },
    confirmSelectedBank() {
      axios.defaults.withCredentials = true;
      this.loading = true;
      axios
        .post(
          "/dcr",
          { bank: this.selectedBank },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        )
        .then((res) => {
          this.$router.push({
            name: "payment-menu",
            params: {
              data: {
                selectedBank: this.selectedBank,
                clientId: res.data.clientId,
              },
            },
          });
        }).catch((err) => {
          console.log(err);
          this.loading = false;
        });
    },
    getBanks(data) {
      for (var i = 0; i < data.length; i++) {
        if (data[i].AuthorisationServers) {
          for (let y = 0; y < data[i].AuthorisationServers.length; y++) {
            this.banks.push({
              id: uuid(),
              avatar: data[i].AuthorisationServers[y].CustomerFriendlyLogoUri,
              title: data[i].AuthorisationServers[y].CustomerFriendlyName,
            });
          }
        }
      }
    },
  },

  computed: {
    searchBank() {
      return this.banks.filter((bank) => {
        return bank.title.toLowerCase().includes(this.search.toLowerCase());
      });
    },
  },

  mounted() {
    axios.get("/banks", { withCredentials: true }).then((response) => {
      this.getBanks(response.data);
    });
  },
};
</script>
