<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Consent Response Menu" />

        <v-sheet min-height="70vh" rounded="lg" v-if="!loading">
          <v-container class="pa-md-12">
            <div class="pa-2"></div>
            <v-row>
              <v-col cols="12" md="8">
                <v-card elevation="2" outlined>
                  <v-card-title style="color: white; background-color: #004c50">Consent Response</v-card-title
                  >
                  <v-card-text>
                    <pre class="pt-4" style="overflow: auto">
{{ consentPayload }}
                    </pre>
                  </v-card-text>
                </v-card>
              </v-col>
              <v-col cols="12" md="3">
                <h3>Consents Granted</h3>
                <v-dialog transition="dialog-bottom-transition" max-width="800">
                  <template v-slot:activator="{ on, attrs }">
                    <v-btn
                      class="ma-1 mt-4"
                      outlined
                      color="primary"
                      v-bind="attrs"
                      v-on="on"
                      v-for="(consent, index) in grantedConsents"
                      :key="index"
                      @click="() => { getConsentInfo(consent)}"
                    >
                      <v-icon left>mdi-information</v-icon>
                      {{ consent.category }}
                    </v-btn>

                  </template>
                  <template v-slot:default="dialog">
                    <v-card>
                      <v-toolbar
                        class="blue-grey darken-4 font-weight-bold"
                        dark
                        >Permissions</v-toolbar
                      >
                      <v-card-text>
                        <div>
                          <v-row>
                            <v-col>
                              <v-card>
                                <v-card-title
                                  class="subheading font-weight-bold mt-6"
                                >
                                  {{ grantedConsentsCategory }}
                                </v-card-title>

                                <v-divider></v-divider>
                                  <v-list-item>
                                    <v-list-item-content>
                                       <strong>Group(s)</strong>
                                      </v-list-item-content
                                    >
                                    <v-list-item-content class="align-end">
                                      <strong>Permissions</strong>
                                    </v-list-item-content>
                                  </v-list-item>
                                <v-divider></v-divider>

                                <v-list
                                  v-for="(consentObj, index) in consentsArr"
                                  :key="index" 
                                  dense>
                                  <v-list-item>
                                    <v-list-item-content>
                                       {{ consentObj.group }}
                                      </v-list-item-content
                                    >
                                    <v-list-item-content class="align-end">
                                      {{ consentObj.permissions }}
                                    </v-list-item-content>
                                  </v-list-item>
                                </v-list>
                              </v-card>
                            </v-col>
                          </v-row>
                        </div>
                      </v-card-text>
                      <v-card-actions class="justify-end">
                        <v-btn text @click="dialog.value = false">Close</v-btn>
                      </v-card-actions>
                    </v-card>
                  </template>
                </v-dialog>
              </v-col>
            </v-row>
            <div class="pa-2"></div>
            <v-divider class="mt-5"></v-divider>
            <h3 class="ma-3 mt-5">What do you want to do: </h3>
            <v-btn color="primary" class="ma-3 mt-5" @click="$router.push('customers')">
              1. Customers
            </v-btn>
            <v-btn color="primary" class="ma-3 mt-5" @click="$router.push('accounts')">
              2. Accounts
            </v-btn>
            <v-btn color="primary" class="ma-3 mt-5" @click="$router.push('credit-card-accounts')">
              3. Credit Card
            </v-btn>
            <v-btn color="primary" class="ma-3 mt-5">
              4. Credit Operations
            </v-btn>
            <v-btn color="primary" class="ma-3 mt-5" @click="$router.push('resources')">
              5. Resources
            </v-btn>
          </v-container>
        </v-sheet>
      </v-col>
      <v-col cols="12" sm="2"> </v-col>
    </v-row>
    <v-overlay :value="loading">
      <v-progress-circular indeterminate size="100"></v-progress-circular>
    </v-overlay>
  </v-main>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import axios from "../util/axios.js";
import { mapGetters } from "vuex";

export default {
  name: "ConsentResponseMenu",
  components: {
    SheetAppBar,
  },
  data() {
    return {
      selected: true,
      loading: true,
      consentPayload: "",
      grantedConsents: [],
      grantedConsentsCategory: "",
      consentsArr: [], 
    };
  },

  methods: {
    getConsentInfo(consentData){
      this.grantedConsentsCategory = consentData.category;
      this.consentsArr = consentData.permissionsArray;
    },

    convertArrayToString(arr){
      let text = "";
      arr.forEach((item) => {
        text = text + " " + item;
      });

      return text;
    }
  },

  computed: {
    ...mapGetters(["consents"])
  },
  created(){
      axios.get("/consent", {withCredentials: true})
      .then((response) => {
        this.consentPayload = response.data.consent;
        this.grantedConsents = response.data.permissionsData;

        let consentsArr = [];
        this.grantedConsents.forEach((consentData) => {
          this.consents.forEach(consent => {
            if(consent.dataCategory === consentData.category && consent.id === consentData.id){
              consentsArr.push(consent);
            }
          });
        });

        //Get all consents that have the same category
        let duplicates = this.grantedConsents.map((consent) => consent.category).filter((consentCategory, i, arr) => arr.indexOf(consentCategory) !== i);
        duplicates =[...new Set(duplicates)]; //unique duplicates

        const consentsList = [];
        duplicates.forEach((consentCategory) => {
          consentsArr.forEach((consentObj) => {
            if(consentCategory === consentObj.dataCategory){
              consentsList.push(consentObj);
            }
          });
        });

        const formatedConsents = [];
        for(let consentCategory of duplicates){

          const consentObj = {
            category: "",
            permissionsArray: [],
          };

          consentsList.forEach((consentItem) => {
            if(consentCategory === consentItem.dataCategory){
              consentObj.category = consentCategory;
              consentObj.permissionsArray.push({
                group: consentItem.group,
                permissions: this.convertArrayToString(consentItem.permissions)
              });
            }
          });

          formatedConsents.push(consentObj);
        }

        //Get all consents except the consents with duplicate categories
        consentsArr = consentsArr.filter((consent) => {
          return !duplicates.includes(consent.dataCategory);
        });

        //Standarise the consent objects format
        consentsArr = consentsArr.map((item) => {
          return {
            category: item.dataCategory,
            permissionsArray: [{
              group: item.group,
              permissions: this.convertArrayToString(item.permissions)
            }]
          }
        });

        this.grantedConsents = [...consentsArr, ...formatedConsents];
        this.loading = false;
      });
  }
};
</script>

