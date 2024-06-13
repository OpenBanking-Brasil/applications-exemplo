CREATE TABLE client_webhook_uri
(
    reference_id                                SERIAL PRIMARY KEY NOT NULL,
    client_id                                   varchar NOT NULL,
    webhook_uri                                 varchar,
    created_at                                  date,
    created_by                                  varchar(20),
    updated_at                                  date,
    updated_by                                  varchar(20),
    hibernate_status                            varchar
);

CREATE TABLE client_webhook_uri_aud
(
    reference_id                                integer,
    client_id                                   varchar,
    webhook_uri                                 varchar,
    created_at                                  date,
    created_by                                  varchar(20),
    updated_at                                  date,
    updated_by                                  varchar(20),
    hibernate_status                            varchar,
    rev                                         integer NOT NULL,
    revtype                                     smallint,
    PRIMARY KEY (reference_id, rev)
);