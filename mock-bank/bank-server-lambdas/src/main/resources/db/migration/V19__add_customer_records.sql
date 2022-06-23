CREATE TABLE "personal_identifications" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_identifications_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()),
    "account_holder_id" uuid,
    "brand_name" varchar(80),
    "civil_name" varchar(70),
    "social_name" varchar(70),
    "birth_date" date,
    "marital_status_code" varchar,
    "marital_status_additional_info" varchar,
    "sex" varchar,
    "has_brazilian_nationality" boolean,
    "cpf_number" varchar(11),
    "passport_number" varchar(20),
    "passport_country" varchar(3),
    "passport_expiration_date" date,
    "passport_issue_date" date,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("account_holder_id") REFERENCES "account_holders" ("account_holder_id")
);

CREATE TABLE "personal_identifications_company_cnpj" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_identifications_id" uuid NOT NULL,
    "company_cnpj" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("personal_identifications_id") REFERENCES "personal_identifications" ("personal_identifications_id")
);

CREATE TABLE "personal_other_documents" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_identifications_id" uuid NOT NULL,
    "type" varchar,
    "type_additional_info" varchar(70),
    "number" varchar(11),
    "check_digit" varchar(2),
    "additional_info" varchar(50),
    "expiration_date" date,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("personal_identifications_id") REFERENCES "personal_identifications" ("personal_identifications_id")
);

CREATE TABLE "personal_nationality" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_nationality_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()),
    "personal_identifications_id" uuid NOT NULL,
    "other_nationalities_info" varchar(40),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("personal_identifications_id") REFERENCES "personal_identifications" ("personal_identifications_id")
);

CREATE TABLE "personal_nationality_documents" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_nationality_id" uuid NOT NULL,
    "type" varchar,
    "number" varchar(11),
    "expiration_date" date,
    "issue_date" date,
    "country" varchar(80),
    "type_additional_info" varchar(70),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("personal_nationality_id") REFERENCES "personal_nationality" ("personal_nationality_id")
);

CREATE TABLE "personal_filiation" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_identifications_id" uuid NOT NULL,
    "type" varchar,
    "civil_name" varchar(70),
    "social_name" varchar(70),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("personal_identifications_id") REFERENCES "personal_identifications" ("personal_identifications_id")
);

CREATE TABLE "personal_postal_addresses" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_identifications_id" uuid NOT NULL,
    "is_main" boolean,
    "address" varchar(150),
    "additional_info" varchar(30),
    "district_name" varchar(50),
    "town_name" varchar(50),
    "ibge_town_code" varchar(7),
    "country_subdivision" varchar,
    "post_code" varchar(8),
    "country" varchar(80),
    "country_code" varchar(3),
    "latitude" varchar(13),
    "longitude" varchar(13),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("personal_identifications_id") REFERENCES "personal_identifications" ("personal_identifications_id")
);

CREATE TABLE "personal_phones" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_identifications_id" uuid NOT NULL,
    "is_main" boolean,
    "type" varchar(5),
    "additional_info" varchar(70),
    "country_calling_code" varchar(4),
    "area_code" varchar(2),
    "number" varchar(11),
    "phone_extension" varchar(5),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("personal_identifications_id") REFERENCES "personal_identifications" ("personal_identifications_id")
);

CREATE TABLE "personal_emails" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_identifications_id" uuid NOT NULL,
    "is_main" boolean,
    "email" varchar(320),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("personal_identifications_id") REFERENCES "personal_identifications" ("personal_identifications_id")
);

CREATE TABLE "personal_qualifications" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_qualifications_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()),
    "account_holder_id" uuid,
    "company_cnpj" varchar(14),
    "occupation_code" varchar,
    "occupation_description" varchar(100),
    "informed_income_frequency" varchar,
    "informed_income_amount" numeric,
    "informed_income_currency" varchar(3),
    "informed_income_date" date,
    "informed_patrimony_amount" numeric,
    "informed_patrimony_currency" varchar(3),
    "informed_patrimony_year" integer,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("account_holder_id") REFERENCES "account_holders" ("account_holder_id")
);

CREATE TABLE "personal_financial_relations" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_financial_relations_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()),
    "account_holder_id" uuid,
    "start_date" date,
    "products_services_type_additional_info" varchar(100),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("account_holder_id") REFERENCES "account_holders" ("account_holder_id")
);

CREATE TABLE "personal_financial_relations_products_services_type" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_financial_relations_id" uuid NOT NULL,
    "type" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("personal_financial_relations_id") REFERENCES "personal_financial_relations" ("personal_financial_relations_id")
);

CREATE TABLE "personal_financial_relations_procurators" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_financial_relations_id" uuid NOT NULL,
    "type" varchar(19),
    "cpf_number" varchar(11),
    "civil_name" varchar(70),
    "social_name" varchar(70),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("personal_financial_relations_id") REFERENCES "personal_financial_relations" ("personal_financial_relations_id")
);

CREATE TABLE "personal_financial_relations_accounts" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "personal_financial_relations_id" uuid NOT NULL,
    "compe_code" varchar(3),
    "branch_code" varchar(4),
    "number" varchar(20),
    "check_digit" varchar(1),
    "type" varchar,
    "sub_type" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("personal_financial_relations_id") REFERENCES "personal_financial_relations" ("personal_financial_relations_id")
);

CREATE TABLE "business_identifications" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "business_identifications_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()),
    "account_holder_id" uuid,
    "brand_name" varchar(80),
    "company_name" varchar(70),
    "trade_name" varchar(70),
    "incorporation_date" date,
    "cnpj_number" varchar(14),
    "company_cnpj" varchar(14),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("account_holder_id") REFERENCES "account_holders" ("account_holder_id")
);

CREATE TABLE "business_other_documents" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "business_identifications_id" uuid NOT NULL,
    "type" varchar(20),
    "number" varchar(20),
    "country" varchar(3),
    "expiration_date" date,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("business_identifications_id") REFERENCES "business_identifications" ("business_identifications_id")
);

CREATE TABLE "business_parties" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "business_identifications_id" uuid NOT NULL,
    "person_type" varchar,
    "type" varchar(13),
    "civil_name" varchar(70),
    "social_name" varchar(70),
    "company_name" varchar(70),
    "trade_name" varchar(70),
    "start_date" date,
    "shareholding" varchar(4),
    "document_type" varchar,
    "document_number" varchar(20),
    "document_addtional_info" varchar(100),
    "document_country" varchar(3),
    "document_expiration_date" date,
    "document_issue_date" date,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("business_identifications_id") REFERENCES "business_identifications" ("business_identifications_id")
);

CREATE TABLE "business_postal_addresses" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "business_identifications_id" uuid NOT NULL,
    "is_main" boolean,
    "address" varchar(150),
    "additional_info" varchar(30),
    "district_name" varchar(50),
    "town_name" varchar(50),
    "ibge_town_code" varchar(7),
    "country_subdivision" varchar,
    "post_code" varchar(8),
    "country" varchar(80),
    "country_code" varchar(3),
    "latitude" varchar(13),
    "longitude" varchar(13),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("business_identifications_id") REFERENCES "business_identifications" ("business_identifications_id")
);

CREATE TABLE "business_phones" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "business_identifications_id" uuid NOT NULL,
    "is_main" boolean,
    "type" varchar(5),
    "additional_info" varchar(70),
    "country_calling_code" varchar(4),
    "area_code" varchar(2),
    "number" varchar(11),
    "phone_extension" varchar(5),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("business_identifications_id") REFERENCES "business_identifications" ("business_identifications_id")
);

CREATE TABLE "business_emails" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "business_identifications_id" uuid NOT NULL,
    "is_main" boolean,
    "email" varchar(320),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("business_identifications_id") REFERENCES "business_identifications" ("business_identifications_id")
);

CREATE TABLE "business_qualifications" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "business_qualifications_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()),
    "account_holder_id" uuid,
    "informed_revenue_frequency" varchar,
    "informed_revenue_frequency_additional_information" varchar(100),
    "informed_revenue_amount" numeric,
    "informed_revenue_currency" varchar(3),
    "informed_revenue_year" integer,
    "informed_patrimony_amount" varchar(20),
    "informed_patrimony_currency" varchar(3),
    "informed_patrimony_date" date,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("account_holder_id") REFERENCES "account_holders" ("account_holder_id")
);

CREATE TABLE "business_qualifications_economic_activities" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "business_qualifications_id" uuid NOT NULL,
    "code" integer,
    "is_main" boolean,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("business_qualifications_id") REFERENCES "business_qualifications" ("business_qualifications_id")
);

CREATE TABLE "business_financial_relations" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "business_financial_relations_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()),
    "account_holder_id" uuid,
    "start_date" date,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("account_holder_id") REFERENCES "account_holders" ("account_holder_id")
);

CREATE TABLE "business_financial_relations_products_services_type" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "business_financial_relations_id" uuid NOT NULL,
    "type" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("business_financial_relations_id") REFERENCES "business_financial_relations" ("business_financial_relations_id")
);

CREATE TABLE "business_financial_relations_procurators" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "business_financial_relations_id" uuid NOT NULL,
    "type" varchar(19),
    "cnpj_cpf_number" varchar(11),
    "civil_name" varchar(70),
    "social_name" varchar(70),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("business_financial_relations_id") REFERENCES "business_financial_relations" ("business_financial_relations_id")
);

-- # Audit tables

CREATE TABLE "personal_identifications_aud" (
    "reference_id" integer NOT NULL,
    "personal_identifications_id" uuid,
    "account_holder_id" uuid,
    "brand_name" varchar(80),
    "civil_name" varchar(70),
    "social_name" varchar(70),
    "birth_date" date,
    "marital_status_code" varchar,
    "marital_status_additional_info" varchar,
    "sex" varchar,
    "has_brazilian_nationality" boolean,
    "cpf_number" varchar(11),
    "passport_number" varchar(20),
    "passport_country" varchar(3),
    "passport_expiration_date" date,
    "passport_issue_date" date,
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

CREATE TABLE "personal_identifications_company_cnpj_aud" (
    "reference_id" integer NOT NULL,
    "personal_identifications_id" uuid,
    "company_cnpj" varchar,
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

CREATE TABLE "personal_other_documents_aud" (
    "reference_id" integer NOT NULL,
    "personal_identifications_id" uuid,
    "type" varchar,
    "type_additional_info" varchar(70),
    "number" varchar(11),
    "check_digit" varchar(2),
    "additional_info" varchar(50),
    "expiration_date" date,
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

CREATE TABLE "personal_nationality_aud" (
    "reference_id" integer NOT NULL,
    "personal_nationality_id" uuid,
    "personal_identifications_id" uuid,
    "other_nationalities_info" varchar(40),
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

CREATE TABLE "personal_nationality_documents_aud" (
    "reference_id" integer NOT NULL,
    "personal_nationality_id" uuid,
    "type" varchar,
    "number" varchar(11),
    "expiration_date" date,
    "issue_date" date,
    "country" varchar(80),
    "type_additional_info" varchar(70),
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

CREATE TABLE "personal_filiation_aud" (
    "reference_id" integer NOT NULL,
    "personal_identifications_id" uuid,
    "type" varchar,
    "civil_name" varchar(70),
    "social_name" varchar(70),
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

CREATE TABLE "personal_postal_addresses_aud" (
    "reference_id" integer NOT NULL,
    "personal_identifications_id" uuid,
    "is_main" boolean,
    "address" varchar(150),
    "additional_info" varchar(30),
    "district_name" varchar(50),
    "town_name" varchar(50),
    "ibge_town_code" varchar(7),
    "country_subdivision" varchar,
    "post_code" varchar(8),
    "country" varchar(80),
    "country_code" varchar(3),
    "latitude" varchar(13),
    "longitude" varchar(13),
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

CREATE TABLE "personal_phones_aud" (
    "reference_id" integer NOT NULL,
    "personal_identifications_id" uuid,
    "is_main" boolean,
    "type" varchar(5),
    "additional_info" varchar(70),
    "country_calling_code" varchar(4),
    "area_code" varchar(2),
    "number" varchar(11),
    "phone_extension" varchar(5),
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

CREATE TABLE "personal_emails_aud" (
    "reference_id" integer NOT NULL,
    "personal_identifications_id" uuid NOT NULL,
    "is_main" boolean,
    "email" varchar(320),
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

CREATE TABLE "personal_qualifications_aud" (
    "reference_id" integer NOT NULL,
    "personal_qualifications_id" uuid UNIQUE NOT NULL DEFAULT (uuid_generate_v4()),
    "account_holder_id" uuid,
    "company_cnpj" varchar(14),
    "occupation_code" varchar,
    "occupation_description" varchar(100),
    "informed_income_frequency" varchar,
    "informed_income_amount" numeric,
    "informed_income_currency" varchar(3),
    "informed_income_date" date,
    "informed_patrimony_amount" numeric,
    "informed_patrimony_currency" varchar(3),
    "informed_patrimony_year" integer,
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

CREATE TABLE "personal_financial_relations_aud" (
    "reference_id" integer NOT NULL,
    "personal_financial_relations_id" uuid,
    "account_holder_id" uuid,
    "start_date" date,
    "products_services_type_additional_info" varchar(100),
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

CREATE TABLE "personal_financial_relations_products_services_type_aud" (
    "reference_id" integer NOT NULL,
    "personal_financial_relations_id" uuid,
    "type" varchar,
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

CREATE TABLE "personal_financial_relations_procurators_aud" (
    "reference_id" integer NOT NULL,
    "personal_financial_relations_id" uuid,
    "type" varchar(19),
    "cpf_number" varchar(11),
    "civil_name" varchar(70),
    "social_name" varchar(70),
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

CREATE TABLE "personal_financial_relations_accounts_aud" (
    "reference_id" integer NOT NULL,
    "personal_financial_relations_id" uuid,
    "compe_code" varchar(3),
    "branch_code" varchar(4),
    "number" varchar(20),
    "check_digit" varchar(1),
    "type" varchar,
    "sub_type" varchar,
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

CREATE TABLE "business_identifications_aud" (
    "reference_id" integer NOT NULL,
    "business_identifications_id" uuid,
    "account_holder_id" uuid,
    "brand_name" varchar(80),
    "company_name" varchar(70),
    "trade_name" varchar(70),
    "incorporation_date" date,
    "cnpj_number" varchar(14),
    "company_cnpj" varchar(14),
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

CREATE TABLE "business_other_documents_aud" (
    "reference_id" integer NOT NULL,
    "business_identifications_id" uuid,
    "type" varchar(20),
    "number" varchar(20),
    "country" varchar(3),
    "expiration_date" date,
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

CREATE TABLE "business_parties_aud" (
    "reference_id" integer NOT NULL,
    "business_identifications_id" uuid,
    "person_type" varchar,
    "type" varchar(13),
    "civil_name" varchar(70),
    "social_name" varchar(70),
    "company_name" varchar(70),
    "trade_name" varchar(70),
    "start_date" date,
    "shareholding" varchar(4),
    "document_type" varchar,
    "document_number" varchar(20),
    "document_addtional_info" varchar(100),
    "document_country" varchar(3),
    "document_expiration_date" date,
    "document_issue_date" date,
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

CREATE TABLE "business_postal_addresses_aud" (
    "reference_id" integer NOT NULL,
    "business_identifications_id" uuid,
    "is_main" boolean,
    "address" varchar(150),
    "additional_info" varchar(30),
    "district_name" varchar(50),
    "town_name" varchar(50),
    "ibge_town_code" varchar(7),
    "country_subdivision" varchar,
    "post_code" varchar(8),
    "country" varchar(80),
    "country_code" varchar(3),
    "latitude" varchar(13),
    "longitude" varchar(13),
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

CREATE TABLE "business_phones_aud" (
    "reference_id" integer NOT NULL,
    "business_identifications_id" uuid,
    "is_main" boolean,
    "type" varchar(5),
    "additional_info" varchar(70),
    "country_calling_code" varchar(4),
    "area_code" varchar(2),
    "number" varchar(11),
    "phone_extension" varchar(5),
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

CREATE TABLE "business_emails_aud" (
    "reference_id" integer NOT NULL,
    "business_identifications_id" uuid,
    "is_main" boolean,
    "email" varchar(320),
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

CREATE TABLE "business_qualifications_aud" (
    "reference_id" integer NOT NULL,
    "business_qualifications_id" uuid,
    "account_holder_id" uuid,
    "informed_revenue_frequency" varchar,
    "informed_revenue_frequency_additional_information" varchar(100),
    "informed_revenue_amount" numeric,
    "informed_revenue_currency" varchar(3),
    "informed_revenue_year" integer,
    "informed_patrimony_amount" varchar(20),
    "informed_patrimony_currency" varchar(3),
    "informed_patrimony_date" date,
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

CREATE TABLE "business_qualifications_economic_activities_aud" (
    "reference_id" integer NOT NULL,
    "business_qualifications_id" uuid,
    "code" integer,
    "is_main" boolean,
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

CREATE TABLE "business_financial_relations_aud" (
    "reference_id" integer NOT NULL,
    "business_financial_relations_id" uuid,
    "account_holder_id" uuid,
    "start_date" date,
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

CREATE TABLE "business_financial_relations_products_services_type_aud" (
    "reference_id" integer NOT NULL,
    "business_financial_relations_id" uuid,
    "type" varchar,
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

CREATE TABLE "business_financial_relations_procurators_aud" (
    "reference_id" integer NOT NULL,
    "business_financial_relations_id" uuid,
    "type" varchar(19),
    "cnpj_cpf_number" varchar(11),
    "civil_name" varchar(70),
    "social_name" varchar(70),
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

