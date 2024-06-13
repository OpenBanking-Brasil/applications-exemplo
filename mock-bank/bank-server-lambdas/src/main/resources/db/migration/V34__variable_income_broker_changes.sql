ALTER TABLE variable_incomes_broker_notes
    DROP CONSTRAINT variable_incomes_broker_notes_investment_id_fkey,
    DROP COLUMN investment_id;

ALTER TABLE variable_incomes_broker_notes_aud
    DROP COLUMN investment_id;

