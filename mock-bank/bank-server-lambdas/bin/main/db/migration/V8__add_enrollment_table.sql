CREATE TABLE enrollments
(
    reference_id                     SERIAL PRIMARY KEY NOT NULL,
    enrollment_id                    text UNIQUE,
    account_holder_id                uuid,
    account_id                       uuid,
    client_id                        varchar,
    status                           varchar,
    creation_date_time               timestamp,
    expiration_date_time             timestamp,
    status_update_date_time          timestamp          NOT NULL,
    idempotency_key                  varchar,
    business_document_identification varchar,
    business_document_rel            varchar,
    transaction_limit                varchar,
    daily_limit                      varchar,
    cancelled_by_document_identification varchar,
    cancelled_by_document_rel       varchar,
    cancelled_from                  varchar,
    reject_reason                   varchar,
    rejected_at                     date,
    revocation_reason               varchar,
    created_at                      date,
    created_by                      varchar(20),
    updated_at                      date,
    updated_by                      varchar(20),
    hibernate_status                varchar,
    FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE,
    FOREIGN KEY (account_id) REFERENCES accounts (account_id) ON DELETE CASCADE
);

CREATE TABLE enrollments_aud
(
    reference_id                     SERIAL PRIMARY KEY NOT NULL,
    enrollment_id                    text UNIQUE,
    account_holder_id                uuid,
    account_id                       uuid,
    client_id                        varchar,
    status                           varchar,
    creation_date_time               timestamp,
    expiration_date_time             timestamp,
    status_update_date_time          timestamp          NOT NULL,
    idempotency_key                  varchar,
    business_document_identification varchar,
    business_document_rel            varchar,
    transaction_limit                varchar,
    daily_limit                      varchar,
    cancelled_by_document_identification varchar,
    cancelled_by_document_rel       varchar,
    cancelled_from                  varchar,
    reject_reason                   varchar,
    rejected_at                     date,
    revocation_reason               varchar,
    created_at                      date,
    created_by                      varchar(20),
    updated_at                      date,
    updated_by                      varchar(20),
    hibernate_status                varchar,
    rev                             integer NOT NULL,
    revtype                         smallint,
    FOREIGN KEY (rev) REFERENCES revinfo (rev)
);