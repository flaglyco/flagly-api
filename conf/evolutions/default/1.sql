-- !Ups

CREATE TABLE "accounts"(
    "id"         UUID PRIMARY KEY,
    "name"       TEXT NOT NULL,
    "email"      TEXT NOT NULL UNIQUE,
    "password"   TEXT NOT NULL,
    "salt"       TEXT NOT NULL,
    "created_at" TIMESTAMPTZ(3) NOT NULL,
    "updated_at" TIMESTAMPTZ(3) NOT NULL
);

CREATE TABLE "applications"(
    "id"         UUID PRIMARY KEY,
    "account_id" UUID NOT NULL REFERENCES "accounts"("id"),
    "name"       TEXT NOT NULL,
    "token"      TEXT NOT NULL UNIQUE,
    "created_at" TIMESTAMPTZ(3) NOT NULL,
    "updated_at" TIMESTAMPTZ(3) NOT NULL
);

CREATE TABLE "flags"(
    "id"             UUID PRIMARY KEY,
    "application_id" UUID NOT NULL REFERENCES "applications"("id"),
    "name"           TEXT NOT NULL,
    "description"    TEXT NOT NULL,
    "value"          BOOLEAN NOT NULL,
    "created_at"     TIMESTAMPTZ(3) NOT NULL,
    "updated_at"     TIMESTAMPTZ(3) NOT NULL,

    UNIQUE("application_id", "name")
);

CREATE TABLE "sessions"(
    "id"         UUID PRIMARY KEY,
    "account_id" UUID NOT NULL REFERENCES "accounts"("id"),
    "token"      TEXT NOT NULL,
    "created_at" TIMESTAMPTZ(3) NOT NULL,
    "updated_at" TIMESTAMPTZ(3) NOT NULL
);

-- !Downs

DROP TABLE "sessions";
DROP TABLE "flags";
DROP TABLE "applications";
DROP TABLE "accounts";
