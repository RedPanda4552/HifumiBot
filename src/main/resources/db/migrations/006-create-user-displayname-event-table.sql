CREATE TABLE IF NOT EXISTS
  "user_displayname_event" (
    "id" INTEGER NOT NULL UNIQUE,
    "fk_user" INTEGER NOT NULL,
    "old_displayname" TEXT NOT NULL,
    "new_displayname" TEXT NOT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT)
  );