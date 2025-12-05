CREATE TABLE IF NOT EXISTS
  `message_event` (
    `id` integer NOT NULL PRIMARY KEY AUTOINCREMENT,
    `fk_user` integer NOT NULL,
    `fk_message` integer NOT NULL,
    `timestamp` integer NOT NULL,
    `action` varchar(45) NOT NULL,
    `content` varchar(4000) DEFAULT NULL,
    `fk_reply_to_message` integer DEFAULT NULL,
    `reply_is_ping` integer DEFAULT NULL,
    UNIQUE (`id`)
  );