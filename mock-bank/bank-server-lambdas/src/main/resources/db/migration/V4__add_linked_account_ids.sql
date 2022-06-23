ALTER TABLE consent_account_ids DROP CONSTRAINT consent_account_ids_consent_id_fkey,
        ADD CONSTRAINT consent_account_ids_consent_id_fkey
        FOREIGN KEY(consent_id)
        REFERENCES consents(consent_id) ON DELETE CASCADE;

ALTER TABLE consent_account_ids ADD COLUMN account_type TEXT NOT NULL;
ALTER TABLE consent_account_ids_aud ADD COLUMN account_type TEXT NOT NULL;
