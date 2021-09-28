-- noinspection SqlNoDataSourceInspectionForFile

CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE SEQUENCE IF NOT EXISTS hibernate_sequence INCREMENT 1 START 1 MINVALUE 1;

CREATE TABLE "accounts" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "account_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()),
    "status" varchar,
    "status_update_date_time" timestamp,
    "currency" varchar,
    "account_type" varchar,
    "account_sub_type" varchar,
    "description" varchar,
    "nickname" varchar,
    "opening_date" timestamp,
    "maturity_date" timestamp,
    "switch_status" varchar,
    "servicer_scheme_name" varchar,
    "servicer_identification" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar
);

CREATE TABLE "accounts_priv" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "scheme_name" varchar NOT NULL,
    "identification" varchar NOT NULL,
    "name" varchar,
    "secondary_identification" varchar,
    "account_id" uuid NOT NULL,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("account_id") REFERENCES "accounts" ("account_id")
);

CREATE TABLE "consents" (
    "reference_id" SERIAL PRIMARY KEY,
    "consent_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()),
    "expiration_date_time" timestamp,
    "transaction_from_date_time" timestamp,
    "transaction_to_date_time" timestamp,
    "creation_date_time" timestamp,
    "status_update_date_time" timestamp NOT NULL,
    "status" varchar NOT NULL,
    "risk" varchar NOT NULL,
    "client_id" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar
);

CREATE TABLE "consent_permissions" (
    "reference_id" SERIAL PRIMARY KEY,
    "permission" varchar NOT NULL,
    "consent_id" uuid NOT NULL,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("consent_id") REFERENCES "consents" ("consent_id")
);

CREATE TABLE "consent_account_ids" (
    "reference_id" SERIAL PRIMARY KEY,
    "account_id" varchar NOT NULL,
    "consent_id" uuid NOT NULL,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("consent_id") REFERENCES "consents" ("consent_id")
);

-- ## AUDIT TABLES

CREATE TABLE "revinfo" (
    "rev" int4 NOT NULL,
    "revtstmp" int8,
    PRIMARY KEY ("rev")
);

CREATE TABLE "accounts_aud" (
    "reference_id" integer NOT NULL,
    "account_id" uuid,
    "status" varchar,
    "status_update_date_time" timestamp,
    "currency" varchar,
    "account_type" varchar,
    "account_sub_type" varchar,
    "description" varchar,
    "nickname" varchar,
    "opening_date" timestamp,
    "maturity_date" timestamp,
    "switch_status" varchar,
    "servicer_scheme_name" varchar,
    "servicer_identification" varchar,
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

CREATE TABLE "accounts_priv_aud" (
    "reference_id" integer NOT NULL,
    "scheme_name" varchar,
    "identification" varchar,
    "name" varchar ,
    "secondary_identification" varchar,
    "account_id" uuid NOT NULL,
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

CREATE TABLE "consents_aud" (
    "reference_id" integer NOT NULL,
    "consent_id" uuid,
    "expiration_date_time" timestamp,
    "transaction_from_date_time" timestamp,
    "transaction_to_date_time" timestamp,
    "creation_date_time" timestamp,
    "status_update_date_time" timestamp,
    "status" varchar,
    "risk" varchar,
    "client_id" varchar,
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

CREATE TABLE "consent_permissions_aud" (
    "reference_id" integer NOT NULL,
    "permission" varchar,
    "consent_id" uuid,
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

CREATE TABLE "consent_account_ids_aud" (
    "reference_id" integer NOT NULL,
    "account_id" varchar,
    "consent_id" uuid,
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