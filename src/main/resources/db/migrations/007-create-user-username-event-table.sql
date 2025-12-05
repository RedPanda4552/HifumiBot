CREATE TABLE IF NOT EXISTS
  "user_username_event" (
    "id" INTEGER NOT NULL UNIQUE,
    "fk_user" INTEGER NOT NULL,
    "old_username" TEXT NOT NULL,
    "new_username" TEXT NOT NULL,
    PRIMARY KEY ("id" AUTOINCREMENT)
  );