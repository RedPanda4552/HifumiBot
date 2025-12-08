CREATE TABLE IF NOT EXISTS
  "warez_event" (
    "id" integer NOT NULL,
    "timestamp" integer NOT NULL,
    "fk_user" integer NOT NULL,
    "action" varchar(45) NOT NULL,
    "fk_message" INTEGER DEFAULT NULL,
    UNIQUE ("id"),
    PRIMARY KEY ("id" AUTOINCREMENT),
    CONSTRAINT "fk_warez_event_user" FOREIGN KEY ("fk_user") REFERENCES "user" ("discord_id")
  );