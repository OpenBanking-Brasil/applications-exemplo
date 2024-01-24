CREATE TABLE exchanges_operation_event
(
    event_id                                    uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    operation_id                                uuid,
    event_sequence_number                       varchar(20),
    event_type                                  varchar(10),
    event_date                                  timestamp,
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
    created_at                                  date,
    created_by                                  varchar(20),
    updated_at                                  date,
    updated_by                                  varchar(20),
    hibernate_status                            varchar,
    FOREIGN KEY (operation_id) REFERENCES exchanges_operation (operation_id)
);

CREATE TABLE exchanges_operation_event_aud
(
    event_id                                    uuid,
    operation_id                                uuid,
    event_sequence_number                       varchar(20),
    event_type                                  varchar(10),
    event_date                                  timestamp,
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
    created_at                                  date,
    created_by                                  varchar(20),
    updated_at                                  date,
    updated_by                                  varchar(20),
    hibernate_status                            varchar,
    rev                                         integer NOT NULL,
    revtype                                     smallint,
    PRIMARY KEY (event_id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo (rev)
);
