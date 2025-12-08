CREATE TABLE IF NOT EXISTS
  `message_embed` (
    `id` integer NOT NULL PRIMARY KEY AUTOINCREMENT,
    `fk_message` integer NOT NULL,
    `timestamp` integer NOT NULL,
    `title` varchar(2048) DEFAULT NULL,
    `description` varchar(4096) DEFAULT NULL,
    `author` varchar(1024) DEFAULT NULL,
    `footer` varchar(2048) DEFAULT NULL,
    UNIQUE (`id`),
    CONSTRAINT `fk_message_embed_1` FOREIGN KEY (`fk_message`) REFERENCES `message` (`message_id`)
  );