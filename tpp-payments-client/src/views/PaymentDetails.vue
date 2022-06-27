<template>
  <v-main class="payment-details">
    <v-row>
      <v-col cols="12" sm="2"> </v-col>
      <v-col cols="12" sm="8">
        <SheetAppBar header="Payment Details" />
        <v-form @submit.prevent="createPayment">
          <v-sheet min-height="70vh" rounded="lg">
            <v-container class="pa-md-12">
              <v-card elevation="2" outlined color="">
                <v-card-title class="white--text cyan darken-4"
                  >Debtor</v-card-title
                >
                <v-card-text>
                  <v-row class="pa-6">
                    <v-col cols="12" sm="4" md="4">
                      <b>Account Number </b>
                      <v-icon
                        small
                        title="
                type: string
                minLength: 3
                maxLength: 20
                pattern: '^\d{3,20}$'
                example: '1234567890'
                description: Deve ser preenchido com o número da conta transacional do usuário pagador, com dígito verificador (se este existir), se houver valor alfanumérico, este deve ser convertido para 0."
                      >
                        mdi-information
                      </v-icon>
                      <v-text-field
                        class="text-green"
                        placeholder="94088392"
                        outlined
                        filled
                        v-model="formDataObj.debtorAccount_number"
                        name="debtorAccount_number"
                        id="debtorAccount_number"
                      ></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="4" md="4">
                      <b>Account Type </b>
                      <v-icon
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
                        name="debtorAccount_accountType"
                        id="debtorAccount_accountType"
                        v-model="formDataObj.debtorAccount_accountType"
                      ></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="4" md="4">
                      <b>ISPB </b
                      ><v-icon
                        small
                        title="
                type: string
            minLength: 8
            maxLength: 8
            pattern: '^[0-9]{8}$'
            example: '12345678'
            description: Deve ser preenchido com o ISPB (Identificador do Sistema de Pagamentos Brasileiros) do participante do SPI (Sistema de pagamentos instantâneos) somente com números."
                      >
                        mdi-information
                      </v-icon>
                      <v-text-field
                        placeholder="12345678"
                        outlined
                        filled
                        v-model="formDataObj.debtorAccount_ispb"
                        name="debtorAccount_ispb"
                        id="debtorAccount_ispb"
                      ></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="4" md="4">
                      <b>Issuer </b
                      ><v-icon
                        small
                        title="
               type: string
            maxLength: 4
            pattern: '^\d{4}$'
            example: '1774'
            description: Código da Agência emissora da conta sem dígito. (Agência é a dependência destinada ao atendimento aos clientes, ao público em geral e aos associados de cooperativas de crédito, no exercício de atividades da instituição, não podendo ser móvel ou transitória).  
            [Restrição] Preenchimento obrigatório para os seguintes tipos de conta: CACC (CONTA_DEPOSITO_A_VISTA), SVGS (CONTA_POUPANCA) e SLRY (CONTA_SALARIO)."
                      >
                        mdi-information
                      </v-icon>
                      <v-text-field
                        placeholder="6272"
                        outlined
                        filled
                        name="debtorAccount_issuer"
                        id="debtorAccount_issuer"
                        v-model="formDataObj.debtorAccount_issuer"
                      ></v-text-field>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-card>
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
                        name="loggedUser_document_identification"
                        id="loggedUser_document_identification"
                        v-model="formDataObj.loggedUser_document_identification"
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
                        name="loggedUser_document_rel"
                        id="loggedUser_document_rel"
                        v-model="formDataObj.loggedUser_document_rel"
                      ></v-text-field>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-card>
              <div class="pa-2"></div>
              <v-card elevation="2" outlined color="">
                <v-card-title class="white--text cyan darken-4"
                  >Creditor</v-card-title
                >
                <v-card-text>
                  <v-row class="pa-6">
                    <v-col cols="12" sm="4" md="4">
                      <b>Name </b
                      ><v-icon
                        small
                        title="
                type: string
            maxLength: 140
            pattern: '[\w\W\s]*'
            example: Marco Antonio de Brito
            description: Em caso de pessoa natural deve ser informado o nome completo do titular da conta do recebedor.  
            Em caso de pessoa jurídica deve ser informada a razão social ou o nome fantasia da conta do recebedor."
                      >
                        mdi-information
                      </v-icon>
                      <v-text-field
                        class="text-green"
                        placeholder="Marco Antonio de Brito"
                        outlined
                        filled
                        name="creditor_name"
                        id="creditor_name"
                        v-model="formDataObj.creditor_name"
                      ></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="4" md="4">
                      <b>CPF/CNPJ </b
                      ><v-icon
                        small
                        title="
                type: string
            minLength: 11
            maxLength: 14
            pattern: '^\d{11}$|^\d{14}$'
            example: '58764789000137'
            description: Identificação da pessoa envolvida na transação.  
            Preencher com o CPF ou CNPJ, de acordo com o valor escolhido no campo type.  
            O CPF será utilizado com 11 números e deverá ser informado sem pontos ou traços.  
            O CNPJ será utilizado com 14 números e deverá ser informado sem pontos ou traços."
                      >
                        mdi-information
                      </v-icon>
                      <v-text-field
                        placeholder="48847377765"
                        outlined
                        filled
                        name="creditor_cpfCnpj"
                        id="creditor_cpfCnpj"
                        v-model="formDataObj.creditor_cpfCnpj"
                      ></v-text-field>
                    </v-col>
                    <v-col cols="12" sm="4" md="4">
                      <b>Person Type </b
                      ><v-icon
                        small
                        title="
                type: string
            maxLength: 15
            enum:
            - PESSOA_NATURAL
            - PESSOA_JURIDICA
            description: Titular, pessoa natural ou juridica a quem se referem os dados de recebedor (creditor)."
                      >
                        mdi-information
                      </v-icon>
                      <v-text-field
                        placeholder="123456"
                        outlined
                        filled
                        name="creditor_personType"
                        id="creditor_personType"
                        v-model="formDataObj.creditor_personType"
                      ></v-text-field>
                    </v-col>
                  </v-row>
                </v-card-text>
              </v-card>
              <div class="pa-2"></div>
              <v-card elevation="2" outlined color="">
                <v-card-title class="white--text cyan darken-4"
                  >Payment</v-card-title
                >
                <v-card-text class="pa-6">
                  <v-row class="pl-6">
                    <v-col cols="12" sm="4" md="4">
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
                    <v-col cols="12" sm="4" md="4">
                      <b>Payment Scheduled </b>
                      <v-select
                        v-model="formDataObj.selected"
                        :items="['Yes', 'No']"
                        label=""
                        outlined
                        filled
                      ></v-select>
                    </v-col>
                    <v-col
                      v-if="formDataObj.selected == 'Yes'"
                      cols="12"
                      sm="2"
                      md="2"
                    >
                      <b>Payment Date</b>
                      <v-dialog
                        ref="dialog"
                        v-model="modal"
                        :return-value.sync="formDataObj.date"
                        persistent
                        width="290px"
                      >
                        <template v-slot:activator="{ on, attrs }">
                          <v-text-field
                            outlined
                            filled
                            v-model="formDataObj.date"
                            name="date"
                            prepend-icon="mdi-calendar"
                            readonly
                            v-bind="attrs"
                            v-on="on"
                          ></v-text-field>
                        </template>
                        <v-date-picker v-model="formDataObj.date" scrollable>
                          <v-spacer></v-spacer>
                          <v-btn text color="primary" @click="modal = false">
                            Cancel
                          </v-btn>
                          <v-btn
                            text
                            color="primary"
                            @click="$refs.dialog.save(formDataObj.date)"
                          >
                            OK
                          </v-btn>
                        </v-date-picker>
                      </v-dialog>
                    </v-col>
                  </v-row>
                  <v-row class="pl-6">
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
                    <v-col cols="12" sm="4" md="4">
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
                    <v-col cols="12" sm="4" md="4">
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
                    <v-col cols="12" sm="4" md="4">
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
                    <v-col cols="12" sm="4" md="4">
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
                    <v-col cols="12" sm="4" md="4">
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
                    <v-col cols="12" sm="4" md="4">
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
      {{ text }}

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

export default {
  name: "PaymentDetail",
  components: {
    SheetAppBar,
    BackButton,
  },
  data: () => ({
    multiLine: true,
    snackbar: false,
    text: "Payment schedule date must be in the future",
    loading: false,
    modal: false,
    bankName: "",
    today: new Date(Date.now() - new Date().getTimezoneOffset() * 60000)
      .toISOString()
      .substr(0, 10),
    formDataObj: {
      selected: "No",
      date: new Date(Date.now() - new Date().getTimezoneOffset() * 60000)
        .toISOString()
        .substr(0, 10),
      debtorAccount_number: "94088392",
      debtorAccount_accountType: "CACC",
      debtorAccount_ispb: "12345678",
      debtorAccount_issuer: "6272",
      loggedUser_document_identification: "76109277673",
      loggedUser_document_rel: "CPF",
      creditor_name: "Marco Antonio de Brito",
      creditor_cpfCnpj: "48847377765",
      creditor_personType: "PESSOA_NATURAL",
      payment_amount: "1335.00",
      payment_type: "PIX",
      payment_details_proxy: "12345678901",
      payment_details_localInstrument: "DICT",
      payment_details_creditAccount_number: "1234567890",
      payment_details_creditAccount_accountType: "CACC",
      payment_details_creditAccount_ispb: "12345678",
      payment_details_creditAccount_issuer: "1774",
    },
  }),

  created() {
    this.bankName = this.$route.params.data;
  },

  methods: {
    createPayment() {
      if (
        this.formDataObj.selected === "Yes" &&
        this.formDataObj.date.toString() === this.today.toString()
      ) {
        this.snackbar = true;
        return;
      }

      let formBody = [];
      for (let property in this.formDataObj) {
        const encodedKey = encodeURIComponent(property);
        const encodedValue = encodeURIComponent(this.formDataObj[property]);
        formBody.push(encodedKey + "=" + encodedValue);
      }
      formBody = formBody.join("&");
      axios.defaults.withCredentials = true;

      let bankConsent = window.open("", "_self");
      this.loading = true;
      axios
        .post("/payments/payment-consent", formBody, {
          headers: {
            "Content-Type": "application/x-www-form-urlencoded",
          },
        })
        .then((res) => {
          if (res.status === 200) {
            axios
              .post(
                "/payments/make-payment",
                { bank: this.bankName },
                {
                  headers: {
                    "Content-Type": "application/json",
                  },
                }
              )
              .then((response) => {
                bankConsent.location.href = response.data.authUrl;
              }).catch((error) => {
                this.loading = false;
                this.snackbar = true;
                this.text = error.response.data.errors[0].title;
              });
          }
        })
        .catch(function (response) {
          console.log(response);
          this.loading = false;
        });
    },
  },
};
</script>
