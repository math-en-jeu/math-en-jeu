CREATE TABLE `regles_case_special` (
  `id` INTEGER UNSIGNED NOT NULL AUTO_INCREMENT,
  `type` INTEGER UNSIGNED NOT NULL,
  `priorite` INTEGER UNSIGNED NOT NULL,
  PRIMARY KEY (`id`)
)
ENGINE = InnoDB;