ALTER TABLE payment_consents
    ADD COLUMN reject_reason_code varchar;
ALTER TABLE payment_consents
    ADD COLUMN reject_reason_detail varchar;
ALTER TABLE payment_consents_aud
    ADD COLUMN reject_reason_code varchar;
ALTER TABLE payment_consents_aud
    ADD COLUMN reject_reason_detail varchar;