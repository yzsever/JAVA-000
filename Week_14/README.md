### Week14 作业题目：

#### 周四作业：
1. （选做）自己安装和操作 RabbitMQ，RocketMQ，Pulsar，以及 Camel 和 Spring Integration。
2. （必做）思考和设计自定义MQ第二个版本或第三个版本，写代码实现其中至少一个功能点，把设计思路和实现代码，提交到 GitHub。
   1. 第一个版本-内存Queue1、基于内存Queue实现生产和消费API（已经完成）
      - 1）创建内存Queue，作为底层消息存储
      - 2）定义Topic，支持多个Topic
      - 3）定义Producer，支持Send消息
      - 4）定义Consumer，支持Poll消息
    2. 第二个版本：自定义Queue2、去掉内存Queue，设计自定义Queue，实现消息确认和消费offset
      - 1）自定义内存Message数组模拟Queue。
      - 2）使用指针记录当前消息写入位置。
      - 3）对于每个命名消费者，用指针记录消费位置。
    3. 第三个版本：基于SpringMVC实现MQServer3、拆分broker和client(包括producer和consumer)
      - 1）将Queue保存到web server端
      - 2）设计消息读写API接口，确认接口，提交offset接口
      - 3）producer和consumer通过httpclient访问Queue
      - 4）实现消息确认，offset提交
      - 5）实现consumer从offset增量拉取
    4. 第四个版本：功能完善MQ4、增加多种策略（各条之间没有关系，可以任意选择实现）
      - 1）考虑实现消息过期，消息重试，消息定时投递等策略
      - 2）考虑批量操作，包括读写，可以打包和压缩
      - 3）考虑消息清理策略，包括定时清理，按容量清理等
      - 4）考虑消息持久化，存入数据库，或WAL日志文件，或BookKeeper
      - 5）考虑将spring mvc替换成netty下的tcp传输协议
    5. 第五个版本：体系完善MQ5、对接各种技术（各条之间没有关系，可以任意选择实现）
      - 1）考虑封装 JMS 1.1 接口规范
      - 2）考虑实现 STOMP 消息规范
      - 3）考虑实现消息事务机制与事务管理器
      - 4）对接Spring
      - 5）对接Camel或Spring Integration
      - 6）优化内存和磁盘的使用
3. （挑战☆☆☆☆☆）完成所有其他版本的要求。期限一年。

#### 周六作业：
1. （选做）思考一下自己负责的系统，或者做过的系统，能否描述清楚其架构。
2. （选做）考虑一下，如果让你做一个针对双十一，某东某宝半价抢 100 个 IPhone 的活动系统，你该如何考虑，从什么地方入手。
3. （选做）可以自行学习以下参考书的一两本。推荐架构书籍：
   - 《软件架构》Mourad Chabane Oussalah
   - 《架构实战 - 软件架构设计的过程》Peter EeLes
   - 《软件系统架构 - 使用视点和视角与利益相关者合作》Nick Rozanski
   - 《企业 IT 架构转型之道》
   - 《大型网站技术架构演进与性能优化》
   - 《银行信息系统架构》
   - 《商业银行分布式架构实践》