
-- First, drop *all* existing data. Yes all of it. But try not to step on flyway.

DO $$ DECLARE
    r RECORD;
BEGIN
    FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = current_schema() AND tablename NOT LIKE '%flyway%') LOOP
            EXECUTE 'TRUNCATE TABLE ' || quote_ident(r.tablename) || ' CASCADE;';
        END LOOP;
END $$;

-- Now insert the people and their accounts

DO $$DECLARE
    docId varchar := '96644087000';
    accountHolderName varchar := 'Janice Santana Matos';
    accountHolderEmail varchar := 'janice.matos@email.com';
    acctId uuid;
    contractId uuid;
    creditCardId uuid;
    consentId text;
    personalId uuid;
    nationalitiesId uuid;
    billId uuid;
    billId2 uuid;
BEGIN
    PERFORM addAccountHolder(docId::varchar, 'CPF'::varchar, accountHolderName, accountHolderEmail);

    acctId := addAccount(docId, 'AVAILABLE', 'BRL','CONTA_DEPOSITO_A_VISTA', 'INDIVIDUAL', 'Banco Bradesco S.A', '60746948000112', '237',
                         '8546', '85215959', '5', 16025.6975, 'BRL', 0.0000, 'BRL',
                         0.0000, 'BRL', 0.0000,
                         'BRL', 0.0000, 'BRL', 0.0000,'BRL',
                         '12345678', '1774', 'SLRY');

    creditCardId := addCreditCardAccounts(docId, 'Banco Bradesco S.A', '60746948000112', 'Cartão Pós Pago',
                                    'PLATINUM', 'NA', 'VISA', 'NA' , 'AVAILABLE');

    consentId := addConsent(docId, 'urn:bradesco:BDC568642159', addBusinesDocument(docId, 'CPF'),
                      NOW()::date, NOW()::date,NOW()::date,
                      NOW()::date,NOW()::date,'AUTHORISED','NA');

    PERFORM addConsentPermissions('CUSTOMERS_PERSONAL_IDENTIFICATIONS_READ', consentId);
    PERFORM addConsentPermissions('CUSTOMERS_PERSONAL_ADITTIONALINFO_READ', consentId);
    PERFORM addConsentPermissions('CREDIT_CARDS_ACCOUNTS_READ', consentId);
    PERFORM addConsentPermissions('CREDIT_CARDS_ACCOUNTS_BILLS_READ', consentId);
    PERFORM addConsentPermissions('CREDIT_CARDS_ACCOUNTS_BILLS_TRANSACTIONS_READ', consentId);
    PERFORM addConsentPermissions('CREDIT_CARDS_ACCOUNTS_LIMITS_READ', consentId);
    PERFORM addConsentPermissions('CREDIT_CARDS_ACCOUNTS_TRANSACTIONS_READ', consentId);
    PERFORM addConsentPermissions('INVOICE_FINANCINGS_READ', consentId);
    PERFORM addConsentPermissions('INVOICE_FINANCINGS_SCHEDULED_INSTALMENTS_READ', consentId);
    PERFORM addConsentPermissions('INVOICE_FINANCINGS_PAYMENTS_READ', consentId);
    PERFORM addConsentPermissions('INVOICE_FINANCINGS_WARRANTIES_READ', consentId);
    PERFORM addConsentPermissions('RESOURCES_READ', consentId);

    PERFORM addPersonalFinancialRelations(docId, '2020-05-21T08:30:00', null);
    PERFORM addPersonalFinancialRelationsProcurator(docId, 'NAO_SE_APLICA','NA', 'NA', 'NA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CONTA_DEPOSITO_A_VISTA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'OPERACAO_CREDITO');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CARTAO_CREDITO');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'PREVIDENCIA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'INVESTIMENTO');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'OPERACOES_CAMBIO');

    personalId := addPersonalIdentifications(docId, 'Banco Bradesco S.A', 'Janice Santana Matos',
                                     'NA', '1968-11-24', 'VIUVA', null, 'FEMININO', true, docId,
                                     '13531890704588959425', 'BRA', '2027-02-22', '2017-02-22');
    PERFORM addPersonalIdentificationsCompanyCnpj(personalId, '60746948000112');
    PERFORM addPersonalIdentificationsOtherDocuments(personalId, 'CNH', 'NA', '4588594', 'A', 'SSP/PA', '2021-06-22');
    nationalitiesId := addPersonalIdentificationsNationality(personalId, 'NA');
    PERFORM addPersonalIdentificationsNationalityDocument(nationalitiesId, 'NA', 'NA', NOW()::date, NOW()::date, 'NA', 'NA');
    PERFORM addPersonalFiliation(personalId, 'MAE', 'Luzia Matos', 'NA');
    PERFORM addPersonalFiliation(personalId, 'PAI', 'André Matos', 'NA');

    PERFORM addPersonalPostalAddresses(personalId, true, 'Vila Chico Buarque', 'Casa laranja', 'Jurunas',
                                       'Belém', '1501402', 'PA', '66030366', 'Brasil', 'BRA', '-1.45502', '-48.5024');
    PERFORM addPersonalPhones(personalId, true, 'MOVEL', 'NA', '55', '91', '995590211', 'NA');
    PERFORM addPersonalEmails(personalId, true, 'janice.matos@email.com');

    PERFORM addPersonalQualifications(docId, '60746948000112', 'RECEITA_FEDERAL', 'Técnico em laboratório de farmácia', 'MENSAL',
                                     5257.00, 'BRL', '2021-05-20', 35006.96, 'BRL', 2020);

    PERFORM addCreditCardsAccountPaymentMethod(creditCardId, '5320', false);

    PERFORM addCreditCardAccountsLimits(creditCardId, 'TOTAL', 'CONSOLIDADO', '5320', 'CREDITO_A_VISTA', 'NA', false,
                                        'BRL', 3000.0000, 'BRL', 343.0400, 'BRL', 2656.9600);

    billId := addCreditCardAccountsBills(creditCardId, '2021-07-15', 409.2600, 'BRL', 143.8912, 'BRL', true);
    PERFORM addCreditCardAccountsBillsFinanceCharge(billId, 'JUROS_REMUNERATORIOS_ATRASO_PAGAMENTO_FATURA', 'NA', 35.4500, 'BRL');
    PERFORM addCreditCardAccountsBillsFinanceCharge(billId, 'IOF', 'NA', 11.0900, 'BRL');
    PERFORM addCreditCardAccountsBillsFinanceCharge(billId, 'JUROS_MORA_ATRASO_PAGAMENTO_FATURA', 'NA', 9.6800, 'BRL');
    PERFORM addCreditCardAccountsBillsPayment(billId, 'OUTRO_VALOR_PAGO_FATURA', '2021-06-21', 'DEBITO_CONTA_CORRENTE', 1990.0000, 'BRL');
    PERFORM addCreditCardAccountsTransaction(creditCardId, billId, '5320', 'CREDITO_A_VISTA', 'ARMAZEM DOS MÓVEIS', 'CREDITO', 'OPERACOES_CREDITO_CONTRATADAS_CARTAO',
                                             'NA', 'A_VISTA', 'SMS', 'NA', 'CREDITO_ROTATIVO', 'NA', 'PARCELA_UNICA', 1, 2043.0400, 0.0000, 'BRL',
                                             '2021-06-01', '2021-06-01', 5912);
    PERFORM addCreditCardAccountsTransaction(creditCardId, billId, '5320', 'CREDITO_A_VISTA', 'BORRACHARIA DO', 'CREDITO', 'OPERACOES_CREDITO_CONTRATADAS_CARTAO',
                                             'NA', 'A_VISTA', 'SMS', 'NA', 'CREDITO_ROTATIVO', 'NA', 'PARCELA_UNICA', 1, 300.0000, 0.0000, 'BRL',
                                             '2021-05-20', '2021-05-20', 5912);

    billId2 := addCreditCardAccountsBills(creditCardId, '2021-06-15', 2343.0400, 'BRL', 250.8923, 'BRL', true);
    PERFORM addCreditCardAccountsBillsFinanceCharge(billId2, 'SEM_ENCARGO', 'NA', 0.0000, 'BRL');
    PERFORM addCreditCardAccountsBillsPayment(billId2, 'OUTRO_VALOR_PAGO_FATURA', '2021-06-15', 'DEBITO_CONTA_CORRENTE', 10.0000, 'BRL');

    contractId := addContract(docId, 'AVAILABLE', 'BRL', '60746948000112','FINANCING', '989898456', '2021-06-21', 'Direitos Creditórios Descontados',
                              'DIREITOS_CREDITORIOS_DESCONTADOS', 'ANTECIPACAO_FATURA_CARTAO_CREDITO', '2021-06-21',
                              '2021-06-21', '1990.0000', '2021-07-15', 'SEM_PERIODICIDADE_REGULAR', 'Adiantamento de fatura.',
                              '2021-06-21', 0.0990, 'SAC', 'NA', '607469480303196644087000989898456', 0, 1990.0000,
                              'SEM_PRAZO_TOTAL', 0, 'SEM_PRAZO_REMANESCENTE', 0, 4, 0);
    PERFORM addContractInterestRates(contractId, 'EFETIVA', 'COMPOSTO', 'AM', '30/365', 'PRE_FIXADO',
                                     'PRE_FIXADO', 'NA', 0.0690, 0.0000, 'NA');
    PERFORM addContractedFees(contractId, 'Anteciação de Fatura', 'ANTECIPA_FATURA_CREDITO', 'UNICA', 'MAXIMO', 165.0000, 0.0000);
    PERFORM addContractedFinanceCharges(contractId, 'JUROS_REMUNERATORIOS_POR_ATRASO', 'NA', 0.0210);
    PERFORM addContractedFinanceCharges(contractId, 'JUROS_MORA_ATRASO', 'NA', 0.0710);
    PERFORM addContractedFinanceCharges(contractId, 'IOF_CONTRATACAO', 'NA', 0.0190);
    PERFORM addContractedFinanceCharges(contractId, 'IOF_POR_ATRASO', 'NA', 0.0180);
    PERFORM addContractWarranties(contractId, 'BRL', 'GARANTIA_FIDEJUSSORIA', 'PESSOA_FISICA_EXTERIOR', 8600.4000);
    PERFORM addBalloonPayments(contractId, '2021-06-15', 'BRL', 0.0000);

END $$;

DO $$DECLARE
    docId varchar := '10117409073';
    accountHolderName varchar = 'Eliane Sophia Melo';
    accountHolderEmail varchar := 'eliane0512@email.com';
    acctId uuid;
    acctId2 uuid;
    creditCardId uuid;
    billId uuid;
    personalId uuid;
    nationalitiesId uuid;
    contractId uuid;
    releasesId uuid;
    releasesId2 uuid;
    releasesId3 uuid;
BEGIN

    -- An Account holder
    PERFORM addAccountHolder(docId::varchar, 'CPF'::varchar, accountHolderName, accountHolderEmail);

    -- Personal Financial Relations
    PERFORM addPersonalFinancialRelations(docId, '2005-11-15T08:30:00', null);
    PERFORM addPersonalFinancialRelationsProcurator(docId, 'NAO_SE_APLICA','NA', 'NA', 'NA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CONTA_DEPOSITO_A_VISTA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CONTA_POUPANCA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CARTAO_CREDITO');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'OPERACOES_CAMBIO');
    -- skipping personal financial-relations account pending answer to question

    -- personal identifications is full of one-many relationships and we need to know IDs between calls

    personalId := addPersonalIdentifications(docId, 'Banco Cooperativo Sicredi S.A. – Bansicredi', 'Eliane Sophia Melo',
                                            'NA', '1975-12-05', 'CASADO', null, 'FEMININO', true, docId,
                                            '22459031982954795537', 'BRA', '2028-03-23', '2018-03-23');
    PERFORM addPersonalIdentificationsCompanyCnpj(personalId, '01181521000155');
    PERFORM addPersonalIdentificationsOtherDocuments(personalId, 'RG', 'NA', '40701685', '5', 'SSP/ES', '2022-12-15');
    nationalitiesId := addPersonalIdentificationsNationality(personalId, 'NA');
    PERFORM addPersonalIdentificationsNationalityDocument(nationalitiesId, 'NA', 'NA', CAST (NOW() AS date), CAST (NOW() as date), 'NA', 'NA');
    PERFORM addPersonalFiliation(personalId, 'PAI', 'Luiz Miguel Melo', 'NA');

    -- Personal Qualifications
    PERFORM addPersonalQualifications(docId, '01181521000155', 'RECEITA_FEDERAL', 'Contador', 'MENSAL',
                                     7500.04, 'BRL', '2021-05-25', 98200.00, 'BRL', 2020);

    -- Credit Card Accounts
    creditCardId := addCreditCardAccountsWithId(docId, '608b1ae4-d458-11eb-b8bc-0242ac130003',  'Banco Cooperativo Sicredi S.A. – Bansicredi', '01181521000155', 'Nanquim Elo',
                                                'NANQUIM', 'NA', 'ELO', 'NA' , 'AVAILABLE');
    PERFORM addCreditCardsAccountPaymentMethod(creditCardId, '5489', false);
    PERFORM addCreditCardAccountsLimits(creditCardId, 'TOTAL', 'CONSOLIDADO', '5489', 'SAQUE_CREDITO_BRASIL', 'NA', true,
                                        'BRL', 18400.0000, 'BRL', 102.0000, 'BRL', 18298.0000);

    billId := addCreditCardAccountsBillsWithId(creditCardId, '75dd9322-d458-11eb-b8bc-0242ac130003', '2021-01-15', 2102.7700, 'BRL', 210.2800, 'BRL', true);
    PERFORM addCreditCardAccountsBillsFinanceCharge(billId, 'JUROS_REMUNERATORIOS_ATRASO_PAGAMENTO_FATURA', 'NA', 1.0200, 'BRL');
    PERFORM addCreditCardAccountsBillsPayment(billId, 'OUTRO_VALOR_PAGO_FATURA', '2021-04-21', 'BOLETO_BANCARIO', 2000.7700, 'BRL');
    PERFORM addCreditCardAccountsTransactionWithId(creditCardId, billId, '83cef05c-d458-11eb-b8bc-0242ac130003', '5489', 'SAQUE_CREDITO_BRASIL', 'SAQUE', 'DEBITO',
                                                   'OPERACOES_CREDITO_CONTRATADAS_CARTAO', 'NA', 'A_VISTA', 'SAQUE_CARTAO_BRASIL', 'NA', 'CREDITO_ROTATIVO',
                                                   'NA', 'PARCELA_1', 1, 2102.7700, 0.0000, 'BRL', '2021-01-14', '2021-01-15', 5992);

    -- Accounts
    acctId := addAccountWithId(docId, '5859f81e-d461-11eb-b8bc-0242ac130003','AVAILABLE', 'BRL','CONTA_DEPOSITO_A_VISTA', 'CONJUNTA_SIMPLES',
                               'Banco Cooperativo Sicredi S.A. – Bansicredi', '01181521000155', '748',
                               '0718', '58795644', '3', 9.0000, 'BRL', 5610.0000, 'BRL',
                               1.0000, 'BRL', 500.0000,
                               'BRL', 0.0000, 'BRL', 0.0000,'BRL',
                               '12345678', '1774', 'SLRY');

    acctId2 := addAccountWithId(docId, '6ffc471a-d461-11eb-b8bc-0242ac130003', 'AVAILABLE', 'BRL','CONTA_POUPANCA', 'INDIVIDUAL',
                               'Banco Cooperativo Sicredi S.A. – Bansicredi', '01181521000155', '748',
                               '0718', '54888745', '3', 16025.6975, 'BRL', 0.0000, 'BRL',
                               0.0000, 'BRL', 0.0000,
                               'BRL', 0.0000, 'BRL', 0.0000,'BRL',
                               '12345678', '1774', 'SLRY');

    -- Contracts
    -- This one is a Financing
    contractId := addContractWithId(docId, '3a74427a-9c10-3779-a5ac-3f65c3e815aa','AVAILABLE', 'BRL','01181521000155', 'FINANCING', '25587487101', '2021-08-01', 'Aquisição de equipamentos',
                                    'FINANCIAMENTOS', 'AQUISICAO_BENS_OUTROS_BENS', '2021-08-01', null, 12070.6000, '2023-08-01', 'OUTROS',
                                    'DIA', '2021-08-02', 0.0150,
                                    'PRICE', 'NA', '01181521040211011740907325668478542336599', 3,14402.3790, 'DIA',
                                    730, 'DIA', 727, 727, 1);

    -- add account transactions
    PERFORM addAccountTransaction(acctId, '72f985f8-d4ee-11eb-b8bc-0242ac130003','TRANSACAO_EFETIVADA','CREDITO',
                                  'TRANSFSCD224325','DEPOSITO',1500.0500,'BRL','2021-07-05',
                                  docId,'NATURAL','748','0718','58795644','3');
    PERFORM addAccountTransaction(acctId2, 'c3763b88-d4ea-11eb-b8bc-0242ac130003','TRANSACAO_EFETIVADA','DEBITO',
                                  'TRANSFSCD224325','CARTAO',2000.7700,'BRL','2021-01-18',
                                  docId,'NATURAL','748','0718','58795644','2');
    PERFORM addAccountTransaction(acctId2, 'c77565ce-d4ea-11eb-b8bc-0242ac130003','LANCAMENTO_FUTURO','DEBITO',
                                  'TRANSFSCD224325','BOLETO',778.6500,'BRL','2021-01-23',
                                  docId,'NATURAL','748','0718','58795644','2');

    PERFORM addContractInterestRates(contractId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');
    PERFORM addContractedFees(contractId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(contractId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(contractId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(contractId, '2020-01-10', 'BRL', 0.0000);

    releasesId := addReleases(contractId, '6bf425f8-d500-11eb-b8bc-0242ac130003', false, '6fcf6e8a-d500-11eb-b8bc-0242ac130003','2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(releasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(releasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    releasesId2 := addReleases(contractId, 'e7cce9de-d4fc-11eb-b8bc-0242ac130003', false, '6053ecec-d500-11eb-b8bc-0242ac130003', '2021-08-03', 'BRL', 20.0870);
    PERFORM addOverParcelFee(releasesId2, 'NA', 'NA', 0.0000);
    PERFORM addOverParcelCharge(releasesId2, 'SEM_ENCARGO', 'NA', 0.0000);

    releasesId3 := addReleases(contractId, 'b60f7c86-d4fc-11eb-b8bc-0242ac130003', false, 'e7cce722-d4fc-11eb-b8bc-0242ac130003', '2021-08-02', 'BRL', 20.0870);
    PERFORM addOverParcelFee(releasesId3, 'NA', 'NA', 0.0000);
    PERFORM addOverParcelCharge(releasesId3, 'SEM_ENCARGO', 'NA', 0.0000);

    PERFORM addConsent(docId, 'urn:raidiambank:5a2a4bf9-71b7-4104-ad4b-74b0596bc546', addBusinesDocument('111111111', 'CPF'),
                      NOW()::date,NOW()::date,NOW()::date,
                      NOW()::date,NOW()::date,'AUTHORISED','608b1ae4-d458-11eb-b8bc-0242ac130003');
    PERFORM addConsent(docId, 'urn:raidiambank:b6e3c1f7-8fd5-47bd-b0cf-7f997c64b47e', addBusinesDocument('2222222222', 'CPF'),
                      NOW()::date, NOW()::date,NOW()::date,
                      NOW()::date,NOW()::date,'AUTHORISED','608b1ae4-d458-11eb-b8bc-0242ac130003');
END $$;


DO $$DECLARE
    docId varchar := '76109277673';
    accountHolderName varchar := 'Ralph Bragg';
    accountHolderEmail varchar := 'ralph.bragg@gmail.com';
    businessIdentification uuid;
    account1Id uuid;
    account2Id uuid;
    personalId uuid;
    nationalitiesId uuid;
    businessId uuid;
    creditCardId uuid;
    billId uuid;

    loanId uuid;
    financingId uuid;
    invoiceFinancingId uuid;
    overdraftId uuid;

    loanReleasesId uuid;
    financingReleasesId uuid;
    invoiceFinancingReleasesId uuid;
    overdraftReleasesId uuid;
BEGIN
    PERFORM addAccountHolder(docId::varchar, 'CPF'::varchar, accountHolderName, accountHolderEmail);
    businessIdentification := addBusinessIdentifications(docId, 'Organização A', 'Luiza e Benjamin Assessoria Jurídica Ltda',
                                                         'Mundo da Eletronica', '2021-05-21T08:30:00Z', '50685362006773');
    PERFORM addBusinessIdentificationsCompanyCnpj(businessIdentification, '50685362000135');
    PERFORM addBusinessIdentificationsCompanyCnpj(businessIdentification, '50685362006555');
    PERFORM addBusinessIdentificationsOtherDocument(businessIdentification, 'EIN', '128328453', 'CAN', '2021-05-21');
    PERFORM addBusinessIdentificationsParties(businessIdentification, 'PESSOA_NATURAL', 'SOCIO', 'Juan Kaique Cláudio Fernandes', 'Karina',
                                              'Luiza e Benjamin Assessoria Jurídica Ltda', 'Mundo da Eletronica', '2021-05-21T08:30:00Z', '0.51',
                                              'CPF', '73677831148', 'CNH', 'CAN', '2021-05-21', '2021-05-21');
    PERFORM addBusinessIdentificationsPostalAddress(businessIdentification, true, 'Av Naburo Ykesaki, 1270', 'Fundos', 'Centro', 'Marília', '3550308', 'countrySubDivision',
                                                    '17500001', 'Brasil', 'BRA', '-89.8365180', '-178.836519');
    PERFORM addBusinessIdentificationsPhone(businessIdentification, true, 'FIXO', 'Informações adicionais.', '55', '19', '29875132', '932');
    PERFORM addBusinessIdentificationsEmail(businessIdentification, true, 'karinafernandes-81@br.inter.net');

    account1Id := addAccountWithId(docId, '291e5a29-49ed-401f-a583-193caa7aceee', 'AVAILABLE', 'BRL', 'CONTA_DEPOSITO_A_VISTA', 'INDIVIDUAL', 'Sib Bank', '40156018000100', '123',
                                   '6272', '94088392', '4', 100000.04, 'BRL', 12345.0001, 'BRL', 15000.00, 'BRL', 99.9999, 'BRL', 10000.9999, 'BRL',
                                   99.9999, 'BRL', '12345678', '1774', 'CACC');

    PERFORM addAccountTransaction(account1Id, 'e67ed6ac-6841-44a6-b3d7-1f19d34dc204', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                  'CARTAO', 771.52, 'BRL', '2021-05-20', '87517400444', 'NATURAL',
                                  '123', '6272', '94088392', '4');

    account2Id := addAccountWithId(docId, '291e5a29-49ed-401f-a583-193caa7acddd', 'AVAILABLE', 'BRL', 'CONTA_POUPANCA', 'INDIVIDUAL', 'Sib Bank', '40156018000100', '123',
                                   '6272', '11188222', '4', 41233.07, 'BRL', 999.99, 'BRL', 15000.00, 'BRL', 99.9999, 'BRL', 12345.4000,
                                   'BRL', 99.9999, 'BRL', '12345678', '1774', 'SVGS');

    PERFORM addAccountTransaction(account2Id, 'bccfee35-979e-4901-b12a-415fefda9a74', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                  'CARTAO', 312.52, 'BRL', '2021-05-21', '87517400444', 'NATURAL',
                                  '123', '6272', '94088392', '4');

    PERFORM addAccountTransaction(account2Id, 'b880d1c9-b1cb-4df5-9d7e-3af9f517ba67', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                  'CARTAO', 3311.35, 'BRL', '2021-05-21', '87517400444', 'NATURAL',
                                  '123', '6272', '94088392', '4');

    PERFORM addPersonalQualifications(docId, '50685362000135', 'OUTRO', '01', 'OUTROS', 100000.04, 'BRL', '2021-05-21', 100000.04, 'BRL', 2010);

    -- the swagger included this with uuid 'c57-466-1254590932-3fa314', which is not a valid uuid...
    personalId := addPersonalIdentifications(docId, 'Alana e Bianca Assessoria Jurídica Ltda', 'Tatiana Galvão', '',
                                             '1989-03-23', 'SOLTEIRO', '', 'MASCULINO', false, '53580793004', '34229643119827236458',
                                             'CAN', '2022-05-24', '2018-05-24');
    PERFORM addPersonalIdentificationsCompanyCnpj(personalId, '25349207000105');
    PERFORM addPersonalIdentificationsOtherDocuments(personalId, 'CNH', 'NA', '58438287', 'P', 'SSP/RS', '2025-05-21');
    nationalitiesId := addPersonalIdentificationsNationality(personalId, 'true');
    PERFORM addPersonalIdentificationsNationalityDocument(nationalitiesId, 'SOCIAL SEC', '423929299', '2022-08-21', '2020-05-30', 'Brasil', '');
    PERFORM addPersonalFiliation(personalId, 'MAE', 'Andreia Galvao', 'NA');

    PERFORM addPersonalPostalAddresses(personalId, true, 'Rua Laguna 129', 'Casa Amarela', 'Cavalhada',
                                       'Porto Alegre', '4314902', 'RS', '90820060', 'Brasil', 'BRA', '-30.0953952', '-51.2279909');
    PERFORM addPersonalPhones(personalId, true, 'FIXO', 'Informações adicionais.', '55', '51', '325421328', '258');
    PERFORM addPersonalEmails(personalId, true, 'tatiana.galvao@email.com');

    businessId := addBusinessQualifications(docId, 'DIARIA', 'Informações adicionais', 100000.04, 'BRL', 2010, 100000.04, 'BRL', '2021-05-21');
    PERFORM addBusinessQualificationsEconomicActivities(businessId, 8599604, true);

    creditCardId := addCreditCardAccounts(docId, 'Sib Bank', '40156018000100', 'Dinners Grafite', 'GRAFITE', 'Dinners Elo Grafite', 'DINERS_CLUB',
                                          'NA', 'AVAILABLE');

    PERFORM addCreditCardsAccountPaymentMethod(creditCardId, '8921', false);

    PERFORM addCreditCardAccountsLimits(creditCardId, 'TOTAL', 'CONSOLIDADO', '8921', 'CREDITO_A_VISTA', 'NA', true, 'BRL',
                                        23000.9761, 'BRL', 7500.05, 'BRL', 15500.9261);

    billId := addCreditCardAccountsBills(creditCardId, '2021-05-21', 100000.04, 'BRL', 1000.04, 'BRL', false);
    PERFORM addCreditCardAccountsBillsFinanceCharge(billId, 'JUROS_REMUNERATORIOS_ATRASO_PAGAMENTO_FATURA', 'Informações Adicionais', 100000.04, 'BRL');
    PERFORM addCreditCardAccountsBillsPayment(billId, 'VALOR_PAGAMENTO_FATURA_PARCELADO', '2021-05-21', 'DEBITO_CONTA_CORRENTE', 100000.04, 'BRL');

    PERFORM addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'A_VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', 'PARCELA_1', 3, 100000.04, 100000.04, 'BRL', '2021-05-21', '2021-05-21', 5137);
    -- same transaction repeated in original data...
    PERFORM addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'A_VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', 'PARCELA_1', 3, 100000.04, 100000.04, 'BRL', '2021-05-21', '2021-05-21', 5137);

    loanId := addContractWithId(docId, 'dadd421d-184e-4689-a085-409d1bca4193','AVAILABLE', 'BRL','13832718000196', 'LOAN', '90847453264', '2022-01-08',
                                'Aquisição de equipamentos', 'EMPRESTIMOS', 'CREDITO_PESSOAL_SEM_CONSIGNACAO', '2022-01-08', null, 12070.6000, '2023-01-08',
                                'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA', '01181521040211011740907325668478542336597', 3, 14402.3790, 'DIA',
                                730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(loanId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');
    PERFORM addContractedFees(loanId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(loanId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(loanId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(loanId, '2020-01-10', 'BRL', 0.0000);

    loanReleasesId := addReleases(loanId, 'abe6e9bf-d969-44d8-87c1-f74f0f8ecb0d', true, '6bb40f5a-23e4-4c46-a2a4-c287ec72c0ac','2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(loanReleasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(loanReleasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    financingId := addContractWithId(docId, '3cbc58f0-47b9-426a-930e-ee6b55b3c087','AVAILABLE', 'BRL','13832718000193', 'FINANCING', '90847453265', '2022-01-08',
                                     'Aquisição de equipamentos', 'FINANCIAMENTOS', 'AQUISICAO_BENS_OUTROS_BENS', '2022-01-08', null, 12070.6000, '2023-01-08',
                                     'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA', '01181521040211011740907325668478542336598', 3, 14402.3790, 'DIA',
                                     730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(financingId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');

    PERFORM addContractedFees(financingId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(financingId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(financingId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(financingId, '2020-01-10', 'BRL', 0.0000);

    financingReleasesId := addReleases(financingId, '5ddbb084-4db0-4519-9b78-a75523746d05', true, '314e3f9b-960e-4775-bef8-7f2f02eadd9a','2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(financingReleasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(financingReleasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    invoiceFinancingId := addContractWithId(docId, '623ebf90-d2b6-40ad-bc51-81a3e06d65a0','AVAILABLE', 'BRL','13832718000194', 'INVOICE_FINANCING',
                                            '90847453266', '2022-01-08', 'Aquisição de equipamentos', 'DIREITOS_CREDITORIOS_DESCONTADOS', 'DESCONTO_CHEQUES',
                                            '2022-01-08', null, 12070.6000, '2023-01-08', 'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA',
                                            '01181521040211011740907325668478542336596', 3, 14402.3790, 'DIA', 730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(invoiceFinancingId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');

    PERFORM addContractedFees(invoiceFinancingId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(invoiceFinancingId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(invoiceFinancingId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(invoiceFinancingId, '2020-01-10', 'BRL', 0.0000);

    invoiceFinancingReleasesId := addReleases(invoiceFinancingId, '92d755e3-7f63-41da-b214-5a494cdc03b4', true, '99511562-efb0-4b0a-8717-25ec7b8fa4b3',
                                              '2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(invoiceFinancingReleasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(invoiceFinancingReleasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    overdraftId := addContractWithId(docId, 'e5fae2fe-603b-42c9-ae7d-70fbaad1809c','AVAILABLE', 'BRL','13832718000195', 'UNARRANGED_ACCOUNT_OVERDRAFT',
                                     '90847453267', '2022-01-08', 'Aquisição de equipamentos', 'ADIANTAMENTO_A_DEPOSITANTES', 'ADIANTAMENTO_A_DEPOSITANTES',
                                     '2022-01-08', null, 12070.6000, '2023-01-08', 'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA',
                                     '01181521040211011740907325668478542336595', 3, 14402.3790, 'DIA', 730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(overdraftId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');

    PERFORM addContractedFees(overdraftId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(overdraftId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(overdraftId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(overdraftId, '2020-01-10', 'BRL', 0.0000);

    overdraftReleasesId := addReleases(overdraftId, '5b20eb1a-9195-44ff-a145-081c5e7954f7', true, '07e289c2-8ba2-4882-9ce3-16822960a9d3',
                                       '2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(overdraftReleasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(overdraftReleasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);
END $$;

DO $$DECLARE
    docId varchar := '97812797457';
    accountHolderName varchar := 'Three Loan Guy';
    accountHolderEmail varchar := 'loan.guy@test.com';
    account1Id uuid;

    loanId uuid;
    loanId2 uuid;
    loanId3 uuid;
    loanReleasesId uuid;
    loanReleasesId2 uuid;
    loanReleasesId3 uuid;

    financingId uuid;
BEGIN
    PERFORM addAccountHolder(docId::varchar, 'CPF'::varchar, accountHolderName, accountHolderEmail);


    account1Id := addAccount(docId, 'AVAILABLE', 'BRL', 'CONTA_DEPOSITO_A_VISTA', 'INDIVIDUAL', 'Sib Bank', '40156018000100', '123',
                             '6272', '94088392', '4', 100000.04, 'BRL', 12345.0001, 'BRL', 15000.00, 'BRL', 99.9999, 'BRL', 10000.9999, 'BRL',
                             99.9999, 'BRL', '12345678', '1774', 'CACC');

    loanId := addContractWithId(docId,'470072b3-0cdf-47f8-a073-b7ead47cc718', 'AVAILABLE', 'BRL','13832718000196', 'LOAN', '90847453264', '2022-01-08',
                                'Aquisição de equipamentos', 'EMPRESTIMOS', 'CREDITO_PESSOAL_SEM_CONSIGNACAO', '2022-01-08', null, 12070.6000, '2023-01-08',
                                'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA', '01181521040211011740907325668478542336597', 3, 14402.3790, 'DIA',
                                730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(loanId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO', 'PRE_FIXADO', null, 0.0150, 0.0253, 'NA');
    PERFORM addContractedFees(loanId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(loanId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(loanId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(loanId, '2020-01-10', 'BRL', 0.0000);

    loanReleasesId := addReleases(loanId, 'abe6e9bf-d969-44d8-87c1-f74f0f8ecb0d', true, '6bb40f5a-23e4-4c46-a2a4-c287ec72c0ac','2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(loanReleasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(loanReleasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    loanId2 := addContractWithId(docId, '4973a578-0148-4ebb-9593-920eac3bdb98', 'AVAILABLE', 'BRL','13832718000196', 'LOAN', '90847453264', '2022-01-08',
                              'Aquisição de equipamentos', 'EMPRESTIMOS', 'CREDITO_PESSOAL_SEM_CONSIGNACAO', '2022-01-08', null, 12070.6000, '2023-01-08',
                              'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA', '01181521040211011740907325668478542336597', 3, 14402.3790, 'DIA',
                              730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(loanId2, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO', 'PRE_FIXADO', null, 0.0150, 0.0250, 'NA');
    PERFORM addContractedFees(loanId2, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(loanId2, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(loanId2, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(loanId2, '2020-01-10', 'BRL', 0.0000);

    loanReleasesId2 := addReleases(loanId2, 'abe6e9bf-d969-44d8-87c1-f74f0f8ecb0d', true, '6bb40f5a-23e4-4c46-a2a4-c287ec72c0ac','2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(loanReleasesId2, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(loanReleasesId2, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    loanId3 := addContractWithId(docId, '35fc1140-50de-493a-bbe8-b0db153b8727', 'AVAILABLE', 'BRL','13832718000196', 'LOAN', '90847453264', '2022-01-08',
                               'Aquisição de equipamentos', 'EMPRESTIMOS', 'CREDITO_PESSOAL_SEM_CONSIGNACAO', '2022-01-08', null, 12070.6000, '2023-01-08',
                               'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA', '01181521040211011740907325668478542336597', 3, 14402.3790, 'DIA',
                               730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(loanId3, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO', 'PRE_FIXADO', null, 0.0150, 0.0250, 'NA');
    PERFORM addContractedFees(loanId3, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(loanId3, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(loanId3, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(loanId3, '2020-01-10', 'BRL', 0.0000);

    loanReleasesId3 := addReleases(loanId3, 'abe6e9bf-d969-44d8-87c1-f74f0f8ecb0d', true, '6bb40f5a-23e4-4c46-a2a4-c287ec72c0ac','2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(loanReleasesId3, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(loanReleasesId3, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    financingId := addContractWithId(docId, '8c49d82d-d5d3-4a2a-b790-92e08d6df77a','AVAILABLE', 'BRL','13832718000192', 'FINANCING', '90847453271', '2022-01-08',
                                     'Aquisição de equipamentos', 'FINANCIAMENTOS', 'AQUISICAO_BENS_OUTROS_BENS', '2022-01-08', null, 12070.6000, '2023-01-08',
                                     'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA', '01181521040211011740907325668478542336591', 3, 14402.3790, 'DIA',
                                     730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(financingId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0253, 'NA');

    PERFORM addContractedFees(financingId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(financingId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(financingId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(financingId, '2020-01-10', 'BRL', 0.0000);

END $$;
