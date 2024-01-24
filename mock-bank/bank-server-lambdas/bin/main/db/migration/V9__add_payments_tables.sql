CREATE TABLE "creditors" (
    "creditor_id" SERIAL PRIMARY KEY NOT NULL,
    "person_type" varchar,
    "cpf_cnpj" varchar,
    "name" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar
);

CREATE TABLE "debtor_accounts" (
    "debtor_account_id" SERIAL PRIMARY KEY NOT NULL,
    "ispb" varchar,
    "issuer" varchar,
    "number" varchar,
    "account_type" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar
);

CREATE TABLE "payment_consent_payments" (
    "payment_id" SERIAL PRIMARY KEY NOT NULL,
    "payment_type" varchar,
    "payment_date" timestamp,
    "currency" varchar,
    "amount" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar
);

CREATE TABLE "payment_consents" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "payment_consent_id" varchar UNIQUE,
    "client_id" varchar,
    "status" varchar,
    "business_entity_document_id" integer,
    "logged_in_user_entity_document_id" integer,
    "creditor_id" integer,
    "debtor_account_id" integer,
    "payment_id" integer,
    "creation_date_time" timestamp,
    "expiration_date_time" timestamp,
    "status_update_date_time" timestamp NOT NULL,
    "idempotency_key" varchar UNIQUE,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("business_entity_document_id") REFERENCES "business_entity_documents" ("business_entity_document_id"),
    FOREIGN KEY ("logged_in_user_entity_document_id") REFERENCES "logged_in_user_entity_documents" ("logged_in_user_entity_document_id"),
    FOREIGN KEY ("creditor_id") REFERENCES "creditors" ("creditor_id"),
    FOREIGN KEY ("debtor_account_id") REFERENCES "debtor_accounts" ("debtor_account_id"),
    FOREIGN KEY ("payment_id") REFERENCES "payment_consent_payments" ("payment_id")
);

CREATE TABLE "pix_payments_payments" (
    "pix_payment_id" SERIAL PRIMARY KEY NOT NULL,
    "currency" varchar,
    "amount" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar
);

CREATE TABLE "creditor_accounts" (
    "creditor_account_id" SERIAL PRIMARY KEY NOT NULL,
    "ispb" varchar,
    "issuer" varchar,
    "number" varchar,
    "account_type" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar
);

CREATE TABLE "pix_payments" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "payment_id" varchar,
    "local_instrument" varchar,
    "pix_payment_id" integer,
    "creditor_account_id" integer,
    "remittance_information" varchar,
    "qr_code" varchar,
    "proxy" varchar,
    "status" varchar,
    "creation_date_time" timestamp,
    "status_update_date_time" timestamp NOT NULL,
    "rejection_reason" varchar,
    "idempotency_key" varchar UNIQUE,
    "payment_consent_id" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("pix_payment_id") REFERENCES "pix_payments_payments" ("pix_payment_id"),
    FOREIGN KEY ("creditor_account_id") REFERENCES "creditor_accounts" ("creditor_account_id"),
    FOREIGN KEY ("payment_consent_id") REFERENCES "payment_consents" ("payment_consent_id")
);

-- ## AUDIT TABLES

CREATE TABLE "creditors_aud" (
    "creditor_id" integer NOT NULL,
    "person_type" varchar,
    "cpf_cnpj" varchar,
    "name" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("creditor_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "debtor_accounts_aud" (
    "debtor_account_id" integer NOT NULL,
    "ispb" varchar,
    "issuer" varchar,
    "number" varchar,
    "account_type" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("debtor_account_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "payment_consent_payments_aud" (
    "payment_id" integer NOT NULL,
    "payment_type" varchar,
    "payment_date" timestamp,
    "currency" varchar,
    "amount" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("payment_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "payment_consents_aud" (
    "reference_id" integer NOT NULL,
    "payment_consent_id" varchar,
    "client_id" varchar,
    "status" varchar,
    "business_entity_document_id" integer,
    "logged_in_user_entity_document_id" integer,
    "creditor_id" integer,
    "debtor_account_id" integer,
    "payment_id" integer,
    "creation_date_time" timestamp,
    "expiration_date_time" timestamp,
    "status_update_date_time" timestamp,
    "idempotency_key" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("reference_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "pix_payments_payments_aud" (
    "pix_payment_id" integer NOT NULL,
    "currency" varchar,
    "amount" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("pix_payment_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "creditor_accounts_aud" (
    "creditor_account_id" integer NOT NULL,
    "ispb" varchar,
    "issuer" varchar,
    "number" varchar,
    "account_type" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("creditor_account_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "pix_payments_aud" (
    "reference_id" integer NOT NULL,
    "payment_id" varchar,
    "local_instrument" varchar,
    "pix_payment_id" integer,
    "creditor_account_id" integer,
    "remittance_information" varchar,
    "qr_code" varchar,
    "proxy" varchar,
    "status" varchar,
    "creation_date_time" timestamp,
    "status_update_date_time" timestamp,
    "rejection_reason" varchar,
    "idempotency_key" varchar,
    "payment_consent_id" varchar, -- THIS IS JUST WRONG, HIBERNATE DONE GONE WEIRD, the field is a varchar
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("payment_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);
