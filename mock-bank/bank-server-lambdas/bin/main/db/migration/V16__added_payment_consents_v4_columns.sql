ALTER TABLE payment_consents
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

ALTER TABLE payment_consents_aud
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

