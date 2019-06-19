-- !Ups

CREATE TABLE "flags"(
  "id" UUID PRIMARY KEY,
  "name" TEXT NOT NULL UNIQUE,
  "description" TEXT NOT NULL,
  "value" BOOLEAN NOT NULL,
  "created_at" TIMESTAMPTZ(3) NOT NULL,
  "updated_at" TIMESTAMPTZ(3) NOT NULL
);

-- !Downs

DROP TABLE "flags";
