学习笔记

### 本周作业
Week07 作业题目（周四）：
1. （选做）用今天课上学习的知识，分析自己系统的 SQL 和表结构
2. （必做）按自己设计的表结构，插入 100 万订单模拟数据，测试不同方式的插入效率
3. （选做）按自己设计的表结构，插入 1000 万订单模拟数据，测试不同方式的插入效
4. （选做）使用不同的索引或组合，测试不同方式查询效率
5. （选做）调整测试数据，使得数据尽量均匀，模拟 1 年时间内的交易，计算一年的销售报表：销售总额，订单数，客单价，每月销售量，前十的商品等等（可以自己设计更多指标）
6. （选做）尝试自己做一个 ID 生成器（可以模拟 Seq 或 Snowflake）
7. （选做）尝试实现或改造一个非精确分页的程序

Week07 作业题目（周六）：
1. （选做）配置一遍异步复制，半同步复制、组复制
2. （必做）读写分离 - 动态切换数据源版本 1.0
3. （必做）读写分离 - 数据库框架版本 2.0
4. （选做）读写分离 - 数据库中间件版本 3.0
5. （选做）配置 MHA，模拟 master 宕机
6. （选做）配置 MGR，模拟 master 宕机
7. （选做）配置 Orchestrator，模拟 master 宕机，演练 UI 调整拓扑结构

### 完成作业
#### 必做
1. （必做）按自己设计的表结构，插入 100 万订单模拟数据，测试不同方式的插入效率
   - [作业目录:01-InsertData](https://github.com/yzsever/JAVA-000/tree/main/Week_07/01-InsertData)
2. （必做）读写分离 - 动态切换数据源版本 1.0
   - [作业目录:02-DynamicSwitchDatasource](https://github.com/yzsever/JAVA-000/tree/main/Week_07/02-DynamicSwitchDatasource)
3. （必做）读写分离 - 数据库框架版本 2.0
   - [作业目录:03-ShardingSphere-RWSplitting](https://github.com/yzsever/JAVA-000/tree/main/Week_07/03-ShardingSphere-RWSplitting)