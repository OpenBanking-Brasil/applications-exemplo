CREATE TABLE consents_extension
(
    reference_id SERIAL NOT NULL PRIMARY KEY,
    consent_id                     text NOT NULL,
    expiration_date_time           timestamp,
    logged_document_identification varchar,
    logged_document_rel            varchar,
    request_date_time              timestamp,

    FOREIGN KEY (consent_id) REFERENCES consents (consent_id) ON DELETE CASCADE
);


CREATE TABLE consents_extension_aud
(
    reference_id SERIAL NOT NULL,
    consent_id                     text,
    expiration_date_time           timestamp,
    logged_document_identification varchar,
    logged_document_rel            varchar,
    request_date_time              timestamp,
    rev                            integer NOT NULL,
    revtype      smallint,
    PRIMARY KEY (reference_id, rev)
);