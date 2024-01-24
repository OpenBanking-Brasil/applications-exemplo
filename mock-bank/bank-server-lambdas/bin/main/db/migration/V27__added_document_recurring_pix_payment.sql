ALTER TABLE pix_payments
    ADD COLUMN document_identification varchar(11),
    ADD COLUMN document_rel            varchar(3);

ALTER TABLE pix_payments_aud
    ADD COLUMN document_identification varchar(11),
    ADD COLUMN document_rel            varchar(3);