ALTER TABLE payment_consents DROP COLUMN "debtor_account_id";
ALTER TABLE payment_consents ADD COLUMN "account_id" uuid;
ALTER TABLE payment_consents_aud DROP COLUMN "debtor_account_id";
ALTER TABLE payment_consents_aud ADD COLUMN "account_id" uuid;
ALTER TABLE accounts ADD COLUMN "debtor_ispb" varchar;
ALTER TABLE accounts ADD COLUMN "debtor_issuer" varchar;
ALTER TABLE accounts ADD COLUMN "debtor_type" varchar;
ALTER TABLE accounts_aud ADD COLUMN "debtor_ispb" varchar;
ALTER TABLE accounts_aud ADD COLUMN "debtor_issuer" varchar;
ALTER TABLE accounts_aud ADD COLUMN "debtor_type" varchar;

ALTER TABLE payment_consents
    ADD CONSTRAINT payment_consents_account_id_fkey
        FOREIGN KEY(account_id)
            REFERENCES accounts(account_id) ON DELETE CASCADE;

DROP TABLE debtor_accounts;
DROP TABLE debtor_accounts_aud;