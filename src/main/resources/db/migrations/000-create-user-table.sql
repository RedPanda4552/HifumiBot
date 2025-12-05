CREATE TABLE IF NOT EXISTS
  `user` (
    `discord_id` integer NOT NULL,
    `created_datetime` integer NOT NULL,
    `username` varchar(45) NOT NULL,
    PRIMARY KEY (`discord_id`),
    UNIQUE (`discord_id`)
  );