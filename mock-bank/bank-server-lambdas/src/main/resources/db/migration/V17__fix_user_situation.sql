CREATE TABLE "account_holders" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "account_holder_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()),
    "document_identification" varchar(11),
    "document_rel" varchar (3),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar
);

CREATE TABLE "account_holders_aud" (
    "reference_id" integer NOT NULL,
    "account_holder_id" uuid,
    "document_identification" varchar(11),
    "document_rel" varchar (3),
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

-- Fix up the accounts table to resemble the OBB Brazil version
DROP TABLE "accounts_priv";
DROP TABLE "accounts_priv_aud";

ALTER TABLE "accounts" DROP COLUMN "status_update_date_time";
ALTER TABLE "accounts" DROP COLUMN "description";
ALTER TABLE "accounts" DROP COLUMN "nickname";
ALTER TABLE "accounts" DROP COLUMN "opening_date";
ALTER TABLE "accounts" DROP COLUMN "maturity_date";
ALTER TABLE "accounts" DROP COLUMN "switch_status";
ALTER TABLE "accounts" DROP COLUMN "servicer_scheme_name";
ALTER TABLE "accounts" DROP COLUMN "servicer_identification";

ALTER TABLE "accounts" ADD COLUMN "brand_name" varchar(80);
ALTER TABLE "accounts" ADD COLUMN "company_cnpj" varchar(14);
ALTER TABLE "accounts" ADD COLUMN "compe_code" varchar(3);
ALTER TABLE "accounts" ADD COLUMN "branch_code" varchar(4);
ALTER TABLE "accounts" ADD COLUMN "number" varchar(20);
ALTER TABLE "accounts" ADD COLUMN "check_digit" varchar(1);

ALTER TABLE "accounts_aud" DROP COLUMN "status_update_date_time";
ALTER TABLE "accounts_aud" DROP COLUMN "description";
ALTER TABLE "accounts_aud" DROP COLUMN "nickname";
ALTER TABLE "accounts_aud" DROP COLUMN "opening_date";
ALTER TABLE "accounts_aud" DROP COLUMN "maturity_date";
ALTER TABLE "accounts_aud" DROP COLUMN "switch_status";
ALTER TABLE "accounts_aud" DROP COLUMN "servicer_scheme_name";
ALTER TABLE "accounts_aud" DROP COLUMN "servicer_identification";

ALTER TABLE "accounts_aud" ADD COLUMN "brand_name" varchar(80);
ALTER TABLE "accounts_aud" ADD COLUMN "company_cnpj" varchar(14);
ALTER TABLE "accounts_aud" ADD COLUMN "compe_code" varchar(3);
ALTER TABLE "accounts_aud" ADD COLUMN "branch_code" varchar(4);
ALTER TABLE "accounts_aud" ADD COLUMN "number" varchar(20);
ALTER TABLE "accounts_aud" ADD COLUMN "check_digit" varchar(1);

-- account balances does not need to be a separate table
DROP TABLE "account_balances_api";
DROP TABLE "account_balances_api_aud";

DROP TABLE "account_balances";
DROP TABLE "account_balances_aud";

DROP TABLE "account_overdraft_limits";
DROP TABLE "account_overdraft_limits_aud";

ALTER TABLE "accounts" ADD COLUMN "available_amount" double precision;
ALTER TABLE "accounts" ADD COLUMN "available_amount_currency" varchar;
ALTER TABLE "accounts" ADD COLUMN "blocked_amount" double precision;
ALTER TABLE "accounts" ADD COLUMN "blocked_amount_currency" varchar;
ALTER TABLE "accounts" ADD COLUMN "automatically_invested_amount" double precision;
ALTER TABLE "accounts" ADD COLUMN "automatically_invested_amount_currency" varchar;
ALTER TABLE "accounts" ADD COLUMN "overdraft_contracted_limit" varchar;
ALTER TABLE "accounts" ADD COLUMN "overdraft_contracted_limit_currency" varchar;
ALTER TABLE "accounts" ADD COLUMN "overdraft_used_limit" varchar;
ALTER TABLE "accounts" ADD COLUMN "overdraft_used_limit_currency" varchar;
ALTER TABLE "accounts" ADD COLUMN "unarranged_overdraft_amount" varchar;
ALTER TABLE "accounts" ADD COLUMN "unarranged_overdraft_amount_currency" varchar;

ALTER TABLE "accounts_aud" ADD COLUMN "available_amount" double precision;
ALTER TABLE "accounts_aud" ADD COLUMN "available_amount_currency" varchar;
ALTER TABLE "accounts_aud" ADD COLUMN "blocked_amount" double precision;
ALTER TABLE "accounts_aud" ADD COLUMN "blocked_amount_currency" varchar;
ALTER TABLE "accounts_aud" ADD COLUMN "automatically_invested_amount" double precision;
ALTER TABLE "accounts_aud" ADD COLUMN "automatically_invested_amount_currency" varchar;
ALTER TABLE "accounts_aud" ADD COLUMN "overdraft_contracted_limit" varchar;
ALTER TABLE "accounts_aud" ADD COLUMN "overdraft_contracted_limit_currency" varchar;
ALTER TABLE "accounts_aud" ADD COLUMN "overdraft_used_limit" varchar;
ALTER TABLE "accounts_aud" ADD COLUMN "overdraft_used_limit_currency" varchar;
ALTER TABLE "accounts_aud" ADD COLUMN "unarranged_overdraft_amount" varchar;
ALTER TABLE "accounts_aud" ADD COLUMN "unarranged_overdraft_amount_currency" varchar;


-- now attach it to the account holders table
-- existing data migration?
ALTER TABLE "accounts" ADD COLUMN "account_holder_id" uuid;
ALTER TABLE "accounts" ADD FOREIGN KEY ("account_holder_id") REFERENCES "account_holders" ("account_holder_id");
ALTER TABLE "accounts_aud" ADD COLUMN "account_holder_id" uuid;

-- fix up consents

ALTER TABLE "consents" DROP CONSTRAINT "consent_led_fk";
ALTER TABLE "consents" DROP COLUMN "logged_in_user_entity_document_id";
ALTER TABLE "consents" ADD COLUMN "account_holder_id" uuid;
ALTER TABLE "consents" ADD FOREIGN KEY ("account_holder_id") REFERENCES "account_holders" ("account_holder_id");

ALTER TABLE "consents_aud" DROP COLUMN "logged_in_user_entity_document_id";
ALTER TABLE "consents_aud" ADD COLUMN "account_holder_id" uuid;

-- and payment_consents

ALTER TABLE "payment_consents" DROP CONSTRAINT "payment_consents_logged_in_user_entity_document_id_fkey";
ALTER TABLE "payment_consents" DROP COLUMN "logged_in_user_entity_document_id";
ALTER TABLE "payment_consents" ADD COLUMN "account_holder_id" uuid;
ALTER TABLE "payment_consents" ADD FOREIGN KEY ("account_holder_id") REFERENCES "account_holders" ("account_holder_id");

ALTER TABLE "payment_consents_aud" DROP COLUMN "logged_in_user_entity_document_id";
ALTER TABLE "payment_consents_aud" ADD COLUMN "account_holder_id" uuid;

-- make payment_consent_details conform to base entity requirements

ALTER TABLE "payment_consent_details" ADD COLUMN "created_at" timestamp;
ALTER TABLE "payment_consent_details" ADD COLUMN "updated_at" timestamp;
ALTER TABLE "payment_consent_details" ADD COLUMN "hibernate_status" varchar;
ALTER TABLE "payment_consent_details" ADD COLUMN "created_by" varchar;
ALTER TABLE "payment_consent_details" ADD COLUMN "updated_by" varchar;

ALTER TABLE "payment_consent_details_aud" ADD COLUMN "created_at" timestamp;
ALTER TABLE "payment_consent_details_aud" ADD COLUMN "updated_at" timestamp;
ALTER TABLE "payment_consent_details_aud" ADD COLUMN "hibernate_status" varchar;
ALTER TABLE "payment_consent_details_aud" ADD COLUMN "created_by" varchar;
ALTER TABLE "payment_consent_details_aud" ADD COLUMN "updated_by" varchar;

-- fix up contracts

ALTER TABLE "contracts" ADD COLUMN "account_holder_id" uuid;
ALTER TABLE "contracts" ADD FOREIGN KEY ("account_holder_id") REFERENCES "account_holders" ("account_holder_id");
ALTER TABLE "contracts" ADD COLUMN "paid_instalments" integer;
ALTER TABLE "contracts" ADD COLUMN "contract_outstanding_balance" double precision;

ALTER TABLE "contracts" ADD COLUMN "type_number_of_instalments" varchar;
ALTER TABLE "contracts" ADD COLUMN "total_number_of_instalments" integer;
ALTER TABLE "contracts" ADD COLUMN "type_contract_remaining" varchar;
ALTER TABLE "contracts" ADD COLUMN "contract_remaining_number" integer;
ALTER TABLE "contracts" ADD COLUMN "due_instalments" integer;
ALTER TABLE "contracts" ADD COLUMN "past_due_instalments" integer;

ALTER TABLE "contracts_aud" ADD COLUMN "account_holder_id" uuid;
ALTER TABLE "contracts_aud" ADD COLUMN "paid_instalments" integer;
ALTER TABLE "contracts_aud" ADD COLUMN "contract_outstanding_balance" double precision;
ALTER TABLE "contracts_aud" ADD COLUMN "type_number_of_instalments" varchar;
ALTER TABLE "contracts_aud" ADD COLUMN "total_number_of_instalments" integer;
ALTER TABLE "contracts_aud" ADD COLUMN "type_contract_remaining" varchar;
ALTER TABLE "contracts_aud" ADD COLUMN "contract_remaining_number" integer;
ALTER TABLE "contracts_aud" ADD COLUMN "due_instalments" integer;
ALTER TABLE "contracts_aud" ADD COLUMN "past_due_instalments" integer;


ALTER TABLE "releases" DROP CONSTRAINT "releases_payments_id_fkey";
ALTER TABLE "releases" DROP COLUMN "payments_id";
ALTER TABLE "releases" ADD COLUMN "payments_id" uuid DEFAULT uuid_generate_v4();
ALTER TABLE "releases" ADD COLUMN "contract_id" uuid;
ALTER TABLE "releases" ADD FOREIGN KEY ("contract_id") REFERENCES "contracts" ("contract_id");

ALTER TABLE "releases_aud" DROP COLUMN "payments_id";
ALTER TABLE "releases_aud" ADD COLUMN "payments_id" uuid DEFAULT uuid_generate_v4();
ALTER TABLE "releases_aud" ADD COLUMN "contract_id" uuid;

ALTER TABLE "balloon_payments" DROP CONSTRAINT "balloon_payments_scheduled_instalments_id_fkey";
ALTER TABLE "balloon_payments" DROP COLUMN "scheduled_instalments_id";
ALTER TABLE "balloon_payments" ADD COLUMN "contract_id" uuid;
ALTER TABLE "balloon_payments_aud" DROP COLUMN "scheduled_instalments_id";
ALTER TABLE "balloon_payments_aud" ADD COLUMN "contract_id" uuid;

DROP TABLE "payments";
DROP TABLE "payments_aud";

DROP TABLE "scheduled_instalments";
DROP TABLE "scheduled_instalments_aud";

-- remove the 'owner id' from an account balance as it is attached to the account now
-- migration plan?
-- ALTER TABLE "account_balances_api" DROP COLUMN "owner_id";
-- ALTER TABLE "account_balances_api_aud" DROP COLUMN "owner_id";

