-- simple change to add a human-readable username to the accounts table
ALTER TABLE account_holders ADD COLUMN "account_holder_name" varchar;
ALTER TABLE account_holders_aud ADD COLUMN "account_holder_name" varchar;