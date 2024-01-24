ALTER TABLE fido_jwk
    ADD COLUMN enrollment_id text UNIQUE,
    ADD CONSTRAINT fido_jwk_enrollment_id_fkey FOREIGN KEY (enrollment_id) REFERENCES enrollments (enrollment_id) ON DELETE CASCADE;

ALTER TABLE fido_jwk_aud
    ADD COLUMN enrollment_id text;
