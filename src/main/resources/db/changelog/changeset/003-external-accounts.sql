ALTER TABLE users ALTER COLUMN username DROP NOT NULL;
ALTER TABLE users ALTER COLUMN username TYPE VARCHAR(255);

CREATE TABLE external_accounts (
    provider VARCHAR(32) NOT NULL,
    external_id VARCHAR(255) NOT NULL,
    user_id UUID NOT NULL REFERENCES users(id),
    PRIMARY KEY (provider, external_id)
);

CREATE INDEX external_accounts_user_id_idx ON external_accounts(user_id);
