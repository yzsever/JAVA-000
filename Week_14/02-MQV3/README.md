### 题目要求

第三个版本：基于SpringMVC实现MQServer3、拆分broker和client(包括producer和consumer)
   - 1）将Queue保存到web server端
   - 2）设计消息读写API接口，确认接口，提交offset接口
   - 3）producer和consumer通过httpclient访问Queue
   - 4）实现消息确认，offset提交
   - 5）实现consumer从offset增量拉取