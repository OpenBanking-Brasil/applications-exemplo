<template>
  <v-main class="payment-details">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Payment Details" />
        <v-form @submit.prevent="createPayment">
          <v-sheet min-height="70vh" rounded="lg">
            <v-container class="pa-md-12">
              <div class="pa-2"></div>
              <v-row>
                <v-col cols="12" sm="9">
                  <v-card elevation="2" outlined color="">
                    <v-card-title class="white--text cyan darken-4"
                      >Payment</v-card-title
                    >
                    <v-card-text class="pa-6">
                      <v-row class="pl-6">
                        <v-col cols="12" sm="6" md="6">
                          <b>Amount </b
                          ><v-icon
                            small
                            title="
                    type: string
                minLength: 4
                maxLength: 19
                pattern: '^((\d{1,16}\.\d{2}))$'
                example: '100000.12'
                description: Valor da transação com 2 casas decimais."
                          >
                            mdi-information
                          </v-icon>
                          <v-text-field
                            class="text-green"
                            placeholder="1335.00"
                            outlined
                            filled
                            name="payment_amount"
                            id="payment_amount"
                            v-model="formDataObj.payment_amount"
                          ></v-text-field>
                        </v-col>
                        <v-col cols="12" sm="6" md="6">
                          <b>Type</b
                          ><v-icon
                            small
                            title="
                    EnumPixPaymentType:
                  enum:
                    - PIX
                  example: PIX
                EnumTedPaymentType:
                  enum:
                    - TED
                  example: TED
                EnumTefPaymentType:
                  enum:
                    - TEF
                  example: TEF
                description: Este campo define o tipo de pagamento que será iniciado após a autorização do consentimento."
                          >
                            mdi-information
                          </v-icon>
                          <v-text-field
                            placeholder="PIX"
                            outlined
                            filled
                            name="payment_type"
                            id="payment_type"
                            v-model="formDataObj.payment_type"
                          ></v-text-field>
                        </v-col>
                      </v-row>
                      <v-row class="pl-6">
                        <v-col cols="12" sm="6" md="6">
                          <b>Proxy </b
                          ><v-icon
                            small
                            title="
                    type: string
                maxLength: 77
                pattern: '[\w\W\s]*'
                example: '12345678901'
                description: 
                Chave cadastrada no DICT pertencente ao recebedor. Os tipos de chaves podem ser: telefone, e-mail, cpf/cnpj ou chave aleatória.
                No caso de telefone celular deve ser informado no padrão E.1641.
                Para e-mail deve ter o formato xxxxxxxx@xxxxxxx.xxx(.xx) e no máximo 77 caracteres.
                No caso de CPF deverá ser informado com 11 números, sem pontos ou traços.
                Para o caso de CNPJ deverá ser informado com 14 números, sem pontos ou traços.
                No caso de chave aleatória deve ser informado o UUID gerado pelo DICT, conforme formato especificado na RFC41223.
                Se informado, a detentora da conta deve validar o proxy no DICT quando localInstrument for igual a DICT, QRDN ou QRES e validar o campo creditorAccount.
                Esta validação é opcional caso o localInstrument for igual a INIC.
                [Restrição]
                Se localInstrument for igual a MANU, o campo proxy não deve ser preenchido.
                Se localInstrument for igual INIC, DICT, QRDN ou QRES, o campo proxy deve ser sempre preenchido com a chave Pix."
                          >
                            mdi-information
                          </v-icon>
                          <v-text-field
                            placeholder="12345678901"
                            outlined
                            filled
                            name="payment_details_proxy"
                            id="payment_details_proxy"
                            v-model="formDataObj.payment_details_proxy"
                          ></v-text-field>
                        </v-col>
                        <v-col cols="12" sm="6" md="6">
                          <b>Local Instrument </b
                          ><v-icon
                            small
                            title="
                    type: string
                maxLength: 4
                enum:
                - MANU
                - DICT
                - QRDN
                - QRES
                - INIC
                example: DICT
                description: |
                Especifica a forma de iniciação do pagamento:
                - MANU - Inserção manual de dados da conta transacional
                - DICT - Inserção manual de chave Pix
                - QRDN - QR code dinâmico
                - QRES - QR code estático
                - INIC - Indica que o recebedor (creditor) contratou o Iniciador de Pagamentos especificamente para realizar iniciações de pagamento em que o beneficiário é previamente conhecido."
                          >
                            mdi-information
                          </v-icon>
                          <v-text-field
                            placeholder="DICT"
                            outlined
                            filled
                            name="payment_details_localInstrument"
                            id="payment_details_localInstrument"
                            v-model="formDataObj.payment_details_localInstrument"
                          ></v-text-field>
                        </v-col>
                      </v-row>
                      <v-row class="pl-6">
                        <v-col cols="12" sm="6" md="6">
                          <b>Credit Account Number </b
                          ><v-icon
                            small
                            title="
                    type: string
                minLength: 3
                maxLength: 20
                pattern: '^\d{3,20}$'
                example: '1234567890'
                description: 
                  Deve ser preenchido com o número da conta do usuário recebedor, com dígito verificador (se este existir), se houver valor alfanumérico, este deve ser convertido para 0."
                          >
                            mdi-information
                          </v-icon>
                          <v-text-field
                            placeholder="1234567890"
                            outlined
                            filled
                            id="payment_details_creditAccount_number"
                            name="payment_details_creditAccount_number"
                            v-model="
                              formDataObj.payment_details_creditAccount_number
                            "
                          ></v-text-field>
                        </v-col>
                        <v-col cols="12" sm="6" md="6">
                          <b>Credit Account Type </b
                          ><v-icon
                            small
                            title="
                    type: string
                maxLength: 4
                enum:
                  - CACC
                  - SLRY
                  - SVGS
                  - TRAN
                example: CACC
                description: Tipos de contas usadas para pagamento via Pix.
                  Modalidades tradicionais previstas pela Resolução 4.753, não contemplando contas vinculadas, conta de domiciliados no exterior, contas em moedas estrangeiras e conta correspondente moeda eletrônica.
                  Segue descrição de cada valor do ENUM para o escopo do Pix.
                  CACC - Current - Conta Corrente.
                  SLRY - Salary - Conta-Salário.
                  SVGS - Savings - Conta de Poupança.
                  TRAN - TransactingAccount - Conta de Pagamento pré-paga.
                  [Restrição] O campo data.payment.creditorAccount.accountType quando o arranjo alvo for TED só suportará os tipos CACC (Conta corrente), SVGS (Poupança) e TRAN (Conta de Pagamento pré-paga)."
                          >
                            mdi-information
                          </v-icon>
                          <v-text-field
                            placeholder="CACC"
                            outlined
                            filled
                            name="payment_details_creditAccount_accountType"
                            id="payment_details_creditAccount_accountType"
                            v-model="
                              formDataObj.payment_details_creditAccount_accountType
                            "
                          ></v-text-field>
                        </v-col>
                      </v-row>
                      <v-row class="pl-6">
                        <v-col cols="12" sm="6" md="6">
                          <b>Credit Account ISPB </b
                          ><v-icon
                            small
                            title="
                    type: string
                minLength: 8
                maxLength: 8
                pattern: '^[0-9]{8}$'
                example: '12345678'
                description: |
                  Deve ser preenchido com o ISPB (Identificador do Sistema de Pagamentos Brasileiros) do participante do SPI (Sistema de pagamentos instantâneos) somente com números."
                          >
                            mdi-information
                          </v-icon>
                          <v-text-field
                            placeholder="12345678"
                            outlined
                            filled
                            name="payment_details_creditAccount_ispb"
                            id="payment_details_creditAccount_ispb"
                            v-model="formDataObj.payment_details_creditAccount_ispb"
                          ></v-text-field>
                        </v-col>
                        <v-col cols="12" sm="6" md="6">
                          <b>Credit Account Issuer </b
                          ><v-icon
                            small
                            title="
                    type: string
                maxLength: 4
                pattern: '^\d{4}$'
                example: '1774'
                description: |
                  Código da Agência emissora da conta sem dígito.  
                  (Agência é a dependência destinada ao atendimento aos clientes, ao público em geral e aos associados de cooperativas de crédito,  
                  no exercício de atividades da instituição, não podendo ser móvel ou transitória).  
                  [Restrição] Preenchimento obrigatório para os seguintes tipos de conta: CACC (CONTA_DEPOSITO_A_VISTA), SVGS (CONTA_POUPANCA) e SLRY (CONTA_SALARIO)."
                          >
                            mdi-information
                          </v-icon>
                          <v-text-field
                            placeholder="1774"
                            outlined
                            filled
                            name="payment_details_creditAccount_issuer"
                            id="payment_details_creditAccount_issuer"
                            v-model="
                              formDataObj.payment_details_creditAccount_issuer
                            "
                          ></v-text-field>
                        </v-col>
                      </v-row>
                    </v-card-text>
                  </v-card>
                </v-col>
                <v-col cols="12" sm="3">
                    <v-card class="mx-auto" max-width="300" tile>
                      <v-subheader>Available Consent IDs</v-subheader>
                      <v-list dense max-height="20vh" style="overflow: auto">
                        <v-list-item-group color="primary">
                          <v-list-item
                            v-for="(consent, i) in consents"
                            :key="i"
                            @click="
                              () => {
                                setConsent(consent);
                              }
                            "
                          >
                            <v-list-item-content>
                              <v-list-item-title
                                v-text="consent.data.consentId"
                              ></v-list-item-title>
                            </v-list-item-content>
                          </v-list-item>
                        </v-list-item-group>
                      </v-list>
                    </v-card>
                </v-col>
              </v-row>
              <v-container class="pt-16">
                <v-col align="center">
                  <v-btn depressed color="primary" x-large type="submit"
                    ><v-icon left> mdi-file </v-icon>
                    Create Payment
                  </v-btn>
                </v-col>
              </v-container>
            </v-container>
          </v-sheet>
        </v-form>
      </v-col>
      <v-col cols="12" sm="2">
        <BackButton path="payment-menu" />
      </v-col>
    </v-row>
    <v-overlay :value="loading">
      <v-progress-circular indeterminate size="100"></v-progress-circular>
    </v-overlay>
    <v-snackbar v-model="snackbar" :multi-line="multiLine">
      {{ messageText }}

      <template v-slot:action="{ attrs }">
        <v-btn color="white" text v-bind="attrs" @click="snackbar = false">
          Close
        </v-btn>
      </template>
    </v-snackbar>
  </v-main>
</template>

<script>
// @ is an alias to /src
import SheetAppBar from "@/components/GeneralAppComponents/SheetAppBar.vue";
import BackButton from "@/components/GeneralAppComponents/BackButton.vue";
import axios from "../util/axios.js";
import { mapGetters } from "vuex";

export default {
  name: "PaymentDetail",
  components: {
    SheetAppBar,
    BackButton,
  },
  data: () => ({
    multiLine: true,
    snackbar: false,
    loading: false,
    bankName: "",
    messageText: "",
    consents: [],
    selectedConsent: null,
    formDataObj: {
      payment_amount: "",
      payment_type: "",
      payment_details_proxy: "",
      payment_details_localInstrument: "",
      payment_details_creditAccount_number: "",
      payment_details_creditAccount_accountType: "",
      payment_details_creditAccount_ispb: "",
      payment_details_creditAccount_issuer: "",
    },
  }),

  computed: {
    ...mapGetters(["consent"])
  },

  created() {
    this.bankName = this.$route.params.data;
    this.consents.push(this.consent);
  },

  methods: {
    async createPayment() {
      this.loading = true;
      axios.defaults.withCredentials = true;
      try {
        await axios.post (
          "/payments/make-payment",
          { 
            ...this.formDataObj,
            consentPayload: this.selectedConsent,
            bank: this.bankName,
          },
          {
            headers: {
              "Content-Type": "application/json",
            },
          }
        );
        this.loading = false;
        this.$router.push("payment-response");
      } catch(error) {
        this.loading = false;
        this.snackbar = true;
        this.messageText = error;
      }
    },

    setConsent(consent) {
      this.selectedConsent = consent;

      this.formDataObj = {
        payment_amount: this.consent.data.payment.amount,
        payment_type: this.consent.data.payment.type,
        payment_details_proxy: this.consent.data.payment.details.proxy,
        payment_details_localInstrument: this.consent.data.payment.details.localInstrument,
        payment_details_creditAccount_number: this.consent.data.payment.details.creditorAccount.number,
        payment_details_creditAccount_accountType: this.consent.data.payment.details.creditorAccount.accountType,
        payment_details_creditAccount_ispb: this.consent.data.payment.details.creditorAccount.ispb,
        payment_details_creditAccount_issuer: this.consent.data.payment.details.creditorAccount.issuer,
      };
    },
  },
};
</script>
