CREATE TABLE IF NOT EXISTS
  `filter_event` (
    `id` integer NOT NULL PRIMARY KEY AUTOINCREMENT,
    `fk_user` integer NOT NULL,
    `fk_message` integer NOT NULL,
    `timestamp` integer NOT NULL,
    `filter_name` varchar(255) NOT NULL,
    `filter_regex_name` varchar(255) NOT NULL,
    `informational` integer DEFAULT NULL,
    UNIQUE (`id`),
    CONSTRAINT `fk_filter_event_1` FOREIGN KEY (`fk_user`) REFERENCES `user` (`discord_id`),
    CONSTRAINT `fk_filter_event_2` FOREIGN KEY (`fk_message`) REFERENCES `message` (`message_id`)
  );