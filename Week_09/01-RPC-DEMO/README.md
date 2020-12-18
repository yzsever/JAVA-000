### 作业要求：（必做）改造自定义 RPC 的程序，提交到 GitHub：
1. 尝试将服务端写死查找接口实现类变成泛型和反射；
2. 尝试将客户端动态代理改成 AOP，添加异常处理；
3. 尝试使用 Netty+HTTP 作为 client 端传输方式。

### 实现思路
1、尝试将服务端写死查找接口实现类变成泛型和反射；
- RpcfxResolver添加泛型的resolve方法
- 通过反射调用service的方法

2、尝试将客户端动态代理改成 AOP，添加异常处理；
- 采用ByteBuddy字节码增强的方式

3、尝试使用 Netty+HTTP 作为 client 端传输方式。
- 通过NettyClient同步通信的方式，等待异步结果返回。
- Response存在NettyHttpClientOutboundHandler中，主线程中保留NettyHttpClientOutboundHandler的实例

### 参考文档
1. [xstream使用手册](http://x-stream.github.io/tutorial.html)
2. [ByteBuddy入门教程](https://zhuanlan.zhihu.com/p/151843984)
3. [ByteBuddy官方文档](https://bytebuddy.net/#/)


