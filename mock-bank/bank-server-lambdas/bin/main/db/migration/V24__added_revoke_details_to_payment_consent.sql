ALTER TABLE payment_consents
    ADD COLUMN revoke_reason_code varchar,
    ADD COLUMN revoke_reason_detail varchar;

ALTER TABLE payment_consents_aud
    ADD COLUMN revoke_reason_code varchar,
    ADD COLUMN revoke_reason_detail varchar;
