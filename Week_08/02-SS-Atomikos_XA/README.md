### 作业要求
（必做）基于 hmily TCC 或 ShardingSphere 的 Atomikos XA 实现一个简单的分布式事务应用 demo（二选一），提交到 Github。

### 实现步骤
#### 选择方案
选用ShardingSphere 的 Atomikos XA实现，参考[用户手册](https://shardingsphere.apache.org/document/current/cn/user-manual/shardingsphere-jdbc/usage/transaction/spring-boot-starter/)。这里选用的是Spring Boot Starter的方式。

#### 代码实现
**1、引入 Maven 依赖**
```
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-jdbc-core-spring-boot-starter</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用 XA 事务时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-xa-core</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>

<!-- 使用 BASE 事务时，需要引入此模块 -->
<dependency>
    <groupId>org.apache.shardingsphere</groupId>
    <artifactId>shardingsphere-transaction-base-seata-at</artifactId>
    <version>${shardingsphere.version}</version>
</dependency>
```

**2、配置事务管理器**
```
@Configuration
@EnableTransactionManagement
public class TransactionConfiguration {
    
    @Bean
    public PlatformTransactionManager txManager(final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }
    
    @Bean
    public JdbcTemplate jdbcTemplate(final DataSource dataSource) {
        return new JdbcTemplate(dataSource);
    }
}
```

**3、使用分布式事务**
```
@Transactional
@ShardingTransactionType(TransactionType.XA)  // 支持TransactionType.LOCAL, TransactionType.XA, TransactionType.BASE
public void insert() {
    jdbcTemplate.execute("INSERT INTO t_order (user_id, status) VALUES (?, ?)", (PreparedStatementCallback<Object>) ps -> {
        ps.setObject(1, i);
        ps.setObject(2, "init");
        ps.executeUpdate();
    });
}
```

### 花式掉坑
1、错误信息如下：
```
Caused by: org.springframework.beans.factory.UnsatisfiedDependencyException: Error creating bean with name 'txManager' defined in class path resource [homework/jenson/TransactionConfiguration.class]: Unsatisfied dependency expressed through method 'txManager' parameter 0; nested exception is org.springframework.beans.factory.NoSuchBeanDefinitionException: No qualifying bean of type 'javax.sql.DataSource' available: expected at least 1 bean which qualifies as autowire candidate. Dependency annotations: {}
```

解决方案：是因为缺少了shardingsphere-transaction-xa-core模块的应用。

2、单元测试时报错：需要指定junit版本

3、No qualifying bean of type 'javax.sql.DataSource' available。原因：shardingsphere-jdbc-core-spring-boot-starter模块引入异常

总结：由于我开始没有看到分布式事务的用户手册，所以直接从ShardingSphere的example找到了spring-boot的用例，然后将它剪切出来修改运行的。但是example引用的项目本身，导致很多依赖的模块都是有问题的。所以直接根据用户手册来配，就没问题了。


