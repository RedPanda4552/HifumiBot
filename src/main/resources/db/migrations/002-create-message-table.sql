CREATE TABLE IF NOT EXISTS
  `message` (
    `message_id` integer NOT NULL,
    `fk_channel` integer NOT NULL,
    `jump_link` varchar(255) DEFAULT NULL,
    `fk_reply_to_message` integer DEFAULT NULL,
    `timestamp` integer DEFAULT NULL,
    `fk_user` integer DEFAULT NULL,
    PRIMARY KEY (`message_id`),
    UNIQUE (`message_id`),
    CONSTRAINT `fk_message_1` FOREIGN KEY (`fk_reply_to_message`) REFERENCES `message` (`message_id`),
    CONSTRAINT `fk_message_2` FOREIGN KEY (`fk_channel`) REFERENCES `channel` (`discord_id`),
    CONSTRAINT `fk_message_3` FOREIGN KEY (`fk_reply_to_message`) REFERENCES `message` (`message_id`),
    CONSTRAINT `fk_message_4` FOREIGN KEY (`fk_user`) REFERENCES `user` (`discord_id`)
  );