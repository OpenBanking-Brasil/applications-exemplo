CREATE TABLE bank_fixed_incomes
(
    investment_id                   uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    company_cnpj                    varchar(14),
    brand_name                      varchar(80),
    investment_type                 varchar(15),
    isin_code                       varchar(12),
    pre_fixed_rate                  double precision,
    post_fixed_indexer_percentage   double precision,
    rate_type                       varchar(20),
    rate_periodicity                varchar(20),
    calculation                     varchar(20),
    indexer                         varchar(20),
    indexer_additional_info         varchar(40),
    issue_unit_price_amount         double precision,
    issue_unit_price_currency       varchar(3),
    due_date                        date,
    issue_date                      date,
    clearing_code                   varchar(30),
    purchase_date                   date,
    grace_period_date               date,
    created_at                      date,
    created_by                      varchar(20),
    updated_at                      date,
    updated_by                      varchar(20),
    hibernate_status                varchar
);

CREATE TABLE bank_fixed_incomes_balance
(
    balance_id                          uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    investment_id                       uuid NOT NULL,
    reference_date_time                 timestamp,
    updated_unit_price                  double precision,
    updated_unit_price_currency         varchar(3),
    gross_amount                        double precision,
    gross_amount_currency               varchar(3),
    net_amount                          double precision,
    net_amount_currency                 varchar(3),
    income_tax_amount                   double precision,
    income_tax_currency                 varchar(3),
    financial_transaction_tax_amount    double precision,
    financial_transaction_tax_currency  varchar(3),
    blocked_balance                     double precision,
    blocked_balance_currency            varchar(3),
    purchase_unit_price                 double precision,
    purchase_unit_price_currency        varchar(3),
    quantity                            double precision,
    pre_fixed_rate                      double precision,
    post_fixed_indexer_percentage       double precision,
    created_at                          date,
    created_by                          varchar(20),
    updated_at                          date,
    updated_by                          varchar(20),
    hibernate_status                    varchar,
    FOREIGN KEY (investment_id) REFERENCES bank_fixed_incomes (investment_id) ON DELETE CASCADE
);

CREATE TABLE bank_fixed_incomes_transactions
(
    transaction_id                      uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    investment_id                       uuid NOT NULL,
    type                                varchar(11),
    transaction_type                    varchar(20),
    transaction_type_additional_info    varchar(80),
    transaction_date                    date,
    transaction_quantity                double precision,
    transaction_unit_price              double precision,
    transaction_unit_price_currency     varchar(3),
    transaction_gross_value             double precision,
    transaction_gross_value_currency    varchar(3),
    income_tax_value                    double precision,
    income_tax_currency                 varchar(3),
    financial_transaction_tax_value     double precision,
    financial_transaction_tax_currency  varchar(3),
    transaction_net_value               double precision,
    transaction_net_currency            varchar(3),
    remuneration_transaction_rate       double precision,
    indexer_percentage                  double precision,
    created_at                          date,
    created_by                          varchar(20),
    updated_at                          date,
    updated_by                          varchar(20),
    hibernate_status                    varchar,
    FOREIGN KEY (investment_id) REFERENCES bank_fixed_incomes (investment_id) ON DELETE CASCADE
);

CREATE TABLE bank_fixed_incomes_aud
(
    investment_id            uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    company_cnpj                    varchar(14),
    brand_name                      varchar(80),
    investment_type                 varchar(15),
    isin_code                       varchar(12),
    pre_fixed_rate                  double precision,
    post_fixed_indexer_percentage   double precision,
    rate_type                       varchar(20),
    rate_periodicity                varchar(20),
    calculation                     varchar(20),
    indexer                         varchar(20),
    indexer_additional_info         varchar(40),
    issue_unit_price_amount         double precision,
    issue_unit_price_currency       varchar(3),
    due_date                        date,
    issue_date                      date,
    clearing_code                   varchar(30),
    purchase_date                   date,
    grace_period_date               date,
    created_at                      date,
    created_by                      varchar(20),
    updated_at                      date,
    updated_by                      varchar(20),
    hibernate_status                varchar,
    rev                             integer NOT NULL,
    revtype                         smallint,
    FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE bank_fixed_incomes_balance_aud
(
    balance_id                          uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    investment_id                       uuid NOT NULL,
    reference_date_time                 timestamp,
    updated_unit_price                  double precision,
    updated_unit_price_currency         varchar(3),
    gross_amount                        double precision,
    gross_amount_currency               varchar(3),
    net_amount                          double precision,
    net_amount_currency                 varchar(3),
    income_tax_amount                   double precision,
    income_tax_currency                 varchar(3),
    financial_transaction_tax_amount    double precision,
    financial_transaction_tax_currency  varchar(3),
    blocked_balance                     double precision,
    blocked_balance_currency            varchar(3),
    purchase_unit_price                 double precision,
    purchase_unit_price_currency        varchar(3),
    quantity                            double precision,
    pre_fixed_rate                      double precision,
    post_fixed_indexer_percentage       double precision,
    created_at                          date,
    created_by                          varchar(20),
    updated_at                          date,
    updated_by                          varchar(20),
    hibernate_status                    varchar,
    rev                                 integer NOT NULL,
    revtype                             smallint,
    FOREIGN KEY (investment_id) REFERENCES bank_fixed_incomes_aud (investment_id) ON DELETE CASCADE,
    FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE bank_fixed_incomes_transactions_aud
(
    transaction_id                      uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    investment_id                       uuid NOT NULL,
    type                                varchar(11),
    transaction_type                    varchar(20),
    transaction_type_additional_info    varchar(80),
    transaction_date                    date,
    transaction_quantity                double precision,
    transaction_unit_price              double precision,
    transaction_unit_price_currency     varchar(3),
    transaction_gross_value             double precision,
    transaction_gross_value_currency    varchar(3),
    income_tax_value                    double precision,
    income_tax_currency                 varchar(3),
    financial_transaction_tax_value     double precision,
    financial_transaction_tax_currency  varchar(3),
    transaction_net_value               double precision,
    transaction_net_currency            varchar(3),
    remuneration_transaction_rate       double precision,
    indexer_percentage                  double precision,
    created_at                          date,
    created_by                          varchar(20),
    updated_at                          date,
    updated_by                          varchar(20),
    hibernate_status                    varchar,
    rev                                 integer NOT NULL,
    revtype                             smallint,
    FOREIGN KEY (investment_id) REFERENCES bank_fixed_incomes_aud (investment_id) ON DELETE CASCADE,
    FOREIGN KEY (rev) REFERENCES revinfo (rev)
);