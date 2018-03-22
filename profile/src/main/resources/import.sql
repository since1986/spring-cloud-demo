USE `profile`;

DROP TABLE IF EXISTS `profile`;
CREATE TABLE `profile` (
  `id`       BIGINT NOT NULL,
  `_name`    VARCHAR(64),
  `username` VARCHAR(16),
  `user_id`  BIGINT,
  `gender`   CHAR(1),
  `birthday` CHAR(10),
  `email`    VARCHAR(64),
  `phone`    VARCHAR(11),
  PRIMARY KEY (`id`)
);

INSERT INTO `profile` (`id`, `_name`, `username`, `user_id`, `gender`, `birthday`, `email`, `phone`) VALUES (1, '阿达敏', 'admin', 1, 'M', '2018-1-10', '385741668@qq.com', '12000000000');

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