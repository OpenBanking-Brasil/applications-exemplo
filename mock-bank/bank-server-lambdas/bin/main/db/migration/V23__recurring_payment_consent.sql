CREATE TABLE periodic_limits
(
    reference_id                            SERIAL PRIMARY KEY UNIQUE NOT NULL,
    periodic_limits_day_quantity_limit      integer,
    periodic_limits_day_transaction_limit   varchar,
    periodic_limits_week_quantity_limit     integer,
    periodic_limits_week_transaction_limit  varchar,
    periodic_limits_month_quantity_limit    integer,
    periodic_limits_month_transaction_limit varchar,
    periodic_limits_year_quantity_limit     integer,
    periodic_limits_year_transaction_limit  varchar,
    created_at                              date,
    created_by                              varchar(20),
    updated_at                              date,
    updated_by                              varchar(20),
    hibernate_status                        varchar
);

CREATE TABLE periodic_limits_aud
(
    reference_id                            integer,
    periodic_limits_day_quantity_limit      integer,
    periodic_limits_day_transaction_limit   varchar,
    periodic_limits_week_quantity_limit     integer,
    periodic_limits_week_transaction_limit  varchar,
    periodic_limits_month_quantity_limit    integer,
    periodic_limits_month_transaction_limit varchar,
    periodic_limits_year_quantity_limit     integer,
    periodic_limits_year_transaction_limit  varchar,
    created_at                              date,
    created_by                              varchar(20),
    updated_at                              date,
    updated_by                              varchar(20),
    hibernate_status                        varchar,
    rev                                     integer NOT NULL,
    revtype                                 smallint,
    PRIMARY KEY (reference_id, rev)
);


CREATE TABLE automatic_recurring_configuration
(
    reference_id                            SERIAL PRIMARY KEY UNIQUE NOT NULL,
    contract_id                             varchar,
    amount                                  varchar,
    transaction_limit                       varchar,
    period                                  varchar,
    day_of_month                            integer,
    day_of_week                             varchar,
    month                                   varchar,
    contract_debtor_name                    varchar,
    contract_debtor_identification          varchar,
    contract_debtor_rel                     varchar,
    immediate_payment_type                  varchar,
    immediate_payment_date                  date,
    immediate_payment_currency              varchar,
    immediate_payment_amount                varchar,
    immediate_payment_creditor_ispb         varchar,
    immediate_payment_creditor_issuer       varchar,
    immediate_payment_creditor_number       varchar,
    immediate_payment_creditor_account_type varchar,
    created_at                              date,
    created_by                              varchar(20),
    updated_at                              date,
    updated_by                              varchar(20),
    hibernate_status                        varchar
);

CREATE TABLE automatic_recurring_configuration_aud
(
    reference_id                            integer,
    contract_id                             varchar,
    amount                                  varchar,
    transaction_limit                       varchar,
    period                                  varchar,
    day_of_month                            integer,
    day_of_week                             varchar,
    month                                   varchar,
    contract_debtor_name                    varchar,
    contract_debtor_identification          varchar,
    contract_debtor_rel                     varchar,
    immediate_payment_type                  varchar,
    immediate_payment_date                  date,
    immediate_payment_currency              varchar,
    immediate_payment_amount                varchar,
    immediate_payment_creditor_ispb         varchar,
    immediate_payment_creditor_issuer       varchar,
    immediate_payment_creditor_number       varchar,
    immediate_payment_creditor_account_type varchar,
    created_at                              date,
    created_by                              varchar(20),
    updated_at                              date,
    updated_by                              varchar(20),
    hibernate_status                        varchar,
    rev                                     integer NOT NULL,
    revtype                                 smallint,
    PRIMARY KEY (reference_id, rev)
);


CREATE TABLE post_sweeping_recurring_configuration
(
    reference_id                 SERIAL PRIMARY KEY UNIQUE NOT NULL,
    amount                       varchar,
    transaction_limit            varchar,
    periodic_limits_reference_id integer,
    created_at                   date,
    created_by                   varchar(20),
    updated_at                   date,
    updated_by                   varchar(20),
    hibernate_status             varchar,
    FOREIGN KEY (periodic_limits_reference_id) REFERENCES periodic_limits (reference_id) ON DELETE CASCADE
);

CREATE TABLE post_sweeping_recurring_configuration_aud
(
    reference_id                 integer,
    amount                       varchar,
    transaction_limit            varchar,
    periodic_limits_reference_id integer,
    created_at                   date,
    created_by                   varchar(20),
    updated_at                   date,
    updated_by                   varchar(20),
    hibernate_status             varchar,
    rev                          integer NOT NULL,
    revtype                      smallint,
    PRIMARY KEY (reference_id, rev)
);

CREATE TABLE vrp_recurring_configuration
(
    reference_id                 SERIAL PRIMARY KEY UNIQUE NOT NULL,
    transaction_limit            varchar,
    global_quantity_limit        integer,
    global_transaction_limit     varchar,
    periodic_limits_reference_id integer,
    created_at                   date,
    created_by                   varchar(20),
    updated_at                   date,
    updated_by                   varchar(20),
    hibernate_status             varchar,
    FOREIGN KEY (periodic_limits_reference_id) REFERENCES periodic_limits (reference_id) ON DELETE CASCADE
);

CREATE TABLE vrp_recurring_configuration_aud
(
    reference_id                 integer,
    transaction_limit            varchar,
    global_quantity_limit        integer,
    global_transaction_limit     varchar,
    periodic_limits_reference_id integer,
    created_at                   date,
    created_by                   varchar(20),
    updated_at                   date,
    updated_by                   varchar(20),
    hibernate_status             varchar,
    rev                          integer NOT NULL,
    revtype                      smallint,
    PRIMARY KEY (reference_id, rev)
);


ALTER TABLE creditors
    ADD COLUMN payment_consent_reference_id integer;

ALTER TABLE creditors_aud
    ADD COLUMN payment_consent_reference_id integer;

WITH payment_consent AS (SELECT reference_id r_id, creditor_id c_id FROM payment_consents)
UPDATE creditors SET payment_consent_reference_id = payment_consent.r_id FROM payment_consent WHERE creditors.creditor_id = payment_consent.c_id;

ALTER TABLE creditors
    ADD CONSTRAINT creditors_payment_consent_reference_id_fkey FOREIGN KEY (payment_consent_reference_id) REFERENCES payment_consents (reference_id) ON DELETE CASCADE;


ALTER TABLE payment_consents
    DROP COLUMN creditor_id,
    ADD COLUMN start_date_time                                    timestamp,
    ADD COLUMN rejected_by                                        varchar,
    ADD COLUMN rejected_from                                      varchar,
    ADD COLUMN rejected_at                                        timestamp,
    ADD COLUMN revoked_by                                         varchar,
    ADD COLUMN revoked_from                                       varchar,
    ADD COLUMN revoked_at                                         timestamp,
    ADD COLUMN automatic_recurring_configuration_reference_id     integer,
    ADD COLUMN post_sweeping_recurring_configuration_reference_id integer,
    ADD COLUMN vrp_recurring_configuration_reference_id           integer,

    ADD CONSTRAINT payment_consents_automatic_configuration_reference_id_fkey
        FOREIGN KEY (automatic_recurring_configuration_reference_id)
            REFERENCES automatic_recurring_configuration (reference_id) ON DELETE CASCADE,

    ADD CONSTRAINT payment_consents_post_sweeping_configuration_reference_id_fkey
        FOREIGN KEY (post_sweeping_recurring_configuration_reference_id)
            REFERENCES post_sweeping_recurring_configuration (reference_id) ON DELETE CASCADE,

    ADD CONSTRAINT payment_consents_vrp_configuration_reference_id_fkey
        FOREIGN KEY (vrp_recurring_configuration_reference_id)
            REFERENCES vrp_recurring_configuration (reference_id) ON DELETE CASCADE;


ALTER TABLE payment_consents_aud
    DROP COLUMN creditor_id,
    ADD COLUMN start_date_time                                    timestamp,
    ADD COLUMN rejected_by                                        varchar,
    ADD COLUMN rejected_from                                      varchar,
    ADD COLUMN rejected_at                                        timestamp,
    ADD COLUMN revoked_by                                         varchar,
    ADD COLUMN revoked_from                                       varchar,
    ADD COLUMN revoked_at                                         timestamp,
    ADD COLUMN automatic_recurring_configuration_reference_id     integer,
    ADD COLUMN post_sweeping_recurring_configuration_reference_id integer,
    ADD COLUMN vrp_recurring_configuration_reference_id           integer;