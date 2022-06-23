CREATE TABLE "contracts" (
    "contract_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()) PRIMARY KEY,
    "contract_type" varchar,
    "contract_date" varchar,
    "company_cnpj" varchar,
    "product_name" varchar,
    "product_type" varchar,
    "product_sub_type" varchar,
    "disbursement_date" varchar,
    "settlement_date" varchar,
    "contract_amount" double precision,
    "currency" varchar,
    "due_date" varchar,
    "instalment_periodicity" varchar,
    "instalment_periodicity_additional_info" varchar,
    "first_instalment_due_date" varchar,
    "cet" double precision,
    "amortization_scheduled" varchar,
    "amortization_scheduled_additional_info" varchar,
    "ipoc_code" varchar NOT NULL,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar
);

CREATE TABLE "interest_rates" (
    "interest_rates_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()) PRIMARY KEY,
    "tax_type" varchar,
    "interest_rate_type" varchar,
    "tax_periodicity" varchar,
    "calculation" varchar,
    "referential_rate_indexer_type" varchar,
    "referential_rate_indexer_sub_type" varchar,
    "referential_rate_indexer_additional_info" varchar,
    "pre_fixed_rate" double precision,
    "post_fixed_rate" double precision,
    "additional_info" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "contract_id" uuid NOT NULL,
    FOREIGN KEY ("contract_id") REFERENCES "contracts" ("contract_id")
);

CREATE TABLE "contracted_fees" (
    "contracted_fees_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()) PRIMARY KEY,
    "fee_name" varchar,
    "fee_code" varchar,
    "fee_charge_type" varchar,
    "fee_charge" varchar,
    "fee_amount" double precision,
    "fee_rate" double precision,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "contract_id" uuid NOT NULL,
    FOREIGN KEY ("contract_id") REFERENCES "contracts" ("contract_id")
);

CREATE TABLE "contracted_finance_charges" (
    "contracted_finance_charges_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()) PRIMARY KEY,
    "charge_type" varchar,
    "charge_additional_info" varchar,
    "charge_rate" double precision,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "contract_id" uuid NOT NULL,
    FOREIGN KEY ("contract_id") REFERENCES "contracts" ("contract_id")
);

CREATE TABLE "payments" (
    "payments_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()) PRIMARY KEY,
    "paid_instalments" double precision NOT NULL,
    "contract_outstanding_balance" double precision NOT NULL,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "contract_id" uuid NOT NULL,
    FOREIGN KEY ("contract_id") REFERENCES "contracts" ("contract_id")
);

CREATE TABLE "releases" (
    "releases_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()) PRIMARY KEY,
    "is_over_parcel_payment" boolean NOT NULL,
    "instalment_id" varchar NOT NULL,
    "paid_date" varchar NOT NULL,
    "currency" varchar NOT NULL,
    "paid_amount" double precision NOT NULL,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "payments_id" uuid NOT NULL,
    FOREIGN KEY ("payments_id") REFERENCES "payments" ("payments_id")
);

CREATE TABLE "over_parcel_fees" (
    "over_parcel_fees_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()) PRIMARY KEY,
    "fee_name" varchar NOT NULL,
    "fee_code" varchar NOT NULL,
    "fee_amount" double precision NOT NULL,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "releases_id" uuid NOT NULL,
    FOREIGN KEY ("releases_id") REFERENCES "releases" ("releases_id")
);

CREATE TABLE "over_parcel_charges" (
    "over_parcel_charges_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()) PRIMARY KEY,
    "charge_type" varchar NOT NULL,
    "charge_additional_info" varchar NOT NULL,
    "charge_amount" double precision NOT NULL,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "releases_id" uuid NOT NULL,
    FOREIGN KEY ("releases_id") REFERENCES "releases" ("releases_id")
);

CREATE TABLE "scheduled_instalments" (
    "scheduled_instalments_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()) PRIMARY KEY,
    "type_number_of_instalments" varchar NOT NULL,
    "total_number_of_instalments" double precision NOT NULL,
    "type_contract_remaining" varchar NOT NULL,
    "contract_remaining_number" double precision NOT NULL,
    "paid_instalments" double precision NOT NULL,
    "due_instalments" double precision NOT NULL,
    "past_due_instalments" double precision NOT NULL,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "contract_id" uuid NOT NULL,
    FOREIGN KEY ("contract_id") REFERENCES "contracts" ("contract_id")
);

CREATE TABLE "balloon_payments" (
    "balloon_payments_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()) PRIMARY KEY,
    "due_date" varchar NOT NULL,
    "currency" varchar NOT NULL,
    "amount" double precision NOT NULL,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "scheduled_instalments_id" uuid NOT NULL,
    FOREIGN KEY ("scheduled_instalments_id") REFERENCES "scheduled_instalments" ("scheduled_instalments_id")
);

CREATE TABLE "warranties" (
    "warranty_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()) PRIMARY KEY,
    "currency" varchar NOT NULL,
    "warranty_type" varchar NOT NULL,
    "warranty_subtype" varchar NOT NULL,
    "warranty_amount" double precision,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "contract_id" uuid NOT NULL,
    FOREIGN KEY ("contract_id") REFERENCES "contracts" ("contract_id")
);

CREATE TABLE "contracts_aud" (
    "contract_id" uuid NOT NULL,
    "contract_type" varchar,
    "contract_date" varchar,
    "company_cnpj" varchar,
    "product_name" varchar,
    "product_type" varchar,
    "product_sub_type" varchar,
    "disbursement_date" varchar,
    "settlement_date" varchar,
    "contract_amount" double precision,
    "currency" varchar,
    "due_date" varchar,
    "instalment_periodicity" varchar,
    "instalment_periodicity_additional_info" varchar,
    "first_instalment_due_date" varchar,
    "cet" double precision,
    "amortization_scheduled" varchar,
    "amortization_scheduled_additional_info" varchar,
    "ipoc_code" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("contract_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "interest_rates_aud" (
    "interest_rates_id" uuid NOT NULL,
    "tax_type" varchar,
    "interest_rate_type" varchar,
    "tax_periodicity" varchar,
    "calculation" varchar,
    "referential_rate_indexer_type" varchar,
    "referential_rate_indexer_sub_type" varchar,
    "referential_rate_indexer_additional_info" varchar,
    "pre_fixed_rate" double precision,
    "post_fixed_rate" double precision,
    "additional_info" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "contract_id" uuid,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("interest_rates_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "contracted_fees_aud" (
    "contracted_fees_id" uuid NOT NULL,
    "fee_name" varchar,
    "fee_code" varchar,
    "fee_charge_type" varchar,
    "fee_charge" varchar,
    "fee_amount" double precision,
    "fee_rate" double precision,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "contract_id" uuid,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("contracted_fees_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "contracted_finance_charges_aud" (
    "contracted_finance_charges_id" uuid NOT NULL,
    "charge_type" varchar,
    "charge_additional_info" varchar,
    "charge_rate" double precision,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "contract_id" uuid,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("contracted_finance_charges_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "over_parcel_fees_aud" (
    "over_parcel_fees_id" uuid NOT NULL,
    "fee_name" varchar,
    "fee_code" varchar,
    "fee_amount" double precision,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "releases_id" uuid NOT NULL,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("over_parcel_fees_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "over_parcel_charges_aud" (
    "over_parcel_charges_id" uuid NOT NULL,
    "charge_type" varchar,
    "charge_additional_info" varchar,
    "charge_amount" double precision,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "releases_id" uuid NOT NULL,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("over_parcel_charges_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "payments_aud" (
    "payments_id" uuid UNIQUE NOT NULL,
    "paid_instalments" double precision,
    "contract_outstanding_balance" double precision,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "contract_id" uuid,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("payments_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "releases_aud" (
    "releases_id" uuid NOT NULL,
    "payment_id" varchar,
    "is_over_parcel_payment" boolean,
    "instalment_id" varchar,
    "paid_date" varchar,
    "currency" varchar,
    "paid_amount" double precision,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "payments_id" uuid,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("releases_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "scheduled_instalments_aud" (
    "scheduled_instalments_id" uuid UNIQUE NOT NULL,
    "type_number_of_instalments" varchar,
    "total_number_of_instalments" double precision,
    "type_contract_remaining" varchar,
    "contract_remaining_number" double precision,
    "paid_instalments" double precision,
    "due_instalments" double precision,
    "past_due_instalments" double precision,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "contract_id" uuid,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("scheduled_instalments_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "balloon_payments_aud" (
    "balloon_payments_id" uuid NOT NULL,
    "due_date" varchar,
    "currency" varchar,
    "amount" double precision,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "scheduled_instalments_id" uuid,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("balloon_payments_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "warranties_aud" (
    "warranty_id" uuid UNIQUE NOT NULL,
    "currency" varchar,
    "warranty_type" varchar,
    "warranty_subtype" varchar,
    "warranty_amount" double precision,
    "created_at" timestamp,
    "updated_at" timestamp,
    "created_by" varchar,
    "updated_by" varchar,
    "hibernate_status" varchar,
    "contract_id" uuid,
    "rev" int4 NOT NULL,
    "revtype" int2,
    PRIMARY KEY ("warranty_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);