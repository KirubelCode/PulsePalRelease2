-- create the database (if needed) and switch to it
CREATE DATABASE IF NOT EXISTS `pulsepalDB`
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;
USE `pulsepalDB`;

-- 1) users
CREATE TABLE `users` (
  `id`          INT            NOT NULL AUTO_INCREMENT,
  `fullName`    VARCHAR(255),
  `email`       VARCHAR(255)   UNIQUE,
  `password`    VARCHAR(255),
  `age`         INT,
  `gender`      VARCHAR(10),
  `height`      FLOAT,
  `weight`      FLOAT,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB;

-- 2) exercise_sessions
CREATE TABLE `exercise_sessions` (
  `id`               INT      NOT NULL AUTO_INCREMENT,
  `user_id`          INT,
  `date`             DATETIME,
  `duration_minutes` FLOAT,
  `total_steps`      INT,
  `total_distance`   FLOAT,
  `avg_pace`         FLOAT,
  `avg_heart_rate`   FLOAT,
  `total_calories`   FLOAT,
  PRIMARY KEY (`id`),
  INDEX (`user_id`),
  FOREIGN KEY (`user_id`)
    REFERENCES `users`(`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- 3) session_data_points
CREATE TABLE `session_data_points` (
  `id`         INT      NOT NULL AUTO_INCREMENT,
  `session_id` INT,
  `timestamp`  DATETIME,
  `heart_rate` FLOAT,
  `steps`      INT,
  `distance`   FLOAT,
  `pace`       FLOAT,
  `calories`   FLOAT    DEFAULT 0,
  PRIMARY KEY (`id`),
  INDEX (`session_id`),
  FOREIGN KEY (`session_id`)
    REFERENCES `exercise_sessions`(`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB;

-- 4) session_goals
CREATE TABLE `session_goals` (
  `id`           INT       NOT NULL AUTO_INCREMENT,
  `session_id`   INT       NOT NULL,
  `metric`       ENUM('heart_rate','pace','distance','steps','calories') NOT NULL,
  `operator`     ENUM('>=','<=')                                         NOT NULL,
  `target_value` FLOAT     NOT NULL,
  `duration_sec` INT       NULL,       -- only for heart_rate goals
  `created_at`   DATETIME  DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  INDEX (`session_id`),
  FOREIGN KEY (`session_id`)
    REFERENCES `exercise_sessions`(`id`)
    ON DELETE CASCADE
) ENGINE=InnoDB;
