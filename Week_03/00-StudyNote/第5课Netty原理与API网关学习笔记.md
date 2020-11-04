## 学习笔记
## 第5课 Netty原理与API网关
### 什么是高性能?

1. 高并发用户(Concurrent Users) 
   - 系统外部角度， 业务指标
   - wrk [-c 40]
2. 高吞吐量(Throughout)
   - 系统内部角度 技术指标
   - QPS 请求数  wrk：Requests/sec
   - TPS 交易数  Transaction/sec
3. 低延迟(Latency)
   - wrk：Latency Distribution
   -《数据密集型应用系统架构》 
      - 这里指的是：**响应时间** ：（针对客户/调用者） = 数据回来t2 - 请求发送时间t1
      - 延迟：（系统内） 请求出系统时间t4 - 请求进系统时间t3
   - 百分比的衡量方式（看毛刺，平稳时看P90、P99）

#### 高性能的副作用
1. 系统复杂度*10以上
   - 并发量不同时，系统完全是两个东西
2. 建设与维护成本++++
   - 复杂度高了，改东西影响的范围就大了
3. 故障或BUG导致的破坏性*10以上
   - 出问题时有更可怕的后果，修改一个小功能可能影响很多的用户

#### 应对策略
**稳定性建设（混沌工程）Netflix：**
1. 容量
   - 压测、监控确定系统的并发数和吞吐量
   - 国内线上实际的TPS是多少
      - 一天有86400s
      - 如果TPS是1000， 订单为 86 400 000 = 8640w
      - 淘宝每天有3000w订单，滴滴美团2000w
      - 淘宝每天的并发量为 3000/8640*1000=347。如果归到8小时：TPS=347*24/8=1041。
      - 聚划算秒杀，618，双十一的并发量是最大的。19年双十一第一分钟支付宝TPS=45w/s
   - 互联网要面对的问题：活动的几天的并发量会特别高。
      - 最大的三家云服务厂商：Amazon、MicroSoft、Alibaba
      - 电商为了应对活动需要5-10倍的机器
2. 爆炸半径
   - 系统变动就会影响现有系统：如上线、服务迁移、动数据库等
   - 让影响范围控制在一定范围内
      - 微服务
3. 工程方面积累与改进
   - 系统出问题三大原因
      - 天灾：光缆被挖断了
      - 人祸：操作人员未按流程来执行
      - 出了超出以前认知的BUG
---

### Netty如何实现高性能
#### Netty是一种网络应用开发框架
1. 异步
2. 事件驱动：让模块解耦
3. 基于NIO：让IO操作性能高

#### Netty是抽像统一的网络编程框架，适用于:
1. 服务端
2. 客户端
3. TCP/UDP：网络协议编程模型的统一
> 使不使用NIO只能配置参数，服务端和客户端的代码也是统一的

#### 事件处理的机制
1. 请求的事件（Event）先进一个队列（Event Queue），防止处理不过来
2. 事件的分发器（Event Mediator）分发到事件管道中（Event Channel）
3. 管道分给不同的事件处理器（Event Processor）调用module进行处理

#### Reactor模型
1. Service Handler 是为了Hold住大量的请求，保证高并发。将输入的请求多路复用的分发给相应的EventHandler。

#### Netty NIO模型
1.  核心处理部分和Reactor模型一样
   - Boss EventLoopGroup 老板负责和客户谈、接任务
   - Worker EventLoopGroup 处理老板分发的任务
2. EventHandler有decode、compute和encode三个过程

#### Netty 运行原理
1. Channel 管道+过滤器的模型
   - PipeLine 是任务的处理过程
   - 设计目的：1. 高性能 2. 灵活性
      - 不同协议的处理过程不一样
      - 不同协议可以使用不同的Cannel Handler
2. IO处理和业务处理（Executor Group异步）分隔开
3. EventLoop 单线程轮询

#### 关键对象
1. Bootstrap: 启动线程，开启socket
   - ServerBootstrap：启动服务器，需要listen
   - Bootstrap：启动客户端
2. EventLoopGroup：线程池
3. EventLoop：线程
4. SocketChannel：连接
5. ChannelInitializer：初始化。绑定处理器链和处理器
6. ChannelPipeline：处理器链
   - 数据是有方向的
   - ChannelInboundHandler：处理加工请求数据（Request）
   - ChannelOutboundHandler：处理加工响应数据（Response）
   - Server和Client的Inbound和OutBound是反的
7. ChannelHandler：处理器

#### Netty应用组成
1. 网络事件
   1. 入站事件
      - 通道激活和停用
      - 读操作事件
      - 异常事件
   2. 出站事件
      - 打开连接
      - 关闭连接
      - 写入数据
      - 刷新数据
2. 应用程序逻辑事件
3. 事件处理程序
   1. 事件处理程序接口
      - ChannelHandler
      - ChannelOutboundHandler
      - ChannelInboundHandler
   2. 适配器(空实现,需要继承使用):
      - ChannelInboundHandlerAdapter
      - ChannelOutboundHandlerAdapter

---
### Netty应用的优化
#### 粘包和拆包的问题
- TCP上层的问题，TCP是一个稳定的长连接的协议，不存在粘包和拆包的问题
- 服务端和客户端都有缓冲区，两个应用相互发数据
- 粘包是业务将不同Packet数据放到一起发送，都是人为的问题
- 通信需要保证数据完整，数据顺序正确

如何解决粘包与拆包的问题？规范好数据。ByteToMessageDecoder 提供的一些常见的解码器实现类:
1. FixedLengthFrameDecoder:定长协议解码器,我们可以指定固定的字节数算一个完整的报文
2. LineBasedFrameDecoder:行分隔符解码器,遇到\n 或者\r\n,则认为是一个完整的报文
3. DelimiterBasedFrameDecoder:分隔符解码器,分隔符可以自己指定
4. LengthFieldBasedFrameDecoder:长度编码解码器,将报文划分为报文头/报文体
   - 如Netty变长的变码器，Chunk
5. JsonObjectDecoder:json 格式解码器,当检测到匹配数量的“{” 、”}”或”[””]”时,则认为是一个完整的 json 对象或者 json 数组

#### HTTP断点续传的问题
如果不能断点续传：
1. 浪费带宽和时间等资源
2. 没法使用多线程请求
请求内存包含请求大小和请求数据Range

#### Nagle 与 TCP_NODELAY
1. 数据传输限制：
   - MTU: Maxitum Transmission Unit 最大传输单元：TCP：1500 Byte
   - MSS: Maxitum Segment Size 最大分段大小：TCP：1460 Byte
   - MTU = IP头（20）+TCP头（20）+MSS
2. 网络拥堵与 Nagle 算法优化：就发给网络
调用send和recv命令都不是把数据给网卡发出去，只是把数据给操作系统的缓冲区。Nagle 算法优化达到触发条件后，将系统缓冲区内的数据发送给网络 
   - 缓冲区满
   - 达到超时(200ms)
3. TCP_NODELAY
如果每次的传输数据很小的时候且不频繁时，则只能等到超时才发送，性能就低。可以使用TCP_NODELAY关闭Nagle算法优化。

#### 连接优化
TCP建立连接三次握手建立连接
- 发生了SYN和ACK两件事
- 客户端SYN问服务端你在不在
- 服务端ACK回答我在，再SYN问客户端在不在
- 客户端ACK回答我在并设置socket为可用状态
- 服务端收到ACK设置socket为可用状态

TCP断开连接
- IP:PORT
- TIME-WAIT占用端口的，等待2MSL后才真正释放掉
   - Linux：MSL 2min
   - Windows：MSL 1min
TIME-WAIT占用端口时间很长，该如何优化呢？
   - 降低等待的周期MSL：如30ms
   - 打开端口复用参数

#### Netty 优化
1. 不要阻塞 EventLoop
   - 单线程
   - sync
2. 系统参数优化
   - ulimit -a  linux上一切皆文件，将单进程能够使用的文件调到足够大 
   - 调/proc/sys/net/ipv4/tcp_fin_timeout(linux), TcpTimedWaitDelay(windows)参数, 降低端口占用时间
3. 缓冲区优化
   - SO_RCVBUF/SO_SNDBUF/SO_BACKLOG/ REUSEXXX
   - 接受方的缓冲区可调，太小（阻塞或丢弃）
   - 发送方的缓冲区可调
   - 保持连接状态的个数可配置
   - 重用端口地址
4. 心跳频率周期优化
   - 心跳机制（探测连接是否断了）与断线重连
   - 复用当前真实的网络数据的传输
   - 和数据库连接池相关，开始一分钟发给数据库“select 1”， JDBC4开始底层提供isvalid
5. 内存与 ByteBuffer 优化
   - DirectBuffer与HeapBuffer（反复使用）
   - 减少用户态和内核态的数据拷贝开销
6. 其他优化
   - ioRatio: 做io和非io操作的cpu使用比例（1：1）
   - Watermark：[配置参数]缓存区写满的水位，进行调整处理
   - TrafficShaping：[Handler]流控的机制，限流
---

### 4. 典型应用:API 网关
业务直接暴露给客户，有以下风险：
1、安全的风险
2、流量大了，业务系统冲垮的风险
为了抵挡风险，我们需要增加保护层：API网关

#### 网关的结构和功能?
四大职能
1. 请求接入：作为所有API接口服务请求的接入口
2. 业务聚合：座位所有后端业务服务的聚合点
3. 中介策略：实现安全、验证、路由、过滤、流控等策略
4. 统一管理：对所有API服务和策略进行统一管理

网关的分类
1. 流量网关：关注稳定与安全 Ngnix
   - 全局性流控
   - 日志统计
   - 防止SQL注入
   - 防止Web攻击
   - 屏蔽工具扫描
   - 黑白IP名单
   - 证书/加解密处理
2. 业务网关：提供更好的服务 Zuul、Zuul2、Spring Cloud Gateway
   - 服务级别流控 
   - 服务降级与熔断
   - 路由与负载均衡、灰度策略
   - 服务过滤、聚合与发现
   - 权限验证与用户等级策略
   - 业务规则与参数校验
   - 多级缓存策略

#### Zuul
Zuul 是 Netflix 开源的 API 网关系统,它的主要设计目标是动态路由、监控、弹性和安全。Zuul 的内部原理可以简单看做是很多不同功能 filter 的集合
- pre 调用业务服务 API 之前的请求处理
- routing 调用业务服务 API 之后的响应处理
- post 直接响应

#### Zuul2
Zuul 2.x 是基于 Netty 内核重构的版本。在Spring Cloud Gateway出来的晚
- Netty Server Handlers
- Netty Clent Handlers

#### Spring Cloud Gateway
Filters设计与Zuul类似；基于Netty
- Pre Filters
- Post Filters
- 基于Spring Webflux

#### 网关对比
1. 流量网关：性能非常好
   - OpenResty
   - Kong
2. 业务网关：扩展性好，二次开发
   - Spring Cloud Gateway
   - Zuul2
---

### 5. 自己动手实现 API 网关

#### 最简单的网关V1.0
1. HTTP Server接收请求 localhost:8888
2. 客户端请求数据（后端服务+请求）
3. 后端服务（服务响应数据）localhost:8801

#### 最简单的网关V2.0
1. HTTP Server接收请求 localhost:8888
2. InBoundFilter
3. 客户端请求数据（后端服务+请求）
4. 后端服务（服务响应数据）localhost:8801
5. OutBoundFilter

#### 最简单的网关V3.0
1. HTTP Server接收请求 localhost:8888
2. InBoundFilter
3. Router
4. 客户端请求数据（后端服务+请求）
5. 后端服务（服务响应数据）localhost:8801
6. OutBoundFilter



