作业要求：（必做）按自己设计的表结构，插入 100 万订单模拟数据，测试不同方式的插入效率

大批量写入的优化
1. PreparedStatement 减少 SQL 解析 
2. Multiple Values/Add Batch 减少交互
   - Multiple Values insert into order values(), (), ()
   - Add Batch 数据连接需要添加参数：rewriteBatchedStatements=true	
3. Load Data，直接导入
   - load data local infile 'file_name' into table table_name
4. 索引和约束问题
   - 数据导入完成后，再统一建立索引

参考效率比较：Load Data > Procedure > PreparedStatement