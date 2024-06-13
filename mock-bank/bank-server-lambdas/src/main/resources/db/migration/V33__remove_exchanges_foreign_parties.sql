DROP TABLE exchanges_operation_foreign_parties;

DROP TABLE exchanges_operation_foreign_parties_aud;

DROP TABLE exchanges_operation_event_foreign_parties;

DROP TABLE exchanges_operation_event_foreign_parties_aud;


ALTER TABLE exchanges_operation_event
    ADD COLUMN relationship_code                           varchar(80),
    ADD COLUMN foreign_partie_name                         varchar(80),
    ADD COLUMN foreign_partie_country_code                 varchar(80);

ALTER TABLE exchanges_operation_event_aud
    ADD COLUMN relationship_code                           varchar(80),
    ADD COLUMN foreign_partie_name                         varchar(80),
    ADD COLUMN foreign_partie_country_code                 varchar(80);