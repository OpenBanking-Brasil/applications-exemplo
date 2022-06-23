ALTER TABLE consents
    DROP COLUMN consent_id;

ALTER TABLE consents_aud
    DROP COLUMN consent_id;

ALTER TABLE consent_permissions
    DROP COLUMN consent_id;

ALTER TABLE consent_account_ids
    DROP COLUMN consent_id;

ALTER TABLE consents
    RENAME COLUMN new_consent_id TO consent_id;

ALTER TABLE consents_aud
    RENAME COLUMN new_consent_id TO consent_id;

ALTER TABLE consent_permissions
    RENAME COLUMN new_consent_id TO consent_id;

ALTER TABLE consent_account_ids
    RENAME COLUMN new_consent_id TO consent_id;

ALTER TABLE consent_account_ids_aud
    ALTER COLUMN consent_id SET DATA TYPE TEXT;

ALTER TABLE consent_permissions_aud
    ALTER COLUMN consent_id SET DATA TYPE TEXT;

ALTER TABLE consent_permissions
        ADD CONSTRAINT consent_permissions_consent_id_fkey
        FOREIGN KEY(consent_id)
        REFERENCES consents(consent_id) ON DELETE CASCADE;

ALTER TABLE consent_account_ids
        ADD CONSTRAINT consent_account_ids_consent_id_fkey
        FOREIGN KEY(consent_id)
        REFERENCES consents(consent_id) ON DELETE CASCADE;
--

