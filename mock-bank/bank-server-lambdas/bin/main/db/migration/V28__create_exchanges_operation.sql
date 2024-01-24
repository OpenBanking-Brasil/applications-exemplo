CREATE TABLE exchanges_operation
(
    operation_id                                uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    account_holder_id                           uuid,
    company_cnpj                                varchar(14),
    brand_name                                  varchar(80),
    intermediary_institution_cnpj_number        varchar(14),
    intermediary_institution_name               varchar(80),
    operation_number                            varchar,
    operation_type                              varchar(20),
    operation_date                              date,
    due_date                                    date,
    local_currency_operation_tax_amount         double precision,
    local_currency_operation_tax_currency       varchar(3),
    local_currency_operation_value_amount       double precision,
    local_currency_operation_value_currency     varchar(3),
    foreign_operation_value_amount              double precision,
    foreign_operation_value_currency            varchar(3),
    operation_outstanding_balance_amount        double precision,
    operation_outstanding_balance_currency      varchar(3),
    vet_amount_amount                           double precision,
    vet_amount_currency                         varchar(3),
    local_currency_advance_percentage           double precision,
    delivery_foreign_currency                   varchar(50),
    operation_category_code                     varchar,
    status                                      varchar,
    created_at                                  date,
    created_by                                  varchar(20),
    updated_at                                  date,
    updated_by                                  varchar(20),
    hibernate_status                            varchar
);

CREATE TABLE exchanges_operation_aud
(
    operation_id                                uuid,
    account_holder_id                           uuid,
    company_cnpj                                varchar(14),
    brand_name                                  varchar(80),
    intermediary_institution_cnpj_number        varchar(14),
    intermediary_institution_name               varchar(80),
    operation_number                            varchar,
    operation_type                              varchar(20),
    operation_date                              date,
    due_date                                    date,
    local_currency_operation_tax_amount         double precision,
    local_currency_operation_tax_currency       varchar(3),
    local_currency_operation_value_amount       double precision,
    local_currency_operation_value_currency     varchar(3),
    foreign_operation_value_amount              double precision,
    foreign_operation_value_currency            varchar(3),
    operation_outstanding_balance_amount        double precision,
    operation_outstanding_balance_currency      varchar(3),
    vet_amount_amount                           double precision,
    vet_amount_currency                         varchar(3),
    local_currency_advance_percentage           double precision,
    delivery_foreign_currency                   varchar(50),
    operation_category_code                     varchar,
    status                                      varchar,
    created_at                                  date,
    created_by                                  varchar(20),
    updated_at                                  date,
    updated_by                                  varchar(20),
    hibernate_status                            varchar,
    rev                                         integer NOT NULL,
    revtype                                     smallint,
    PRIMARY KEY (operation_id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE consent_exchanges_operation
(
    reference_id SERIAL PRIMARY KEY NOT NULL,
    consent_id   varchar,
    operation_id  uuid,
    created_at                                  date,
    created_by                                  varchar(20),
    updated_at                                  date,
    updated_by                                  varchar(20),
    hibernate_status                            varchar,
    FOREIGN KEY (consent_id) REFERENCES consents (consent_id) ON DELETE CASCADE,
    FOREIGN KEY (operation_id) REFERENCES exchanges_operation (operation_id) ON DELETE CASCADE
);

CREATE TABLE consent_exchanges_operation_aud
(
    reference_id integer NOT NULL,
    consent_id   varchar,
    operation_id  uuid,
    created_at                                  date,
    created_by                                  varchar(20),
    updated_at                                  date,
    updated_by                                  varchar(20),
    hibernate_status                            varchar,
    rev                                         integer NOT NULL,
    revtype                                     smallint,
    PRIMARY KEY (reference_id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo (rev)
);