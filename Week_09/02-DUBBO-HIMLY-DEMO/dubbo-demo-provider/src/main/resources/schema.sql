DROP TABLE IF EXISTS `usd_account`;

CREATE TABLE `usd_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `balance` decimal(10,0) NOT NULL COMMENT '用户余额',
  `create_time` bigint(20) NOT NULL,
  `update_time` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

DROP TABLE IF EXISTS `cnh_account`;

CREATE TABLE `cnh_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `balance` decimal(10,0) NOT NULL COMMENT '用户余额',
  `create_time` bigint(20) NOT NULL,
  `update_time` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;

DROP TABLE IF EXISTS `freeze_account`;

CREATE TABLE `freeze_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `freeze_usd` decimal(10,0) NOT NULL COMMENT '冻结美元数',
  `freeze_cnh` decimal(10,0) NOT NULL COMMENT '冻结人民币数',
  `create_time` bigint(20) NOT NULL,
  `update_time` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;