ALTER TABLE transaction_logs
    DROP COLUMN entity_type,
    DROP COLUMN entity_id,
    DROP COLUMN entity_uuid,
    DROP COLUMN request_body;
