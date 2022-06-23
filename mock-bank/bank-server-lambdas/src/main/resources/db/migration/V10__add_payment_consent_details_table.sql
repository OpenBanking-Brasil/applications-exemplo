CREATE TABLE "payment_consent_details" (
    "payment_consent_details_id" SERIAL PRIMARY KEY NOT NULL,
    "local_instrument" varchar,
    "qr_code" varchar,
    "proxy" varchar,
    "creditor_ispb" varchar,
    "creditor_issuer" varchar,
    "creditor_account_number" varchar,
    "creditor_account_type" varchar
);
DELETE from pix_payments;
DELETE FROM payment_consents;
DELETE FROM payment_consent_payments;

ALTER TABLE payment_consent_payments ADD COLUMN payment_consent_details_id SERIAL;
ALTER TABLE payment_consent_payments_aud ADD COLUMN payment_consent_details_id SERIAL;

ALTER TABLE payment_consent_payments ADD CONSTRAINT payment_consent_details_fk FOREIGN KEY(payment_consent_details_id) REFERENCES payment_consent_details(payment_consent_details_id);

CREATE TABLE "payment_consent_details_aud" (
    "payment_consent_details_id" SERIAL PRIMARY KEY NOT NULL,
    "local_instrument" varchar,
    "qr_code" varchar,
    "proxy" varchar,
    "creditor_ispb" varchar,
    "creditor_issuer" varchar,
    "creditor_account_number" varchar,
    "creditor_account_type" varchar,
    "rev" int4 NOT NULL,
    "revtype" int2
);
