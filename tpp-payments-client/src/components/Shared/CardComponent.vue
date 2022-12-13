<template>
  <v-container>
    <v-card class="text-center" outlined>
      <v-list-item>
        <v-list-item-content>
          <v-row>
            <v-col class="d-flex" cols="12" sm="12" right>
              <v-icon :title="fullPath" medium color="secondary" class="ml-17">
                mdi-information
              </v-icon>
            </v-col>
          </v-row>
          <h1 class="text-overline mb-5 ml-3">
            {{ title }}
          </h1>
          <v-text-field v-if="displayTextField" dense outlined placeholder="Resource ID" v-model="theResourceId">
          </v-text-field>
        </v-list-item-content>
      </v-list-item>

      <v-row justify="center" class="mb-10" v-if="supportsQueryParam">
        <v-dialog v-model="dialog" persistent max-width="600px">
          <template v-slot:activator="{ on, attrs }">
            <v-btn color="primary" v-bind="attrs" v-on="on" outlined rounded text>
              Add Query Params
            </v-btn>
          </template>
          <v-card>
            <v-card-title>
              <span class="text-h5">Add Query Params</span>
            </v-card-title>
            <v-card-text>
              <v-container>
                <v-form v-if="flag === 'ACCOUNT_TRANSACTIONS' || flag === 'ACCOUNT_TRANSACTIONS_CURRENT'" ref="form"
                  v-model="valid" lazy-validation>
                  <v-row>
                    <v-col cols="12" sm="6">
                      <date-picker
                        v-model="queryParams[flag === 'ACCOUNT_TRANSACTIONS_CURRENT' ? 'fromBookingDateMaxLimited' : 'fromBookingDate']"
                        :label="flag === 'ACCOUNT_TRANSACTIONS_CURRENT' ? 'From Booking Date Max Limited' : 'From Booking Date'" />
                    </v-col>
                    <v-col cols="12" sm="6">
                      <date-picker
                        v-model="queryParams[flag === 'ACCOUNT_TRANSACTIONS_CURRENT' ? 'toBookingDateMaxLimited' : 'toBookingDate']"
                        :label="flag === 'ACCOUNT_TRANSACTIONS_CURRENT' ? 'To Booking Date Max Limited' : 'To Booking Date'" />
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-text-field dense outlined label="Page Size" v-model="queryParams['page-size']"></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-text-field dense outlined label="Page" v-model="queryParams['page']"></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="6" v-if="flag === 'ACCOUNT_TRANSACTIONS_CURRENT'">
                      <v-text-field dense outlined label="Pagination Key" v-model="queryParams['pagination-key']">
                      </v-text-field>
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-select :items="['CREDIT', 'DEBIT']" label="Credit Debit Indicator" dense outlined
                        v-model="queryParams['creditDebitIndicator']"></v-select>
                    </v-col>
                  </v-row>
                </v-form>

                <v-form
                  v-if="flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS' || flag === 'CREDIT_CARD_ACCOUNT_BILLS_TRANSACTIONS' || flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT'"
                  ref="form" v-model="valid" lazy-validation>
                  <v-row>
                    <v-col cols="12" sm="6">
                      <date-picker
                        v-model="queryParams[flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'fromTransactionDateMaxLimited' : 'fromTransactionDate']"
                        :label="flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'From Transaction Date Max Limited' : 'To Transaction Date'" />
                    </v-col>
                    <v-col cols="12" sm="6">
                      <date-picker
                        v-model="queryParams[flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'toTransactionDateMaxLimited' : 'toTransactionDate']"
                        :label="flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'To Transaction Date Max Limited' : 'To Transaction Date'" />
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-text-field dense outlined label="Page Size" v-model="queryParams['page-size']"></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-text-field dense outlined label="Page" v-model="queryParams['page']"></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-text-field dense outlined
                        :label="flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'Credit Card Payee MCC' : 'Payee MCC'"
                        v-model="queryParams[flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'creditCardPayeeMCC' : 'payeeMCC']">
                      </v-text-field>
                    </v-col>
                    <v-col cols="12" sm="6" v-if="flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT'">
                      <v-text-field dense outlined label="Pagination Key" v-model="queryParams['pagination-key']">
                      </v-text-field>
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-select
                        :items="['PAGAMENTO', 'TARIFA', 'OPERACOES_CREDITO_CONTRATADAS_CARTAO', 'ESTORNO', 'CASHBACK', 'OUTROS']"
                        label="Transaction Type" dense outlined
                        v-model="queryParams[flag === 'CREDIT_CARD_ACCOUNT_TRANSACTIONS_CURRENT' ? 'creditCardTransactionType' : 'transactionType']">
                      </v-select>
                    </v-col>
                  </v-row>
                </v-form>

                <v-form v-if="flag === 'CREDIT_CARD_ACCOUNT_BILLS'" ref="form" v-model="valid" lazy-validation>
                  <v-row>
                    <v-col cols="12" sm="6">
                      <date-picker v-model="queryParams['fromDueDate']" label="From Due Date" />
                    </v-col>
                    <v-col cols="12" sm="6">
                      <date-picker v-model="queryParams['toDueDate']" label="To Due Date" />
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-text-field dense outlined label="Page Size" v-model="queryParams['page-size']"></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="6">
                      <v-text-field dense outlined label="Page" v-model="queryParams['page']"></v-text-field>
                    </v-col>
                  </v-row>
                </v-form>

                <v-form v-if="flag === 'CREDIT_OPERATION' || flag === 'CUSTOMERS'" ref="form" v-model="valid"
                  lazy-validation>
                  <v-row>
                    <v-col :cols="ApiVersion === 'v2' ? 4 : 6" :md="ApiVersion === 'v2' ? 4 : 6">
                      <v-text-field dense outlined label="Page Size" v-model="queryParams['page-size']"></v-text-field>
                    </v-col>
                    <v-col :cols="ApiVersion === 'v2' ? 4 : 6" :md="ApiVersion === 'v2' ? 4 : 6">
                      <v-text-field dense outlined label="Page" v-model="queryParams['page']"></v-text-field>
                    </v-col>
                    <v-col cols="4" md="4" v-if="ApiVersion === 'v2'">
                      <v-text-field dense outlined label="Pagination Key" v-model="queryParams['pagination-key']">
                      </v-text-field>
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
              <v-btn color="blue darken-1" text @click="saveParams">
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
  </v-container>
</template>

<script>
import { mapActions } from "vuex";
import DatePicker from "@/components/Shared/DatePicker.vue";

export default {
  name: "CardComponent",
  components: {
    DatePicker,
  },
  emits: ["fetch-data", "resource-id-change"],
  props: {
    title: {
      type: String,
    },
    btnText: {
      type: String,
      default: "RUN"
    },
    resourceId: {
      type: String,
    },
    path: {
      type: String,
    },
    displayTextField: {
      type: Boolean,
      default: true
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
    },
    ApiVersion: {
      type: String,
    }
  },
  data() {
    return {
      errorMessage: "You must provide resource ID",
      theResourceId: "",
      dialog: false,
      textFieldLabel: "",
      valid: true,
    };
  },
  methods: {
    ...mapActions(["setError"]),

    onClickAccount() {
      if (this.displayTextField && !this.resourceId) {
        this.setError(this.errorMessage);
        return;
      }

      let queryParams = "";
      if (this.supportsQueryParam) {
        queryParams = this.getPathWithQueryParams(this.queryParams);
      }
      this.$emit("fetch-data", this.path + queryParams);
    },

    async saveParams() {
      this.$refs.form.validate();
      await setTimeout(() => { }, 100);
      if (!this.valid) {
        return;
      }
      this.dialog = false;
    },

    closeQueryParamsDialog() {
      this.queryParams.fromBookingDate = null;
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
