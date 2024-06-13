ALTER TABLE account_transactions
    DROP COLUMN transaction_date;

ALTER TABLE account_transactions_aud
    DROP COLUMN transaction_date;

ALTER TABLE account_transactions
ADD COLUMN transaction_date_time timestamp;

ALTER TABLE account_transactions_aud
    ADD COLUMN transaction_date_time timestamp;
