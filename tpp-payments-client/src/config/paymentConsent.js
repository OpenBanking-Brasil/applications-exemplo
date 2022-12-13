export const formItems = [
    {
        title: "Debtor",
        items: [
            {
                label: "Account Type ",
                placeholder: "94088392",
                value: "debtorAccount_number",
                name: "debtorAccount_number",
                id: "debtorAccount_number",
                cols: "6",
                iconTitle: `type: string
minLength: 3
maxLength: 20
pattern: '^\d{3,20}$'
example: '1234567890'
description: Deve ser preenchido com o número da conta transacional do usuário pagador, com dígito verificador (se este existir), se houver valor alfanumérico, este deve ser convertido para 0.`,
            },
            {
                label: "Account Number ",
                placeholder: "CACC",
                value: "debtorAccount_accountType",
                name: "debtorAccount_accountType",
                id: "debtorAccount_accountType",
                cols: "6",
                iconTitle:
                    `type: string
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
[Restrição] O campo data.payment.creditorAccount.accountType quando o arranjo alvo for TED só suportará os tipos CACC (Conta corrente), SVGS (Poupança) e TRAN (Conta de Pagamento pré-paga).
`,
            },
            {
                label: "ISPB",
                placeholder: "12345678",
                value: "debtorAccount_ispb",
                name: "debtorAccount_ispb",
                id: "debtorAccount_ispb",
                cols: "6",
                iconTitle:
                    `type: string
minLength: 8
maxLength: 8
pattern: '^[0-9]{8}$'
example: '12345678'
description: Deve ser preenchido com o ISPB (Identificador do Sistema de Pagamentos Brasileiros) do participante do SPI (Sistema de pagamentos instantâneos) somente com números.`,
            },
            {
                label: "Issuer",
                placeholder: "6272",
                value: "debtorAccount_issuer",
                name: "debtorAccount_issuer",
                id: "debtorAccount_issuer",
                cols: "6",
                iconTitle:
                    `type: string
maxLength: 4
pattern: '^\d{4}$'
example: '1774'
description: Código da Agência emissora da conta sem dígito. (Agência é a dependência destinada ao atendimento aos clientes, ao público em geral e aos associados de cooperativas de crédito, no exercício de atividades da instituição, não podendo ser móvel ou transitória).  
[Restrição] Preenchimento obrigatório para os seguintes tipos de conta: CACC (CONTA_DEPOSITO_A_VISTA), SVGS (CONTA_POUPANCA) e SLRY (CONTA_SALARIO).`,
            },
        ],
    },
    {
        title: "Logged User",
        items: [
            {
                label: "Document Identification",
                placeholder: "76109277673",
                value: "loggedUser_document_identification",
                name: "loggedUser_document_identification",
                id: "loggedUser_document_identification",
                cols: "6",
                iconTitle:
                    `type: string
maxLength: 14
description: Número do documento de identificação oficial do titular pessoa jurídica.
example: '11111111111111'
pattern: '^\d{14}$'`,
            },
            {
                label: "Document Rel",
                placeholder: "CPF",
                value: "loggedUser_document_rel",
                name: "loggedUser_document_rel",
                id: "loggedUser_document_rel",
                cols: "6",
                iconTitle:
                    `type: string
maxLength: 4
description: Tipo do documento de identificação oficial do titular pessoa jurídica.
example: CNPJ
pattern: '^[A-Z]{4}$'`,
            },
        ],
    },
    {
        title: "Creditor",
        items: [
            {
                label: "Name",
                placeholder: "Marco Antonio de Brito",
                value: "creditor_name",
                name: "creditor_name",
                id: "creditor_name",
                cols: "6",
                iconTitle:
                    `type: string
maxLength: 140
pattern: '[\w\W\s]*'
example: Marco Antonio de Brito
description: Em caso de pessoa natural deve ser informado o nome completo do titular da conta do recebedor.  
Em caso de pessoa jurídica deve ser informada a razão social ou o nome fantasia da conta do recebedor.`,
            },
            {
                label: "CPF/CNPJ",
                placeholder: "48847377765",
                value: "creditor_cpfCnpj",
                name: "creditor_cpfCnpj",
                id: "creditor_cpfCnpj",
                cols: "6",
                iconTitle:
                    `type: string
minLength: 11
maxLength: 14
pattern: '^\d{11}$|^\d{14}$'
example: '58764789000137'
description: Identificação da pessoa envolvida na transação.  
Preencher com o CPF ou CNPJ, de acordo com o valor escolhido no campo type.  
O CPF será utilizado com 11 números e deverá ser informado sem pontos ou traços.  
O CNPJ será utilizado com 14 números e deverá ser informado sem pontos ou traços.`,
            },
            {
                label: "Person Type",
                placeholder: "123456",
                value: "creditor_personType",
                name: "creditor_personType",
                id: "creditor_personType",
                cols: "12",
                iconTitle:
                    `type: string
maxLength: 15
enum:
- PESSOA_NATURAL
- PESSOA_JURIDICA
description: Titular, pessoa natural ou juridica a quem se referem os dados de recebedor (creditor).`,
            },
        ],
    },
    {
        title: "Payment",
        items: [
            {
                label: "Amount",
                placeholder: "1335.00",
                value: "payment_amount",
                name: "payment_amount",
                id: "payment_amount",
                cols: "12",
                iconTitle:
                    `type: string
minLength: 4
maxLength: 19
pattern: '^((\d{1,16}\.\d{2}))$'
example: '100000.12'
description: Valor da transação com 2 casas decimais.`,
            },
            {
                label: "Payment Scheduled",
                placeholder: "",
                value: "selected",
                name: "",
                id: "",
                cols: "6",
                items: ["Yes", "No"],
                type: "select",
                iconTitle: "Payment Scheduled",
            },
            {
                label: "Payment Date", // v-if="formDataObj.selected == 'Yes'"
                placeholder: "",
                value: "date",
                name: "date",
                id: "",
                cols: "6",
                type: "dataPicker",
                iconTitle: "",
            },
            {
                label: "Type",
                placeholder: "PIX",
                value: "payment_type",
                name: "payment_type",
                id: "payment_type",
                cols: "6",
                iconTitle: `EnumPixPaymentType:
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
description: Este campo define o tipo de pagamento que será iniciado após a autorização do consentimento.`,
            },
            {
                label: "Proxy",
                placeholder: "12345678901",
                value: "payment_details_proxy",
                name: "payment_details_proxy",
                id: "payment_details_proxy",
                cols: "6",
                iconTitle:
                    `type: string
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
Se localInstrument for igual INIC, DICT, QRDN ou QRES, o campo proxy deve ser sempre preenchido com a chave Pix.`,
            },
            {
                label: "Local Instrument",
                placeholder: "DICT",
                value: "payment_details_localInstrument",
                name: "payment_details_localInstrument",
                id: "payment_details_localInstrument",
                cols: "12",
                iconTitle:
                    `type: string
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
- INIC - Indica que o recebedor (creditor) contratou o Iniciador de Pagamentos especificamente para realizar iniciações de pagamento em que o beneficiário é previamente conhecido.`,
            },
            {
                label: "Credit Account Number",
                placeholder: "1234567890",
                value: "payment_details_creditAccount_number",
                name: "payment_details_creditAccount_number",
                id: "payment_details_creditAccount_number",
                cols: "6",
                iconTitle:
                    `type: string
minLength: 3
maxLength: 20
pattern: '^\d{3,20}$'
example: '1234567890'
description: 
Deve ser preenchido com o número da conta do usuário recebedor, com dígito verificador (se este existir), se houver valor alfanumérico, este deve ser convertido para 0.`,
            },
            {
                label: "Credit Account Type",
                placeholder: "CACC",
                value: "payment_details_creditAccount_accountType",
                name: "payment_details_creditAccount_accountType",
                id: "payment_details_creditAccount_accountType",
                cols: "6",
                iconTitle:
                    `type: string
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
[Restrição] O campo data.payment.creditorAccount.accountType quando o arranjo alvo for TED só suportará os tipos CACC (Conta corrente), SVGS (Poupança) e TRAN (Conta de Pagamento pré-paga).`,
            },
            {
                label: "Credit Account ISPB",
                placeholder: "12345678",
                value: "payment_details_creditAccount_ispb",
                name: "payment_details_creditAccount_ispb",
                id: "payment_details_creditAccount_ispb",
                cols: "6",
                iconTitle:
                    `type: string
minLength: 8
maxLength: 8
pattern: '^[0-9]{8}$'
example: '12345678'
description: |
Deve ser preenchido com o ISPB (Identificador do Sistema de Pagamentos Brasileiros) do participante do SPI (Sistema de pagamentos instantâneos) somente com números.`,
            },
            {
                label: "Credit Account Issuer",
                placeholder: "1774",
                value: "payment_details_creditAccount_issuer",
                name: "payment_details_creditAccount_issuer",
                id: "payment_details_creditAccount_issuer",
                cols: "6",
                iconTitle:
                    `type: string
maxLength: 4
pattern: '^\d{4}$'
example: '1774'
description: |
Código da Agência emissora da conta sem dígito.  
(Agência é a dependência destinada ao atendimento aos clientes, ao público em geral e aos associados de cooperativas de crédito,  
no exercício de atividades da instituição, não podendo ser móvel ou transitória).  
[Restrição] Preenchimento obrigatório para os seguintes tipos de conta: CACC (CONTA_DEPOSITO_A_VISTA), SVGS (CONTA_POUPANCA) e SLRY (CONTA_SALARIO).`,
            },
        ],
    }
];