CREATE TABLE IF NOT EXISTS
  "automod_event" (
    "id" INTEGER NOT NULL UNIQUE,
    "fk_user" INTEGER NOT NULL,
    "fk_message" INTEGER,
    "fk_channel" INTEGER,
    "alert_message_id" INTEGER,
    "rule_id" INTEGER NOT NULL,
    "timestamp" INTEGER NOT NULL,
    "trigger" TEXT NOT NULL,
    "content" TEXT,
    "matched_content" TEXT,
    "matched_keyword" TEXT,
    "response_type" TEXT NOT NULL,
    FOREIGN KEY ("fk_user") REFERENCES "user" ("discord_id"),
    FOREIGN KEY ("fk_message") REFERENCES "message" ("message_id"),
    FOREIGN KEY ("fk_channel") REFERENCES "channel" ("discord_id"),
    PRIMARY KEY ("id" AUTOINCREMENT)
  );