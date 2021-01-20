### 题目要求

第三个版本：基于SpringMVC实现MQServer3、拆分broker和client(包括producer和consumer)
   - 1）将Queue保存到web server端
   - 2）设计消息读写API接口，确认接口，提交offset接口
   - 3）producer和consumer通过httpclient访问Queue
   - 4）实现消息确认，offset提交
   - 5）实现consumer从offset增量拉取


### 功能设计
1. 将Queue保存到web server端
   - 创建MQBroker提供MQ的核心功能
2. 设计消息读写API接口，确认接口，提交offset接口
   - API1：GET消息读   /poll
   - API2：POST消息写   /send
   - API3：POST消息读确认(提交offset) /poll_ack
   - API4：POST消息写确认 /send_ack
3. producer和consumer通过httpclient访问Queue
   - 使用httpclient的GET、POST对API进行调用
4. 实现消息确认，offset提交
   - 使用httpclient对相应API进行调用
5. 实现consumer从offset增量拉取
   - broker端记录每个消费者的offset，poll数据时从offset拉取
