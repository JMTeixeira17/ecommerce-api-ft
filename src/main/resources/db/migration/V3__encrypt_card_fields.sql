ALTER TABLE tokenized_cards
    DROP CONSTRAINT IF EXISTS chk_expiration_year;

ALTER TABLE tokenized_cards
    DROP CONSTRAINT IF EXISTS chk_expiration_month;

ALTER TABLE tokenized_cards
    ALTER COLUMN expiration_month TYPE VARCHAR(255),
    ALTER COLUMN expiration_year TYPE VARCHAR(255);