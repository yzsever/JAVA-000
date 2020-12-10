### 作业要求：（必做）读写分离 - 数据库框架版本 2.0

### 实现思路
1. 数据库框架采用ShardingSphere-JDBC
2. 配置1个主库、1个从库
3. 参考ShardingSphere-JDBC exmaple中的读写分离的样例进行实现


### 测试结果
配置主库为ds_0, 从库为ds_1,具体配置如下：
```
spring.shardingsphere.rules.replica-query.load-balancers.round_robin.type=ROUND_ROBIN
spring.shardingsphere.rules.replica-query.data-sources.pr_ds.primary-data-source-name=ds_0
spring.shardingsphere.rules.replica-query.data-sources.pr_ds.replica-data-source-names=ds_1
spring.shardingsphere.rules.replica-query.data-sources.pr_ds.load-balancer-name=round_robin
```

根据日志可以看出，读写分离成功：
1. DDL操作走主库ds_0
2. insert操作走主库，在同一个事务中insert之后的查询走主库
```
[INFO ] 2020-12-10 15:34:57,025 --main-- [ShardingSphere-SQL] Logic SQL: DROP TABLE IF EXISTS t_order; 
[INFO ] 2020-12-10 15:34:57,026 --main-- [ShardingSphere-SQL] SQLStatement: MySQLDropTableStatement() 
[INFO ] 2020-12-10 15:34:57,026 --main-- [ShardingSphere-SQL] Actual SQL: ds_0 ::: DROP TABLE IF EXISTS t_order; 
[INFO ] 2020-12-10 15:34:57,670 --main-- [ShardingSphere-SQL] Logic SQL: CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT AUTO_INCREMENT, user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id)); 
[INFO ] 2020-12-10 15:34:57,670 --main-- [ShardingSphere-SQL] SQLStatement: MySQLCreateTableStatement(isNotExisted=true) 
[INFO ] 2020-12-10 15:34:57,670 --main-- [ShardingSphere-SQL] Actual SQL: ds_0 ::: CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT AUTO_INCREMENT, user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id)); 
[INFO ] 2020-12-10 15:34:57,997 --main-- [ShardingSphere-SQL] Logic SQL: TRUNCATE TABLE t_order; 
[INFO ] 2020-12-10 15:34:57,998 --main-- [ShardingSphere-SQL] SQLStatement: MySQLTruncateStatement() 
[INFO ] 2020-12-10 15:34:57,998 --main-- [ShardingSphere-SQL] Actual SQL: ds_0 ::: TRUNCATE TABLE t_order; 
-------------- Process Success Begin ---------------
---------------------------- Insert Data ----------------------------
[INFO ] 2020-12-10 15:34:58,374 --main-- [ShardingSphere-SQL] Logic SQL: INSERT INTO t_order (user_id, address_id, status) VALUES (?, ?, ?); 
[INFO ] 2020-12-10 15:34:58,374 --main-- [ShardingSphere-SQL] SQLStatement: MySQLInsertStatement(setAssignment=Optional.empty, onDuplicateKeyColumns=Optional.empty) 
[INFO ] 2020-12-10 15:34:58,374 --main-- [ShardingSphere-SQL] Actual SQL: ds_0 ::: INSERT INTO t_order (user_id, address_id, status) VALUES (?, ?, ?); ::: [1, 1, INSERT_TEST] 
[INFO ] 2020-12-10 15:34:58,388 --main-- [ShardingSphere-SQL] Logic SQL: INSERT INTO t_order (user_id, address_id, status) VALUES (?, ?, ?); 
[INFO ] 2020-12-10 15:34:58,388 --main-- [ShardingSphere-SQL] SQLStatement: MySQLInsertStatement(setAssignment=Optional.empty, onDuplicateKeyColumns=Optional.empty) 
...
---------------------------- Print Order Data -----------------------
[INFO ] 2020-12-10 15:34:58,456 --main-- [ShardingSphere-SQL] Logic SQL: SELECT * FROM t_order; 
[INFO ] 2020-12-10 15:34:58,462 --main-- [ShardingSphere-SQL] SQLStatement: MySQLSelectStatement(limit=Optional.empty, lock=Optional.empty) 
[INFO ] 2020-12-10 15:34:58,463 --main-- [ShardingSphere-SQL] Actual SQL: ds_0 ::: SELECT * FROM t_order; 
order_id: 1, user_id: 1, address_id: 1, status: INSERT_TEST
...
-------------- Process Success Finish --------------
```

1. 单独的查询操作走的从库ds_1
```
---------------------------- Print Order Data -----------------------
[INFO ] 2020-12-10 15:34:58,494 --main-- [ShardingSphere-SQL] Logic SQL: SELECT * FROM t_order; 
[INFO ] 2020-12-10 15:34:58,495 --main-- [ShardingSphere-SQL] SQLStatement: MySQLSelectStatement(limit=Optional.empty, lock=Optional.empty) 
[INFO ] 2020-12-10 15:34:58,495 --main-- [ShardingSphere-SQL] Actual SQL: ds_1 ::: SELECT * FROM t_order; 
[INFO ] 2020-12-10 15:34:58,554 --main-- [org.springframework.beans.factory.xml.XmlBeanDefinitionReader] Loading XML bean definitions from class path resource [org/springframework/jdbc/support/sql-error-codes.xml] 
```
