-- ${flyway:timestamp}
-- First, drop *all* existing data. Yes all of it. But try not to step on flyway.
DO $$DECLARE
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
    invoiceFinancingId uuid;
    creditCardId uuid;
    consentId text;
    personalId uuid;
    nationalitiesId uuid;
    billId uuid;
    billId2 uuid;
    loanId uuid;
    loanReleasesId uuid;
    loanId2 uuid;
    loanReleasesId2 uuid;
BEGIN
    PERFORM addAccountHolder(docId::varchar, 'CPF'::varchar, accountHolderName, accountHolderEmail);

    acctId := addAccountWithId(docId, '142fb6cc-d995-11eb-b8bc-0242ac130003', 'AVAILABLE', 'BRL','CONTA_DEPOSITO_A_VISTA', 'INDIVIDUAL', 'Banco Bradesco S.A', '60746948000112', '237',
                         '8546', '85215959', '5', 16025.6975, 'BRL', 0.0000, 'BRL',
                         0.0000, 'BRL', 0.0000,
                         'BRL', 0.0000, 'BRL', 0.0000,'BRL',
                         '12345678', '1774', 'SLRY');

    creditCardId := addCreditCardAccounts(docId, 'Banco Bradesco S.A', '60746948000112', 'Cartão Pós Pago',
                                    'PLATINUM', 'NA', 'VISA', 'NA' , 'AVAILABLE');

    consentId := addConsent(docId, 'urn:bradesco:BDC568642159', docId, 'CPF',
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
    PERFORM addPersonalFinancialRelationsProcurator(docId, 'REPRESENTANTE_LEGAL','NA', 'NA', 'NA');
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

    PERFORM  addAccountTransaction(acctId, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                   'CARTAO', 771.52, 'BRL', '2022-03-01', '87517400444', 'NATURAL',
                                   '237', '8546', '85215959', '5');
    PERFORM  addAccountTransaction(acctId, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                   'CARTAO', 771.52, 'BRL', '2022-04-01', '87517400444', 'NATURAL',
                                   '237', '8546', '85215959', '5');
    PERFORM  addAccountTransaction(acctId, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                   'CARTAO', 771.52, 'BRL', '2022-07-01', '87517400444', 'NATURAL',
                                   '237', '8546', '85215959', '5');
    PERFORM  addAccountTransaction(acctId, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                   'CARTAO', 771.52, 'BRL', '2022-09-01', '87517400444', 'NATURAL',
                                   '237', '8546', '85215959', '5');

    FOR i IN 1..50 LOOP
        PERFORM  addAccountTransaction(acctId, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                       'CARTAO', 771.52, 'BRL', CURRENT_DATE, '87517400444', 'NATURAL',
                                       '237', '8546', '85215959', '5');
    END LOOP;

    PERFORM addCreditCardsAccountPaymentMethod(creditCardId, '5320', false);

    PERFORM addCreditCardAccountsLimits(creditCardId, 'TOTAL', 'CONSOLIDADO', '5320', 'CREDITO_A_VISTA', 'NA', false,
                                        'BRL', 3000.0000, 'BRL', 343.0400, 'BRL', 2656.9600);

    billId := addCreditCardAccountsBills(creditCardId, '2021-07-15', 409.2600, 'BRL', 143.8912, 'BRL', true);
    PERFORM addCreditCardAccountsBillsFinanceCharge(billId, 'JUROS_REMUNERATORIOS_ATRASO_PAGAMENTO_FATURA', 'NA', 35.4500, 'BRL');
    PERFORM addCreditCardAccountsBillsFinanceCharge(billId, 'IOF', 'NA', 11.0900, 'BRL');
    PERFORM addCreditCardAccountsBillsFinanceCharge(billId, 'JUROS_MORA_ATRASO_PAGAMENTO_FATURA', 'NA', 9.6800, 'BRL');
    PERFORM addCreditCardAccountsBillsPayment(billId, 'OUTRO_VALOR_PAGO_FATURA', '2021-06-21', 'DEBITO_CONTA_CORRENTE', 1990.0000, 'BRL');
    PERFORM addCreditCardAccountsTransaction(creditCardId, billId, '5320', 'CREDITO_A_VISTA', 'BORRACHARIA DO', 'CREDITO', 'OPERACOES_CREDITO_CONTRATADAS_CARTAO',
                                             'NA', 'VISTA', 'SMS', 'NA', 'CREDITO_ROTATIVO', 'NA', '1', 1, 300.0000, 0.0000, 'BRL',
                                             '2021-05-20', '2021-05-20T08:30:00', '2021-05-20', 5912);

    FOR i IN 1..50 LOOP
        PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '5320', 'CREDITO_A_VISTA', 'ARMAZEM DOS MÓVEIS', 'CREDITO', 'OPERACOES_CREDITO_CONTRATADAS_CARTAO',
                                                  'NA', 'VISTA', 'SMS', 'NA', 'CREDITO_ROTATIVO', 'NA', '1', 1, 2043.0400, 0.0000, 'BRL',
                                                  CURRENT_DATE, CURRENT_DATE, CURRENT_DATE, 5912);
    END LOOP;

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
    PERFORM addBalloonPayments(contractId, '2021-06-15', 'BRL', 0.1000);

    loanId := addContractWithId(docId,'8cdf6902-f7d7-4d35-a8b5-db72250fd510', 'TEMPORARILY_UNAVAILABLE', 'BRL','13832718000196', 'LOAN', '90847453264', '2022-01-08',
                                'Aquisição de equipamentos', 'EMPRESTIMOS', 'CREDITO_PESSOAL_SEM_CONSIGNACAO', '2022-01-08', '2021-06-21', 12070.6000, '2023-01-08',
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

    loanId2 := addContractWithId(docId,'b7c92a74-a517-4f7c-8f6f-62ccbf032cc1', 'AVAILABLE', 'BRL','13832718000196', 'LOAN', '90847453264', '2022-01-08',
                                'Aquisição de equipamentos', 'EMPRESTIMOS', 'CREDITO_PESSOAL_SEM_CONSIGNACAO', '2022-01-08', '2021-06-21', 12070.6000, '2023-01-08',
                                'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA', '01181521040211011740907325668478542336597', 3, 14402.3790, 'DIA',
                                730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(loanId2, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');
    PERFORM addContractedFees(loanId2, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(loanId2, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(loanId2, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(loanId2, '2020-01-10', 'BRL', 0.0000);

    loanReleasesId2 := addReleases(loanId2, 'abe6e9bf-d969-44d8-87c1-f74f0f8ecb0d', true, '6bb40f5a-23e4-4c46-a2a4-c287ec72c0ac','2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(loanReleasesId2, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(loanReleasesId2, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    FOR i IN 1..20 LOOP
        invoiceFinancingId := addContract(docId, 'AVAILABLE', 'BRL', '13832718000194', 'INVOICE_FINANCING', '90847453266', CURRENT_DATE, 'Aquisição de equipamentos',
                                          'DIREITOS_CREDITORIOS_DESCONTADOS', 'DESCONTO_CHEQUES', CURRENT_DATE,
                                          CURRENT_DATE, 12070.6000, CURRENT_DATE, 'OUTROS', 'DIA', CURRENT_DATE, 0.0150, 'PRICE', 'NA',
                                          '01181521040211011740907325668478542336596', 3, 14402.3790, 'DIA', 730, 'DIA', 727, 727, 1);
        PERFORM addContractInterestRates(invoiceFinancingId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                         'PRE_FIXADO', null, 0.0150,
                                         0.0000, 'NA');
        PERFORM addContractedFees(invoiceFinancingId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
        PERFORM addContractedFinanceCharges(invoiceFinancingId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
        PERFORM addContractWarranties(invoiceFinancingId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
        PERFORM addBalloonPayments(invoiceFinancingId, '2022-12-05', 'BRL', 0.0000);
    END LOOP;

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
    PERFORM addPersonalFinancialRelationsProcurator(docId, 'REPRESENTANTE_LEGAL','NA', 'NA', 'NA');
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
                                                   'OPERACOES_CREDITO_CONTRATADAS_CARTAO', 'NA', 'VISTA', 'SAQUE_CARTAO_BRASIL', 'NA', 'CREDITO_ROTATIVO',
                                                   'NA', '1', 1, 2102.7700, 0.0000, 'BRL', '2021-01-14', '2021-01-14T08:30:00', '2021-01-15', 5992);

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
                                    'FINANCIAMENTOS', 'AQUISICAO_BENS_OUTROS_BENS', '2021-08-01', '2021-06-21', 12070.6000, '2023-08-01', 'OUTROS',
                                    'DIA', '2021-08-02', 0.0150,
                                    'PRICE', 'NA', '01181521040211011740907325668478542336599', 3,14402.3790, 'DIA',
                                    730, 'DIA', 727, 727, 1);

    -- add account transactions
    PERFORM addAccountTransactionWithId(acctId, '72f985f8-d4ee-11eb-b8bc-0242ac130003','TRANSACAO_EFETIVADA','CREDITO',
                                        'TRANSFSCD224325','DEPOSITO',1500.0500,'BRL','2021-07-05',
                                        docId,'NATURAL','748','0718','58795644','3');
    PERFORM addAccountTransactionWithId(acctId2, 'c3763b88-d4ea-11eb-b8bc-0242ac130003','TRANSACAO_EFETIVADA','DEBITO',
                                        'TRANSFSCD224325','CARTAO',2000.7700,'BRL','2021-01-18',
                                        docId,'NATURAL','748','0718','58795644','2');
    PERFORM addAccountTransactionWithId(acctId2, 'c77565ce-d4ea-11eb-b8bc-0242ac130003','LANCAMENTO_FUTURO','DEBITO',
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

    PERFORM addConsent(docId, 'urn:raidiambank:5a2a4bf9-71b7-4104-ad4b-74b0596bc546', '111111111', 'CPF',
                      NOW()::date,NOW()::date,NOW()::date,
                      NOW()::date,NOW()::date,'AUTHORISED','608b1ae4-d458-11eb-b8bc-0242ac130003');
    PERFORM addConsent(docId, 'urn:raidiambank:b6e3c1f7-8fd5-47bd-b0cf-7f997c64b47e', '2222222222', 'CPF',
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
    loanId2 uuid;
    financingId uuid;
    invoiceFinancingId uuid;
    overdraftId uuid;

    loanReleasesId uuid;
    financingReleasesId uuid;
    invoiceFinancingReleasesId uuid;
    overdraftReleasesId uuid;
    operationId uuid;
    eventId uuid;
BEGIN
    PERFORM addAccountHolder(docId::varchar, 'CPF'::varchar, accountHolderName, accountHolderEmail);
    businessIdentification := addBusinessIdentifications(docId, 'Organização A', 'Luiza e Benjamin Assessoria Jurídica Ltda',
                                                         'Mundo da Eletronica', '2021-05-21T08:30:00Z', '50685362006773');
        PERFORM addBusinessIdentificationsCompanyCnpj(businessIdentification, '50685362000131');
    PERFORM addBusinessIdentificationsCompanyCnpj(businessIdentification, '50685362006555');
    PERFORM addBusinessIdentificationsOtherDocument(businessIdentification, 'EIN', '128328453', 'CAN', '2021-05-21');
    PERFORM addBusinessIdentificationsParties(businessIdentification, 'PESSOA_NATURAL', 'SOCIO', 'Juan Kaique Cláudio Fernandes', 'Karina',
                                              'Luiza e Benjamin Assessoria Jurídica Ltda', 'Mundo da Eletronica', '2021-05-21T08:30:00Z', '0.51',
                                              'CPF', '73677831148', 'CNH', 'CAN', '2021-05-21', '2021-05-21');
    PERFORM addBusinessIdentificationsPostalAddress(businessIdentification, true, 'Av Naburo Ykesaki, 1270', 'Fundos', 'Centro', 'Marília', '3550308', 'NA',
                                                    '17500001', 'Brasil', 'BRA', '-89.8365180', '-178.836519');
    PERFORM addBusinessIdentificationsPhone(businessIdentification, true, 'FIXO', 'Informações adicionais.', '55', '19', '29875132', '932');
    PERFORM addBusinessIdentificationsEmail(businessIdentification, true, 'karinafernandes-81@br.inter.net');

    account1Id := addAccountWithId(docId, '291e5a29-49ed-401f-a583-193caa7aceee', 'AVAILABLE', 'BRL', 'CONTA_DEPOSITO_A_VISTA', 'INDIVIDUAL', 'Sib Bank', '40156018000100', '123',
                                   '6272', '94088392', '4', 100000.04, 'BRL', 12345.0001, 'BRL', 15000.00, 'BRL', 99.9999, 'BRL', 10000.9999, 'BRL',
                                   99.9999, 'BRL', '12345678', '1774', 'CACC');

    PERFORM addAccountTransactionWithId(account1Id, 'e67ed6ac-6841-44a6-b3d7-1f19d34dc204', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                        'CARTAO', 771.52, 'BRL', '2021-05-20', '87517400444', 'NATURAL',
                                        '123', '6272', '94088392', '4');

    FOR i IN 1..30 LOOP
            PERFORM  addAccountTransaction(account1Id, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                          'CARTAO', 771.52, 'BRL', '2022-07-01', '87517400444', 'NATURAL',
                                          '123', '6272', '94088392', '4');
    END LOOP;

    FOR i IN 1..30 LOOP
        PERFORM  addAccountTransaction(account1Id, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                              'CARTAO', 771.52, 'BRL', '2022-01-01', '87517400444', 'NATURAL',
                                              '123', '6272', '94088392', '4');
    END LOOP;

    -- Add a few transaction with current date
    FOR i IN 1..50 LOOP
            PERFORM  addAccountTransaction(account1Id, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                                  'CARTAO', 771.52, 'BRL', now()::timestamp at time zone 'UTC', '87517400444', 'NATURAL',
                                                  '123', '6272', '94088392', '4');
    END LOOP;

    PERFORM addAccountTransaction(account1Id, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                  'CARTAO', 771.52, 'BRL', '2021-08-20', '87517400444', 'NATURAL',
                                  '123', '6272', '94088392', '4');

    account2Id := addAccountWithId(docId, '291e5a29-49ed-401f-a583-193caa7acddd', 'AVAILABLE', 'BRL', 'CONTA_POUPANCA', 'INDIVIDUAL', 'Sib Bank', '40156018000100', '123',
                                   '6272', '11188222', '4', 41233.07, 'BRL', 999.99, 'BRL', 15000.00, 'BRL', 99.9999, 'BRL', 12345.4000,
                                   'BRL', 99.9999, 'BRL', '12345678', '1774', 'SVGS');

    PERFORM addAccountTransactionWithId(account2Id, 'bccfee35-979e-4901-b12a-415fefda9a74', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                        'CARTAO', 312.52, 'BRL', '2021-05-21', '87517400444', 'NATURAL',
                                  '123', '6272', '94088392', '4');

    PERFORM addAccountTransactionWithId(account2Id, 'b880d1c9-b1cb-4df5-9d7e-3af9f517ba67', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                        'CARTAO', 3311.35, 'BRL', '2021-05-21', '87517400444', 'NATURAL',
                                        '123', '6272', '94088392', '4');

    PERFORM addPersonalQualifications(docId, '50685362000131', 'OUTRO', '01', 'OUTROS', 100000.04, 'BRL', '2021-05-21', 100000.04, 'BRL', 2010);

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

    PERFORM addPersonalFinancialRelations(docId, '2020-05-21T08:30:00', 'Additional Information');
    PERFORM addPersonalFinancialRelationsProcurator(docId, 'PROCURADOR','76109277673', 'NA', 'NA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CONTA_DEPOSITO_A_VISTA');
    PERFORM addPersonalFinancialRelationsPortabilitiesReceived(docId, 'Test','94612232090', '98620943347036', '39416146', '2024-05-08');
    PERFORM addPersonalFinancialRelationsPaychecksBankLink(docId, 'Test','03888535872', '00462736623620', '99708230', '2024-05-08');

    businessId := addBusinessQualifications(docId, 'DIARIA', 'Informações adicionais', 100000.04, 'BRL', 2010, 100000.04, 'BRL', '2021-05-21');
    PERFORM addBusinessQualificationsEconomicActivities(businessId, 8599604, true);

    PERFORM addBusinessFinancialRelations(docId, '2022-05-21T08:30:00');
    PERFORM addBusinessFinancialRelationsProcurator(docId, 'PROCURADOR','76109277673', 'NA', 'NA');
    PERFORM addBusinessFinancialRelationsProductServicesType(docId, 'CONTA_DEPOSITO_A_VISTA');

    creditCardId := addCreditCardAccounts(docId, 'Sib Bank', '40156018000100', 'Dinners Grafite', 'GRAFITE', 'Dinners Elo Grafite', 'DINERS_CLUB',
                                          'NA', 'AVAILABLE');

    PERFORM addCreditCardsAccountPaymentMethod(creditCardId, '8921', false);

    PERFORM addCreditCardAccountsLimits(creditCardId, 'TOTAL', 'CONSOLIDADO', '8921', 'CREDITO_A_VISTA', 'NA', true, 'BRL',
                                        23000.9761, 'BRL', 7500.05, 'BRL', 15500.9261);

    billId := addCreditCardAccountsBills(creditCardId, '2022-05-21', 100000.04, 'BRL', 1000.04, 'BRL', false);
    PERFORM addCreditCardAccountsBillsFinanceCharge(billId, 'JUROS_REMUNERATORIOS_ATRASO_PAGAMENTO_FATURA', 'Informações Adicionais', 100000.04, 'BRL');
    PERFORM addCreditCardAccountsBillsPayment(billId, 'VALOR_PAGAMENTO_FATURA_PARCELADO', '2022-05-21', 'DEBITO_CONTA_CORRENTE', 100000.04, 'BRL');

    PERFORM addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', '2022-05-21', '2022-05-21T08:30:00', '2022-05-21', 5137);
    -- same transaction repeated in original data...
    PERFORM addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', '2022-05-21', '2022-05-21T08:30:00', '2022-05-21', 5137);

    FOR i IN 1..30 LOOP
        PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', '2022-07-01', '2022-07-01T08:30:00', '2022-07-01', 5137);
    END LOOP;

    FOR i IN 1..30 LOOP
        PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', '2022-01-01', '2022-01-01T08:30:00', '2022-01-01', 5137);
    END LOOP;

    -- Add a few transaction with current date
    FOR i IN 1..15 LOOP
        PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4454', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                                      'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', CURRENT_DATE-i, CURRENT_DATE-i, CURRENT_DATE-i, 5137);
        PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4452', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                                      'CREDITO_ROTATIVO', 'string', '1', 3, 200000.04, 200000.04, 'BRL', CURRENT_DATE-i, CURRENT_DATE-i, CURRENT_DATE-i, 5137);
        PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4455', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                                  'CREDITO_ROTATIVO', 'string', '1', 3, 300000.04, 300000.04, 'BRL', CURRENT_DATE-i, CURRENT_DATE-i, CURRENT_DATE-i, 5137);
    END LOOP;
    FOR i IN 1..20 LOOP
            PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4454', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                                      'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE, 5137);
            PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4452', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                                      'CREDITO_ROTATIVO', 'string', '1', 3, 200000.04, 200000.04, 'BRL', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE, 5137);
            PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4455', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                                      'CREDITO_ROTATIVO', 'string', '1', 3, 300000.04, 300000.04, 'BRL', CURRENT_DATE, CURRENT_DATE, CURRENT_DATE, 5137);
        END LOOP;
    FOR i IN 1..15 LOOP
        PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4454', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                                      'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', CURRENT_DATE+i, CURRENT_DATE-i, CURRENT_DATE+i, 5137);
        PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4452', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                                      'CREDITO_ROTATIVO', 'string', '1', 3, 200000.04, 200000.04, 'BRL', CURRENT_DATE+i, CURRENT_DATE-i, CURRENT_DATE+i, 5137);
        PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4455', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                                  'CREDITO_ROTATIVO', 'string', '1', 3, 300000.04, 300000.04, 'BRL', CURRENT_DATE+i, CURRENT_DATE-i, CURRENT_DATE+i, 5137);
    END LOOP;

    -- LOANS
    FOR i IN 1..15
        LOOP
            PERFORM addContract(docId, 'AVAILABLE', 'BRL', '13832718000196', 'LOAN', '90847453264', '2022-01-08',
                                'Aquisição de equipamentos', 'EMPRESTIMOS', 'CREDITO_PESSOAL_SEM_CONSIGNACAO',
                                '2022-01-08', '2021-06-21', 12070.6000, '2023-01-08',
                                'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA',
                                '01181521040211011740907325668478542336597', 3, 14402.3790, 'DIA',
                                730, 'DIA', 727, 727, 1);
        END LOOP;

    loanId := addContractWithId(docId, 'dadd421d-184e-4689-a085-409d1bca4193','AVAILABLE', 'BRL','13832718000196', 'LOAN', '90847453264', '2022-01-08',
                                'Aquisição de equipamentos', 'EMPRESTIMOS', 'CREDITO_PESSOAL_SEM_CONSIGNACAO', '2022-01-08', '2021-06-21', 12070.6000, '2023-01-08',
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

    -- Loan with unavailable status
    loanId2 := addContractWithId(docId, 'd7639535-e0a4-43a5-9555-0eff035715d9','UNAVAILABLE', 'BRL','13832718000197', 'LOAN', '90847453264', '2022-10-08',
                                'Aquisição de equipamentos', 'EMPRESTIMOS', 'CREDITO_PESSOAL_SEM_CONSIGNACAO', '2022-10-08', '2021-06-21', 12070.6000, '2023-01-08',
                                'OUTROS', 'DIA', '2022-10-08', 0.0150, 'PRICE', 'NA', '01181521040211011740907325668478542336597', 3, 14402.3790, 'DIA',
                                730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(loanId2, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');
    PERFORM addContractedFees(loanId2, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(loanId2, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(loanId2, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(loanId2, '2020-01-10', 'BRL', 0.0000);

    loanReleasesId := addReleases(loanId, 'abe6e9bf-d969-44d8-87c1-f74f0f8ecb0d', true, '6bb40f5a-23e4-4c46-a2a4-c287ec72c0ac','2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(loanReleasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(loanReleasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    -- FINANCING
    FOR i IN 1..15
        LOOP
            PERFORM addContract(docId, 'AVAILABLE', 'BRL', '13832718000193', 'FINANCING', '90847453265', '2022-01-08',
                                'Aquisição de equipamentos', 'FINANCIAMENTOS', 'AQUISICAO_BENS_OUTROS_BENS',
                                '2022-01-08', '2022-01-08', 12070.6000, '2023-01-08',
                                'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA',
                                '01181521040211011740907325668478542336598', 3, 14402.3790, 'DIA',
                                730, 'DIA', 727, 727, 1);
        END LOOP;

    financingId := addContractWithId(docId, '3cbc58f0-47b9-426a-930e-ee6b55b3c087','AVAILABLE', 'BRL','13832718000193', 'FINANCING', '90847453265', '2022-01-08',
                                     'Aquisição de equipamentos', 'FINANCIAMENTOS', 'AQUISICAO_BENS_OUTROS_BENS', '2022-01-08', '2022-01-08', 12070.6000, '2023-01-08',
                                     'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA', '01181521040211011740907325668478542336598', 3, 14402.3790, 'DIA',
                                     730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(financingId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');

    PERFORM addContractedFees(financingId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(financingId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(financingId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);

    FOR i IN 1..8 LOOP
            PERFORM addBalloonPayments(financingId, '2020-01-10', 'BRL', 0.0000);
    END LOOP;

    financingReleasesId := addReleases(financingId, '5ddbb084-4db0-4519-9b78-a75523746d05', true, '314e3f9b-960e-4775-bef8-7f2f02eadd9a','2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(financingReleasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(financingReleasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    -- INVOICE_FINANCING
    FOR i IN 1..15
        LOOP
            PERFORM addContract(docId, 'AVAILABLE', 'BRL', '13832718000194', 'INVOICE_FINANCING',
                                '90847453266', '2022-01-08', 'Aquisição de equipamentos',
                                'DIREITOS_CREDITORIOS_DESCONTADOS', 'DESCONTO_CHEQUES',
                                '2022-01-08', '2022-01-08', 12070.6000, '2023-01-08', 'OUTROS', 'DIA', '2022-01-08',
                                0.0150, 'PRICE', 'NA',
                                '01181521040211011740907325668478542336596', 3, 14402.3790, 'DIA', 730, 'DIA', 727, 727,
                                1);
        END LOOP;


    invoiceFinancingId := addContractWithId(docId, '623ebf90-d2b6-40ad-bc51-81a3e06d65a0','AVAILABLE', 'BRL','13832718000194', 'INVOICE_FINANCING',
                                            '90847453266', '2022-01-08', 'Aquisição de equipamentos', 'DIREITOS_CREDITORIOS_DESCONTADOS', 'DESCONTO_CHEQUES',
                                            '2022-01-08', '2022-01-08', 12070.6000, '2023-01-08', 'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA',
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

    -- UNARRANGED_ACCOUNT_OVERDRAFT

    FOR i IN 1..15
        LOOP
            overdraftId := addContract(docId, 'AVAILABLE', 'BRL', '13832718000195', 'UNARRANGED_ACCOUNT_OVERDRAFT',
                                '90847453267', '2022-01-08', 'Aquisição de equipamentos', 'ADIANTAMENTO_A_DEPOSITANTES',
                                'ADIANTAMENTO_A_DEPOSITANTES',
                                '2022-01-08', '2022-07-08', 12070.6000, '2023-01-08', 'OUTROS', 'DIA', '2022-01-08',
                                0.0150, 'PRICE', 'NA',
                                '01181521040211011740907325668478542336595', 3, 14402.3790, 'DIA', 730, 'DIA', 727, 727,
                                1);
            PERFORM addBalloonPayments(overdraftId, '2020-01-10', 'BRL', 0.0000);
        END LOOP;

    overdraftId := addContractWithId(docId, 'e5fae2fe-603b-42c9-ae7d-70fbaad1809c','AVAILABLE', 'BRL','13832718000195', 'UNARRANGED_ACCOUNT_OVERDRAFT',
                                     '90847453267', '2022-01-08', 'Aquisição de equipamentos', 'ADIANTAMENTO_A_DEPOSITANTES', 'ADIANTAMENTO_A_DEPOSITANTES',
                                     '2022-01-08', '2022-07-08', 12070.6000, '2023-01-08', 'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA',
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

    -- EXCHANGES OPERATIONS --
    PERFORM addExchangesOperationsWithId(docId, '3890f305-358d-4b5e-8e76-10f21be0fa5c', '13378052000148', 'Banco Santander (BRASIL) S.A.',
                                         '01546676000148', 'Banco do Brasil S.A.', '984302843848', 'VENDA', '2018-06-30T08:35:12Z', '2024-05-29', 1.930000339, 'BRL', 2344.04, 'BRL',
                                         98023.03, 'BRL', 32321.04, 'BRL', 2430.49, 'BRL', 0.200000, 'CARTAO_PREPAGO', '80502', CURRENT_DATE, CURRENT_DATE);

    PERFORM addExchangesOperationsEventWithId('3890f305-358d-4b5e-8e76-10f21be0fa5c', '511f9ba4-7735-4a7a-afc6-e01ea6192ee0', '540965445930', '1', '2023-05-29T011:35:00Z', '2025-05-30', 9.1, 'BRL', 28420.04, 'BRL',
                                             203944.03, 'EUR', 17003.38, 'EUR', 2430.49, 'BRL', 7.98876, 'CONTA_DEPOSITO', '10786', '90', 'Marlene Menezes', 'RO', CURRENT_DATE, CURRENT_DATE);

    FOR i IN 1..150
        LOOP
            eventId := addExchangesOperationsEvent('3890f305-358d-4b5e-8e76-10f21be0fa5c', '540965445930', '1', '2023-05-29T011:35:00Z', '2025-05-30', 9.1, 'BRL', 28420.04, 'BRL',
                                                      203944.03, 'EUR', 17003.38, 'EUR', 2430.49, 'BRL', 7.98876, 'CONTA_DEPOSITO', '10786', '51', 'Marlene Menezes', 'RO', CURRENT_DATE, CURRENT_DATE);
        END LOOP;

    FOR i IN 1..60
        LOOP
            operationId := addExchangesOperations(docId, '13378052000148', 'Banco Santander (BRASIL) S.A.',
                                                 '01546676000148', 'Banco do Brasil S.A.', '984302843848', 'VENDA', '2018-06-30T08:35:12Z', '2024-05-29', 1.930000339, 'BRL', 2344.04, 'BRL',
                                                 98023.03, 'BRL', 32321.04, 'BRL', 2430.49, 'BRL', 0.200000, 'CARTAO_PREPAGO', '80502', CURRENT_DATE, CURRENT_DATE);
            FOR i IN 1..10
                LOOP
                    PERFORM addExchangesOperationsEvent(operationId, '540965445930', '1', '2023-05-29T011:35:00Z', '2025-05-30', 9.1, 'BRL', 28420.04, 'BRL',
                                                        203944.03, 'EUR', 17003.38, 'EUR', 2430.49, 'BRL', 7.98876, 'CONTA_DEPOSITO', '10786', '51', 'Marlene Menezes', 'RO', CURRENT_DATE, CURRENT_DATE);
                END LOOP;
        END LOOP;

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
                                'Aquisição de equipamentos', 'EMPRESTIMOS', 'CREDITO_PESSOAL_SEM_CONSIGNACAO', '2022-01-08', '2021-06-21', 12070.6000, '2023-01-08',
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
                               'Aquisição de equipamentos', 'EMPRESTIMOS', 'CREDITO_PESSOAL_SEM_CONSIGNACAO', '2022-01-08', '2021-06-21', 12070.6000, '2023-01-08',
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
                                     'Aquisição de equipamentos', 'FINANCIAMENTOS', 'AQUISICAO_BENS_OUTROS_BENS', '2022-01-08', '2021-06-21', 12070.6000, '2023-01-08',
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

DO $$DECLARE
    docId varchar := '37964623168';
    accountHolderName varchar := 'Lilian Psicologia Familiar';
    accountHolderEmail varchar := 'lilian.psicologia@email.com';
    businessIdentification uuid;
    account1Id uuid;
    loanId uuid;
    loanReleasesId1 uuid;
    loanReleasesId2 uuid;
    loanReleasesId3 uuid;
    loanReleasesId4 uuid;
    businessId uuid;
BEGIN
    PERFORM addAccountHolder(docId::varchar, 'CPF'::varchar, accountHolderName, accountHolderEmail);
    PERFORM addAccountHolder('74930176972'::varchar, 'CPF'::varchar, 'Bruno Psicologia Familiar', 'bruno.psicologia@email.com');
    PERFORM addAccountHolder('85631619709'::varchar, 'CPF'::varchar, 'Carlos Psicologia Familiar', 'carlos.psicologia@email.com');
    PERFORM addAccountHolder('07423913537'::varchar, 'CPF'::varchar, 'Lucas Psicologia Familiar', 'lucas.psicologia@email.com');
    PERFORM addAccountHolder('36095824990'::varchar, 'CPF'::varchar, 'Jorge Psicologia Familiar', 'jorge.psicologia@email.com');

    account1Id := addAccountWithId(docId, 'd448bbb0-9d53-306f-816a-d59c12d73630', 'AVAILABLE', 'BRL', 'CONTA_POUPANCA', 'INDIVIDUAL', 'Banco Santander S.A.', '90400888000142', '033',
                                     '4332', '43567324', '9', 1000200.9200, 'BRL', 500961.8350, 'BRL', 35652.9000, 'BRL', 7000.0000, 'BRL', 0.0000, 'BRL',
                                     0.0000, 'BRL', '12345678', '1774', 'CACC');

    PERFORM addAccountTransactionWithId(account1Id, 'e24796a5-e55a-3cd3-b755-bbaa4d5352b0','TRANSACAO_EFETIVADA','CREDITO',
                                            'TRANSFSCD552347902','DEPOSITO',25030.1600,'BRL','2022-09-20',
                                            '90400888000142','JURIDICA','033','4332','43567324','9');
    PERFORM addAccountTransactionWithId(account1Id, '3fc8ba4f-d399-3039-8826-5b8d3f54c53d','LANCAMENTO_FUTURO','CREDITO',
                                            'TRANSFSRR000391244','DOC',200961.1600,'BRL','2022-09-20',
                                            '90400888000142','JURIDICA','033','4332','43567324','9');
    PERFORM addAccountTransactionWithId(account1Id, '265e9a41-2fc8-35a2-8cd8-ebc20f2a22cc','LANCAMENTO_FUTURO','CREDITO',
                                            'TRANSFSCD552347902','TED',300000.0000,'BRL','2022-09-20',
                                             '90400888000142','JURIDICA','033','4332','43567324','9');
    PERFORM addAccountTransactionWithId(account1Id, 'e8a959e2-4e0a-336c-bacb-7fb0450c81b3','TRANSACAO_EFETIVADA','CREDITO',
                                                'TRANSFSES676230112','PIX',15790.7800,'BRL','2022-09-20',
                                                 '90400888000142','JURIDICA','033','4332','43567324','9');

    businessIdentification := addBusinessIdentifications(docId, 'Banco Itaú Unibanco S.A.', 'Lilian Psicologia Familiar',
                                                             'Lilian Psicologia Familiar', '2018-06-30T08:35:12Z', '43053510000130');
    PERFORM addBusinessIdentificationsCompanyCnpj(businessIdentification, '60701190000104');
    PERFORM addBusinessIdentificationsParties(businessIdentification, 'PESSOA_NATURAL', 'ADMINISTRADOR', 'Lilian Vanessa Cristiane Monteiro', 'NA',
                                                  'Lilian Psicologia Familiar', 'Lilian Psicologia Familiar', '2018-10-14T10:35:12Z', '0.25',
                                                  'CPF', '37964623168', 'CPF', 'BRA', '2021-05-21', '2023-05-21');
    PERFORM addBusinessIdentificationsParties(businessIdentification, 'PESSOA_NATURAL', 'SOCIO', 'Bruno Carlos Duarte', 'NA',
                                                      'Lilian Psicologia Familiar', 'Lilian Psicologia Familiar', '2018-06-30T08:35:12Z', '0.15',
                                                      'CPF', '74930176972', 'CPF', 'BRA', '2021-05-21', '2023-05-21');
    PERFORM addBusinessIdentificationsParties(businessIdentification, 'PESSOA_NATURAL', 'SOCIO', 'Jorge da Silva Menezes', 'NA',
                                              'Lilian Psicologia Familiar', 'Lilian Psicologia Familiar', '2018-06-30T08:35:12Z', '0.15',
                                              'CPF', '36095824990', 'CPF', 'BRA', '2021-05-21', '2023-05-21');
    PERFORM addBusinessIdentificationsParties(businessIdentification, 'PESSOA_NATURAL', 'SOCIO', 'Lucas Campolina da Cruz', 'NA',
                                              'Lilian Psicologia Familiar', 'Lilian Psicologia Familiar', '2018-06-30T08:35:12Z', '0.15',
                                              'CPF', '07423913537', 'CPF', 'BRA', '2021-05-21', '2023-05-21');
    PERFORM addBusinessIdentificationsParties(businessIdentification, 'PESSOA_NATURAL', 'SOCIO', 'Carlos Joaquin Silva', 'NA',
                                              'Lilian Psicologia Familiar', 'Lilian Psicologia Familiar', '2018-06-30T08:35:12Z', '0.15',
                                              'CPF', '85631619709', 'CPF', 'BRA', '2021-05-21', '2023-05-21');
    PERFORM addBusinessIdentificationsParties(businessIdentification, 'PESSOA_JURIDICA', 'SOCIO', 'Patrícia Cardoso', 'NA',
                                                      'Patrícia Psicologia Progressiva ME', 'Patrícia Psicologia Progressiva ME', '2019-03-01T08:00:00Z', '0.15',
                                                      'CNPJ', '30290686000163', 'CNH', 'BRA', '2021-05-21', '2023-05-21');

    PERFORM addBusinessIdentificationsPostalAddress(businessIdentification, true, 'Rua Felino Barroso, 581', '', 'Jardim das Américas', 'Curitiba', '4106902', 'PR',
                                                        '81520140', 'Brasil', 'BRA', '-25.4284515', '-49.2737515');

    PERFORM addBusinessIdentificationsPhone(businessIdentification, true, 'FIXO', 'Informações adicionais.', '55', '41', '39028419', '032');
    PERFORM addBusinessIdentificationsEmail(businessIdentification, true, 'lilian.psicologia@email.com');
    PERFORM addBusinessIdentificationsOtherDocument(businessIdentification, 'EIN', '128328453', 'CAN', '2022-10-21');

    PERFORM addBusinessFinancialRelations(docId, '2018-06-30T08:35:12Z');
    PERFORM addBusinessFinancialRelationsProcurator(docId, 'REPRESENTANTE_LEGAL','37964623168', 'Lilian Vanessa Cristiane Monteiro', 'NA');
    PERFORM addBusinessFinancialRelationsProcurator(docId, 'REPRESENTANTE_LEGAL','74930176972', 'Bruno Carlos Duarte', 'NA');
    PERFORM addBusinessFinancialRelationsProcurator(docId, 'REPRESENTANTE_LEGAL','85631619709', 'Carlos Joaquin Silva', 'NA');
    PERFORM addBusinessFinancialRelationsProcurator(docId, 'REPRESENTANTE_LEGAL','07423913537', 'Lucas Campolina da Cruz', 'NA');
    PERFORM addBusinessFinancialRelationsProcurator(docId, 'REPRESENTANTE_LEGAL','36095824990', 'Jorge da Silva Menezes', 'NA');
    PERFORM addBusinessFinancialRelationsProcurator(docId, 'REPRESENTANTE_LEGAL','30290686000', 'Patrícia Psicologia Progressiva ME', 'NA');
    PERFORM addBusinessFinancialRelationsProductServicesType(docId, 'CONTA_DEPOSITO_A_VISTA');
    PERFORM addBusinessFinancialRelationsProductServicesType(docId, 'CONTA_POUPANCA');
    PERFORM addBusinessFinancialRelationsProductServicesType(docId, 'CARTAO_CREDITO');
    PERFORM addBusinessFinancialRelationsProductServicesType(docId, 'SEGURO');

    businessId := addBusinessQualifications(docId, 'SEMANAL', 'Informações adicionais', 4584.5600, 'BRL', 2020, 306210.2500, 'BRL', '2020-12-31');
    PERFORM addBusinessQualificationsEconomicActivities(businessId, 8650003, true);

    loanId := addContractWithId(docId, 'b8a6cccb-9e4e-4f21-9c7d-b83d440f363f', 'AVAILABLE', 'BRL','13832718000196', 'LOAN', '51561588037', '2018-06-14',
                                    'Cheque Especial', 'EMPRESTIMOS', 'CHEQUE_ESPECIAL', '2018-06-14', '2018-10-14', 35000.0000, '2018-10-14',
                                    'MENSAL', 'NA', '2018-07-14', 0.0263, 'PRICE', 'NA', '90400888021328032674854848', 4, 0.0000, 'MES',
                                    4, 'DIA', 0, 0, 0);

    PERFORM addContractInterestRates(loanId, 'NOMINAL', 'SIMPLES', 'AA', '30/365', 'POS_FIXADO', 'TJLP', 'NA', 0.0000, 0.0148, 'NA');
    PERFORM addContractedFees(loanId, 'Taxa de cadastro', 'CADASTRO', 'UNICA', 'FIXO', 150.0000, 0.0000);
    PERFORM addContractedFinanceCharges(loanId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0115);
    PERFORM addContractWarranties(loanId, 'BRL', 'PENHOR', 'OUTROS', 450000.0000);
    PERFORM addBalloonPayments(loanId, '2020-01-10', 'BRL', 0.0000);

    loanReleasesId1 := addReleases(loanId, uuid_generate_v4(), false, '35erb15s-b064-4f60-9f27-d7f45b6a93e4','2018-10-14','BRL', 8801.8000);
    PERFORM addOverParcelFee(loanReleasesId1, 'NA', 'NA', 0.0000);
    PERFORM addOverParcelCharge(loanReleasesId1, 'SEM_ENCARGO', 'NA', 0.0000);

    loanReleasesId2 := addReleases(loanId, uuid_generate_v4(), false, '2w1r356v-36ca-4be7-81bc-0a2eca6e35d2','2018-08-14','BRL', 8801.8000);
    PERFORM addOverParcelFee(loanReleasesId2, 'NA', 'NA', 0.0000);
    PERFORM addOverParcelCharge(loanReleasesId2, 'SEM_ENCARGO', 'NA', 0.0000);

    loanReleasesId3 := addReleases(loanId, uuid_generate_v4(), false, 'wa126v32-af65-48dd-923c-c2cc4b6f6b26','2021-08-04','BRL', 8801.8000);
    PERFORM addOverParcelFee(loanReleasesId3, 'NA', 'NA', 0.0000);
    PERFORM addOverParcelCharge(loanReleasesId3, 'SEM_ENCARGO', 'NA', 0.0000);

    loanReleasesId4 := addReleases(loanId, uuid_generate_v4(), false, 'w6u85bbc-89be-4786-a092-95135bc49595','2018-07-14','BRL', 8951.8000);
    PERFORM addOverParcelFee(loanReleasesId4, 'Taxa de cadastro', 'CADASTRO', 150.0000);
    PERFORM addOverParcelCharge(loanReleasesId4, 'SEM_ENCARGO', 'NA', 0.0000);

END $$;

DO $$DECLARE
    docId varchar := '11713609916';
    accountHolderName varchar := 'Craig Greenhouse';
    accountHolderEmail varchar := 'craig.greenhouse@raidiam.com';
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
    PERFORM addBusinessIdentificationsPostalAddress(businessIdentification, true, 'Av Naburo Ykesaki, 1270', 'Fundos', 'Centro', 'Marília', '3550308', 'NA',
                                                    '17500001', 'Brasil', 'BRA', '-89.8365180', '-178.836519');
    PERFORM addBusinessIdentificationsPhone(businessIdentification, true, 'FIXO', 'Informações adicionais.', '55', '19', '29875132', '932');
    PERFORM addBusinessIdentificationsEmail(businessIdentification, true, 'karinafernandes-81@br.inter.net');

    account1Id := addAccountWithId(docId, 'a538d0c4-64f9-41ca-a0db-b16f88945c6d', 'AVAILABLE', 'BRL', 'CONTA_DEPOSITO_A_VISTA', 'INDIVIDUAL', 'Sib Bank', '40156018000100', '123',
                                   '6272', '94088392', '4', 100000.04, 'BRL', 12345.0001, 'BRL', 15000.00, 'BRL', 99.9999, 'BRL', 10000.9999, 'BRL',
                                   99.9999, 'BRL', '12345678', '1774', 'CACC');

    PERFORM addAccountTransactionWithId(account1Id, 'fd45c039-4d34-4ecb-9c01-ae0bcfdc350e', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                        'CARTAO', 771.52, 'BRL', '2021-05-20', '87517400444', 'NATURAL',
                                        '123', '6272', '94088392', '4');

    FOR i IN 1..30 LOOP
            PERFORM  addAccountTransaction(account1Id, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                          'CARTAO', 771.52, 'BRL', '2022-07-01', '87517400444', 'NATURAL',
                                          '123', '6272', '94088392', '4');
    END LOOP;

    FOR i IN 1..30 LOOP
        PERFORM  addAccountTransaction(account1Id, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                              'CARTAO', 771.52, 'BRL', '2022-01-01', '87517400444', 'NATURAL',
                                              '123', '6272', '94088392', '4');
    END LOOP;

    PERFORM addAccountTransaction(account1Id, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                  'CARTAO', 771.52, 'BRL', '2021-08-20', '87517400444', 'NATURAL',
                                  '123', '6272', '94088392', '4');
    account2Id := addAccountWithId(docId, '093485d0-e4a4-4346-8375-37e7ae0315be', 'AVAILABLE', 'BRL', 'CONTA_POUPANCA', 'INDIVIDUAL', 'Sib Bank', '40156018000100', '123',
                                   '6272', '11188222', '4', 41233.07, 'BRL', 999.99, 'BRL', 15000.00, 'BRL', 99.9999, 'BRL', 12345.4000,
                                   'BRL', 99.9999, 'BRL', '12345678', '1774', 'SVGS');

    PERFORM addAccountTransactionWithId(account2Id, 'd4d2e119-292b-4526-bf42-3d0c78580873', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                        'CARTAO', 312.52, 'BRL', '2021-05-21', '87517400444', 'NATURAL',
                                  '123', '6272', '94088392', '4');

    PERFORM addAccountTransactionWithId(account2Id, '75c7c957-b317-449a-849c-9671f390a4b4', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                        'CARTAO', 3311.35, 'BRL', '2021-05-21', '87517400444', 'NATURAL',
                                        '123', '6272', '94088392', '4');

    PERFORM addPersonalQualifications(docId, '50685362000135', 'OUTRO', '01', 'OUTROS', 100000.04, 'BRL', '2021-05-21', 100000.04, 'BRL', 2010);

    -- the swagger included this with uuid 'c57-466-1254590932-3fa314', which is not a valid uuid...
    personalId := addPersonalIdentifications(docId, 'Alana e Bianca Assessoria Jurídica Ltda', 'Tatiana Galvão', '',
                                             '1989-03-23', 'SOLTEIRO', '', 'MASCULINO', false, '53580793004', '34229643119827236458',
                                             'CAN', '2022-05-24', '2018-05-24');
    PERFORM addPersonalIdentificationsCompanyCnpj(personalId, '77919007500');
    PERFORM addPersonalIdentificationsOtherDocuments(personalId, 'CNH', 'NA', '58438287', 'P', 'SSP/RS', '2025-05-21');
    nationalitiesId := addPersonalIdentificationsNationality(personalId, 'true');
    PERFORM addPersonalIdentificationsNationalityDocument(nationalitiesId, 'SOCIAL SEC', '423929299', '2022-08-21', '2020-05-30', 'Brasil', '');
    PERFORM addPersonalFiliation(personalId, 'MAE', 'Andreia Galvao', 'NA');

    PERFORM addPersonalPostalAddresses(personalId, true, 'Rua Laguna 129', 'Casa Amarela', 'Cavalhada',
                                       'Porto Alegre', '4314902', 'RS', '90820060', 'Brasil', 'BRA', '-30.0953952', '-51.2279909');
    PERFORM addPersonalPhones(personalId, true, 'FIXO', 'Informações adicionais.', '55', '51', '325421328', '258');
    PERFORM addPersonalEmails(personalId, true, 'tatiana.galvao@email.com');

    PERFORM addPersonalFinancialRelations(docId, '2020-05-21T08:30:00', 'Additional Information');
    PERFORM addPersonalFinancialRelationsProcurator(docId, 'PROCURADOR','NA', 'NA', 'NA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CONTA_DEPOSITO_A_VISTA');

    businessId := addBusinessQualifications(docId, 'DIARIA', 'Informações adicionais', 100000.04, 'BRL', 2010, 100000.04, 'BRL', '2021-05-21');
    PERFORM addBusinessQualificationsEconomicActivities(businessId, 8599604, true);

    PERFORM addBusinessFinancialRelations(docId, '2022-05-21T08:30:00');
    PERFORM addBusinessFinancialRelationsProcurator(docId, 'PROCURADOR','NA', 'NA', 'NA');
    PERFORM addBusinessFinancialRelationsProductServicesType(docId, 'CONTA_DEPOSITO_A_VISTA');

    creditCardId := addCreditCardAccounts(docId, 'Sib Bank', '40156018000100', 'Dinners Grafite', 'GRAFITE', 'Dinners Elo Grafite', 'DINERS_CLUB',
                                          'NA', 'AVAILABLE');

    PERFORM addCreditCardsAccountPaymentMethod(creditCardId, '8921', false);

    PERFORM addCreditCardAccountsLimits(creditCardId, 'TOTAL', 'CONSOLIDADO', '8921', 'CREDITO_A_VISTA', 'NA', true, 'BRL',
                                        23000.9761, 'BRL', 7500.05, 'BRL', 15500.9261);

    billId := addCreditCardAccountsBills(creditCardId, '2022-05-21', 100000.04, 'BRL', 1000.04, 'BRL', false);
    PERFORM addCreditCardAccountsBillsFinanceCharge(billId, 'JUROS_REMUNERATORIOS_ATRASO_PAGAMENTO_FATURA', 'Informações Adicionais', 100000.04, 'BRL');
    PERFORM addCreditCardAccountsBillsPayment(billId, 'VALOR_PAGAMENTO_FATURA_PARCELADO', '2022-05-21', 'DEBITO_CONTA_CORRENTE', 100000.04, 'BRL');

    PERFORM addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', '2022-05-21', '2022-05-21T08:30:00', '2022-05-21', 5137);
    -- same transaction repeated in original data...
    PERFORM addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', '2022-05-21', '2022-05-21T08:30:00', '2022-05-21', 5137);

    FOR i IN 1..30 LOOP 
        PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', '2022-07-01', '2022-07-01T08:30:00', '2022-07-01', 5137);
    END LOOP;

    FOR i IN 1..30 LOOP
        PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', '2022-01-01', '2022-01-01T08:30:00', '2022-01-01', 5137);
    END LOOP;

    loanId := addContractWithId(docId, 'd0612755-ed3b-4738-9cad-86d9664fc29b','AVAILABLE', 'BRL','13832718000196', 'LOAN', '90847453264', '2022-01-08',
                                'Aquisição de equipamentos', 'EMPRESTIMOS', 'CREDITO_PESSOAL_SEM_CONSIGNACAO', '2022-01-08', '2021-06-21', 12070.6000, '2023-01-08',
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

    financingId := addContractWithId(docId, 'ea3dd3b7-e555-460f-9670-2ca0d7565d3b','AVAILABLE', 'BRL','13832718000193', 'FINANCING', '90847453265', '2022-01-08',
                                     'Aquisição de equipamentos', 'FINANCIAMENTOS', 'AQUISICAO_BENS_OUTROS_BENS', '2022-01-08', '2022-01-08', 12070.6000, '2023-01-08',
                                     'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA', '01181521040211011740907325668478542336598', 3, 14402.3790, 'DIA',
                                     730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(financingId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');

    PERFORM addContractedFees(financingId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(financingId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(financingId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(financingId, '2020-01-10', 'BRL', 0.0000);

    financingReleasesId := addReleases(financingId, 'bbfb2da8-fb68-4c10-9ca5-38bc3f75af18', true, '169108f8-6147-46d2-907d-8aefd0edac97','2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(financingReleasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(financingReleasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    invoiceFinancingId := addContractWithId(docId, '59f14d70-bd01-4c1d-89f1-39e4d2883f2f','AVAILABLE', 'BRL','13832718000194', 'INVOICE_FINANCING',
                                            '90847453266', '2022-01-08', 'Aquisição de equipamentos', 'DIREITOS_CREDITORIOS_DESCONTADOS', 'DESCONTO_CHEQUES',
                                            '2022-01-08', '2022-01-08', 12070.6000, '2023-01-08', 'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA',
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

    overdraftId := addContractWithId(docId, '16ed567a-21e4-456e-aad5-b3c69b7e24de','AVAILABLE', 'BRL','13832718000195', 'UNARRANGED_ACCOUNT_OVERDRAFT',
                                     '90847453267', '2022-01-08', 'Aquisição de equipamentos', 'ADIANTAMENTO_A_DEPOSITANTES', 'ADIANTAMENTO_A_DEPOSITANTES',
                                     '2022-01-08', '2022-07-08', 12070.6000, '2023-01-08', 'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA',
                                     '01181521040211011740907325668478542336595', 3, 14402.3790, 'DIA', 730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(overdraftId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');

    PERFORM addContractedFees(overdraftId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(overdraftId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(overdraftId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(overdraftId, '2020-01-10', 'BRL', 0.0000);

    overdraftReleasesId := addReleases(overdraftId, '6905d180-af7a-4ddd-93b9-d16b79bf9454', true, '07e289c2-8ba2-4882-9ce3-16822960a9d3',
                                       '2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(overdraftReleasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(overdraftReleasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);
END $$;

DO $$DECLARE
    docId varchar := '89606262790';
    accountHolderName varchar := 'George McIntosh';
    accountHolderEmail varchar := 'george.mcintosh@raidiam.com';
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
    businessIdentification := addBusinessIdentifications(docId, 'Organização A', 'Elevenware Limited',
                                                         'Mundo da Eletronica', '2021-05-21T08:30:00Z', '50685362006773');
    PERFORM addBusinessIdentificationsCompanyCnpj(businessIdentification, '50685362000135');
    PERFORM addBusinessIdentificationsCompanyCnpj(businessIdentification, '50685362006555');
    PERFORM addBusinessIdentificationsOtherDocument(businessIdentification, 'EIN', '128328453', 'CAN', '2021-05-21');
    PERFORM addBusinessIdentificationsParties(businessIdentification, 'PESSOA_NATURAL', 'SOCIO', 'Juan Kaique Cláudio Fernandes', 'Karina',
                                              'Luiza e Benjamin Assessoria Jurídica Ltda', 'Mundo da Eletronica', '2021-05-21T08:30:00Z', '0.51',
                                              'CPF', '73677831148', 'CNH', 'CAN', '2021-05-21', '2021-05-21');
    PERFORM addBusinessIdentificationsPostalAddress(businessIdentification, true, 'Av Naburo Ykesaki, 1270', 'Fundos', 'Centro', 'Marília', '3550308', 'NA',
                                                    '17500001', 'Brasil', 'BRA', '-89.8365180', '-178.836519');
    PERFORM addBusinessIdentificationsPhone(businessIdentification, true, 'FIXO', 'Informações adicionais.', '55', '19', '29875132', '932');
    PERFORM addBusinessIdentificationsEmail(businessIdentification, true, 'karinafernandes-81@br.inter.net');

    account1Id := addAccountWithId(docId, '30394b8d-98e6-427b-bd61-50709a5536bd', 'AVAILABLE', 'BRL', 'CONTA_DEPOSITO_A_VISTA', 'INDIVIDUAL', 'Sib Bank', '40156018000100', '123',
                                   '6272', '94088392', '4', 100000.04, 'BRL', 12345.0001, 'BRL', 15000.00, 'BRL', 99.9999, 'BRL', 10000.9999, 'BRL',
                                   99.9999, 'BRL', '12345678', '1774', 'CACC');

    PERFORM addAccountTransactionWithId(account1Id, 'bb705216-90f2-4180-863e-45cd9a74b8ec', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                        'CARTAO', 771.52, 'BRL', '2021-05-20', '87517400444', 'NATURAL',
                                        '123', '6272', '94088392', '4');

FOR i IN 1..30 LOOP
            PERFORM  addAccountTransaction(account1Id, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                          'CARTAO', 771.52, 'BRL', '2022-07-01', '87517400444', 'NATURAL',
                                          '123', '6272', '94088392', '4');
END LOOP;

FOR i IN 1..30 LOOP
        PERFORM  addAccountTransaction(account1Id, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                              'CARTAO', 771.52, 'BRL', '2022-01-01', '87517400444', 'NATURAL',
                                              '123', '6272', '94088392', '4');
END LOOP;

    PERFORM addAccountTransaction(account1Id, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                  'CARTAO', 771.52, 'BRL', '2021-08-20', '87517400444', 'NATURAL',
                                  '123', '6272', '94088392', '4');
    account2Id := addAccountWithId(docId, '51115a5c-d797-4ba2-921c-da318a75bf02', 'AVAILABLE', 'BRL', 'CONTA_POUPANCA', 'INDIVIDUAL', 'Sib Bank', '40156018000100', '123',
                                   '6272', '11188222', '4', 41233.07, 'BRL', 999.99, 'BRL', 15000.00, 'BRL', 99.9999, 'BRL', 12345.4000,
                                   'BRL', 99.9999, 'BRL', '12345678', '1774', 'SVGS');

    PERFORM addAccountTransactionWithId(account2Id, '08d0e026-9731-4d01-a0f0-0e71a76867c1', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                        'CARTAO', 312.52, 'BRL', '2021-05-21', '87517400444', 'NATURAL',
                                  '123', '6272', '94088392', '4');

    PERFORM addAccountTransactionWithId(account2Id, '11f2828c-93af-468d-87b8-059ea23ad528', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                        'CARTAO', 3311.35, 'BRL', '2021-05-21', '87517400444', 'NATURAL',
                                        '123', '6272', '94088392', '4');

    PERFORM addPersonalQualifications(docId, '50685362000135', 'OUTRO', '01', 'OUTROS', 100000.04, 'BRL', '2021-05-21', 100000.04, 'BRL', 2010);

    -- the swagger included this with uuid 'c57-466-1254590932-3fa314', which is not a valid uuid...
    personalId := addPersonalIdentifications(docId, 'Alana e Bianca Assessoria Jurídica Ltda', 'Tatiana Galvão', '',
                                             '1989-03-23', 'SOLTEIRO', '', 'MASCULINO', false, '53580793004', '34229643119827236458',
                                             'CAN', '2022-05-24', '2018-05-24');
    PERFORM addPersonalIdentificationsCompanyCnpj(personalId, '77919007500');
    PERFORM addPersonalIdentificationsOtherDocuments(personalId, 'CNH', 'NA', '58438287', 'P', 'SSP/RS', '2025-05-21');
    nationalitiesId := addPersonalIdentificationsNationality(personalId, 'true');
    PERFORM addPersonalIdentificationsNationalityDocument(nationalitiesId, 'SOCIAL SEC', '423929299', '2022-08-21', '2020-05-30', 'Brasil', '');
    PERFORM addPersonalFiliation(personalId, 'MAE', 'Andreia Galvao', 'NA');

    PERFORM addPersonalPostalAddresses(personalId, true, 'Rua Laguna 129', 'Casa Amarela', 'Cavalhada',
                                       'Porto Alegre', '4314902', 'RS', '90820060', 'Brasil', 'BRA', '-30.0953952', '-51.2279909');
    PERFORM addPersonalPhones(personalId, true, 'FIXO', 'Informações adicionais.', '55', '51', '325421328', '258');
    PERFORM addPersonalEmails(personalId, true, 'tatiana.galvao@email.com');

    PERFORM addPersonalFinancialRelations(docId, '2020-05-21T08:30:00', 'Additional Information');
    PERFORM addPersonalFinancialRelationsProcurator(docId, 'PROCURADOR','NA', 'NA', 'NA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CONTA_DEPOSITO_A_VISTA');

    businessId := addBusinessQualifications(docId, 'DIARIA', 'Informações adicionais', 100000.04, 'BRL', 2010, 100000.04, 'BRL', '2021-05-21');
    PERFORM addBusinessQualificationsEconomicActivities(businessId, 8599604, true);

    PERFORM addBusinessFinancialRelations(docId, '2022-05-21T08:30:00');
    PERFORM addBusinessFinancialRelationsProcurator(docId, 'PROCURADOR','NA', 'NA', 'NA');
    PERFORM addBusinessFinancialRelationsProductServicesType(docId, 'CONTA_DEPOSITO_A_VISTA');

    creditCardId := addCreditCardAccounts(docId, 'Sib Bank', '40156018000100', 'Dinners Grafite', 'GRAFITE', 'Dinners Elo Grafite', 'DINERS_CLUB',
                                          'NA', 'AVAILABLE');

    PERFORM addCreditCardsAccountPaymentMethod(creditCardId, '8921', false);

    PERFORM addCreditCardAccountsLimits(creditCardId, 'TOTAL', 'CONSOLIDADO', '8921', 'CREDITO_A_VISTA', 'NA', true, 'BRL',
                                        23000.9761, 'BRL', 7500.05, 'BRL', 15500.9261);

    billId := addCreditCardAccountsBills(creditCardId, '2022-05-21', 100000.04, 'BRL', 1000.04, 'BRL', false);
    PERFORM addCreditCardAccountsBillsFinanceCharge(billId, 'JUROS_REMUNERATORIOS_ATRASO_PAGAMENTO_FATURA', 'Informações Adicionais', 100000.04, 'BRL');
    PERFORM addCreditCardAccountsBillsPayment(billId, 'VALOR_PAGAMENTO_FATURA_PARCELADO', '2022-05-21', 'DEBITO_CONTA_CORRENTE', 100000.04, 'BRL');

    PERFORM addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', '2022-01-01', '2022-01-01T08:30:00', '2022-05-21', 5137);
    -- same transaction repeated in original data...
    PERFORM addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', '2022-05-21', '2022-01-01T08:30:00', '2022-05-21', 5137);

FOR i IN 1..30 LOOP
        PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', '2022-07-01', '2022-01-01T08:30:00', '2022-07-01', 5137);
END LOOP;

FOR i IN 1..30 LOOP
        PERFORM  addCreditCardAccountsTransaction(creditCardId, billId, '4453', 'CREDITO_A_VISTA', 'PGTO', 'DEBITO', 'CASHBACK', 'string', 'VISTA', 'ANUIDADE', 'string',
                                             'CREDITO_ROTATIVO', 'string', '1', 3, 100000.04, 100000.04, 'BRL', '2022-01-01', '2022-01-01T08:30:00', '2022-01-01', 5137);
END LOOP;

    loanId := addContractWithId(docId, '5b19f31e-2e8a-4e98-84c4-b2b1399b7387','AVAILABLE', 'BRL','13832718000196', 'LOAN', '90847453264', '2022-01-08',
                                'Aquisição de equipamentos', 'EMPRESTIMOS', 'CREDITO_PESSOAL_SEM_CONSIGNACAO', '2022-01-08', '2021-06-21', 12070.6000, '2023-01-08',
                                'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA', '01181521040211011740907325668478542336597', 3, 14402.3790, 'DIA',
                                730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(loanId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');
    PERFORM addContractedFees(loanId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(loanId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(loanId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(loanId, '2020-01-10', 'BRL', 0.0000);

    loanReleasesId := addReleases(loanId, 'd7828d62-1c8f-426a-9b34-401ceb993b35', true, '6bb40f5a-23e4-4c46-a2a4-c287ec72c0ac','2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(loanReleasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(loanReleasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    financingId := addContractWithId(docId, '3e31de28-7d64-484a-8d38-61488ff26cf1','AVAILABLE', 'BRL','13832718000193', 'FINANCING', '90847453265', '2022-01-08',
                                     'Aquisição de equipamentos', 'FINANCIAMENTOS', 'AQUISICAO_BENS_OUTROS_BENS', '2022-01-08', '2022-01-08', 12070.6000, '2023-01-08',
                                     'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA', '01181521040211011740907325668478542336598', 3, 14402.3790, 'DIA',
                                     730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(financingId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');

    PERFORM addContractedFees(financingId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(financingId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(financingId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(financingId, '2020-01-10', 'BRL', 0.0000);

    financingReleasesId := addReleases(financingId, '97cadc25-977a-4b1c-840f-46437ed4339b', true, '169108f8-6147-46d2-907d-8aefd0edac97','2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(financingReleasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(financingReleasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    invoiceFinancingId := addContractWithId(docId, '14b02a76-4abd-4951-9293-2e0eeac415b9','AVAILABLE', 'BRL','13832718000194', 'INVOICE_FINANCING',
                                            '90847453266', '2022-01-08', 'Aquisição de equipamentos', 'DIREITOS_CREDITORIOS_DESCONTADOS', 'DESCONTO_CHEQUES',
                                            '2022-01-08', '2022-01-08', 12070.6000, '2023-01-08', 'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA',
                                            '01181521040211011740907325668478542336596', 3, 14402.3790, 'DIA', 730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(invoiceFinancingId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');

    PERFORM addContractedFees(invoiceFinancingId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(invoiceFinancingId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(invoiceFinancingId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(invoiceFinancingId, '2020-01-10', 'BRL', 0.0000);

    invoiceFinancingReleasesId := addReleases(invoiceFinancingId, 'c4fed5db-1c28-49fd-b449-20c9ea78cc51', true, '99511562-efb0-4b0a-8717-25ec7b8fa4b3',
                                              '2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(invoiceFinancingReleasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(invoiceFinancingReleasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);

    overdraftId := addContractWithId(docId, '1b0d62f8-42c3-42ab-9ed2-a3ac6ea151c2','AVAILABLE', 'BRL','13832718000195', 'UNARRANGED_ACCOUNT_OVERDRAFT',
                                     '90847453267', '2022-01-08', 'Aquisição de equipamentos', 'ADIANTAMENTO_A_DEPOSITANTES', 'ADIANTAMENTO_A_DEPOSITANTES',
                                     '2022-01-08', '2022-07-08', 12070.6000, '2023-01-08', 'OUTROS', 'DIA', '2022-01-08', 0.0150, 'PRICE', 'NA',
                                     '01181521040211011740907325668478542336595', 3, 14402.3790, 'DIA', 730, 'DIA', 727, 727, 1);

    PERFORM addContractInterestRates(overdraftId, 'NOMINAL', 'SIMPLES', 'AA', '21/252', 'PRE_FIXADO',
                                     'PRE_FIXADO', null, 0.0150,
                                     0.0000, 'NA');

    PERFORM addContractedFees(overdraftId, 'Taxa de administracao', 'ADMNISTRACAO', 'UNICA', 'FIXO', 200.5000, 0.0000);
    PERFORM addContractedFinanceCharges(overdraftId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.0600);
    PERFORM addContractWarranties(overdraftId, 'BRL', 'CESSAO_DIREITOS_CREDITORIOS', 'ACOES_DEBENTURES', 15000.3100);
    PERFORM addBalloonPayments(overdraftId, '2020-01-10', 'BRL', 0.0000);

    overdraftReleasesId := addReleases(overdraftId, '8e56acd4-d2d2-466b-bc26-4b6252999b47', true, '07e289c2-8ba2-4882-9ce3-16822960a9d3',
                                       '2021-08-04','BRL', 220.5870);
    PERFORM addOverParcelFee(overdraftReleasesId, 'Taxa de administracao', 'ADMNISTRACAO', 200.5000);
    PERFORM addOverParcelCharge(overdraftReleasesId, 'MULTA_ATRASO_PAGAMENTO', 'NA', 0.9921);
END $$;


DO $$DECLARE
    docId varchar := '87517400444';
    accountHolderName varchar := 'Gabriel Nunes';
    accountHolderEmail varchar := 'gabriel.nunes@email.com';
    account1Id uuid;
    personalId uuid;
BEGIN
    PERFORM addAccountHolder(docId::varchar, 'CPF'::varchar, accountHolderName, accountHolderEmail);

    account1Id := addAccountWithId(docId, '291e5a29-49ed-401f-a583-193caa7ac79d', 'PENDING_AUTHORISATION', 'BRL', 'CONTA_DEPOSITO_A_VISTA', 'INDIVIDUAL', 'Banco do Brasil S.A', '00000000000191', '001',
                                   '1478', '94088392', '4', 12000.2400, 'BRL', 2240.0000, 'BRL', 14500.0000, 'BRL', 0.0000, 'BRL', 1640.0600, 'BRL',
                                   99.9999, 'BRL', '12345678', '1774', 'CACC');

    PERFORM addAccountTransactionWithId(account1Id, '3c5ced44-e303-4156-9f6d-07aa7ba08e93', 'TRANSACAO_EFETIVADA', 'CREDITO', 'DEPOSITO',
                                        'PORTABILIDADE_SALARIO', 9563.0400, 'BRL', CURRENT_DATE, '87517400444', 'PESSOA_JURIDICA',
                                        '001', '85285285', '125', '4');

    PERFORM addAccountTransactionWithId(account1Id, 'b3238c51-947c-44a3-9521-accb8232e00f', 'TRANSACAO_EFETIVADA', 'DEBITO', 'BOLETO',
                                        'BOLETO', 2500.7500, 'BRL', CURRENT_DATE, '05367450000130', 'PESSOA_JURIDICA',
                                        '001', '85285285', '125', '4');

    PERFORM addAccountTransactionWithId(account1Id, '2e014b6a-a882-42de-9db0-9b99345461bf', 'TRANSACAO_EFETIVADA', 'DEBITO', 'DEPOSITO',
                                        'PORTABILIDADE_SALARIO', 9563.0400, 'BRL', CURRENT_DATE, '87517400444', 'PESSOA_JURIDICA',
                                        '001', '85285285', '125', '4');

    PERFORM addAccountTransaction(account1Id, 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO',
                                        'TED', 850.0000, 'BRL', CURRENT_DATE, '87517400444', 'PESSOA_JURIDICA',
                                        '001', '92545546', '125', '4');

    PERFORM addAccountTransactionWithId(account1Id, '7ca2434d-8b94-470e-9d47-508598465b39', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PAGAMENTO ENERGISA S.A.',
                                        'BOLETO', 568.0000, 'BRL', CURRENT_DATE, '00864214000106', 'PESSOA_NATURAL',
                                        '001', '2588', '15885222', '2');

    PERFORM addAccountTransactionWithId(account1Id, '71da9f4c-29f0-4dee-92e8-5cb4a6ea98ad', 'TRANSACAO_EFETIVADA', 'DEBITO', 'PIX',
                                        'PIX', 2.0000, 'BRL', CURRENT_DATE, '87517400444', 'PESSOA_JURIDICA',
                                        '123', '6272', '25585555', '2');

    PERFORM addAccountTransactionWithId(account1Id, '9dc1f627-e449-4b15-8c3d-50ed3e680fb0', 'TRANSACAO_EFETIVADA', 'DEBITO', 'DEPOSITO',
                                        'PORTABILIDADE_SALARIO', 9563.0400, 'BRL', CURRENT_DATE-1, '76235444036', 'PESSOA_JURIDICA',
                                        '001', '85285285', '125', '4');

    PERFORM addAccountTransactionWithId(account1Id, '599a80ed-4ffc-4a49-baa8-6be91e528a75', 'TRANSACAO_EFETIVADA', 'DEBITO', 'SAQUE 24H',
                                        'SAQUE', 1000.0000, 'BRL', CURRENT_DATE-2, '87517400444', 'PESSOA_NATURAL',
                                        '001', '1478', '94088392', '4');

    PERFORM addAccountTransactionWithId(account1Id, '60bec1aa-6a1b-4c7e-b106-cfb8889330ab', 'TRANSACAO_EFETIVADA', 'DEBITO', 'DEPOSITO',
                                        'DEPOSITO', 250.0000, 'BRL', CURRENT_DATE-3, '87517400444', 'PESSOA_JURIDICA',
                                        '001', '85285285', '125', '4');

    PERFORM addAccountTransactionWithId(account1Id, 'f8dc3d58-c35f-4aab-b1c2-bdca147a811e', 'TRANSACAO_EFETIVADA', 'DEBITO', 'BOLETO',
                                        'BOLETO', 2500.7500, 'BRL', CURRENT_DATE-4, '05367450000130', 'PESSOA_JURIDICA',
                                        '001', '85285285', '125', '4');

    PERFORM addAccountTransactionWithId(account1Id, '038b9adc-8c7d-4cca-92db-772705ab86a8', 'TRANSACAO_EFETIVADA', 'DEBITO', 'DEPOSITO',
                                        'PORTABILIDADE_SALARIO', 9563.0400, 'BRL', CURRENT_DATE-5, '87517400444', 'PESSOA_JURIDICA',
                                        '001', '85285285', '125', '4');

    PERFORM addAccountTransactionWithId(account1Id, 'a86cfb31-9dc5-4ebc-b5c1-70f29873a24b', 'TRANSACAO_EFETIVADA', 'DEBITO', 'DEPOSITO',
                                        'PORTABILIDADE_SALARIO', 9563.0400, 'BRL', CURRENT_DATE-6, '87517400444', 'PESSOA_JURIDICA',
                                        '001', '85285285', '125', '4');

    PERFORM addAccountTransactionWithId(account1Id, 'bbb22ace-46f8-4010-97dd-61acc3486153', 'TRANSACAO_EFETIVADA', 'DEBITO', 'BOLETO',
                                        'BOLETO', 2500.7500, 'BRL', CURRENT_DATE-7, '05367450000130', 'PESSOA_JURIDICA',
                                        '001', '85285285', '125', '4');

    PERFORM addAccountTransactionWithId(account1Id, 'f42b3120-e4fa-4a0e-aad0-a3f5c01d4131', 'TRANSACAO_EFETIVADA', 'DEBITO', 'DEPOSITO',
                                        'PORTABILIDADE_SALARIO', 9563.0400, 'BRL', CURRENT_DATE-8, '87517400444', 'PESSOA_JURIDICA',
                                        '001', '85285285', '125', '4');

    PERFORM addAccountTransactionWithId(account1Id, '6a09fa73-56d0-4a7d-a252-3e33b384a3ce', 'TRANSACAO_EFETIVADA', 'DEBITO', 'DEPOSITO',
                                        'PORTABILIDADE_SALARIO', 9563.0400, 'BRL', CURRENT_DATE-9, '87517400444', 'PESSOA_JURIDICA',
                                        '001', '85285285', '125', '4');

    PERFORM addAccountTransactionWithId(account1Id, 'd32035c8-bfe9-4a6b-bcf7-369fada8de83', 'TRANSACAO_EFETIVADA', 'DEBITO', 'BOLETO',
                                        'BOLETO', 2500.7500, 'BRL', CURRENT_DATE-10, '05367450000130', 'PESSOA_JURIDICA',
                                        '001', '85285285', '125', '4');

    personalId := addPersonalIdentifications(docId, 'Banco do Brasil S.A', 'Gabriel Nunes', '',
                                             '1995-05-18', 'SOLTEIRO', '', 'MASCULINO', true, '87517400444', '46779984543055562918',
                                             'BRA', '2022-05-21', '2012-05-21');
    PERFORM addPersonalIdentificationsCompanyCnpj(personalId, '00000000000191');
    PERFORM addPersonalIdentificationsOtherDocuments(personalId, 'CNH', 'NA', '58438287', 'B', 'SSP/RS', '2025-05-21');

    PERFORM addPersonalFiliation(personalId, 'MAE', 'Juliana Nunes', 'NA');

    PERFORM addPersonalPostalAddresses(personalId, true, 'Avenida Jorge Teixeira 3628', 'Fundos', 'Centro',
                                       'Alto Paraíso', '3554128', 'RO', '76862970', 'Brasil', 'BRA', '-23.5211', '-53.7264');
    PERFORM addPersonalPhones(personalId, true, 'FIXO', 'Informações adicionais.', '55', '69', '325421328', '123');
    PERFORM addPersonalEmails(personalId, true, 'gabriel.nunes@email.com');

    PERFORM addPersonalFinancialRelations(docId, '2020-05-21T08:30:00', 'Additional Information');
    PERFORM addPersonalFinancialRelationsProcurator(docId, 'PROCURADOR','43075253022', 'Juliana Nunes', 'NA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CONTA_DEPOSITO_A_VISTA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CONTA_POUPANCA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CONTA_PAGAMENTO_PRE_PAGA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CARTAO_CREDITO');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'OPERACAO_CREDITO');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'SEGURO');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'PREVIDENCIA');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'INVESTIMENTO');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'OPERACOES_CAMBIO');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CONTA_SALARIO');
    PERFORM addPersonalFinancialRelationsProductServicesType(docId, 'CREDENCIAMENTO');

    PERFORM addPersonalQualifications(docId, '00000000000191', 'RECEITA_FEDERAL', 'Administradores de tecnologia da informação', 'QUINZENAL',
                                      9563.04, 'BRL', '2021-05-20', 354200.96, 'BRL', 2006);


END $$;


DO $$DECLARE
    bankFixedIncomeInvestmentId uuid := 'e1561120-ed09-42a8-a94e-f19c62e0826f';
    docId varchar := '76109277673';
BEGIN
    PERFORM addInvestmentBankFixedIncomesWithId(docId, bankFixedIncomeInvestmentId, 'Banco do Brasil S.A', '00000000000191', 'CDB', 'BBBRT4CTF001', 0.300000, 1.100000,
                                                'LINEAR', 'MENSAL', 'DIAS_CORRIDOS', 'CDI', 'Dólar', 1000.000004, 'BRL',
                                                '2023-02-15', '2023-02-16', 'CDB421GPXXX', '2023-02-15', '2023-02-16',
                                                CURRENT_DATE, 'AVAILABLE');

    FOR i IN 1..60
        LOOP
            PERFORM addInvestmentBankFixedIncomes(docId, 'Banco do Brasil S.A', '00000000000191', 'CDB', 'BBBRT4CTF001',
                                                  0.300000, 1.100000,
                                                  'LINEAR', 'MENSAL', 'DIAS_CORRIDOS', 'CDI', 'Dólar', 1000.000004,
                                                  'BRL', '2023-02-15', '2023-02-16', 'CDB421GPXXX', '2023-02-15',
                                                  '2023-02-16', CURRENT_DATE - i, 'AVAILABLE');
        END LOOP;

    PERFORM addInvestmentBankFixedIncomesBalances(bankFixedIncomeInvestmentId, '2023-04-21T17:32:00Z', 1000.000004, 'BRL',
                                                  1000.0004, 'BRL', 1000.0004, 'BRL', 1000.0004, 'BRL', 1000.0004,
                                                  'BRL',
                                                  1000.0004, 'BRL', 1000.000004, 'BRL', 0.300000, 1.000000, 1.000000);

    PERFORM addInvestmentBankFixedIncomesTransactionsWithId('ed077660-41d5-4c4a-89b1-e4342df9f26b', bankFixedIncomeInvestmentId, 'ENTRADA', 'APLICACAO', 'string',
                                                      '2023-02-15', 42.00000025, 1000.0004, 'BRL', 1000.0004, 'BRL', 1000.04,
                                                      'BRL', 1000.04, 'BRL',
                                                      1000.04, 'BRL', 0.300000, 1.100000);

    FOR day_offset IN 1..7
        LOOP
            FOR i IN 1..60
                LOOP
                    PERFORM addInvestmentBankFixedIncomesTransactions(bankFixedIncomeInvestmentId, 'ENTRADA',
                                                                      'APLICACAO',
                                                                      'string',
                                                                      CURRENT_DATE - 1 - day_offset, 42.00000025, 1000.0004, 'BRL',
                                                                      1000.0004, 'BRL', 1000.04,
                                                                      'BRL', 1000.04, 'BRL',
                                                                      1000.04, 'BRL', 0.300000, 1.100000);
                END LOOP;

            FOR i IN 1..60
                LOOP
                    PERFORM addInvestmentBankFixedIncomesTransactions(bankFixedIncomeInvestmentId, 'ENTRADA',
                                                                      'APLICACAO', 'string',
                                                                      CURRENT_DATE - 180 - day_offset, 42.00000025, 1000.0004,
                                                                      'BRL', 1000.0004, 'BRL', 1000.04,
                                                                      'BRL', 1000.04, 'BRL',
                                                                      1000.04, 'BRL', 0.300000, 1.100000);
                END LOOP;
        END LOOP;

END $$;

DO $$DECLARE
    creditFixedIncomeInvestmentId uuid := '22276676-8264-452c-bf4d-cd3bf17b057f';
    docId varchar := '76109277673';
BEGIN
    PERFORM addInvestmentCreditFixedIncomesWithId(docId, creditFixedIncomeInvestmentId, 'Sicredi', '26294016000148', 'CRI', 'SCRST4CTF001', 0.350000, 1.160000,
                                                  'LINEAR', 'MENSAL', 'DIAS_CORRIDOS', 'CDI', 'Dólar', 1000.000004,
                                                  'BRL', '2023-02-15', '2023-02-16', 'CDB421GPXXX', '2023-02-15',
                                                  '2023-02-16', CURRENT_DATE, 'AVAILABLE', 'SIM', 'SIM', 'MENSAL');

    FOR i IN 1..60
        LOOP
            PERFORM addInvestmentCreditFixedIncomes(docId, 'Sicredi', '26294016000148', 'CRI', 'SCRST4CTF001', 0.350000,
                                                    1.160000,
                                                    'LINEAR', 'MENSAL', 'DIAS_CORRIDOS', 'CDI', 'Dólar', 1000.000004,
                                                    'BRL', '2023-02-15', '2023-02-16', 'CDB421GPXXX', '2023-02-15',
                                                    '2023-02-16', CURRENT_DATE - i, 'AVAILABLE', 'SIM', 'SIM',
                                                    'MENSAL');

        END LOOP;


    PERFORM addInvestmentCreditFixedIncomesBalances(creditFixedIncomeInvestmentId, '2023-04-21T17:32:00Z', 1000.0400, 'BRL',
                                                 1000.04, 'BRL', 1000.04, 'BRL', 1000.04, 'BRL', 1000.04, 'BRL',
                                                  1000.04, 'BRL', 1000.000004, 'BRL', 15.00, 0.300000, 1.100000);
    PERFORM addInvestmentCreditFixedIncomesTransactionsWithId('bbc47272-4940-4dfa-909b-6224fa56d801', creditFixedIncomeInvestmentId, 'ENTRADA', 'COMPRA', 'Compra de ativo',
                                                      '2023-02-15', 42.25, 1520.2560, 'BRL', 1000.0004, 'BRL', 1000.04,
                                                      'BRL', 1000.04, 'BRL',
                                                      1000.04, 'BRL', 0.300000, 1.120000);
    FOR day_offset IN 1..7
        LOOP
            FOR i IN 1..60
                LOOP
                    PERFORM addInvestmentCreditFixedIncomesTransactions(creditFixedIncomeInvestmentId, 'ENTRADA',
                                                                        'COMPRA',
                                                                        'Compra de ativo',
                                                                        CURRENT_DATE - 1 - day_offset, 42.00000025, 1000.0004, 'BRL',
                                                                        1000.0004, 'BRL', 1000.04,
                                                                        'BRL', 1000.04, 'BRL',
                                                                        1000.04, 'BRL', 0.300000, 1.120000);
                END LOOP;
            FOR i IN 1..60
                LOOP
                    PERFORM addInvestmentCreditFixedIncomesTransactions(creditFixedIncomeInvestmentId, 'ENTRADA',
                                                                        'COMPRA', 'Compra de ativo',
                                                                        CURRENT_DATE - 180 - day_offset, 42.00000025, 1000.0004,
                                                                        'BRL', 1000.0004, 'BRL', 1000.04,
                                                                        'BRL', 1000.04, 'BRL',
                                                                        1000.04, 'BRL', 0.300000, 1.120000);
                END LOOP;
        END LOOP;

END $$;

DO $$DECLARE
    fundsInvestmentId uuid := 'c0826748-22b6-432d-9b3f-a7e5a876e0bf';
    docId varchar := '76109277673';
BEGIN
    PERFORM addInvestmentFundsWithId(docId, fundsInvestmentId, 'Caixa Econômica Federal', '00360305000104', 'RENDA_FIXA', 'Renda Fixa', 'Longo Prazo',
                                     'CONSTELLATION MASTER FIA', 'BRCST4CTF001', CURRENT_DATE, 'AVAILABLE');


    FOR i IN 1..60
        LOOP
            PERFORM addInvestmentFunds(docId, 'Caixa Econômica Federal', '00360305000104', 'RENDA_FIXA', 'Renda Fixa',
                                       'Longo Prazo',
                                       'CONSTELLATION MASTER FIA', 'BRCST4CTF001', CURRENT_DATE - i, 'AVAILABLE');
        END LOOP;

    PERFORM addInvestmentFundsBalances(fundsInvestmentId, '2023-01-07', 42.25,
                                                 1000.04, 'BRL', 1000.04, 'BRL', 1000.04, 'BRL', 1000.04, 'BRL',
                                                  1000.04, 'BRL', 1000.000004, 'BRL');
    PERFORM addInvestmentFundsTransactionsWithId('91eb8ad6-20df-4b26-9625-b6b40d9cd5aa', fundsInvestmentId, 'ENTRADA', 'AMORTIZACAO', 'NA',
                                                      '2023-02-15', 42.25, 1520.2560, 'BRL', 1000.0004, 'BRL', 1000.04,
                                                      'BRL', 1000.04, 'BRL',
                                                      1000.04, 'BRL', 1000.04, 'BRL', 1000.04, 'BRL');


    FOR day_offset IN 1..7
        LOOP
            FOR i IN 1..60
                LOOP
                    PERFORM addInvestmentFundsTransactions(fundsInvestmentId, 'ENTRADA', 'AMORTIZACAO', 'NA',
                                                           CURRENT_DATE - 1 - day_offset, 42.25, 1520.2560, 'BRL',
                                                           1000.0004, 'BRL',
                                                           1000.04,
                                                           'BRL', 1000.04, 'BRL',
                                                           1000.04, 'BRL', 1000.04, 'BRL', 1000.04, 'BRL');
                END LOOP;


            FOR i IN 1..60
                LOOP
                    PERFORM addInvestmentFundsTransactions(fundsInvestmentId, 'ENTRADA', 'AMORTIZACAO', 'NA',
                                                           CURRENT_DATE - 180 - day_offset, 42.25, 1520.2560, 'BRL',
                                                           1000.0004, 'BRL', 1000.04,
                                                           'BRL', 1000.04, 'BRL',
                                                           1000.04, 'BRL', 1000.04, 'BRL', 1000.04, 'BRL');
                END LOOP;
        END LOOP;

END $$;

DO $$DECLARE
    treasureTitleId uuid := 'a5ae963d-1156-4911-ad66-59cd079afaab';
    docId varchar := '76109277673';
BEGIN
    PERFORM addInvestmentTreasureTitlesWithId(docId, treasureTitleId, 'Banco do Brasil S.A', 'TESOURODIRETO', '00000000000191', 'SIM', 'MENSAL', 'Diaria',
                                              'BRBB54CYF002', 0.300000, 0.300000, 'DIARIO', 'DIAS_CORRIDOS', 'CDI',
                                              'Dolar', '2023-02-15', '2023-02-16', CURRENT_DATE, 'AVAILABLE');

    FOR i IN 1..60
        LOOP
            PERFORM addInvestmentTreasureTitles(docId, 'Banco do Brasil S.A', 'TESOURODIRETO', '00000000000191', 'SIM',
                                                'MENSAL', 'Diaria',
                                                'BRBB54CYF002', 0.300000, 0.300000, 'DIARIO', 'DIAS_CORRIDOS', 'CDI',
                                                'Dolar', '2023-02-15', '2023-02-16', CURRENT_DATE - i, 'AVAILABLE');
        END LOOP;

    PERFORM addInvestmentTreasureTitlesBalances(treasureTitleId, '2023-04-21T17:32:00Z', 1000.0400, 'BRL',
                                                 1000.04, 'BRL', 1000.04, 'BRL', 1000.04, 'BRL', 1000.04, 'BRL',
                                                  1000.04, 'BRL', 1000.000004, 'BRL', 15.00);
    PERFORM addInvestmentTreasureTitlesTransactionsWithId('a5ae963d-1156-4911-ad66-59cd079afaab', treasureTitleId, 'ENTRADA', 'AMORTIZACAO', '',
                                                      '2023-02-15', 42.25, 1520.2560, 'BRL', 1000.0004, 'BRL', 1000.04,
                                                      'BRL', 1000.04, 'BRL',
                                                      1000.04, 'BRL', 0.300000);

    FOR day_offset IN 1..7
        LOOP
            FOR i IN 1..60
                LOOP
                    PERFORM addInvestmentTreasureTitlesTransactions(treasureTitleId, 'ENTRADA', 'AMORTIZACAO', '',
                                                                    CURRENT_DATE - 1 - day_offset, 42.25, 1520.2560,
                                                                    'BRL', 1000.0004,
                                                                    'BRL', 1000.04,
                                                                    'BRL', 1000.04, 'BRL',
                                                                    1000.04, 'BRL', 0.300000);
                END LOOP;


            FOR i IN 1..60
                LOOP
                    PERFORM addInvestmentTreasureTitlesTransactions(treasureTitleId, 'ENTRADA', 'AMORTIZACAO', '',
                                                                    CURRENT_DATE - 180 - day_offset, 42.25, 1520.2560,
                                                                    'BRL', 1000.0004, 'BRL', 1000.04,
                                                                    'BRL', 1000.04, 'BRL',
                                                                    1000.04, 'BRL', 0.300000);
                END LOOP;
        END LOOP;



END $$;

DO $$DECLARE
    variableIncomeInvestmentId uuid := 'e4e6bce7-2182-4502-a5c7-574af90a537e';
    brokerNoteId uuid := '50fa1645-d45b-4c06-a8c2-1f402aeda850';
    docId varchar := '76109277673';
BEGIN
    PERFORM addInvestmentVariableIncomesWithId(docId, variableIncomeInvestmentId, 'Itau', '60701190000104', 'PETR4',
                                               'BRDIVOCTF002', CURRENT_DATE, 'AVAILABLE');

    FOR i IN 1..60
        LOOP
            PERFORM addInvestmentVariableIncomes(docId, 'Itau', '60701190000104', 'PETR4', 'BRDIVOCTF002',
                                                 CURRENT_DATE - i, 'AVAILABLE');
        END LOOP;

    PERFORM addInvestmentVariableIncomesBalances(variableIncomeInvestmentId, '2023-06-01', 137.0105, 1000.0400, 'BRL',
                                                 1000.04, 'BRL', 1000.04, 'BRL', 1000.04);
    PERFORM addInvestmentVariableIncomesTransactionsWithId('ed077660-41d5-4c4a-89b1-e4342df9f26b', variableIncomeInvestmentId, '50fa1645-d45b-4c06-a8c2-1f402aeda850', 'ENTRADA', 'DIVIDENDOS', 'string',
                                                      '2023-02-15', 42.25, 42.25, 1520.2560, 'BRL', 1000.0004, 'BRL');

    FOR day_offset IN 1..7
        LOOP
            FOR i IN 1..60
                LOOP
                    PERFORM addInvestmentVariableIncomesTransactions(variableIncomeInvestmentId, brokerNoteId,
                                                                     'ENTRADA',
                                                                     'DIVIDENDOS', 'string',
                                                                     CURRENT_DATE - 1 - day_offset, 42.25, 42.25,
                                                                     1520.2560, 'BRL',
                                                                     1000.0004, 'BRL');
                END LOOP;
            FOR i IN 1..60
                LOOP
                    PERFORM addInvestmentVariableIncomesTransactions(variableIncomeInvestmentId, brokerNoteId,
                                                                     'ENTRADA', 'DIVIDENDOS', 'string',
                                                                     CURRENT_DATE - 180 - day_offset, 42.25, 42.25,
                                                                     1520.2560, 'BRL', 1000.0004, 'BRL');
                END LOOP;
        END LOOP;


    PERFORM addInvestmentVariableIncomesBrokerNotesWithId(brokerNoteId,'1854009930314350', 5000.0024, 'BRL', 5000.0024, 'BRL', 5000.0024, 'BRL',
                                                5000.0024, 'BRL', 5000.0024, 'BRL', 5000.0024, 'BRL', 5000.0024, 'BRL',
                                                5000.0024, 'BRL', 5000.0024, 'BRL', 5000.0024, 'BRL');
END $$;