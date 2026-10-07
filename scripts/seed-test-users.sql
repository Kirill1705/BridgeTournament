BEGIN;

SELECT pg_advisory_xact_lock(hashtextextended('usernames:TELEGRAM', 0));

INSERT INTO users (id, created_at, updated_at, name, surname, username, sport_category, registration_provider)
SELECT gen_random_uuid(), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP,
       'Тестовый', 'Игрок ' || n, 'test_user_' || n, 5.0, 'TELEGRAM'
FROM generate_series(1, 10) AS n
WHERE NOT EXISTS (
    SELECT 1 FROM external_accounts ea
    WHERE ea.provider = 'TELEGRAM' AND ea.external_id = 'seed:test_user_' || n
)
ON CONFLICT DO NOTHING;

-- Synthetic IDs cannot collide with real, numeric Telegram user IDs.
INSERT INTO external_accounts (provider, external_id, user_id, username)
SELECT 'TELEGRAM', 'seed:test_user_' || n, u.id, 'test_user_' || n
FROM generate_series(1, 10) AS n
JOIN users u ON u.registration_provider = 'TELEGRAM'
    AND lower(ltrim(btrim(u.username), '@')) = 'test_user_' || n
WHERE u.removed = false
    AND NOT EXISTS (
        SELECT 1 FROM external_accounts ea
        WHERE ea.provider = 'TELEGRAM'
            AND (ea.user_id = u.id OR lower(ltrim(btrim(ea.username), '@')) = 'test_user_' || n)
    )
ON CONFLICT DO NOTHING;

COMMIT;
