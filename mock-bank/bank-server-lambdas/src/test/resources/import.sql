--
-- PostgreSQL database dump
--

-- Dumped from database version 11.9 (Debian 11.9-1.pgdg90+1)
-- Dumped by pg_dump version 11.9 (Debian 11.9-1.pgdg90+1)

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner:
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


--
-- Name: addaccount(character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, double precision, character varying, double precision, character varying, double precision, character varying, double precision, character varying, double precision, character varying, double precision, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addaccount(docid character varying, status character varying, currency character varying, accounttype character varying, accountsubtype character varying, brandname character varying, companycnpj character varying, compecode character varying, branchcode character varying, number character varying, checkdigit character varying, availableamount double precision, availableamountcurrency character varying, blockedamount double precision, blockedamountcurrency character varying, automaticallyinvestedamount double precision, automaticallyinvestedamountcurrency character varying, overdraftcontractedlimit double precision, overdraftcontractedlimitcurrency character varying, overdraftusedlimit double precision, overdraftusedlimitcurrency character varying, unarrangedoverdraftamount double precision, unarrangedoverdraftamountcurrency character varying, debtorispb character varying, debtorissuer character varying, debtortype character varying) RETURNS uuid
    LANGUAGE sql
    AS $$
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
$$;


ALTER FUNCTION public.addaccount(docid character varying, status character varying, currency character varying, accounttype character varying, accountsubtype character varying, brandname character varying, companycnpj character varying, compecode character varying, branchcode character varying, number character varying, checkdigit character varying, availableamount double precision, availableamountcurrency character varying, blockedamount double precision, blockedamountcurrency character varying, automaticallyinvestedamount double precision, automaticallyinvestedamountcurrency character varying, overdraftcontractedlimit double precision, overdraftcontractedlimitcurrency character varying, overdraftusedlimit double precision, overdraftusedlimitcurrency character varying, unarrangedoverdraftamount double precision, unarrangedoverdraftamountcurrency character varying, debtorispb character varying, debtorissuer character varying, debtortype character varying) OWNER TO test;

--
-- Name: addaccountholder(character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addaccountholder(doc character varying, rel character varying, accountholdername character varying, userid character varying) RETURNS uuid
    LANGUAGE sql
    AS $$
    INSERT INTO account_holders (document_identification, document_rel, account_holder_name, user_id,
                                 created_at, created_by, updated_at, updated_by)
    VALUES (doc, rel, accountHolderName, userId,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING account_holder_id
$$;


ALTER FUNCTION public.addaccountholder(doc character varying, rel character varying, accountholdername character varying, userid character varying) OWNER TO test;

--
-- Name: addaccounttransaction(uuid, character varying, character varying, character varying, character varying, double precision, character varying, date, character varying, character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addaccounttransaction(accountid uuid, completedauthorisedpaymenttype character varying, creditdebittype character varying, transactionname character varying, transactiontype character varying, transactionamount double precision, transactioncurrency character varying, transactiondate date, partiecnpjcpf character varying, partiepersontype character varying, partiecompecode character varying, partiebranchcode character varying, partienumber character varying, partiecheckdigit character varying) RETURNS integer
    LANGUAGE sql
    AS $$
    INSERT INTO account_transactions (account_id, transaction_id, completed_authorised_payment_type, credit_debit_type,
                                  transaction_name, type, amount, transaction_currency, transaction_date,
                                  partie_cnpj_cpf, partie_person_type, partie_compe_code, partie_branch_code,
                                  partie_number, partie_check_digit,
                                  created_at, created_by, updated_at, updated_by)
    VALUES (accountId, uuid_generate_v4(), completedAuthorisedPaymentType, creditDebitType, transactionName,
            transactionType, transactionAmount, transactionCurrency, transactionDate, partieCnpjCpf, partiePersonType,
            partieCompeCode, partieBranchCode, partieNumber, partieCheckDigit,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING account_transaction_id
$$;


ALTER FUNCTION public.addaccounttransaction(accountid uuid, completedauthorisedpaymenttype character varying, creditdebittype character varying, transactionname character varying, transactiontype character varying, transactionamount double precision, transactioncurrency character varying, transactiondate date, partiecnpjcpf character varying, partiepersontype character varying, partiecompecode character varying, partiebranchcode character varying, partienumber character varying, partiecheckdigit character varying) OWNER TO test;

--
-- Name: addaccounttransactionwithid(uuid, uuid, character varying, character varying, character varying, character varying, double precision, character varying, date, character varying, character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addaccounttransactionwithid(accountid uuid, transactionid uuid, completedauthorisedpaymenttype character varying, creditdebittype character varying, transactionname character varying, transactiontype character varying, transactionamount double precision, transactioncurrency character varying, transactiondate date, partiecnpjcpf character varying, partiepersontype character varying, partiecompecode character varying, partiebranchcode character varying, partienumber character varying, partiecheckdigit character varying) RETURNS integer
    LANGUAGE sql
    AS $$
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
$$;


ALTER FUNCTION public.addaccounttransactionwithid(accountid uuid, transactionid uuid, completedauthorisedpaymenttype character varying, creditdebittype character varying, transactionname character varying, transactiontype character varying, transactionamount double precision, transactioncurrency character varying, transactiondate date, partiecnpjcpf character varying, partiepersontype character varying, partiecompecode character varying, partiebranchcode character varying, partienumber character varying, partiecheckdigit character varying) OWNER TO test;

--
-- Name: addaccounttransactionwithid(uuid, character varying, character varying, character varying, character varying, character varying, double precision, character varying, date, character varying, character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addaccounttransactionwithid(accountid uuid, transactionid character varying, completedauthorisedpaymenttype character varying, creditdebittype character varying, transactionname character varying, transactiontype character varying, transactionamount double precision, transactioncurrency character varying, transactiondate date, partiecnpjcpf character varying, partiepersontype character varying, partiecompecode character varying, partiebranchcode character varying, partienumber character varying, partiecheckdigit character varying) RETURNS integer
    LANGUAGE sql
    AS $$
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
$$;


ALTER FUNCTION public.addaccounttransactionwithid(accountid uuid, transactionid character varying, completedauthorisedpaymenttype character varying, creditdebittype character varying, transactionname character varying, transactiontype character varying, transactionamount double precision, transactioncurrency character varying, transactiondate date, partiecnpjcpf character varying, partiepersontype character varying, partiecompecode character varying, partiebranchcode character varying, partienumber character varying, partiecheckdigit character varying) OWNER TO test;

--
-- Name: addaccountwithid(character varying, uuid, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, double precision, character varying, double precision, character varying, double precision, character varying, double precision, character varying, double precision, character varying, double precision, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addaccountwithid(docid character varying, accountid uuid, status character varying, currency character varying, accounttype character varying, accountsubtype character varying, brandname character varying, companycnpj character varying, compecode character varying, branchcode character varying, number character varying, checkdigit character varying, availableamount double precision, availableamountcurrency character varying, blockedamount double precision, blockedamountcurrency character varying, automaticallyinvestedamount double precision, automaticallyinvestedamountcurrency character varying, overdraftcontractedlimit double precision, overdraftcontractedlimitcurrency character varying, overdraftusedlimit double precision, overdraftusedlimitcurrency character varying, unarrangedoverdraftamount double precision, unarrangedoverdraftamountcurrency character varying, debtorispb character varying, debtorissuer character varying, debtortype character varying) RETURNS uuid
    LANGUAGE sql
    AS $$
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
$$;


ALTER FUNCTION public.addaccountwithid(docid character varying, accountid uuid, status character varying, currency character varying, accounttype character varying, accountsubtype character varying, brandname character varying, companycnpj character varying, compecode character varying, branchcode character varying, number character varying, checkdigit character varying, availableamount double precision, availableamountcurrency character varying, blockedamount double precision, blockedamountcurrency character varying, automaticallyinvestedamount double precision, automaticallyinvestedamountcurrency character varying, overdraftcontractedlimit double precision, overdraftcontractedlimitcurrency character varying, overdraftusedlimit double precision, overdraftusedlimitcurrency character varying, unarrangedoverdraftamount double precision, unarrangedoverdraftamountcurrency character varying, debtorispb character varying, debtorissuer character varying, debtortype character varying) OWNER TO test;

--
-- Name: addballoonpayments(uuid, character varying, character varying, double precision); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addballoonpayments(contractid uuid, duedate character varying, currency character varying, amount double precision) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO balloon_payments (contract_id , due_date , currency, amount,
                                  created_at, created_by, updated_at, updated_by)
    VALUES (contractId, dueDate, currency, amount,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addballoonpayments(contractid uuid, duedate character varying, currency character varying, amount double precision) OWNER TO test;

--
-- Name: addbusinessfinancialrelations(character varying, timestamp without time zone); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addbusinessfinancialrelations(docid character varying, startdate timestamp without time zone) RETURNS uuid
    LANGUAGE sql
    AS $$
    INSERT INTO business_financial_relations (account_holder_id, start_date,
                                              created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId), startDate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING business_financial_relations_id
$$;


ALTER FUNCTION public.addbusinessfinancialrelations(docid character varying, startdate timestamp without time zone) OWNER TO test;

--
-- Name: addbusinessfinancialrelationsprocurator(character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addbusinessfinancialrelationsprocurator(docid character varying, type character varying, cnpj_cpf character varying, civilname character varying, socialname character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO business_financial_relations_procurators (business_financial_relations_id, type, cnpj_cpf_number, civil_name,
                                                          social_name,
                                                          created_at, created_by, updated_at, updated_by)
    VALUES (getBusinessFinancialRelationsId(docId), type, cnpj_cpf, civilName, socialName,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addbusinessfinancialrelationsprocurator(docid character varying, type character varying, cnpj_cpf character varying, civilname character varying, socialname character varying) OWNER TO test;

--
-- Name: addbusinessfinancialrelationsproductservicestype(character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addbusinessfinancialrelationsproductservicestype(docid character varying, type character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO business_financial_relations_products_services_type (business_financial_relations_id, type,
                                                                     created_at, created_by, updated_at, updated_by)
    VALUES (getBusinessFinancialRelationsId(docId), type,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addbusinessfinancialrelationsproductservicestype(docid character varying, type character varying) OWNER TO test;

--
-- Name: addbusinessidentifications(character varying, character varying, character varying, character varying, date, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addbusinessidentifications(docid character varying, brandname character varying, companyname character varying, tradename character varying, incorporationdate date, cnpjnumber character varying) RETURNS uuid
    LANGUAGE sql
    AS $$

    INSERT INTO business_identifications (account_holder_id, brand_name, company_name, trade_name,
                                          incorporation_date, cnpj_number,
                                          created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId),brandName, companyName, tradeName, incorporationDate, cnpjNumber,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING business_identifications_id
$$;


ALTER FUNCTION public.addbusinessidentifications(docid character varying, brandname character varying, companyname character varying, tradename character varying, incorporationdate date, cnpjnumber character varying) OWNER TO test;

--
-- Name: addbusinessidentificationscompanycnpj(uuid, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addbusinessidentificationscompanycnpj(businessidentificationsid uuid, companycnpj character varying) RETURNS uuid
    LANGUAGE sql
    AS $$
    INSERT INTO business_identifications_company_cnpj (business_identifications_id, company_cnpj,
                                                   created_at, created_by, updated_at, updated_by)
    VALUES (businessIdentificationsId, companyCnpj,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING business_identifications_id
$$;


ALTER FUNCTION public.addbusinessidentificationscompanycnpj(businessidentificationsid uuid, companycnpj character varying) OWNER TO test;

--
-- Name: addbusinessidentificationsemail(uuid, boolean, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addbusinessidentificationsemail(businessidentificationsid uuid, ismain boolean, email character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO business_emails (business_identifications_id, is_main, email,
                                 created_at, created_by, updated_at, updated_by)
    VALUES (businessIdentificationsId, isMain, email,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addbusinessidentificationsemail(businessidentificationsid uuid, ismain boolean, email character varying) OWNER TO test;

--
-- Name: addbusinessidentificationsotherdocument(uuid, character varying, character varying, character varying, date); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addbusinessidentificationsotherdocument(businessidentificationsid uuid, type character varying, number character varying, country character varying, expirationdate date) RETURNS uuid
    LANGUAGE sql
    AS $$
    INSERT INTO business_other_documents (business_identifications_id, type, number, country, expiration_date,
                                          created_at, created_by, updated_at, updated_by)
    VALUES (businessIdentificationsId, type, number, country, expirationDate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING business_identifications_id
$$;


ALTER FUNCTION public.addbusinessidentificationsotherdocument(businessidentificationsid uuid, type character varying, number character varying, country character varying, expirationdate date) OWNER TO test;

--
-- Name: addbusinessidentificationsparties(uuid, character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, character varying, character varying, character varying, character varying, date, date); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addbusinessidentificationsparties(businessidentificationsid uuid, persontype character varying, type character varying, civilname character varying, socialname character varying, companyname character varying, tradename character varying, startdate date, shareholding character varying, documenttype character varying, documentnumber character varying, documentadditionalinfo character varying, documentcountry character varying, documentexpirationdate date, documentissuedate date) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO business_parties (business_identifications_id, person_type, type, civil_name, social_name, company_name,
                                  trade_name, start_date, shareholding, document_type, document_number,
                                  document_additional_info, document_country, document_expiration_date,
                                  document_issue_date,
                                  created_at, created_by, updated_at, updated_by)
    VALUES (businessIdentificationsId, personType, type, civilName, socialName, companyName, tradeName, startDate,
            shareholding, documentType, documentNumber, documentAdditionalInfo, documentCountry, documentExpirationDate,
            documentIssueDate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addbusinessidentificationsparties(businessidentificationsid uuid, persontype character varying, type character varying, civilname character varying, socialname character varying, companyname character varying, tradename character varying, startdate date, shareholding character varying, documenttype character varying, documentnumber character varying, documentadditionalinfo character varying, documentcountry character varying, documentexpirationdate date, documentissuedate date) OWNER TO test;

--
-- Name: addbusinessidentificationsphone(uuid, boolean, character varying, character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addbusinessidentificationsphone(businessidentificationsid uuid, ismain boolean, type character varying, additionalinfo character varying, countrycallingcode character varying, areacode character varying, number character varying, phoneextension character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO business_phones (business_identifications_id, is_main, type, additional_info, country_calling_code,
                                 area_code, number, phone_extension,
                                 created_at, created_by, updated_at, updated_by)
    VALUES (businessIdentificationsId, isMain, type, additionalInfo, countryCallingCode, areaCode, number, phoneExtension,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addbusinessidentificationsphone(businessidentificationsid uuid, ismain boolean, type character varying, additionalinfo character varying, countrycallingcode character varying, areacode character varying, number character varying, phoneextension character varying) OWNER TO test;

--
-- Name: addbusinessidentificationspostaladdress(uuid, boolean, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addbusinessidentificationspostaladdress(businessidentificationsid uuid, ismain boolean, address character varying, additionalinfo character varying, districtname character varying, townname character varying, ibgetowncode character varying, countrysubdivision character varying, postcode character varying, country character varying, countrycode character varying, latitude character varying, longitude character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO business_postal_addresses (business_identifications_id, is_main, address, additional_info,
                                           district_name, town_name, ibge_town_code, country_subdivision,
                                           post_code, country, country_code, latitude, longitude,
                                           created_at, created_by, updated_at, updated_by)
    VALUES (businessIdentificationsId, isMain, address, additionalInfo, districtName, townName, ibgeTownCode,
            countrySubdivision, postCode, country, countryCode, latitude, longitude,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addbusinessidentificationspostaladdress(businessidentificationsid uuid, ismain boolean, address character varying, additionalinfo character varying, districtname character varying, townname character varying, ibgetowncode character varying, countrysubdivision character varying, postcode character varying, country character varying, countrycode character varying, latitude character varying, longitude character varying) OWNER TO test;

--
-- Name: addbusinessqualifications(character varying, character varying, character varying, numeric, character varying, integer, double precision, character varying, date); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addbusinessqualifications(docid character varying, informedrevenuefrequency character varying, informedrevenuefrequencyadditionalinformation character varying, informedrevenueamount numeric, informedrevenuecurrency character varying, informedrevenueyear integer, informedpatrimonyamount double precision, informedpatrimonycurrency character varying, informedpatrimonydate date) RETURNS uuid
    LANGUAGE sql
    AS $$
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
$$;


ALTER FUNCTION public.addbusinessqualifications(docid character varying, informedrevenuefrequency character varying, informedrevenuefrequencyadditionalinformation character varying, informedrevenueamount numeric, informedrevenuecurrency character varying, informedrevenueyear integer, informedpatrimonyamount double precision, informedpatrimonycurrency character varying, informedpatrimonydate date) OWNER TO test;

--
-- Name: addbusinessqualificationseconomicactivities(uuid, integer, boolean); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addbusinessqualificationseconomicactivities(businessqualificationsid uuid, code integer, ismain boolean) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO business_qualifications_economic_activities (business_qualifications_id, code, is_main,
                                                             created_at, created_by, updated_at, updated_by)
    VALUES (businessQualificationsId, code, isMain,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addbusinessqualificationseconomicactivities(businessqualificationsid uuid, code integer, ismain boolean) OWNER TO test;

--
-- Name: addconsent(character varying, character varying, character varying, character varying, date, date, date, date, date, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addconsent(docid character varying, consentid character varying, businessdocumentidentification character varying, businessdocumentrel character varying, expirationdatetime date, transactionfromdatetime date, transactiontodatetime date, creationdatetime date, statusupdatedatetime date, status character varying, clientid character varying) RETURNS text
    LANGUAGE sql
    AS $$
    INSERT INTO consents (consent_id, business_document_identification, business_document_rel, account_holder_id, expiration_date_time, transaction_from_date_time,
                          transaction_to_date_time, creation_date_time, status_update_date_time, status, client_id,
                          created_at, created_by, updated_at, updated_by)
    VALUES (consentId, businessDocumentIdentification, businessDocumentRel, getAccountHolderId(docId), expirationDateTime,
            transactionFromDateTime, transactionToDateTime, creationDateTime, statusUpdateDateTime, status, clientId,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING consent_id
$$;


ALTER FUNCTION public.addconsent(docid character varying, consentid character varying, businessdocumentidentification character varying, businessdocumentrel character varying, expirationdatetime date, transactionfromdatetime date, transactiontodatetime date, creationdatetime date, statusupdatedatetime date, status character varying, clientid character varying) OWNER TO test;

--
-- Name: addconsentpermissions(character varying, text); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addconsentpermissions(permission character varying, consentid text) RETURNS integer
    LANGUAGE sql
    AS $$
    INSERT INTO consent_permissions (permission, consent_id, created_at, created_by, updated_at, updated_by)
    VALUES (permission, consentId,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING reference_id
$$;


ALTER FUNCTION public.addconsentpermissions(permission character varying, consentid text) OWNER TO test;

--
-- Name: addcontract(character varying, character varying, character varying, character varying, character varying, character varying, date, character varying, character varying, character varying, date, date, double precision, date, character varying, character varying, date, double precision, character varying, character varying, character varying, integer, double precision, character varying, integer, character varying, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcontract(docid character varying, status character varying, currency character varying, companycnpj character varying, contracttype character varying, contractnumber character varying, contractdate date, productname character varying, producttype character varying, productsubtype character varying, disbursementdate date, settlementdate date, contractamount double precision, duedate date, instalmentperiodicity character varying, instalmentperiodicityadditionalinfo character varying, firstinstalmentduedate date, cet double precision, amortizationscheduled character varying, amortizationscheduledadditionalinfo character varying, ipoccode character varying, paidinstalments integer, contractoutstandingbalance double precision, typenumberofinstalments character varying, totalnumberofinstalments integer, typecontractremaining character varying, contractremainingnumber integer, dueinstalments integer, pastdueinstalments integer) RETURNS uuid
    LANGUAGE sql
    AS $$
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
$$;


ALTER FUNCTION public.addcontract(docid character varying, status character varying, currency character varying, companycnpj character varying, contracttype character varying, contractnumber character varying, contractdate date, productname character varying, producttype character varying, productsubtype character varying, disbursementdate date, settlementdate date, contractamount double precision, duedate date, instalmentperiodicity character varying, instalmentperiodicityadditionalinfo character varying, firstinstalmentduedate date, cet double precision, amortizationscheduled character varying, amortizationscheduledadditionalinfo character varying, ipoccode character varying, paidinstalments integer, contractoutstandingbalance double precision, typenumberofinstalments character varying, totalnumberofinstalments integer, typecontractremaining character varying, contractremainingnumber integer, dueinstalments integer, pastdueinstalments integer) OWNER TO test;

--
-- Name: addcontractedfees(uuid, character varying, character varying, character varying, character varying, double precision, double precision); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcontractedfees(contractid uuid, feename character varying, feecode character varying, feechargetype character varying, feecharge character varying, feeamount double precision, feerate double precision) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO contracted_fees (contract_id, fee_name, fee_code, fee_charge_type,
                                 fee_charge, fee_amount, fee_rate,
                                 created_at, created_by, updated_at, updated_by)
    VALUES (contractId, feeName, feeCode, feeChargeType, feeCharge, feeAmount, feeRate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addcontractedfees(contractid uuid, feename character varying, feecode character varying, feechargetype character varying, feecharge character varying, feeamount double precision, feerate double precision) OWNER TO test;

--
-- Name: addcontractedfinancecharges(uuid, character varying, character varying, double precision); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcontractedfinancecharges(contractid uuid, chargetype character varying, chargeadditionalinfo character varying, chargerate double precision) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO contracted_finance_charges (contract_id , charge_type , charge_additional_info, charge_rate,
                                            created_at, created_by, updated_at, updated_by)
    VALUES (contractId, chargeType, chargeAdditionalInfo, chargeRate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addcontractedfinancecharges(contractid uuid, chargetype character varying, chargeadditionalinfo character varying, chargerate double precision) OWNER TO test;

--
-- Name: addcontractinterestrates(uuid, character varying, character varying, character varying, character varying, character varying, character varying, character varying, double precision, double precision, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcontractinterestrates(contractid uuid, taxtype character varying, interestratetype character varying, taxperiodicity character varying, calculation character varying, referentialrateindexertype character varying, referentialrateindexersubtype character varying, referentialrateindexeradditionalinfo character varying, prefixedrate double precision, postfixedrate double precision, additionalinfo character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO interest_rates (contract_id, tax_type, interest_rate_type, tax_periodicity, calculation,
                                referential_rate_indexer_type, referential_rate_indexer_sub_type,
                                referential_rate_indexer_additional_info, pre_fixed_rate, post_fixed_rate,
                                additional_info,
                                created_at, created_by, updated_at, updated_by)
    VALUES (contractId, taxType, interestRateType, taxPeriodicity, calculation, referentialRateIndexerType,
            referentialRateIndexerSubType, referentialRateIndexerAdditionalInfo, preFixedRate,
            postFixedRate, additionalInfo,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addcontractinterestrates(contractid uuid, taxtype character varying, interestratetype character varying, taxperiodicity character varying, calculation character varying, referentialrateindexertype character varying, referentialrateindexersubtype character varying, referentialrateindexeradditionalinfo character varying, prefixedrate double precision, postfixedrate double precision, additionalinfo character varying) OWNER TO test;

--
-- Name: addcontractwarranties(uuid, character varying, character varying, character varying, double precision); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcontractwarranties(contractid uuid, currency character varying, warrantytype character varying, warrantysubtype character varying, warrantyamount double precision) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO warranties (contract_id , currency , warranty_type, warranty_subtype, warranty_amount,
                            created_at, created_by, updated_at, updated_by)
    VALUES (contractId, currency, warrantyType, warrantySubtype, warrantyAmount,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addcontractwarranties(contractid uuid, currency character varying, warrantytype character varying, warrantysubtype character varying, warrantyamount double precision) OWNER TO test;

--
-- Name: addcontractwithid(character varying, uuid, character varying, character varying, character varying, character varying, character varying, date, character varying, character varying, character varying, date, date, double precision, date, character varying, character varying, date, double precision, character varying, character varying, character varying, integer, double precision, character varying, integer, character varying, integer, integer, integer); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcontractwithid(docid character varying, contractid uuid, status character varying, currency character varying, companycnpj character varying, contracttype character varying, contractnumber character varying, contractdate date, productname character varying, producttype character varying, productsubtype character varying, disbursementdate date, settlementdate date, contractamount double precision, duedate date, instalmentperiodicity character varying, instalmentperiodicityadditionalinfo character varying, firstinstalmentduedate date, cet double precision, amortizationscheduled character varying, amortizationscheduledadditionalinfo character varying, ipoccode character varying, paidinstalments integer, contractoutstandingbalance double precision, typenumberofinstalments character varying, totalnumberofinstalments integer, typecontractremaining character varying, contractremainingnumber integer, dueinstalments integer, pastdueinstalments integer) RETURNS uuid
    LANGUAGE sql
    AS $$
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
$$;


ALTER FUNCTION public.addcontractwithid(docid character varying, contractid uuid, status character varying, currency character varying, companycnpj character varying, contracttype character varying, contractnumber character varying, contractdate date, productname character varying, producttype character varying, productsubtype character varying, disbursementdate date, settlementdate date, contractamount double precision, duedate date, instalmentperiodicity character varying, instalmentperiodicityadditionalinfo character varying, firstinstalmentduedate date, cet double precision, amortizationscheduled character varying, amortizationscheduledadditionalinfo character varying, ipoccode character varying, paidinstalments integer, contractoutstandingbalance double precision, typenumberofinstalments character varying, totalnumberofinstalments integer, typecontractremaining character varying, contractremainingnumber integer, dueinstalments integer, pastdueinstalments integer) OWNER TO test;

--
-- Name: addcreditcardaccounts(character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcreditcardaccounts(docid character varying, brandname character varying, companycnpj character varying, name character varying, producttype character varying, productadditionalinfo character varying, creditcardnetwork character varying, networkadditionalinfo character varying, status character varying) RETURNS uuid
    LANGUAGE sql
    AS $$
    INSERT INTO credit_card_accounts(brand_name, company_cnpj, name, product_type, product_additional_info,
                                     credit_card_network, network_additional_info, status, account_holder_id,
                                     created_at, created_by, updated_at, updated_by)
    VALUES (brandName, companyCnpj, name, productType, productAdditionalInfo, creditCardNetwork,
            networkAdditionalInfo, status, getAccountHolderId(docId),
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING credit_card_account_id
$$;


ALTER FUNCTION public.addcreditcardaccounts(docid character varying, brandname character varying, companycnpj character varying, name character varying, producttype character varying, productadditionalinfo character varying, creditcardnetwork character varying, networkadditionalinfo character varying, status character varying) OWNER TO test;

--
-- Name: addcreditcardaccountsbills(uuid, date, double precision, character varying, double precision, character varying, boolean); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcreditcardaccountsbills(creditcardaccountid uuid, duedate date, billtotalamount double precision, billtotalamountcurrency character varying, billminimumamount double precision, billminimumamountcurrency character varying, isinstalment boolean) RETURNS uuid
    LANGUAGE sql
    AS $$
    INSERT INTO credit_card_accounts_bills(credit_card_account_id, due_date, bill_total_amount,
                                           bill_total_amount_currency, bill_minimum_amount,
                                           bill_minimum_amount_currency, is_instalment,
                                           created_at, created_by, updated_at, updated_by)
    VALUES (creditCardAccountId, dueDate, billTotalAmount, billTotalAmountCurrency, billMinimumAmount,
            billMinimumAmountCurrency, isInstalment,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING bill_id
$$;


ALTER FUNCTION public.addcreditcardaccountsbills(creditcardaccountid uuid, duedate date, billtotalamount double precision, billtotalamountcurrency character varying, billminimumamount double precision, billminimumamountcurrency character varying, isinstalment boolean) OWNER TO test;

--
-- Name: addcreditcardaccountsbillsfinancecharge(uuid, character varying, character varying, double precision, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcreditcardaccountsbillsfinancecharge(billid uuid, type character varying, additionalinfo character varying, amount double precision, currency character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO credit_card_accounts_bills_finance_charge(bill_id, type, additional_info, amount, currency,
                                                          created_at, created_by, updated_at, updated_by)
    VALUES (billId, type, additionalInfo, amount, currency,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addcreditcardaccountsbillsfinancecharge(billid uuid, type character varying, additionalinfo character varying, amount double precision, currency character varying) OWNER TO test;

--
-- Name: addcreditcardaccountsbillspayment(uuid, character varying, date, character varying, double precision, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcreditcardaccountsbillspayment(billid uuid, valuetype character varying, paymentdate date, paymentmode character varying, amount double precision, currency character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO credit_card_accounts_bills_payment(bill_id, value_type, payment_date, payment_mode, amount, currency,
                                                   created_at, created_by, updated_at, updated_by)
    VALUES (billId, valueType, paymentDate, paymentMode, amount, currency,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addcreditcardaccountsbillspayment(billid uuid, valuetype character varying, paymentdate date, paymentmode character varying, amount double precision, currency character varying) OWNER TO test;

--
-- Name: addcreditcardaccountsbillswithid(uuid, uuid, date, double precision, character varying, double precision, character varying, boolean); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcreditcardaccountsbillswithid(creditcardaccountid uuid, billid uuid, duedate date, billtotalamount double precision, billtotalamountcurrency character varying, billminimumamount double precision, billminimumamountcurrency character varying, isinstalment boolean) RETURNS uuid
    LANGUAGE sql
    AS $$
INSERT INTO credit_card_accounts_bills(credit_card_account_id, bill_id, due_date, bill_total_amount,
                                       bill_total_amount_currency, bill_minimum_amount, bill_minimum_amount_currency,
                                       is_instalment,
                                       created_at, created_by, updated_at, updated_by)
    VALUES (creditCardAccountId, billId, dueDate, billTotalAmount, billTotalAmountCurrency, billMinimumAmount,
            billMinimumAmountCurrency, isInstalment,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING bill_id
$$;


ALTER FUNCTION public.addcreditcardaccountsbillswithid(creditcardaccountid uuid, billid uuid, duedate date, billtotalamount double precision, billtotalamountcurrency character varying, billminimumamount double precision, billminimumamountcurrency character varying, isinstalment boolean) OWNER TO test;

--
-- Name: addcreditcardaccountslimits(uuid, character varying, character varying, character varying, character varying, character varying, boolean, character varying, double precision, character varying, double precision, character varying, double precision); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcreditcardaccountslimits(creditcardaccountid uuid, creditlinelimittype character varying, consolidationtype character varying, identificationnumber character varying, linename character varying, linenameadditionalinfo character varying, islimitflexible boolean, limitamountcurrency character varying, limitamount double precision, usedamountcurrency character varying, usedamount double precision, availableamountcurrency character varying, availableamount double precision) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO credit_card_accounts_limits(credit_card_account_id, credit_line_limit_type, consolidation_type,
                                            identification_number, line_name, line_name_additional_info,
                                            is_limit_flexible, limit_amount_currency, limit_amount, used_amount_currency,
                                            used_amount, available_amount_currency, available_amount,
                                            created_at, created_by, updated_at, updated_by)
    VALUES (creditCardAccountId, creditLineLimitType, consolidationType, identificationNumber, lineName,
            lineNameAdditionalInfo, isLimitFlexible, limitAmountCurrency, limitAmount, usedAmountCurrency, usedAmount,
            availableAmountCurrency, availableAmount,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addcreditcardaccountslimits(creditcardaccountid uuid, creditlinelimittype character varying, consolidationtype character varying, identificationnumber character varying, linename character varying, linenameadditionalinfo character varying, islimitflexible boolean, limitamountcurrency character varying, limitamount double precision, usedamountcurrency character varying, usedamount double precision, availableamountcurrency character varying, availableamount double precision) OWNER TO test;

--
-- Name: addcreditcardaccountstransaction(uuid, uuid, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, bigint, double precision, double precision, character varying, date, date, bigint); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcreditcardaccountstransaction(creditcardaccountid uuid, billid uuid, identificationnumber character varying, linename character varying, transactionname character varying, creditdebittype character varying, transactiontype character varying, transactionaladditionalinfo character varying, paymenttype character varying, feetype character varying, feetypeadditionalinfo character varying, othercreditstype character varying, othercreditsadditionalinfo character varying, chargeidentificator character varying, chargenumber bigint, brazilianamount double precision, amount double precision, currency character varying, transactiondate date, billpostdate date, payeemcc bigint) RETURNS void
    LANGUAGE sql
    AS $$
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
$$;


ALTER FUNCTION public.addcreditcardaccountstransaction(creditcardaccountid uuid, billid uuid, identificationnumber character varying, linename character varying, transactionname character varying, creditdebittype character varying, transactiontype character varying, transactionaladditionalinfo character varying, paymenttype character varying, feetype character varying, feetypeadditionalinfo character varying, othercreditstype character varying, othercreditsadditionalinfo character varying, chargeidentificator character varying, chargenumber bigint, brazilianamount double precision, amount double precision, currency character varying, transactiondate date, billpostdate date, payeemcc bigint) OWNER TO test;

--
-- Name: addcreditcardaccountstransactionwithid(uuid, uuid, uuid, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, bigint, double precision, double precision, character varying, date, date, bigint); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcreditcardaccountstransactionwithid(creditcardaccountid uuid, billid uuid, transactionid uuid, identificationnumber character varying, linename character varying, transactionname character varying, creditdebittype character varying, transactiontype character varying, transactionaladditionalinfo character varying, paymenttype character varying, feetype character varying, feetypeadditionalinfo character varying, othercreditstype character varying, othercreditsadditionalinfo character varying, chargeidentificator character varying, chargenumber bigint, brazilianamount double precision, amount double precision, currency character varying, transactiondate date, billpostdate date, payeemcc bigint) RETURNS void
    LANGUAGE sql
    AS $$
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
$$;


ALTER FUNCTION public.addcreditcardaccountstransactionwithid(creditcardaccountid uuid, billid uuid, transactionid uuid, identificationnumber character varying, linename character varying, transactionname character varying, creditdebittype character varying, transactiontype character varying, transactionaladditionalinfo character varying, paymenttype character varying, feetype character varying, feetypeadditionalinfo character varying, othercreditstype character varying, othercreditsadditionalinfo character varying, chargeidentificator character varying, chargenumber bigint, brazilianamount double precision, amount double precision, currency character varying, transactiondate date, billpostdate date, payeemcc bigint) OWNER TO test;

--
-- Name: addcreditcardaccountswithid(character varying, uuid, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcreditcardaccountswithid(docid character varying, creditcardaccountid uuid, brandname character varying, companycnpj character varying, name character varying, producttype character varying, productadditionalinfo character varying, creditcardnetwork character varying, networkadditionalinfo character varying, status character varying) RETURNS uuid
    LANGUAGE sql
    AS $$
INSERT INTO credit_card_accounts(brand_name, credit_card_account_id, company_cnpj, name, product_type,
                                 product_additional_info, credit_card_network, network_additional_info, status,
                                 account_holder_id,
                                 created_at, created_by, updated_at, updated_by)
    VALUES (brandName, creditCardAccountId, companyCnpj, name, productType, productAdditionalInfo, creditCardNetwork,
            networkAdditionalInfo, status, getAccountHolderId(docId),
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING credit_card_account_id
$$;


ALTER FUNCTION public.addcreditcardaccountswithid(docid character varying, creditcardaccountid uuid, brandname character varying, companycnpj character varying, name character varying, producttype character varying, productadditionalinfo character varying, creditcardnetwork character varying, networkadditionalinfo character varying, status character varying) OWNER TO test;

--
-- Name: addcreditcardsaccountpaymentmethod(uuid, character varying, boolean); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addcreditcardsaccountpaymentmethod(creditcardaccountid uuid, identificationnumber character varying, ismultiplecreditcard boolean) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO credit_cards_account_payment_method(credit_card_account_id, identification_number,
                                                    is_multiple_credit_card,
                                                    created_at, created_by, updated_at, updated_by)
    VALUES (creditCardAccountId, identificationNumber, isMultipleCreditCard,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addcreditcardsaccountpaymentmethod(creditcardaccountid uuid, identificationnumber character varying, ismultiplecreditcard boolean) OWNER TO test;

--
-- Name: addoverparcelcharge(uuid, character varying, character varying, double precision); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addoverparcelcharge(releasesid uuid, chargetype character varying, chargeadditionalinfo character varying, chargeamount double precision) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO over_parcel_charges (releases_id, charge_type, charge_additional_info, charge_amount,
                                  created_at, created_by, updated_at, updated_by)
    VALUES (releasesId, chargeType, chargeAdditionalInfo, chargeAmount,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addoverparcelcharge(releasesid uuid, chargetype character varying, chargeadditionalinfo character varying, chargeamount double precision) OWNER TO test;

--
-- Name: addoverparcelfee(uuid, character varying, character varying, double precision); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addoverparcelfee(releasesid uuid, feename character varying, feecode character varying, feeamount double precision) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO over_parcel_fees (releases_id, fee_name, fee_code, fee_amount,
                                  created_at, created_by, updated_at, updated_by)
    VALUES (releasesId, feeName, feeCode, feeAmount,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addoverparcelfee(releasesid uuid, feename character varying, feecode character varying, feeamount double precision) OWNER TO test;

--
-- Name: addpersonalemails(uuid, boolean, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalemails(personalid uuid, ismain boolean, email character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO personal_emails(personal_identifications_id, is_main, email,
                                created_at, created_by, updated_at, updated_by)
    VALUES (personalId, isMain, email,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE');
$$;


ALTER FUNCTION public.addpersonalemails(personalid uuid, ismain boolean, email character varying) OWNER TO test;

--
-- Name: addpersonalfiliation(uuid, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalfiliation(personalidentificationsid uuid, type character varying, civilname character varying, socialname character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO personal_filiation (personal_identifications_id, type, civil_name, social_name,
                                    created_at, created_by, updated_at, updated_by)
    VALUES (personalIdentificationsId, type, civilName, socialName,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addpersonalfiliation(personalidentificationsid uuid, type character varying, civilname character varying, socialname character varying) OWNER TO test;

--
-- Name: addpersonalfinancialrelations(character varying, timestamp without time zone, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalfinancialrelations(docid character varying, startdate timestamp without time zone, additional character varying) RETURNS uuid
    LANGUAGE sql
    AS $$
    INSERT INTO personal_financial_relations (account_holder_id, start_date, products_services_type_additional_info,
                                              created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId), startDate, additional,
        NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING personal_financial_relations_id
$$;


ALTER FUNCTION public.addpersonalfinancialrelations(docid character varying, startdate timestamp without time zone, additional character varying) OWNER TO test;

--
-- Name: addpersonalfinancialrelationsprocurator(character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalfinancialrelationsprocurator(docid character varying, type character varying, cpf character varying, civilname character varying, socialname character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO personal_financial_relations_procurators (personal_financial_relations_id, type, cpf_number, civil_name,
                                                          social_name,
                                                          created_at, created_by, updated_at, updated_by)
    VALUES (getPersonalFinancialRelationsId(docId), type, cpf, civilName, socialName,
        NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addpersonalfinancialrelationsprocurator(docid character varying, type character varying, cpf character varying, civilname character varying, socialname character varying) OWNER TO test;

--
-- Name: addpersonalfinancialrelationsproductservicestype(character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalfinancialrelationsproductservicestype(docid character varying, type character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO personal_financial_relations_products_services_type (personal_financial_relations_id, type,
                                                                     created_at, created_by, updated_at, updated_by)
    VALUES (getPersonalFinancialRelationsId(docId), type,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addpersonalfinancialrelationsproductservicestype(docid character varying, type character varying) OWNER TO test;

--
-- Name: addpersonalidentifications(character varying, character varying, character varying, character varying, date, character varying, character varying, character varying, boolean, character varying, character varying, character varying, date, date); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalidentifications(docid character varying, brandname character varying, civilname character varying, socialname character varying, birthdate date, maritalstatuscode character varying, maritalstatusadditionalinfo character varying, sex character varying, hasbraziliannationality boolean, cpfnumber character varying, passportnumber character varying, passportcountry character varying, passportexpirationdate date, passportissuedate date) RETURNS uuid
    LANGUAGE sql
    AS $$
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
$$;


ALTER FUNCTION public.addpersonalidentifications(docid character varying, brandname character varying, civilname character varying, socialname character varying, birthdate date, maritalstatuscode character varying, maritalstatusadditionalinfo character varying, sex character varying, hasbraziliannationality boolean, cpfnumber character varying, passportnumber character varying, passportcountry character varying, passportexpirationdate date, passportissuedate date) OWNER TO test;

--
-- Name: addpersonalidentificationscompanycnpj(uuid, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalidentificationscompanycnpj(personalidentificationsid uuid, companycnpj character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO personal_identifications_company_cnpj (personal_identifications_id, company_cnpj,
                                                     created_at, created_by, updated_at, updated_by)
    VALUES (personalIdentificationsId, companyCnpj,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addpersonalidentificationscompanycnpj(personalidentificationsid uuid, companycnpj character varying) OWNER TO test;

--
-- Name: addpersonalidentificationsnationality(uuid, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalidentificationsnationality(personalidentificationsid uuid, othernationalitiesinfo character varying) RETURNS uuid
    LANGUAGE sql
    AS $$
    INSERT INTO personal_nationality (personal_identifications_id, other_nationalities_info,
                                          created_at, created_by, updated_at, updated_by)
    VALUES (personalIdentificationsId, otherNationalitiesInfo,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING personal_nationality_id
$$;


ALTER FUNCTION public.addpersonalidentificationsnationality(personalidentificationsid uuid, othernationalitiesinfo character varying) OWNER TO test;

--
-- Name: addpersonalidentificationsnationalitydocument(uuid, character varying, character varying, date, date, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalidentificationsnationalitydocument(personalnationalityid uuid, doctype character varying, number character varying, expirationdate date, issuedate date, country character varying, typeadditionalinfo character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO personal_nationality_documents (personal_nationality_id, type, number, expiration_date, issue_date,
                                                country, type_additional_info,
                                                created_at, created_by, updated_at, updated_by)
    VALUES (personalNationalityId, docType, number, expirationDate, issueDate, country, typeAdditionalInfo,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addpersonalidentificationsnationalitydocument(personalnationalityid uuid, doctype character varying, number character varying, expirationdate date, issuedate date, country character varying, typeadditionalinfo character varying) OWNER TO test;

--
-- Name: addpersonalidentificationsotherdocuments(uuid, character varying, character varying, character varying, character varying, character varying, date); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalidentificationsotherdocuments(personalidentificationsid uuid, type character varying, typeadditionalinfo character varying, number character varying, checkdigit character varying, additionalinfo character varying, expirationdate date) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO personal_other_documents (personal_identifications_id, type, type_additional_info, number, check_digit,
                                          additional_info, expiration_date,
                                          created_at, created_by, updated_at, updated_by)
    VALUES (personalIdentificationsId, type, typeAdditionalInfo, number, checkDigit, additionalInfo, expirationDate,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addpersonalidentificationsotherdocuments(personalidentificationsid uuid, type character varying, typeadditionalinfo character varying, number character varying, checkdigit character varying, additionalinfo character varying, expirationdate date) OWNER TO test;

--
-- Name: addpersonalidentificationswithid(character varying, uuid, character varying, character varying, character varying, date, character varying, character varying, character varying, boolean, character varying, character varying, character varying, date, date); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalidentificationswithid(docid character varying, personalidentificationsid uuid, brandname character varying, civilname character varying, socialname character varying, birthdate date, maritalstatuscode character varying, maritalstatusadditionalinfo character varying, sex character varying, hasbraziliannationality boolean, cpfnumber character varying, passportnumber character varying, passportcountry character varying, passportexpirationdate date, passportissuedate date) RETURNS uuid
    LANGUAGE sql
    AS $$
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
$$;


ALTER FUNCTION public.addpersonalidentificationswithid(docid character varying, personalidentificationsid uuid, brandname character varying, civilname character varying, socialname character varying, birthdate date, maritalstatuscode character varying, maritalstatusadditionalinfo character varying, sex character varying, hasbraziliannationality boolean, cpfnumber character varying, passportnumber character varying, passportcountry character varying, passportexpirationdate date, passportissuedate date) OWNER TO test;

--
-- Name: addpersonalphones(uuid, boolean, character varying, character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalphones(personalid uuid, ismain boolean, type character varying, additionalinfo character varying, countrycallingcode character varying, areacode character varying, number character varying, phoneextension character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO personal_phones(personal_identifications_id, is_main, type, additional_info, country_calling_code,
                                area_code, number, phone_extension,
                                created_at, created_by, updated_at, updated_by)
    VALUES (personalId, isMain, type, additionalInfo, countryCallingCode, areaCode, number, phoneExtension,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE');
$$;


ALTER FUNCTION public.addpersonalphones(personalid uuid, ismain boolean, type character varying, additionalinfo character varying, countrycallingcode character varying, areacode character varying, number character varying, phoneextension character varying) OWNER TO test;

--
-- Name: addpersonalpostaladdresses(uuid, boolean, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying, character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalpostaladdresses(personalid uuid, ismain boolean, address character varying, additional_info character varying, districtname character varying, townname character varying, ibgetowncode character varying, countrysubdivision character varying, postcode character varying, country character varying, countrycode character varying, latitude character varying, longitude character varying) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO personal_postal_addresses (personal_identifications_id, is_main, address, additional_info, district_name,
                                           town_name, ibge_town_code, country_subdivision, post_code, country,
                                           country_code, latitude, longitude,
                                           created_at, created_by, updated_at, updated_by)
    VALUES (personalId, isMain, address, additional_info, districtName, townName, ibgeTownCode, countrySubdivision,
            postCode, country, countryCode, latitude, longitude,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE');
$$;


ALTER FUNCTION public.addpersonalpostaladdresses(personalid uuid, ismain boolean, address character varying, additional_info character varying, districtname character varying, townname character varying, ibgetowncode character varying, countrysubdivision character varying, postcode character varying, country character varying, countrycode character varying, latitude character varying, longitude character varying) OWNER TO test;

--
-- Name: addpersonalqualifications(character varying, character varying, character varying, character varying, character varying, numeric, character varying, date, numeric, character varying, integer); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addpersonalqualifications(docid character varying, companycnpj character varying, occupationcode character varying, occupationdescription character varying, informedincomefrequency character varying, informedincomeamount numeric, informedincomecurrency character varying, informedincomedate date, informedpatrimonyamount numeric, informedpatrimonycurrency character varying, informedpatrimonyyear integer) RETURNS void
    LANGUAGE sql
    AS $$
    INSERT INTO personal_qualifications (account_holder_id, company_cnpj, occupation_code, occupation_description,
                                         informed_income_frequency, informed_income_amount, informed_income_currency,
                                         informed_income_date, informed_patrimony_amount, informed_patrimony_currency,
                                         informed_patrimony_year,
                                         created_at, created_by, updated_at, updated_by)
    VALUES (getAccountHolderId(docId), companyCnpj, occupationCode, occupationDescription, informedIncomeFrequency,
            informedIncomeAmount, informedIncomeCurrency, informedIncomeDate, informedPatrimonyAmount,
            informedPatrimonyCurrency, informedPatrimonyYear,
        NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
$$;


ALTER FUNCTION public.addpersonalqualifications(docid character varying, companycnpj character varying, occupationcode character varying, occupationdescription character varying, informedincomefrequency character varying, informedincomeamount numeric, informedincomecurrency character varying, informedincomedate date, informedpatrimonyamount numeric, informedpatrimonycurrency character varying, informedpatrimonyyear integer) OWNER TO test;

--
-- Name: addreleases(uuid, uuid, boolean, character varying, character varying, character varying, double precision); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.addreleases(contractid uuid, paymentsid uuid, isoverparcelpayment boolean, instalmentid character varying, paiddate character varying, currency character varying, paidamount double precision) RETURNS uuid
    LANGUAGE sql
    AS $$
    INSERT INTO releases (contract_id, payments_id, is_over_parcel_payment, instalment_id,
                          paid_date, currency, paid_amount,
                          created_at, created_by, updated_at, updated_by)
    VALUES (contractId, paymentsId, isOverParcelPayment, instalmentId, paidDate,currency, paidAmount,
            NOW(), 'PREPOPULATE', NOW(), 'PREPOPULATE')
    RETURNING releases_id
$$;


ALTER FUNCTION public.addreleases(contractid uuid, paymentsid uuid, isoverparcelpayment boolean, instalmentid character varying, paiddate character varying, currency character varying, paidamount double precision) OWNER TO test;

--
-- Name: getaccountholderid(character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.getaccountholderid(docid character varying) RETURNS uuid
    LANGUAGE sql
    AS $$
    SELECT account_holder_id FROM account_holders WHERE document_identification = docId;
$$;


ALTER FUNCTION public.getaccountholderid(docid character varying) OWNER TO test;

--
-- Name: getbusinessfinancialrelationsid(character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.getbusinessfinancialrelationsid(docid character varying) RETURNS uuid
    LANGUAGE sql
    AS $$
    -- works as this is a 1:1 relation with accountholder
    SELECT business_financial_relations_id FROM business_financial_relations WHERE account_holder_id = getAccountHolderId(docId) LIMIT 1;
$$;


ALTER FUNCTION public.getbusinessfinancialrelationsid(docid character varying) OWNER TO test;

--
-- Name: getpersonalfinancialrelationsid(character varying); Type: FUNCTION; Schema: public; Owner: test
--

CREATE FUNCTION public.getpersonalfinancialrelationsid(docid character varying) RETURNS uuid
    LANGUAGE sql
    AS $$
    -- works as this is a 1:1 relation with accountholder
    SELECT personal_financial_relations_id FROM personal_financial_relations WHERE account_holder_id = getAccountHolderId(docId) LIMIT 1;
$$;


ALTER FUNCTION public.getpersonalfinancialrelationsid(docid character varying) OWNER TO test;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: account_holders; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.account_holders (
    reference_id integer NOT NULL,
    account_holder_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    document_identification character varying(11),
    document_rel character varying(3),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    account_holder_name character varying,
    user_id character varying
);


ALTER TABLE public.account_holders OWNER TO test;

--
-- Name: account_holders_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.account_holders_aud (
    reference_id integer NOT NULL,
    account_holder_id uuid,
    document_identification character varying(11),
    document_rel character varying(3),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint,
    account_holder_name character varying,
    user_id character varying
);


ALTER TABLE public.account_holders_aud OWNER TO test;

--
-- Name: account_holders_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.account_holders_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.account_holders_reference_id_seq OWNER TO test;

--
-- Name: account_holders_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.account_holders_reference_id_seq OWNED BY public.account_holders.reference_id;


--
-- Name: account_transactions; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.account_transactions (
    account_transaction_id integer NOT NULL,
    account_id uuid NOT NULL,
    transaction_id character varying,
    completed_authorised_payment_type character varying,
    credit_debit_type character varying,
    transaction_name character varying,
    type character varying,
    amount double precision,
    transaction_currency character varying,
    transaction_date date,
    partie_cnpj_cpf character varying,
    partie_person_type character varying,
    partie_compe_code character varying,
    partie_branch_code character varying,
    partie_number character varying,
    partie_check_digit character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.account_transactions OWNER TO test;

--
-- Name: account_transactions_account_transaction_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.account_transactions_account_transaction_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.account_transactions_account_transaction_id_seq OWNER TO test;

--
-- Name: account_transactions_account_transaction_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.account_transactions_account_transaction_id_seq OWNED BY public.account_transactions.account_transaction_id;


--
-- Name: account_transactions_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.account_transactions_aud (
    account_transaction_id integer NOT NULL,
    account_id uuid,
    transaction_id character varying,
    completed_authorised_payment_type character varying,
    credit_debit_type character varying,
    transaction_name character varying,
    type character varying,
    amount double precision,
    transaction_currency character varying,
    transaction_date date,
    partie_cnpj_cpf character varying,
    partie_person_type character varying,
    partie_compe_code character varying,
    partie_branch_code character varying,
    partie_number character varying,
    partie_check_digit character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.account_transactions_aud OWNER TO test;

--
-- Name: accounts; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.accounts (
    reference_id integer NOT NULL,
    account_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    status character varying,
    currency character varying,
    account_type character varying,
    account_sub_type character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    brand_name character varying(80),
    company_cnpj character varying(14),
    compe_code character varying(3),
    branch_code character varying(4),
    number character varying(20),
    check_digit character varying(1),
    available_amount double precision,
    available_amount_currency character varying,
    blocked_amount double precision,
    blocked_amount_currency character varying,
    automatically_invested_amount double precision,
    automatically_invested_amount_currency character varying,
    overdraft_contracted_limit_currency character varying,
    overdraft_used_limit_currency character varying,
    unarranged_overdraft_amount_currency character varying,
    account_holder_id uuid,
    overdraft_contracted_limit double precision,
    overdraft_used_limit double precision,
    unarranged_overdraft_amount double precision,
    debtor_ispb character varying,
    debtor_issuer character varying,
    debtor_type character varying
);


ALTER TABLE public.accounts OWNER TO test;

--
-- Name: accounts_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.accounts_aud (
    reference_id integer NOT NULL,
    account_id uuid,
    status character varying,
    currency character varying,
    account_type character varying,
    account_sub_type character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint,
    brand_name character varying(80),
    company_cnpj character varying(14),
    compe_code character varying(3),
    branch_code character varying(4),
    number character varying(20),
    check_digit character varying(1),
    available_amount double precision,
    available_amount_currency character varying,
    blocked_amount double precision,
    blocked_amount_currency character varying,
    automatically_invested_amount double precision,
    automatically_invested_amount_currency character varying,
    overdraft_contracted_limit_currency character varying,
    overdraft_used_limit_currency character varying,
    unarranged_overdraft_amount_currency character varying,
    account_holder_id uuid,
    overdraft_contracted_limit double precision,
    overdraft_used_limit double precision,
    unarranged_overdraft_amount double precision,
    debtor_ispb character varying,
    debtor_issuer character varying,
    debtor_type character varying
);


ALTER TABLE public.accounts_aud OWNER TO test;

--
-- Name: accounts_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.accounts_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.accounts_reference_id_seq OWNER TO test;

--
-- Name: accounts_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.accounts_reference_id_seq OWNED BY public.accounts.reference_id;


--
-- Name: balloon_payments; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.balloon_payments (
    balloon_payments_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    due_date character varying NOT NULL,
    currency character varying NOT NULL,
    amount double precision NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    contract_id uuid
);


ALTER TABLE public.balloon_payments OWNER TO test;

--
-- Name: balloon_payments_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.balloon_payments_aud (
    balloon_payments_id uuid NOT NULL,
    due_date character varying,
    currency character varying,
    amount double precision,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    rev integer NOT NULL,
    revtype smallint,
    contract_id uuid
);


ALTER TABLE public.balloon_payments_aud OWNER TO test;

--
-- Name: business_emails; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_emails (
    reference_id integer NOT NULL,
    business_identifications_id uuid NOT NULL,
    is_main boolean,
    email character varying(320),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.business_emails OWNER TO test;

--
-- Name: business_emails_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_emails_aud (
    reference_id integer NOT NULL,
    business_identifications_id uuid,
    is_main boolean,
    email character varying(320),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.business_emails_aud OWNER TO test;

--
-- Name: business_emails_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.business_emails_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.business_emails_reference_id_seq OWNER TO test;

--
-- Name: business_emails_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.business_emails_reference_id_seq OWNED BY public.business_emails.reference_id;


--
-- Name: business_financial_relations; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_financial_relations (
    reference_id integer NOT NULL,
    business_financial_relations_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    account_holder_id uuid,
    start_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.business_financial_relations OWNER TO test;

--
-- Name: business_financial_relations_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_financial_relations_aud (
    reference_id integer NOT NULL,
    business_financial_relations_id uuid,
    account_holder_id uuid,
    start_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.business_financial_relations_aud OWNER TO test;

--
-- Name: business_financial_relations_procurators; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_financial_relations_procurators (
    reference_id integer NOT NULL,
    business_financial_relations_id uuid NOT NULL,
    type character varying(19),
    cnpj_cpf_number character varying(11),
    civil_name character varying(70),
    social_name character varying(70),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.business_financial_relations_procurators OWNER TO test;

--
-- Name: business_financial_relations_procurators_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_financial_relations_procurators_aud (
    reference_id integer NOT NULL,
    business_financial_relations_id uuid,
    type character varying(19),
    cnpj_cpf_number character varying(11),
    civil_name character varying(70),
    social_name character varying(70),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.business_financial_relations_procurators_aud OWNER TO test;

--
-- Name: business_financial_relations_procurators_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.business_financial_relations_procurators_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.business_financial_relations_procurators_reference_id_seq OWNER TO test;

--
-- Name: business_financial_relations_procurators_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.business_financial_relations_procurators_reference_id_seq OWNED BY public.business_financial_relations_procurators.reference_id;


--
-- Name: business_financial_relations_products_services_type; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_financial_relations_products_services_type (
    reference_id integer NOT NULL,
    business_financial_relations_id uuid NOT NULL,
    type character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.business_financial_relations_products_services_type OWNER TO test;

--
-- Name: business_financial_relations_products_services_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.business_financial_relations_products_services_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.business_financial_relations_products_services_reference_id_seq OWNER TO test;

--
-- Name: business_financial_relations_products_services_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.business_financial_relations_products_services_reference_id_seq OWNED BY public.business_financial_relations_products_services_type.reference_id;


--
-- Name: business_financial_relations_products_services_type_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_financial_relations_products_services_type_aud (
    reference_id integer NOT NULL,
    business_financial_relations_id uuid,
    type character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.business_financial_relations_products_services_type_aud OWNER TO test;

--
-- Name: business_financial_relations_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.business_financial_relations_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.business_financial_relations_reference_id_seq OWNER TO test;

--
-- Name: business_financial_relations_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.business_financial_relations_reference_id_seq OWNED BY public.business_financial_relations.reference_id;


--
-- Name: business_identifications; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_identifications (
    reference_id integer NOT NULL,
    business_identifications_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    account_holder_id uuid,
    brand_name character varying(80),
    company_name character varying(70),
    trade_name character varying(70),
    incorporation_date date,
    cnpj_number character varying(14),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.business_identifications OWNER TO test;

--
-- Name: business_identifications_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_identifications_aud (
    reference_id integer NOT NULL,
    business_identifications_id uuid,
    account_holder_id uuid,
    brand_name character varying(80),
    company_name character varying(70),
    trade_name character varying(70),
    incorporation_date date,
    cnpj_number character varying(14),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.business_identifications_aud OWNER TO test;

--
-- Name: business_identifications_company_cnpj; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_identifications_company_cnpj (
    reference_id integer NOT NULL,
    business_identifications_id uuid NOT NULL,
    company_cnpj character varying(14),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.business_identifications_company_cnpj OWNER TO test;

--
-- Name: business_identifications_company_cnpj_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_identifications_company_cnpj_aud (
    reference_id integer NOT NULL,
    business_identifications_id uuid NOT NULL,
    company_cnpj character varying(14),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.business_identifications_company_cnpj_aud OWNER TO test;

--
-- Name: business_identifications_company_cnpj_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.business_identifications_company_cnpj_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.business_identifications_company_cnpj_reference_id_seq OWNER TO test;

--
-- Name: business_identifications_company_cnpj_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.business_identifications_company_cnpj_reference_id_seq OWNED BY public.business_identifications_company_cnpj.reference_id;


--
-- Name: business_identifications_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.business_identifications_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.business_identifications_reference_id_seq OWNER TO test;

--
-- Name: business_identifications_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.business_identifications_reference_id_seq OWNED BY public.business_identifications.reference_id;


--
-- Name: business_other_documents; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_other_documents (
    reference_id integer NOT NULL,
    business_identifications_id uuid NOT NULL,
    type character varying(20),
    number character varying(20),
    country character varying(3),
    expiration_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.business_other_documents OWNER TO test;

--
-- Name: business_other_documents_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_other_documents_aud (
    reference_id integer NOT NULL,
    business_identifications_id uuid,
    type character varying(20),
    number character varying(20),
    country character varying(3),
    expiration_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.business_other_documents_aud OWNER TO test;

--
-- Name: business_other_documents_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.business_other_documents_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.business_other_documents_reference_id_seq OWNER TO test;

--
-- Name: business_other_documents_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.business_other_documents_reference_id_seq OWNED BY public.business_other_documents.reference_id;


--
-- Name: business_parties; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_parties (
    reference_id integer NOT NULL,
    business_identifications_id uuid NOT NULL,
    person_type character varying,
    type character varying(13),
    civil_name character varying(70),
    social_name character varying(70),
    company_name character varying(70),
    trade_name character varying(70),
    start_date date,
    shareholding character varying(4),
    document_type character varying,
    document_number character varying(20),
    document_additional_info character varying(100),
    document_country character varying(3),
    document_expiration_date date,
    document_issue_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.business_parties OWNER TO test;

--
-- Name: business_parties_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_parties_aud (
    reference_id integer NOT NULL,
    business_identifications_id uuid,
    person_type character varying,
    type character varying(13),
    civil_name character varying(70),
    social_name character varying(70),
    company_name character varying(70),
    trade_name character varying(70),
    start_date date,
    shareholding character varying(4),
    document_type character varying,
    document_number character varying(20),
    document_additional_info character varying(100),
    document_country character varying(3),
    document_expiration_date date,
    document_issue_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.business_parties_aud OWNER TO test;

--
-- Name: business_parties_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.business_parties_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.business_parties_reference_id_seq OWNER TO test;

--
-- Name: business_parties_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.business_parties_reference_id_seq OWNED BY public.business_parties.reference_id;


--
-- Name: business_phones; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_phones (
    reference_id integer NOT NULL,
    business_identifications_id uuid NOT NULL,
    is_main boolean,
    type character varying(5),
    additional_info character varying(70),
    country_calling_code character varying(4),
    area_code character varying(2),
    number character varying(11),
    phone_extension character varying(5),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.business_phones OWNER TO test;

--
-- Name: business_phones_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_phones_aud (
    reference_id integer NOT NULL,
    business_identifications_id uuid,
    is_main boolean,
    type character varying(5),
    additional_info character varying(70),
    country_calling_code character varying(4),
    area_code character varying(2),
    number character varying(11),
    phone_extension character varying(5),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.business_phones_aud OWNER TO test;

--
-- Name: business_phones_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.business_phones_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.business_phones_reference_id_seq OWNER TO test;

--
-- Name: business_phones_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.business_phones_reference_id_seq OWNED BY public.business_phones.reference_id;


--
-- Name: business_postal_addresses; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_postal_addresses (
    reference_id integer NOT NULL,
    business_identifications_id uuid NOT NULL,
    is_main boolean,
    address character varying(150),
    additional_info character varying(30),
    district_name character varying(50),
    town_name character varying(50),
    ibge_town_code character varying(7),
    country_subdivision character varying,
    post_code character varying(8),
    country character varying(80),
    country_code character varying(3),
    latitude character varying(13),
    longitude character varying(13),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.business_postal_addresses OWNER TO test;

--
-- Name: business_postal_addresses_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_postal_addresses_aud (
    reference_id integer NOT NULL,
    business_identifications_id uuid,
    is_main boolean,
    address character varying(150),
    additional_info character varying(30),
    district_name character varying(50),
    town_name character varying(50),
    ibge_town_code character varying(7),
    country_subdivision character varying,
    post_code character varying(8),
    country character varying(80),
    country_code character varying(3),
    latitude character varying(13),
    longitude character varying(13),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.business_postal_addresses_aud OWNER TO test;

--
-- Name: business_postal_addresses_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.business_postal_addresses_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.business_postal_addresses_reference_id_seq OWNER TO test;

--
-- Name: business_postal_addresses_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.business_postal_addresses_reference_id_seq OWNED BY public.business_postal_addresses.reference_id;


--
-- Name: business_qualifications; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_qualifications (
    reference_id integer NOT NULL,
    business_qualifications_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    account_holder_id uuid,
    informed_revenue_frequency character varying,
    informed_revenue_frequency_additional_information character varying(100),
    informed_revenue_amount numeric,
    informed_revenue_currency character varying(3),
    informed_revenue_year integer,
    informed_patrimony_currency character varying(3),
    informed_patrimony_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    informed_patrimony_amount double precision
);


ALTER TABLE public.business_qualifications OWNER TO test;

--
-- Name: business_qualifications_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_qualifications_aud (
    reference_id integer NOT NULL,
    business_qualifications_id uuid,
    account_holder_id uuid,
    informed_revenue_frequency character varying,
    informed_revenue_frequency_additional_information character varying(100),
    informed_revenue_amount numeric,
    informed_revenue_currency character varying(3),
    informed_revenue_year integer,
    informed_patrimony_currency character varying(3),
    informed_patrimony_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint,
    informed_patrimony_amount double precision
);


ALTER TABLE public.business_qualifications_aud OWNER TO test;

--
-- Name: business_qualifications_economic_activities; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_qualifications_economic_activities (
    reference_id integer NOT NULL,
    business_qualifications_id uuid NOT NULL,
    code integer,
    is_main boolean,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.business_qualifications_economic_activities OWNER TO test;

--
-- Name: business_qualifications_economic_activities_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.business_qualifications_economic_activities_aud (
    reference_id integer NOT NULL,
    business_qualifications_id uuid,
    code integer,
    is_main boolean,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.business_qualifications_economic_activities_aud OWNER TO test;

--
-- Name: business_qualifications_economic_activities_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.business_qualifications_economic_activities_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.business_qualifications_economic_activities_reference_id_seq OWNER TO test;

--
-- Name: business_qualifications_economic_activities_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.business_qualifications_economic_activities_reference_id_seq OWNED BY public.business_qualifications_economic_activities.reference_id;


--
-- Name: business_qualifications_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.business_qualifications_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.business_qualifications_reference_id_seq OWNER TO test;

--
-- Name: business_qualifications_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.business_qualifications_reference_id_seq OWNED BY public.business_qualifications.reference_id;


--
-- Name: consent_accounts; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.consent_accounts (
    reference_id integer NOT NULL,
    consent_id character varying,
    account_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.consent_accounts OWNER TO test;

--
-- Name: consent_accounts_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.consent_accounts_aud (
    reference_id integer NOT NULL,
    consent_id character varying,
    account_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.consent_accounts_aud OWNER TO test;

--
-- Name: consent_accounts_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.consent_accounts_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.consent_accounts_reference_id_seq OWNER TO test;

--
-- Name: consent_accounts_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.consent_accounts_reference_id_seq OWNED BY public.consent_accounts.reference_id;


--
-- Name: consent_contracts; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.consent_contracts (
    reference_id integer NOT NULL,
    consent_id character varying,
    contract_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.consent_contracts OWNER TO test;

--
-- Name: consent_contracts_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.consent_contracts_aud (
    reference_id integer NOT NULL,
    consent_id character varying,
    contract_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.consent_contracts_aud OWNER TO test;

--
-- Name: consent_contracts_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.consent_contracts_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.consent_contracts_reference_id_seq OWNER TO test;

--
-- Name: consent_contracts_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.consent_contracts_reference_id_seq OWNED BY public.consent_contracts.reference_id;


--
-- Name: consent_credit_card_accounts; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.consent_credit_card_accounts (
    reference_id integer NOT NULL,
    consent_id character varying,
    credit_card_account_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.consent_credit_card_accounts OWNER TO test;

--
-- Name: consent_credit_card_accounts_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.consent_credit_card_accounts_aud (
    reference_id integer NOT NULL,
    consent_id character varying,
    credit_card_account_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.consent_credit_card_accounts_aud OWNER TO test;

--
-- Name: consent_credit_card_accounts_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.consent_credit_card_accounts_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.consent_credit_card_accounts_reference_id_seq OWNER TO test;

--
-- Name: consent_credit_card_accounts_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.consent_credit_card_accounts_reference_id_seq OWNED BY public.consent_credit_card_accounts.reference_id;


--
-- Name: consent_permissions; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.consent_permissions (
    reference_id integer NOT NULL,
    permission character varying NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    consent_id text
);


ALTER TABLE public.consent_permissions OWNER TO test;

--
-- Name: consent_permissions_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.consent_permissions_aud (
    reference_id integer NOT NULL,
    permission character varying,
    consent_id text,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint,
    new_consent_id text
);


ALTER TABLE public.consent_permissions_aud OWNER TO test;

--
-- Name: consent_permissions_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.consent_permissions_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.consent_permissions_reference_id_seq OWNER TO test;

--
-- Name: consent_permissions_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.consent_permissions_reference_id_seq OWNED BY public.consent_permissions.reference_id;


--
-- Name: consents; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.consents (
    reference_id integer NOT NULL,
    expiration_date_time timestamp without time zone,
    transaction_from_date_time timestamp without time zone,
    transaction_to_date_time timestamp without time zone,
    creation_date_time timestamp without time zone,
    status_update_date_time timestamp without time zone NOT NULL,
    status character varying NOT NULL,
    client_id character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    consent_id text,
    account_holder_id uuid,
    business_document_identification character varying,
    business_document_rel character varying
);


ALTER TABLE public.consents OWNER TO test;

--
-- Name: consents_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.consents_aud (
    reference_id integer NOT NULL,
    expiration_date_time timestamp without time zone,
    transaction_from_date_time timestamp without time zone,
    transaction_to_date_time timestamp without time zone,
    creation_date_time timestamp without time zone,
    status_update_date_time timestamp without time zone,
    status character varying,
    risk character varying,
    client_id character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint,
    consent_id text,
    account_holder_id uuid,
    business_document_identification character varying,
    business_document_rel character varying
);


ALTER TABLE public.consents_aud OWNER TO test;

--
-- Name: consents_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.consents_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.consents_reference_id_seq OWNER TO test;

--
-- Name: consents_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.consents_reference_id_seq OWNED BY public.consents.reference_id;


--
-- Name: contracted_fees; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.contracted_fees (
    contracted_fees_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    fee_name character varying,
    fee_code character varying,
    fee_charge_type character varying,
    fee_charge character varying,
    fee_amount double precision,
    fee_rate double precision,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    contract_id uuid NOT NULL
);


ALTER TABLE public.contracted_fees OWNER TO test;

--
-- Name: contracted_fees_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.contracted_fees_aud (
    contracted_fees_id uuid NOT NULL,
    fee_name character varying,
    fee_code character varying,
    fee_charge_type character varying,
    fee_charge character varying,
    fee_amount double precision,
    fee_rate double precision,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    contract_id uuid,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.contracted_fees_aud OWNER TO test;

--
-- Name: contracted_finance_charges; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.contracted_finance_charges (
    contracted_finance_charges_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    charge_type character varying,
    charge_additional_info character varying,
    charge_rate double precision,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    contract_id uuid NOT NULL
);


ALTER TABLE public.contracted_finance_charges OWNER TO test;

--
-- Name: contracted_finance_charges_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.contracted_finance_charges_aud (
    contracted_finance_charges_id uuid NOT NULL,
    charge_type character varying,
    charge_additional_info character varying,
    charge_rate double precision,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    contract_id uuid,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.contracted_finance_charges_aud OWNER TO test;

--
-- Name: contracts; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.contracts (
    contract_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    contract_type character varying,
    company_cnpj character varying,
    product_name character varying,
    product_type character varying,
    product_sub_type character varying,
    contract_amount double precision,
    currency character varying,
    instalment_periodicity character varying,
    instalment_periodicity_additional_info character varying,
    cet double precision,
    amortization_scheduled character varying,
    amortization_scheduled_additional_info character varying,
    ipoc_code character varying NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    account_holder_id uuid,
    paid_instalments integer,
    contract_outstanding_balance double precision,
    type_number_of_instalments character varying,
    total_number_of_instalments integer,
    type_contract_remaining character varying,
    contract_remaining_number integer,
    due_instalments integer,
    past_due_instalments integer,
    status character varying,
    contract_date date,
    disbursement_date date,
    settlement_date date,
    due_date date,
    first_instalment_due_date date,
    contract_number character varying
);


ALTER TABLE public.contracts OWNER TO test;

--
-- Name: contracts_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.contracts_aud (
    contract_id uuid NOT NULL,
    contract_type character varying,
    company_cnpj character varying,
    product_name character varying,
    product_type character varying,
    product_sub_type character varying,
    contract_amount double precision,
    currency character varying,
    instalment_periodicity character varying,
    instalment_periodicity_additional_info character varying,
    cet double precision,
    amortization_scheduled character varying,
    amortization_scheduled_additional_info character varying,
    ipoc_code character varying,
    created_by character varying,
    updated_by character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    rev integer NOT NULL,
    revtype smallint,
    account_holder_id uuid,
    paid_instalments integer,
    contract_outstanding_balance double precision,
    type_number_of_instalments character varying,
    total_number_of_instalments integer,
    type_contract_remaining character varying,
    contract_remaining_number integer,
    due_instalments integer,
    past_due_instalments integer,
    status character varying,
    contract_date date,
    disbursement_date date,
    settlement_date date,
    due_date date,
    first_instalment_due_date date,
    contract_number character varying
);


ALTER TABLE public.contracts_aud OWNER TO test;

--
-- Name: credit_card_accounts; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_card_accounts (
    credit_card_account_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    brand_name character varying(80),
    company_cnpj character varying(14),
    name character varying(50),
    product_type character varying(26),
    product_additional_info character varying(50),
    credit_card_network character varying(17),
    network_additional_info character varying(50),
    status character varying,
    account_holder_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.credit_card_accounts OWNER TO test;

--
-- Name: credit_card_accounts_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_card_accounts_aud (
    credit_card_account_id uuid NOT NULL,
    brand_name character varying(80),
    company_cnpj character varying(14),
    name character varying(50),
    product_type character varying(26),
    product_additional_info character varying(50),
    credit_card_network character varying(17),
    network_additional_info character varying(50),
    status character varying,
    account_holder_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.credit_card_accounts_aud OWNER TO test;

--
-- Name: credit_card_accounts_bills; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_card_accounts_bills (
    bill_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    due_date date,
    bill_total_amount double precision,
    bill_total_amount_currency character varying(3),
    bill_minimum_amount double precision,
    bill_minimum_amount_currency character varying(3),
    is_instalment boolean,
    credit_card_account_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.credit_card_accounts_bills OWNER TO test;

--
-- Name: credit_card_accounts_bills_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_card_accounts_bills_aud (
    bill_id uuid NOT NULL,
    due_date date,
    bill_total_amount double precision,
    bill_total_amount_currency character varying(3),
    bill_minimum_amount double precision,
    bill_minimum_amount_currency character varying(3),
    is_instalment boolean,
    credit_card_account_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.credit_card_accounts_bills_aud OWNER TO test;

--
-- Name: credit_card_accounts_bills_finance_charge; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_card_accounts_bills_finance_charge (
    finance_charge_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    type character varying(44),
    additional_info character varying(140),
    amount double precision,
    currency character varying(3),
    bill_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.credit_card_accounts_bills_finance_charge OWNER TO test;

--
-- Name: credit_card_accounts_bills_finance_charge_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_card_accounts_bills_finance_charge_aud (
    finance_charge_id uuid NOT NULL,
    type character varying(44),
    additional_info character varying(140),
    amount double precision,
    currency character varying(3),
    bill_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.credit_card_accounts_bills_finance_charge_aud OWNER TO test;

--
-- Name: credit_card_accounts_bills_payment; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_card_accounts_bills_payment (
    payment_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    value_type character varying(32),
    payment_date date,
    payment_mode character varying(21),
    amount double precision,
    currency character varying(3),
    bill_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.credit_card_accounts_bills_payment OWNER TO test;

--
-- Name: credit_card_accounts_bills_payment_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_card_accounts_bills_payment_aud (
    payment_id uuid NOT NULL,
    value_type character varying(32),
    payment_date date,
    payment_mode character varying(21),
    amount double precision,
    currency character varying(3),
    bill_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.credit_card_accounts_bills_payment_aud OWNER TO test;

--
-- Name: credit_card_accounts_limits; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_card_accounts_limits (
    limit_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    credit_line_limit_type character varying(34),
    consolidation_type character varying(11),
    identification_number character varying(100),
    line_name character varying(28),
    line_name_additional_info character varying,
    is_limit_flexible boolean,
    limit_amount_currency character varying(3),
    limit_amount double precision,
    used_amount_currency character varying(3),
    used_amount double precision,
    available_amount_currency character varying(3),
    available_amount double precision,
    credit_card_account_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.credit_card_accounts_limits OWNER TO test;

--
-- Name: credit_card_accounts_limits_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_card_accounts_limits_aud (
    limit_id uuid NOT NULL,
    credit_line_limit_type character varying(34),
    consolidation_type character varying(11),
    identification_number character varying(100),
    line_name character varying(28),
    line_name_additional_info character varying,
    is_limit_flexible boolean,
    limit_amount_currency character varying(3),
    limit_amount double precision,
    used_amount_currency character varying(3),
    used_amount double precision,
    available_amount_currency character varying(3),
    available_amount double precision,
    credit_card_account_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.credit_card_accounts_limits_aud OWNER TO test;

--
-- Name: credit_card_accounts_transaction; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_card_accounts_transaction (
    transaction_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    identification_number character varying(100),
    line_name character varying(28),
    transaction_name character varying(100),
    bill_id uuid,
    credit_debit_type character varying(7),
    transaction_type character varying(36),
    transactional_additional_info character varying(140),
    payment_type character varying(7),
    fee_type character varying(29),
    fee_type_additional_info character varying(140),
    other_credits_type character varying(19),
    other_credits_additional_info character varying(50),
    charge_identificator character varying(50),
    charge_number bigint,
    brazilian_amount double precision,
    amount double precision,
    currency character varying(3),
    transaction_date date,
    bill_post_date date,
    payee_mcc bigint,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    credit_card_account_id uuid
);


ALTER TABLE public.credit_card_accounts_transaction OWNER TO test;

--
-- Name: credit_card_accounts_transaction_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_card_accounts_transaction_aud (
    transaction_id uuid NOT NULL,
    identification_number character varying(100),
    line_name character varying(28),
    transaction_name character varying(100),
    bill_id uuid,
    credit_debit_type character varying(7),
    transaction_type character varying(36),
    transactional_additional_info character varying(140),
    payment_type character varying(7),
    fee_type character varying(29),
    fee_type_additional_info character varying(140),
    other_credits_type character varying(19),
    other_credits_additional_info character varying(50),
    charge_identificator character varying(50),
    charge_number bigint,
    brazilian_amount double precision,
    amount double precision,
    currency character varying(3),
    transaction_date date,
    bill_post_date date,
    payee_mcc bigint,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint,
    credit_card_account_id uuid
);


ALTER TABLE public.credit_card_accounts_transaction_aud OWNER TO test;

--
-- Name: credit_cards_account_payment_method; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_cards_account_payment_method (
    payment_method_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    identification_number character varying,
    is_multiple_credit_card boolean,
    credit_card_account_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.credit_cards_account_payment_method OWNER TO test;

--
-- Name: credit_cards_account_payment_method_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.credit_cards_account_payment_method_aud (
    payment_method_id uuid NOT NULL,
    identification_number character varying,
    is_multiple_credit_card boolean,
    credit_card_account_id uuid,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.credit_cards_account_payment_method_aud OWNER TO test;

--
-- Name: creditor_accounts; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.creditor_accounts (
    creditor_account_id integer NOT NULL,
    ispb character varying,
    issuer character varying,
    number character varying,
    account_type character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.creditor_accounts OWNER TO test;

--
-- Name: creditor_accounts_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.creditor_accounts_aud (
    creditor_account_id integer NOT NULL,
    ispb character varying,
    issuer character varying,
    number character varying,
    account_type character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.creditor_accounts_aud OWNER TO test;

--
-- Name: creditor_accounts_creditor_account_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.creditor_accounts_creditor_account_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.creditor_accounts_creditor_account_id_seq OWNER TO test;

--
-- Name: creditor_accounts_creditor_account_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.creditor_accounts_creditor_account_id_seq OWNED BY public.creditor_accounts.creditor_account_id;


--
-- Name: creditors; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.creditors (
    creditor_id integer NOT NULL,
    person_type character varying,
    cpf_cnpj character varying,
    name character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.creditors OWNER TO test;

--
-- Name: creditors_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.creditors_aud (
    creditor_id integer NOT NULL,
    person_type character varying,
    cpf_cnpj character varying,
    name character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.creditors_aud OWNER TO test;

--
-- Name: creditors_creditor_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.creditors_creditor_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.creditors_creditor_id_seq OWNER TO test;

--
-- Name: creditors_creditor_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.creditors_creditor_id_seq OWNED BY public.creditors.creditor_id;


--
-- Name: flyway_schema_history; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.flyway_schema_history (
    installed_rank integer NOT NULL,
    version character varying(50),
    description character varying(200) NOT NULL,
    type character varying(20) NOT NULL,
    script character varying(1000) NOT NULL,
    checksum integer,
    installed_by character varying(100) NOT NULL,
    installed_on timestamp without time zone DEFAULT now() NOT NULL,
    execution_time integer NOT NULL,
    success boolean NOT NULL
);


ALTER TABLE public.flyway_schema_history OWNER TO test;

--
-- Name: hibernate_sequence; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.hibernate_sequence
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.hibernate_sequence OWNER TO test;

--
-- Name: interest_rates; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.interest_rates (
    interest_rates_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    tax_type character varying,
    interest_rate_type character varying,
    tax_periodicity character varying,
    calculation character varying,
    referential_rate_indexer_type character varying,
    referential_rate_indexer_sub_type character varying,
    referential_rate_indexer_additional_info character varying,
    pre_fixed_rate double precision,
    post_fixed_rate double precision,
    additional_info character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    contract_id uuid NOT NULL
);


ALTER TABLE public.interest_rates OWNER TO test;

--
-- Name: interest_rates_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.interest_rates_aud (
    interest_rates_id uuid NOT NULL,
    tax_type character varying,
    interest_rate_type character varying,
    tax_periodicity character varying,
    calculation character varying,
    referential_rate_indexer_type character varying,
    referential_rate_indexer_sub_type character varying,
    referential_rate_indexer_additional_info character varying,
    pre_fixed_rate double precision,
    post_fixed_rate double precision,
    additional_info character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    contract_id uuid,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.interest_rates_aud OWNER TO test;

--
-- Name: jti; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.jti (
    id integer NOT NULL,
    jti character varying NOT NULL,
    created_date timestamp without time zone
);


ALTER TABLE public.jti OWNER TO test;

--
-- Name: jti_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.jti_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.jti_id_seq OWNER TO test;

--
-- Name: jti_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.jti_id_seq OWNED BY public.jti.id;


--
-- Name: logged_in_user_entity_documents; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.logged_in_user_entity_documents (
    logged_in_user_entity_document_id integer NOT NULL,
    identification character varying,
    rel character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.logged_in_user_entity_documents OWNER TO test;

--
-- Name: logged_in_user_entity_documen_logged_in_user_entity_documen_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.logged_in_user_entity_documen_logged_in_user_entity_documen_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.logged_in_user_entity_documen_logged_in_user_entity_documen_seq OWNER TO test;

--
-- Name: logged_in_user_entity_documen_logged_in_user_entity_documen_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.logged_in_user_entity_documen_logged_in_user_entity_documen_seq OWNED BY public.logged_in_user_entity_documents.logged_in_user_entity_document_id;


--
-- Name: logged_in_user_entity_documents_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.logged_in_user_entity_documents_aud (
    logged_in_user_entity_document_id integer NOT NULL,
    identification character varying,
    rel character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    rev integer NOT NULL,
    revtype smallint,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.logged_in_user_entity_documents_aud OWNER TO test;

--
-- Name: over_parcel_charges; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.over_parcel_charges (
    over_parcel_charges_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    charge_type character varying NOT NULL,
    charge_additional_info character varying NOT NULL,
    charge_amount double precision NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    releases_id uuid NOT NULL
);


ALTER TABLE public.over_parcel_charges OWNER TO test;

--
-- Name: over_parcel_charges_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.over_parcel_charges_aud (
    over_parcel_charges_id uuid NOT NULL,
    charge_type character varying,
    charge_additional_info character varying,
    charge_amount double precision,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    releases_id uuid NOT NULL,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.over_parcel_charges_aud OWNER TO test;

--
-- Name: over_parcel_fees; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.over_parcel_fees (
    over_parcel_fees_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    fee_name character varying NOT NULL,
    fee_code character varying NOT NULL,
    fee_amount double precision NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    releases_id uuid NOT NULL
);


ALTER TABLE public.over_parcel_fees OWNER TO test;

--
-- Name: over_parcel_fees_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.over_parcel_fees_aud (
    over_parcel_fees_id uuid NOT NULL,
    fee_name character varying,
    fee_code character varying,
    fee_amount double precision,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    releases_id uuid NOT NULL,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.over_parcel_fees_aud OWNER TO test;

--
-- Name: payment_consent_details; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.payment_consent_details (
    payment_consent_details_id integer NOT NULL,
    local_instrument character varying,
    qr_code character varying,
    proxy character varying,
    creditor_ispb character varying,
    creditor_issuer character varying,
    creditor_account_number character varying,
    creditor_account_type character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.payment_consent_details OWNER TO test;

--
-- Name: payment_consent_details_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.payment_consent_details_aud (
    payment_consent_details_id integer NOT NULL,
    local_instrument character varying,
    qr_code character varying,
    proxy character varying,
    creditor_ispb character varying,
    creditor_issuer character varying,
    creditor_account_number character varying,
    creditor_account_type character varying,
    rev integer NOT NULL,
    revtype smallint,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.payment_consent_details_aud OWNER TO test;

--
-- Name: payment_consent_details_aud_payment_consent_details_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.payment_consent_details_aud_payment_consent_details_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.payment_consent_details_aud_payment_consent_details_id_seq OWNER TO test;

--
-- Name: payment_consent_details_aud_payment_consent_details_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.payment_consent_details_aud_payment_consent_details_id_seq OWNED BY public.payment_consent_details_aud.payment_consent_details_id;


--
-- Name: payment_consent_details_payment_consent_details_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.payment_consent_details_payment_consent_details_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.payment_consent_details_payment_consent_details_id_seq OWNER TO test;

--
-- Name: payment_consent_details_payment_consent_details_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.payment_consent_details_payment_consent_details_id_seq OWNED BY public.payment_consent_details.payment_consent_details_id;


--
-- Name: payment_consent_payments; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.payment_consent_payments (
    payment_id integer NOT NULL,
    payment_type character varying,
    payment_date timestamp without time zone,
    currency character varying,
    amount character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    payment_consent_details_id integer NOT NULL,
    schedule timestamp without time zone
);


ALTER TABLE public.payment_consent_payments OWNER TO test;

--
-- Name: payment_consent_payments_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.payment_consent_payments_aud (
    payment_id integer NOT NULL,
    payment_type character varying,
    payment_date timestamp without time zone,
    currency character varying,
    amount character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint,
    payment_consent_details_id integer NOT NULL,
    schedule timestamp without time zone
);


ALTER TABLE public.payment_consent_payments_aud OWNER TO test;

--
-- Name: payment_consent_payments_aud_payment_consent_details_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.payment_consent_payments_aud_payment_consent_details_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.payment_consent_payments_aud_payment_consent_details_id_seq OWNER TO test;

--
-- Name: payment_consent_payments_aud_payment_consent_details_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.payment_consent_payments_aud_payment_consent_details_id_seq OWNED BY public.payment_consent_payments_aud.payment_consent_details_id;


--
-- Name: payment_consent_payments_payment_consent_details_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.payment_consent_payments_payment_consent_details_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.payment_consent_payments_payment_consent_details_id_seq OWNER TO test;

--
-- Name: payment_consent_payments_payment_consent_details_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.payment_consent_payments_payment_consent_details_id_seq OWNED BY public.payment_consent_payments.payment_consent_details_id;


--
-- Name: payment_consent_payments_payment_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.payment_consent_payments_payment_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.payment_consent_payments_payment_id_seq OWNER TO test;

--
-- Name: payment_consent_payments_payment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.payment_consent_payments_payment_id_seq OWNED BY public.payment_consent_payments.payment_id;


--
-- Name: payment_consents; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.payment_consents (
    reference_id integer NOT NULL,
    payment_consent_id character varying,
    client_id character varying,
    status character varying,
    creditor_id integer,
    payment_id integer,
    creation_date_time timestamp without time zone,
    expiration_date_time timestamp without time zone,
    status_update_date_time timestamp without time zone NOT NULL,
    idempotency_key character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    account_holder_id uuid,
    account_id uuid,
    business_document_identification character varying,
    business_document_rel character varying
);


ALTER TABLE public.payment_consents OWNER TO test;

--
-- Name: payment_consents_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.payment_consents_aud (
    reference_id integer NOT NULL,
    payment_consent_id character varying,
    client_id character varying,
    status character varying,
    creditor_id integer,
    payment_id integer,
    creation_date_time timestamp without time zone,
    expiration_date_time timestamp without time zone,
    status_update_date_time timestamp without time zone,
    idempotency_key character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint,
    account_holder_id uuid,
    account_id uuid,
    business_document_identification character varying,
    business_document_rel character varying
);


ALTER TABLE public.payment_consents_aud OWNER TO test;

--
-- Name: payment_consents_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.payment_consents_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.payment_consents_reference_id_seq OWNER TO test;

--
-- Name: payment_consents_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.payment_consents_reference_id_seq OWNED BY public.payment_consents.reference_id;


--
-- Name: payments_simulate_response; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.payments_simulate_response (
    id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    client_id character varying NOT NULL,
    payment_consent_id character varying,
    request_time timestamp without time zone NOT NULL,
    request_end_time timestamp without time zone NOT NULL,
    http_status character varying,
    http_error_message character varying,
    duration integer,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.payments_simulate_response OWNER TO test;

--
-- Name: payments_simulate_response_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.payments_simulate_response_aud (
    id uuid NOT NULL,
    client_id character varying,
    payment_consent_id character varying,
    http_status character varying,
    http_error_message character varying,
    duration integer,
    request_time timestamp without time zone,
    request_end_time timestamp without time zone,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.payments_simulate_response_aud OWNER TO test;

--
-- Name: personal_emails; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_emails (
    reference_id integer NOT NULL,
    personal_identifications_id uuid NOT NULL,
    is_main boolean,
    email character varying(320),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.personal_emails OWNER TO test;

--
-- Name: personal_emails_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_emails_aud (
    reference_id integer NOT NULL,
    personal_identifications_id uuid NOT NULL,
    is_main boolean,
    email character varying(320),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.personal_emails_aud OWNER TO test;

--
-- Name: personal_emails_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.personal_emails_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_emails_reference_id_seq OWNER TO test;

--
-- Name: personal_emails_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.personal_emails_reference_id_seq OWNED BY public.personal_emails.reference_id;


--
-- Name: personal_filiation; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_filiation (
    reference_id integer NOT NULL,
    personal_identifications_id uuid NOT NULL,
    type character varying,
    civil_name character varying(70),
    social_name character varying(70),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.personal_filiation OWNER TO test;

--
-- Name: personal_filiation_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_filiation_aud (
    reference_id integer NOT NULL,
    personal_identifications_id uuid,
    type character varying,
    civil_name character varying(70),
    social_name character varying(70),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.personal_filiation_aud OWNER TO test;

--
-- Name: personal_filiation_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.personal_filiation_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_filiation_reference_id_seq OWNER TO test;

--
-- Name: personal_filiation_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.personal_filiation_reference_id_seq OWNED BY public.personal_filiation.reference_id;


--
-- Name: personal_financial_relations; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_financial_relations (
    reference_id integer NOT NULL,
    personal_financial_relations_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    account_holder_id uuid,
    start_date date,
    products_services_type_additional_info character varying(100),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.personal_financial_relations OWNER TO test;

--
-- Name: personal_financial_relations_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_financial_relations_aud (
    reference_id integer NOT NULL,
    personal_financial_relations_id uuid,
    account_holder_id uuid,
    start_date date,
    products_services_type_additional_info character varying(100),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.personal_financial_relations_aud OWNER TO test;

--
-- Name: personal_financial_relations_procurators; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_financial_relations_procurators (
    reference_id integer NOT NULL,
    personal_financial_relations_id uuid NOT NULL,
    type character varying(19),
    cpf_number character varying(11),
    civil_name character varying(70),
    social_name character varying(70),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.personal_financial_relations_procurators OWNER TO test;

--
-- Name: personal_financial_relations_procurators_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_financial_relations_procurators_aud (
    reference_id integer NOT NULL,
    personal_financial_relations_id uuid,
    type character varying(19),
    cpf_number character varying(11),
    civil_name character varying(70),
    social_name character varying(70),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.personal_financial_relations_procurators_aud OWNER TO test;

--
-- Name: personal_financial_relations_procurators_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.personal_financial_relations_procurators_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_financial_relations_procurators_reference_id_seq OWNER TO test;

--
-- Name: personal_financial_relations_procurators_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.personal_financial_relations_procurators_reference_id_seq OWNED BY public.personal_financial_relations_procurators.reference_id;


--
-- Name: personal_financial_relations_products_services_type; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_financial_relations_products_services_type (
    reference_id integer NOT NULL,
    personal_financial_relations_id uuid NOT NULL,
    type character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.personal_financial_relations_products_services_type OWNER TO test;

--
-- Name: personal_financial_relations_products_services_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.personal_financial_relations_products_services_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_financial_relations_products_services_reference_id_seq OWNER TO test;

--
-- Name: personal_financial_relations_products_services_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.personal_financial_relations_products_services_reference_id_seq OWNED BY public.personal_financial_relations_products_services_type.reference_id;


--
-- Name: personal_financial_relations_products_services_type_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_financial_relations_products_services_type_aud (
    reference_id integer NOT NULL,
    personal_financial_relations_id uuid,
    type character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.personal_financial_relations_products_services_type_aud OWNER TO test;

--
-- Name: personal_financial_relations_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.personal_financial_relations_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_financial_relations_reference_id_seq OWNER TO test;

--
-- Name: personal_financial_relations_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.personal_financial_relations_reference_id_seq OWNED BY public.personal_financial_relations.reference_id;


--
-- Name: personal_identifications; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_identifications (
    reference_id integer NOT NULL,
    personal_identifications_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    account_holder_id uuid,
    brand_name character varying(80),
    civil_name character varying(70),
    social_name character varying(70),
    birth_date date,
    marital_status_code character varying,
    marital_status_additional_info character varying,
    sex character varying,
    has_brazilian_nationality boolean,
    cpf_number character varying(11),
    passport_number character varying(20),
    passport_country character varying(3),
    passport_expiration_date date,
    passport_issue_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.personal_identifications OWNER TO test;

--
-- Name: personal_identifications_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_identifications_aud (
    reference_id integer NOT NULL,
    personal_identifications_id uuid,
    account_holder_id uuid,
    brand_name character varying(80),
    civil_name character varying(70),
    social_name character varying(70),
    birth_date date,
    marital_status_code character varying,
    marital_status_additional_info character varying,
    sex character varying,
    has_brazilian_nationality boolean,
    cpf_number character varying(11),
    passport_number character varying(20),
    passport_country character varying(3),
    passport_expiration_date date,
    passport_issue_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.personal_identifications_aud OWNER TO test;

--
-- Name: personal_identifications_company_cnpj; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_identifications_company_cnpj (
    reference_id integer NOT NULL,
    personal_identifications_id uuid NOT NULL,
    company_cnpj character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.personal_identifications_company_cnpj OWNER TO test;

--
-- Name: personal_identifications_company_cnpj_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_identifications_company_cnpj_aud (
    reference_id integer NOT NULL,
    personal_identifications_id uuid,
    company_cnpj character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.personal_identifications_company_cnpj_aud OWNER TO test;

--
-- Name: personal_identifications_company_cnpj_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.personal_identifications_company_cnpj_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_identifications_company_cnpj_reference_id_seq OWNER TO test;

--
-- Name: personal_identifications_company_cnpj_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.personal_identifications_company_cnpj_reference_id_seq OWNED BY public.personal_identifications_company_cnpj.reference_id;


--
-- Name: personal_identifications_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.personal_identifications_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_identifications_reference_id_seq OWNER TO test;

--
-- Name: personal_identifications_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.personal_identifications_reference_id_seq OWNED BY public.personal_identifications.reference_id;


--
-- Name: personal_nationality; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_nationality (
    reference_id integer NOT NULL,
    personal_nationality_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    personal_identifications_id uuid NOT NULL,
    other_nationalities_info character varying(40),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.personal_nationality OWNER TO test;

--
-- Name: personal_nationality_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_nationality_aud (
    reference_id integer NOT NULL,
    personal_nationality_id uuid,
    personal_identifications_id uuid,
    other_nationalities_info character varying(40),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.personal_nationality_aud OWNER TO test;

--
-- Name: personal_nationality_documents; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_nationality_documents (
    reference_id integer NOT NULL,
    personal_nationality_id uuid NOT NULL,
    type character varying,
    number character varying(11),
    expiration_date date,
    issue_date date,
    country character varying(80),
    type_additional_info character varying(70),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.personal_nationality_documents OWNER TO test;

--
-- Name: personal_nationality_documents_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_nationality_documents_aud (
    reference_id integer NOT NULL,
    personal_nationality_id uuid,
    type character varying,
    number character varying(11),
    expiration_date date,
    issue_date date,
    country character varying(80),
    type_additional_info character varying(70),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.personal_nationality_documents_aud OWNER TO test;

--
-- Name: personal_nationality_documents_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.personal_nationality_documents_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_nationality_documents_reference_id_seq OWNER TO test;

--
-- Name: personal_nationality_documents_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.personal_nationality_documents_reference_id_seq OWNED BY public.personal_nationality_documents.reference_id;


--
-- Name: personal_nationality_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.personal_nationality_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_nationality_reference_id_seq OWNER TO test;

--
-- Name: personal_nationality_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.personal_nationality_reference_id_seq OWNED BY public.personal_nationality.reference_id;


--
-- Name: personal_other_documents; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_other_documents (
    reference_id integer NOT NULL,
    personal_identifications_id uuid NOT NULL,
    type character varying,
    type_additional_info character varying(70),
    number character varying(11),
    check_digit character varying(2),
    additional_info character varying(50),
    expiration_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.personal_other_documents OWNER TO test;

--
-- Name: personal_other_documents_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_other_documents_aud (
    reference_id integer NOT NULL,
    personal_identifications_id uuid,
    type character varying,
    type_additional_info character varying(70),
    number character varying(11),
    check_digit character varying(2),
    additional_info character varying(50),
    expiration_date date,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.personal_other_documents_aud OWNER TO test;

--
-- Name: personal_other_documents_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.personal_other_documents_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_other_documents_reference_id_seq OWNER TO test;

--
-- Name: personal_other_documents_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.personal_other_documents_reference_id_seq OWNED BY public.personal_other_documents.reference_id;


--
-- Name: personal_phones; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_phones (
    reference_id integer NOT NULL,
    personal_identifications_id uuid NOT NULL,
    is_main boolean,
    type character varying(5),
    additional_info character varying(70),
    country_calling_code character varying(4),
    area_code character varying(2),
    number character varying(11),
    phone_extension character varying(5),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.personal_phones OWNER TO test;

--
-- Name: personal_phones_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_phones_aud (
    reference_id integer NOT NULL,
    personal_identifications_id uuid,
    is_main boolean,
    type character varying(5),
    additional_info character varying(70),
    country_calling_code character varying(4),
    area_code character varying(2),
    number character varying(11),
    phone_extension character varying(5),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.personal_phones_aud OWNER TO test;

--
-- Name: personal_phones_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.personal_phones_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_phones_reference_id_seq OWNER TO test;

--
-- Name: personal_phones_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.personal_phones_reference_id_seq OWNED BY public.personal_phones.reference_id;


--
-- Name: personal_postal_addresses; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_postal_addresses (
    reference_id integer NOT NULL,
    personal_identifications_id uuid NOT NULL,
    is_main boolean,
    address character varying(150),
    additional_info character varying(30),
    district_name character varying(50),
    town_name character varying(50),
    ibge_town_code character varying(7),
    country_subdivision character varying,
    post_code character varying(8),
    country character varying(80),
    country_code character varying(3),
    latitude character varying(13),
    longitude character varying(13),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.personal_postal_addresses OWNER TO test;

--
-- Name: personal_postal_addresses_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_postal_addresses_aud (
    reference_id integer NOT NULL,
    personal_identifications_id uuid,
    is_main boolean,
    address character varying(150),
    additional_info character varying(30),
    district_name character varying(50),
    town_name character varying(50),
    ibge_town_code character varying(7),
    country_subdivision character varying,
    post_code character varying(8),
    country character varying(80),
    country_code character varying(3),
    latitude character varying(13),
    longitude character varying(13),
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.personal_postal_addresses_aud OWNER TO test;

--
-- Name: personal_postal_addresses_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.personal_postal_addresses_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_postal_addresses_reference_id_seq OWNER TO test;

--
-- Name: personal_postal_addresses_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.personal_postal_addresses_reference_id_seq OWNED BY public.personal_postal_addresses.reference_id;


--
-- Name: personal_qualifications; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_qualifications (
    reference_id integer NOT NULL,
    personal_qualifications_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    account_holder_id uuid,
    company_cnpj character varying(14),
    occupation_code character varying,
    occupation_description character varying(100),
    informed_income_frequency character varying,
    informed_income_amount numeric,
    informed_income_currency character varying(3),
    informed_income_date date,
    informed_patrimony_amount numeric,
    informed_patrimony_currency character varying(3),
    informed_patrimony_year integer,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.personal_qualifications OWNER TO test;

--
-- Name: personal_qualifications_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.personal_qualifications_aud (
    reference_id integer NOT NULL,
    personal_qualifications_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    account_holder_id uuid,
    company_cnpj character varying(14),
    occupation_code character varying,
    occupation_description character varying(100),
    informed_income_frequency character varying,
    informed_income_amount numeric,
    informed_income_currency character varying(3),
    informed_income_date date,
    informed_patrimony_amount numeric,
    informed_patrimony_currency character varying(3),
    informed_patrimony_year integer,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.personal_qualifications_aud OWNER TO test;

--
-- Name: personal_qualifications_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.personal_qualifications_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.personal_qualifications_reference_id_seq OWNER TO test;

--
-- Name: personal_qualifications_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.personal_qualifications_reference_id_seq OWNED BY public.personal_qualifications.reference_id;


--
-- Name: pix_payments; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.pix_payments (
    reference_id integer NOT NULL,
    payment_id character varying,
    local_instrument character varying,
    pix_payment_id integer,
    creditor_account_id integer,
    remittance_information character varying,
    qr_code character varying,
    proxy character varying,
    status character varying,
    creation_date_time timestamp without time zone,
    status_update_date_time timestamp without time zone NOT NULL,
    rejection_reason character varying,
    idempotency_key character varying,
    payment_consent_id character varying,
    transaction_identification character varying,
    end_to_end_id character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    cancellation_reason character varying,
    cancellation_from character varying
);


ALTER TABLE public.pix_payments OWNER TO test;

--
-- Name: pix_payments_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.pix_payments_aud (
    reference_id integer NOT NULL,
    payment_id character varying NOT NULL,
    local_instrument character varying,
    pix_payment_id integer,
    creditor_account_id integer,
    remittance_information character varying,
    qr_code character varying,
    proxy character varying,
    status character varying,
    creation_date_time timestamp without time zone,
    status_update_date_time timestamp without time zone,
    rejection_reason character varying,
    idempotency_key character varying,
    payment_consent_id character varying,
    transaction_identification character varying,
    end_to_end_id character varying,
    rev integer NOT NULL,
    revtype smallint,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    cancellation_reason character varying,
    cancellation_from character varying
);


ALTER TABLE public.pix_payments_aud OWNER TO test;

--
-- Name: pix_payments_payments; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.pix_payments_payments (
    pix_payment_id integer NOT NULL,
    currency character varying,
    amount character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying
);


ALTER TABLE public.pix_payments_payments OWNER TO test;

--
-- Name: pix_payments_payments_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.pix_payments_payments_aud (
    pix_payment_id integer NOT NULL,
    currency character varying,
    amount character varying,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    hibernate_status character varying,
    created_by character varying,
    updated_by character varying,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.pix_payments_payments_aud OWNER TO test;

--
-- Name: pix_payments_payments_pix_payment_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.pix_payments_payments_pix_payment_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pix_payments_payments_pix_payment_id_seq OWNER TO test;

--
-- Name: pix_payments_payments_pix_payment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.pix_payments_payments_pix_payment_id_seq OWNED BY public.pix_payments_payments.pix_payment_id;


--
-- Name: pix_payments_reference_id_seq; Type: SEQUENCE; Schema: public; Owner: test
--

CREATE SEQUENCE public.pix_payments_reference_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.pix_payments_reference_id_seq OWNER TO test;

--
-- Name: pix_payments_reference_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: test
--

ALTER SEQUENCE public.pix_payments_reference_id_seq OWNED BY public.pix_payments.reference_id;


--
-- Name: releases; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.releases (
    releases_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    is_over_parcel_payment boolean NOT NULL,
    instalment_id character varying NOT NULL,
    paid_date character varying NOT NULL,
    currency character varying NOT NULL,
    paid_amount double precision NOT NULL,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    payments_id uuid DEFAULT public.uuid_generate_v4(),
    contract_id uuid
);


ALTER TABLE public.releases OWNER TO test;

--
-- Name: releases_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.releases_aud (
    releases_id uuid NOT NULL,
    payment_id character varying,
    is_over_parcel_payment boolean,
    instalment_id character varying,
    paid_date character varying,
    currency character varying,
    paid_amount double precision,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    rev integer NOT NULL,
    revtype smallint,
    payments_id uuid DEFAULT public.uuid_generate_v4(),
    contract_id uuid
);


ALTER TABLE public.releases_aud OWNER TO test;

--
-- Name: revinfo; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.revinfo (
    rev integer NOT NULL,
    revtstmp bigint
);


ALTER TABLE public.revinfo OWNER TO test;

--
-- Name: warranties; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.warranties (
    warranty_id uuid DEFAULT public.uuid_generate_v4() NOT NULL,
    currency character varying NOT NULL,
    warranty_type character varying NOT NULL,
    warranty_subtype character varying NOT NULL,
    warranty_amount double precision,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    contract_id uuid NOT NULL
);


ALTER TABLE public.warranties OWNER TO test;

--
-- Name: warranties_aud; Type: TABLE; Schema: public; Owner: test
--

CREATE TABLE public.warranties_aud (
    warranty_id uuid NOT NULL,
    currency character varying,
    warranty_type character varying,
    warranty_subtype character varying,
    warranty_amount double precision,
    created_at timestamp without time zone,
    updated_at timestamp without time zone,
    created_by character varying,
    updated_by character varying,
    hibernate_status character varying,
    contract_id uuid,
    rev integer NOT NULL,
    revtype smallint
);


ALTER TABLE public.warranties_aud OWNER TO test;

--
-- Name: account_holders reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.account_holders ALTER COLUMN reference_id SET DEFAULT nextval('public.account_holders_reference_id_seq'::regclass);


--
-- Name: account_transactions account_transaction_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.account_transactions ALTER COLUMN account_transaction_id SET DEFAULT nextval('public.account_transactions_account_transaction_id_seq'::regclass);


--
-- Name: accounts reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.accounts ALTER COLUMN reference_id SET DEFAULT nextval('public.accounts_reference_id_seq'::regclass);


--
-- Name: business_emails reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_emails ALTER COLUMN reference_id SET DEFAULT nextval('public.business_emails_reference_id_seq'::regclass);


--
-- Name: business_financial_relations reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations ALTER COLUMN reference_id SET DEFAULT nextval('public.business_financial_relations_reference_id_seq'::regclass);


--
-- Name: business_financial_relations_procurators reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations_procurators ALTER COLUMN reference_id SET DEFAULT nextval('public.business_financial_relations_procurators_reference_id_seq'::regclass);


--
-- Name: business_financial_relations_products_services_type reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations_products_services_type ALTER COLUMN reference_id SET DEFAULT nextval('public.business_financial_relations_products_services_reference_id_seq'::regclass);


--
-- Name: business_identifications reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_identifications ALTER COLUMN reference_id SET DEFAULT nextval('public.business_identifications_reference_id_seq'::regclass);


--
-- Name: business_identifications_company_cnpj reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_identifications_company_cnpj ALTER COLUMN reference_id SET DEFAULT nextval('public.business_identifications_company_cnpj_reference_id_seq'::regclass);


--
-- Name: business_other_documents reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_other_documents ALTER COLUMN reference_id SET DEFAULT nextval('public.business_other_documents_reference_id_seq'::regclass);


--
-- Name: business_parties reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_parties ALTER COLUMN reference_id SET DEFAULT nextval('public.business_parties_reference_id_seq'::regclass);


--
-- Name: business_phones reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_phones ALTER COLUMN reference_id SET DEFAULT nextval('public.business_phones_reference_id_seq'::regclass);


--
-- Name: business_postal_addresses reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_postal_addresses ALTER COLUMN reference_id SET DEFAULT nextval('public.business_postal_addresses_reference_id_seq'::regclass);


--
-- Name: business_qualifications reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_qualifications ALTER COLUMN reference_id SET DEFAULT nextval('public.business_qualifications_reference_id_seq'::regclass);


--
-- Name: business_qualifications_economic_activities reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_qualifications_economic_activities ALTER COLUMN reference_id SET DEFAULT nextval('public.business_qualifications_economic_activities_reference_id_seq'::regclass);


--
-- Name: consent_accounts reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_accounts ALTER COLUMN reference_id SET DEFAULT nextval('public.consent_accounts_reference_id_seq'::regclass);


--
-- Name: consent_contracts reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_contracts ALTER COLUMN reference_id SET DEFAULT nextval('public.consent_contracts_reference_id_seq'::regclass);


--
-- Name: consent_credit_card_accounts reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_credit_card_accounts ALTER COLUMN reference_id SET DEFAULT nextval('public.consent_credit_card_accounts_reference_id_seq'::regclass);


--
-- Name: consent_permissions reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_permissions ALTER COLUMN reference_id SET DEFAULT nextval('public.consent_permissions_reference_id_seq'::regclass);


--
-- Name: consents reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consents ALTER COLUMN reference_id SET DEFAULT nextval('public.consents_reference_id_seq'::regclass);


--
-- Name: creditor_accounts creditor_account_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.creditor_accounts ALTER COLUMN creditor_account_id SET DEFAULT nextval('public.creditor_accounts_creditor_account_id_seq'::regclass);


--
-- Name: creditors creditor_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.creditors ALTER COLUMN creditor_id SET DEFAULT nextval('public.creditors_creditor_id_seq'::regclass);


--
-- Name: jti id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.jti ALTER COLUMN id SET DEFAULT nextval('public.jti_id_seq'::regclass);


--
-- Name: logged_in_user_entity_documents logged_in_user_entity_document_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.logged_in_user_entity_documents ALTER COLUMN logged_in_user_entity_document_id SET DEFAULT nextval('public.logged_in_user_entity_documen_logged_in_user_entity_documen_seq'::regclass);


--
-- Name: payment_consent_details payment_consent_details_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consent_details ALTER COLUMN payment_consent_details_id SET DEFAULT nextval('public.payment_consent_details_payment_consent_details_id_seq'::regclass);


--
-- Name: payment_consent_details_aud payment_consent_details_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consent_details_aud ALTER COLUMN payment_consent_details_id SET DEFAULT nextval('public.payment_consent_details_aud_payment_consent_details_id_seq'::regclass);


--
-- Name: payment_consent_payments payment_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consent_payments ALTER COLUMN payment_id SET DEFAULT nextval('public.payment_consent_payments_payment_id_seq'::regclass);


--
-- Name: payment_consent_payments payment_consent_details_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consent_payments ALTER COLUMN payment_consent_details_id SET DEFAULT nextval('public.payment_consent_payments_payment_consent_details_id_seq'::regclass);


--
-- Name: payment_consent_payments_aud payment_consent_details_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consent_payments_aud ALTER COLUMN payment_consent_details_id SET DEFAULT nextval('public.payment_consent_payments_aud_payment_consent_details_id_seq'::regclass);


--
-- Name: payment_consents reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consents ALTER COLUMN reference_id SET DEFAULT nextval('public.payment_consents_reference_id_seq'::regclass);


--
-- Name: personal_emails reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_emails ALTER COLUMN reference_id SET DEFAULT nextval('public.personal_emails_reference_id_seq'::regclass);


--
-- Name: personal_filiation reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_filiation ALTER COLUMN reference_id SET DEFAULT nextval('public.personal_filiation_reference_id_seq'::regclass);


--
-- Name: personal_financial_relations reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations ALTER COLUMN reference_id SET DEFAULT nextval('public.personal_financial_relations_reference_id_seq'::regclass);


--
-- Name: personal_financial_relations_procurators reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations_procurators ALTER COLUMN reference_id SET DEFAULT nextval('public.personal_financial_relations_procurators_reference_id_seq'::regclass);


--
-- Name: personal_financial_relations_products_services_type reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations_products_services_type ALTER COLUMN reference_id SET DEFAULT nextval('public.personal_financial_relations_products_services_reference_id_seq'::regclass);


--
-- Name: personal_identifications reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_identifications ALTER COLUMN reference_id SET DEFAULT nextval('public.personal_identifications_reference_id_seq'::regclass);


--
-- Name: personal_identifications_company_cnpj reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_identifications_company_cnpj ALTER COLUMN reference_id SET DEFAULT nextval('public.personal_identifications_company_cnpj_reference_id_seq'::regclass);


--
-- Name: personal_nationality reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_nationality ALTER COLUMN reference_id SET DEFAULT nextval('public.personal_nationality_reference_id_seq'::regclass);


--
-- Name: personal_nationality_documents reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_nationality_documents ALTER COLUMN reference_id SET DEFAULT nextval('public.personal_nationality_documents_reference_id_seq'::regclass);


--
-- Name: personal_other_documents reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_other_documents ALTER COLUMN reference_id SET DEFAULT nextval('public.personal_other_documents_reference_id_seq'::regclass);


--
-- Name: personal_phones reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_phones ALTER COLUMN reference_id SET DEFAULT nextval('public.personal_phones_reference_id_seq'::regclass);


--
-- Name: personal_postal_addresses reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_postal_addresses ALTER COLUMN reference_id SET DEFAULT nextval('public.personal_postal_addresses_reference_id_seq'::regclass);


--
-- Name: personal_qualifications reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_qualifications ALTER COLUMN reference_id SET DEFAULT nextval('public.personal_qualifications_reference_id_seq'::regclass);


--
-- Name: pix_payments reference_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.pix_payments ALTER COLUMN reference_id SET DEFAULT nextval('public.pix_payments_reference_id_seq'::regclass);


--
-- Name: pix_payments_payments pix_payment_id; Type: DEFAULT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.pix_payments_payments ALTER COLUMN pix_payment_id SET DEFAULT nextval('public.pix_payments_payments_pix_payment_id_seq'::regclass);


--
-- Data for Name: account_holders; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.account_holders (reference_id, account_holder_id, document_identification, document_rel, created_at, updated_at, hibernate_status, created_by, updated_by, account_holder_name, user_id) FROM stdin;
\.


--
-- Data for Name: account_holders_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.account_holders_aud (reference_id, account_holder_id, document_identification, document_rel, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype, account_holder_name, user_id) FROM stdin;
\.


--
-- Data for Name: account_transactions; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.account_transactions (account_transaction_id, account_id, transaction_id, completed_authorised_payment_type, credit_debit_type, transaction_name, type, amount, transaction_currency, transaction_date, partie_cnpj_cpf, partie_person_type, partie_compe_code, partie_branch_code, partie_number, partie_check_digit, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: account_transactions_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.account_transactions_aud (account_transaction_id, account_id, transaction_id, completed_authorised_payment_type, credit_debit_type, transaction_name, type, amount, transaction_currency, transaction_date, partie_cnpj_cpf, partie_person_type, partie_compe_code, partie_branch_code, partie_number, partie_check_digit, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: accounts; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.accounts (reference_id, account_id, status, currency, account_type, account_sub_type, created_at, updated_at, hibernate_status, created_by, updated_by, brand_name, company_cnpj, compe_code, branch_code, number, check_digit, available_amount, available_amount_currency, blocked_amount, blocked_amount_currency, automatically_invested_amount, automatically_invested_amount_currency, overdraft_contracted_limit_currency, overdraft_used_limit_currency, unarranged_overdraft_amount_currency, account_holder_id, overdraft_contracted_limit, overdraft_used_limit, unarranged_overdraft_amount, debtor_ispb, debtor_issuer, debtor_type) FROM stdin;
\.


--
-- Data for Name: accounts_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.accounts_aud (reference_id, account_id, status, currency, account_type, account_sub_type, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype, brand_name, company_cnpj, compe_code, branch_code, number, check_digit, available_amount, available_amount_currency, blocked_amount, blocked_amount_currency, automatically_invested_amount, automatically_invested_amount_currency, overdraft_contracted_limit_currency, overdraft_used_limit_currency, unarranged_overdraft_amount_currency, account_holder_id, overdraft_contracted_limit, overdraft_used_limit, unarranged_overdraft_amount, debtor_ispb, debtor_issuer, debtor_type) FROM stdin;
\.


--
-- Data for Name: balloon_payments; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.balloon_payments (balloon_payments_id, due_date, currency, amount, created_at, updated_at, created_by, updated_by, hibernate_status, contract_id) FROM stdin;
\.


--
-- Data for Name: balloon_payments_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.balloon_payments_aud (balloon_payments_id, due_date, currency, amount, created_at, updated_at, created_by, updated_by, hibernate_status, rev, revtype, contract_id) FROM stdin;
\.


--
-- Data for Name: business_emails; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_emails (reference_id, business_identifications_id, is_main, email, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: business_emails_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_emails_aud (reference_id, business_identifications_id, is_main, email, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: business_financial_relations; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_financial_relations (reference_id, business_financial_relations_id, account_holder_id, start_date, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: business_financial_relations_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_financial_relations_aud (reference_id, business_financial_relations_id, account_holder_id, start_date, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: business_financial_relations_procurators; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_financial_relations_procurators (reference_id, business_financial_relations_id, type, cnpj_cpf_number, civil_name, social_name, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: business_financial_relations_procurators_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_financial_relations_procurators_aud (reference_id, business_financial_relations_id, type, cnpj_cpf_number, civil_name, social_name, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: business_financial_relations_products_services_type; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_financial_relations_products_services_type (reference_id, business_financial_relations_id, type, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: business_financial_relations_products_services_type_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_financial_relations_products_services_type_aud (reference_id, business_financial_relations_id, type, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: business_identifications; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_identifications (reference_id, business_identifications_id, account_holder_id, brand_name, company_name, trade_name, incorporation_date, cnpj_number, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: business_identifications_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_identifications_aud (reference_id, business_identifications_id, account_holder_id, brand_name, company_name, trade_name, incorporation_date, cnpj_number, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: business_identifications_company_cnpj; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_identifications_company_cnpj (reference_id, business_identifications_id, company_cnpj, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: business_identifications_company_cnpj_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_identifications_company_cnpj_aud (reference_id, business_identifications_id, company_cnpj, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: business_other_documents; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_other_documents (reference_id, business_identifications_id, type, number, country, expiration_date, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: business_other_documents_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_other_documents_aud (reference_id, business_identifications_id, type, number, country, expiration_date, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: business_parties; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_parties (reference_id, business_identifications_id, person_type, type, civil_name, social_name, company_name, trade_name, start_date, shareholding, document_type, document_number, document_additional_info, document_country, document_expiration_date, document_issue_date, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: business_parties_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_parties_aud (reference_id, business_identifications_id, person_type, type, civil_name, social_name, company_name, trade_name, start_date, shareholding, document_type, document_number, document_additional_info, document_country, document_expiration_date, document_issue_date, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: business_phones; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_phones (reference_id, business_identifications_id, is_main, type, additional_info, country_calling_code, area_code, number, phone_extension, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: business_phones_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_phones_aud (reference_id, business_identifications_id, is_main, type, additional_info, country_calling_code, area_code, number, phone_extension, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: business_postal_addresses; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_postal_addresses (reference_id, business_identifications_id, is_main, address, additional_info, district_name, town_name, ibge_town_code, country_subdivision, post_code, country, country_code, latitude, longitude, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: business_postal_addresses_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_postal_addresses_aud (reference_id, business_identifications_id, is_main, address, additional_info, district_name, town_name, ibge_town_code, country_subdivision, post_code, country, country_code, latitude, longitude, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: business_qualifications; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_qualifications (reference_id, business_qualifications_id, account_holder_id, informed_revenue_frequency, informed_revenue_frequency_additional_information, informed_revenue_amount, informed_revenue_currency, informed_revenue_year, informed_patrimony_currency, informed_patrimony_date, created_at, updated_at, hibernate_status, created_by, updated_by, informed_patrimony_amount) FROM stdin;
\.


--
-- Data for Name: business_qualifications_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_qualifications_aud (reference_id, business_qualifications_id, account_holder_id, informed_revenue_frequency, informed_revenue_frequency_additional_information, informed_revenue_amount, informed_revenue_currency, informed_revenue_year, informed_patrimony_currency, informed_patrimony_date, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype, informed_patrimony_amount) FROM stdin;
\.


--
-- Data for Name: business_qualifications_economic_activities; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_qualifications_economic_activities (reference_id, business_qualifications_id, code, is_main, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: business_qualifications_economic_activities_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.business_qualifications_economic_activities_aud (reference_id, business_qualifications_id, code, is_main, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: consent_accounts; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.consent_accounts (reference_id, consent_id, account_id, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: consent_accounts_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.consent_accounts_aud (reference_id, consent_id, account_id, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: consent_contracts; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.consent_contracts (reference_id, consent_id, contract_id, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: consent_contracts_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.consent_contracts_aud (reference_id, consent_id, contract_id, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: consent_credit_card_accounts; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.consent_credit_card_accounts (reference_id, consent_id, credit_card_account_id, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: consent_credit_card_accounts_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.consent_credit_card_accounts_aud (reference_id, consent_id, credit_card_account_id, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: consent_permissions; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.consent_permissions (reference_id, permission, created_at, updated_at, hibernate_status, created_by, updated_by, consent_id) FROM stdin;
\.


--
-- Data for Name: consent_permissions_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.consent_permissions_aud (reference_id, permission, consent_id, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype, new_consent_id) FROM stdin;
\.


--
-- Data for Name: consents; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.consents (reference_id, expiration_date_time, transaction_from_date_time, transaction_to_date_time, creation_date_time, status_update_date_time, status, client_id, created_at, updated_at, hibernate_status, created_by, updated_by, consent_id, account_holder_id, business_document_identification, business_document_rel) FROM stdin;
\.


--
-- Data for Name: consents_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.consents_aud (reference_id, expiration_date_time, transaction_from_date_time, transaction_to_date_time, creation_date_time, status_update_date_time, status, risk, client_id, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype, consent_id, account_holder_id, business_document_identification, business_document_rel) FROM stdin;
\.


--
-- Data for Name: contracted_fees; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.contracted_fees (contracted_fees_id, fee_name, fee_code, fee_charge_type, fee_charge, fee_amount, fee_rate, created_at, updated_at, created_by, updated_by, hibernate_status, contract_id) FROM stdin;
\.


--
-- Data for Name: contracted_fees_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.contracted_fees_aud (contracted_fees_id, fee_name, fee_code, fee_charge_type, fee_charge, fee_amount, fee_rate, created_at, updated_at, created_by, updated_by, hibernate_status, contract_id, rev, revtype) FROM stdin;
\.


--
-- Data for Name: contracted_finance_charges; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.contracted_finance_charges (contracted_finance_charges_id, charge_type, charge_additional_info, charge_rate, created_at, updated_at, created_by, updated_by, hibernate_status, contract_id) FROM stdin;
\.


--
-- Data for Name: contracted_finance_charges_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.contracted_finance_charges_aud (contracted_finance_charges_id, charge_type, charge_additional_info, charge_rate, created_at, updated_at, created_by, updated_by, hibernate_status, contract_id, rev, revtype) FROM stdin;
\.


--
-- Data for Name: contracts; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.contracts (contract_id, contract_type, company_cnpj, product_name, product_type, product_sub_type, contract_amount, currency, instalment_periodicity, instalment_periodicity_additional_info, cet, amortization_scheduled, amortization_scheduled_additional_info, ipoc_code, created_at, updated_at, created_by, updated_by, hibernate_status, account_holder_id, paid_instalments, contract_outstanding_balance, type_number_of_instalments, total_number_of_instalments, type_contract_remaining, contract_remaining_number, due_instalments, past_due_instalments, status, contract_date, disbursement_date, settlement_date, due_date, first_instalment_due_date, contract_number) FROM stdin;
\.


--
-- Data for Name: contracts_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.contracts_aud (contract_id, contract_type, company_cnpj, product_name, product_type, product_sub_type, contract_amount, currency, instalment_periodicity, instalment_periodicity_additional_info, cet, amortization_scheduled, amortization_scheduled_additional_info, ipoc_code, created_by, updated_by, created_at, updated_at, hibernate_status, rev, revtype, account_holder_id, paid_instalments, contract_outstanding_balance, type_number_of_instalments, total_number_of_instalments, type_contract_remaining, contract_remaining_number, due_instalments, past_due_instalments, status, contract_date, disbursement_date, settlement_date, due_date, first_instalment_due_date, contract_number) FROM stdin;
\.


--
-- Data for Name: credit_card_accounts; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_card_accounts (credit_card_account_id, brand_name, company_cnpj, name, product_type, product_additional_info, credit_card_network, network_additional_info, status, account_holder_id, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: credit_card_accounts_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_card_accounts_aud (credit_card_account_id, brand_name, company_cnpj, name, product_type, product_additional_info, credit_card_network, network_additional_info, status, account_holder_id, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: credit_card_accounts_bills; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_card_accounts_bills (bill_id, due_date, bill_total_amount, bill_total_amount_currency, bill_minimum_amount, bill_minimum_amount_currency, is_instalment, credit_card_account_id, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: credit_card_accounts_bills_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_card_accounts_bills_aud (bill_id, due_date, bill_total_amount, bill_total_amount_currency, bill_minimum_amount, bill_minimum_amount_currency, is_instalment, credit_card_account_id, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: credit_card_accounts_bills_finance_charge; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_card_accounts_bills_finance_charge (finance_charge_id, type, additional_info, amount, currency, bill_id, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: credit_card_accounts_bills_finance_charge_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_card_accounts_bills_finance_charge_aud (finance_charge_id, type, additional_info, amount, currency, bill_id, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: credit_card_accounts_bills_payment; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_card_accounts_bills_payment (payment_id, value_type, payment_date, payment_mode, amount, currency, bill_id, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: credit_card_accounts_bills_payment_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_card_accounts_bills_payment_aud (payment_id, value_type, payment_date, payment_mode, amount, currency, bill_id, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: credit_card_accounts_limits; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_card_accounts_limits (limit_id, credit_line_limit_type, consolidation_type, identification_number, line_name, line_name_additional_info, is_limit_flexible, limit_amount_currency, limit_amount, used_amount_currency, used_amount, available_amount_currency, available_amount, credit_card_account_id, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: credit_card_accounts_limits_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_card_accounts_limits_aud (limit_id, credit_line_limit_type, consolidation_type, identification_number, line_name, line_name_additional_info, is_limit_flexible, limit_amount_currency, limit_amount, used_amount_currency, used_amount, available_amount_currency, available_amount, credit_card_account_id, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: credit_card_accounts_transaction; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_card_accounts_transaction (transaction_id, identification_number, line_name, transaction_name, bill_id, credit_debit_type, transaction_type, transactional_additional_info, payment_type, fee_type, fee_type_additional_info, other_credits_type, other_credits_additional_info, charge_identificator, charge_number, brazilian_amount, amount, currency, transaction_date, bill_post_date, payee_mcc, created_at, updated_at, hibernate_status, created_by, updated_by, credit_card_account_id) FROM stdin;
\.


--
-- Data for Name: credit_card_accounts_transaction_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_card_accounts_transaction_aud (transaction_id, identification_number, line_name, transaction_name, bill_id, credit_debit_type, transaction_type, transactional_additional_info, payment_type, fee_type, fee_type_additional_info, other_credits_type, other_credits_additional_info, charge_identificator, charge_number, brazilian_amount, amount, currency, transaction_date, bill_post_date, payee_mcc, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype, credit_card_account_id) FROM stdin;
\.


--
-- Data for Name: credit_cards_account_payment_method; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_cards_account_payment_method (payment_method_id, identification_number, is_multiple_credit_card, credit_card_account_id, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: credit_cards_account_payment_method_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.credit_cards_account_payment_method_aud (payment_method_id, identification_number, is_multiple_credit_card, credit_card_account_id, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: creditor_accounts; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.creditor_accounts (creditor_account_id, ispb, issuer, number, account_type, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: creditor_accounts_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.creditor_accounts_aud (creditor_account_id, ispb, issuer, number, account_type, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: creditors; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.creditors (creditor_id, person_type, cpf_cnpj, name, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: creditors_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.creditors_aud (creditor_id, person_type, cpf_cnpj, name, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: flyway_schema_history; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.flyway_schema_history (installed_rank, version, description, type, script, checksum, installed_by, installed_on, execution_time, success) FROM stdin;
1	1	bank db init	SQL	V1__bank_db_init.sql	-797840297	test	2022-09-01 11:02:12.808325	38	t
2	2	use obb consents	SQL	V2__use_obb_consents.sql	1974840662	test	2022-09-01 11:02:12.869027	31	t
3	3	make business entities optional	SQL	V3__make_business_entities_optional.sql	184549556	test	2022-09-01 11:02:12.916253	8	t
4	4	add linked account ids	SQL	V4__add_linked_account_ids.sql	-729410023	test	2022-09-01 11:02:12.939038	9	t
5	5	add consent id temp column	SQL	V5__add_consent_id_temp_column.sql	-896513137	test	2022-09-01 11:02:12.963348	11	t
6	6	ConsentIdMigration	JDBC	db.migration.V6__ConsentIdMigration	64984658	test	2022-09-01 11:02:13.017629	37	t
7	7	MigrateExistingPermissions	JDBC	db.migration.V7__MigrateExistingPermissions	4978491	test	2022-09-01 11:02:13.066049	34	t
8	8	rename new consent id	SQL	V8__rename_new_consent_id.sql	-907289308	test	2022-09-01 11:02:13.084991	20	t
9	9	add payments tables	SQL	V9__add_payments_tables.sql	319106568	test	2022-09-01 11:02:13.125021	42	t
10	10	add payment consent details table	SQL	V10__add_payment_consent_details_table.sql	1137821738	test	2022-09-01 11:02:13.183561	27	t
11	11	add jti table	SQL	V11__add_jti_table.sql	-1940158719	test	2022-09-01 11:02:13.229145	11	t
12	12	adding contracts tables	SQL	V12__adding_contracts_tables.sql	78805329	test	2022-09-01 11:02:13.269081	69	t
13	13	add schedule column	SQL	V13__add_schedule_column.sql	153766538	test	2022-09-01 11:02:13.353996	10	t
14	14	pix payment transaction identification	SQL	V14__pix_payment_transaction_identification.sql	1520780521	test	2022-09-01 11:02:13.377526	7	t
15	15	add accounts entities	SQL	V15__add_accounts_entities.sql	477333927	test	2022-09-01 11:02:13.400985	25	t
16	16	add owner ids	SQL	V16__add_owner_ids.sql	-1359388111	test	2022-09-01 11:02:13.440124	6	t
17	17	fix user situation	SQL	V17__fix_user_situation.sql	-643275879	test	2022-09-01 11:02:13.465356	134	t
18	18	fix consent accounts relation	SQL	V18__fix_consent_accounts_relation.sql	-1116214457	test	2022-09-01 11:02:13.614907	20	t
19	19	add customer records	SQL	V19__add_customer_records.sql	1392635866	test	2022-09-01 11:02:13.662444	138	t
20	20	fix contracts table	SQL	V20__fix_contracts_table.sql	-600222947	test	2022-09-01 11:02:13.872083	103	t
21	21	add account transactions table	SQL	V21__add_account_transactions_table.sql	-655143683	test	2022-09-01 11:02:13.987801	11	t
22	22	add username	SQL	V22__add_username.sql	-1871150235	test	2022-09-01 11:02:14.013139	8	t
23	23	add credit card tables	SQL	V23__add_credit_card_tables.sql	-645762698	test	2022-09-01 11:02:14.037898	40	t
24	24	fix payment consent	SQL	V24__fix_payment_consent.sql	-466542705	test	2022-09-01 11:02:14.089625	16	t
25	25	fix credit rard transaction	SQL	V25__fix_credit_rard_transaction.sql	344352929	test	2022-09-01 11:02:14.116946	14	t
26	26	multiple business cnpjs	SQL	V26__multiple_business_cnpjs.sql	1870985311	test	2022-09-01 11:02:14.146145	16	t
27	27	add user sub	SQL	V27__add_user_sub.sql	-1294281799	test	2022-09-01 11:02:14.172626	6	t
28	28	add payments forced response table	SQL	V28__add_payments_forced_response_table.sql	662221187	test	2022-09-01 11:02:14.189497	10	t
29	29	ditch business entity	SQL	V29__ditch_business_entity.sql	-1403196391	test	2022-09-01 11:02:14.224403	28	t
30	30	transactions unique id	SQL	V30__transactions_unique_id.sql	369232883	test	2022-09-01 11:02:14.264885	6	t
31	31	pix payment add end to end id	SQL	V31__pix_payment_add_end_to_end_id.sql	-268667258	test	2022-09-01 11:02:14.28182	5	t
\.


--
-- Data for Name: interest_rates; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.interest_rates (interest_rates_id, tax_type, interest_rate_type, tax_periodicity, calculation, referential_rate_indexer_type, referential_rate_indexer_sub_type, referential_rate_indexer_additional_info, pre_fixed_rate, post_fixed_rate, additional_info, created_at, updated_at, created_by, updated_by, hibernate_status, contract_id) FROM stdin;
\.


--
-- Data for Name: interest_rates_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.interest_rates_aud (interest_rates_id, tax_type, interest_rate_type, tax_periodicity, calculation, referential_rate_indexer_type, referential_rate_indexer_sub_type, referential_rate_indexer_additional_info, pre_fixed_rate, post_fixed_rate, additional_info, created_at, updated_at, created_by, updated_by, hibernate_status, contract_id, rev, revtype) FROM stdin;
\.


--
-- Data for Name: jti; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.jti (id, jti, created_date) FROM stdin;
\.


--
-- Data for Name: logged_in_user_entity_documents; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.logged_in_user_entity_documents (logged_in_user_entity_document_id, identification, rel, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: logged_in_user_entity_documents_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.logged_in_user_entity_documents_aud (logged_in_user_entity_document_id, identification, rel, created_at, updated_at, hibernate_status, rev, revtype, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: over_parcel_charges; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.over_parcel_charges (over_parcel_charges_id, charge_type, charge_additional_info, charge_amount, created_at, updated_at, created_by, updated_by, hibernate_status, releases_id) FROM stdin;
\.


--
-- Data for Name: over_parcel_charges_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.over_parcel_charges_aud (over_parcel_charges_id, charge_type, charge_additional_info, charge_amount, created_at, updated_at, created_by, updated_by, hibernate_status, releases_id, rev, revtype) FROM stdin;
\.


--
-- Data for Name: over_parcel_fees; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.over_parcel_fees (over_parcel_fees_id, fee_name, fee_code, fee_amount, created_at, updated_at, created_by, updated_by, hibernate_status, releases_id) FROM stdin;
\.


--
-- Data for Name: over_parcel_fees_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.over_parcel_fees_aud (over_parcel_fees_id, fee_name, fee_code, fee_amount, created_at, updated_at, created_by, updated_by, hibernate_status, releases_id, rev, revtype) FROM stdin;
\.


--
-- Data for Name: payment_consent_details; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.payment_consent_details (payment_consent_details_id, local_instrument, qr_code, proxy, creditor_ispb, creditor_issuer, creditor_account_number, creditor_account_type, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: payment_consent_details_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.payment_consent_details_aud (payment_consent_details_id, local_instrument, qr_code, proxy, creditor_ispb, creditor_issuer, creditor_account_number, creditor_account_type, rev, revtype, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: payment_consent_payments; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.payment_consent_payments (payment_id, payment_type, payment_date, currency, amount, created_at, updated_at, hibernate_status, created_by, updated_by, payment_consent_details_id, schedule) FROM stdin;
\.


--
-- Data for Name: payment_consent_payments_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.payment_consent_payments_aud (payment_id, payment_type, payment_date, currency, amount, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype, payment_consent_details_id, schedule) FROM stdin;
\.


--
-- Data for Name: payment_consents; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.payment_consents (reference_id, payment_consent_id, client_id, status, creditor_id, payment_id, creation_date_time, expiration_date_time, status_update_date_time, idempotency_key, created_at, updated_at, hibernate_status, created_by, updated_by, account_holder_id, account_id, business_document_identification, business_document_rel) FROM stdin;
\.


--
-- Data for Name: payment_consents_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.payment_consents_aud (reference_id, payment_consent_id, client_id, status, creditor_id, payment_id, creation_date_time, expiration_date_time, status_update_date_time, idempotency_key, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype, account_holder_id, account_id, business_document_identification, business_document_rel) FROM stdin;
\.


--
-- Data for Name: payments_simulate_response; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.payments_simulate_response (id, client_id, payment_consent_id, request_time, request_end_time, http_status, http_error_message, duration, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: payments_simulate_response_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.payments_simulate_response_aud (id, client_id, payment_consent_id, http_status, http_error_message, duration, request_time, request_end_time, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: personal_emails; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_emails (reference_id, personal_identifications_id, is_main, email, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: personal_emails_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_emails_aud (reference_id, personal_identifications_id, is_main, email, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: personal_filiation; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_filiation (reference_id, personal_identifications_id, type, civil_name, social_name, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: personal_filiation_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_filiation_aud (reference_id, personal_identifications_id, type, civil_name, social_name, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: personal_financial_relations; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_financial_relations (reference_id, personal_financial_relations_id, account_holder_id, start_date, products_services_type_additional_info, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: personal_financial_relations_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_financial_relations_aud (reference_id, personal_financial_relations_id, account_holder_id, start_date, products_services_type_additional_info, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: personal_financial_relations_procurators; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_financial_relations_procurators (reference_id, personal_financial_relations_id, type, cpf_number, civil_name, social_name, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: personal_financial_relations_procurators_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_financial_relations_procurators_aud (reference_id, personal_financial_relations_id, type, cpf_number, civil_name, social_name, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: personal_financial_relations_products_services_type; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_financial_relations_products_services_type (reference_id, personal_financial_relations_id, type, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: personal_financial_relations_products_services_type_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_financial_relations_products_services_type_aud (reference_id, personal_financial_relations_id, type, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: personal_identifications; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_identifications (reference_id, personal_identifications_id, account_holder_id, brand_name, civil_name, social_name, birth_date, marital_status_code, marital_status_additional_info, sex, has_brazilian_nationality, cpf_number, passport_number, passport_country, passport_expiration_date, passport_issue_date, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: personal_identifications_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_identifications_aud (reference_id, personal_identifications_id, account_holder_id, brand_name, civil_name, social_name, birth_date, marital_status_code, marital_status_additional_info, sex, has_brazilian_nationality, cpf_number, passport_number, passport_country, passport_expiration_date, passport_issue_date, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: personal_identifications_company_cnpj; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_identifications_company_cnpj (reference_id, personal_identifications_id, company_cnpj, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: personal_identifications_company_cnpj_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_identifications_company_cnpj_aud (reference_id, personal_identifications_id, company_cnpj, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: personal_nationality; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_nationality (reference_id, personal_nationality_id, personal_identifications_id, other_nationalities_info, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: personal_nationality_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_nationality_aud (reference_id, personal_nationality_id, personal_identifications_id, other_nationalities_info, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: personal_nationality_documents; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_nationality_documents (reference_id, personal_nationality_id, type, number, expiration_date, issue_date, country, type_additional_info, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: personal_nationality_documents_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_nationality_documents_aud (reference_id, personal_nationality_id, type, number, expiration_date, issue_date, country, type_additional_info, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: personal_other_documents; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_other_documents (reference_id, personal_identifications_id, type, type_additional_info, number, check_digit, additional_info, expiration_date, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: personal_other_documents_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_other_documents_aud (reference_id, personal_identifications_id, type, type_additional_info, number, check_digit, additional_info, expiration_date, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: personal_phones; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_phones (reference_id, personal_identifications_id, is_main, type, additional_info, country_calling_code, area_code, number, phone_extension, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: personal_phones_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_phones_aud (reference_id, personal_identifications_id, is_main, type, additional_info, country_calling_code, area_code, number, phone_extension, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: personal_postal_addresses; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_postal_addresses (reference_id, personal_identifications_id, is_main, address, additional_info, district_name, town_name, ibge_town_code, country_subdivision, post_code, country, country_code, latitude, longitude, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: personal_postal_addresses_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_postal_addresses_aud (reference_id, personal_identifications_id, is_main, address, additional_info, district_name, town_name, ibge_town_code, country_subdivision, post_code, country, country_code, latitude, longitude, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: personal_qualifications; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_qualifications (reference_id, personal_qualifications_id, account_holder_id, company_cnpj, occupation_code, occupation_description, informed_income_frequency, informed_income_amount, informed_income_currency, informed_income_date, informed_patrimony_amount, informed_patrimony_currency, informed_patrimony_year, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: personal_qualifications_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.personal_qualifications_aud (reference_id, personal_qualifications_id, account_holder_id, company_cnpj, occupation_code, occupation_description, informed_income_frequency, informed_income_amount, informed_income_currency, informed_income_date, informed_patrimony_amount, informed_patrimony_currency, informed_patrimony_year, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: pix_payments; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.pix_payments (reference_id, payment_id, local_instrument, pix_payment_id, creditor_account_id, remittance_information, qr_code, proxy, status, creation_date_time, status_update_date_time, rejection_reason, idempotency_key, payment_consent_id, transaction_identification, end_to_end_id, created_at, updated_at, hibernate_status, created_by, updated_by, cancellation_reason, cancellation_from) FROM stdin;
\.


--
-- Data for Name: pix_payments_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.pix_payments_aud (reference_id, payment_id, local_instrument, pix_payment_id, creditor_account_id, remittance_information, qr_code, proxy, status, creation_date_time, status_update_date_time, rejection_reason, idempotency_key, payment_consent_id, transaction_identification, end_to_end_id, rev, revtype, created_at, updated_at, hibernate_status, created_by, updated_by, cancellation_reason, cancellation_from) FROM stdin;
\.


--
-- Data for Name: pix_payments_payments; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.pix_payments_payments (pix_payment_id, currency, amount, created_at, updated_at, hibernate_status, created_by, updated_by) FROM stdin;
\.


--
-- Data for Name: pix_payments_payments_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.pix_payments_payments_aud (pix_payment_id, currency, amount, created_at, updated_at, hibernate_status, created_by, updated_by, rev, revtype) FROM stdin;
\.


--
-- Data for Name: releases; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.releases (releases_id, is_over_parcel_payment, instalment_id, paid_date, currency, paid_amount, created_at, updated_at, created_by, updated_by, hibernate_status, payments_id, contract_id) FROM stdin;
\.


--
-- Data for Name: releases_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.releases_aud (releases_id, payment_id, is_over_parcel_payment, instalment_id, paid_date, currency, paid_amount, created_at, updated_at, created_by, updated_by, hibernate_status, rev, revtype, payments_id, contract_id) FROM stdin;
\.


--
-- Data for Name: revinfo; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.revinfo (rev, revtstmp) FROM stdin;
\.


--
-- Data for Name: warranties; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.warranties (warranty_id, currency, warranty_type, warranty_subtype, warranty_amount, created_at, updated_at, created_by, updated_by, hibernate_status, contract_id) FROM stdin;
\.


--
-- Data for Name: warranties_aud; Type: TABLE DATA; Schema: public; Owner: test
--

COPY public.warranties_aud (warranty_id, currency, warranty_type, warranty_subtype, warranty_amount, created_at, updated_at, created_by, updated_by, hibernate_status, contract_id, rev, revtype) FROM stdin;
\.


--
-- Name: account_holders_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.account_holders_reference_id_seq', 1, false);


--
-- Name: account_transactions_account_transaction_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.account_transactions_account_transaction_id_seq', 1, false);


--
-- Name: accounts_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.accounts_reference_id_seq', 1, false);


--
-- Name: business_emails_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.business_emails_reference_id_seq', 1, false);


--
-- Name: business_financial_relations_procurators_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.business_financial_relations_procurators_reference_id_seq', 1, false);


--
-- Name: business_financial_relations_products_services_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.business_financial_relations_products_services_reference_id_seq', 1, false);


--
-- Name: business_financial_relations_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.business_financial_relations_reference_id_seq', 1, false);


--
-- Name: business_identifications_company_cnpj_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.business_identifications_company_cnpj_reference_id_seq', 1, false);


--
-- Name: business_identifications_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.business_identifications_reference_id_seq', 1, false);


--
-- Name: business_other_documents_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.business_other_documents_reference_id_seq', 1, false);


--
-- Name: business_parties_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.business_parties_reference_id_seq', 1, false);


--
-- Name: business_phones_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.business_phones_reference_id_seq', 1, false);


--
-- Name: business_postal_addresses_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.business_postal_addresses_reference_id_seq', 1, false);


--
-- Name: business_qualifications_economic_activities_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.business_qualifications_economic_activities_reference_id_seq', 1, false);


--
-- Name: business_qualifications_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.business_qualifications_reference_id_seq', 1, false);


--
-- Name: consent_accounts_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.consent_accounts_reference_id_seq', 1, false);


--
-- Name: consent_contracts_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.consent_contracts_reference_id_seq', 1, false);


--
-- Name: consent_credit_card_accounts_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.consent_credit_card_accounts_reference_id_seq', 1, false);


--
-- Name: consent_permissions_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.consent_permissions_reference_id_seq', 1, false);


--
-- Name: consents_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.consents_reference_id_seq', 1, false);


--
-- Name: creditor_accounts_creditor_account_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.creditor_accounts_creditor_account_id_seq', 1, false);


--
-- Name: creditors_creditor_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.creditors_creditor_id_seq', 1, false);


--
-- Name: hibernate_sequence; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.hibernate_sequence', 12, true);


--
-- Name: jti_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.jti_id_seq', 1, false);


--
-- Name: logged_in_user_entity_documen_logged_in_user_entity_documen_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.logged_in_user_entity_documen_logged_in_user_entity_documen_seq', 1, false);


--
-- Name: payment_consent_details_aud_payment_consent_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.payment_consent_details_aud_payment_consent_details_id_seq', 1, false);


--
-- Name: payment_consent_details_payment_consent_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.payment_consent_details_payment_consent_details_id_seq', 1, false);


--
-- Name: payment_consent_payments_aud_payment_consent_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.payment_consent_payments_aud_payment_consent_details_id_seq', 1, false);


--
-- Name: payment_consent_payments_payment_consent_details_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.payment_consent_payments_payment_consent_details_id_seq', 1, false);


--
-- Name: payment_consent_payments_payment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.payment_consent_payments_payment_id_seq', 1, false);


--
-- Name: payment_consents_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.payment_consents_reference_id_seq', 1, false);


--
-- Name: personal_emails_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.personal_emails_reference_id_seq', 1, false);


--
-- Name: personal_filiation_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.personal_filiation_reference_id_seq', 1, false);


--
-- Name: personal_financial_relations_procurators_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.personal_financial_relations_procurators_reference_id_seq', 1, false);


--
-- Name: personal_financial_relations_products_services_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.personal_financial_relations_products_services_reference_id_seq', 1, false);


--
-- Name: personal_financial_relations_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.personal_financial_relations_reference_id_seq', 1, false);


--
-- Name: personal_identifications_company_cnpj_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.personal_identifications_company_cnpj_reference_id_seq', 1, false);


--
-- Name: personal_identifications_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.personal_identifications_reference_id_seq', 1, false);


--
-- Name: personal_nationality_documents_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.personal_nationality_documents_reference_id_seq', 1, false);


--
-- Name: personal_nationality_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.personal_nationality_reference_id_seq', 1, false);


--
-- Name: personal_other_documents_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.personal_other_documents_reference_id_seq', 1, false);


--
-- Name: personal_phones_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.personal_phones_reference_id_seq', 1, false);


--
-- Name: personal_postal_addresses_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.personal_postal_addresses_reference_id_seq', 1, false);


--
-- Name: personal_qualifications_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.personal_qualifications_reference_id_seq', 1, false);


--
-- Name: pix_payments_payments_pix_payment_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.pix_payments_payments_pix_payment_id_seq', 1, false);


--
-- Name: pix_payments_reference_id_seq; Type: SEQUENCE SET; Schema: public; Owner: test
--

SELECT pg_catalog.setval('public.pix_payments_reference_id_seq', 1, false);


--
-- Name: account_holders account_holders_account_holder_id_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.account_holders
    ADD CONSTRAINT account_holders_account_holder_id_key UNIQUE (account_holder_id);


--
-- Name: account_holders_aud account_holders_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.account_holders_aud
    ADD CONSTRAINT account_holders_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: account_holders account_holders_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.account_holders
    ADD CONSTRAINT account_holders_pkey PRIMARY KEY (reference_id);


--
-- Name: account_transactions_aud account_transactions_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.account_transactions_aud
    ADD CONSTRAINT account_transactions_aud_pkey PRIMARY KEY (account_transaction_id, rev);


--
-- Name: account_transactions account_transactions_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.account_transactions
    ADD CONSTRAINT account_transactions_pkey PRIMARY KEY (account_transaction_id);


--
-- Name: accounts accounts_account_id_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_account_id_key UNIQUE (account_id);


--
-- Name: accounts_aud accounts_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.accounts_aud
    ADD CONSTRAINT accounts_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: accounts accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_pkey PRIMARY KEY (reference_id);


--
-- Name: balloon_payments_aud balloon_payments_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.balloon_payments_aud
    ADD CONSTRAINT balloon_payments_aud_pkey PRIMARY KEY (balloon_payments_id, rev);


--
-- Name: balloon_payments balloon_payments_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.balloon_payments
    ADD CONSTRAINT balloon_payments_pkey PRIMARY KEY (balloon_payments_id);


--
-- Name: business_emails_aud business_emails_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_emails_aud
    ADD CONSTRAINT business_emails_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: business_emails business_emails_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_emails
    ADD CONSTRAINT business_emails_pkey PRIMARY KEY (reference_id);


--
-- Name: business_financial_relations_aud business_financial_relations_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations_aud
    ADD CONSTRAINT business_financial_relations_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: business_financial_relations business_financial_relations_business_financial_relations_i_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations
    ADD CONSTRAINT business_financial_relations_business_financial_relations_i_key UNIQUE (business_financial_relations_id);


--
-- Name: business_financial_relations business_financial_relations_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations
    ADD CONSTRAINT business_financial_relations_pkey PRIMARY KEY (reference_id);


--
-- Name: business_financial_relations_procurators_aud business_financial_relations_procurators_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations_procurators_aud
    ADD CONSTRAINT business_financial_relations_procurators_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: business_financial_relations_procurators business_financial_relations_procurators_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations_procurators
    ADD CONSTRAINT business_financial_relations_procurators_pkey PRIMARY KEY (reference_id);


--
-- Name: business_financial_relations_products_services_type_aud business_financial_relations_products_services_type_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations_products_services_type_aud
    ADD CONSTRAINT business_financial_relations_products_services_type_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: business_financial_relations_products_services_type business_financial_relations_products_services_type_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations_products_services_type
    ADD CONSTRAINT business_financial_relations_products_services_type_pkey PRIMARY KEY (reference_id);


--
-- Name: business_identifications_aud business_identifications_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_identifications_aud
    ADD CONSTRAINT business_identifications_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: business_identifications business_identifications_business_identifications_id_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_identifications
    ADD CONSTRAINT business_identifications_business_identifications_id_key UNIQUE (business_identifications_id);


--
-- Name: business_identifications_company_cnpj_aud business_identifications_company_cnpj_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_identifications_company_cnpj_aud
    ADD CONSTRAINT business_identifications_company_cnpj_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: business_identifications_company_cnpj business_identifications_company_cnpj_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_identifications_company_cnpj
    ADD CONSTRAINT business_identifications_company_cnpj_pkey PRIMARY KEY (reference_id);


--
-- Name: business_identifications business_identifications_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_identifications
    ADD CONSTRAINT business_identifications_pkey PRIMARY KEY (reference_id);


--
-- Name: business_other_documents_aud business_other_documents_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_other_documents_aud
    ADD CONSTRAINT business_other_documents_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: business_other_documents business_other_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_other_documents
    ADD CONSTRAINT business_other_documents_pkey PRIMARY KEY (reference_id);


--
-- Name: business_parties_aud business_parties_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_parties_aud
    ADD CONSTRAINT business_parties_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: business_parties business_parties_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_parties
    ADD CONSTRAINT business_parties_pkey PRIMARY KEY (reference_id);


--
-- Name: business_phones_aud business_phones_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_phones_aud
    ADD CONSTRAINT business_phones_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: business_phones business_phones_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_phones
    ADD CONSTRAINT business_phones_pkey PRIMARY KEY (reference_id);


--
-- Name: business_postal_addresses_aud business_postal_addresses_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_postal_addresses_aud
    ADD CONSTRAINT business_postal_addresses_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: business_postal_addresses business_postal_addresses_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_postal_addresses
    ADD CONSTRAINT business_postal_addresses_pkey PRIMARY KEY (reference_id);


--
-- Name: business_qualifications_aud business_qualifications_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_qualifications_aud
    ADD CONSTRAINT business_qualifications_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: business_qualifications business_qualifications_business_qualifications_id_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_qualifications
    ADD CONSTRAINT business_qualifications_business_qualifications_id_key UNIQUE (business_qualifications_id);


--
-- Name: business_qualifications_economic_activities_aud business_qualifications_economic_activities_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_qualifications_economic_activities_aud
    ADD CONSTRAINT business_qualifications_economic_activities_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: business_qualifications_economic_activities business_qualifications_economic_activities_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_qualifications_economic_activities
    ADD CONSTRAINT business_qualifications_economic_activities_pkey PRIMARY KEY (reference_id);


--
-- Name: business_qualifications business_qualifications_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_qualifications
    ADD CONSTRAINT business_qualifications_pkey PRIMARY KEY (reference_id);


--
-- Name: consent_accounts_aud consent_accounts_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_accounts_aud
    ADD CONSTRAINT consent_accounts_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: consent_accounts consent_accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_accounts
    ADD CONSTRAINT consent_accounts_pkey PRIMARY KEY (reference_id);


--
-- Name: consent_contracts_aud consent_contracts_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_contracts_aud
    ADD CONSTRAINT consent_contracts_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: consent_contracts consent_contracts_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_contracts
    ADD CONSTRAINT consent_contracts_pkey PRIMARY KEY (reference_id);


--
-- Name: consent_credit_card_accounts_aud consent_credit_card_accounts_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_credit_card_accounts_aud
    ADD CONSTRAINT consent_credit_card_accounts_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: consent_credit_card_accounts consent_credit_card_accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_credit_card_accounts
    ADD CONSTRAINT consent_credit_card_accounts_pkey PRIMARY KEY (reference_id);


--
-- Name: consent_permissions_aud consent_permissions_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_permissions_aud
    ADD CONSTRAINT consent_permissions_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: consent_permissions consent_permissions_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_permissions
    ADD CONSTRAINT consent_permissions_pkey PRIMARY KEY (reference_id);


--
-- Name: consents_aud consents_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consents_aud
    ADD CONSTRAINT consents_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: consents consents_new_consent_id_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consents
    ADD CONSTRAINT consents_new_consent_id_key UNIQUE (consent_id);


--
-- Name: consents consents_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consents
    ADD CONSTRAINT consents_pkey PRIMARY KEY (reference_id);


--
-- Name: contracted_fees_aud contracted_fees_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.contracted_fees_aud
    ADD CONSTRAINT contracted_fees_aud_pkey PRIMARY KEY (contracted_fees_id, rev);


--
-- Name: contracted_fees contracted_fees_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.contracted_fees
    ADD CONSTRAINT contracted_fees_pkey PRIMARY KEY (contracted_fees_id);


--
-- Name: contracted_finance_charges_aud contracted_finance_charges_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.contracted_finance_charges_aud
    ADD CONSTRAINT contracted_finance_charges_aud_pkey PRIMARY KEY (contracted_finance_charges_id, rev);


--
-- Name: contracted_finance_charges contracted_finance_charges_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.contracted_finance_charges
    ADD CONSTRAINT contracted_finance_charges_pkey PRIMARY KEY (contracted_finance_charges_id);


--
-- Name: contracts_aud contracts_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.contracts_aud
    ADD CONSTRAINT contracts_aud_pkey PRIMARY KEY (contract_id, rev);


--
-- Name: contracts contracts_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.contracts
    ADD CONSTRAINT contracts_pkey PRIMARY KEY (contract_id);


--
-- Name: credit_card_accounts_aud credit_card_accounts_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_aud
    ADD CONSTRAINT credit_card_accounts_aud_pkey PRIMARY KEY (credit_card_account_id, rev);


--
-- Name: credit_card_accounts_bills_aud credit_card_accounts_bills_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_bills_aud
    ADD CONSTRAINT credit_card_accounts_bills_aud_pkey PRIMARY KEY (bill_id, rev);


--
-- Name: credit_card_accounts_bills_finance_charge_aud credit_card_accounts_bills_finance_charge_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_bills_finance_charge_aud
    ADD CONSTRAINT credit_card_accounts_bills_finance_charge_aud_pkey PRIMARY KEY (finance_charge_id, rev);


--
-- Name: credit_card_accounts_bills_finance_charge credit_card_accounts_bills_finance_charge_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_bills_finance_charge
    ADD CONSTRAINT credit_card_accounts_bills_finance_charge_pkey PRIMARY KEY (finance_charge_id);


--
-- Name: credit_card_accounts_bills_payment_aud credit_card_accounts_bills_payment_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_bills_payment_aud
    ADD CONSTRAINT credit_card_accounts_bills_payment_aud_pkey PRIMARY KEY (payment_id, rev);


--
-- Name: credit_card_accounts_bills_payment credit_card_accounts_bills_payment_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_bills_payment
    ADD CONSTRAINT credit_card_accounts_bills_payment_pkey PRIMARY KEY (payment_id);


--
-- Name: credit_card_accounts_bills credit_card_accounts_bills_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_bills
    ADD CONSTRAINT credit_card_accounts_bills_pkey PRIMARY KEY (bill_id);


--
-- Name: credit_card_accounts_limits_aud credit_card_accounts_limits_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_limits_aud
    ADD CONSTRAINT credit_card_accounts_limits_aud_pkey PRIMARY KEY (limit_id, rev);


--
-- Name: credit_card_accounts_limits credit_card_accounts_limits_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_limits
    ADD CONSTRAINT credit_card_accounts_limits_pkey PRIMARY KEY (limit_id);


--
-- Name: credit_card_accounts credit_card_accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts
    ADD CONSTRAINT credit_card_accounts_pkey PRIMARY KEY (credit_card_account_id);


--
-- Name: credit_card_accounts_transaction_aud credit_card_accounts_transaction_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_transaction_aud
    ADD CONSTRAINT credit_card_accounts_transaction_aud_pkey PRIMARY KEY (transaction_id, rev);


--
-- Name: credit_card_accounts_transaction credit_card_accounts_transaction_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_transaction
    ADD CONSTRAINT credit_card_accounts_transaction_pkey PRIMARY KEY (transaction_id);


--
-- Name: credit_cards_account_payment_method_aud credit_cards_account_payment_method_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_cards_account_payment_method_aud
    ADD CONSTRAINT credit_cards_account_payment_method_aud_pkey PRIMARY KEY (payment_method_id, rev);


--
-- Name: credit_cards_account_payment_method credit_cards_account_payment_method_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_cards_account_payment_method
    ADD CONSTRAINT credit_cards_account_payment_method_pkey PRIMARY KEY (payment_method_id);


--
-- Name: creditor_accounts_aud creditor_accounts_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.creditor_accounts_aud
    ADD CONSTRAINT creditor_accounts_aud_pkey PRIMARY KEY (creditor_account_id, rev);


--
-- Name: creditor_accounts creditor_accounts_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.creditor_accounts
    ADD CONSTRAINT creditor_accounts_pkey PRIMARY KEY (creditor_account_id);


--
-- Name: creditors_aud creditors_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.creditors_aud
    ADD CONSTRAINT creditors_aud_pkey PRIMARY KEY (creditor_id, rev);


--
-- Name: creditors creditors_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.creditors
    ADD CONSTRAINT creditors_pkey PRIMARY KEY (creditor_id);


--
-- Name: flyway_schema_history flyway_schema_history_pk; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.flyway_schema_history
    ADD CONSTRAINT flyway_schema_history_pk PRIMARY KEY (installed_rank);


--
-- Name: interest_rates_aud interest_rates_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.interest_rates_aud
    ADD CONSTRAINT interest_rates_aud_pkey PRIMARY KEY (interest_rates_id, rev);


--
-- Name: interest_rates interest_rates_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.interest_rates
    ADD CONSTRAINT interest_rates_pkey PRIMARY KEY (interest_rates_id);


--
-- Name: jti jti_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.jti
    ADD CONSTRAINT jti_pkey PRIMARY KEY (id);


--
-- Name: logged_in_user_entity_documents_aud logged_in_user_entity_documents_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.logged_in_user_entity_documents_aud
    ADD CONSTRAINT logged_in_user_entity_documents_aud_pkey PRIMARY KEY (logged_in_user_entity_document_id, rev);


--
-- Name: logged_in_user_entity_documents logged_in_user_entity_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.logged_in_user_entity_documents
    ADD CONSTRAINT logged_in_user_entity_documents_pkey PRIMARY KEY (logged_in_user_entity_document_id);


--
-- Name: over_parcel_charges_aud over_parcel_charges_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.over_parcel_charges_aud
    ADD CONSTRAINT over_parcel_charges_aud_pkey PRIMARY KEY (over_parcel_charges_id, rev);


--
-- Name: over_parcel_charges over_parcel_charges_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.over_parcel_charges
    ADD CONSTRAINT over_parcel_charges_pkey PRIMARY KEY (over_parcel_charges_id);


--
-- Name: over_parcel_fees_aud over_parcel_fees_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.over_parcel_fees_aud
    ADD CONSTRAINT over_parcel_fees_aud_pkey PRIMARY KEY (over_parcel_fees_id, rev);


--
-- Name: over_parcel_fees over_parcel_fees_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.over_parcel_fees
    ADD CONSTRAINT over_parcel_fees_pkey PRIMARY KEY (over_parcel_fees_id);


--
-- Name: payment_consent_details_aud payment_consent_details_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consent_details_aud
    ADD CONSTRAINT payment_consent_details_aud_pkey PRIMARY KEY (payment_consent_details_id);


--
-- Name: payment_consent_details payment_consent_details_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consent_details
    ADD CONSTRAINT payment_consent_details_pkey PRIMARY KEY (payment_consent_details_id);


--
-- Name: payment_consent_payments_aud payment_consent_payments_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consent_payments_aud
    ADD CONSTRAINT payment_consent_payments_aud_pkey PRIMARY KEY (payment_id, rev);


--
-- Name: payment_consent_payments payment_consent_payments_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consent_payments
    ADD CONSTRAINT payment_consent_payments_pkey PRIMARY KEY (payment_id);


--
-- Name: payment_consents_aud payment_consents_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consents_aud
    ADD CONSTRAINT payment_consents_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: payment_consents payment_consents_idempotency_key_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consents
    ADD CONSTRAINT payment_consents_idempotency_key_key UNIQUE (idempotency_key);


--
-- Name: payment_consents payment_consents_payment_consent_id_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consents
    ADD CONSTRAINT payment_consents_payment_consent_id_key UNIQUE (payment_consent_id);


--
-- Name: payment_consents payment_consents_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consents
    ADD CONSTRAINT payment_consents_pkey PRIMARY KEY (reference_id);


--
-- Name: payments_simulate_response_aud payments_simulate_response_aud_payment_consent_id_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payments_simulate_response_aud
    ADD CONSTRAINT payments_simulate_response_aud_payment_consent_id_key UNIQUE (payment_consent_id);


--
-- Name: payments_simulate_response_aud payments_simulate_response_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payments_simulate_response_aud
    ADD CONSTRAINT payments_simulate_response_aud_pkey PRIMARY KEY (id, rev);


--
-- Name: payments_simulate_response payments_simulate_response_payment_consent_id_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payments_simulate_response
    ADD CONSTRAINT payments_simulate_response_payment_consent_id_key UNIQUE (payment_consent_id);


--
-- Name: personal_emails_aud personal_emails_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_emails_aud
    ADD CONSTRAINT personal_emails_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: personal_emails personal_emails_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_emails
    ADD CONSTRAINT personal_emails_pkey PRIMARY KEY (reference_id);


--
-- Name: personal_filiation_aud personal_filiation_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_filiation_aud
    ADD CONSTRAINT personal_filiation_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: personal_filiation personal_filiation_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_filiation
    ADD CONSTRAINT personal_filiation_pkey PRIMARY KEY (reference_id);


--
-- Name: personal_financial_relations_aud personal_financial_relations_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations_aud
    ADD CONSTRAINT personal_financial_relations_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: personal_financial_relations personal_financial_relations_personal_financial_relations_i_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations
    ADD CONSTRAINT personal_financial_relations_personal_financial_relations_i_key UNIQUE (personal_financial_relations_id);


--
-- Name: personal_financial_relations personal_financial_relations_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations
    ADD CONSTRAINT personal_financial_relations_pkey PRIMARY KEY (reference_id);


--
-- Name: personal_financial_relations_procurators_aud personal_financial_relations_procurators_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations_procurators_aud
    ADD CONSTRAINT personal_financial_relations_procurators_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: personal_financial_relations_procurators personal_financial_relations_procurators_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations_procurators
    ADD CONSTRAINT personal_financial_relations_procurators_pkey PRIMARY KEY (reference_id);


--
-- Name: personal_financial_relations_products_services_type_aud personal_financial_relations_products_services_type_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations_products_services_type_aud
    ADD CONSTRAINT personal_financial_relations_products_services_type_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: personal_financial_relations_products_services_type personal_financial_relations_products_services_type_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations_products_services_type
    ADD CONSTRAINT personal_financial_relations_products_services_type_pkey PRIMARY KEY (reference_id);


--
-- Name: personal_identifications_aud personal_identifications_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_identifications_aud
    ADD CONSTRAINT personal_identifications_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: personal_identifications_company_cnpj_aud personal_identifications_company_cnpj_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_identifications_company_cnpj_aud
    ADD CONSTRAINT personal_identifications_company_cnpj_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: personal_identifications_company_cnpj personal_identifications_company_cnpj_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_identifications_company_cnpj
    ADD CONSTRAINT personal_identifications_company_cnpj_pkey PRIMARY KEY (reference_id);


--
-- Name: personal_identifications personal_identifications_personal_identifications_id_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_identifications
    ADD CONSTRAINT personal_identifications_personal_identifications_id_key UNIQUE (personal_identifications_id);


--
-- Name: personal_identifications personal_identifications_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_identifications
    ADD CONSTRAINT personal_identifications_pkey PRIMARY KEY (reference_id);


--
-- Name: personal_nationality_aud personal_nationality_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_nationality_aud
    ADD CONSTRAINT personal_nationality_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: personal_nationality_documents_aud personal_nationality_documents_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_nationality_documents_aud
    ADD CONSTRAINT personal_nationality_documents_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: personal_nationality_documents personal_nationality_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_nationality_documents
    ADD CONSTRAINT personal_nationality_documents_pkey PRIMARY KEY (reference_id);


--
-- Name: personal_nationality personal_nationality_personal_nationality_id_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_nationality
    ADD CONSTRAINT personal_nationality_personal_nationality_id_key UNIQUE (personal_nationality_id);


--
-- Name: personal_nationality personal_nationality_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_nationality
    ADD CONSTRAINT personal_nationality_pkey PRIMARY KEY (reference_id);


--
-- Name: personal_other_documents_aud personal_other_documents_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_other_documents_aud
    ADD CONSTRAINT personal_other_documents_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: personal_other_documents personal_other_documents_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_other_documents
    ADD CONSTRAINT personal_other_documents_pkey PRIMARY KEY (reference_id);


--
-- Name: personal_phones_aud personal_phones_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_phones_aud
    ADD CONSTRAINT personal_phones_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: personal_phones personal_phones_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_phones
    ADD CONSTRAINT personal_phones_pkey PRIMARY KEY (reference_id);


--
-- Name: personal_postal_addresses_aud personal_postal_addresses_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_postal_addresses_aud
    ADD CONSTRAINT personal_postal_addresses_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: personal_postal_addresses personal_postal_addresses_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_postal_addresses
    ADD CONSTRAINT personal_postal_addresses_pkey PRIMARY KEY (reference_id);


--
-- Name: personal_qualifications_aud personal_qualifications_aud_personal_qualifications_id_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_qualifications_aud
    ADD CONSTRAINT personal_qualifications_aud_personal_qualifications_id_key UNIQUE (personal_qualifications_id);


--
-- Name: personal_qualifications_aud personal_qualifications_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_qualifications_aud
    ADD CONSTRAINT personal_qualifications_aud_pkey PRIMARY KEY (reference_id, rev);


--
-- Name: personal_qualifications personal_qualifications_personal_qualifications_id_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_qualifications
    ADD CONSTRAINT personal_qualifications_personal_qualifications_id_key UNIQUE (personal_qualifications_id);


--
-- Name: personal_qualifications personal_qualifications_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_qualifications
    ADD CONSTRAINT personal_qualifications_pkey PRIMARY KEY (reference_id);


--
-- Name: pix_payments_aud pix_payments_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.pix_payments_aud
    ADD CONSTRAINT pix_payments_aud_pkey PRIMARY KEY (payment_id, rev);


--
-- Name: pix_payments pix_payments_idempotency_key_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.pix_payments
    ADD CONSTRAINT pix_payments_idempotency_key_key UNIQUE (idempotency_key);


--
-- Name: pix_payments_payments_aud pix_payments_payments_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.pix_payments_payments_aud
    ADD CONSTRAINT pix_payments_payments_aud_pkey PRIMARY KEY (pix_payment_id, rev);


--
-- Name: pix_payments_payments pix_payments_payments_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.pix_payments_payments
    ADD CONSTRAINT pix_payments_payments_pkey PRIMARY KEY (pix_payment_id);


--
-- Name: pix_payments pix_payments_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.pix_payments
    ADD CONSTRAINT pix_payments_pkey PRIMARY KEY (reference_id);


--
-- Name: releases_aud releases_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.releases_aud
    ADD CONSTRAINT releases_aud_pkey PRIMARY KEY (releases_id, rev);


--
-- Name: releases releases_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.releases
    ADD CONSTRAINT releases_pkey PRIMARY KEY (releases_id);


--
-- Name: revinfo revinfo_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.revinfo
    ADD CONSTRAINT revinfo_pkey PRIMARY KEY (rev);


--
-- Name: account_transactions unique_transaction_id; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.account_transactions
    ADD CONSTRAINT unique_transaction_id UNIQUE (transaction_id);


--
-- Name: warranties_aud warranties_aud_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.warranties_aud
    ADD CONSTRAINT warranties_aud_pkey PRIMARY KEY (warranty_id, rev);


--
-- Name: warranties_aud warranties_aud_warranty_id_key; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.warranties_aud
    ADD CONSTRAINT warranties_aud_warranty_id_key UNIQUE (warranty_id);


--
-- Name: warranties warranties_pkey; Type: CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.warranties
    ADD CONSTRAINT warranties_pkey PRIMARY KEY (warranty_id);


--
-- Name: flyway_schema_history_s_idx; Type: INDEX; Schema: public; Owner: test
--

CREATE INDEX flyway_schema_history_s_idx ON public.flyway_schema_history USING btree (success);


--
-- Name: account_holders_aud account_holders_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.account_holders_aud
    ADD CONSTRAINT account_holders_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: account_transactions account_transactions_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.account_transactions
    ADD CONSTRAINT account_transactions_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.accounts(account_id);


--
-- Name: account_transactions_aud account_transactions_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.account_transactions_aud
    ADD CONSTRAINT account_transactions_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: accounts accounts_account_holder_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.accounts
    ADD CONSTRAINT accounts_account_holder_id_fkey FOREIGN KEY (account_holder_id) REFERENCES public.account_holders(account_holder_id);


--
-- Name: accounts_aud accounts_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.accounts_aud
    ADD CONSTRAINT accounts_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: balloon_payments_aud balloon_payments_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.balloon_payments_aud
    ADD CONSTRAINT balloon_payments_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: business_emails_aud business_emails_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_emails_aud
    ADD CONSTRAINT business_emails_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: business_emails business_emails_business_identifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_emails
    ADD CONSTRAINT business_emails_business_identifications_id_fkey FOREIGN KEY (business_identifications_id) REFERENCES public.business_identifications(business_identifications_id);


--
-- Name: business_financial_relations_products_services_type business_financial_relations__business_financial_relations_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations_products_services_type
    ADD CONSTRAINT business_financial_relations__business_financial_relations_fkey FOREIGN KEY (business_financial_relations_id) REFERENCES public.business_financial_relations(business_financial_relations_id);


--
-- Name: business_financial_relations business_financial_relations_account_holder_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations
    ADD CONSTRAINT business_financial_relations_account_holder_id_fkey FOREIGN KEY (account_holder_id) REFERENCES public.account_holders(account_holder_id);


--
-- Name: business_financial_relations_aud business_financial_relations_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations_aud
    ADD CONSTRAINT business_financial_relations_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: business_financial_relations_procurators business_financial_relations_business_financial_relations_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations_procurators
    ADD CONSTRAINT business_financial_relations_business_financial_relations_fkey1 FOREIGN KEY (business_financial_relations_id) REFERENCES public.business_financial_relations(business_financial_relations_id);


--
-- Name: business_financial_relations_procurators_aud business_financial_relations_procurators_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations_procurators_aud
    ADD CONSTRAINT business_financial_relations_procurators_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: business_financial_relations_products_services_type_aud business_financial_relations_products_services_type_au_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_financial_relations_products_services_type_aud
    ADD CONSTRAINT business_financial_relations_products_services_type_au_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: business_identifications business_identifications_account_holder_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_identifications
    ADD CONSTRAINT business_identifications_account_holder_id_fkey FOREIGN KEY (account_holder_id) REFERENCES public.account_holders(account_holder_id);


--
-- Name: business_identifications_aud business_identifications_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_identifications_aud
    ADD CONSTRAINT business_identifications_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: business_identifications_company_cnpj business_identifications_compa_business_identifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_identifications_company_cnpj
    ADD CONSTRAINT business_identifications_compa_business_identifications_id_fkey FOREIGN KEY (business_identifications_id) REFERENCES public.business_identifications(business_identifications_id);


--
-- Name: business_identifications_company_cnpj_aud business_identifications_company_cnpj_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_identifications_company_cnpj_aud
    ADD CONSTRAINT business_identifications_company_cnpj_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: business_other_documents_aud business_other_documents_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_other_documents_aud
    ADD CONSTRAINT business_other_documents_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: business_other_documents business_other_documents_business_identifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_other_documents
    ADD CONSTRAINT business_other_documents_business_identifications_id_fkey FOREIGN KEY (business_identifications_id) REFERENCES public.business_identifications(business_identifications_id);


--
-- Name: business_parties_aud business_parties_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_parties_aud
    ADD CONSTRAINT business_parties_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: business_parties business_parties_business_identifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_parties
    ADD CONSTRAINT business_parties_business_identifications_id_fkey FOREIGN KEY (business_identifications_id) REFERENCES public.business_identifications(business_identifications_id);


--
-- Name: business_phones_aud business_phones_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_phones_aud
    ADD CONSTRAINT business_phones_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: business_phones business_phones_business_identifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_phones
    ADD CONSTRAINT business_phones_business_identifications_id_fkey FOREIGN KEY (business_identifications_id) REFERENCES public.business_identifications(business_identifications_id);


--
-- Name: business_postal_addresses_aud business_postal_addresses_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_postal_addresses_aud
    ADD CONSTRAINT business_postal_addresses_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: business_postal_addresses business_postal_addresses_business_identifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_postal_addresses
    ADD CONSTRAINT business_postal_addresses_business_identifications_id_fkey FOREIGN KEY (business_identifications_id) REFERENCES public.business_identifications(business_identifications_id);


--
-- Name: business_qualifications business_qualifications_account_holder_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_qualifications
    ADD CONSTRAINT business_qualifications_account_holder_id_fkey FOREIGN KEY (account_holder_id) REFERENCES public.account_holders(account_holder_id);


--
-- Name: business_qualifications_aud business_qualifications_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_qualifications_aud
    ADD CONSTRAINT business_qualifications_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: business_qualifications_economic_activities business_qualifications_economi_business_qualifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_qualifications_economic_activities
    ADD CONSTRAINT business_qualifications_economi_business_qualifications_id_fkey FOREIGN KEY (business_qualifications_id) REFERENCES public.business_qualifications(business_qualifications_id);


--
-- Name: business_qualifications_economic_activities_aud business_qualifications_economic_activities_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.business_qualifications_economic_activities_aud
    ADD CONSTRAINT business_qualifications_economic_activities_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: consent_accounts consent_accounts_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_accounts
    ADD CONSTRAINT consent_accounts_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.accounts(account_id);


--
-- Name: consent_accounts_aud consent_accounts_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_accounts_aud
    ADD CONSTRAINT consent_accounts_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: consent_accounts consent_accounts_consent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_accounts
    ADD CONSTRAINT consent_accounts_consent_id_fkey FOREIGN KEY (consent_id) REFERENCES public.consents(consent_id);


--
-- Name: consent_contracts_aud consent_contracts_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_contracts_aud
    ADD CONSTRAINT consent_contracts_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: consent_contracts consent_contracts_consent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_contracts
    ADD CONSTRAINT consent_contracts_consent_id_fkey FOREIGN KEY (consent_id) REFERENCES public.consents(consent_id);


--
-- Name: consent_contracts consent_contracts_contract_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_contracts
    ADD CONSTRAINT consent_contracts_contract_id_fkey FOREIGN KEY (contract_id) REFERENCES public.contracts(contract_id);


--
-- Name: consent_credit_card_accounts_aud consent_credit_card_accounts_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_credit_card_accounts_aud
    ADD CONSTRAINT consent_credit_card_accounts_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: consent_credit_card_accounts consent_credit_card_accounts_consent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_credit_card_accounts
    ADD CONSTRAINT consent_credit_card_accounts_consent_id_fkey FOREIGN KEY (consent_id) REFERENCES public.consents(consent_id);


--
-- Name: consent_credit_card_accounts consent_credit_card_accounts_credit_card_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_credit_card_accounts
    ADD CONSTRAINT consent_credit_card_accounts_credit_card_account_id_fkey FOREIGN KEY (credit_card_account_id) REFERENCES public.credit_card_accounts(credit_card_account_id);


--
-- Name: consent_permissions_aud consent_permissions_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_permissions_aud
    ADD CONSTRAINT consent_permissions_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: consent_permissions consent_permissions_consent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consent_permissions
    ADD CONSTRAINT consent_permissions_consent_id_fkey FOREIGN KEY (consent_id) REFERENCES public.consents(consent_id) ON DELETE CASCADE;


--
-- Name: consents consents_account_holder_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consents
    ADD CONSTRAINT consents_account_holder_id_fkey FOREIGN KEY (account_holder_id) REFERENCES public.account_holders(account_holder_id);


--
-- Name: consents_aud consents_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.consents_aud
    ADD CONSTRAINT consents_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: contracted_fees_aud contracted_fees_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.contracted_fees_aud
    ADD CONSTRAINT contracted_fees_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: contracted_fees contracted_fees_contract_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.contracted_fees
    ADD CONSTRAINT contracted_fees_contract_id_fkey FOREIGN KEY (contract_id) REFERENCES public.contracts(contract_id);


--
-- Name: contracted_finance_charges_aud contracted_finance_charges_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.contracted_finance_charges_aud
    ADD CONSTRAINT contracted_finance_charges_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: contracted_finance_charges contracted_finance_charges_contract_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.contracted_finance_charges
    ADD CONSTRAINT contracted_finance_charges_contract_id_fkey FOREIGN KEY (contract_id) REFERENCES public.contracts(contract_id);


--
-- Name: contracts contracts_account_holder_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.contracts
    ADD CONSTRAINT contracts_account_holder_id_fkey FOREIGN KEY (account_holder_id) REFERENCES public.account_holders(account_holder_id);


--
-- Name: contracts_aud contracts_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.contracts_aud
    ADD CONSTRAINT contracts_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: credit_card_accounts credit_card_accounts_account_holder_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts
    ADD CONSTRAINT credit_card_accounts_account_holder_id_fkey FOREIGN KEY (account_holder_id) REFERENCES public.account_holders(account_holder_id);


--
-- Name: credit_card_accounts_aud credit_card_accounts_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_aud
    ADD CONSTRAINT credit_card_accounts_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: credit_card_accounts_bills_aud credit_card_accounts_bills_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_bills_aud
    ADD CONSTRAINT credit_card_accounts_bills_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: credit_card_accounts_bills credit_card_accounts_bills_credit_card_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_bills
    ADD CONSTRAINT credit_card_accounts_bills_credit_card_account_id_fkey FOREIGN KEY (credit_card_account_id) REFERENCES public.credit_card_accounts(credit_card_account_id);


--
-- Name: credit_card_accounts_bills_finance_charge_aud credit_card_accounts_bills_finance_charge_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_bills_finance_charge_aud
    ADD CONSTRAINT credit_card_accounts_bills_finance_charge_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: credit_card_accounts_bills_finance_charge credit_card_accounts_bills_finance_charge_bill_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_bills_finance_charge
    ADD CONSTRAINT credit_card_accounts_bills_finance_charge_bill_id_fkey FOREIGN KEY (bill_id) REFERENCES public.credit_card_accounts_bills(bill_id);


--
-- Name: credit_card_accounts_bills_payment_aud credit_card_accounts_bills_payment_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_bills_payment_aud
    ADD CONSTRAINT credit_card_accounts_bills_payment_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: credit_card_accounts_bills_payment credit_card_accounts_bills_payment_bill_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_bills_payment
    ADD CONSTRAINT credit_card_accounts_bills_payment_bill_id_fkey FOREIGN KEY (bill_id) REFERENCES public.credit_card_accounts_bills(bill_id);


--
-- Name: credit_card_accounts_limits_aud credit_card_accounts_limits_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_limits_aud
    ADD CONSTRAINT credit_card_accounts_limits_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: credit_card_accounts_limits credit_card_accounts_limits_credit_card_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_limits
    ADD CONSTRAINT credit_card_accounts_limits_credit_card_account_id_fkey FOREIGN KEY (credit_card_account_id) REFERENCES public.credit_card_accounts(credit_card_account_id);


--
-- Name: credit_card_accounts_transaction_aud credit_card_accounts_transaction_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_transaction_aud
    ADD CONSTRAINT credit_card_accounts_transaction_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: credit_card_accounts_transaction credit_card_accounts_transaction_bill_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_transaction
    ADD CONSTRAINT credit_card_accounts_transaction_bill_id_fkey FOREIGN KEY (bill_id) REFERENCES public.credit_card_accounts_bills(bill_id);


--
-- Name: credit_card_accounts_transaction credit_card_accounts_transaction_credit_card_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_card_accounts_transaction
    ADD CONSTRAINT credit_card_accounts_transaction_credit_card_account_id_fkey FOREIGN KEY (credit_card_account_id) REFERENCES public.credit_card_accounts(credit_card_account_id);


--
-- Name: credit_cards_account_payment_method_aud credit_cards_account_payment_method_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_cards_account_payment_method_aud
    ADD CONSTRAINT credit_cards_account_payment_method_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: credit_cards_account_payment_method credit_cards_account_payment_method_credit_card_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.credit_cards_account_payment_method
    ADD CONSTRAINT credit_cards_account_payment_method_credit_card_account_id_fkey FOREIGN KEY (credit_card_account_id) REFERENCES public.credit_card_accounts(credit_card_account_id);


--
-- Name: creditor_accounts_aud creditor_accounts_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.creditor_accounts_aud
    ADD CONSTRAINT creditor_accounts_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: creditors_aud creditors_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.creditors_aud
    ADD CONSTRAINT creditors_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: interest_rates_aud interest_rates_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.interest_rates_aud
    ADD CONSTRAINT interest_rates_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: interest_rates interest_rates_contract_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.interest_rates
    ADD CONSTRAINT interest_rates_contract_id_fkey FOREIGN KEY (contract_id) REFERENCES public.contracts(contract_id);


--
-- Name: logged_in_user_entity_documents_aud logged_in_user_entity_documents_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.logged_in_user_entity_documents_aud
    ADD CONSTRAINT logged_in_user_entity_documents_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: over_parcel_charges_aud over_parcel_charges_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.over_parcel_charges_aud
    ADD CONSTRAINT over_parcel_charges_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: over_parcel_charges over_parcel_charges_releases_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.over_parcel_charges
    ADD CONSTRAINT over_parcel_charges_releases_id_fkey FOREIGN KEY (releases_id) REFERENCES public.releases(releases_id);


--
-- Name: over_parcel_fees_aud over_parcel_fees_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.over_parcel_fees_aud
    ADD CONSTRAINT over_parcel_fees_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: over_parcel_fees over_parcel_fees_releases_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.over_parcel_fees
    ADD CONSTRAINT over_parcel_fees_releases_id_fkey FOREIGN KEY (releases_id) REFERENCES public.releases(releases_id);


--
-- Name: payment_consent_payments payment_consent_details_fk; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consent_payments
    ADD CONSTRAINT payment_consent_details_fk FOREIGN KEY (payment_consent_details_id) REFERENCES public.payment_consent_details(payment_consent_details_id);


--
-- Name: payment_consent_payments_aud payment_consent_payments_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consent_payments_aud
    ADD CONSTRAINT payment_consent_payments_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: payment_consents payment_consents_account_holder_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consents
    ADD CONSTRAINT payment_consents_account_holder_id_fkey FOREIGN KEY (account_holder_id) REFERENCES public.account_holders(account_holder_id);


--
-- Name: payment_consents payment_consents_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consents
    ADD CONSTRAINT payment_consents_account_id_fkey FOREIGN KEY (account_id) REFERENCES public.accounts(account_id) ON DELETE CASCADE;


--
-- Name: payment_consents_aud payment_consents_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consents_aud
    ADD CONSTRAINT payment_consents_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: payment_consents payment_consents_creditor_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consents
    ADD CONSTRAINT payment_consents_creditor_id_fkey FOREIGN KEY (creditor_id) REFERENCES public.creditors(creditor_id);


--
-- Name: payment_consents payment_consents_payment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payment_consents
    ADD CONSTRAINT payment_consents_payment_id_fkey FOREIGN KEY (payment_id) REFERENCES public.payment_consent_payments(payment_id);


--
-- Name: payments_simulate_response_aud payments_simulate_response_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.payments_simulate_response_aud
    ADD CONSTRAINT payments_simulate_response_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: personal_emails_aud personal_emails_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_emails_aud
    ADD CONSTRAINT personal_emails_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: personal_emails personal_emails_personal_identifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_emails
    ADD CONSTRAINT personal_emails_personal_identifications_id_fkey FOREIGN KEY (personal_identifications_id) REFERENCES public.personal_identifications(personal_identifications_id);


--
-- Name: personal_filiation_aud personal_filiation_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_filiation_aud
    ADD CONSTRAINT personal_filiation_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: personal_filiation personal_filiation_personal_identifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_filiation
    ADD CONSTRAINT personal_filiation_personal_identifications_id_fkey FOREIGN KEY (personal_identifications_id) REFERENCES public.personal_identifications(personal_identifications_id);


--
-- Name: personal_financial_relations_products_services_type personal_financial_relations__personal_financial_relations_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations_products_services_type
    ADD CONSTRAINT personal_financial_relations__personal_financial_relations_fkey FOREIGN KEY (personal_financial_relations_id) REFERENCES public.personal_financial_relations(personal_financial_relations_id);


--
-- Name: personal_financial_relations personal_financial_relations_account_holder_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations
    ADD CONSTRAINT personal_financial_relations_account_holder_id_fkey FOREIGN KEY (account_holder_id) REFERENCES public.account_holders(account_holder_id);


--
-- Name: personal_financial_relations_aud personal_financial_relations_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations_aud
    ADD CONSTRAINT personal_financial_relations_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: personal_financial_relations_procurators personal_financial_relations_personal_financial_relations_fkey1; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations_procurators
    ADD CONSTRAINT personal_financial_relations_personal_financial_relations_fkey1 FOREIGN KEY (personal_financial_relations_id) REFERENCES public.personal_financial_relations(personal_financial_relations_id);


--
-- Name: personal_financial_relations_procurators_aud personal_financial_relations_procurators_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations_procurators_aud
    ADD CONSTRAINT personal_financial_relations_procurators_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: personal_financial_relations_products_services_type_aud personal_financial_relations_products_services_type_au_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_financial_relations_products_services_type_aud
    ADD CONSTRAINT personal_financial_relations_products_services_type_au_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: personal_identifications personal_identifications_account_holder_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_identifications
    ADD CONSTRAINT personal_identifications_account_holder_id_fkey FOREIGN KEY (account_holder_id) REFERENCES public.account_holders(account_holder_id);


--
-- Name: personal_identifications_aud personal_identifications_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_identifications_aud
    ADD CONSTRAINT personal_identifications_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: personal_identifications_company_cnpj personal_identifications_compa_personal_identifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_identifications_company_cnpj
    ADD CONSTRAINT personal_identifications_compa_personal_identifications_id_fkey FOREIGN KEY (personal_identifications_id) REFERENCES public.personal_identifications(personal_identifications_id);


--
-- Name: personal_identifications_company_cnpj_aud personal_identifications_company_cnpj_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_identifications_company_cnpj_aud
    ADD CONSTRAINT personal_identifications_company_cnpj_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: personal_nationality_aud personal_nationality_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_nationality_aud
    ADD CONSTRAINT personal_nationality_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: personal_nationality_documents_aud personal_nationality_documents_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_nationality_documents_aud
    ADD CONSTRAINT personal_nationality_documents_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: personal_nationality_documents personal_nationality_documents_personal_nationality_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_nationality_documents
    ADD CONSTRAINT personal_nationality_documents_personal_nationality_id_fkey FOREIGN KEY (personal_nationality_id) REFERENCES public.personal_nationality(personal_nationality_id);


--
-- Name: personal_nationality personal_nationality_personal_identifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_nationality
    ADD CONSTRAINT personal_nationality_personal_identifications_id_fkey FOREIGN KEY (personal_identifications_id) REFERENCES public.personal_identifications(personal_identifications_id);


--
-- Name: personal_other_documents_aud personal_other_documents_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_other_documents_aud
    ADD CONSTRAINT personal_other_documents_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: personal_other_documents personal_other_documents_personal_identifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_other_documents
    ADD CONSTRAINT personal_other_documents_personal_identifications_id_fkey FOREIGN KEY (personal_identifications_id) REFERENCES public.personal_identifications(personal_identifications_id);


--
-- Name: personal_phones_aud personal_phones_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_phones_aud
    ADD CONSTRAINT personal_phones_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: personal_phones personal_phones_personal_identifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_phones
    ADD CONSTRAINT personal_phones_personal_identifications_id_fkey FOREIGN KEY (personal_identifications_id) REFERENCES public.personal_identifications(personal_identifications_id);


--
-- Name: personal_postal_addresses_aud personal_postal_addresses_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_postal_addresses_aud
    ADD CONSTRAINT personal_postal_addresses_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: personal_postal_addresses personal_postal_addresses_personal_identifications_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_postal_addresses
    ADD CONSTRAINT personal_postal_addresses_personal_identifications_id_fkey FOREIGN KEY (personal_identifications_id) REFERENCES public.personal_identifications(personal_identifications_id);


--
-- Name: personal_qualifications personal_qualifications_account_holder_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_qualifications
    ADD CONSTRAINT personal_qualifications_account_holder_id_fkey FOREIGN KEY (account_holder_id) REFERENCES public.account_holders(account_holder_id);


--
-- Name: personal_qualifications_aud personal_qualifications_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.personal_qualifications_aud
    ADD CONSTRAINT personal_qualifications_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: pix_payments_aud pix_payments_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.pix_payments_aud
    ADD CONSTRAINT pix_payments_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: pix_payments pix_payments_creditor_account_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.pix_payments
    ADD CONSTRAINT pix_payments_creditor_account_id_fkey FOREIGN KEY (creditor_account_id) REFERENCES public.creditor_accounts(creditor_account_id);


--
-- Name: pix_payments pix_payments_payment_consent_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.pix_payments
    ADD CONSTRAINT pix_payments_payment_consent_id_fkey FOREIGN KEY (payment_consent_id) REFERENCES public.payment_consents(payment_consent_id);


--
-- Name: pix_payments_payments_aud pix_payments_payments_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.pix_payments_payments_aud
    ADD CONSTRAINT pix_payments_payments_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: pix_payments pix_payments_pix_payment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.pix_payments
    ADD CONSTRAINT pix_payments_pix_payment_id_fkey FOREIGN KEY (pix_payment_id) REFERENCES public.pix_payments_payments(pix_payment_id);


--
-- Name: releases_aud releases_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.releases_aud
    ADD CONSTRAINT releases_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: releases releases_contract_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.releases
    ADD CONSTRAINT releases_contract_id_fkey FOREIGN KEY (contract_id) REFERENCES public.contracts(contract_id);


--
-- Name: warranties_aud warranties_aud_rev_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.warranties_aud
    ADD CONSTRAINT warranties_aud_rev_fkey FOREIGN KEY (rev) REFERENCES public.revinfo(rev);


--
-- Name: warranties warranties_contract_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: test
--

ALTER TABLE ONLY public.warranties
    ADD CONSTRAINT warranties_contract_id_fkey FOREIGN KEY (contract_id) REFERENCES public.contracts(contract_id);


--
-- PostgreSQL database dump complete
--

