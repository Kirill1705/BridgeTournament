-- Before external accounts were introduced, this application's profiles were registered through Telegram.
ALTER TABLE users ADD COLUMN registration_provider VARCHAR(32) NOT NULL DEFAULT 'TELEGRAM';

UPDATE users u
SET registration_provider = account.provider
FROM (
    SELECT user_id, min(provider) AS provider
    FROM external_accounts
    GROUP BY user_id
    HAVING count(DISTINCT provider) = 1
) account
WHERE account.user_id = u.id;

-- Keep a duplicate username only on the profile whose external account owns it.
-- Unverified legacy profiles retain their UUIDs and historical references, without a stale username.
WITH duplicates AS (
    SELECT registration_provider, lower(ltrim(btrim(username), '@')) AS username
    FROM users
    WHERE username IS NOT NULL
    GROUP BY registration_provider, lower(ltrim(btrim(username), '@'))
    HAVING count(*) > 1
)
UPDATE users u
SET username = NULL, updated_at = CURRENT_TIMESTAMP
FROM duplicates d
WHERE u.registration_provider = d.registration_provider
    AND lower(ltrim(btrim(u.username), '@')) = d.username
    AND NOT EXISTS (
        SELECT 1 FROM external_accounts ea
        WHERE ea.user_id = u.id AND ea.provider = u.registration_provider
            AND lower(ltrim(btrim(ea.username), '@')) = d.username
    );

CREATE UNIQUE INDEX users_registration_provider_username_key
    ON users (registration_provider, lower(ltrim(btrim(username), '@')))
    WHERE username IS NOT NULL;
