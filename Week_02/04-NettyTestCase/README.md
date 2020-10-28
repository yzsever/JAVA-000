### 作业4:(可选)运行课上的例子，以及 Netty 的例子，分析相关现象。

#### 单线程
```
wrk -t8 -c40 -d60s http://localhost:8801
Running 1m test @ http://localhost:8801
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   921.68ms  123.54ms   1.01s    95.19%
    Req/Sec     3.94      5.35    30.00     90.84%
  478 requests in 1.00m, 77.62KB read
  Socket errors: connect 0, read 2545, write 8, timeout 0
Requests/sec:      7.96
Transfer/sec:      1.29KB
```

#### 多线程
```
wrk -t8 -c40 -d60s http://localhost:8802
Running 1m test @ http://localhost:8802
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    26.13ms   10.50ms  98.90ms   96.77%
    Req/Sec     5.57      7.67    80.00     92.12%
  1206 requests in 1.00m, 1.61MB read
  Socket errors: connect 0, read 93457, write 17, timeout 0
Requests/sec:     20.07
Transfer/sec:     27.37KB
```

#### 线程池
```
wrk -t8 -c40 -d60s http://localhost:8803
Running 1m test @ http://localhost:8803
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    31.53ms   22.76ms 114.76ms   89.24%
    Req/Sec     3.77      8.65    60.00     93.24%
  344 requests in 1.00m, 1.48MB read
  Socket errors: connect 0, read 93466, write 6, timeout 0
Requests/sec:      5.72
Transfer/sec:     25.15KB
```

#### Netty
```
wrk -t8 -c40 -d60s http://localhost:8808/test
Running 1m test @ http://localhost:8808/test
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency   802.71us    6.73ms 250.03ms   99.49%
    Req/Sec    12.22k     1.17k   24.70k    96.53%
  5826961 requests in 1.00m, 605.72MB read
Requests/sec:  97048.35
Transfer/sec:     10.09MB
```

### 分析
使用了老师提供的最新的代码进行测试，但是测试结果不理想。
Netty的RPS远远优于其他，线程池由于单线程，理论上线程池减少了多线程的资源抢占性能应该高于多线程，但是实际结果没有体现。