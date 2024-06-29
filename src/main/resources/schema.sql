CREATE TABLE IF NOT EXISTS "application" (
    id integer NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "uid" VARCHAR(255) NOT NULL UNIQUE,
    "client_id" VARCHAR(255) NOT NULL UNIQUE,
    "secret" VARCHAR(255) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS "application_idx_uid" ON "application" USING HASH (uid);

CREATE TABLE IF NOT EXISTS "asset" (
    id integer NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    "description" VARCHAR(255),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "application_asset" (
    application_id integer REFERENCES "application" (id) NOT NULL,
    asset_id integer REFERENCES "asset" (id) NOT NULL,
    PRIMARY KEY (application_id, asset_id)
);

CREATE TABLE IF NOT EXISTS "iam_role" (
    id integer NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "application_iam_role" (
    application_id integer REFERENCES "application" (id) NOT NULL,
    role_id integer REFERENCES "iam_role" (id) NOT NULL,
    PRIMARY KEY (application_id, role_id)
);

CREATE TABLE IF NOT EXISTS "tag" (
    id integer NOT NULL,
    "name" VARCHAR(255) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS "asset_tag" (
    asset_id integer REFERENCES "asset" (id) NOT NULL,
    tag_id integer REFERENCES "tag" (id) NOT NULL,
    PRIMARY KEY (asset_id, tag_id)
);