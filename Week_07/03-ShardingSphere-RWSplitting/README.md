
作业要求：（必做）读写分离 - 数据库框架版本 2.0








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
order_id: 2, user_id: 2, address_id: 2, status: INSERT_TEST
order_id: 3, user_id: 3, address_id: 3, status: INSERT_TEST
order_id: 4, user_id: 4, address_id: 4, status: INSERT_TEST
order_id: 5, user_id: 5, address_id: 5, status: INSERT_TEST
order_id: 6, user_id: 6, address_id: 6, status: INSERT_TEST
order_id: 7, user_id: 7, address_id: 7, status: INSERT_TEST
order_id: 8, user_id: 8, address_id: 8, status: INSERT_TEST
order_id: 9, user_id: 9, address_id: 9, status: INSERT_TEST
order_id: 10, user_id: 10, address_id: 10, status: INSERT_TEST
-------------- Process Success Finish --------------
---------------------------- Print Order Data -----------------------
[INFO ] 2020-12-10 15:34:58,494 --main-- [ShardingSphere-SQL] Logic SQL: SELECT * FROM t_order; 
[INFO ] 2020-12-10 15:34:58,495 --main-- [ShardingSphere-SQL] SQLStatement: MySQLSelectStatement(limit=Optional.empty, lock=Optional.empty) 
[INFO ] 2020-12-10 15:34:58,495 --main-- [ShardingSphere-SQL] Actual SQL: ds_1 ::: SELECT * FROM t_order; 
[INFO ] 2020-12-10 15:34:58,554 --main-- [org.springframework.beans.factory.xml.XmlBeanDefinitionReader] Loading XML bean definitions from class path resource [org/springframework/jdbc/support/sql-error-codes.xml] 
[INFO ] 2020-12-10 15:34:58,644 --main-- [ShardingSphere-SQL] Logic SQL: DROP TABLE IF EXISTS t_order; 
[INFO ] 2020-12-10 15:34:58,644 --main-- [ShardingSphere-SQL] SQLStatement: MySQLDropTableStatement() 
[INFO ] 2020-12-10 15:34:58,644 --main-- [ShardingSphere-SQL] Actual SQL: ds_0 ::: DROP TABLE IF EXISTS t_order; 