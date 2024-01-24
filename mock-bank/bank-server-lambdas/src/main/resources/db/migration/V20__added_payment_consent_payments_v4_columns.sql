ALTER TABLE payment_consent_payments
    ADD COLUMN schedule_single_date                   timestamp,
    ADD COLUMN schedule_daily_start_date              timestamp,
    ADD COLUMN schedule_daily_quantity                integer,
    ADD COLUMN schedule_weekly_day_of_week            varchar,
    ADD COLUMN schedule_weekly_start_date             timestamp,
    ADD COLUMN schedule_weekly_quantity               integer,
    ADD COLUMN schedule_monthly_start_date            timestamp,
    ADD COLUMN schedule_monthly_day_of_month          integer,
    ADD COLUMN schedule_monthly_quantity              integer,
    ADD COLUMN schedule_custom_dates                  timestamp[],
    ADD COLUMN schedule_custom_additional_information text;

ALTER TABLE payment_consent_payments_aud
    ADD COLUMN schedule_single_date                   timestamp,
    ADD COLUMN schedule_daily_start_date              timestamp,
    ADD COLUMN schedule_daily_quantity                integer,
    ADD COLUMN schedule_weekly_day_of_week            varchar,
    ADD COLUMN schedule_weekly_start_date             timestamp,
    ADD COLUMN schedule_weekly_quantity               integer,
    ADD COLUMN schedule_monthly_start_date            timestamp,
    ADD COLUMN schedule_monthly_day_of_month          integer,
    ADD COLUMN schedule_monthly_quantity              integer,
    ADD COLUMN schedule_custom_dates                  timestamp[],
    ADD COLUMN schedule_custom_additional_information text;

ALTER TABLE payment_consents
    DROP COLUMN schedule_single_date,
    DROP COLUMN schedule_daily_start_date,
    DROP COLUMN schedule_daily_quantity,
    DROP COLUMN schedule_weekly_day_of_week,
    DROP COLUMN schedule_weekly_start_date,
    DROP COLUMN schedule_weekly_quantity,
    DROP COLUMN schedule_monthly_start_date,
    DROP COLUMN schedule_monthly_day_of_month,
    DROP COLUMN schedule_monthly_quantity,
    DROP COLUMN schedule_custom_dates,
    DROP COLUMN schedule_custom_additional_information;

ALTER TABLE payment_consents_aud
    DROP COLUMN schedule_single_date,
    DROP COLUMN schedule_daily_start_date,
    DROP COLUMN schedule_daily_quantity,
    DROP COLUMN schedule_weekly_day_of_week,
    DROP COLUMN schedule_weekly_start_date,
    DROP COLUMN schedule_weekly_quantity,
    DROP COLUMN schedule_monthly_start_date,
    DROP COLUMN schedule_monthly_day_of_month,
    DROP COLUMN schedule_monthly_quantity,
    DROP COLUMN schedule_custom_dates,
    DROP COLUMN schedule_custom_additional_information;

