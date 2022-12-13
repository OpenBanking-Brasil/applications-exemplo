export const formItems = [
    {
      label: "Amount",
      placeholder: "1335.00",
      value: "payment_amount",
      name: "payment_amount",
      id: "payment_amount",
      iconTitle: `type: string
minLength: 4
maxLength: 19
pattern: '^((\d{1,16}\.\d{2}))$'
example: '100000.12'
description: Valor da transação com 2 casas decimais.`,
    },
    {
      label: "Type",
      placeholder: "PIX",
      value: "payment_type",
      name: "payment_type",
      id: "payment_type",
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
      iconTitle: `type: string
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
      iconTitle: `type: string
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
      iconTitle: `type: string
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
      iconTitle: `type: string
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
      iconTitle: `type: string
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
      iconTitle: `type: string
maxLength: 4
pattern: '^\d{4}$'
example: '1774'
description: |
Código da Agência emissora da conta sem dígito.  
(Agência é a dependência destinada ao atendimento aos clientes, ao público em geral e aos associados de cooperativas de crédito,  
no exercício de atividades da instituição, não podendo ser móvel ou transitória).  
[Restrição] Preenchimento obrigatório para os seguintes tipos de conta: CACC (CONTA_DEPOSITO_A_VISTA), SVGS (CONTA_POUPANCA) e SLRY (CONTA_SALARIO).`,
    },
  ];