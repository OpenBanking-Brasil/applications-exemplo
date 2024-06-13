--- RECREATABLE MIGRATION FILE ---
--- This will be run every time flyway detects that its hash has changed ---
--- Needs to be kept up to date with schema changes in the other migrations ---

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- Functions we're going to need to get the job done ...
CREATE OR REPLACE FUNCTION addAccountHolder(doc varchar, rel varchar, accountHolderName varchar, userId varchar) RETURNS uuid AS $$
    INSERT INTO account_holders (document_identification, document_rel, account_holder_name, user_id,
                                 created_at, created_by, updated_at, updated_by)
    VALUES (doc, rel, accountHolderName, userId,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING account_holder_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION getAccountHolderId(docId varchar) RETURNS uuid AS $$
    SELECT account_holder_id FROM account_holders WHERE document_identification = docId;
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalFinancialRelations(docId varchar, startDate timestamp, additional varchar) RETURNS uuid AS $$
    INSERT INTO personal_financial_relations (account_holder_id, start_date, products_services_type_additional_info,
                                              created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId), startDate, additional,
        NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING personal_financial_relations_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION getPersonalFinancialRelationsId(docId varchar) RETURNS uuid AS $$
    -- works as this is a 1:1 relation with accountholder
    SELECT personal_financial_relations_id FROM personal_financial_relations WHERE account_holder_id = getAccountHolderId(docId) LIMIT 1;
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalFinancialRelationsProcurator(docId varchar, type varchar, cpf varchar, civilName varchar,
                                                                   socialName varchar) RETURNS void AS $$
    INSERT INTO personal_financial_relations_procurators (personal_financial_relations_id, type, cpf_number, civil_name,
                                                          social_name,
                                                          created_at, created_by, updated_at, updated_by)
    VALUES (getPersonalFinancialRelationsId(docId), type, cpf, civilName, socialName,
        NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalFinancialRelationsProductServicesType(docId varchar, type varchar) RETURNS void AS $$
    INSERT INTO personal_financial_relations_products_services_type (personal_financial_relations_id, type,
                                                                     created_at, created_by, updated_at, updated_by)
    VALUES (getPersonalFinancialRelationsId(docId), type,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalFinancialRelationsPortabilitiesReceived(docId varchar, employerName varchar, employerCnpjCpf varchar, paycheckBankDetainerCnpj varchar,
                                                                              paycheckBankDetainerIspb varchar, portabilityApprovalDate date) RETURNS void AS $$
INSERT INTO personal_financial_relations_portabilities_received (personal_financial_relations_id, employer_name, employer_cnpj_cpf, paycheck_bank_detainer_cnpj,
                                                                 paycheck_bank_detainer_ispb, portability_approval_date, created_at, created_by, updated_at, updated_by)
VALUES (getPersonalFinancialRelationsId(docId), employerName, employerCnpjCpf, paycheckBankDetainerCnpj, paycheckBankDetainerIspb, portabilityApprovalDate,
        NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalFinancialRelationsPaychecksBankLink(docId varchar, employerName varchar, employerCnpjCpf varchar, paycheckBankCnpj varchar,
                                                                              paycheckBankIspb varchar, accountOpeningDate date) RETURNS void AS $$
INSERT INTO personal_financial_relations_paychecks_bank_link (personal_financial_relations_id, employer_name, employer_cnpj_cpf, paycheck_bank_cnpj,
                                                                 paycheck_bank_ispb, account_opening_date, created_at, created_by, updated_at, updated_by)
VALUES (getPersonalFinancialRelationsId(docId), employerName, employerCnpjCpf, paycheckBankCnpj, paycheckBankIspb, accountOpeningDate,
    NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalIdentifications(docId varchar, brandName varchar, civilName varchar, socialName varchar,
                                                      birthDate date, maritalStatusCode varchar, maritalStatusAdditionalInfo varchar,
                                                      sex varchar, hasBrazilianNationality boolean, cpfNumber varchar, passportNumber varchar,
                                                      passportCountry varchar, passportExpirationDate date, passportIssueDate date) RETURNS uuid AS $$
    INSERT INTO personal_identifications (account_holder_id, brand_name, civil_name, social_name, birth_date,
                                          marital_status_code, marital_status_additional_info, sex,
                                          has_brazilian_nationality, cpf_number, passport_number, passport_country,
                                          passport_expiration_date, passport_issue_date,
                                          created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId),brandName, civilName, socialName, birthDate, maritalStatusCode,
            maritalStatusAdditionalInfo, sex, hasBrazilianNationality, cpfNumber, passportNumber,
            passportCountry, passportExpirationDate, passportIssueDate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING personal_identifications_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalIdentificationsWithId(docId varchar, personalIdentificationsId uuid, brandName varchar,
                                                            civilName varchar, socialName varchar, birthDate date, maritalStatusCode varchar,
                                                            maritalStatusAdditionalInfo varchar, sex varchar, hasBrazilianNationality boolean,
                                                            cpfNumber varchar, passportNumber varchar, passportCountry varchar,
                                                            passportExpirationDate date, passportIssueDate date) RETURNS uuid AS $$
    INSERT INTO personal_identifications (account_holder_id, personal_identifications_id, brand_name, civil_name,
                                      social_name, birth_date, marital_status_code, marital_status_additional_info, sex,
                                      has_brazilian_nationality, cpf_number, passport_number, passport_country,
                                      passport_expiration_date, passport_issue_date,
                                      created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId), personalIdentificationsId, brandName, civilName, socialName, birthDate,
        maritalStatusCode, maritalStatusAdditionalInfo, sex, hasBrazilianNationality, cpfNumber, passportNumber,
        passportCountry, passportExpirationDate, passportIssueDate,
        NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING personal_identifications_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalIdentificationsCompanyCnpj(personalIdentificationsId uuid, companyCnpj varchar) RETURNS void AS $$
    INSERT INTO personal_identifications_company_cnpj (personal_identifications_id, company_cnpj,
                                                     created_at, created_by, updated_at, updated_by)
    VALUES (personalIdentificationsId, companyCnpj,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalIdentificationsOtherDocuments(personalIdentificationsId uuid, type varchar, typeAdditionalInfo varchar,
                                                                    number varchar, checkDigit varchar, additionalInfo varchar,
                                                                    expirationDate date) RETURNS void AS $$
    INSERT INTO personal_other_documents (personal_identifications_id, type, type_additional_info, number, check_digit,
                                          additional_info, expiration_date,
                                          created_at, created_by, updated_at, updated_by)
    VALUES (personalIdentificationsId, type, typeAdditionalInfo, number, checkDigit, additionalInfo, expirationDate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalIdentificationsNationality(personalIdentificationsId uuid, otherNationalitiesInfo varchar) RETURNS uuid AS $$
    INSERT INTO personal_nationality (personal_identifications_id, other_nationalities_info,
                                          created_at, created_by, updated_at, updated_by)
    VALUES (personalIdentificationsId, otherNationalitiesInfo,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING personal_nationality_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalIdentificationsNationalityDocument(personalNationalityId uuid, docType varchar, number varchar,
                                                                         expirationDate date, issueDate date, country varchar,
                                                                         typeAdditionalInfo varchar) RETURNS void AS $$
    INSERT INTO personal_nationality_documents (personal_nationality_id, type, number, expiration_date, issue_date,
                                                country, type_additional_info,
                                                created_at, created_by, updated_at, updated_by)
    VALUES (personalNationalityId, docType, number, expirationDate, issueDate, country, typeAdditionalInfo,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalFiliation(personalIdentificationsId uuid, type varchar, civilName varchar, socialName varchar) RETURNS void AS $$
    INSERT INTO personal_filiation (personal_identifications_id, type, civil_name, social_name,
                                    created_at, created_by, updated_at, updated_by)
    VALUES (personalIdentificationsId, type, civilName, socialName,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalQualifications(docId varchar, companyCnpj varchar, occupationCode varchar, occupationDescription varchar,
                                                     informedIncomeFrequency varchar, informedIncomeAmount numeric, informedIncomeCurrency varchar,
                                                     informedIncomeDate date, informedPatrimonyAmount numeric, informedPatrimonyCurrency varchar,
                                                     informedPatrimonyYear integer) RETURNS void AS $$
    INSERT INTO personal_qualifications (account_holder_id, company_cnpj, occupation_code, occupation_description,
                                         informed_income_frequency, informed_income_amount, informed_income_currency,
                                         informed_income_date, informed_patrimony_amount, informed_patrimony_currency,
                                         informed_patrimony_year,
                                         created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId), companyCnpj, occupationCode, occupationDescription, informedIncomeFrequency,
            informedIncomeAmount, informedIncomeCurrency, informedIncomeDate, informedPatrimonyAmount,
            informedPatrimonyCurrency, informedPatrimonyYear,
        NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addAccount(docId varchar, status varchar, currency varchar, accountType varchar, accountSubType varchar,
                                      brandName varchar, companyCnpj varchar, compeCode varchar, branchCode varchar, number varchar,
                                      checkDigit varchar, availableAmount double precision, availableAmountCurrency varchar,
                                      blockedAmount double precision, blockedAmountCurrency varchar, automaticallyInvestedAmount double precision,
                                      automaticallyInvestedAmountCurrency varchar, overdraftContractedLimit double precision,
                                      overdraftContractedLimitCurrency varchar, overdraftUsedLimit double precision,
                                      overdraftUsedLimitCurrency varchar, unarrangedOverdraftAmount double precision,
                                      unarrangedOverdraftAmountCurrency varchar, debtorIspb varchar, debtorIssuer varchar,
                                      debtorType varchar) RETURNS uuid AS $$
    INSERT INTO accounts (account_holder_id, status, currency, account_type, account_sub_type, brand_name, company_cnpj,
                          compe_code, branch_code, number, check_digit, available_amount, available_amount_currency,
                          blocked_amount, blocked_amount_currency, automatically_invested_amount,
                          automatically_invested_amount_currency, overdraft_contracted_limit,
                          overdraft_contracted_limit_currency, overdraft_used_limit, overdraft_used_limit_currency,
                          unarranged_overdraft_amount, unarranged_overdraft_amount_currency, debtor_ispb, debtor_issuer,
                          debtor_type,
                          created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId), status, currency, accountType, accountSubType, brandName, companyCnpj,
            compeCode, branchCode, number, checkDigit, availableAmount, availableAmountCurrency, blockedAmount,
            blockedAmountCurrency, automaticallyInvestedAmount, automaticallyInvestedAmountCurrency,
            overdraftContractedLimit, overdraftContractedLimitCurrency, overdraftUsedLimit, overdraftUsedLimitCurrency,
            unarrangedOverdraftAmount, unarrangedOverdraftAmountCurrency, debtorIspb, debtorIssuer, debtorType,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING account_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addAccountWithId(docId varchar, accountId uuid, status varchar, currency varchar, accountType varchar,
                                            accountSubType varchar, brandName varchar, companyCnpj varchar, compeCode varchar,
                                            branchCode varchar, number varchar, checkDigit varchar, availableAmount double precision,
                                            availableAmountCurrency varchar, blockedAmount double precision, blockedAmountCurrency varchar,
                                            automaticallyInvestedAmount double precision, automaticallyInvestedAmountCurrency varchar,
                                            overdraftContractedLimit double precision, overdraftContractedLimitCurrency varchar,
                                            overdraftUsedLimit double precision, overdraftUsedLimitCurrency varchar,
                                            unarrangedOverdraftAmount double precision, unarrangedOverdraftAmountCurrency varchar,
                                            debtorIspb varchar, debtorIssuer varchar, debtorType varchar) RETURNS uuid AS $$
    INSERT INTO accounts (account_holder_id, account_id, status, currency, account_type, account_sub_type, brand_name,
                          company_cnpj, compe_code, branch_code, number, check_digit, available_amount,
                          available_amount_currency, blocked_amount, blocked_amount_currency,
                          automatically_invested_amount, automatically_invested_amount_currency,
                          overdraft_contracted_limit, overdraft_contracted_limit_currency, overdraft_used_limit,
                          overdraft_used_limit_currency, unarranged_overdraft_amount,
                          unarranged_overdraft_amount_currency, debtor_ispb, debtor_issuer, debtor_type,
                          created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId), accountId, status, currency, accountType, accountSubType, brandName,
            companyCnpj, compeCode, branchCode, number, checkDigit, availableAmount, availableAmountCurrency,
            blockedAmount, blockedAmountCurrency, automaticallyInvestedAmount, automaticallyInvestedAmountCurrency,
            overdraftContractedLimit, overdraftContractedLimitCurrency, overdraftUsedLimit, overdraftUsedLimitCurrency,
            unarrangedOverdraftAmount, unarrangedOverdraftAmountCurrency, debtorIspb, debtorIssuer, debtorType,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING account_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addContract(docId varchar, status varchar, currency varchar, companyCnpj varchar, contractType varchar,
                                       contractNumber varchar, contractDate date, productName varchar, productType varchar,
                                       productSubType varchar, disbursementDate date, settlementDate date, contractAmount double precision,
                                       dueDate date, instalmentPeriodicity varchar, instalmentPeriodicityAdditionalInfo varchar,
                                       firstInstalmentDueDate date, cet double precision, amortizationScheduled varchar,
                                       amortizationScheduledAdditionalInfo varchar, ipocCode varchar, paidInstalments integer,
                                       contractOutstandingBalance double precision, typeNumberOfInstalments varchar, totalNumberOfInstalments integer,
                                       typeContractRemaining varchar, contractRemainingNumber integer, dueInstalments integer,
                                       pastDueInstalments integer) RETURNS uuid AS $$
    INSERT INTO contracts (account_holder_id, status, currency, company_cnpj, contract_type, contract_number,
                           contract_date, product_name, product_type, product_sub_type, disbursement_date,
                           settlement_date, contract_amount, due_date, instalment_periodicity,
                           instalment_periodicity_additional_info, first_instalment_due_date, cet,
                           amortization_scheduled, amortization_scheduled_additional_info, ipoc_code, paid_instalments,
                           contract_outstanding_balance, type_number_of_instalments, total_number_of_instalments,
                           type_contract_remaining, contract_remaining_number, due_instalments, past_due_instalments,
                           created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId), status, currency, companyCnpj, contractType, contractNumber, contractDate,
            productName, productType, productSubType, disbursementDate, settlementDate, contractAmount, dueDate,
            instalmentPeriodicity, instalmentPeriodicityAdditionalInfo, firstInstalmentDueDate, cet,
            amortizationScheduled, amortizationScheduledAdditionalInfo, ipocCode, paidInstalments,
            contractOutstandingBalance, typeNumberOfInstalments, totalNumberOfInstalments, typeContractRemaining,
            contractRemainingNumber, dueInstalments, pastDueInstalments,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING contract_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addContractWithId(docId varchar, contractId uuid, status varchar, currency varchar, companyCnpj varchar,
                                             contractType varchar, contractNumber varchar, contractDate date, productName varchar,
                                             productType varchar, productSubType varchar, disbursementDate date, settlementDate date,
                                             contractAmount double precision, dueDate date, instalmentPeriodicity varchar,
                                             instalmentPeriodicityAdditionalInfo varchar, firstInstalmentDueDate date,
                                             cet double precision, amortizationScheduled varchar, amortizationScheduledAdditionalInfo varchar,
                                             ipocCode varchar, paidInstalments integer, contractOutstandingBalance double precision,
                                             typeNumberOfInstalments varchar, totalNumberOfInstalments integer, typeContractRemaining varchar,
                                             contractRemainingNumber integer, dueInstalments integer, pastDueInstalments integer) RETURNS uuid AS $$
INSERT INTO contracts (account_holder_id, contract_id, status, currency, company_cnpj, contract_type, contract_number,
                       contract_date, product_name, product_type, product_sub_type, disbursement_date, settlement_date,
                       contract_amount, due_date, instalment_periodicity, instalment_periodicity_additional_info,
                       first_instalment_due_date, cet, amortization_scheduled, amortization_scheduled_additional_info,
                       ipoc_code, paid_instalments, contract_outstanding_balance, type_number_of_instalments,
                       total_number_of_instalments, type_contract_remaining, contract_remaining_number, due_instalments,
                       past_due_instalments,
                       created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId), contractId, status, currency, companyCnpj, contractType, contractNumber,
            contractDate, productName, productType, productSubType, disbursementDate, settlementDate, contractAmount,
            dueDate, instalmentPeriodicity, instalmentPeriodicityAdditionalInfo, firstInstalmentDueDate, cet,
            amortizationScheduled, amortizationScheduledAdditionalInfo, ipocCode, paidInstalments,
            contractOutstandingBalance, typeNumberOfInstalments, totalNumberOfInstalments, typeContractRemaining,
            contractRemainingNumber, dueInstalments, pastDueInstalments,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING contract_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addContractInterestRates(contractId uuid, taxType varchar, interestRateType varchar, taxPeriodicity varchar,
                                                    calculation varchar, referentialRateIndexerType varchar, referentialRateIndexerSubType varchar,
                                                    referentialRateIndexerAdditionalInfo varchar, preFixedRate double precision,
                                                    postFixedRate double precision, additionalInfo varchar) RETURNS void AS $$
    INSERT INTO interest_rates (contract_id, tax_type, interest_rate_type, tax_periodicity, calculation,
                                referential_rate_indexer_type, referential_rate_indexer_sub_type,
                                referential_rate_indexer_additional_info, pre_fixed_rate, post_fixed_rate,
                                additional_info,
                                created_at, created_by, updated_at, updated_by)
    VALUES (contractId, taxType, interestRateType, taxPeriodicity, calculation, referentialRateIndexerType,
            referentialRateIndexerSubType, referentialRateIndexerAdditionalInfo, preFixedRate,
            postFixedRate, additionalInfo,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addContractedFees(contractId uuid, feeName varchar, feeCode varchar, feeChargeType varchar,
                                             feeCharge varchar, feeAmount double precision, feeRate double precision) RETURNS void AS $$
    INSERT INTO contracted_fees (contract_id, fee_name, fee_code, fee_charge_type,
                                 fee_charge, fee_amount, fee_rate,
                                 created_at, created_by, updated_at, updated_by)
    VALUES (contractId, feeName, feeCode, feeChargeType, feeCharge, feeAmount, feeRate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addContractedFinanceCharges(contractId uuid, chargeType varchar, chargeAdditionalInfo varchar,
                                                       chargeRate double precision) RETURNS void AS $$
    INSERT INTO contracted_finance_charges (contract_id , charge_type , charge_additional_info, charge_rate,
                                            created_at, created_by, updated_at, updated_by)
    VALUES (contractId, chargeType, chargeAdditionalInfo, chargeRate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addContractWarranties(contractId uuid, currency varchar, warrantyType varchar, warrantySubtype varchar,
                                                 warrantyAmount double precision) RETURNS void AS $$
    INSERT INTO warranties (contract_id , currency , warranty_type, warranty_subtype, warranty_amount,
                            created_at, created_by, updated_at, updated_by)
    VALUES (contractId, currency, warrantyType, warrantySubtype, warrantyAmount,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addBalloonPayments(contractId uuid, dueDate varchar, currency varchar, amount double precision) RETURNS void AS $$
    INSERT INTO balloon_payments (contract_id , due_date , currency, amount,
                                  created_at, created_by, updated_at, updated_by)
    VALUES (contractId, dueDate, currency, amount,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addReleases(contractId uuid, paymentsId uuid, isOverParcelPayment boolean, instalmentId varchar, paidDate varchar,
                                       currency varchar, paidAmount double precision) RETURNS uuid AS $$
    INSERT INTO releases (contract_id, payments_id, is_over_parcel_payment, instalment_id,
                          paid_date, currency, paid_amount,
                          created_at, created_by, updated_at, updated_by)
    VALUES (contractId, paymentsId, isOverParcelPayment, instalmentId, paidDate,currency, paidAmount,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING releases_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addOverParcelFee(releasesId uuid, feeName varchar, feeCode varchar, feeAmount double precision) RETURNS void AS $$
    INSERT INTO over_parcel_fees (releases_id, fee_name, fee_code, fee_amount,
                                  created_at, created_by, updated_at, updated_by)
    VALUES (releasesId, feeName, feeCode, feeAmount,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addOverParcelCharge(releasesId uuid, chargeType varchar, chargeAdditionalInfo varchar,
                                               chargeAmount double precision) RETURNS void AS $$
    INSERT INTO over_parcel_charges (releases_id, charge_type, charge_additional_info, charge_amount,
                                  created_at, created_by, updated_at, updated_by)
    VALUES (releasesId, chargeType, chargeAdditionalInfo, chargeAmount,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;


DROP FUNCTION IF EXISTS addAccountTransactionWithId;
DROP FUNCTION IF EXISTS addAccountTransaction;
CREATE OR REPLACE FUNCTION addAccountTransactionWithId(accountId uuid, transactionId uuid, completedAuthorisedPaymentType varchar,
                                                 creditDebitType varchar, transactionName varchar, transactionType varchar,
                                                 transactionAmount double precision, transactionCurrency varchar, transactionDateTime timestamp,
                                                 partieCnpjCpf varchar, partiePersonType varchar, partieCompeCode varchar,
                                                 partieBranchCode varchar, partieNumber varchar, partieCheckDigit varchar) RETURNS int AS $$
INSERT INTO account_transactions(account_id, transaction_id, completed_authorised_payment_type, credit_debit_type,
                                  transaction_name, type, amount, transaction_currency, transaction_date_time,
                                  partie_cnpj_cpf, partie_person_type, partie_compe_code, partie_branch_code,
                                  partie_number, partie_check_digit,
                                  created_at, created_by, updated_at, updated_by)
VALUES (accountId, transactionId, completedAuthorisedPaymentType, creditDebitType, transactionName,
        transactionType, transactionAmount, transactionCurrency, transactionDateTime, partieCnpjCpf, partiePersonType,
        partieCompeCode, partieBranchCode, partieNumber, partieCheckDigit,
        NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING account_transaction_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addAccountTransaction(accountId uuid, completedAuthorisedPaymentType varchar,
                                                 creditDebitType varchar, transactionName varchar, transactionType varchar,
                                                 transactionAmount double precision, transactionCurrency varchar, transactionDateTime timestamp with time zone,
                                                 partieCnpjCpf varchar, partiePersonType varchar, partieCompeCode varchar,
                                                 partieBranchCode varchar, partieNumber varchar, partieCheckDigit varchar) RETURNS int AS $$
    INSERT INTO account_transactions (account_id, transaction_id, completed_authorised_payment_type, credit_debit_type,
                                  transaction_name, type, amount, transaction_currency, transaction_date_time,
                                  partie_cnpj_cpf, partie_person_type, partie_compe_code, partie_branch_code,
                                  partie_number, partie_check_digit,
                                  created_at, created_by, updated_at, updated_by)
    VALUES (accountId, uuid_generate_v4(), completedAuthorisedPaymentType, creditDebitType, transactionName,
            transactionType, transactionAmount, transactionCurrency, transactionDateTime, partieCnpjCpf, partiePersonType,
            partieCompeCode, partieBranchCode, partieNumber, partieCheckDigit,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING account_transaction_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addConsent(docId varchar, consentId varchar, businessDocumentIdentification varchar, businessDocumentRel varchar, expirationDateTime date,
                                      transactionFromDateTime date, transactionToDateTime date, creationDateTime date,
                                      statusUpdateDateTime date, status varchar, clientId varchar) RETURNS text AS $$
    INSERT INTO consents (consent_id, business_document_identification, business_document_rel, account_holder_id, expiration_date_time, transaction_from_date_time,
                          transaction_to_date_time, creation_date_time, status_update_date_time, status, client_id,
                          created_at, created_by, updated_at, updated_by)
    VALUES (consentId, businessDocumentIdentification, businessDocumentRel, getAccountHolderId(docId), expirationDateTime,
            transactionFromDateTime, transactionToDateTime, creationDateTime, statusUpdateDateTime, status, clientId,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING consent_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addConsentPermissions(permission varchar, consentId text) RETURNS int AS $$
    INSERT INTO consent_permissions (permission, consent_id, created_at, created_by, updated_at, updated_by)
    VALUES (permission, consentId,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING reference_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalPostalAddresses(personalId uuid, isMain boolean, address varchar, additional_info varchar,
                                                      districtName varchar, townName varchar, ibgeTownCode varchar, countrySubdivision varchar,
                                                      postCode varchar, country varchar, countryCode varchar, latitude varchar,
                                                      longitude varchar) RETURNS void AS $$
    INSERT INTO personal_postal_addresses (personal_identifications_id, is_main, address, additional_info, district_name,
                                           town_name, ibge_town_code, country_subdivision, post_code, country,
                                           country_code, latitude, longitude,
                                           created_at, created_by, updated_at, updated_by)
    VALUES (personalId, isMain, address, additional_info, districtName, townName, ibgeTownCode, countrySubdivision,
            postCode, country, countryCode, latitude, longitude,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE');
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalPhones(personalId uuid, isMain boolean, type varchar, additionalInfo varchar, countryCallingCode varchar,
                                             areaCode varchar, number varchar, phoneExtension varchar) RETURNS void AS $$
    INSERT INTO personal_phones(personal_identifications_id, is_main, type, additional_info, country_calling_code,
                                area_code, number, phone_extension,
                                created_at, created_by, updated_at, updated_by)
    VALUES (personalId, isMain, type, additionalInfo, countryCallingCode, areaCode, number, phoneExtension,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE');
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addPersonalEmails(personalId uuid, isMain boolean, email varchar) RETURNS void AS $$
    INSERT INTO personal_emails(personal_identifications_id, is_main, email,
                                created_at, created_by, updated_at, updated_by)
    VALUES (personalId, isMain, email,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE');
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addBusinessIdentifications(docId varchar, brandName varchar, companyName varchar, tradeName varchar,
                                                      incorporationDate date, cnpjNumber varchar) RETURNS uuid AS $$

    INSERT INTO business_identifications (account_holder_id, brand_name, company_name, trade_name,
                                          incorporation_date, cnpj_number,
                                          created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId),brandName, companyName, tradeName, incorporationDate, cnpjNumber,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING business_identifications_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addBusinessIdentificationsCompanyCnpj(businessIdentificationsId uuid, companyCnpj varchar) RETURNS uuid AS $$
    INSERT INTO business_identifications_company_cnpj (business_identifications_id, company_cnpj,
                                                   created_at, created_by, updated_at, updated_by)
    VALUES (businessIdentificationsId, companyCnpj,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING business_identifications_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addBusinessIdentificationsOtherDocument(businessIdentificationsId uuid, type varchar, number varchar,
                                                                   country varchar, expirationDate date) RETURNS uuid AS $$
    INSERT INTO business_other_documents (business_identifications_id, type, number, country, expiration_date,
                                          created_at, created_by, updated_at, updated_by)
    VALUES (businessIdentificationsId, type, number, country, expirationDate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING business_identifications_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addBusinessIdentificationsParties(businessIdentificationsId uuid, personType varchar, type varchar,
                                                             civilName varchar, socialName varchar, companyName varchar,
                                                             tradeName varchar, startDate date, shareholding varchar, documentType varchar,
                                                             documentNumber varchar, documentAdditionalInfo varchar, documentCountry varchar,
                                                             documentExpirationDate date, documentIssueDate date) RETURNS void AS $$
    INSERT INTO business_parties (business_identifications_id, person_type, type, civil_name, social_name, company_name,
                                  trade_name, start_date, shareholding, document_type, document_number,
                                  document_additional_info, document_country, document_expiration_date,
                                  document_issue_date,
                                  created_at, created_by, updated_at, updated_by)
    VALUES (businessIdentificationsId, personType, type, civilName, socialName, companyName, tradeName, startDate,
            shareholding, documentType, documentNumber, documentAdditionalInfo, documentCountry, documentExpirationDate,
            documentIssueDate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addBusinessIdentificationsPostalAddress(businessIdentificationsId uuid, isMain boolean, address varchar,
                                                                   additionalInfo varchar, districtName varchar, townName varchar,
                                                                   ibgeTownCode varchar, countrySubdivision varchar, postCode varchar,
                                                                   country varchar, countryCode varchar, latitude varchar,
                                                                   longitude varchar) RETURNS void AS $$
    INSERT INTO business_postal_addresses (business_identifications_id, is_main, address, additional_info,
                                           district_name, town_name, ibge_town_code, country_subdivision,
                                           post_code, country, country_code, latitude, longitude,
                                           created_at, created_by, updated_at, updated_by)
    VALUES (businessIdentificationsId, isMain, address, additionalInfo, districtName, townName, ibgeTownCode,
            countrySubdivision, postCode, country, countryCode, latitude, longitude,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addBusinessIdentificationsPhone(businessIdentificationsId uuid, isMain boolean, type varchar,
                                                           additionalInfo varchar, countryCallingCode varchar, areaCode varchar,
                                                           number varchar, phoneExtension varchar) RETURNS void AS $$
    INSERT INTO business_phones (business_identifications_id, is_main, type, additional_info, country_calling_code,
                                 area_code, number, phone_extension,
                                 created_at, created_by, updated_at, updated_by)
    VALUES (businessIdentificationsId, isMain, type, additionalInfo, countryCallingCode, areaCode, number, phoneExtension,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addBusinessIdentificationsEmail(businessIdentificationsId uuid, isMain boolean, email varchar) RETURNS void AS $$
    INSERT INTO business_emails (business_identifications_id, is_main, email,
                                 created_at, created_by, updated_at, updated_by)
    VALUES (businessIdentificationsId, isMain, email,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addBusinessQualifications(docId varchar, informedRevenueFrequency varchar, informedRevenueFrequencyAdditionalInformation varchar,
                                                     informedRevenueAmount numeric, informedRevenueCurrency varchar, informedRevenueYear integer,
                                                     informedPatrimonyAmount double precision, informedPatrimonyCurrency varchar,
                                                     informedPatrimonyDate date) RETURNS uuid AS $$
    INSERT INTO business_qualifications (account_holder_id, informed_revenue_frequency,
                                         informed_revenue_frequency_additional_information, informed_revenue_amount,
                                         informed_revenue_currency, informed_revenue_year, informed_patrimony_amount,
                                         informed_patrimony_currency, informed_patrimony_date,
                                         created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId), informedRevenueFrequency, informedRevenueFrequencyAdditionalInformation,
            informedRevenueAmount, informedRevenueCurrency, informedRevenueYear, informedPatrimonyAmount,
            informedPatrimonyCurrency, informedPatrimonyDate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING business_qualifications_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addBusinessQualificationsEconomicActivities(businessQualificationsId uuid, code integer, isMain boolean) RETURNS void AS $$
    INSERT INTO business_qualifications_economic_activities (business_qualifications_id, code, is_main,
                                                             created_at, created_by, updated_at, updated_by)
    VALUES (businessQualificationsId, code, isMain,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addCreditCardAccounts(docId varchar, brandName varchar, companyCnpj varchar, name varchar, productType varchar,
                                                 productAdditionalInfo varchar, creditCardNetwork varchar, networkAdditionalInfo varchar,
                                                 status varchar) RETURNS uuid AS $$
    INSERT INTO credit_card_accounts(brand_name, company_cnpj, name, product_type, product_additional_info,
                                     credit_card_network, network_additional_info, status, account_holder_id,
                                     created_at, created_by, updated_at, updated_by)
    VALUES (brandName, companyCnpj, name, productType, productAdditionalInfo, creditCardNetwork,
            networkAdditionalInfo, status, getAccountHolderId(docId),
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING credit_card_account_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addCreditCardAccountsWithId(docId varchar, creditCardAccountId uuid, brandName varchar, companyCnpj varchar,
                                                       name varchar, productType varchar, productAdditionalInfo varchar,
                                                       creditCardNetwork varchar, networkAdditionalInfo varchar, status varchar) RETURNS uuid AS $$
INSERT INTO credit_card_accounts(brand_name, credit_card_account_id, company_cnpj, name, product_type,
                                 product_additional_info, credit_card_network, network_additional_info, status,
                                 account_holder_id,
                                 created_at, created_by, updated_at, updated_by)
    VALUES (brandName, creditCardAccountId, companyCnpj, name, productType, productAdditionalInfo, creditCardNetwork,
            networkAdditionalInfo, status, getAccountHolderId(docId),
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING credit_card_account_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addCreditCardsAccountPaymentMethod(creditCardAccountId uuid, identificationNumber varchar,
                                                              isMultipleCreditCard boolean) RETURNS void AS $$
    INSERT INTO credit_cards_account_payment_method(credit_card_account_id, identification_number,
                                                    is_multiple_credit_card,
                                                    created_at, created_by, updated_at, updated_by)
    VALUES (creditCardAccountId, identificationNumber, isMultipleCreditCard,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addCreditCardAccountsBills(creditCardAccountId uuid, dueDate date, billTotalAmount double precision,
                                                      billTotalAmountCurrency varchar, billMinimumAmount double precision,
                                                      billMinimumAmountCurrency varchar, isInstalment boolean) RETURNS uuid AS $$
    INSERT INTO credit_card_accounts_bills(credit_card_account_id, due_date, bill_total_amount,
                                           bill_total_amount_currency, bill_minimum_amount,
                                           bill_minimum_amount_currency, is_instalment,
                                           created_at, created_by, updated_at, updated_by)
    VALUES (creditCardAccountId, dueDate, billTotalAmount, billTotalAmountCurrency, billMinimumAmount,
            billMinimumAmountCurrency, isInstalment,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING bill_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addCreditCardAccountsBillsWithId(creditCardAccountId uuid, billId uuid, dueDate date, billTotalAmount double precision,
                                                            billTotalAmountCurrency varchar, billMinimumAmount double precision,
                                                            billMinimumAmountCurrency varchar, isInstalment boolean) RETURNS uuid AS $$
INSERT INTO credit_card_accounts_bills(credit_card_account_id, bill_id, due_date, bill_total_amount,
                                       bill_total_amount_currency, bill_minimum_amount, bill_minimum_amount_currency,
                                       is_instalment,
                                       created_at, created_by, updated_at, updated_by)
    VALUES (creditCardAccountId, billId, dueDate, billTotalAmount, billTotalAmountCurrency, billMinimumAmount,
            billMinimumAmountCurrency, isInstalment,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING bill_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addCreditCardAccountsBillsFinanceCharge(billId uuid, type varchar, additionalInfo varchar, amount double precision,
                                                                   currency varchar) RETURNS void AS $$
    INSERT INTO credit_card_accounts_bills_finance_charge(bill_id, type, additional_info, amount, currency,
                                                          created_at, created_by, updated_at, updated_by)
    VALUES (billId, type, additionalInfo, amount, currency,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addCreditCardAccountsBillsPayment(billId uuid, valueType varchar, paymentDate date, paymentMode varchar,
                                                             amount double precision, currency varchar) RETURNS void AS $$
    INSERT INTO credit_card_accounts_bills_payment(bill_id, value_type, payment_date, payment_mode, amount, currency,
                                                   created_at, created_by, updated_at, updated_by)
    VALUES (billId, valueType, paymentDate, paymentMode, amount, currency,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addCreditCardAccountsLimits(creditCardAccountId uuid, creditLineLimitType varchar, consolidationType varchar,
                                                       identificationNumber varchar, lineName varchar, lineNameAdditionalInfo varchar,
                                                       isLimitFlexible boolean, limitAmountCurrency varchar, limitAmount double precision,
                                                       usedAmountCurrency varchar, usedAmount double precision, availableAmountCurrency varchar,
                                                       availableAmount double precision) RETURNS void AS $$
    INSERT INTO credit_card_accounts_limits(credit_card_account_id, credit_line_limit_type, consolidation_type,
                                            identification_number, line_name, line_name_additional_info,
                                            is_limit_flexible, limit_amount_currency, limit_amount, used_amount_currency,
                                            used_amount, available_amount_currency, available_amount,
                                            created_at, created_by, updated_at, updated_by)
    VALUES (creditCardAccountId, creditLineLimitType, consolidationType, identificationNumber, lineName,
            lineNameAdditionalInfo, isLimitFlexible, limitAmountCurrency, limitAmount, usedAmountCurrency, usedAmount,
            availableAmountCurrency, availableAmount,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addCreditCardAccountsTransaction(creditCardAccountId uuid, billId uuid, identificationNumber varchar,
                                                            lineName varchar, transactionName varchar, creditDebitType varchar,
                                                            transactionType varchar, transactionalAdditionalInfo varchar,
                                                            paymentType varchar, feeType varchar, feeTypeAdditionalInfo varchar,
                                                            otherCreditsType varchar, otherCreditsAdditionalInfo varchar,
                                                            chargeIdentificator varchar, chargeNumber int8, brazilianAmount double precision,
                                                            amount double precision, currency varchar, transactionDate date, transactionDateTime timestamp,
                                                            billPostDate date, payeeMCC int8) RETURNS void AS $$
    INSERT INTO credit_card_accounts_transaction(credit_card_account_id, bill_id, identification_number, line_name,
                                                 transaction_name, credit_debit_type, transaction_type,
                                                 transactional_additional_info, payment_type, fee_type,
                                                 fee_type_additional_info, other_credits_type,
                                                 other_credits_additional_info, charge_identificator, charge_number,
                                                 brazilian_amount, amount, currency, transaction_date, transaction_date_time, bill_post_date,
                                                 payee_mcc,
                                                 created_at, created_by, updated_at, updated_by)
    VALUES (creditCardAccountId, billId, identificationNumber, lineName, transactionName, creditDebitType,
            transactionType, transactionalAdditionalInfo, paymentType, feeType, feeTypeAdditionalInfo, otherCreditsType,
            otherCreditsAdditionalInfo, chargeIdentificator, chargeNumber, brazilianAmount, amount, currency,
            transactionDate, transactionDateTime, billPostDate, payeeMCC,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addCreditCardAccountsTransactionWithId(creditCardAccountId uuid, billId uuid, transactionId uuid,
                                                                  identificationNumber varchar, lineName varchar, transactionName varchar,
                                                                  creditDebitType varchar, transactionType varchar, transactionalAdditionalInfo varchar,
                                                                  paymentType varchar, feeType varchar, feeTypeAdditionalInfo varchar,
                                                                  otherCreditsType varchar, otherCreditsAdditionalInfo varchar,
                                                                  chargeIdentificator varchar, chargeNumber int8, brazilianAmount double precision,
                                                                  amount double precision, currency varchar, transactionDate date, transactionDateTime timestamp, billPostDate date, payeeMCC int8) RETURNS void AS $$
INSERT INTO credit_card_accounts_transaction(credit_card_account_id, bill_id, transaction_id, identification_number,
                                             line_name, transaction_name, credit_debit_type, transaction_type,
                                             transactional_additional_info, payment_type, fee_type,
                                             fee_type_additional_info, other_credits_type, other_credits_additional_info,
                                             charge_identificator, charge_number, brazilian_amount, amount, currency,
                                             transaction_date, transaction_date_time, bill_post_date, payee_mcc,
                                             created_at, created_by, updated_at, updated_by)
    VALUES (creditCardAccountId, billId, transactionId, identificationNumber, lineName, transactionName, creditDebitType,
            transactionType, transactionalAdditionalInfo, paymentType, feeType, feeTypeAdditionalInfo, otherCreditsType,
            otherCreditsAdditionalInfo, chargeIdentificator, chargeNumber, brazilianAmount, amount, currency, transactionDate,
            transactionDateTime, billPostDate, payeeMCC,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addBusinessFinancialRelations(docId varchar, startDate timestamp) RETURNS uuid AS $$
    INSERT INTO business_financial_relations (account_holder_id, start_date,
                                              created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId), startDate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING business_financial_relations_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION getBusinessFinancialRelationsId(docId varchar) RETURNS uuid AS $$
    -- works as this is a 1:1 relation with accountholder
    SELECT business_financial_relations_id FROM business_financial_relations WHERE account_holder_id = getAccountHolderId(docId) LIMIT 1;
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addBusinessFinancialRelationsProcurator(docId varchar, type varchar, cnpj_cpf varchar, civilName varchar,
                                                                   socialName varchar) RETURNS void AS $$
    INSERT INTO business_financial_relations_procurators (business_financial_relations_id, type, cnpj_cpf_number, civil_name,
                                                          social_name,
                                                          created_at, created_by, updated_at, updated_by)
    VALUES (getBusinessFinancialRelationsId(docId), type, cnpj_cpf, civilName, socialName,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addBusinessFinancialRelationsProductServicesType(docId varchar, type varchar) RETURNS void AS $$
    INSERT INTO business_financial_relations_products_services_type (business_financial_relations_id, type,
                                                                     created_at, created_by, updated_at, updated_by)
    VALUES (getBusinessFinancialRelationsId(docId), type,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentBankFixedIncomesWithId(docId varchar, investmentId uuid, brandName varchar, companyCnpj varchar, investmentType varchar, isinCode varchar,
                                                               preFixedRate double precision, postFixedIndexerPercentage double precision, rateType varchar,
                                                               ratePeriodicity varchar, calculation varchar, indexer varchar, indexerAdditionalInfo varchar,
                                                               issueUnitPriceAmount double precision, issueUnitPriceCurrency varchar, dueDate date, issueDate date,
                                                               clearingCode varchar, purchaseDate date,
                                                               gracePeriodDate date, created_at date,
                                                               status varchar) RETURNS void AS
$$
INSERT INTO bank_fixed_incomes(account_holder_id, investment_id, brand_name, company_cnpj, investment_type, isin_code, pre_fixed_rate, post_fixed_indexer_percentage,
                               rate_type, rate_periodicity, calculation, indexer, indexer_additional_info, issue_unit_price_amount, issue_unit_price_currency,
                               due_date, issue_date, clearing_code, purchase_date, grace_period_date, created_at, created_by, updated_at, updated_by, status)
VALUES (getAccountHolderId(docId), investmentId, brandName, companyCnpj, investmentType, isinCode, preFixedRate, postFixedIndexerPercentage, rateType, ratePeriodicity, calculation, indexer,
        indexerAdditionalInfo, issueUnitPriceAmount, issueUnitPriceCurrency, dueDate, issueDate, clearingCode, purchaseDate, gracePeriodDate,
        created_at, 'PREPOPULATE', created_at, 'PREPOPULATE', status)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentBankFixedIncomes(docId varchar, brandName varchar, companyCnpj varchar,
                                                         investmentType varchar, isinCode varchar,
                                                         preFixedRate double precision,
                                                         postFixedIndexerPercentage double precision, rateType varchar,
                                                         ratePeriodicity varchar, calculation varchar, indexer varchar,
                                                         indexerAdditionalInfo varchar,
                                                         issueUnitPriceAmount double precision,
                                                         issueUnitPriceCurrency varchar, dueDate date, issueDate date,
                                                         clearingCode varchar, purchaseDate date, gracePeriodDate date,
                                                         created_at date,
                                                         status varchar) RETURNS void AS
$$
INSERT INTO bank_fixed_incomes(account_holder_id, brand_name, company_cnpj, investment_type, isin_code, pre_fixed_rate,
                               post_fixed_indexer_percentage,
                               rate_type, rate_periodicity, calculation, indexer, indexer_additional_info,
                               issue_unit_price_amount, issue_unit_price_currency,
                               due_date, issue_date, clearing_code, purchase_date, grace_period_date, created_at,
                               created_by, updated_at, updated_by, status)
VALUES (getAccountHolderId(docId), brandName, companyCnpj, investmentType, isinCode, preFixedRate,
        postFixedIndexerPercentage, rateType, ratePeriodicity, calculation, indexer,
        indexerAdditionalInfo, issueUnitPriceAmount, issueUnitPriceCurrency, dueDate, issueDate, clearingCode,
        purchaseDate, gracePeriodDate,
        created_at, 'PREPOPULATE', created_at, 'PREPOPULATE', status)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentBankFixedIncomesBalances(investmentId uuid, referenceDateTime timestamp, updateUnitPrice double precision, updateUnitPriceCurrency varchar,
                                                                grossAmount double precision, grossAmountCurrency varchar, netAmount double precision, netAmountCurrency varchar,
                                                                incomeTaxAmount double precision, incomeTaxCurrency varchar, finalTransactionTaxAmount double precision,
                                                                finalTransactionTaxCurrency varchar, blockedBalance double precision, blockedBalanceCurrency varchar,
                                                                purchaseUnitPrice double precision, purchaseUnitPriceCurrency varchar, quantity double precision,
                                                                preFixedRate double precision, postFixedIndexerPercentage double precision) RETURNS void AS $$
INSERT INTO bank_fixed_incomes_balance(investment_id, reference_date_time, updated_unit_price,
                                       updated_unit_price_currency, gross_amount, gross_amount_currency, net_amount,
                                       net_amount_currency, income_tax_amount, income_tax_currency, financial_transaction_tax_amount,
                                       financial_transaction_tax_currency, blocked_balance, blocked_balance_currency, purchase_unit_price,
                                       purchase_unit_price_currency, quantity, pre_fixed_rate, post_fixed_indexer_percentage)
VALUES (investmentId, referenceDateTime, updateUnitPrice, updateUnitPriceCurrency, netAmount, netAmountCurrency, incomeTaxAmount, incomeTaxCurrency, finalTransactionTaxAmount,
        finalTransactionTaxCurrency, grossAmount, grossAmountCurrency, blockedBalance, blockedBalanceCurrency, purchaseUnitPrice,purchaseUnitPriceCurrency, quantity, preFixedRate, postFixedIndexerPercentage)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentBankFixedIncomesTransactions(investmentId uuid, type varchar, transactionType varchar,
                                                                    transactionTypeAdditionalInfo varchar, transactionDate date, transactionQuantity double precision, transactionUnitPrice double precision, transactionUnitPriceCurrency varchar,
                                                                    transactionGrossValue double precision, transactionGrossValueCurrency varchar, incomeTaxValue double precision,
                                                                    incomeTaxValueCurrency varchar, financialTransactionTaxValue double precision, financialTransactionTaxCurrency varchar,
                                                                     transactionNetValue double precision, transactionNetCurrency varchar, remunerationTransactionRate double precision, indexerPercentage double precision) RETURNS void AS $$
INSERT INTO bank_fixed_incomes_transactions(investment_id, type, transaction_type, transaction_type_additional_info,
                                           transaction_date, transaction_quantity,  transaction_unit_price, transaction_unit_price_currency,
                                            transaction_gross_value, transaction_gross_value_currency,
                                           income_tax_value, income_tax_currency, financial_transaction_tax_value, financial_transaction_tax_currency,
                                           transaction_net_value, transaction_net_currency, remuneration_transaction_rate, indexer_percentage)
VALUES (investmentId, type, transactionType, transactionTypeAdditionalInfo, transactionDate, transactionQuantity, transactionUnitPrice, transactionUnitPriceCurrency, transactionGrossValue, transactionGrossValueCurrency,
        incomeTaxValue, incomeTaxValueCurrency, financialTransactionTaxValue, financialTransactionTaxCurrency, transactionNetValue, transactionNetCurrency, remunerationTransactionRate, indexerPercentage)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentBankFixedIncomesTransactionsWithId(transactionId uuid, investmentId uuid, type varchar, transactionType varchar,
                                                                     transactionTypeAdditionalInfo varchar, transactionDate date, transactionQuantity double precision, transactionUnitPrice double precision, transactionUnitPriceCurrency varchar,
                                                                     transactionGrossValue double precision, transactionGrossValueCurrency varchar, incomeTaxValue double precision,
                                                                     incomeTaxValueCurrency varchar, financialTransactionTaxValue double precision, financialTransactionTaxCurrency varchar,
                                                                     transactionNetValue double precision, transactionNetCurrency varchar, remunerationTransactionRate double precision, indexerPercentage double precision) RETURNS void AS $$
INSERT INTO bank_fixed_incomes_transactions(transaction_id, investment_id, type, transaction_type, transaction_type_additional_info,
                                           transaction_date, transaction_quantity, transaction_unit_price, transaction_unit_price_currency,
                                            transaction_gross_value, transaction_gross_value_currency,
                                           income_tax_value, income_tax_currency, financial_transaction_tax_value, financial_transaction_tax_currency,
                                           transaction_net_value, transaction_net_currency, remuneration_transaction_rate, indexer_percentage)
VALUES (transactionId, investmentId, type, transactionType, transactionTypeAdditionalInfo, transactionDate, transactionQuantity, transactionUnitPrice, transactionUnitPriceCurrency, transactionGrossValue, transactionGrossValueCurrency,
        incomeTaxValue, incomeTaxValueCurrency, financialTransactionTaxValue, financialTransactionTaxCurrency, transactionNetValue, transactionNetCurrency, remunerationTransactionRate, indexerPercentage)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentCreditFixedIncomesWithId(docId varchar, investmentId uuid, brandName varchar, companyCnpj varchar, investmentType varchar, isinCode varchar,
                                                               preFixedRate double precision, postFixedIndexerPercentage double precision, rateType varchar,
                                                               ratePeriodicity varchar, calculation varchar, indexer varchar, indexerAdditionalInfo varchar,
                                                               issueUnitPriceAmount double precision, issueUnitPriceCurrency varchar, dueDate date, issueDate date,
                                                                 clearingCode varchar, purchaseDate date,
                                                                 gracePeriodDate date, created_at date, status varchar,
                                                                 tax_exempt_product varchar,
                                                                 voucher_payment_indicator varchar,
                                                                 voucher_payment_periodicity varchar) RETURNS void AS
$$
INSERT INTO credit_fixed_incomes(account_holder_id, investment_id, brand_name, company_cnpj, investment_type, isin_code, pre_fixed_rate, post_fixed_indexer_percentage,
                               rate_type, rate_periodicity, calculation, indexer, indexer_additional_info, issue_unit_price_amount, issue_unit_price_currency,
                                 due_date, issue_date, clearing_code, purchase_date, grace_period_date, created_at,
                                 created_by, updated_at, updated_by, status, tax_exempt_product,
                                 voucher_payment_indicator, voucher_payment_periodicity)
VALUES (getAccountHolderId(docId), investmentId, brandName, companyCnpj, investmentType, isinCode, preFixedRate, postFixedIndexerPercentage, rateType, ratePeriodicity, calculation, indexer,
        indexerAdditionalInfo, issueUnitPriceAmount, issueUnitPriceCurrency, dueDate, issueDate, clearingCode, purchaseDate, gracePeriodDate,
        created_at, 'PREPOPULATE', created_at, 'PREPOPULATE', status, tax_exempt_product, voucher_payment_indicator,
        voucher_payment_periodicity)
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION addInvestmentCreditFixedIncomes(docId varchar, brandName varchar, companyCnpj varchar,
                                                           investmentType varchar, isinCode varchar,
                                                           preFixedRate double precision,
                                                           postFixedIndexerPercentage double precision,
                                                           rateType varchar,
                                                           ratePeriodicity varchar, calculation varchar,
                                                           indexer varchar, indexerAdditionalInfo varchar,
                                                           issueUnitPriceAmount double precision,
                                                           issueUnitPriceCurrency varchar, dueDate date, issueDate date,
                                                           clearingCode varchar, purchaseDate date,
                                                           gracePeriodDate date, created_at date, status varchar,
                                                           tax_exempt_product varchar,
                                                           voucher_payment_indicator varchar,
                                                           voucher_payment_periodicity varchar) RETURNS void AS
$$
INSERT INTO credit_fixed_incomes(account_holder_id, brand_name, company_cnpj, investment_type, isin_code,
                                 pre_fixed_rate, post_fixed_indexer_percentage,
                                 rate_type, rate_periodicity, calculation, indexer, indexer_additional_info,
                                 issue_unit_price_amount, issue_unit_price_currency,
                                 due_date, issue_date, clearing_code, purchase_date, grace_period_date, created_at,
                                 created_by, updated_at, updated_by, status, tax_exempt_product,
                                 voucher_payment_indicator, voucher_payment_periodicity)
VALUES (getAccountHolderId(docId), brandName, companyCnpj, investmentType, isinCode, preFixedRate,
        postFixedIndexerPercentage, rateType, ratePeriodicity, calculation, indexer,
        indexerAdditionalInfo, issueUnitPriceAmount, issueUnitPriceCurrency, dueDate, issueDate, clearingCode,
        purchaseDate, gracePeriodDate,
        created_at, 'PREPOPULATE', created_at, 'PREPOPULATE', status, tax_exempt_product, voucher_payment_indicator,
        voucher_payment_periodicity)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentCreditFixedIncomesBalances(investmentId uuid, referenceDateTime timestamp, updateUnitPrice double precision, updateUnitPriceCurrency varchar,
                                                                grossAmount double precision, grossAmountCurrency varchar, netAmount double precision, netAmountCurrency varchar,
                                                                incomeTaxAmount double precision, incomeTaxCurrency varchar, finalTransactionTaxAmount double precision,
                                                                finalTransactionTaxCurrency varchar, blockedBalance double precision, blockedBalanceCurrency varchar,
                                                                purchaseUnitPrice double precision, purchaseUnitPriceCurrency varchar, quantity double precision,
                                                                preFixedRate double precision, postFixedIndexerPercentage double precision) RETURNS void AS $$
INSERT INTO credit_fixed_incomes_balance(investment_id, reference_date_time, updated_unit_price,
                                       updated_unit_price_currency, gross_amount, gross_amount_currency, net_amount,
                                       net_amount_currency, income_tax_amount, income_tax_currency, financial_transaction_tax_amount,
                                       financial_transaction_tax_currency, blocked_balance, blocked_balance_currency, purchase_unit_price,
                                       purchase_unit_price_currency, quantity, pre_fixed_rate, post_fixed_indexer_percentage)
VALUES (investmentId, referenceDateTime, updateUnitPrice, updateUnitPriceCurrency, netAmount, netAmountCurrency, incomeTaxAmount, incomeTaxCurrency, finalTransactionTaxAmount,
        finalTransactionTaxCurrency, grossAmount, grossAmountCurrency, blockedBalance, blockedBalanceCurrency, purchaseUnitPrice,purchaseUnitPriceCurrency, quantity, preFixedRate, postFixedIndexerPercentage)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentCreditFixedIncomesTransactions(investmentId uuid, type varchar, transactionType varchar,
                                                                    transactionTypeAdditionalInfo varchar, transactionDate date, transactionQuantity double precision, transactionUnitPrice double precision, transactionUnitPriceCurrency varchar,
                                                                    transactionGrossValue double precision, transactionGrossValueCurrency varchar, incomeTaxValue double precision,
                                                                    incomeTaxValueCurrency varchar, financialTransactionTaxValue double precision, financialTransactionTaxCurrency varchar,
                                                                     transactionNetValue double precision, transactionNetCurrency varchar, remunerationTransactionRate double precision, indexerPercentage double precision) RETURNS void AS $$
INSERT INTO credit_fixed_incomes_transactions(investment_id, type, transaction_type, transaction_type_additional_info,
                                           transaction_date, transaction_quantity,  transaction_unit_price, transaction_unit_price_currency,
                                            transaction_gross_value, transaction_gross_value_currency,
                                           income_tax_value, income_tax_currency, financial_transaction_tax_value, financial_transaction_tax_currency,
                                           transaction_net_value, transaction_net_currency, remuneration_transaction_rate, indexer_percentage)
VALUES (investmentId, type, transactionType, transactionTypeAdditionalInfo, transactionDate, transactionQuantity, transactionUnitPrice, transactionUnitPriceCurrency, transactionGrossValue, transactionGrossValueCurrency,
        incomeTaxValue, incomeTaxValueCurrency, financialTransactionTaxValue, financialTransactionTaxCurrency, transactionNetValue, transactionNetCurrency, remunerationTransactionRate, indexerPercentage)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentCreditFixedIncomesTransactionsWithId(transactionId uuid, investmentId uuid, type varchar, transactionType varchar,
                                                                     transactionTypeAdditionalInfo varchar, transactionDate date, transactionQuantity double precision, transactionUnitPrice double precision, transactionUnitPriceCurrency varchar,
                                                                     transactionGrossValue double precision, transactionGrossValueCurrency varchar, incomeTaxValue double precision,
                                                                     incomeTaxValueCurrency varchar, financialTransactionTaxValue double precision, financialTransactionTaxCurrency varchar,
                                                                     transactionNetValue double precision, transactionNetCurrency varchar, remunerationTransactionRate double precision, indexerPercentage double precision) RETURNS void AS $$
INSERT INTO credit_fixed_incomes_transactions(transaction_id, investment_id, type, transaction_type, transaction_type_additional_info,
                                           transaction_date, transaction_quantity, transaction_unit_price, transaction_unit_price_currency,
                                            transaction_gross_value, transaction_gross_value_currency,
                                           income_tax_value, income_tax_currency, financial_transaction_tax_value, financial_transaction_tax_currency,
                                           transaction_net_value, transaction_net_currency, remuneration_transaction_rate, indexer_percentage)
VALUES (transactionId, investmentId, type, transactionType, transactionTypeAdditionalInfo, transactionDate, transactionQuantity, transactionUnitPrice, transactionUnitPriceCurrency, transactionGrossValue, transactionGrossValueCurrency,
        incomeTaxValue, incomeTaxValueCurrency, financialTransactionTaxValue, financialTransactionTaxCurrency, transactionNetValue, transactionNetCurrency, remunerationTransactionRate, indexerPercentage)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentFundsWithId(docId varchar, investmentId uuid, brandName varchar, companyCnpj varchar, anbimaCategory varchar, anbimaClass varchar,
                                                    anbimaSubclass varchar, name varchar, isinCode varchar,
                                                    created_at date, status varchar) RETURNS void AS
$$
INSERT INTO funds(account_holder_id, investment_id, brand_name, company_cnpj, anbima_category, anbima_class, anbima_subclass, name, isin_code, created_at, created_by, updated_at, updated_by, status)
VALUES (getAccountHolderId(docId), investmentId, brandName, companyCnpj, anbimaCategory, anbimaClass, anbimaSubclass, name, isinCode,
        created_at, 'PREPOPULATE', created_at, 'PREPOPULATE', status)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentFunds(docId varchar, brandName varchar, companyCnpj varchar,
                                              anbimaCategory varchar, anbimaClass varchar,
                                              anbimaSubclass varchar, name varchar, isinCode varchar, created_at date,
                                              status varchar) RETURNS void AS
$$
INSERT INTO funds(account_holder_id, brand_name, company_cnpj, anbima_category, anbima_class, anbima_subclass, name,
                  isin_code, created_at, created_by, updated_at, updated_by, status)
VALUES (getAccountHolderId(docId), brandName, companyCnpj, anbimaCategory, anbimaClass, anbimaSubclass, name, isinCode,
        created_at, 'PREPOPULATE', created_at, 'PREPOPULATE', status)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentFundsBalances(investmentId uuid, referenceDate date, quotaQuantity double precision,
                                                                grossAmount double precision, grossAmountCurrency varchar, netAmount double precision, netAmountCurrency varchar,
                                                                incomeTaxProvisionAmount double precision, incomeTaxProvisionCurrency varchar, financialTransactionTaxProvisionAmount double precision,
                                                                financialTransactionTaxProvisionCurrency varchar, blockedBalance double precision, blockedBalanceCurrency varchar,
                                                                quotaGrossPriceValueAmount double precision, quotaGrossPriceValueCurrency varchar) RETURNS void AS $$
INSERT INTO funds_balance(investment_id, reference_date, quota_quantity,
                                       gross_amount, gross_amount_currency, net_amount,
                                       net_amount_currency, income_tax_provision_amount, income_tax_provision_currency, financial_transaction_tax_provision_amount,
                                       financial_transaction_tax_provision_currency, blocked_amount, blocked_amount_currency, quota_gross_price_value_amount,
                                       quota_gross_price_value_amount_currency)
VALUES (investmentId, referenceDate, quotaQuantity, grossAmount, grossAmountCurrency, netAmount, netAmountCurrency, incomeTaxProvisionAmount, incomeTaxProvisionCurrency, financialTransactionTaxProvisionAmount,
        financialTransactionTaxProvisionCurrency, blockedBalance, blockedBalanceCurrency, quotaGrossPriceValueAmount,quotaGrossPriceValueCurrency)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentFundsTransactions(investmentId uuid, type varchar, transactionType varchar,
                                                                    transactionTypeAdditionalInfo varchar, transactionConversionDate date, transactionQuotaQuantity double precision, transactionQuotaPrice double precision, transactionQuotaPriceCurrency varchar,
                                                                    transactionValue double precision, transactionValueCurrency varchar, transactionGrossValue double precision, transactionGrossValueCurrency varchar, incomeTaxValue double precision,
                                                                    incomeTaxCurrency varchar, financialTransactionTaxValue double precision, financialTransactionTaxCurrency varchar,
                                                                     transactionNetValue double precision, transactionNetCurrency varchar, transactionExitFeeAmount double precision, transactionExitFeeCurrency varchar) RETURNS void AS $$
INSERT INTO funds_transactions(investment_id, type, transaction_type, transaction_type_additional_info,
                                           transaction_conversion_date, transaction_quota_quantity,  transaction_quota_price, transaction_quota_price_currency, transaction_value, transaction_value_currency,
                                            transaction_gross_value, transaction_gross_value_currency,
                                           income_tax_value, income_tax_currency, financial_transaction_tax_value, financial_transaction_tax_currency,
                                           transaction_net_value, transaction_net_currency, transaction_exit_fee_amount, transaction_exit_fee_currency)
VALUES (investmentId, type, transactionType, transactionTypeAdditionalInfo, transactionConversionDate, transactionQuotaQuantity, transactionQuotaPrice, transactionQuotaPriceCurrency, transactionValue, transactionValueCurrency, transactionGrossValue, transactionGrossValueCurrency,
        incomeTaxValue, incomeTaxCurrency, financialTransactionTaxValue, financialTransactionTaxCurrency, transactionNetValue, transactionNetCurrency, transactionExitFeeAmount, transactionExitFeeCurrency)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentFundsTransactionsWithId(transactionId uuid, investmentId uuid, type varchar, transactionType varchar,
                                                                    transactionTypeAdditionalInfo varchar, transactionConversionDate date, transactionQuotaQuantity double precision, transactionQuotaPrice double precision, transactionQuotaPriceCurrency varchar,
                                                                    transactionValue double precision, transactionValueCurrency varchar, transactionGrossValue double precision, transactionGrossValueCurrency varchar, incomeTaxValue double precision,
                                                                    incomeTaxCurrency varchar, financialTransactionTaxValue double precision, financialTransactionTaxCurrency varchar,
                                                                     transactionNetValue double precision, transactionNetCurrency varchar, transactionExitFeeAmount double precision, transactionExitFeeCurrency varchar) RETURNS void AS $$
INSERT INTO funds_transactions(transaction_id, investment_id, type, transaction_type, transaction_type_additional_info,
                                           transaction_conversion_date, transaction_quota_quantity,  transaction_quota_price, transaction_quota_price_currency, transaction_value, transaction_value_currency,
                                            transaction_gross_value, transaction_gross_value_currency,
                                           income_tax_value, income_tax_currency, financial_transaction_tax_value, financial_transaction_tax_currency,
                                           transaction_net_value, transaction_net_currency, transaction_exit_fee_amount, transaction_exit_fee_currency)
VALUES (transactionId, investmentId, type, transactionType, transactionTypeAdditionalInfo, transactionConversionDate, transactionQuotaQuantity, transactionQuotaPrice, transactionQuotaPriceCurrency, transactionValue, transactionValueCurrency, transactionGrossValue, transactionGrossValueCurrency,
        incomeTaxValue, incomeTaxCurrency, financialTransactionTaxValue, financialTransactionTaxCurrency, transactionNetValue, transactionNetCurrency, transactionExitFeeAmount, transactionExitFeeCurrency)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentTreasureTitlesWithId(docId varchar, investmentId uuid, brandName varchar, productName varchar, companyCnpj varchar, voucherPaymentIndicator varchar, voucherPaymentPeriodicity varchar,
                                                               voucherPaymentPeriodicityAdditionalInfo varchar, isinCode varchar,
                                                               preFixedRate double precision, postFixedIndexerPercentage double precision,
                                                               ratePeriodicity varchar, calculation varchar, indexer varchar, indexerAdditionalInfo varchar,
                                                             dueDate date, purchaseDate date, created_at date,
                                                             status varchar) RETURNS void AS
$$
INSERT INTO treasure_titles(account_holder_id, investment_id, brand_name, product_name, company_cnpj, voucher_payment_indicator, voucher_payment_periodicity, voucher_payment_periodicity_additional_info, isin_code, pre_fixed_rate, post_fixed_indexer_percentage,
                               rate_periodicity, calculation, indexer, indexer_additional_info,
                               due_date, purchase_date, created_at, created_by, updated_at, updated_by, status)
VALUES (getAccountHolderId(docId), investmentId, brandName, productName, companyCnpj, voucherPaymentIndicator, voucherPaymentPeriodicity, voucherPaymentPeriodicityAdditionalInfo, isinCode, preFixedRate, postFixedIndexerPercentage, ratePeriodicity, calculation, indexer,
        indexerAdditionalInfo, dueDate, purchaseDate,
        created_at, 'PREPOPULATE', created_at, 'PREPOPULATE', status)
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION addInvestmentTreasureTitles(docId varchar, brandName varchar, productName varchar,
                                                       companyCnpj varchar, voucherPaymentIndicator varchar,
                                                       voucherPaymentPeriodicity varchar,
                                                       voucherPaymentPeriodicityAdditionalInfo varchar,
                                                       isinCode varchar,
                                                       preFixedRate double precision,
                                                       postFixedIndexerPercentage double precision,
                                                       ratePeriodicity varchar, calculation varchar, indexer varchar,
                                                       indexerAdditionalInfo varchar,
                                                       dueDate date, purchaseDate date, created_at date,
                                                       status varchar) RETURNS void AS
$$
INSERT INTO treasure_titles(account_holder_id, brand_name, product_name, company_cnpj, voucher_payment_indicator,
                            voucher_payment_periodicity, voucher_payment_periodicity_additional_info, isin_code,
                            pre_fixed_rate, post_fixed_indexer_percentage,
                            rate_periodicity, calculation, indexer, indexer_additional_info,
                            due_date, purchase_date, created_at, created_by, updated_at, updated_by, status)
VALUES (getAccountHolderId(docId), brandName, productName, companyCnpj, voucherPaymentIndicator,
        voucherPaymentPeriodicity, voucherPaymentPeriodicityAdditionalInfo, isinCode, preFixedRate,
        postFixedIndexerPercentage, ratePeriodicity, calculation, indexer,
        indexerAdditionalInfo, dueDate, purchaseDate,
        created_at, 'PREPOPULATE', created_at, 'PREPOPULATE', status)
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION addInvestmentTreasureTitlesBalances(investmentId uuid, referenceDateTime timestamp, updateUnitPrice double precision, updateUnitPriceCurrency varchar,
                                                                grossAmount double precision, grossAmountCurrency varchar, netAmount double precision, netAmountCurrency varchar,
                                                                incomeTaxAmount double precision, incomeTaxCurrency varchar, finalTransactionTaxAmount double precision,
                                                                finalTransactionTaxCurrency varchar, blockedBalance double precision, blockedBalanceCurrency varchar,
                                                                purchaseUnitPrice double precision, purchaseUnitPriceCurrency varchar, quantity double precision) RETURNS void AS $$
INSERT INTO treasure_titles_balance(investment_id, reference_date_time, updated_unit_price,
                                       updated_unit_price_currency, gross_amount, gross_amount_currency, net_amount,
                                       net_amount_currency, income_tax_amount, income_tax_currency, financial_transaction_tax_amount,
                                       financial_transaction_tax_currency, blocked_balance, blocked_balance_currency, purchase_unit_price,
                                       purchase_unit_price_currency, quantity)
VALUES (investmentId, referenceDateTime, updateUnitPrice, updateUnitPriceCurrency, netAmount, netAmountCurrency, incomeTaxAmount, incomeTaxCurrency, finalTransactionTaxAmount,
        finalTransactionTaxCurrency, grossAmount, grossAmountCurrency, blockedBalance, blockedBalanceCurrency, purchaseUnitPrice,purchaseUnitPriceCurrency, quantity)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentTreasureTitlesTransactions(investmentId uuid, type varchar, transactionType varchar,
                                                                    transactionTypeAdditionalInfo varchar, transactionDate date, transactionQuantity double precision, transactionUnitPrice double precision, transactionUnitPriceCurrency varchar,
                                                                    transactionGrossValue double precision, transactionGrossValueCurrency varchar, incomeTaxValue double precision,
                                                                    incomeTaxValueCurrency varchar, financialTransactionTaxValue double precision, financialTransactionTaxCurrency varchar,
                                                                     transactionNetValue double precision, transactionNetCurrency varchar, remunerationTransactionRate double precision) RETURNS void AS $$
INSERT INTO treasure_titles_transactions(investment_id, type, transaction_type, transaction_type_additional_info,
                                           transaction_date, transaction_quantity,  transaction_unit_price, transaction_unit_price_currency,
                                            transaction_gross_value, transaction_gross_value_currency,
                                           income_tax_value, income_tax_currency, financial_transaction_tax_value, financial_transaction_tax_currency,
                                           transaction_net_value, transaction_net_currency, remuneration_transaction_rate)
VALUES (investmentId, type, transactionType, transactionTypeAdditionalInfo, transactionDate, transactionQuantity, transactionUnitPrice, transactionUnitPriceCurrency, transactionGrossValue, transactionGrossValueCurrency,
        incomeTaxValue, incomeTaxValueCurrency, financialTransactionTaxValue, financialTransactionTaxCurrency, transactionNetValue, transactionNetCurrency, remunerationTransactionRate)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentTreasureTitlesTransactionsWithId(transactionId uuid, investmentId uuid, type varchar, transactionType varchar,
                                                                    transactionTypeAdditionalInfo varchar, transactionDate date, transactionQuantity double precision, transactionUnitPrice double precision, transactionUnitPriceCurrency varchar,
                                                                    transactionGrossValue double precision, transactionGrossValueCurrency varchar, incomeTaxValue double precision,
                                                                    incomeTaxValueCurrency varchar, financialTransactionTaxValue double precision, financialTransactionTaxCurrency varchar,
                                                                     transactionNetValue double precision, transactionNetCurrency varchar, remunerationTransactionRate double precision) RETURNS void AS $$
INSERT INTO treasure_titles_transactions(transaction_id, investment_id, type, transaction_type, transaction_type_additional_info,
                                           transaction_date, transaction_quantity,  transaction_unit_price, transaction_unit_price_currency,
                                            transaction_gross_value, transaction_gross_value_currency,
                                           income_tax_value, income_tax_currency, financial_transaction_tax_value, financial_transaction_tax_currency,
                                           transaction_net_value, transaction_net_currency, remuneration_transaction_rate)
VALUES (transactionId, investmentId, type, transactionType, transactionTypeAdditionalInfo, transactionDate, transactionQuantity, transactionUnitPrice, transactionUnitPriceCurrency, transactionGrossValue, transactionGrossValueCurrency,
        incomeTaxValue, incomeTaxValueCurrency, financialTransactionTaxValue, financialTransactionTaxCurrency, transactionNetValue, transactionNetCurrency, remunerationTransactionRate)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentVariableIncomesWithId(docId varchar, investmentId uuid, brandName varchar,
                                                              companyCnpj varchar, ticker varchar, isinCode varchar,
                                                              created_at date, status varchar) RETURNS void AS
$$
INSERT INTO variable_incomes(account_holder_id, investment_id, brand_name, company_cnpj, ticker, isin_code,created_at, created_by, updated_at, updated_by, status)
VALUES (getAccountHolderId(docId), investmentId, brandName, companyCnpj, ticker, isinCode,
        created_at, 'PREPOPULATE', created_at, 'PREPOPULATE', status)
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION addInvestmentVariableIncomes(docId varchar, brandName varchar, companyCnpj varchar,
                                                        ticker varchar, isinCode varchar, created_at date,
                                                        status varchar) RETURNS void AS
$$
INSERT INTO variable_incomes(account_holder_id, brand_name, company_cnpj, ticker, isin_code, created_at, created_by,
                             updated_at, updated_by, status)
VALUES (getAccountHolderId(docId), brandName, companyCnpj, ticker, isinCode,
        created_at, 'PREPOPULATE', created_at, 'PREPOPULATE', status)
$$ LANGUAGE SQL;


CREATE OR REPLACE FUNCTION addInvestmentVariableIncomesBalances(investmentId uuid, referenceDate date, priceFactor double precision,
                                                                grossAmount double precision, grossAmountCurrency varchar, blockedBalance double precision, blockedBalanceCurrency varchar,
                                                                closingPrice double precision, closingPriceCurrency varchar, quantity double precision) RETURNS void AS $$
INSERT INTO variable_incomes_balance(investment_id, reference_date, price_factor,
                                       gross_amount, gross_amount_currency, blocked_balance, blocked_balance_currency, closing_price,
                                       closing_price_currency, quantity)
VALUES (investmentId, referenceDate, priceFactor, grossAmount, grossAmountCurrency, blockedBalance, blockedBalanceCurrency, closingPrice,closingPriceCurrency, quantity)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentVariableIncomesTransactions(investmentId uuid, brokerNoteId uuid, type varchar, transactionType varchar,
                                                                    transactionTypeAdditionalInfo varchar, transactionDate date, priceFactor double precision, transactionQuantity double precision, transactionUnitPrice double precision, transactionUnitPriceCurrency varchar,
                                                                    transactionValue double precision, transactionValueCurrency varchar) RETURNS void AS $$
INSERT INTO variable_incomes_transactions(investment_id, broker_note_id, type, transaction_type, transaction_type_additional_info,
                                           transaction_date, price_factor, transaction_quantity,  transaction_unit_price, transaction_unit_price_currency,
                                            transaction_value, transaction_value_currency)
VALUES (investmentId, brokerNoteId, type, transactionType, transactionTypeAdditionalInfo, transactionDate, priceFactor, transactionQuantity, transactionUnitPrice, transactionUnitPriceCurrency, transactionValue, transactionValueCurrency)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentVariableIncomesTransactionsWithId(transactionId uuid, investmentId uuid, brokerNoteId uuid, type varchar, transactionType varchar,
                                                                    transactionTypeAdditionalInfo varchar, transactionDate date, priceFactor double precision, transactionQuantity double precision, transactionUnitPrice double precision, transactionUnitPriceCurrency varchar,
                                                                    transactionValue double precision, transactionValueCurrency varchar) RETURNS void AS $$
INSERT INTO variable_incomes_transactions(transaction_id, investment_id, broker_note_id, type, transaction_type, transaction_type_additional_info,
                                           transaction_date, price_factor, transaction_quantity,  transaction_unit_price, transaction_unit_price_currency,
                                            transaction_value, transaction_value_currency)
VALUES (transactionId, investmentId, brokerNoteId, type, transactionType, transactionTypeAdditionalInfo, transactionDate, priceFactor, transactionQuantity, transactionUnitPrice, transactionUnitPriceCurrency, transactionValue, transactionValueCurrency)
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addInvestmentVariableIncomesBrokerNotesWithId(brokerNoteId uuid, brokerNoteNumber varchar, grossValueAmount double precision, grossValueCurrency varchar,
                                                                    brokerageFeeAmount double precision, brokerageFeeCurrency varchar,
                                                                    clearingSettlementFeeAmount double precision, clearingSettlementFeeCurrency varchar,
                                                                    clearingRegistrationFeeAmount double precision, clearingRegistrationFeeCurrency varchar,
                                                                    stockExchangeAssetTradeNoticeFeeAmount double precision, stockExchangeAssetTradeNoticeFeeCurrency varchar,
                                                                    stockExchangeFeeAmount double precision, stockExchangeFeeCurrency varchar,
                                                                    clearingCustodyFeeAmount double precision, clearingCustodyFeeCurrency varchar,
                                                                    taxesAmount double precision, taxesCurrency varchar,
                                                                    incomeTaxAmount double precision, incomeTaxCurrency varchar,
                                                                    netValueAmount double precision, netValueCurrency varchar) RETURNS void AS $$
INSERT INTO variable_incomes_broker_notes(broker_note_id, broker_note_number, gross_value_amount, gross_value_currency,
                                           brokerage_fee_amount, brokerage_fee_currency,
                                           clearing_settlement_fee_amount, clearing_settlement_fee_currency,
                                           clearing_registration_fee_amount, clearing_registration_fee_currency,
                                           stock_exchange_asset_trade_notice_fee_amount, stock_exchange_asset_trade_notice_fee_currency,
                                           stock_exchange_fee_amount, stock_exchange_fee_currency,
                                           clearing_custody_fee_amount, clearing_custody_fee_currency,
                                           taxes_amount, taxes_currency,
                                           income_tax_amount, income_tax_currency,
                                           net_value_amount, net_value_currency)
VALUES (brokerNoteId, brokerNoteNumber, grossValueAmount, grossValueCurrency,
                                            brokerageFeeAmount, brokerageFeeCurrency,
                                            clearingSettlementFeeAmount, clearingSettlementFeeCurrency,
                                            clearingRegistrationFeeAmount, clearingRegistrationFeeCurrency,
                                            stockExchangeAssetTradeNoticeFeeAmount, stockExchangeAssetTradeNoticeFeeCurrency,
                                            stockExchangeFeeAmount, stockExchangeFeeCurrency,
                                            clearingCustodyFeeAmount, clearingCustodyFeeCurrency,
                                            taxesAmount, taxesCurrency,
                                            incomeTaxAmount, incomeTaxCurrency,
                                            netValueAmount, netValueCurrency)
$$ LANGUAGE SQL;
DROP FUNCTION IF EXISTS addExchangesOperationsWithId;

CREATE OR REPLACE FUNCTION addExchangesOperationsWithId(docId varchar, operationId uuid, companyCnpj varchar, brandName varchar, intermediaryInstitutionCnpjNumber varchar,
                                                        intermediaryInstitutionName varchar, operationNumber varchar, operationType varchar, operationDate date, dueDate date, localCurrencyOperationTaxAmount double precision,
                                                        localCurrencyOperationTaxCurrency varchar, localCurrencyOperationValueAmount double precision, localCurrencyOperationValueCurrency varchar,
                                                        foreignOperationValueAmount double precision, foreignOperationValueCurrency varchar, operationOutstandingBalanceAmount double precision,
                                                        operationOutstandingBalanceCurrency varchar, vetAmountAmount double precision, vetAmountCurrency varchar, localCurrencyAdvancePercentage double precision,
                                                        deliveryForeignCurrency varchar, operationCategoryCode varchar, createdAt date, updatedAt date) RETURNS void AS $$

    INSERT INTO exchanges_operation(account_holder_id, operation_id, company_cnpj, brand_name,
                                    intermediary_institution_cnpj_number, intermediary_institution_name,
                                    operation_number, operation_type, operation_date, due_date, local_currency_operation_tax_amount,
                                    local_currency_operation_tax_currency, local_currency_operation_value_amount, local_currency_operation_value_currency,
                                    foreign_operation_value_amount, foreign_operation_value_currency, operation_outstanding_balance_amount,
                                    operation_outstanding_balance_currency, vet_amount_amount, vet_amount_currency, local_currency_advance_percentage,
                                    delivery_foreign_currency, operation_category_code, created_at, created_by,
                                    updated_at, updated_by, status)
    VALUES (getAccountHolderId(docId), operationId, companyCnpj, brandName, intermediaryInstitutionCnpjNumber, intermediaryInstitutionName,
            operationNumber, operationType,  operationDate, dueDate, localCurrencyOperationTaxAmount,
            localCurrencyOperationTaxCurrency, localCurrencyOperationValueAmount, localCurrencyOperationValueCurrency,
            foreignOperationValueAmount, foreignOperationValueCurrency, operationOutstandingBalanceAmount,
            operationOutstandingBalanceCurrency, vetAmountAmount, vetAmountCurrency, localCurrencyAdvancePercentage,
            deliveryForeignCurrency, operationCategoryCode, createdAt, 'PREPOPULATE',
            updatedAt, 'PREPOPULATE', 'AVAILABLE')
$$ LANGUAGE SQL;

DROP FUNCTION IF EXISTS addExchangesOperations;

CREATE OR REPLACE FUNCTION addExchangesOperations(docId varchar, companyCnpj varchar, brandName varchar, intermediaryInstitutionCnpjNumber varchar,
                                                        intermediaryInstitutionName varchar, operationNumber varchar, operationType varchar, operationDate date, dueDate date, localCurrencyOperationTaxAmount double precision,
                                                        localCurrencyOperationTaxCurrency varchar, localCurrencyOperationValueAmount double precision, localCurrencyOperationValueCurrency varchar,
                                                        foreignOperationValueAmount double precision, foreignOperationValueCurrency varchar, operationOutstandingBalanceAmount double precision,
                                                        operationOutstandingBalanceCurrency varchar, vetAmountAmount double precision, vetAmountCurrency varchar, localCurrencyAdvancePercentage double precision,
                                                        deliveryForeignCurrency varchar, operationCategoryCode varchar, createdAt date, updatedAt date) RETURNS uuid AS $$

INSERT INTO exchanges_operation(account_holder_id, company_cnpj, brand_name,
                                intermediary_institution_cnpj_number, intermediary_institution_name,
                                operation_number, operation_type, operation_date, due_date, local_currency_operation_tax_amount,
                                local_currency_operation_tax_currency, local_currency_operation_value_amount, local_currency_operation_value_currency,
                                foreign_operation_value_amount, foreign_operation_value_currency, operation_outstanding_balance_amount,
                                operation_outstanding_balance_currency, vet_amount_amount, vet_amount_currency, local_currency_advance_percentage,
                                delivery_foreign_currency, operation_category_code, created_at, created_by,
                                updated_at, updated_by, status)
VALUES (getAccountHolderId(docId), companyCnpj, brandName, intermediaryInstitutionCnpjNumber, intermediaryInstitutionName,
        operationNumber, operationType,  operationDate, dueDate, localCurrencyOperationTaxAmount,
        localCurrencyOperationTaxCurrency, localCurrencyOperationValueAmount, localCurrencyOperationValueCurrency,
        foreignOperationValueAmount, foreignOperationValueCurrency, operationOutstandingBalanceAmount,
        operationOutstandingBalanceCurrency, vetAmountAmount, vetAmountCurrency, localCurrencyAdvancePercentage,
        deliveryForeignCurrency, operationCategoryCode, createdAt, 'PREPOPULATE',
        updatedAt, 'PREPOPULATE', 'AVAILABLE')
RETURNING operation_id
$$ LANGUAGE SQL;

DROP FUNCTION IF EXISTS addExchangesOperationsEventWithId;
CREATE OR REPLACE FUNCTION addExchangesOperationsEventWithId(operationId uuid, eventId uuid, eventSequenceNumbe varchar, eventType varchar, eventDate timestamp, dueDate date, localCurrencyOperationTaxAmount double precision,
                                                        localCurrencyOperationTaxCurrency varchar, localCurrencyOperationValueAmount double precision, localCurrencyOperationValueCurrency varchar,
                                                        foreignOperationValueAmount double precision, foreignOperationValueCurrency varchar, operationOutstandingBalanceAmount double precision,
                                                        operationOutstandingBalanceCurrency varchar, vetAmountAmount double precision, vetAmountCurrency varchar, localCurrencyAdvancePercentage double precision,
                                                        deliveryForeignCurrency varchar, operationCategoryCode varchar, relationshipCode varchar, foreignPartieName varchar, foreignPartieCountryCode varchar,
                                                        createdAt date, updatedAt date) RETURNS void AS $$

INSERT INTO exchanges_operation_event(operation_id, event_id, event_sequence_number, event_type,
                                event_date, due_date, local_currency_operation_tax_amount,
                                local_currency_operation_tax_currency, local_currency_operation_value_amount, local_currency_operation_value_currency,
                                foreign_operation_value_amount, foreign_operation_value_currency, operation_outstanding_balance_amount,
                                operation_outstanding_balance_currency, vet_amount_amount, vet_amount_currency, local_currency_advance_percentage,
                                delivery_foreign_currency, operation_category_code, relationship_code, foreign_partie_name,foreign_partie_country_code,
                                created_at, created_by, updated_at, updated_by)
VALUES (operationId, eventId, eventSequenceNumbe, eventType, eventDate, dueDate, localCurrencyOperationTaxAmount,
        localCurrencyOperationTaxCurrency, localCurrencyOperationValueAmount, localCurrencyOperationValueCurrency,
        foreignOperationValueAmount, foreignOperationValueCurrency, operationOutstandingBalanceAmount,
        operationOutstandingBalanceCurrency, vetAmountAmount, vetAmountCurrency, localCurrencyAdvancePercentage,
        deliveryForeignCurrency, operationCategoryCode, relationshipCode, foreignPartieName, foreignPartieCountryCode,
        createdAt, 'PREPOPULATE', updatedAt, 'PREPOPULATE')
$$ LANGUAGE SQL;

DROP FUNCTION IF EXISTS addExchangesOperationsEvent;
CREATE OR REPLACE FUNCTION addExchangesOperationsEvent(operationId uuid, eventSequenceNumbe varchar, eventType varchar, eventDate timestamp, dueDate date, localCurrencyOperationTaxAmount double precision,
                                                             localCurrencyOperationTaxCurrency varchar, localCurrencyOperationValueAmount double precision, localCurrencyOperationValueCurrency varchar,
                                                             foreignOperationValueAmount double precision, foreignOperationValueCurrency varchar, operationOutstandingBalanceAmount double precision,
                                                             operationOutstandingBalanceCurrency varchar, vetAmountAmount double precision, vetAmountCurrency varchar, localCurrencyAdvancePercentage double precision,
                                                             deliveryForeignCurrency varchar, operationCategoryCode varchar, relationshipCode varchar, foreignPartieName varchar, foreignPartieCountryCode varchar,
                                                             createdAt date, updatedAt date) RETURNS uuid AS $$

INSERT INTO exchanges_operation_event(operation_id, event_sequence_number, event_type,
                                      event_date, due_date, local_currency_operation_tax_amount,
                                      local_currency_operation_tax_currency, local_currency_operation_value_amount, local_currency_operation_value_currency,
                                      foreign_operation_value_amount, foreign_operation_value_currency, operation_outstanding_balance_amount,
                                      operation_outstanding_balance_currency, vet_amount_amount, vet_amount_currency, local_currency_advance_percentage,
                                      delivery_foreign_currency, operation_category_code, relationship_code, foreign_partie_name,foreign_partie_country_code,
                                      created_at, created_by, updated_at, updated_by)
VALUES (operationId, eventSequenceNumbe, eventType, eventDate, dueDate, localCurrencyOperationTaxAmount,
        localCurrencyOperationTaxCurrency, localCurrencyOperationValueAmount, localCurrencyOperationValueCurrency,
        foreignOperationValueAmount, foreignOperationValueCurrency, operationOutstandingBalanceAmount,
        operationOutstandingBalanceCurrency, vetAmountAmount, vetAmountCurrency, localCurrencyAdvancePercentage,
        deliveryForeignCurrency, operationCategoryCode, relationshipCode, foreignPartieName, foreignPartieCountryCode,
        createdAt, 'PREPOPULATE', updatedAt, 'PREPOPULATE')

RETURNING event_id
$$ LANGUAGE SQL;

DROP FUNCTION IF EXISTS addWebhookUri;
CREATE OR REPLACE FUNCTION addWebhookUri(clientId varchar, webhookUri varchar, createdAt date, updatedAt date)
RETURNS void AS $$

INSERT INTO client_webhook_uri(client_id, webhook_uri, created_at, created_by, updated_at, updated_by)
VALUES(clientId, webhookUri, createdAt, 'PREPOPULATE', updatedAt, 'PREPOPULATE')

$$ LANGUAGE SQL;