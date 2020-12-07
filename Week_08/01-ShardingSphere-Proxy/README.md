### 作业要求
（必做）设计对前面的订单表数据进行水平分库分表，拆分2个库，每个库16张表。并在新结构在演示常见的增删改查操作。代码、sql和配置文件，上传到 Github。

### 环境搭建
#### 1、在服务器中的docker中准备两个mysql:5.7数据库
```
# docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                               NAMES
79da9322d5cd        mysql:5.7           "docker-entrypoint.s…"   5 hours ago         Up 5 hours          33060/tcp, 0.0.0.0:3310->3306/tcp   master02
c2e509a41b29        mysql:5.7           "docker-entrypoint.s…"   45 hours ago        Up 42 hours         33060/tcp, 0.0.0.0:3308->3306/tcp   slave
ec4a868c061d        mysql:5.7           "docker-entrypoint.s…"   45 hours ago        Up 42 hours         33060/tcp, 0.0.0.0:3309->3306/tcp   master
```
这里使用master和master02。

#### 2、在管网下载shardingsphere-proxy的tar包[下载地址](https://www.apache.org/dyn/closer.cgi/shardingsphere/5.0.0-alpha/apache-shardingsphere-5.0.0-alpha-shardingsphere-proxy-bin.tar.gz)。
   - 服务器上准备java环境(使用版本：1.8.0_271)
   - tar -zxvf apache-shardingsphere-5.0.0-alpha-shardingsphere-proxy-bin.tar.gz 解压
   - 进入%SHARDINGSPHERE_PROXY_HOME%/lib 目录。 下载mysql驱动执行 `wget https://repo1.maven.org/maven2/mysql/mysql-connector-java/5.1.47/mysql-connector-java-5.1.47.jar`
   - 配置文件
      - %SHARDINGSPHERE_PROXY_HOME%/conf/config-sharding.yaml
      - %SHARDINGSPHERE_PROXY_HOME%/conf/server.yaml
   - 启动服务：sh %SHARDINGSPHERE_PROXY_HOME%/bin/start.sh

#### 3、配置文件
**config-sharding.yaml**
```
schemaName: eshop_proxy

dataSources:
  ds_0:
    url: jdbc:mysql://127.0.0.1:3309/eshop?serverTimezone=UTC&useSSL=false
    username: root
    password: 123456
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 10
  ds_1:
    url: jdbc:mysql://127.0.0.1:3310/eshop?serverTimezone=UTC&useSSL=false
    username: root
    password: 123456
    connectionTimeoutMilliseconds: 30000
    idleTimeoutMilliseconds: 60000
    maxLifetimeMilliseconds: 1800000
    maxPoolSize: 10

rules:
- !SHARDING
  tables:
    order:
      actualDataNodes: ds_${0..1}.order_${0..15}
      tableStrategy:
        standard:
          shardingColumn: order_id
          shardingAlgorithmName: order_inline
      keyGenerateStrategy:
        column: order_id
        keyGeneratorName: snowflake
  defaultDatabaseStrategy:
    standard:
      shardingColumn: user_id
      shardingAlgorithmName: database_inline
  defaultTableStrategy:
    none:

  shardingAlgorithms:
    database_inline:
      type: INLINE
      props:
        algorithm-expression: ds_${user_id % 2}
    order_inline:
      type: INLINE
      props:
        algorithm-expression: order_${order_id % 16}

  keyGenerators:
    snowflake:
      type: SNOWFLAKE
      props:
        worker-id: 123

```
order表分片算法，以user_id分库，以order_id分表。


**server.yaml**
```
authentication:
  users:
    root:
      password: 123456

props:
  max-connections-size-per-query: 1
  acceptor-size: 16  # The default value is available processors count * 2.
  executor-size: 16  # Infinite by default.
  proxy-frontend-flush-threshold: 128  # The default value is 128.
    # LOCAL: Proxy will run with LOCAL transaction.
    # XA: Proxy will run with XA transaction.
    # BASE: Proxy will run with B.A.S.E transaction.
  proxy-transaction-type: LOCAL
  proxy-opentracing-enabled: false
  proxy-hint-enabled: false
  query-with-cipher-column: true
  sql-show: true
  check-table-metadata-enabled: false
```


### 演示
ShardingSphere-Proxy的sql日志打印需要修改server.yaml中的sql-show属性为true。

#### 访问数据库
启动服务后，通过3307端口访问数据库
```
Server version: 5.7.32-log-ShardingSphere-Proxy 5.0.0-RC1
Copyright (c) 2000, 2014, Oracle and/or its affiliates. All rights reserved.
...
mysql> show databases;
+-------------+
| Database    |
+-------------+
| eshop_proxy |
+-------------+
1 row in set (0.07 sec)
```

#### 增
执行sql测试分库分表策略是否生效：
```
insert into `order`(order_id, user_id) values(1, 1), (2, 1), (3, 1), (4, 1), (5, 1), (6, 1), (7, 1), (8, 1), (9,1), (10, 1),(11, 1), (12, 1),(13, 1), (14,1), (15,1),(16,1), (17,2), (18, 2), (19, 2), (20, 2), (21, 2), (22, 2),(23, 2), (24, 2), (25, 2), (26, 2), (27, 2), (28, 2), (29,2), (30, 2), (31, 2), (32, 2);
```
查看日志：
```
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Logic SQL: insert into `order`(order_id, user_id) values(1, 1), (2, 1), (3, 1), (4, 1), (5, 1), (6, 1), (7, 1), (8, 1), (9,1), (10, 1),(11, 1), (12, 1),(13, 1), (14,1), (15,1),(16,1), (17,2), (18, 2), (19, 2), (20, 2), (21, 2), (22, 2),(23, 2), (24, 2), (25, 2), (26, 2), (27, 2), (28, 2), (29,2), (30, 2), (31, 2), (32, 2)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - SQLStatement: MySQLInsertStatement(setAssignment=Optional.empty, onDuplicateKeyColumns=Optional.empty)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_1`(order_id, user_id) values(1, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_2`(order_id, user_id) values(2, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_3`(order_id, user_id) values(3, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_4`(order_id, user_id) values(4, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_5`(order_id, user_id) values(5, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_6`(order_id, user_id) values(6, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_7`(order_id, user_id) values(7, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_8`(order_id, user_id) values(8, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_9`(order_id, user_id) values(9, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_10`(order_id, user_id) values(10, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_11`(order_id, user_id) values(11, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_12`(order_id, user_id) values(12, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_13`(order_id, user_id) values(13, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_14`(order_id, user_id) values(14, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_15`(order_id, user_id) values(15, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_1 ::: insert into `order_0`(order_id, user_id) values(16, 1)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_1`(order_id, user_id) values(17, 2)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_2`(order_id, user_id) values(18, 2)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_3`(order_id, user_id) values(19, 2)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_4`(order_id, user_id) values(20, 2)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_5`(order_id, user_id) values(21, 2)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_6`(order_id, user_id) values(22, 2)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_7`(order_id, user_id) values(23, 2)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_8`(order_id, user_id) values(24, 2)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_9`(order_id, user_id) values(25, 2)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_10`(order_id, user_id) values(26, 2)
[INFO ] 16:23:28.940 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_11`(order_id, user_id) values(27, 2)
[INFO ] 16:23:28.941 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_12`(order_id, user_id) values(28, 2)
[INFO ] 16:23:28.941 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_13`(order_id, user_id) values(29, 2)
[INFO ] 16:23:28.941 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_14`(order_id, user_id) values(30, 2)
[INFO ] 16:23:28.941 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_15`(order_id, user_id) values(31, 2)
[INFO ] 16:23:28.941 [ShardingSphere-Command-13] ShardingSphere-SQL - Actual SQL: ds_0 ::: insert into `order_0`(order_id, user_id) values(32, 2)
```
数据成功插入2个数据库，各16张数据库表中。

#### 删
确定order_id和user_id删除数据，可准确定位到对应数据库和表。
```
[INFO ] 16:54:58.049 [ShardingSphere-Command-7] ShardingSphere-SQL - Logic SQL: delete from `order` where order_id=10 and user_id=1
[INFO ] 16:54:58.050 [ShardingSphere-Command-7] ShardingSphere-SQL - SQLStatement: MySQLDeleteStatement(orderBy=Optional.empty, limit=Optional.empty)
[INFO ] 16:54:58.050 [ShardingSphere-Command-7] ShardingSphere-SQL - Actual SQL: ds_1 ::: delete from `order_10` where order_id=10 and user_id=1
```

#### 改
更新user_id为1的用户的订单更新时间为现在。从日志可以看出，直接通过user_id路由到了ds_1
```
[INFO ] 16:50:30.816 [ShardingSphere-Command-2] ShardingSphere-SQL - Logic SQL: update `order` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - SQLStatement: MySQLUpdateStatement(orderBy=Optional.empty, limit=Optional.empty)
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_0` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_1` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_2` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_3` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_4` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_5` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_6` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_7` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_8` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_9` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_10` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_11` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_12` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_13` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_14` set update_time=now() where user_id=1
[INFO ] 16:50:30.817 [ShardingSphere-Command-2] ShardingSphere-SQL - Actual SQL: ds_1 ::: update `order_15` set update_time=now() where user_id=1
```

#### 查
执行语句select * from `order` where order_id=15。无user_id，需要两个数据库都进行查询，但是根据order_id路由到了对应的数据库表，所以只要查两个表。
```
[INFO ] 16:52:35.902 [ShardingSphere-Command-5] ShardingSphere-SQL - Logic SQL: select * from `order` where order_id=15
[INFO ] 16:52:35.902 [ShardingSphere-Command-5] ShardingSphere-SQL - SQLStatement: MySQLSelectStatement(limit=Optional.empty, lock=Optional.empty)
[INFO ] 16:52:35.902 [ShardingSphere-Command-5] ShardingSphere-SQL - Actual SQL: ds_0 ::: select * from `order_15` where order_id=15
[INFO ] 16:52:35.902 [ShardingSphere-Command-5] ShardingSphere-SQL - Actual SQL: ds_1 ::: select * from `order_15` where order_id=15
```


