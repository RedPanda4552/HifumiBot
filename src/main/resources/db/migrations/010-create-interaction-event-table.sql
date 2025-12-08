CREATE TABLE IF NOT EXISTS
  "interaction_event" (
    "id" INTEGER NOT NULL UNIQUE,
    "timestamp" INTEGER NOT NULL,
    "user_fk" INTEGER NOT NULL,
    PRIMARY KEY ("id"),
    CONSTRAINT "fk_interaction_event_user" FOREIGN KEY ("user_fk") REFERENCES "user" ("discord_id")
  );