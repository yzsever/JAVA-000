### 作业要求：（必做）读写分离 - 动态切换数据源版本 1.0

### 实现思路
1. 读写分离就是在执行更新操作时使用主库，查询操作时使用从库。同时，为保证数据的一致性和考虑到同步的延时，同一事务中更新操作之后的查询操作也需要走主库。
2. 主库和从库的实例通过DataSource实现，即在执行对应操作时，需要选择正确的DataSource实例。
3. 服务启动时，创建主库和从库的DataSource实例，供操作时选择
4. 使用Spring AOP+注解的方式，在service层方法上添加注解，在执行方法前切换DataSource


### 测试结果
1、DDL操作使用主库
```
========数据源切换至：{}master
···
[DEBUG] 2020-12-10 20:54:21,422 --main-- [me.jenson.repository.OrderRepository.dropTable] ==>  Preparing: DROP TABLE IF EXISTS t_order;  
[DEBUG] 2020-12-10 20:54:21,445 --main-- [me.jenson.repository.OrderRepository.dropTable] ==> Parameters:  
[DEBUG] 2020-12-10 20:54:21,465 --main-- [me.jenson.repository.OrderRepository.dropTable] <==    Updates: 0 
···
[DEBUG] 2020-12-10 20:54:21,466 --main-- [me.jenson.repository.OrderRepository.createTableIfNotExists] ==>  Preparing: CREATE TABLE IF NOT EXISTS t_order (order_id BIGINT AUTO_INCREMENT, user_id INT NOT NULL, address_id BIGINT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id));  
[DEBUG] 2020-12-10 20:54:21,466 --main-- [me.jenson.repository.OrderRepository.createTableIfNotExists] ==> Parameters:  
[DEBUG] 2020-12-10 20:54:21,498 --main-- [me.jenson.repository.OrderRepository.createTableIfNotExists] <==    Updates: 0 
···
[DEBUG] 2020-12-10 20:54:21,499 --main-- [me.jenson.repository.OrderRepository.truncateTable] ==>  Preparing: TRUNCATE TABLE t_order;  
[DEBUG] 2020-12-10 20:54:21,499 --main-- [me.jenson.repository.OrderRepository.truncateTable] ==> Parameters:  
[DEBUG] 2020-12-10 20:54:21,521 --main-- [me.jenson.repository.OrderRepository.truncateTable] <==    Updates: 0 
···
```

2、Insert与之后的查询操作使用主库
```
========数据源切换至：{}master
···
-------------- Process Success Begin ---------------
···
[DEBUG] 2020-12-10 20:54:21,536 --main-- [me.jenson.repository.OrderRepository.insert] ==>  Preparing: INSERT INTO t_order (user_id, address_id, status) VALUES (?, ?, ?);  
[DEBUG] 2020-12-10 20:54:21,539 --main-- [me.jenson.repository.OrderRepository.insert] ==> Parameters: 1(Integer), 1(Long), INSERT_TEST(String) 
[DEBUG] 2020-12-10 20:54:21,558 --main-- [me.jenson.repository.OrderRepository.insert] <==    Updates: 1 
···
---------------------------- Print Order Data -----------------------
···
[DEBUG] 2020-12-10 20:54:21,736 --main-- [me.jenson.repository.OrderRepository.selectAll] ==>  Preparing: SELECT * FROM t_order;  
[DEBUG] 2020-12-10 20:54:21,737 --main-- [me.jenson.repository.OrderRepository.selectAll] ==> Parameters:  
[DEBUG] 2020-12-10 20:54:21,761 --main-- [me.jenson.repository.OrderRepository.selectAll] <==      Total: 10 
order_id: 10, user_id: 10, address_id: 10, status: INSERT_TEST
···
-------------- Process Success Finish --------------
```

3、单独的查询操作使用从库
```

========数据源切换至：{}slave
---------------------------- Print Order Data -----------------------
···
[DEBUG] 2020-12-10 20:54:21,888 --main-- [me.jenson.repository.OrderRepository.selectAll] ==>  Preparing: SELECT * FROM t_order;  
[DEBUG] 2020-12-10 20:54:21,889 --main-- [me.jenson.repository.OrderRepository.selectAll] ==> Parameters:  
[DEBUG] 2020-12-10 20:54:21,903 --main-- [me.jenson.repository.OrderRepository.selectAll] <==      Total: 10 
[DEBUG] 2020-12-10 20:54:21,903 --main-- [org.mybatis.spring.SqlSessionUtils] Closing non transactional SqlSession [org.apache.ibatis.session.defaults.DefaultSqlSession@3e850122] 
···
order_id: 10, user_id: 10, address_id: 10, status: INSERT_TEST
```


### 参考文档：
1. [读写分离很难吗？springboot结合aop简单就实现了](https://www.cnblogs.com/yeya/p/11936239.html)
2. [SpringBoot之多数据源动态切换数据源](https://www.cnblogs.com/shihaiming/p/11067623.html)
