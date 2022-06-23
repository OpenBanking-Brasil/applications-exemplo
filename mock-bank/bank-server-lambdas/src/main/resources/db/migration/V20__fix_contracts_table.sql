-- some fields should be dates
ALTER TABLE contracts DROP COLUMN "contract_date";
ALTER TABLE contracts ADD COLUMN "contract_date" date;
ALTER TABLE contracts DROP COLUMN "disbursement_date";
ALTER TABLE contracts ADD COLUMN "disbursement_date" date;
ALTER TABLE contracts DROP COLUMN "settlement_date";
ALTER TABLE contracts ADD COLUMN "settlement_date" date;
ALTER TABLE contracts DROP COLUMN "due_date";
ALTER TABLE contracts ADD COLUMN "due_date" date;
ALTER TABLE contracts DROP COLUMN "first_instalment_due_date";
ALTER TABLE contracts ADD COLUMN "first_instalment_due_date" date;

ALTER TABLE contracts ADD COLUMN "contract_number" varchar;

ALTER TABLE contracts_aud DROP COLUMN "contract_date";
ALTER TABLE contracts_aud ADD COLUMN "contract_date" date;
ALTER TABLE contracts_aud DROP COLUMN "disbursement_date";
ALTER TABLE contracts_aud ADD COLUMN "disbursement_date" date;
ALTER TABLE contracts_aud DROP COLUMN "settlement_date";
ALTER TABLE contracts_aud ADD COLUMN "settlement_date" date;
ALTER TABLE contracts_aud DROP COLUMN "due_date";
ALTER TABLE contracts_aud ADD COLUMN "due_date" date;
ALTER TABLE contracts_aud DROP COLUMN "first_instalment_due_date";
ALTER TABLE contracts_aud ADD COLUMN "first_instalment_due_date" date;

ALTER TABLE contracts_aud ADD COLUMN "contract_number" varchar;

ALTER TABLE "accounts" DROP COLUMN "overdraft_contracted_limit";
ALTER TABLE "accounts" ADD COLUMN "overdraft_contracted_limit" double precision;
ALTER TABLE "accounts" DROP COLUMN "overdraft_used_limit";
ALTER TABLE "accounts" ADD COLUMN "overdraft_used_limit" double precision;
ALTER TABLE "accounts" DROP COLUMN "unarranged_overdraft_amount";
ALTER TABLE "accounts" ADD COLUMN "unarranged_overdraft_amount" double precision;

ALTER TABLE "accounts_aud" DROP COLUMN "overdraft_contracted_limit";
ALTER TABLE "accounts_aud" ADD COLUMN "overdraft_contracted_limit" double precision;
ALTER TABLE "accounts_aud" DROP COLUMN "overdraft_used_limit";
ALTER TABLE "accounts_aud" ADD COLUMN "overdraft_used_limit" double precision;
ALTER TABLE "accounts_aud" DROP COLUMN "unarranged_overdraft_amount";
ALTER TABLE "accounts_aud" ADD COLUMN "unarranged_overdraft_amount" double precision;

DROP TABLE personal_financial_relations_accounts;
DROP TABLE personal_financial_relations_accounts_aud;