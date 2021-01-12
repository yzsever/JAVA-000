### 作业要求：（必做）搭建 ActiveMQ 服务，基于 JMS，写代码分别实现对于 queue 和 topic 的消息生产和消费，代码提交到 github。

### ActiveMQ环境搭建
使用阿里云+docker

```sh
docker pull webcenter/activemq
docker run -d --name activemq -p 61616:61616 -p 8161:8161 webcenter/activemq
```

### Spring Boot集成ActiveMQ

Spring Boot针对ActiveMQ专门提供了spring-boot-starter-activemq，用来支持ActiveMQ在Spring Boot的自动集成配置。在此基础上我们可以很轻易的进行集成和使用。

创建标准的Spring Boot项目，并在项目中引入以下依赖：
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-activemq</artifactId>
</dependency>
```

配置文件:在application.properties中添加如下配置：
```
spring.activemq.broker-url=tcp://127.0.0.1:61616
spring.activemq.user=admin
spring.activemq.password=admin
```

### JMS操作

1. ActiveMQQueue：由ActiveMQ对javax.jms.Queue的接口实现，ActiveMQQueue需要传入消息队列的名称进行实例化Queue和Topic。
2. JmsMessagingTemplate: Spring提供发送消息的工具类，结合Queue和Topic对消息进行发送。JmsMessagingTemplate默认已经被实例化。
3. Spring提供了注解式监听器端点：使用@JmsListener。
4. 同时支持队列模式(queue)和支持广播模式(topic)，需要自定义JmsListenerContainerFactory实例，使用@JmsListener时指定containerFactory为自定义的实例。

### 生产者
```java
@Component
public class Producer {

    @Resource
    private JmsMessagingTemplate jmsMessagingTemplate;

    @Resource
    private Queue queue;

    @Resource
    private Topic topic;

    public void sendMsg(String msg) {
        System.out.println("发送消息内容 :" + msg);
        this.jmsMessagingTemplate.convertAndSend(this.queue, msg);
    }

    public void sendTopic(String msg) {
        System.out.println("发送Topic消息内容 :"+msg);
        this.jmsMessagingTemplate.convertAndSend(this.topic, msg);
    }
}
```

### 消费者
```java
@Component
public class Consumer {

    @JmsListener(destination = "test.queue", containerFactory = "queueListenerFactory")
    public void queueConsume(String text) {
        System.out.println("接收到queue的消息 : " + text);
    }

    @JmsListener(destination = "test.topic", containerFactory = "topicListenerFactory")
    public void topicConsume1(String text) {
        System.out.println("消费者1 接收到queue的消息 : " + text);
    }

    @JmsListener(destination = "test.topic", containerFactory = "topicListenerFactory")
    public void topicConsume2(String text) {
        System.out.println("消费者2 接收到queue的消息 : " + text);
    }
}
```


### 参考文档
1. [SpringBoot集成ActiveMQ实例详解](https://www.cnblogs.com/secbro/p/13521394.html)