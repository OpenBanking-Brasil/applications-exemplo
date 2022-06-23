CREATE TABLE "account_balances" (
    "account_balances_id" integer PRIMARY KEY NOT NULL,
    "available_amount" float8,
    "available_amount_currency" varchar,
    "blocked_amount" float8,
    "blocked_amount_currency" varchar,
    "automatically_invested_amount" float8,
    "automatically_invested_amount_currency" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar
);

CREATE TABLE "account_balances_aud" (
    "account_balances_id" integer NOT NULL,
    "available_amount" float8,
    "available_amount_currency" varchar,
    "blocked_amount" float8,
    "blocked_amount_currency" varchar,
    "automatically_invested_amount" float8,
    "automatically_invested_amount_currency" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
     PRIMARY KEY ("account_balances_id", "rev"),
        FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "account_balances_api" (
    "reference_id" integer PRIMARY KEY NOT NULL,
    "account_id" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar
);

CREATE TABLE "account_balances_api_aud" (
    "reference_id" integer NOT NULL,
    "account_id" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    "account_balances" SERIAL,
    PRIMARY KEY ("reference_id", "rev"),
        FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);
ALTER TABLE account_balances_api ADD COLUMN account_balances SERIAL;
ALTER TABLE account_balances_api ADD CONSTRAINT account_balances_id FOREIGN KEY(account_balances) REFERENCES account_balances(account_balances_id);

CREATE TABLE "account_overdraft_limits" (
    "account_overdraft_limits_id" integer PRIMARY KEY NOT NULL,
    "account_id" varchar,
    "overdraft_contracted_limit" float8,
    "overdraft_contracted_limit_currency" varchar,
    "overdraft_used_limit" float8,
    "overdraft_used_limit_currency" varchar,
    "unarranged_overdraft_amount" float8,
    "unarranged_overdraft_amount_currency" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar
);

CREATE TABLE "account_overdraft_limits_aud" (
    "account_overdraft_limits_id" integer NOT NULL,
    "account_id" varchar,
    "overdraft_contracted_limit" float8,
    "overdraft_contracted_limit_currency" varchar,
    "overdraft_used_limit" float8,
    "overdraft_used_limit_currency" varchar,
    "unarranged_overdraft_amount" float8,
    "unarranged_overdraft_amount_currency" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
     PRIMARY KEY ("account_overdraft_limits_id", "rev"),
        FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);