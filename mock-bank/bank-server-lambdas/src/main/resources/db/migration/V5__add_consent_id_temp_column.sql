ALTER TABLE consents ADD COLUMN new_consent_id TEXT UNIQUE;
ALTER TABLE consents_aud ADD COLUMN new_consent_id TEXT;
ALTER TABLE consent_permissions ADD COLUMN new_consent_id TEXT;
ALTER TABLE consent_permissions_aud ADD COLUMN new_consent_id TEXT;
ALTER TABLE consent_account_ids ADD COLUMN new_consent_id TEXT;
ALTER TABLE consent_account_ids_aud ADD COLUMN new_consent_id TEXT;