USE `profile`;

DROP TABLE IF EXISTS `profile`;
CREATE TABLE `profile` (
  `id`       INT(11) NOT NULL,
  `_name`    VARCHAR(64),
  `username` VARCHAR(16),
  `user_id`  INT(11),
  `gender`   CHAR(1),
  `birthday` CHAR(10),
  PRIMARY KEY (`id`)
);

INSERT INTO `profile` (`id`, `_name`, `username`, `user_id`, `gender`, `birthday`) VALUES (1, '阿达敏', 'admin', 1, 'M', '2018-1-10');