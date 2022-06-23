DROP TABLE "consent_account_ids";
DROP TABLE "consent_account_ids_aud";

CREATE TABLE "consent_accounts" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "consent_id" varchar,
    "account_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("consent_id") REFERENCES "consents" ("consent_id"),
    FOREIGN KEY ("account_id") REFERENCES "accounts" ("account_id")
);

CREATE TABLE "consent_accounts_aud" (
    "reference_id" integer,
    "consent_id" varchar,
    "account_id" uuid,
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

CREATE TABLE "consent_contracts" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "consent_id" varchar,
    "contract_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("consent_id") REFERENCES "consents" ("consent_id"),
    FOREIGN KEY ("contract_id") REFERENCES "contracts" ("contract_id")
);

CREATE TABLE "consent_contracts_aud" (
    "reference_id" integer,
    "consent_id" varchar,
    "contract_id" uuid,
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

ALTER TABLE "contracts" ADD COLUMN "status" varchar;
ALTER TABLE "contracts_aud" ADD COLUMN "status" varchar;