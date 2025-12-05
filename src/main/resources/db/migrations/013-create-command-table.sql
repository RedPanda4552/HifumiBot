CREATE TABLE IF NOT EXISTS
  "command" (
    "discord_id" INTEGER NOT NULL UNIQUE,
    "type" TEXT NOT NULL,
    "name" TEXT NOT NULL,
    "subgroup" TEXT,
    "subcmd" TEXT,
    PRIMARY KEY ("discord_id")
  );