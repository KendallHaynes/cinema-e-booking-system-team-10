USE cinema_db_schema;

-- Copy values from the legacy snake_case columns before removing them.
UPDATE users
SET firstName = COALESCE(NULLIF(first_name, ''), firstName),
    lastName = COALESCE(NULLIF(last_name, ''), lastName),
    userType = COALESCE(NULLIF(user_type, ''), userType),
    customerStatus = COALESCE(customer_status, customerStatus);

ALTER TABLE users
    DROP COLUMN first_name,
    DROP COLUMN last_name,
    DROP COLUMN user_type,
    DROP COLUMN customer_status;
