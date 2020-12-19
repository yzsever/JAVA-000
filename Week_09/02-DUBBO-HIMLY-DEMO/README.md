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
   - 使用SharingSphere-Proxy部署分库分表，database使用trade-proxy数据库

### himly+dubbo(apache dubbo)实现TCC
1、引入himly依赖(spring boot)
```
<dependency>
    <groupId>org.dromara</groupId>
    <artifactId>hmily-spring-boot-starter-apache-dubbo</artifactId>
    <version>2.1.1</version>
</dependency>
```

2、在项目的 resource 新建文件名为:hmily.yml配置文件（local模式）
```
hmily:
  server:
    configMode: local
    appName: xiaoyu
  #  如果server.configMode eq local 的时候才会读取到这里的配置信息.
  config:
    appName: xiaoyu
    serializer: kryo
    contextTransmittalMode: threadLocal
    scheduledThreadMax: 16
    scheduledRecoveryDelay: 60
    scheduledCleanDelay: 60
    scheduledPhyDeletedDelay: 600
    scheduledInitDelay: 30
    recoverDelayTime: 60
    cleanDelayTime: 180
    limit: 200
    retryMax: 10
    bufferSize: 8192
    consumerThreads: 16
    asyncRepository: true
    autoSql: true
    phyDeleted: true
    storeDays: 3
    repository: mysql

repository:
  database:
    driverClassName: com.mysql.jdbc.Driver
    url : jdbc:mysql://localhost:3306/hmily?useUnicode=true&characterEncoding=utf8&useSSL=false
    username: root
    password: 123456
    maxActive: 20
    minIdle: 10
    connectionTimeout: 30000
    idleTimeout: 600000
    maxLifetime: 1800000
  zookeeper:
    host: localhost:2181
    sessionTimeOut: 1000000000
    rootPath: /hmily
```

> zookeeper是必须要的，所以官网下了个tar包，解压以后本地启动一下。

3、实现接口上添加注解

对@Hmily 标识的接口方法的具体实现上，加上@HmilyTCC(confirmMethod = "confirm", cancelMethod = "cancel")
- confirmMethod : 确认方法名称，该方法参数列表与返回类型应与标识方法一致。
- cancelMethod : 回滚方法名称，该方法参数列表与返回类型应与标识方法一致。

TCC模式应该保证 confirm 和 cancel 方法的幂等性，用户需要自行去开发这个2个方法，所有的事务的确认与回滚，完全由用户决定。Hmily框架只是负责来进行调用


### 花式掉坑
1. himlyTCC事务时，在执行confirm的方法时一直提示找不到对应方法。
   - 原来confirm和cancel方法的参数需要和Try方法一致，看用户手册的时候没有注意到这点！！！所以一定要细心。


### 参考文档
1. [himly-dubbo用户手册](https://dromara.org/website/zh-cn/docs/hmily/user-dubbo.html)
2. [himly-dubbo-demo源码内](https://github.com/dromara/hmily/tree/master/hmily-demo/hmily-demo-dubbo)




