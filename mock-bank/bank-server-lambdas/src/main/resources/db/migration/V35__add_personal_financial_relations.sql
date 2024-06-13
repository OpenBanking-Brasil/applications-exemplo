CREATE TABLE personal_financial_relations_portabilities_received
(
    reference_id                           SERIAL PRIMARY KEY NOT NULL,
    personal_financial_relations_id        uuid               NOT NULL,
    employer_name                          varchar(80),
    employer_cnpj_cpf                      varchar(14),
    paycheck_bank_detainer_cnpj            varchar(14),
    paycheck_bank_detainer_ispb            varchar(8),
    portability_approval_date              date,
    created_at                             date,
    created_by                             varchar(20),
    updated_at                             date,
    updated_by                             varchar(20),
    hibernate_status                       varchar,
    FOREIGN KEY (personal_financial_relations_id) REFERENCES personal_financial_relations (personal_financial_relations_id) ON DELETE CASCADE
);

CREATE TABLE personal_financial_relations_portabilities_received_aud
(
    reference_id                    integer NOT NULL,
    personal_financial_relations_id uuid,
    employer_name                          varchar(80),
    employer_cnpj_cpf                      varchar(14),
    paycheck_bank_detainer_cnpj            varchar(14),
    paycheck_bank_detainer_ispb            varchar(8),
    portability_approval_date              date,
    created_at                             date,
    created_by                             varchar(20),
    updated_at                             date,
    updated_by                             varchar(20),
    hibernate_status                       varchar,
    rev                             integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE personal_financial_relations_paychecks_bank_link
(
    reference_id                         SERIAL PRIMARY KEY NOT NULL,
    personal_financial_relations_id      uuid               NOT NULL,
    employer_name                        varchar(80),
    employer_cnpj_cpf                    varchar(14),
    paycheck_bank_cnpj                   varchar(14),
    paycheck_bank_ispb                   varchar(8),
    account_opening_date                 date,
    created_at                           date,
    created_by                           varchar(20),
    updated_at                           date,
    updated_by                           varchar(20),
    hibernate_status                     varchar,
    FOREIGN KEY (personal_financial_relations_id) REFERENCES personal_financial_relations (personal_financial_relations_id) ON DELETE CASCADE
);

CREATE TABLE personal_financial_relations_paychecks_bank_link_aud
(
    reference_id                         integer NOT NULL,
    personal_financial_relations_id      uuid,
    employer_name                        varchar(80),
    employer_cnpj_cpf                    varchar(14),
    paycheck_bank_cnpj                   varchar(14),
    paycheck_bank_ispb                   varchar(8),
    account_opening_date                 date,
    created_at                           date,
    created_by                           varchar(20),
    updated_at                           date,
    updated_by                           varchar(20),
    hibernate_status                     varchar,
    rev                             integer NOT NULL,
    PRIMARY KEY (reference_id, rev)
);