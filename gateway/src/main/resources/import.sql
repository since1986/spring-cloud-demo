USE `gateway`;

DROP TABLE IF EXISTS `users`;
DROP TABLE IF EXISTS `authorities`;
DROP TABLE IF EXISTS `group_members`;
DROP TABLE IF EXISTS `group_authorities`;
DROP TABLE IF EXISTS `groups`;

# 建表
CREATE TABLE `users` (
  `id`       BIGINT       NOT NULL,
  `username` VARCHAR(16)  NOT NULL,
  `password` VARCHAR(255) NOT NULL,
  `enabled`  BIT(1)       NOT NULL DEFAULT TRUE,
  PRIMARY KEY (`id`),
  UNIQUE KEY `username_unique` (`username`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
CREATE TABLE `authorities` (
  `id`        BIGINT      NOT NULL,
  `username`  VARCHAR(16) NOT NULL,
  `authority` VARCHAR(64) NOT NULL,
  `user`      BIGINT      NOT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

CREATE TABLE `groups` (
  `id`         BIGINT      NOT NULL,
  `group_name` VARCHAR(64) NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `group_name_unique` (`group_name`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
CREATE TABLE `group_members` (
  `id`       BIGINT      NOT NULL,
  `username` VARCHAR(64) NOT NULL,
  `group_id` BIGINT DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `fk_group_members_group_idx` (`group_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;
CREATE TABLE `group_authorities` (
  `group_id`  BIGINT      NOT NULL,
  `authority` VARCHAR(64) NOT NULL,
  KEY `fk_group_authorities_group_idx` (`group_id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;

# 装入初始数据
INSERT INTO `users` (`id`, `username`, `password`, `enabled`) VALUES (1, 'admin', '$2a$10$Ct3PZQvgUciDcbJtmdPz5Ow1qGCCaYypD.4YILDDUhpWPQ0FfHwsm', TRUE);
INSERT INTO `authorities` (`id`, `username`, `authority`, `user`) VALUES (1, 'admin', 'ROLE_DEVELOPER', 1);

INSERT INTO `users` (`id`, `username`, `password`, `enabled`) VALUES (2, 'user', '$2a$10$Ct3PZQvgUciDcbJtmdPz5Ow1qGCCaYypD.4YILDDUhpWPQ0FfHwsm', TRUE);
INSERT INTO `authorities` (`id`, `username`, `authority`, `user`) VALUES (2, 'user', 'ROLE_USER', 2);

drop table if exists `events`;
create table `events` (
  `id`                                 BIGINT       NOT NULL,
  `_status`                            VARCHAR(16)  NOT NULL,
  `remote_service_interface_name`      VARCHAR(255) NOT NULL,
  `remote_service_spring_bean_name`    VARCHAR(255),
  `remote_service_method_name`         VARCHAR(255) NOT NULL,
  `remote_service_method_param_types`  LONGTEXT     NOT NULL,
  `remote_service_method_param_values` LONGTEXT     NOT NULL,
  `_timestamp`                         BIGINT       NOT NULL,
  PRIMARY KEY (`id`)
)
  ENGINE = InnoDB
  DEFAULT CHARSET = utf8;