CREATE TABLE enrollments_fido_registration_options
(
    reference_id                     SERIAL PRIMARY KEY NOT NULL,
    enrollment_id                    text UNIQUE,
    platform                         varchar,
    rp                               varchar,
    challenge                        varchar,
    timeout                          integer,
    attestation                      varchar,
    extensions                       varchar,
    created_at                      date,
    created_by                      varchar(20),
    updated_at                      date,
    updated_by                      varchar(20),
    hibernate_status                varchar,
    FOREIGN KEY (enrollment_id) REFERENCES enrollments (enrollment_id) ON DELETE CASCADE
);

CREATE TABLE enrollments_fido_registration_options_aud
(
    reference_id                     SERIAL NOT NULL,
    enrollment_id                    text,
    platform                         varchar,
    rp                               varchar,
    challenge                        varchar,
    timeout                          integer,
    attestation                      varchar,
    extensions                       varchar,
    created_at                      date,
    created_by                      varchar(20),
    updated_at                      date,
    updated_by                      varchar(20),
    hibernate_status                varchar,
    rev                             integer NOT NULL,
    revtype                         smallint,
    PRIMARY KEY (reference_id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo (rev)
);