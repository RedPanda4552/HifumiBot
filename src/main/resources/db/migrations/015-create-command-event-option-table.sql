CREATE TABLE IF NOT EXISTS
  "command_event_option" (
    "id" INTEGER NOT NULL UNIQUE,
    "command_event_fk" INTEGER NOT NULL,
    "name" TEXT NOT NULL,
    "value_str" TEXT NOT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT)
  );