ALTER TABLE external_accounts ADD COLUMN username VARCHAR(255);

UPDATE external_accounts ea
SET username = NULLIF(ltrim(btrim(u.username), '@'), '')
FROM users u
WHERE u.id = ea.user_id;

CREATE UNIQUE INDEX external_accounts_provider_username_key
    ON external_accounts (provider, lower(ltrim(btrim(username), '@')))
    WHERE username IS NOT NULL;
