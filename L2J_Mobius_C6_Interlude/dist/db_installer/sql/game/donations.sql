CREATE TABLE IF NOT EXISTS donations (
  `purchase_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `mp_payment_id` BIGINT UNSIGNED DEFAULT 0,
  `mp_preference_id` VARCHAR(52) DEFAULT NULL,
  `payment_method` VARCHAR(4) DEFAULT NULL,
  `player_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `email` VARCHAR(44) NOT NULL DEFAULT '',
  `product_id` INT UNSIGNED NOT NULL DEFAULT 0,
  `quantity` INT UNSIGNED NOT NULL DEFAULT 0,
  `unit_price` INT UNSIGNED NOT NULL DEFAULT 0,
  `date` BIGINT UNSIGNED DEFAULT 0,
  `status` VARCHAR(10) NOT NULL DEFAULT '',
  PRIMARY KEY (`purchase_id`,`mp_payment_id`)
);