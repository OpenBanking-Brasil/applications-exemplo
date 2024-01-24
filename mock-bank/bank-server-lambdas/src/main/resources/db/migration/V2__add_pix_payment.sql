ALTER TABLE pix_payments
ADD COLUMN cancellation_reason varchar;
ALTER TABLE pix_payments
    ADD COLUMN cancellation_from varchar;
ALTER TABLE pix_payments_aud
    ADD COLUMN cancellation_reason varchar;
ALTER TABLE pix_payments_aud
    ADD COLUMN cancellation_from varchar;