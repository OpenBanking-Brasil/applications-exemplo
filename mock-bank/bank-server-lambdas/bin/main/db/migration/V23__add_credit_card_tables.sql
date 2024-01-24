CREATE TABLE "credit_card_accounts"(
    "credit_card_account_id" uuid PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    "brand_name" varchar(80),
    "company_cnpj" varchar(14),
    "name" varchar(50),
    "product_type" varchar(26),
    "product_additional_info" varchar(50),
    "credit_card_network" varchar(17),
    "network_additional_info" varchar(50),
    "status" varchar,
    "account_holder_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("account_holder_id") REFERENCES "account_holders" ("account_holder_id")
);

CREATE TABLE "credit_card_accounts_aud"(
    "credit_card_account_id" uuid NOT NULL,
    "brand_name" varchar(80),
    "company_cnpj" varchar(14),
    "name" varchar(50),
    "product_type" varchar(26),
    "product_additional_info" varchar(50),
    "credit_card_network" varchar(17),
    "network_additional_info" varchar(50),
    "status" varchar,
    "account_holder_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("credit_card_account_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "credit_cards_account_payment_method"(
    "payment_method_id" uuid PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    "identification_number" varchar,
    "is_multiple_credit_card" boolean,
    "credit_card_account_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("credit_card_account_id") REFERENCES "credit_card_accounts" ("credit_card_account_id")
);

CREATE TABLE "credit_cards_account_payment_method_aud"(
    "payment_method_id" uuid NOT NULL,
    "identification_number" varchar,
    "is_multiple_credit_card" boolean,
    "credit_card_account_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("payment_method_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "credit_card_accounts_bills"(
    "bill_id" uuid PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    "due_date" date,
    "bill_total_amount" double precision,
    "bill_total_amount_currency" varchar(3),
    "bill_minimum_amount" double precision,
    "bill_minimum_amount_currency" varchar(3),
    "is_instalment" boolean,
    "credit_card_account_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("credit_card_account_id") REFERENCES "credit_card_accounts" ("credit_card_account_id")
);

CREATE TABLE "credit_card_accounts_bills_aud"(
    "bill_id" uuid NOT NULL,
    "due_date" date,
    "bill_total_amount" double precision,
    "bill_total_amount_currency" varchar(3),
    "bill_minimum_amount" double precision,
    "bill_minimum_amount_currency" varchar(3),
    "is_instalment" boolean,
    "credit_card_account_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("bill_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "credit_card_accounts_bills_finance_charge"(
    "finance_charge_id" uuid PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    "type" varchar(44),
    "additional_info" varchar(140),
    "amount" double precision,
    "currency" varchar(3),
    "bill_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("bill_id") REFERENCES "credit_card_accounts_bills" ("bill_id")
);

CREATE TABLE "credit_card_accounts_bills_finance_charge_aud"(
    "finance_charge_id" uuid NOT NULL,
    "type" varchar(44),
    "additional_info" varchar(140),
    "amount" double precision,
    "currency" varchar(3),
    "bill_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("finance_charge_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "credit_card_accounts_bills_payment"(
    "payment_id" uuid PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    "value_type" varchar(32),
    "payment_date" date,
    "payment_mode" varchar(21),
    "amount" double precision,
    "currency" varchar(3),
    "bill_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("bill_id") REFERENCES "credit_card_accounts_bills" ("bill_id")
);

CREATE TABLE "credit_card_accounts_bills_payment_aud"(
    "payment_id" uuid NOT NULL,
    "value_type" varchar(32),
    "payment_date" date,
    "payment_mode" varchar(21),
    "amount" double precision,
    "currency" varchar(3),
    "bill_id" uuid,
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

CREATE TABLE "credit_card_accounts_transaction"(
    "transaction_id" uuid PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    "identification_number" varchar(100),
    "line_name" varchar(28),
    "transaction_name" varchar(100),
    "bill_id" uuid,
    "credit_debit_type" varchar(7),
    "transaction_type" varchar(36),
    "transactional_additional_info" varchar(140),
    "payment_type" varchar(7),
    "fee_type" varchar(29),
    "fee_type_additional_info" varchar(140),
    "other_credits_type" varchar(19),
    "other_credits_additional_info" varchar(50),
    "charge_identificator" varchar(50),
    "charge_number" int8,
    "brazilian_amount" double precision,
    "amount" double precision,
    "currency" varchar(3),
    "transaction_date" date,
    "bill_post_date" date,
    "payee_mcc" int8,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("bill_id") REFERENCES "credit_card_accounts_bills" ("bill_id")
);

CREATE TABLE "credit_card_accounts_transaction_aud"(
    "transaction_id" uuid NOT NULL,
    "identification_number" varchar(100),
    "line_name" varchar(28),
    "transaction_name" varchar(100),
    "bill_id" uuid,
    "credit_debit_type" varchar(7),
    "transaction_type" varchar(36),
    "transactional_additional_info" varchar(140),
    "payment_type" varchar(7),
    "fee_type" varchar(29),
    "fee_type_additional_info" varchar(140),
    "other_credits_type" varchar(19),
    "other_credits_additional_info" varchar(50),
    "charge_identificator" varchar(50),
    "charge_number" int8,
    "brazilian_amount" double precision,
    "amount" double precision,
    "currency" varchar(3),
    "transaction_date" date,
    "bill_post_date" date,
    "payee_mcc" int8,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("transaction_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "credit_card_accounts_limits"(
    "limit_id" uuid PRIMARY KEY NOT NULL DEFAULT uuid_generate_v4(),
    "credit_line_limit_type" varchar(34),
    "consolidation_type" varchar(11),
    "identification_number" varchar(100),
    "line_name" varchar(28),
    "line_name_additional_info" varchar,
    "is_limit_flexible" boolean,
    "limit_amount_currency" varchar(3),
    "limit_amount" double precision,
    "used_amount_currency" varchar(3),
    "used_amount" double precision,
    "available_amount_currency" varchar(3),
    "available_amount" double precision,
    "credit_card_account_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("credit_card_account_id") REFERENCES "credit_card_accounts" ("credit_card_account_id")
);

CREATE TABLE "credit_card_accounts_limits_aud"(
    "limit_id" uuid NOT NULL,
    "credit_line_limit_type" varchar(34),
    "consolidation_type" varchar(11),
    "identification_number" varchar(100),
    "line_name" varchar(28),
    "line_name_additional_info" varchar,
    "is_limit_flexible" boolean,
    "limit_amount_currency" varchar(3),
    "limit_amount" double precision,
    "used_amount_currency" varchar(3),
    "used_amount" double precision,
    "available_amount_currency" varchar(3),
    "available_amount" double precision,
    "credit_card_account_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("limit_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "consent_credit_card_accounts"(
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "consent_id" varchar,
    "credit_card_account_id" uuid,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("consent_id") REFERENCES "consents" ("consent_id"),
    FOREIGN KEY ("credit_card_account_id") REFERENCES "credit_card_accounts" ("credit_card_account_id")
);

CREATE TABLE "consent_credit_card_accounts_aud"(
    "reference_id" integer NOT NULL,
    "consent_id" varchar,
    "credit_card_account_id" uuid,
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