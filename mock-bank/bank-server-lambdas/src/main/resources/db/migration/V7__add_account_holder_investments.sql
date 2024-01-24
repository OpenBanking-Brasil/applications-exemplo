ALTER TABLE credit_fixed_incomes
    ADD COLUMN account_holder_id uuid,
    ADD COLUMN status varchar;
ALTER TABLE credit_fixed_incomes
    ADD FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE;
ALTER TABLE credit_fixed_incomes_aud
    ADD COLUMN account_holder_id uuid,
    ADD COLUMN status varchar;

ALTER TABLE funds
    ADD COLUMN account_holder_id uuid,
    ADD COLUMN status varchar;
ALTER TABLE funds
    ADD FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE;
ALTER TABLE funds_aud
    ADD COLUMN account_holder_id uuid,
    ADD COLUMN status varchar;

ALTER TABLE treasure_titles
    ADD COLUMN account_holder_id uuid,
    ADD COLUMN status varchar;
ALTER TABLE treasure_titles
    ADD FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE;

ALTER TABLE treasure_titles_aud
    ADD COLUMN account_holder_id uuid,
    ADD COLUMN status varchar;

ALTER TABLE variable_incomes
    ADD COLUMN account_holder_id uuid,
    ADD COLUMN status varchar;

ALTER TABLE variable_incomes
    ADD FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE;

ALTER TABLE variable_incomes_aud
    ADD COLUMN account_holder_id uuid,
    ADD COLUMN status varchar;

ALTER TABLE bank_fixed_incomes
    ADD COLUMN account_holder_id uuid,
    ADD COLUMN status varchar;
ALTER TABLE bank_fixed_incomes
    ADD FOREIGN KEY (account_holder_id) REFERENCES account_holders (account_holder_id) ON DELETE CASCADE;

ALTER TABLE bank_fixed_incomes_aud
    ADD COLUMN account_holder_id uuid,
    ADD COLUMN status varchar;

CREATE TABLE consent_investment
(
    reference_id SERIAL PRIMARY KEY NOT NULL,
    consent_id   varchar,
    bank_fixed_income_id  uuid,
    credit_fixed_income_id  uuid,
    variable_income_id  uuid,
    treasure_title_id  uuid,
    fund_id  uuid,
    created_at timestamp,
    updated_at timestamp,
    hibernate_status varchar,
    created_by varchar,
    updated_by varchar,
    FOREIGN KEY (consent_id) REFERENCES consents (consent_id) ON DELETE CASCADE,
    FOREIGN KEY (bank_fixed_income_id) REFERENCES bank_fixed_incomes (investment_id) ON DELETE CASCADE,
    FOREIGN KEY (credit_fixed_income_id) REFERENCES credit_fixed_incomes (investment_id) ON DELETE CASCADE,
    FOREIGN KEY (variable_income_id) REFERENCES variable_incomes (investment_id) ON DELETE CASCADE,
    FOREIGN KEY (treasure_title_id) REFERENCES treasure_titles (investment_id) ON DELETE CASCADE,
    FOREIGN KEY (fund_id) REFERENCES funds (investment_id) ON DELETE CASCADE
);

CREATE TABLE consent_investment_aud
(
    reference_id integer NOT NULL,
    consent_id   varchar,
    bank_fixed_income_id  uuid,
    credit_fixed_income_id  uuid,
    variable_income_id  uuid,
    treasure_title_id  uuid,
    fund_id  uuid,
    created_at timestamp,
    updated_at timestamp,
    hibernate_status varchar,
    created_by varchar,
    updated_by varchar,
    rev integer NOT NULL,
    revtype smallint,
    PRIMARY KEY (reference_id, rev),
    FOREIGN KEY (rev) REFERENCES revinfo (rev)
);
