
ALTER TABLE business_identifications DROP COLUMN "company_cnpj";
ALTER TABLE business_identifications_aud DROP COLUMN "company_cnpj";

CREATE TABLE "business_identifications_company_cnpj" (
    "reference_id" SERIAL PRIMARY KEY NOT NULL,
    "business_identifications_id" uuid NOT NULL,
    "company_cnpj" varchar(14),
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar,
    FOREIGN KEY ("business_identifications_id") REFERENCES "business_identifications" ("business_identifications_id")
);

CREATE TABLE "business_identifications_company_cnpj_aud" (
    "reference_id" integer,
    "business_identifications_id" uuid NOT NULL,
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

ALTER TABLE business_parties RENAME COLUMN "document_addtional_info" TO "document_additional_info";
ALTER TABLE business_parties_aud RENAME COLUMN "document_addtional_info" TO "document_additional_info";


ALTER TABLE business_qualifications DROP COLUMN "informed_patrimony_amount";
ALTER TABLE business_qualifications_aud DROP COLUMN "informed_patrimony_amount";
ALTER TABLE business_qualifications ADD COLUMN "informed_patrimony_amount" double precision;
ALTER TABLE business_qualifications_aud ADD COLUMN "informed_patrimony_amount" double precision;
