ALTER TABLE credit_card_accounts_transaction ADD COLUMN "credit_card_account_id" uuid;
ALTER TABLE credit_card_accounts_transaction_aud ADD COLUMN "credit_card_account_id" uuid;

ALTER TABLE credit_card_accounts_transaction ADD FOREIGN KEY(credit_card_account_id) REFERENCES credit_card_accounts(credit_card_account_id);