<template>
  <v-main class="patch">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Patch Details" />
        <v-sheet min-height="70vh" rounded="lg">
          <v-container class="pa-md-12">
            <div class="pa-2"></div>
            <v-card elevation="2" outlined color="">
              <v-card-title class="white--text cyan darken-4"
                >Logged User</v-card-title
              >
              <v-card-text>
                <v-row class="pa-6">
                  <v-col cols="12" sm="4" md="4">
                    <b>Document Identification </b
                    ><v-icon
                      small
                      title="
                type: string
            maxLength: 14
            description: Número do documento de identificação oficial do titular pessoa jurídica.
            example: '11111111111111'
            pattern: '^\d{14}$'"
                    >
                      mdi-information
                    </v-icon>
                    <v-text-field
                      class="text-green"
                      placeholder="76109277673"
                      outlined
                      filled
                      id="loggedUser_document_identification"
                      v-model="formData.document_identification"
                    ></v-text-field>
                  </v-col>
                  <v-col cols="12" sm="4" md="4">
                    <b>Document Rel </b
                    ><v-icon
                      small
                      title="
                type: string
            maxLength: 4
            description: Tipo do documento de identificação oficial do titular pessoa jurídica.
            example: CNPJ
            pattern: '^[A-Z]{4}$'"
                    >
                      mdi-information
                    </v-icon>
                    <v-text-field
                      placeholder="CPF"
                      outlined
                      filled
                      id="loggedUser_document_rel"
                      v-model="formData.document_rel"
                    ></v-text-field>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>
            <div class="pa-2"></div>
            <v-card elevation="2" outlined color="">
              <v-card-title class="white--text cyan darken-4"
                >Information</v-card-title
              >
              <v-card-text>
                <v-row class="pa-6">
                  <v-col cols="12" sm="4" md="4">
                    <b>Revoked By </b
                    ><v-icon
                      small
                      title="
                type: string
                maxLength: 8
                enum:
                - USER
                - ASPSP
                - TPP
                example: USER
                description:
                Define qual das partes envolvidas na transação está realizando a revogação. Valores possíveis:
                - USER (Revogado pelo usuário)
                - ASPSP (Provedor de serviços de pagamento para serviços de conta - Detentora de conta)
                - TPP (Instituições Provedoras - iniciadora de pagamentos)"
                    >
                      mdi-information
                    </v-icon>
                    <v-text-field
                      placeholder="USER"
                      outlined
                      filled
                      id="revokedBy"
                      v-model="formData.revoked_by"
                    ></v-text-field>
                  </v-col>
                  <v-col cols="12" sm="4" md="4">
                    <b>Code </b
                    ><v-icon
                      small
                      title="
                type: string
                maxLength: 22
                enum:
                - FRAUD
                - ACCOUNT_CLOSURE
                - OTHER
                example: OTHER
                description: Define o código da razão pela qual o consentimento foi revogado.
                Valores possíveis:
                FRAUD - Indica suspeita de fraude
                ACCOUNT_CLOSURE - Indica que a conta do usuário foi encerrada
                OTHER - Indica que motivo do cancelamento está fora dos motivos pré-estabelecidos."
                    >
                      mdi-information
                    </v-icon>
                    <v-text-field
                      placeholder="OTHER"
                      outlined
                      filled
                      id="revokedCode"
                      v-model="formData.code"
                    ></v-text-field>
                  </v-col>
                  <v-col cols="12" sm="4" md="4">
                    <b>Additional Information </b
                    ><v-icon
                      small
                      title="
                type: string
                maxLength: 140
                pattern: '[\w\W\s]*'
                example: Não quero mais o serviço
                description: 
                Contém informações adicionais definidas pelo requisitante da revogação.
                [Restrição] Deverá ser obrigatoriamente preenchido quando a revogação for feita pela iniciadora ou pela detentora unilateralmente, ou seja, quando o campo revokedBy for igual a TPP ou ASPSP e o motivo de revogação for OTHER."
                    >
                      mdi-information
                    </v-icon>
                    <v-text-field
                      placeholder="Não quero mais o serviço"
                      outlined
                      filled
                      id="revokedAdditionalInfo"
                      v-model="formData.additional_info"
                    ></v-text-field>
                  </v-col>
                </v-row>
              </v-card-text>
            </v-card>
            <v-container class="pt-16">
              <v-col align="center">
                <v-btn
                  depressed
                  color="primary"
                  x-large
                  :loading="loading"
                  @click="revokePayment"
                  ><v-icon left> mdi-file </v-icon>
                  Confirm Patch
                </v-btn>
              </v-col>
            </v-container>
          </v-container>
        </v-sheet>
      </v-col>

      <v-col cols="12" sm="2">
        <BackButton path="payment-menu" />
      </v-col>
    </v-row>
  </v-main>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import BackButton from "@/components/GeneralAppComponents/BackButton.vue";
import axios from "../util/axios.js";

export default {
  name: "PatchView",
  components: {
    SheetAppBar,
    BackButton,
  },

  data: () => ({
    loading: false,
    formData: {
      document_identification: 76109277673,
      document_rel: "CPF",
      revoked_by: "USER",
      code: "OTHER",
      additional_info: "Não quero mais o serviço",
    },
  }),

  methods: {
    async revokePayment() {
      this.loading = true;
      let formBody = [];
      for (let property in this.formData) {
        const encodedKey = encodeURIComponent(property);
        const encodedValue = encodeURIComponent(this.formData[property]);
        formBody.push(encodedKey + "=" + encodedValue);
      }
      formBody = formBody.join("&");

      axios.defaults.withCredentials = true;

      let response;
      try {
        response = await axios.patch("payments/revoke-payment", formBody, {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
        });

        this.$router.push({
          name: "patch-response",
          params: {
            patchResponse: response.data,
            status: response.status,
          },
        });
      } catch (error) {
        this.loading = false;
        this.$router.push({
          name: "patch-response",
          params: {
            patchErrorResponse: error.response.data,
            status: error.response.status,
          },
        });
      }
    },
  },
};
</script>
