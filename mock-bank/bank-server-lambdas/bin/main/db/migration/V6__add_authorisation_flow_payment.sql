ALTER TABLE pix_payments
    ADD COLUMN authorisation_flow varchar;
ALTER TABLE pix_payments_aud
    ADD COLUMN authorisation_flow varchar;