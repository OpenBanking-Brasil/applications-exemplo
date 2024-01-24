ALTER TABLE consents_extension
    ADD COLUMN previous_expiration_date_time timestamp,
    ADD COLUMN x_fapi_customer_ip_address varchar,
    ADD COLUMN x_customer_user_agent varchar;

ALTER TABLE consents_extension_aud
    ADD COLUMN previous_expiration_date_time timestamp,
    ADD COLUMN x_fapi_customer_ip_address varchar,
    ADD COLUMN x_customer_user_agent varchar;