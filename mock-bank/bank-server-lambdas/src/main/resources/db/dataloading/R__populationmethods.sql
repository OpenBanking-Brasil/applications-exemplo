--- RECREATABLE MIGRATION FILE ---
--- This will be run every time flyway detects that its hash has changed ---
--- Needs to be kept up to date with schema changes in the other migrations ---

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

CREATE OR REPLACE FUNCTION addAccountTransaction(accountId uuid, transactionId varchar, completedAuthorisedPaymentType varchar,
                                                 creditDebitType varchar, transactionName varchar, transactionType varchar,
                                                 transactionAmount double precision, transactionCurrency varchar, transactionDate date,
                                                 partieCnpjCpf varchar, partiePersonType varchar, partieCompeCode varchar,
                                                 partieBranchCode varchar, partieNumber varchar, partieCheckDigit varchar) RETURNS int AS $$
INSERT INTO account_transactions (account_id, transaction_id, completed_authorised_payment_type, credit_debit_type,
                                  transaction_name, type, amount, transaction_currency, transaction_date,
                                  partie_cnpj_cpf, partie_person_type, partie_compe_code, partie_branch_code,
                                  partie_number, partie_check_digit,
                                  created_at, created_by, updated_at, updated_by)
VALUES (accountId, transactionId, completedAuthorisedPaymentType, creditDebitType, transactionName,
        transactionType, transactionAmount, transactionCurrency, transactionDate, partieCnpjCpf, partiePersonType,
        partieCompeCode, partieBranchCode, partieNumber, partieCheckDigit,
        NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING account_transaction_id
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addConsent(docId varchar, consentId varchar, businessEntityDocument int, expirationDateTime date,
                                      transactionFromDateTime date, transactionToDateTime date, creationDateTime date,
                                      statusUpdateDateTime date, status varchar, clientId varchar) RETURNS text AS $$
    INSERT INTO consents (consent_id, business_entity_document_id, account_holder_id, expiration_date_time, transaction_from_date_time,
                          transaction_to_date_time, creation_date_time, status_update_date_time, status, client_id,
                          created_at, created_by, updated_at, updated_by)
    VALUES (consentId, businessEntityDocument, getAccountHolderId(docId), expirationDateTime,
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

CREATE OR REPLACE FUNCTION addBusinesDocument(identification varchar, rel varchar) RETURNS int AS $$
    INSERT INTO business_entity_documents (identification, rel, created_at, created_by, updated_at, updated_by)
    VALUES (identification, rel,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING business_entity_document_id;
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
                                                            amount double precision, currency varchar, transactionDate date,
                                                            billPostDate date, payeeMCC int8) RETURNS void AS $$
    INSERT INTO credit_card_accounts_transaction(credit_card_account_id, bill_id, identification_number, line_name,
                                                 transaction_name, credit_debit_type, transaction_type,
                                                 transactional_additional_info, payment_type, fee_type,
                                                 fee_type_additional_info, other_credits_type,
                                                 other_credits_additional_info, charge_identificator, charge_number,
                                                 brazilian_amount, amount, currency, transaction_date, bill_post_date,
                                                 payee_mcc,
                                                 created_at, created_by, updated_at, updated_by)
    VALUES (creditCardAccountId, billId, identificationNumber, lineName, transactionName, creditDebitType,
            transactionType, transactionalAdditionalInfo, paymentType, feeType, feeTypeAdditionalInfo, otherCreditsType,
            otherCreditsAdditionalInfo, chargeIdentificator, chargeNumber, brazilianAmount, amount, currency,
            transactionDate, billPostDate, payeeMCC,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;

CREATE OR REPLACE FUNCTION addCreditCardAccountsTransactionWithId(creditCardAccountId uuid, billId uuid, transactionId uuid,
                                                                  identificationNumber varchar, lineName varchar, transactionName varchar,
                                                                  creditDebitType varchar, transactionType varchar, transactionalAdditionalInfo varchar,
                                                                  paymentType varchar, feeType varchar, feeTypeAdditionalInfo varchar,
                                                                  otherCreditsType varchar, otherCreditsAdditionalInfo varchar,
                                                                  chargeIdentificator varchar, chargeNumber int8, brazilianAmount double precision,
                                                                  amount double precision, currency varchar, transactionDate date,
                                                                  billPostDate date, payeeMCC int8) RETURNS void AS $$
INSERT INTO credit_card_accounts_transaction(credit_card_account_id, bill_id, transaction_id, identification_number,
                                             line_name, transaction_name, credit_debit_type, transaction_type,
                                             transactional_additional_info, payment_type, fee_type,
                                             fee_type_additional_info, other_credits_type, other_credits_additional_info,
                                             charge_identificator, charge_number, brazilian_amount, amount, currency,
                                             transaction_date, bill_post_date, payee_mcc,
                                             created_at, created_by, updated_at, updated_by)
VALUES (creditCardAccountId, billId, transactionId, identificationNumber, lineName, transactionName, creditDebitType,
        transactionType, transactionalAdditionalInfo, paymentType, feeType, feeTypeAdditionalInfo, otherCreditsType,
        otherCreditsAdditionalInfo, chargeIdentificator, chargeNumber, brazilianAmount, amount, currency, transactionDate,
        billPostDate, payeeMCC,
        NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$ LANGUAGE SQL;
