CREATE TABLE IF NOT EXISTS
  `channel` (
    `discord_id` integer NOT NULL,
    `name` varchar(256) NOT NULL,
    PRIMARY KEY (`discord_id`),
    UNIQUE (`discord_id`)
  );