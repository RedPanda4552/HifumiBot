CREATE TABLE IF NOT EXISTS
  `member_event` (
    `id` integer NOT NULL PRIMARY KEY AUTOINCREMENT,
    `timestamp` integer NOT NULL,
    `fk_user` integer NOT NULL,
    `action` varchar(45) NOT NULL,
    UNIQUE (`id`),
    CONSTRAINT `fk_join_event_user` FOREIGN KEY (`fk_user`) REFERENCES `user` (`discord_id`)
  );