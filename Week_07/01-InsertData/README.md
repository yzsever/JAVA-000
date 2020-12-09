作业要求：（必做）按自己设计的表结构，插入 100 万订单模拟数据，测试不同方式的插入效率

### 大批量写入的优化
1. PreparedStatement 减少 SQL 解析 
2. Multiple Values/Add Batch 减少交互
   - Multiple Values insert into order values(), (), ()
   - Add Batch 数据连接需要添加参数：rewriteBatchedStatements=true	
3. Load Data，直接导入
   - load data local infile 'file_name' into table table_name
4. 索引和约束问题
   - 数据导入完成后，再统一建立索引
老师给的参考效率比较：Load Data > Procedure > PreparedStatement

### 测试结果
|批量写入方法  |描述        | 第一次结果  | 第二次结果 | 第三次结果 | 平均结果  | 
|----        |----       | ----      | ----     | ----     |----      |
|Add Batch   |1w 100次   |527403ms   |/         |/         |8min 47.403ms  |
|Add Batch   |100w 1次|577416ms   |/         |/         |9min 37.416ms  |
|Add Batch&rewriteBatchedStatements=true   |1w 100次|34570ms   |46378ms         |50632ms        |43.860s  |
|Add Batch&rewriteBatchedStatements=true   |100w 1次|14568ms   |13885ms         |13660ms         |14.038s  |
|Procedure&Prepared       |100w 1次     |1min 26.46s|1min 25.00s |1 min 23.89 sec| 1min 25.117s|
|Procedure&Multiple Values|100w 1次     |9.40s |8.53s |9.06s |9.00s |
|Load data   |100w 1次 |9.36s  |8.68s  |9.75s |9.26s | 

#### 测试总结
1. 100w条数据分多次执行要慢于1次执行
2. 插入效率比较：Procedure&Multiple Values ~= Load data > Add Batch&rewriteBatchedStatements=true > Procedure&Prepared
3. 理论上Add Batch&rewriteBatchedStatements=true就是将sql处理成Multiple Values的形式
4. Load data实际插入执行操作是Multiple Values的形式
5. 所以使用Multiple Values，减少事务上下文开销，效率最快

### 测试方法详情
#### Procedure&Prepared 
```
USE `test`;
DROP PROCEDURE if exists insert_datas_by_prepare;
DELIMITER $$

CREATE PROCEDURE insert_datas_by_prepare(IN row_nums BIGINT)
BEGIN
    DECLARE i INT DEFAULT 0;

    PREPARE stmt
       FROM 'INSERT INTO `t_order` (user_id, status) VALUES (?, ?)';
    SET @user_id = 0, @status = 'init';

    START TRANSACTION;
    WHILE i < row_nums DO
        SET @user_id = i;
        EXECUTE stmt USING @user_id, @status;
        SET i = i + 1;
    END WHILE;
    COMMIT;
    DEALLOCATE PREPARE stmt;
END$$
DELIMITER ;
```
#### Procedure&Multiple Values
```
USE `test`;
DROP PROCEDURE if exists insert_datas_by_multiple_values;
DELIMITER $$

CREATE PROCEDURE insert_datas_by_multiple_values()

BEGIN
INSERT INTO `t_order` (user_id, status)
SELECT  N, 'init' FROM
(
select a.N + b.N * 10 + c.N * 100 + d.N * 1000 + e.N * 10000 + f.N * 100000 + 1 N
from (select 0 as N union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) a
      , (select 0 as N union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) b
      , (select 0 as N union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) c
      , (select 0 as N union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) d
      , (select 0 as N union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) e
      , (select 0 as N union all select 1 union all select 2 union all select 3 union all select 4 union all select 5 union all select 6 union all select 7 union all select 8 union all select 9) f
) t;

END$$
DELIMITER ;
```

### Load Data

#### 导出数据
```
SELECT order_id, user_id, status INTO OUTFILE '/tmp/t_order.txt'
FIELDS TERMINATED BY ',' OPTIONALLY ENCLOSED BY '"'
LINES TERMINATED BY '\n'
FROM t_order; 
```

#### 导入数据
```
load data infile '/tmp/t_order.txt' replace into table t_order character set utf8mb4 
fields terminated by ',' enclosed by '"' 
lines terminated by '\n' 
(`order_id`,`user_id`,`status`);
```

### 参考文档
[mysql-fill-a-table-within-a-stored-procedure-efficiently](https://stackoverflow.com/questions/17136592/mysql-fill-a-table-within-a-stored-procedure-efficiently?noredirect=1&lq=1)
[are-stored-procedures-more-efficient-in-general-than-inline-statements-on-mode](https://stackoverflow.com/questions/59880/are-stored-procedures-more-efficient-in-general-than-inline-statements-on-mode)


### 花式踩坑
1、“Can’t create/write to file xxx (Errcode: 13)” -> mysql临时文件需要存到系统的临时目录










































