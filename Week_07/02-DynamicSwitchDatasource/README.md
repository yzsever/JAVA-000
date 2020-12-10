### 作业要求：（必做）读写分离 - 动态切换数据源版本 1.0

### 实现思路
1. 读写分离就是在执行更新操作时使用主库，查询操作时使用从库。同时，为保证数据的一致性和考虑到同步的延时，同一事务中更新操作之后的查询操作也需要走主库。
2. 主库和从库的实例通过DataSource实现，即在执行对应操作时，需要选择正确的DataSource实例。
3. 服务启动时，创建主库和从库的DataSource实例，供操作时选择
4. 使用Spring AOP+注解的方式，在执行方法前切换DataSource

### 参考文档：
1. [读写分离很难吗？springboot结合aop简单就实现了](https://www.cnblogs.com/yeya/p/11936239.html)
2. [SpringBoot之多数据源动态切换数据源](https://www.cnblogs.com/shihaiming/p/11067623.html)
