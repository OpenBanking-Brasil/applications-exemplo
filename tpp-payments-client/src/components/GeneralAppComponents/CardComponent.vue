<template>
  <v-container>
    <v-card class="text-center" max-width="344" outlined>
      <v-list-item>
        <v-list-item-content>
          <v-row>
            <v-col class="d-flex" cols="12" sm="12" right>
              <v-icon :title="fullPath" medium color="darken-2" class="ml-17">
                mdi-information
              </v-icon>
            </v-col>
          </v-row>
          <h1 class="text-overline mb-5 ml-3">
            {{ title }}
          </h1>
          <v-text-field
            v-if="displayTextField"
            dense
            outlined
            placeholder="Resource ID"
            v-model="theResourceId"
          ></v-text-field>
        </v-list-item-content>
      </v-list-item>

          <v-row justify="center" class="mb-10" v-if="supportsQueryParam && flag === 'ACCOUNT_TRANSACTIONS' || flag === 'ACCOUNT_TRANSACTIONS_CURRENT'">
            <v-dialog v-model="dialog" persistent max-width="600px">
              <template v-slot:activator="{ on, attrs }">
                <v-btn
                  color="primary"
                  v-bind="attrs"
                  v-on="on"
                  outlined rounded text
                >
                  Add Query Params
                </v-btn>
              </template>
              <v-card>
                <v-card-title>
                  <span class="text-h5">Add Query Params</span>
                </v-card-title>
                <v-card-text>
                  <v-container>
                    <v-form ref="form" v-model="valid" lazy-validation>
                      <v-row>
                          <v-col cols="12" sm="6">
                            <h5>Date Format must be : yyyy-mm-dd</h5>
                            <v-text-field
                              dense
                              outlined
                              :label="flag === 'ACCOUNT_TRANSACTIONS_CURRENT' ? 'From Booking Date Max Limited' : 'From Booking Date'"
                              v-model="queryParams[flag === 'ACCOUNT_TRANSACTIONS_CURRENT' ? 'fromBookingDateMaxLimited' : 'fromBookingDate']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <h5>Date Format must be : yyyy-mm-dd</h5>
                            <v-text-field
                              dense
                              outlined
                              :label="flag === 'ACCOUNT_TRANSACTIONS_CURRENT' ? 'To Booking Date Max Limited' : 'To Booking Date'"
                              v-model="queryParams[flag === 'ACCOUNT_TRANSACTIONS_CURRENT' ? 'toBookingDateMaxLimited' : 'toBookingDate']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <v-text-field
                              dense
                              outlined
                              label="Page Size"
                              v-model="queryParams['page-size']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <v-text-field
                              dense
                              outlined
                              label="Page"
                              v-model="queryParams['page']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6" v-if="flag === 'ACCOUNT_TRANSACTIONS_CURRENT'">
                            <v-text-field
                              dense
                              outlined
                              label="Pagination Key"
                              v-model="queryParams['pagination-key']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <v-select
                              :items="['CREDIT', 'DEBIT']"
                              label="Credit Debit Indicator"
                              dense
                              outlined
                              v-model="queryParams['creditDebitIndicator']"
                            ></v-select>
                          </v-col>
                      </v-row>
                    </v-form>
                  </v-container>
                </v-card-text>
                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn color="blue darken-1" text @click="closeQueryParamsDialog">
                    Close
                  </v-btn>
                  <v-btn
                    color="blue darken-1"
                    text
                    @click="saveParams"
                  >
                    Save
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-row>

          <v-row justify="center" class="mb-10" v-if="supportsQueryParam && flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS' || flag === 'CREDIT_CARD_ACCOUNT_BILLS_TRANSACTIONS' || flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT'">
            <v-dialog v-model="dialog" persistent max-width="600px">
              <template v-slot:activator="{ on, attrs }">
                <v-btn
                  color="primary"
                  v-bind="attrs"
                  v-on="on"
                  outlined rounded text
                >
                  Add Query Params
                </v-btn>
              </template>
              <v-card>
                <v-card-title>
                  <span class="text-h5">Add Query Params</span>
                </v-card-title>
                <v-card-text>
                  <v-container>
                    <v-form ref="form" v-model="valid" lazy-validation>
                      <v-row>
                          <v-col cols="12" sm="6">
                            <h5>Date Format must be : yyyy-mm-dd</h5>
                            <v-text-field
                              dense
                              outlined
                              :label="flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'From Transaction Date Max Limited' : 'To Transaction Date'"
                              v-model="queryParams[flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'fromTransactionDateMaxLimited' : 'fromTransactionDate']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <h5>Date Format must be : yyyy-mm-dd</h5>
                            <v-text-field
                              dense
                              outlined
                              :label="flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'To Transaction Date Max Limited' : 'To Transaction Date'"
                              v-model="queryParams[flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'toTransactionDateMaxLimited' : 'toTransactionDate']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <v-text-field
                              dense
                              outlined
                              label="Page Size"
                              v-model="queryParams['page-size']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <v-text-field
                              dense
                              outlined
                              label="Page"
                              v-model="queryParams['page']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <v-text-field
                              dense
                              outlined
                              :label=" flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'Credit Card Payee MCC' : 'Payee MCC'"
                               v-model="queryParams[flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'creditCardPayeeMCC' : 'payeeMCC']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6" v-if="flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT'">
                            <v-text-field
                              dense
                              outlined
                              label="Pagination Key"
                               v-model="queryParams['pagination-key']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <v-select
                              :items="['PAGAMENTO', 'TARIFA', 'OPERACOES_CREDITO_CONTRATADAS_CARTAO', 'ESTORNO', 'CASHBACK', 'OUTROS']"
                              label="Transaction Type"
                              dense
                              outlined
                              v-model="queryParams[flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'creditCardTransactionType' : 'transactionType']"
                            ></v-select>
                          </v-col>
                      </v-row>
                    </v-form>
                  </v-container>
                </v-card-text>
                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn color="blue darken-1" text @click="closeQueryParamsDialog">
                    Close
                  </v-btn>
                  <v-btn
                    color="blue darken-1"
                    text
                    @click="saveParams"
                  >
                    Save
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-row>

          <v-row justify="center" class="mb-10" v-if="supportsQueryParam && flag === 'CREDIT_CARD_ACCOUNT_BILLS'">
            <v-dialog v-model="dialog" persistent max-width="600px">
              <template v-slot:activator="{ on, attrs }">
                <v-btn
                  color="primary"
                  v-bind="attrs"
                  v-on="on"
                  outlined rounded text
                >
                  Add Query Params
                </v-btn>
              </template>
              <v-card>
                <v-card-title>
                  <span class="text-h5">Add Query Params</span>
                </v-card-title>
                <v-card-text>
                  <v-container>
                    <v-form ref="form" v-model="valid" lazy-validation>
                      <v-row>
                          <v-col cols="12" sm="6">
                            <h5>Date Format must be : yyyy-mm-dd</h5>
                            <v-text-field
                              dense
                              outlined
                              label="From Due Date"
                              v-model="queryParams['fromDueDate']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <h5>Date Format must be : yyyy-mm-dd</h5>
                            <v-text-field
                              dense
                              outlined
                              label="To Due Date"
                              v-model="queryParams['toDueDate']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <v-text-field
                              dense
                              outlined
                              label="Page Size"
                              v-model="queryParams['page-size']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <v-text-field
                              dense
                              outlined
                              label="Page"
                              v-model="queryParams['page']"
                            ></v-text-field>
                          </v-col>
                      </v-row>
                    </v-form>
                  </v-container>
                </v-card-text>
                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn color="blue darken-1" text @click="closeQueryParamsDialog">
                    Close
                  </v-btn>
                  <v-btn
                    color="blue darken-1"
                    text
                    @click="saveParams"
                  >
                    Save
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-row>


          <v-row justify="center" class="mb-10" v-if="supportsQueryParam && flag === 'CREDIT_OPERATION'">
            <v-dialog v-model="dialog" persistent max-width="600px">
              <template v-slot:activator="{ on, attrs }">
                <v-btn
                  color="primary"
                  v-bind="attrs"
                  v-on="on"
                  outlined rounded text
                >
                  Add Query Params
                </v-btn>
              </template>
              <v-card>
                <v-card-title>
                  <span class="text-h5">Add Query Params</span>
                </v-card-title>
                <v-card-text>
                  <v-container>
                    <v-form ref="form" v-model="valid" lazy-validation>
                      <v-row>
                          <v-col cols="12" sm="6">
                            <v-text-field
                              dense
                              outlined
                              label="Page Size"
                              v-model="queryParams['page-size']"
                            ></v-text-field>
                          </v-col>
                          <v-col cols="12" sm="6">
                            <v-text-field
                              dense
                              outlined
                              label="Page"
                              v-model="queryParams['page']"
                            ></v-text-field>
                          </v-col>
                      </v-row>
                    </v-form>
                  </v-container>
                </v-card-text>
                <v-card-actions>
                  <v-spacer></v-spacer>
                  <v-btn color="blue darken-1" text @click="closeQueryParamsDialog">
                    Close
                  </v-btn>
                  <v-btn
                    color="blue darken-1"
                    text
                    @click="saveParams"
                  >
                    Save
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-dialog>
          </v-row>


      <v-card-actions class="justify-center mt-n5 mb-3">
        <v-btn outlined rounded text @click="onClickAccount">
          {{ btnText }}
        </v-btn>
      </v-card-actions>

          
    </v-card>
    <v-snackbar v-model="snackbar" :multi-line="multiLine">
      {{ text }}

      <template v-slot:action="{ attrs }">
        <v-btn color="white" text v-bind="attrs" @click="snackbar = false">
          Close
        </v-btn>
      </template>
    </v-snackbar>
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
    resourceId: {
      type: String,
    },
    path: {
      type: String,
    },
    displayTextField: {
      type: Boolean,
    },
    fullPath: {
      type: String,
    },
    flag: {
      type: String,
    },
    supportsQueryParam: {
      type: Boolean,
    },
    queryParams: {
      type: Object,
    },
    getPathWithQueryParams: {
      type: Function,
    }
  },
  name: "CardComponent",
  emits: ["fetch-data", "resource-id-change"],

  data() {
    return {
      multiLine: true,
      snackbar: false,
      text: "You must provide resource ID",
      theResourceId: "",
      dialog: false,
      textFieldLabel: "",
      valid: true,
    };
  },
  methods: {
    onClickAccount() {
      if (this.displayTextField && !this.resourceId) {
        this.snackbar = true;
        return;
      }

      let queryParams = "";
      if(this.supportsQueryParam){
        queryParams = this.getPathWithQueryParams(this.queryParams);
      }
      this.$emit("fetch-data", this.path + queryParams);
    },

    async saveParams(){
      this.$refs.form.validate();
      await setTimeout(() => {}, 100);
      if(!this.valid){
        return;
      }
      this.dialog = false;
    },

    closeQueryParamsDialog(){
        this.queryParams.fromBookingDate =  null;
        this.queryParams.toBookingDate = null;
        this.queryParams["page-size"] = null;
        this.queryParams.page = null;
        this.queryParams.creditDebitIndicator = null;
        this.dialog = false;
    }
  },

  watch: {
    resourceId(resourceId) {
      this.theResourceId = resourceId;
    },
    theResourceId(resourceId) {
      this.$emit("resource-id-change", resourceId);
    },
  },

  created() {
    this.theResourceId = this.resourceId;
  },
};
</script>
