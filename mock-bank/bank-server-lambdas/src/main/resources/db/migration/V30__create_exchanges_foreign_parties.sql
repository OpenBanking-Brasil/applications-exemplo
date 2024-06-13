CREATE TABLE exchanges_operation_foreign_parties
(
    foreign_id                                    uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    operation_id                                uuid,
    relationship_code                           varchar(80),
    foreign_partie_name                         varchar(80),
    foreign_partie_country_code                 varchar(80),
    created_at                                  date,
    created_by                                  varchar(20),
    updated_at                                  date,
    updated_by                                  varchar(20),
    hibernate_status                            varchar,
    FOREIGN KEY (operation_id) REFERENCES exchanges_operation (operation_id)
);

CREATE TABLE exchanges_operation_foreign_parties_aud
(
    foreign_id                                    uuid,
    operation_id                                uuid,
    relationship_code                           varchar(80),
    foreign_partie_name                         varchar(80),
    foreign_partie_country_code                 varchar(80),
    created_at                                  date,
    created_by                                  varchar(20),
    updated_at                                  date,
    updated_by                                  varchar(20),
    hibernate_status                            varchar,
    rev                                         integer NOT NULL,
    revtype                                     smallint,
    PRIMARY KEY (foreign_id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo (rev)
);

CREATE TABLE exchanges_operation_event_foreign_parties
(
    foreign_id                                  uuid PRIMARY KEY DEFAULT uuid_generate_v4() NOT NULL,
    event_id                                    uuid,
    relationship_code                           varchar(80),
    foreign_partie_name                         varchar(80),
    foreign_partie_country_code                 varchar(80),
    created_at                                  date,
    created_by                                  varchar(20),
    updated_at                                  date,
    updated_by                                  varchar(20),
    hibernate_status                            varchar,
    FOREIGN KEY (event_id) REFERENCES exchanges_operation_event (event_id)
);

CREATE TABLE exchanges_operation_event_foreign_parties_aud
(
    foreign_id                                  uuid,
    event_id                                    uuid,
    relationship_code                           varchar(80),
    foreign_partie_name                         varchar(80),
    foreign_partie_country_code                 varchar(80),
    created_at                                  date,
    created_by                                  varchar(20),
    updated_at                                  date,
    updated_by                                  varchar(20),
    hibernate_status                            varchar,
    rev                                         integer NOT NULL,
    revtype                                     smallint,
    PRIMARY KEY (foreign_id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo (rev)
);