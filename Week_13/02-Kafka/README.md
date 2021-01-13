### 作业要求：（必做）搭建一个 3 节点 Kafka 集群，测试功能和性能；实现 spring kafka 下对 kafka 集群的操作，将代码提交到 github。

## 搭建Kafka集群

> 搭建环境：阿里云+docker

### 一、安装zookeeper
```sh
~# docker pull zookeeper:3.6.2
~# docker run -d --name zookeeper3.6.2 -p 2181:2181 zookeeper:3.6.2
~# docker inspect zookeeper3.6.2 #查看IP地址
~# ...
"IPAddress": "172.17.0.11"
...
```

### 二、单节点Kafka
**1、创建Kafka容器**

```sh
~# docker run -d --name kafka0 -p 9092:9092 -e KAFKA_BROKER_ID=0 -e KAFKA_ZOOKEEPER_CONNECT=172.17.0.2:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://172.17.0.3:9092 -e KAFKA_LISTENERS=PLAINTEXT://172.17.0.3:9092 -e  KAFKA_HEAP_OPTS="-Xms256M -Xmx256M"  -t wurstmeister/kafka
```

**2、功能性测试**

```sh
~# docker exec -it kafka0 /bin/bash
bash-4.4# cd /opt/kafka_2.13-2.7.0
bash-4.4# bin/kafka-topics.sh --zookeeper 172.17.0.2:2181 --list
bash-4.4# bin/kafka-topics.sh --zookeeper 172.17.0.2:2181 --create --topic testk --partitions 4 --replication-factor 1
Created topic testk.

bash-4.4# bin/kafka-topics.sh --zookeeper 172.17.0.2:2181 --describe --topic testk
Topic: testk	PartitionCount: 4	ReplicationFactor: 1	Configs: 
	Topic: testk	Partition: 0	Leader: 0	Replicas: 0	Isr: 0
	Topic: testk	Partition: 1	Leader: 0	Replicas: 0	Isr: 0
	Topic: testk	Partition: 2	Leader: 0	Replicas: 0	Isr: 0
	Topic: testk	Partition: 3	Leader: 0	Replicas: 0	Isr: 0
```

**生产者**

```
bash-4.4# bin/kafka-console-producer.sh --bootstrap-server 172.17.0.3:9092 --topic testk
>hello, I am Jenson
```

**消费者**

```sh
bash-4.4# bin/kafka-console-consumer.sh --bootstrap-server 172.17.0.3:9092 --from-beginning --topic testk
hello, I am Jenson
```

**3、简单性能测试**
```sh
bash-4.4# bin/kafka-producer-perf-test.sh --topic testk --num-records 100000 --record-size 1000 --throughput 100000 --producer-props bootstrap.servers=172.17.0.3:9092
100000 records sent, 21344.717182 records/sec (20.36 MB/sec), 988.85 ms avg latency, 1550.00 ms max latency, 1018 ms 50th, 1432 ms 95th, 1513 ms 99th, 1548 ms 99.9th.


bash-4.4# bin/kafka-consumer-perf-test.sh --bootstrap-server 172.17.0.3:9092 --topic testk --fetch-size 1048576 --messages 100000 --threads 1
start.time, end.time, data.consumed.in.MB, MB.sec, data.consumed.in.nMsg, nMsg.sec, rebalance.time.ms, fetch.time.ms, fetch.MB.sec, fetch.nMsg.sec
2021-01-13 07:14:26:734, 2021-01-13 07:14:29:343, 95.3703, 36.5544, 100010, 38332.6945, 1610522067734, -1610522065125, -0.0000, -0.0001
```

消费参数：
1. --num-records 测试数量
2. --record-size 每条记录大小（字节）
3. --throughput 流控每秒测试数量


### 三、3节点Kafka集群

#### 创建Kafka节点

需要注意的有：
1. 执行前清理掉zk上的所有数据，可以删除zk的本地文件或者用ZooInspector操作 
2. KAFKA_BROKER_ID和监听端口需要不同
3. KAFKA_ADVERTISED_LISTENERS需要使用本地的IPV4地址

```sh
docker run -d --name kafka1 -p 9093:9093 -e KAFKA_BROKER_ID=1 -e KAFKA_ZOOKEEPER_CONNECT=172.17.0.2:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://172.19.36.190:9093 -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9093 -e KAFKA_NUM_PARTITIONS=3 -e KAFKA_DEFAULT_REPLICATION_FACTOR=2 -t wurstmeister/kafka

docker run -d --name kafka2 -p 9094:9094 -e KAFKA_BROKER_ID=2 -e KAFKA_ZOOKEEPER_CONNECT=172.17.0.2:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://172.19.36.190:9094 -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9094 -e KAFKA_NUM_PARTITIONS=3 -e KAFKA_DEFAULT_REPLICATION_FACTOR=2 -t wurstmeister/kafka

docker run -d --name kafka3 -p 9095:9095 -e KAFKA_BROKER_ID=3 -e KAFKA_ZOOKEEPER_CONNECT=172.17.0.2:2181 -e KAFKA_ADVERTISED_LISTENERS=PLAINTEXT://172.19.36.190:9095 -e KAFKA_LISTENERS=PLAINTEXT://0.0.0.0:9095 -e KAFKA_NUM_PARTITIONS=3 -e KAFKA_DEFAULT_REPLICATION_FACTOR=2 -t wurstmeister/kafka
```

### 测试集群功能和性能

#### 1、执行操作测试
```sh
# 创建带有副本的topic：
~# docker exec -it kafka1 /bin/bash
bash-4.4# cd /opt/kafka
bash-4.4# bin/kafka-topics.sh --zookeeper 172.17.0.2:2181 --create --topic test32 --partitions 3 --replication-factor 2
Created topic test32.

# 查看集群状态
bash-4.4# bin/kafka-topics.sh --zookeeper 172.17.0.2:2181 --describe --topic test32
Topic: test32	PartitionCount: 3	ReplicationFactor: 2	Configs: 
	Topic: test32	Partition: 0	Leader: 2	Replicas: 2,1	Isr: 2,1
	Topic: test32	Partition: 1	Leader: 3	Replicas: 3,2	Isr: 3,2
	Topic: test32	Partition: 2	Leader: 1	Replicas: 1,3	Isr: 1,3
```

**生产者**
```
bash-4.4# bin/kafka-console-producer.sh --bootstrap-server 172.17.0.3:9093,172.17.0.4:9094,172.17.0.5:9095 --topic test32
> Hello，Kafka cluster. I am Jenson.
```

**消费者**
```
bash-4.4# bin/kafka-console-consumer.sh --bootstrap-server 172.17.0.3:9093,172.17.0.4:9094,172.17.0.5:9095 --from-beginning --topic test32
Hello，Kafka cluster. I am Jenson.
```

### 四、Spring Kafka操作

#### 1、添加Kafka依赖
```xml
	<dependency>
            <groupId>org.springframework.kafka</groupId>
            <artifactId>spring-kafka</artifactId>
        </dependency>
```

#### 2、添加Kakfa集群配置
application.properties文件中增加配置
```
spring.kafka.bootstrap-servers=172.19.36.190:9093,172.19.36.190:9094,172.19.36.190:9095

spring.kafka.producer.retries=0
spring.kafka.producer.batch-size=16384
spring.kafka.producer.buffer-memory=33554432
spring.kafka.producer.key-serializer=org.apache.kafka.common.serialization.StringSerializer
spring.kafka.producer.value-serializer=org.apache.kafka.common.serialization.StringSerializer

spring.kafka.consumer.auto-offset-reset=latest
spring.kafka.consumer.enable-auto-commit=true
spring.kafka.consumer.auto-commit-interval=100
```

#### 3、生产者代码
```java
@Component
public class KafkaProducer {
    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    private Gson gson = new GsonBuilder().create();

    public void produce() {
        Message message = new Message();
        message.setId(System.currentTimeMillis());
        message.setMessage(UUID.randomUUID().toString());
        message.setSendTime(new Date());
        System.out.println("KafkaProducer message = " + gson.toJson(message));
        kafkaTemplate.send("test32", "jenson", gson.toJson(message));
    }
}
```


#### 4、消费者代码
```java
@Component
public class KafkaConsumer {

    @KafkaListener(topics = {"test32"}, groupId = "test")
    public void consume(ConsumerRecord<?, ?> record) {
        Optional<?> message = Optional.ofNullable(record.value());
        if (message.isPresent()) {
            System.out.println("KafkaConsumer receiver record = " + record);
            System.out.println("KafkaConsumer receiver message = " + message.get());
        }
    }
}
```

### 参考文献
1. [用 Docker 快速搭建 Kafka 集群](https://segmentfault.com/a/1190000022988499)
