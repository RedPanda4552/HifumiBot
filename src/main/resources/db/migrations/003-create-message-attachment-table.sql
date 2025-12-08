CREATE TABLE IF NOT EXISTS
  `message_attachment` (
    `discord_id` integer NOT NULL,
    `timestamp` integer NOT NULL,
    `fk_message` integer NOT NULL,
    `content_type` varchar(255) DEFAULT NULL,
    `proxy_url` varchar(2048) DEFAULT NULL,
    `filename` varchar(1024) DEFAULT NULL,
    PRIMARY KEY (`discord_id`),
    UNIQUE (`discord_id`),
    CONSTRAINT `fk_message_attachment_1` FOREIGN KEY (`fk_message`) REFERENCES `message` (`message_id`)
  );