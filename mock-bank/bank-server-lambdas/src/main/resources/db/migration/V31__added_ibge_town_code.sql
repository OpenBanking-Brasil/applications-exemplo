ALTER TABLE pix_payments
ADD COLUMN ibge_town_code VARCHAR(7);

ALTER TABLE pix_payments_aud
ADD COLUMN ibge_town_code VARCHAR(7);

ALTER TABLE pix_payments
    DROP COLUMN document_identification,
    DROP COLUMN document_rel;

ALTER TABLE pix_payments_aud
    DROP COLUMN document_identification,
    DROP COLUMN document_rel;

ALTER TABLE pix_payments
    ADD COLUMN document_identification varchar(14),
    ADD COLUMN document_rel            varchar(4);

ALTER TABLE pix_payments_aud
    ADD COLUMN document_identification varchar(14),
    ADD COLUMN document_rel            varchar(4);