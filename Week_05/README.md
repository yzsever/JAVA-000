## 本周作业要求

### Week05 作业题目（周四）：
1.（选做）使 Java 里的动态代理，实现一个简单的 AOP。
2.（必做）写代码实现 Spring Bean 的装配，方式越多越好（XML、Annotation 都可以）, 提交到 Github。
3.（选做）实现一个 Spring XML 自定义配置，配置一组 Bean，例如：Student/Klass/School。

### Week05 作业题目（周六）：
1.（选做）总结一下，单例的各种写法，比较它们的优劣。
2.（选做）maven/spring 的 profile 机制，都有什么用法？
3.（选做）总结 Hibernate 与 MyBatis 的各方面异同点。
4.（必做）给前面课程提供的 Student/Klass/School 实现自动配置和 Starter。
5.（选做）学习 MyBatis-generator 的用法和原理，学会自定义 TypeHandler 处理复杂类型。
6.（必做）研究一下 JDBC 接口和数据库连接池，掌握它们的设计和用法：
   - 1.使用 JDBC 原生接口，实现数据库的增删改查操作。
   - 2.使用事务，PrepareStatement 方式，批处理方式，改进上述操作。
   - 3.配置 Hikari 连接池，改进上述操作。提交代码到 Github。

---

### 完成作业如下：
1.（必做）写代码实现 Spring Bean 的装配，方式越多越好（XML、Annotation 都可以）, 提交到 Github。
   - [作业目录](https://github.com/yzsever/JAVA-000/tree/main/Week_05/01-Bean)
   - 实现了通过XML形式装配
   - 实现了通过Annotation形式自动装配
2.（必做）给前面课程提供的 Student/Klass/School 实现自动配置和 Starter。
   - [作业目录](https://github.com/yzsever/JAVA-000/tree/main/Week_05/02-Starter)
   - jenson-spring-boot-starter 自定义Starter
   - start-test 测试项目
