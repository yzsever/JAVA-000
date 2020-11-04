## 作业
按今天的课程要求,实现一个网关,基础代码可以 fork:https://github.com/kimmking/JavaCourseCodes/02nio/nio02 文件夹下实现以后,代码提交到 Github。
1. *周四作业(必做):整合你上次作业的 httpclient/okhttp;
   - 压测：调整NettyServer和HttpClient的参数
   - [作业1目录](https://github.com/yzsever/JAVA-000/tree/main/Week_03/01-Gateway1.0/)
2. 周四作业(可选):使用 netty 实现后端 http 访问(代替上一步骤);
   - 压测：调整NettyServer和NettyClient的参数
   - [作业2目录](https://github.com/yzsever/JAVA-000/tree/main/Week_03/02-Gateway1.0-Netty/)
3. *周六作业（必做）:实现一个request的过滤器filter。
   - filter里把我们请求的http头拿到，然后在里面添加一个新的key-value，key是nio，value是自己的名字拼音。
   - 实际请求后端服务的时候，把这些请求所有的头拿出来，在实际调用后端服务之前全部添加到对后端的请求头里。（我们去调用后端服务的时候，那个请求里面就比网关接入请求多一个我们自定义的头。这样就相当于我们的程序在网关的filter这边做了一个加强）
   - [作业3目录](https://github.com/yzsever/JAVA-000/tree/main/Week_03/03-Gateway1.0-Filter/)
4. 周六作业(可选):实现路由
   - 把现在只代理的一个后端服务，变成了可以代理多个后端服务。我们通过最简的路由算法，比如说随机的对后端的两个服务取一个，然后调用这个服务返回数据。
   - [作业4目录](https://github.com/yzsever/JAVA-000/tree/main/Week_03/04-Gateway1.0-Router/)

之后可以进行更多的扩展：
1. 定义一个过滤响应数据和报文头的resposeFilter
2. 针对用户的过滤可以自定义一个filter
3. 针对安全，可以加密解密token的filter
4. 针对高并发限流，可以调节线程池，也可以加限流算法的filter
5. 针对后端服务的负载均衡，可以加roundribbon或者权重的算法等等

---

### 学习笔记
1. [第5课Netty原理与API网关](https://github.com/yzsever/JAVA-000/tree/main/Week_03/00-StudyNote/第5课Netty原理与API网关学习笔记.md)
2. [第6课Java并发编程-01](https://github.com/yzsever/JAVA-000/tree/main/Week_03/00-StudyNote/第6课Java并发编程-01学习笔记.md)
