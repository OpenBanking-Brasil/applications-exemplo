-- noinspection SqlNoDataSourceInspectionForFile

ALTER TABLE consents DROP COLUMN risk;

CREATE TABLE "business_entity_documents" (
    "business_entity_document_id" SERIAL PRIMARY KEY NOT NULL,
    "identification" varchar,
    "rel" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar
);

CREATE TABLE "business_entity_documents_aud" (
    "business_entity_document_id" integer NOT NULL,
    "identification" varchar,
    "rel" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    "created_by" varchar,
    "updated_by" varchar,
    PRIMARY KEY ("business_entity_document_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

CREATE TABLE "logged_in_user_entity_documents" (
    "logged_in_user_entity_document_id" SERIAL PRIMARY KEY NOT NULL,
    "identification" varchar,
    "rel" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "created_by" varchar,
    "updated_by" varchar
);

CREATE TABLE "logged_in_user_entity_documents_aud" (
    "logged_in_user_entity_document_id" integer NOT NULL,
    "identification" varchar,
    "rel" varchar,
    "created_at" timestamp,
    "updated_at" timestamp,
    "hibernate_status" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2,
    "created_by" varchar,
    "updated_by" varchar,
    PRIMARY KEY ("logged_in_user_entity_document_id", "rev"),
    FOREIGN KEY ("rev") REFERENCES "revinfo" ("rev")
);

ALTER TABLE consents ADD COLUMN business_entity_document_id SERIAL;
ALTER TABLE consents_aud ADD COLUMN business_entity_document_id SERIAL;

ALTER TABLE consents ADD COLUMN logged_in_user_entity_document_id SERIAL;
ALTER TABLE consents_aud ADD COLUMN logged_in_user_entity_document_id SERIAL;

ALTER TABLE consents ADD CONSTRAINT consent_bed_fk FOREIGN KEY(business_entity_document_id) REFERENCES business_entity_documents(business_entity_document_id);
ALTER TABLE consents ADD CONSTRAINT consent_led_fk FOREIGN KEY(logged_in_user_entity_document_id) REFERENCES logged_in_user_entity_documents(logged_in_user_entity_document_id);