### 作业要求：（必做）结合 dubbo+hmily，实现一个 TCC 外汇交易处理，代码提交到 GitHub:
1. 用户 A 的美元账户和人民币账户都在 A 库，使用 1 美元兑换 7 人民币 ;
2. 用户 B 的美元账户和人民币账户都在 B 库，使用 7 人民币兑换 1 美元 ;
3. 设计账户表，冻结资产表，实现上述两个本地事务的分布式事务。

### 作业思路
#### 设计数据库表：
1、美元账户表：usd_account：
```
DROP TABLE IF EXISTS `usd_account`;

CREATE TABLE `usd_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(128) NOT NULL,
  `balance` decimal(10,0) NOT NULL COMMENT '用户余额',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

2、人民币账户表：cnh_account
```
DROP TABLE IF EXISTS `cnh_account`;

CREATE TABLE `cnh_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(128) NOT NULL,
  `balance` decimal(10,0) NOT NULL COMMENT '用户余额',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```

3、资产冻结表： freeze_account
```
DROP TABLE IF EXISTS `freeze_account`;

CREATE TABLE `freeze_account` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `user_id` varchar(128) NOT NULL,
  `freeze_usd` decimal(10,0) NOT NULL COMMENT '冻结美元数',
  `freeze_cnh` decimal(10,0) NOT NULL COMMENT '冻结人民币数',
  `create_time` datetime NOT NULL,
  `update_time` datetime DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=2 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_bin;
```


#### 业务场景过程分析
1、初始状态
- A用户需要美元换人民币：A用户美元账户有1块钱，人民币账户没有钱。
- B用户需要人民币换美元：B用户美元账户没有钱，人民币账户有7块钱。

> 初始化数据
> 设A用户user_id为2，B用户user_id为3。
> insert into `usd_account`(user_id, balance, create_time) values(2, 1, now());
> insert into `cnh_account`(user_id, balance, create_time) values(3, 7, now());

2、业务内容
先需要同时完成A用户1美元换成7人民币，B用户7人民币换成1美元。
1. dubbo-provider
   - USDToCNHService：美元换人民币服务
   - CNHToUSDService：人民币换美元服务 
2. Try过程
   - A用户美元账户减1，资产冻结表A用户加1美元
   - B用户人民币账户减7，资产冻结表B用户加7人民币
3. Confirm过程
   - A用户人民币账户加7，资产冻结表A用户对应数据删除
   - B用户美元账户加1，资产冻结表B用户对应数据删除

3、实现方法
1. A库、B库
   - 水平分库，以user_id作为进行分表





