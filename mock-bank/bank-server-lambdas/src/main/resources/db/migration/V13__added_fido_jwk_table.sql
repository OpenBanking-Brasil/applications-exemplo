CREATE TABLE fido_jwk
(
    reference_id SERIAL PRIMARY KEY NOT NULL,
    kid          text UNIQUE        NOT NULL,
    n            text,
    e            text
);

CREATE TABLE fido_jwk_aud
(
    reference_id SERIAL  NOT NULL,
    kid          text    NOT NULL,
    n            text,
    e            text,
    rev          integer NOT NULL,
    revtype      smallint,
    PRIMARY KEY (reference_id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo (rev)
);