ALTER TABLE enrollments ADD COLUMN additional_information varchar;
ALTER TABLE enrollments_aud
    ADD COLUMN additional_information varchar,
    DROP CONSTRAINT enrollments_aud_pkey,
    DROP CONSTRAINT enrollments_aud_enrollment_id_key,
    ADD PRIMARY KEY(reference_id, rev);
