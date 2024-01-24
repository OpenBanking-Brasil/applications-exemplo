CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SEQUENCE IF NOT EXISTS hibernate_sequence INCREMENT 1 START 1 MINVALUE 1;

CREATE TABLE revinfo
(
    rev      SERIAL PRIMARY KEY NOT NULL,
    revtstmp bigint
);

CREATE TABLE account_holders
(
    reference_id            SERIAL PRIMARY KEY                     NOT NULL,
    account_holder_id       uuid UNIQUE DEFAULT uuid_generate_v4() NOT NULL,
    user_id                 varchar,
    document_identification varchar(11),
    document_rel            varchar(3),
    account_holder_name     varchar
);

CREATE TABLE account_holders_aud
(
    reference_id            integer NOT NULL,
    account_holder_id       uuid,
    user_id                 varchar,
    document_identification varchar(11),
    document_rel            varchar(3),
    account_holder_name     varchar,
    rev                     integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE accounts
(
    reference_id                           SERIAL PRIMARY KEY                     NOT NULL,
    account_id                             uuid UNIQUE DEFAULT uuid_generate_v4() NOT NULL,
    status                                 varchar,
    currency                               varchar,
    account_type                           varchar,
    account_sub_type                       varchar,
    brand_name                             varchar(80),
    company_cnpj                           varchar(14),
    compe_code                             varchar(3),
    branch_code                            varchar(4),
    number                                 varchar(20),
    check_digit                            varchar(1),
    available_amount                       double precision,
    available_amount_currency              varchar,
    blocked_amount                         double precision,
    blocked_amount_currency                varchar,
    automatically_invested_amount          double precision,
    automatically_invested_amount_currency varchar,
    overdraft_contracted_limit_currency    varchar,
    overdraft_used_limit_currency          varchar,
    unarranged_overdraft_amount_currency   varchar,
    account_holder_id                      uuid                                   NOT NULL,
    overdraft_contracted_limit             double precision,
    overdraft_used_limit                   double precision,
    unarranged_overdraft_amount            double precision,
    debtor_ispb                            varchar,
    debtor_issuer                          varchar,
    debtor_type                            varchar,
    FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE
);

CREATE TABLE accounts_aud
(
    reference_id                           integer NOT NULL,
    account_id                             uuid,
    status                                 varchar,
    currency                               varchar,
    account_type                           varchar,
    account_sub_type                       varchar,
    brand_name                             varchar(80),
    company_cnpj                           varchar(14),
    compe_code                             varchar(3),
    branch_code                            varchar(4),
    number                                 varchar(20),
    check_digit                            varchar(1),
    available_amount                       double precision,
    available_amount_currency              varchar,
    blocked_amount                         double precision,
    blocked_amount_currency                varchar,
    automatically_invested_amount          double precision,
    automatically_invested_amount_currency varchar,
    overdraft_contracted_limit_currency    varchar,
    overdraft_used_limit_currency          varchar,
    unarranged_overdraft_amount_currency   varchar,
    account_holder_id                      uuid,
    overdraft_contracted_limit             double precision,
    overdraft_used_limit                   double precision,
    unarranged_overdraft_amount            double precision,
    debtor_ispb                            varchar,
    debtor_issuer                          varchar,
    debtor_type                            varchar,
    rev                                    integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE account_transactions
(
    account_transaction_id            SERIAL PRIMARY KEY                     NOT NULL,
    account_id                        uuid                                   NOT NULL,
    transaction_id                    uuid UNIQUE DEFAULT uuid_generate_v4() NOT NULL,
    completed_authorised_payment_type varchar,
    credit_debit_type                 varchar,
    transaction_name                  varchar,
    type                              varchar,
    amount                            double precision,
    transaction_currency              varchar,
    transaction_date                  date,
    partie_cnpj_cpf                   varchar,
    partie_person_type                varchar,
    partie_compe_code                 varchar,
    partie_branch_code                varchar,
    partie_number                     varchar,
    partie_check_digit                varchar,
    FOREIGN KEY (account_id) REFERENCES accounts (account_id) ON DELETE CASCADE
);

CREATE TABLE account_transactions_aud
(
    account_transaction_id            integer NOT NULL,
    account_id                        uuid,
    transaction_id                    uuid,
    completed_authorised_payment_type varchar,
    credit_debit_type                 varchar,
    transaction_name                  varchar,
    type                              varchar,
    amount                            double precision,
    transaction_currency              varchar,
    transaction_date                  date,
    partie_cnpj_cpf                   varchar,
    partie_person_type                varchar,
    partie_compe_code                 varchar,
    partie_branch_code                varchar,
    partie_number                     varchar,
    partie_check_digit                varchar,
    rev                               integer NOT NULL,
    PRIMARY KEY (account_transaction_id, rev)
);

CREATE TABLE balloon_payments
(
    balloon_payments_id uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    contract_id         uuid,
    due_date            varchar                                     NOT NULL,
    currency            varchar                                     NOT NULL,
    amount              double precision                            NOT NULL
);

CREATE TABLE balloon_payments_aud
(
    balloon_payments_id uuid    NOT NULL,
    contract_id         uuid,
    due_date            varchar,
    currency            varchar,
    amount              double precision,
    rev                 integer NOT NULL,
    PRIMARY KEY (balloon_payments_id, rev)
);

CREATE TABLE business_identifications
(
    business_identifications_id uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    account_holder_id           uuid                                        NOT NULL,
    brand_name                  varchar(80),
    company_name                varchar(70),
    trade_name                  varchar(70),
    incorporation_date          date,
    cnpj_number                 varchar(14),
    FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE
);

CREATE TABLE business_identifications_aud
(
    business_identifications_id uuid    NOT NULL,
    account_holder_id           uuid,
    brand_name                  varchar(80),
    company_name                varchar(70),
    trade_name                  varchar(70),
    incorporation_date          date,
    cnpj_number                 varchar(14),
    rev                         integer NOT NULL,
    PRIMARY KEY (business_identifications_id, rev)
);

CREATE TABLE business_emails
(
    reference_id                SERIAL PRIMARY KEY NOT NULL,
    business_identifications_id uuid               NOT NULL,
    is_main                     boolean,
    email                       varchar(320),
    FOREIGN KEY (business_identifications_id) REFERENCES business_identifications (business_identifications_id) ON DELETE CASCADE
);

CREATE TABLE business_emails_aud
(
    reference_id                integer NOT NULL,
    business_identifications_id uuid,
    is_main                     boolean,
    email                       varchar(320),
    rev                         integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE business_financial_relations
(
    business_financial_relations_id uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    account_holder_id               uuid                                        NOT NULL,
    start_date                      date,
    FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE
);

CREATE TABLE business_financial_relations_aud
(
    business_financial_relations_id uuid,
    account_holder_id               uuid,
    start_date                      date,
    rev                             integer NOT NULL,
    PRIMARY KEY (business_financial_relations_id, rev)
);

CREATE TABLE business_financial_relations_procurators
(
    reference_id                    SERIAL PRIMARY KEY NOT NULL,
    business_financial_relations_id uuid               NOT NULL,
    type                            varchar(19),
    cnpj_cpf_number                 varchar(11),
    civil_name                      varchar(70),
    social_name                     varchar(70),
    FOREIGN KEY (business_financial_relations_id) REFERENCES business_financial_relations (business_financial_relations_id) ON DELETE CASCADE
);

CREATE TABLE business_financial_relations_procurators_aud
(
    reference_id                    integer NOT NULL,
    business_financial_relations_id uuid,
    type                            varchar(19),
    cnpj_cpf_number                 varchar(11),
    civil_name                      varchar(70),
    social_name                     varchar(70),
    rev                             integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE business_financial_relations_products_services_type
(
    reference_id                    SERIAL PRIMARY KEY NOT NULL,
    business_financial_relations_id uuid               NOT NULL,
    type                            varchar,
    FOREIGN KEY (business_financial_relations_id) REFERENCES business_financial_relations (business_financial_relations_id) ON DELETE CASCADE
);

CREATE TABLE business_financial_relations_products_services_type_aud
(
    reference_id                    integer NOT NULL,
    business_financial_relations_id uuid,
    type                            varchar,
    rev                             integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE business_identifications_company_cnpj
(
    reference_id                SERIAL PRIMARY KEY NOT NULL,
    business_identifications_id uuid               NOT NULL,
    company_cnpj                varchar(14),
    FOREIGN KEY (business_identifications_id) REFERENCES business_identifications (business_identifications_id) ON DELETE CASCADE
);

CREATE TABLE business_identifications_company_cnpj_aud
(
    reference_id                integer NOT NULL,
    business_identifications_id uuid,
    company_cnpj                varchar(14),
    rev                         integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE business_other_documents
(
    reference_id                SERIAL PRIMARY KEY NOT NULL,
    business_identifications_id uuid               NOT NULL,
    type                        varchar(20),
    number                      varchar(20),
    country                     varchar(3),
    expiration_date             date,
    FOREIGN KEY (business_identifications_id) REFERENCES business_identifications (business_identifications_id) ON DELETE CASCADE
);

CREATE TABLE business_other_documents_aud
(
    reference_id                integer NOT NULL,
    business_identifications_id uuid,
    type                        varchar(20),
    number                      varchar(20),
    country                     varchar(3),
    expiration_date             date,
    rev                         integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE business_parties
(
    reference_id                SERIAL PRIMARY KEY NOT NULL,
    business_identifications_id uuid               NOT NULL,
    person_type                 varchar,
    type                        varchar(13),
    civil_name                  varchar(70),
    social_name                 varchar(70),
    company_name                varchar(70),
    trade_name                  varchar(70),
    start_date                  date,
    shareholding                varchar(4),
    document_type               varchar,
    document_number             varchar(20),
    document_additional_info    varchar(100),
    document_country            varchar(3),
    document_expiration_date    date,
    document_issue_date         date,
    FOREIGN KEY (business_identifications_id) REFERENCES business_identifications (business_identifications_id) ON DELETE CASCADE
);

CREATE TABLE business_parties_aud
(
    reference_id                integer NOT NULL,
    business_identifications_id uuid,
    person_type                 varchar,
    type                        varchar(13),
    civil_name                  varchar(70),
    social_name                 varchar(70),
    company_name                varchar(70),
    trade_name                  varchar(70),
    start_date                  date,
    shareholding                varchar(4),
    document_type               varchar,
    document_number             varchar(20),
    document_additional_info    varchar(100),
    document_country            varchar(3),
    document_expiration_date    date,
    document_issue_date         date,
    rev                         integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE business_phones
(
    reference_id                SERIAL PRIMARY KEY NOT NULL,
    business_identifications_id uuid               NOT NULL,
    is_main                     boolean,
    type                        varchar(5),
    additional_info             varchar(70),
    country_calling_code        varchar(4),
    area_code                   varchar(2),
    number                      varchar(11),
    phone_extension             varchar(5),
    FOREIGN KEY (business_identifications_id) REFERENCES business_identifications (business_identifications_id) ON DELETE CASCADE
);

CREATE TABLE business_phones_aud
(
    reference_id                integer NOT NULL,
    business_identifications_id uuid,
    is_main                     boolean,
    type                        varchar(5),
    additional_info             varchar(70),
    country_calling_code        varchar(4),
    area_code                   varchar(2),
    number                      varchar(11),
    phone_extension             varchar(5),
    rev                         integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE business_postal_addresses
(
    reference_id                SERIAL PRIMARY KEY NOT NULL,
    business_identifications_id uuid               NOT NULL,
    is_main                     boolean,
    address                     varchar(150),
    additional_info             varchar(30),
    district_name               varchar(50),
    town_name                   varchar(50),
    ibge_town_code              varchar(7),
    country_subdivision         varchar,
    post_code                   varchar(8),
    country                     varchar(80),
    country_code                varchar(3),
    latitude                    varchar(13),
    longitude                   varchar(13),
    FOREIGN KEY (business_identifications_id) REFERENCES business_identifications (business_identifications_id) ON DELETE CASCADE
);

CREATE TABLE business_postal_addresses_aud
(
    reference_id                integer NOT NULL,
    business_identifications_id uuid,
    is_main                     boolean,
    address                     varchar(150),
    additional_info             varchar(30),
    district_name               varchar(50),
    town_name                   varchar(50),
    ibge_town_code              varchar(7),
    country_subdivision         varchar,
    post_code                   varchar(8),
    country                     varchar(80),
    country_code                varchar(3),
    latitude                    varchar(13),
    longitude                   varchar(13),
    rev                         integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE business_qualifications
(
    business_qualifications_id                        uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    account_holder_id                                 uuid                                        NOT NULL,
    informed_revenue_frequency                        varchar,
    informed_revenue_frequency_additional_information varchar(100),
    informed_revenue_amount                           numeric,
    informed_revenue_currency                         varchar(3),
    informed_revenue_year                             integer,
    informed_patrimony_amount                         double precision,
    informed_patrimony_currency                       varchar(3),
    informed_patrimony_date                           date,
    FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE
);

CREATE TABLE business_qualifications_aud
(
    business_qualifications_id                        uuid,
    account_holder_id                                 uuid,
    informed_revenue_frequency                        varchar,
    informed_revenue_frequency_additional_information varchar(100),
    informed_revenue_amount                           numeric,
    informed_revenue_currency                         varchar(3),
    informed_revenue_year                             integer,
    informed_patrimony_amount                         double precision,
    informed_patrimony_currency                       varchar(3),
    informed_patrimony_date                           date,
    rev                                               integer NOT NULL,
    PRIMARY KEY (business_qualifications_id, rev)
);


CREATE TABLE business_qualifications_economic_activities
(
    reference_id               SERIAL PRIMARY KEY NOT NULL,
    business_qualifications_id uuid               NOT NULL,
    code                       integer,
    is_main                    boolean,
    FOREIGN KEY (business_qualifications_id) REFERENCES business_qualifications (business_qualifications_id) ON DELETE CASCADE
);

CREATE TABLE business_qualifications_economic_activities_aud
(
    reference_id               integer NOT NULL,
    business_qualifications_id uuid,
    code                       integer,
    is_main                    boolean,
    rev                        integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE contracts
(
    contract_id                            uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    contract_type                          varchar,
    company_cnpj                           varchar,
    product_name                           varchar,
    product_type                           varchar,
    product_sub_type                       varchar,
    contract_amount                        double precision,
    currency                               varchar,
    instalment_periodicity                 varchar,
    instalment_periodicity_additional_info varchar,
    cet                                    double precision,
    amortization_scheduled                 varchar,
    amortization_scheduled_additional_info varchar,
    ipoc_code                              varchar                                     NOT NULL,
    account_holder_id                      uuid                                        NOT NULL,
    paid_instalments                       integer,
    contract_outstanding_balance           double precision,
    type_number_of_instalments             varchar,
    total_number_of_instalments            integer,
    type_contract_remaining                varchar,
    contract_remaining_number              integer,
    due_instalments                        integer,
    past_due_instalments                   integer,
    status                                 varchar,
    contract_date                          date,
    disbursement_date                      date,
    settlement_date                        date,
    due_date                               date,
    first_instalment_due_date              date,
    contract_number                        varchar,
    FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE
);

CREATE TABLE contracts_aud
(
    contract_id                            uuid    NOT NULL,
    contract_type                          varchar,
    company_cnpj                           varchar,
    product_name                           varchar,
    product_type                           varchar,
    product_sub_type                       varchar,
    contract_amount                        double precision,
    currency                               varchar,
    instalment_periodicity                 varchar,
    instalment_periodicity_additional_info varchar,
    cet                                    double precision,
    amortization_scheduled                 varchar,
    amortization_scheduled_additional_info varchar,
    ipoc_code                              varchar,
    account_holder_id                      uuid,
    paid_instalments                       integer,
    contract_outstanding_balance           double precision,
    type_number_of_instalments             varchar,
    total_number_of_instalments            integer,
    type_contract_remaining                varchar,
    contract_remaining_number              integer,
    due_instalments                        integer,
    past_due_instalments                   integer,
    status                                 varchar,
    contract_date                          date,
    disbursement_date                      date,
    settlement_date                        date,
    due_date                               date,
    first_instalment_due_date              date,
    contract_number                        varchar,
    rev                                    integer NOT NULL,
    PRIMARY KEY (contract_id, rev)
);

CREATE TABLE contracted_fees
(
    contracted_fees_id uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    contract_id        uuid                                        NOT NULL,
    fee_name           varchar,
    fee_code           varchar,
    fee_charge_type    varchar,
    fee_charge         varchar,
    fee_amount         double precision,
    fee_rate           double precision,
    FOREIGN KEY (contract_id) REFERENCES contracts (contract_id) ON DELETE CASCADE
);

CREATE TABLE contracted_fees_aud
(
    contracted_fees_id uuid    NOT NULL,
    contract_id        uuid,
    fee_name           varchar,
    fee_code           varchar,
    fee_charge_type    varchar,
    fee_charge         varchar,
    fee_amount         double precision,
    fee_rate           double precision,
    rev                integer NOT NULL,
    PRIMARY KEY (contracted_fees_id, rev)
);


CREATE TABLE contracted_finance_charges
(
    contracted_finance_charges_id uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    contract_id                   uuid                                        NOT NULL,
    charge_type                   varchar,
    charge_additional_info        varchar,
    charge_rate                   double precision,
    FOREIGN KEY (contract_id) REFERENCES contracts (contract_id) ON DELETE CASCADE
);

CREATE TABLE contracted_finance_charges_aud
(
    contracted_finance_charges_id uuid    NOT NULL,
    contract_id                   uuid,
    charge_type                   varchar,
    charge_additional_info        varchar,
    charge_rate                   double precision,
    rev                           integer NOT NULL,
    PRIMARY KEY (contracted_finance_charges_id, rev)
);

CREATE TABLE credit_card_accounts
(
    credit_card_account_id  uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    brand_name              varchar(80),
    company_cnpj            varchar(14),
    name                    varchar(50),
    product_type            varchar(26),
    product_additional_info varchar(50),
    credit_card_network     varchar(17),
    network_additional_info varchar(50),
    status                  varchar,
    account_holder_id       uuid                                        NOT NULL,
    FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE
);

CREATE TABLE credit_card_accounts_aud
(
    credit_card_account_id  uuid    NOT NULL,
    brand_name              varchar(80),
    company_cnpj            varchar(14),
    name                    varchar(50),
    product_type            varchar(26),
    product_additional_info varchar(50),
    credit_card_network     varchar(17),
    network_additional_info varchar(50),
    status                  varchar,
    account_holder_id       uuid,
    rev                     integer NOT NULL,
    PRIMARY KEY (credit_card_account_id, rev)
);

CREATE TABLE credit_card_accounts_bills
(
    bill_id                      uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    due_date                     date,
    bill_total_amount            double precision,
    bill_total_amount_currency   varchar(3),
    bill_minimum_amount          double precision,
    bill_minimum_amount_currency varchar(3),
    is_instalment                boolean,
    credit_card_account_id       uuid                                        NOT NULL,
    FOREIGN KEY (credit_card_account_id) REFERENCES credit_card_accounts (credit_card_account_id) ON DELETE CASCADE
);

CREATE TABLE credit_card_accounts_bills_aud
(
    bill_id                      uuid    NOT NULL,
    due_date                     date,
    bill_total_amount            double precision,
    bill_total_amount_currency   varchar(3),
    bill_minimum_amount          double precision,
    bill_minimum_amount_currency varchar(3),
    is_instalment                boolean,
    credit_card_account_id       uuid,
    rev                          integer NOT NULL,
    PRIMARY KEY (bill_id, rev)
);

CREATE TABLE credit_card_accounts_bills_finance_charge
(
    finance_charge_id uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    type              varchar(44),
    additional_info   varchar(140),
    amount            double precision,
    currency          varchar(3),
    bill_id           uuid                                        NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES credit_card_accounts_bills (bill_id) ON DELETE CASCADE
);

CREATE TABLE credit_card_accounts_bills_finance_charge_aud
(
    finance_charge_id uuid    NOT NULL,
    type              varchar(44),
    additional_info   varchar(140),
    amount            double precision,
    currency          varchar(3),
    bill_id           uuid,
    rev               integer NOT NULL,
    PRIMARY KEY (finance_charge_id, rev)
);

CREATE TABLE credit_card_accounts_bills_payment
(
    payment_id   uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    value_type   varchar(32),
    payment_date date,
    payment_mode varchar(21),
    amount       double precision,
    currency     varchar(3),
    bill_id      uuid                                        NOT NULL,
    FOREIGN KEY (bill_id) REFERENCES credit_card_accounts_bills (bill_id) ON DELETE CASCADE
);

CREATE TABLE credit_card_accounts_bills_payment_aud
(
    payment_id   uuid    NOT NULL,
    value_type   varchar(32),
    payment_date date,
    payment_mode varchar(21),
    amount       double precision,
    currency     varchar(3),
    bill_id      uuid,
    rev          integer NOT NULL,
    PRIMARY KEY (payment_id, rev)
);

CREATE TABLE credit_card_accounts_limits
(
    limit_id                  uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    credit_line_limit_type    varchar(34),
    consolidation_type        varchar(11),
    identification_number     varchar(100),
    line_name                 varchar(28),
    line_name_additional_info varchar,
    is_limit_flexible         boolean,
    limit_amount_currency     varchar(3),
    limit_amount              double precision,
    used_amount_currency      varchar(3),
    used_amount               double precision,
    available_amount_currency varchar(3),
    available_amount          double precision,
    credit_card_account_id    uuid                                        NOT NULL,
    FOREIGN KEY (credit_card_account_id) REFERENCES credit_card_accounts (credit_card_account_id) ON DELETE CASCADE
);

CREATE TABLE credit_card_accounts_limits_aud
(
    limit_id                  uuid    NOT NULL,
    credit_line_limit_type    varchar(34),
    consolidation_type        varchar(11),
    identification_number     varchar(100),
    line_name                 varchar(28),
    line_name_additional_info varchar,
    is_limit_flexible         boolean,
    limit_amount_currency     varchar(3),
    limit_amount              double precision,
    used_amount_currency      varchar(3),
    used_amount               double precision,
    available_amount_currency varchar(3),
    available_amount          double precision,
    credit_card_account_id    uuid,
    rev                       integer NOT NULL,
    PRIMARY KEY (limit_id, rev)
);

CREATE TABLE credit_card_accounts_transaction
(
    transaction_id                uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    identification_number         varchar(100),
    line_name                     varchar(28),
    transaction_name              varchar(100),
    bill_id                       uuid                                        NOT NULL,
    credit_card_account_id        uuid,
    credit_debit_type             varchar(7),
    transaction_type              varchar(36),
    transactional_additional_info varchar(140),
    payment_type                  varchar(7),
    fee_type                      varchar(29),
    fee_type_additional_info      varchar(140),
    other_credits_type            varchar(19),
    other_credits_additional_info varchar(50),
    charge_identificator          varchar(50),
    charge_number                 bigint,
    brazilian_amount              double precision,
    amount                        double precision,
    currency                      varchar(3),
    transaction_date              date,
    bill_post_date                date,
    payee_mcc                     bigint,
    FOREIGN KEY (bill_id) REFERENCES credit_card_accounts_bills (bill_id) ON DELETE CASCADE,
    FOREIGN KEY (credit_card_account_id) REFERENCES credit_card_accounts (credit_card_account_id) ON DELETE CASCADE
);

CREATE TABLE credit_card_accounts_transaction_aud
(
    transaction_id                uuid    NOT NULL,
    identification_number         varchar(100),
    line_name                     varchar(28),
    transaction_name              varchar(100),
    bill_id                       uuid,
    credit_card_account_id        uuid,
    credit_debit_type             varchar(7),
    transaction_type              varchar(36),
    transactional_additional_info varchar(140),
    payment_type                  varchar(7),
    fee_type                      varchar(29),
    fee_type_additional_info      varchar(140),
    other_credits_type            varchar(19),
    other_credits_additional_info varchar(50),
    charge_identificator          varchar(50),
    charge_number                 bigint,
    brazilian_amount              double precision,
    amount                        double precision,
    currency                      varchar(3),
    transaction_date              date,
    bill_post_date                date,
    payee_mcc                     bigint,
    rev                           integer NOT NULL,
    PRIMARY KEY (transaction_id, rev)
);

CREATE TABLE credit_cards_account_payment_method
(
    payment_method_id       uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    identification_number   varchar,
    is_multiple_credit_card boolean,
    credit_card_account_id  uuid                                        NOT NULL,
    FOREIGN KEY (credit_card_account_id) REFERENCES credit_card_accounts (credit_card_account_id) ON DELETE CASCADE
);

CREATE TABLE credit_cards_account_payment_method_aud
(
    payment_method_id       uuid    NOT NULL,
    identification_number   varchar,
    is_multiple_credit_card boolean,
    credit_card_account_id  uuid,
    rev                     integer NOT NULL,
    PRIMARY KEY (payment_method_id, rev)
);

CREATE TABLE consents
(
    reference_id                     SERIAL PRIMARY KEY NOT NULL,
    consent_id                       text UNIQUE,
    account_holder_id                uuid,
    expiration_date_time             timestamp,
    transaction_from_date_time       timestamp,
    transaction_to_date_time         timestamp,
    creation_date_time               timestamp,
    status_update_date_time          timestamp          NOT NULL,
    status                           varchar            NOT NULL,
    client_id                        varchar,
    business_document_identification varchar,
    business_document_rel            varchar,
    rejected_by                      varchar,
    rejection_code                   varchar,
    rejection_additional_information varchar,
    FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE
);

CREATE TABLE consents_aud
(
    reference_id                     integer NOT NULL,
    consent_id                       text,
    account_holder_id                uuid,
    expiration_date_time             timestamp,
    transaction_from_date_time       timestamp,
    transaction_to_date_time         timestamp,
    creation_date_time               timestamp,
    status_update_date_time          timestamp,
    status                           varchar,
    risk                             varchar,
    client_id                        varchar,
    business_document_identification varchar,
    business_document_rel            varchar,
    rejected_by                      varchar,
    rejection_code                   varchar,
    rejection_additional_information varchar,
    rev                              integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE consent_accounts
(
    reference_id SERIAL PRIMARY KEY NOT NULL,
    consent_id   varchar,
    account_id   uuid,
    FOREIGN KEY (consent_id) REFERENCES consents (consent_id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts (account_id) ON DELETE CASCADE
);

CREATE TABLE consent_accounts_aud
(
    reference_id integer NOT NULL,
    consent_id   varchar,
    account_id   uuid,
    rev          integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE consent_contracts
(
    reference_id SERIAL PRIMARY KEY NOT NULL,
    consent_id   varchar,
    contract_id  uuid,
    FOREIGN KEY (consent_id) REFERENCES consents (consent_id) ON DELETE CASCADE,
    FOREIGN KEY (contract_id) REFERENCES contracts (contract_id) ON DELETE CASCADE
);

CREATE TABLE consent_contracts_aud
(
    reference_id integer NOT NULL,
    consent_id   varchar,
    contract_id  uuid,
    rev          integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE consent_credit_card_accounts
(
    reference_id           SERIAL PRIMARY KEY NOT NULL,
    consent_id             varchar,
    credit_card_account_id uuid,
    FOREIGN KEY (consent_id) REFERENCES consents (consent_id) ON DELETE CASCADE,
    FOREIGN KEY (credit_card_account_id) REFERENCES credit_card_accounts (credit_card_account_id) ON DELETE CASCADE
);

CREATE TABLE consent_credit_card_accounts_aud
(
    reference_id           integer NOT NULL,
    consent_id             varchar,
    credit_card_account_id uuid,
    rev                    integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE consent_permissions
(
    reference_id SERIAL PRIMARY KEY NOT NULL,
    consent_id   text,
    permission   varchar            NOT NULL,
    FOREIGN KEY (consent_id) REFERENCES consents (consent_id) ON DELETE CASCADE
);

CREATE TABLE consent_permissions_aud
(
    reference_id integer NOT NULL,
    consent_id   text,
    permission   varchar,
    rev          integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE creditor_accounts
(
    creditor_account_id SERIAL PRIMARY KEY NOT NULL,
    ispb                varchar,
    issuer              varchar,
    number              varchar,
    account_type        varchar
);

CREATE TABLE creditor_accounts_aud
(
    creditor_account_id integer NOT NULL,
    ispb                varchar,
    issuer              varchar,
    number              varchar,
    account_type        varchar,
    rev                 integer NOT NULL,
    PRIMARY KEY (creditor_account_id, rev)
);

CREATE TABLE creditors
(
    creditor_id SERIAL PRIMARY KEY NOT NULL,
    person_type varchar,
    cpf_cnpj    varchar,
    name        varchar
);

CREATE TABLE creditors_aud
(
    creditor_id integer NOT NULL,
    person_type varchar,
    cpf_cnpj    varchar,
    name        varchar,
    rev         integer NOT NULL,
    PRIMARY KEY (creditor_id, rev)
);

CREATE TABLE interest_rates
(
    interest_rates_id                        uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    contract_id                              uuid                                        NOT NULL,
    tax_type                                 varchar,
    interest_rate_type                       varchar,
    tax_periodicity                          varchar,
    calculation                              varchar,
    referential_rate_indexer_type            varchar,
    referential_rate_indexer_sub_type        varchar,
    referential_rate_indexer_additional_info varchar,
    pre_fixed_rate                           double precision,
    post_fixed_rate                          double precision,
    additional_info                          varchar,
    FOREIGN KEY (contract_id) REFERENCES contracts (contract_id) ON DELETE CASCADE
);

CREATE TABLE interest_rates_aud
(
    interest_rates_id                        uuid    NOT NULL,
    contract_id                              uuid,
    tax_type                                 varchar,
    interest_rate_type                       varchar,
    tax_periodicity                          varchar,
    calculation                              varchar,
    referential_rate_indexer_type            varchar,
    referential_rate_indexer_sub_type        varchar,
    referential_rate_indexer_additional_info varchar,
    pre_fixed_rate                           double precision,
    post_fixed_rate                          double precision,
    additional_info                          varchar,
    rev                                      integer NOT NULL,
    PRIMARY KEY (interest_rates_id, rev)
);

CREATE TABLE jti
(
    id           SERIAL PRIMARY KEY NOT NULL,
    jti          varchar            NOT NULL,
    created_date timestamp
);

CREATE TABLE logged_in_user_entity_documents
(
    logged_in_user_entity_document_id SERIAL PRIMARY KEY NOT NULL,
    identification                    varchar,
    rel                               varchar
);

CREATE TABLE logged_in_user_entity_documents_aud
(
    logged_in_user_entity_document_id integer NOT NULL,
    identification                    varchar,
    rel                               varchar,
    rev                               integer NOT NULL,
    PRIMARY KEY (logged_in_user_entity_document_id, rev)
);

CREATE TABLE releases
(
    releases_id            uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    payments_id            uuid             DEFAULT uuid_generate_v4(),
    contract_id            uuid                                        NOT NULL,
    is_over_parcel_payment boolean                                     NOT NULL,
    instalment_id          varchar                                     NOT NULL,
    paid_date              varchar                                     NOT NULL,
    currency               varchar                                     NOT NULL,
    paid_amount            double precision                            NOT NULL,
    FOREIGN KEY (contract_id) REFERENCES contracts (contract_id) ON DELETE CASCADE
);

CREATE TABLE releases_aud
(
    releases_id            uuid    NOT NULL,
    payments_id            uuid,
    contract_id            uuid,
    payment_id             varchar,
    is_over_parcel_payment boolean,
    instalment_id          varchar,
    paid_date              varchar,
    currency               varchar,
    paid_amount            double precision,
    rev                    integer NOT NULL,
    PRIMARY KEY (releases_id, rev)
);

CREATE TABLE over_parcel_charges
(
    over_parcel_charges_id uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    releases_id            uuid                                        NOT NULL,
    charge_type            varchar                                     NOT NULL,
    charge_additional_info varchar                                     NOT NULL,
    charge_amount          double precision                            NOT NULL,
    FOREIGN KEY (releases_id) REFERENCES releases (releases_id) ON DELETE CASCADE
);

CREATE TABLE over_parcel_charges_aud
(
    over_parcel_charges_id uuid    NOT NULL,
    releases_id            uuid,
    charge_type            varchar,
    charge_additional_info varchar,
    charge_amount          double precision,
    rev                    integer NOT NULL,
    PRIMARY KEY (over_parcel_charges_id, rev)
);

CREATE TABLE over_parcel_fees
(
    over_parcel_fees_id uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    releases_id         uuid                                        NOT NULL,
    fee_name            varchar                                     NOT NULL,
    fee_code            varchar                                     NOT NULL,
    fee_amount          double precision                            NOT NULL,
    FOREIGN KEY (releases_id) REFERENCES releases (releases_id) ON DELETE CASCADE
);

CREATE TABLE over_parcel_fees_aud
(
    over_parcel_fees_id uuid    NOT NULL,
    releases_id         uuid,
    fee_name            varchar,
    fee_code            varchar,
    fee_amount          double precision,
    rev                 integer NOT NULL,
    PRIMARY KEY (over_parcel_fees_id, rev)
);

CREATE TABLE payment_consent_details
(
    payment_consent_details_id SERIAL PRIMARY KEY NOT NULL,
    local_instrument           varchar,
    qr_code                    varchar,
    proxy                      varchar,
    creditor_ispb              varchar,
    creditor_issuer            varchar,
    creditor_account_number    varchar,
    creditor_account_type      varchar
);

CREATE TABLE payment_consent_details_aud
(
    payment_consent_details_id integer NOT NULL,
    local_instrument           varchar,
    qr_code                    varchar,
    proxy                      varchar,
    creditor_ispb              varchar,
    creditor_issuer            varchar,
    creditor_account_number    varchar,
    creditor_account_type      varchar,
    rev                        integer NOT NULL,
    PRIMARY KEY (payment_consent_details_id, rev)
);

CREATE TABLE payment_consent_payments
(
    payment_id                 SERIAL PRIMARY KEY NOT NULL,
    payment_type               varchar,
    payment_date               timestamp,
    currency                   varchar,
    amount                     varchar,
    payment_consent_details_id integer            NOT NULL,
    schedule                   timestamp,
    FOREIGN KEY (payment_consent_details_id) REFERENCES payment_consent_details (payment_consent_details_id) ON DELETE CASCADE
);

CREATE TABLE payment_consent_payments_aud
(
    payment_id                 integer NOT NULL,
    payment_type               varchar,
    payment_date               timestamp,
    currency                   varchar,
    amount                     varchar,
    payment_consent_details_id integer,
    schedule                   timestamp,
    rev                        integer NOT NULL,
    PRIMARY KEY (payment_id, rev)
);

CREATE TABLE payment_consents
(
    reference_id                     SERIAL PRIMARY KEY NOT NULL,
    payment_consent_id               varchar UNIQUE,
    account_holder_id                uuid,
    account_id                       uuid,
    client_id                        varchar,
    status                           varchar,
    creditor_id                      integer,
    payment_id                       integer,
    creation_date_time               timestamp,
    expiration_date_time             timestamp,
    status_update_date_time          timestamp          NOT NULL,
    idempotency_key                  varchar,
    business_document_identification varchar,
    business_document_rel            varchar,
    FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE,
    FOREIGN KEY (creditor_id) REFERENCES creditors (creditor_id) ON DELETE CASCADE,
    FOREIGN KEY (payment_id) REFERENCES payment_consent_payments (payment_id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts (account_id) ON DELETE CASCADE
);

CREATE TABLE payment_consents_aud
(
    reference_id                     integer NOT NULL,
    payment_consent_id               varchar,
    account_holder_id                uuid,
    account_id                       uuid,
    client_id                        varchar,
    status                           varchar,
    creditor_id                      integer,
    payment_id                       integer,
    creation_date_time               timestamp,
    expiration_date_time             timestamp,
    status_update_date_time          timestamp,
    idempotency_key                  varchar,
    business_document_identification varchar,
    business_document_rel            varchar,
    rev                              integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE personal_identifications
(
    personal_identifications_id    uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    account_holder_id              uuid                                        NOT NULL,
    brand_name                     varchar(80),
    civil_name                     varchar(70),
    social_name                    varchar(70),
    birth_date                     date,
    marital_status_code            varchar,
    marital_status_additional_info varchar,
    sex                            varchar,
    has_brazilian_nationality      boolean,
    cpf_number                     varchar(11),
    passport_number                varchar(20),
    passport_country               varchar(3),
    passport_expiration_date       date,
    passport_issue_date            date,
    FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE
);

CREATE TABLE personal_identifications_aud
(
    personal_identifications_id    uuid,
    account_holder_id              uuid,
    brand_name                     varchar(80),
    civil_name                     varchar(70),
    social_name                    varchar(70),
    birth_date                     date,
    marital_status_code            varchar,
    marital_status_additional_info varchar,
    sex                            varchar,
    has_brazilian_nationality      boolean,
    cpf_number                     varchar(11),
    passport_number                varchar(20),
    passport_country               varchar(3),
    passport_expiration_date       date,
    passport_issue_date            date,
    rev                            integer NOT NULL,
    PRIMARY KEY (personal_identifications_id, rev)
);

CREATE TABLE personal_emails
(
    reference_id                SERIAL PRIMARY KEY NOT NULL,
    personal_identifications_id uuid               NOT NULL,
    is_main                     boolean,
    email                       varchar(320),
    FOREIGN KEY (personal_identifications_id) REFERENCES personal_identifications (personal_identifications_id) ON DELETE CASCADE
);

CREATE TABLE personal_emails_aud
(
    reference_id                integer NOT NULL,
    personal_identifications_id uuid,
    is_main                     boolean,
    email                       varchar(320),
    rev                         integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE personal_filiation
(
    reference_id                SERIAL PRIMARY KEY NOT NULL,
    personal_identifications_id uuid               NOT NULL,
    type                        varchar,
    civil_name                  varchar(70),
    social_name                 varchar(70),
    FOREIGN KEY (personal_identifications_id) REFERENCES personal_identifications (personal_identifications_id) ON DELETE CASCADE
);

CREATE TABLE personal_filiation_aud
(
    reference_id                integer NOT NULL,
    personal_identifications_id uuid,
    type                        varchar,
    civil_name                  varchar(70),
    social_name                 varchar(70),
    rev                         integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE personal_financial_relations
(
    personal_financial_relations_id        uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    account_holder_id                      uuid                                        NOT NULL,
    start_date                             date,
    products_services_type_additional_info varchar(100),
    FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE
);

CREATE TABLE personal_financial_relations_aud
(
    personal_financial_relations_id        uuid,
    account_holder_id                      uuid,
    start_date                             date,
    products_services_type_additional_info varchar(100),
    rev                                    integer NOT NULL,
    PRIMARY KEY (personal_financial_relations_id, rev)
);

CREATE TABLE personal_financial_relations_procurators
(
    reference_id                    SERIAL PRIMARY KEY NOT NULL,
    personal_financial_relations_id uuid               NOT NULL,
    type                            varchar(19),
    cpf_number                      varchar(11),
    civil_name                      varchar(70),
    social_name                     varchar(70),
    FOREIGN KEY (personal_financial_relations_id) REFERENCES personal_financial_relations (personal_financial_relations_id) ON DELETE CASCADE
);

CREATE TABLE personal_financial_relations_procurators_aud
(
    reference_id                    integer NOT NULL,
    personal_financial_relations_id uuid,
    type                            varchar(19),
    cpf_number                      varchar(11),
    civil_name                      varchar(70),
    social_name                     varchar(70),
    rev                             integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE personal_financial_relations_products_services_type
(
    reference_id                    SERIAL PRIMARY KEY NOT NULL,
    personal_financial_relations_id uuid               NOT NULL,
    type                            varchar,
    FOREIGN KEY (personal_financial_relations_id) REFERENCES personal_financial_relations (personal_financial_relations_id) ON DELETE CASCADE
);

CREATE TABLE personal_financial_relations_products_services_type_aud
(
    reference_id                    integer NOT NULL,
    personal_financial_relations_id uuid,
    type                            varchar,
    rev                             integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE personal_identifications_company_cnpj
(
    reference_id                SERIAL PRIMARY KEY NOT NULL,
    personal_identifications_id uuid               NOT NULL,
    company_cnpj                varchar,
    FOREIGN KEY (personal_identifications_id) REFERENCES personal_identifications (personal_identifications_id) ON DELETE CASCADE
);

CREATE TABLE personal_identifications_company_cnpj_aud
(
    reference_id                integer NOT NULL,
    personal_identifications_id uuid,
    company_cnpj                varchar,
    rev                         integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE personal_nationality
(
    personal_nationality_id     uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    personal_identifications_id uuid                                        NOT NULL,
    other_nationalities_info    varchar(40),
    FOREIGN KEY (personal_identifications_id) REFERENCES personal_identifications (personal_identifications_id) ON DELETE CASCADE
);

CREATE TABLE personal_nationality_aud
(
    personal_nationality_id     uuid,
    personal_identifications_id uuid,
    other_nationalities_info    varchar(40),
    rev                         integer NOT NULL,
    PRIMARY KEY (personal_nationality_id, rev)
);

CREATE TABLE personal_nationality_documents
(
    reference_id            SERIAL PRIMARY KEY NOT NULL,
    personal_nationality_id uuid               NOT NULL,
    type                    varchar,
    number                  varchar(11),
    expiration_date         date,
    issue_date              date,
    country                 varchar(80),
    type_additional_info    varchar(70),
    FOREIGN KEY (personal_nationality_id) REFERENCES personal_nationality (personal_nationality_id) ON DELETE CASCADE
);

CREATE TABLE personal_nationality_documents_aud
(
    reference_id            integer NOT NULL,
    personal_nationality_id uuid,
    type                    varchar,
    number                  varchar(11),
    expiration_date         date,
    issue_date              date,
    country                 varchar(80),
    type_additional_info    varchar(70),
    rev                     integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE personal_other_documents
(
    reference_id                SERIAL PRIMARY KEY NOT NULL,
    personal_identifications_id uuid               NOT NULL,
    type                        varchar,
    type_additional_info        varchar(70),
    number                      varchar(11),
    check_digit                 varchar(2),
    additional_info             varchar(50),
    expiration_date             date,
    FOREIGN KEY (personal_identifications_id) REFERENCES personal_identifications (personal_identifications_id) ON DELETE CASCADE
);

CREATE TABLE personal_other_documents_aud
(
    reference_id                integer NOT NULL,
    personal_identifications_id uuid,
    type                        varchar,
    type_additional_info        varchar(70),
    number                      varchar(11),
    check_digit                 varchar(2),
    additional_info             varchar(50),
    expiration_date             date,
    rev                         integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);


CREATE TABLE personal_phones
(
    reference_id                SERIAL PRIMARY KEY NOT NULL,
    personal_identifications_id uuid               NOT NULL,
    is_main                     boolean,
    type                        varchar(5),
    additional_info             varchar(70),
    country_calling_code        varchar(4),
    area_code                   varchar(2),
    number                      varchar(11),
    phone_extension             varchar(5),
    FOREIGN KEY (personal_identifications_id) REFERENCES personal_identifications (personal_identifications_id) ON DELETE CASCADE
);

CREATE TABLE personal_phones_aud
(
    reference_id                integer NOT NULL,
    personal_identifications_id uuid,
    is_main                     boolean,
    type                        varchar(5),
    additional_info             varchar(70),
    country_calling_code        varchar(4),
    area_code                   varchar(2),
    number                      varchar(11),
    phone_extension             varchar(5),
    rev                         integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE personal_postal_addresses
(
    reference_id                SERIAL PRIMARY KEY NOT NULL,
    personal_identifications_id uuid               NOT NULL,
    is_main                     boolean,
    address                     varchar(150),
    additional_info             varchar(30),
    district_name               varchar(50),
    town_name                   varchar(50),
    ibge_town_code              varchar(7),
    country_subdivision         varchar,
    post_code                   varchar(8),
    country                     varchar(80),
    country_code                varchar(3),
    latitude                    varchar(13),
    longitude                   varchar(13),
    FOREIGN KEY (personal_identifications_id) REFERENCES personal_identifications (personal_identifications_id) ON DELETE CASCADE
);

CREATE TABLE personal_postal_addresses_aud
(
    reference_id                integer NOT NULL,
    personal_identifications_id uuid,
    is_main                     boolean,
    address                     varchar(150),
    additional_info             varchar(30),
    district_name               varchar(50),
    town_name                   varchar(50),
    ibge_town_code              varchar(7),
    country_subdivision         varchar,
    post_code                   varchar(8),
    country                     varchar(80),
    country_code                varchar(3),
    latitude                    varchar(13),
    longitude                   varchar(13),
    rev                         integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE personal_qualifications
(
    reference_id                SERIAL PRIMARY KEY                     NOT NULL,
    personal_qualifications_id  uuid UNIQUE DEFAULT uuid_generate_v4() NOT NULL,
    account_holder_id           uuid                                   NOT NULL,
    company_cnpj                varchar(14),
    occupation_code             varchar,
    occupation_description      varchar(100),
    informed_income_frequency   varchar,
    informed_income_amount      numeric,
    informed_income_currency    varchar(3),
    informed_income_date        date,
    informed_patrimony_amount   numeric,
    informed_patrimony_currency varchar(3),
    informed_patrimony_year     integer,
    FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE
);

CREATE TABLE personal_qualifications_aud
(
    reference_id                integer NOT NULL,
    personal_qualifications_id  uuid,
    account_holder_id           uuid,
    company_cnpj                varchar(14),
    occupation_code             varchar,
    occupation_description      varchar(100),
    informed_income_frequency   varchar,
    informed_income_amount      numeric,
    informed_income_currency    varchar(3),
    informed_income_date        date,
    informed_patrimony_amount   numeric,
    informed_patrimony_currency varchar(3),
    informed_patrimony_year     integer,
    rev                         integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE pix_payments_payments
(
    pix_payment_id SERIAL PRIMARY KEY NOT NULL,
    currency       varchar,
    amount         varchar
);

CREATE TABLE pix_payments_payments_aud
(
    pix_payment_id integer NOT NULL,
    currency       varchar,
    amount         varchar,
    rev            integer NOT NULL,
    PRIMARY KEY (pix_payment_id, rev)
);

CREATE TABLE pix_payments
(
    reference_id               SERIAL PRIMARY KEY NOT NULL,
    payment_id                 varchar,
    local_instrument           varchar,
    pix_payment_id             integer,
    creditor_account_id        integer,
    remittance_information     varchar,
    qr_code                    varchar,
    proxy                      varchar,
    status                     varchar,
    creation_date_time         timestamp,
    status_update_date_time    timestamp          NOT NULL,
    rejection_reason           varchar,
    idempotency_key            varchar,
    payment_consent_id         varchar,
    transaction_identification varchar,
    end_to_end_id              varchar,
    FOREIGN KEY (pix_payment_id) REFERENCES pix_payments_payments (pix_payment_id) ON DELETE CASCADE,
    FOREIGN KEY (creditor_account_id) REFERENCES creditor_accounts (creditor_account_id) ON DELETE CASCADE,
    FOREIGN KEY (payment_consent_id) REFERENCES payment_consents (payment_consent_id) ON DELETE CASCADE
);

CREATE TABLE pix_payments_aud
(
    reference_id               integer NOT NULL,
    payment_id                 varchar,
    local_instrument           varchar,
    pix_payment_id             integer,
    creditor_account_id        integer,
    remittance_information     varchar,
    qr_code                    varchar,
    proxy                      varchar,
    status                     varchar,
    creation_date_time         timestamp,
    status_update_date_time    timestamp,
    rejection_reason           varchar,
    idempotency_key            varchar,
    payment_consent_id         varchar,
    transaction_identification varchar,
    end_to_end_id              varchar,
    rev                        integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE warranties
(
    warranty_id      uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    contract_id      uuid                                        NOT NULL,
    currency         varchar                                     NOT NULL,
    warranty_type    varchar                                     NOT NULL,
    warranty_subtype varchar                                     NOT NULL,
    warranty_amount  double precision,
    FOREIGN KEY (contract_id) REFERENCES contracts (contract_id) ON DELETE CASCADE
);

CREATE TABLE warranties_aud
(
    warranty_id      uuid    NOT NULL,
    contract_id      uuid,
    currency         varchar,
    warranty_type    varchar,
    warranty_subtype varchar,
    warranty_amount  double precision,
    rev              integer NOT NULL,
    PRIMARY KEY (warranty_id, rev)
);

CREATE TABLE payments_simulate_response
(
    id                 uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    client_id          varchar                                     NOT NULL,
    payment_consent_id varchar UNIQUE,
    request_time       timestamp                                   NOT NULL,
    request_end_time   timestamp                                   NOT NULL,
    http_status        varchar,
    http_error_message varchar,
    duration           int
);

CREATE TABLE payments_simulate_response_aud
(
    id                 uuid,
    client_id          varchar   NOT NULL,
    payment_consent_id varchar UNIQUE,
    request_time       timestamp NOT NULL,
    request_end_time   timestamp NOT NULL,
    http_status        varchar,
    http_error_message varchar,
    duration           int,
    rev                int4      NOT NULL,
    PRIMARY KEY (id, rev)
);

DO
$$
    DECLARE
        r RECORD;
    BEGIN
        FOR r IN (SELECT tablename
                  FROM pg_tables
                  WHERE schemaname = current_schema()
                    AND tablename NOT LIKE '%flyway%'
                    AND tablename NOT LIKE 'jti'
                    AND tablename NOT LIKE 'revinfo')
            LOOP
                IF r.tablename LIKE '%_aud' THEN
                    EXECUTE 'ALTER TABLE ' || quote_ident(r.tablename) || ' ADD COLUMN revtype smallint;';
                    EXECUTE 'ALTER TABLE ' || quote_ident(r.tablename) ||
                            ' ADD FOREIGN KEY (rev) REFERENCES revinfo (rev);';
                END IF;
                EXECUTE 'ALTER TABLE ' || quote_ident(r.tablename) || ' ADD COLUMN created_at timestamp;';
                EXECUTE 'ALTER TABLE ' || quote_ident(r.tablename) || ' ADD COLUMN updated_at timestamp;';
                EXECUTE 'ALTER TABLE ' || quote_ident(r.tablename) || ' ADD COLUMN hibernate_status varchar;';
                EXECUTE 'ALTER TABLE ' || quote_ident(r.tablename) || ' ADD COLUMN created_by varchar;';
                EXECUTE 'ALTER TABLE ' || quote_ident(r.tablename) || ' ADD COLUMN updated_by varchar;';
            END LOOP;
    END
$$;