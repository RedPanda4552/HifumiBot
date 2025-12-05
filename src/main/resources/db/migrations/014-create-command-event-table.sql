CREATE TABLE IF NOT EXISTS
  "command_event" (
    "discord_id" INTEGER NOT NULL UNIQUE,
    "command_fk" INTEGER NOT NULL,
    "user_fk" INTEGER NOT NULL,
    "channel_fk" INTEGER NOT NULL,
    "timestamp" INTEGER NOT NULL,
    "ninja" INTEGER NOT NULL,
    PRIMARY KEY ("discord_id"),
    FOREIGN KEY ("command_fk") REFERENCES "command" ("discord_id"),
    FOREIGN KEY ("user_fk") REFERENCES "user" ("discord_id"),
    FOREIGN KEY ("channel_fk") REFERENCES "channel" ("discord_id")
  );