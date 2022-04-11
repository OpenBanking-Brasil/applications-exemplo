<template>
  <v-main class="consent-menu">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Consent Menu" />
        <v-container class="pa-md-12" style="background: #ffffff">
          <v-data-table
            :hide-default-footer="true"
            :items-per-page="11"
            :headers="headers"
            :items="consents"
            class="elevation-1"
          >
            <template v-slot:[`item.consent`]="{ item }">
              <v-simple-checkbox
                :ripple="false"
                v-model="item.consent"
              ></v-simple-checkbox>
            </template>
          </v-data-table>
          <v-col class="text-right">
            <v-btn class="mt-8 mx-auto" depressed color="primary" @click="continueConsent">
              Continue
            </v-btn>
          </v-col>
        </v-container>
      </v-col>
      <v-col cols="12" sm="2"> </v-col>
    </v-row>
  </v-main>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";

export default {
  name: "ConsentMenu",
  components: {
    SheetAppBar,
  },
  data() {
    return {
      consents: [
        {
          dataCategory: "Cadastro",
          group: "Dados Cadastrais PF",
          permissions: [
            "CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ",
            " RESOURCES_READ",
          ],
          consent: false,
        },
        {
          dataCategory: "Cadastro",
          group: "Informações complementares PF",
          permissions: [
            "CUSTOMERS_PERSONAL_ADITTIONALINFO_READ",
            " RESOURCES_READ",
          ],
          consent: false,
        },
        {
          dataCategory: "Cadastro",
          group: "Dados Cadastrais PJ",
          permissions: [
            "CUSTOMERS_BUSINESS_IDENTIFICATIONS_READ",
            " RESOURCES_READ",
          ],
          consent: false,
        },
        {
          dataCategory: "Cadastro",
          group: "Informações complementares PJ",
          permissions: [
            "CUSTOMERS_BUSINESS_ADITTIONALINFO_READ",
            " RESOURCES_READ",
          ],
          consent: false,
        },
        {
          dataCategory: "Contas",
          group: "Saldos",
          permissions: [
            "ACCOUNTS_READ",
            " ACCOUNTS_BALANCES_READ",
            " RESOURCES_READ",
          ],
          consent: false,
        },
        {
          dataCategory: "Contas",
          group: "Limites",
          permissions: [
            "ACCOUNTS_READ",
            " ACCOUNTS_OVERDRAFT_LIMITS_READ",
            " RESOURCES_READ",
          ],
          consent: false,
        },
        {
          dataCategory: "Contas",
          group: "Extratos",
          permissions: [
            "ACCOUNTS_READ",
            " ACCOUNTS_TRANSACTIONS_READ",
            " RESOURCES_READ",
          ],
          consent: false,
        },
        {
          dataCategory: "Cartão de Crédito",
          group: "Limites",
          permissions: [
            "CREDIT_CARDS_ACCOUNTS_READ",
            " CREDIT_CARDS_ACCOUNTS_LIMITS_READ",
            " RESOURCES_READ",
          ],
          consent: false,
        },
        {
          dataCategory: "Cartão de Crédito",
          group: "Transações",
          permissions: [
            "CREDIT_CARDS_ACCOUNTS_READ",
            " CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ",
            " RESOURCES_READ",
          ],
          consent: false,
        },
        {
          dataCategory: "Cartão de Crédito",
          group: "Faturas",
          permissions: [
            "CREDIT_CARDS_ACCOUNTS_READ",
            " CREDIT_CARDS_ACCOUNTS_BILLS_READ",
            " CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ",
            " RESOURCES_READ",
          ],
          consent: false,
        },
        {
          dataCategory: "Operações de Crédito",
          group: "Dados do Contrato",
          permissions: [
            "LOANS_READ",
            " LOANS_WARRANTIES_READ",
            " LOANS_SCHEDULED_INSTALMENTS_READ",
            " LOANS_PAYMENTS_READ",
            " FINANCINGS_READ",
            " FINANCINGS_WARRANTIES_READ",
            " FINANCINGS_SCHEDULED_INSTALMENTS_READ",
            " FINANCINGS_PAYMENTS_READ",
            " UNARRANGED_ACCOUNTS_OVERDRAFT_READ",
            " UNARRANGED_ACCOUNTS_OVERDRAFT_WARRANTIES_READ",
            " UNARRANGED_ACCOUNTS_OVERDRAFT_SCHEDULED_INSTALMENTS_READ",
            " UNARRANGED_ACCOUNTS_OVERDRAFT_PAYMENTS_READ",
            " INVOICE_FINANCINGS_READ",
            " INVOICE_FINANCINGS_WARRANTIES_READ",
            " INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ",
            " INVOICE_FINANCINGS_PAYMENTS_READ",
            " RESOURCES_READ",

          ],
          consent: false,
        },
      ],
      headers: [
        {
          text: "CATEGORIA DE DADOS",
          align: "start",
          sortable: false,
          value: "dataCategory",
        },
        { text: "AGRUPAMENTO", value: "group" },
        { text: "PERMISSIONS", value: "permissions" },
        { text: "GIVE CONSENT", value: "consent" },
      ],
    };
  },

  methods : {
    continueConsent(){
      this.consents.forEach((rowData) => {
        if(rowData.consent){
          console.log(rowData.group);
        }
      });
    }
  }
};
</script>

