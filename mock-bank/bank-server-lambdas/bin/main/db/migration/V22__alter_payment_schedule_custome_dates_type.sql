ALTER TABLE payment_consent_payments
    DROP COLUMN schedule_custom_dates;

ALTER TABLE payment_consent_payments_aud
    DROP COLUMN schedule_custom_dates;

ALTER TABLE payment_consent_payments
    ADD COLUMN schedule_custom_dates date[];

ALTER TABLE payment_consent_payments_aud
    ADD COLUMN schedule_custom_dates date[];

