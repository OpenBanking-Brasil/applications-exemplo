export const documentIdentificationTitle = 
`type: string
maxLength: 8
enum:
enum: 
- USER
- ASPSP
- TPP
@@ -82,23 +45,15 @@
Define qual das partes envolvidas na transação está realizando a revogação. Valores possíveis:
- USER (Revogado pelo usuário)
- ASPSP (Provedor de serviços de pagamento para serviços de conta - Detentora de conta)
- TPP (Instituições Provedoras - iniciadora de pagamentos)`;

export const documentRelTitle = 
`type: string
maxLength: 22
enum:
@@ -110,66 +65,47 @@
Valores possíveis:
FRAUD - Indica suspeita de fraude
ACCOUNT_CLOSURE - Indica que a conta do usuário foi encerrada
OTHER - Indica que motivo do cancelamento está fora dos motivos pré-estabelecidos.`;

export const revokedByTitle = 
`type: string
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
- TPP (Instituições Provedoras - iniciadora de pagamentos)`;

export const codeTitle = 
`type: string
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
OTHER - Indica que motivo do cancelamento está fora dos motivos pré-estabelecidos.`;

export const additionalInformationTitle = 
`type: string
maxLength: 140
pattern: '[\w\W\s]*'
example: Não quero mais o serviço
description: 
Contém informações adicionais definidas pelo requisitante da revogação.
[Restrição] Deverá ser obrigatoriamente preenchido quando a revogação for feita pela iniciadora ou pela detentora unilateralmente, ou seja, quando o campo revokedBy for igual a TPP ou ASPSP e o motivo de revogação for OTHER.`;