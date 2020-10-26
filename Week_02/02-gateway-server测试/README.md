## 作业2. 使用压测工具(wrk或sb)，演练gateway-server-0.0.1-SNAPSHOT.jar 示例。


|GC类别  | JVM堆大小  | RPS         | 
|------ | ----      | ----        |  
|串行    |128m       |41566.23     |
|串行    |512m       |37668.79     |
|串行    |1g         |45592.67     |
|串行    |4g         |39415.32     |
|并行    |128m       |39107.21     |
|并行    |512m       |44234.18     |
|并行    |1g         |44551.65     |
|并行    |4g         |38559.48     |
|CMS    |128m       |33964.12     |
|CMS    |512m       |43031.65     |
|CMS    |1g         |43211.31     |
|CMS    |4g         |37495.08     |
|G1     |128m       |42465.93     |
|G1     |512m       |39219.79     |
|G1     |1g         |40370.64     |
|G1     |4g         |39904.20     |



## 测试数据
### 串行GC

```
java -jar -XX:+UseSerialGC -Xms128m -Xmx128m gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    19.21ms   66.05ms 599.84ms   92.81%
    Req/Sec     5.61k     1.98k    9.54k    80.22%
  2498143 requests in 1.00m, 298.25MB read
Requests/sec:  41566.23
Transfer/sec:      4.96MB
```

```
java -jar -XX:+UseSerialGC -Xms512m -Xmx512m gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    27.36ms  116.20ms   1.20s    94.57%
    Req/Sec     5.35k     2.18k   11.52k    68.22%
  2262549 requests in 1.00m, 270.13MB read
Requests/sec:  37668.79
Transfer/sec:      4.50MB
```


```
java -jar -XX:+UseSerialGC -Xms1g -Xmx1g gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     9.71ms   40.19ms 357.39ms   94.85%
    Req/Sec     6.30k     2.29k   19.98k    77.03%
  2740030 requests in 1.00m, 327.13MB read
Requests/sec:  45592.67
Transfer/sec:      5.44MB
```

```
java -jar -XX:+UseSerialGC -Xms4g -Xmx4g gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    19.24ms   73.44ms 760.75ms   93.88%
    Req/Sec     5.35k     2.48k   11.55k    63.57%
  2368832 requests in 1.00m, 282.82MB read
Requests/sec:  39415.32
Transfer/sec:      4.71MB
```

### 并行 GC
```
java -jar -Xms128m -Xmx128m gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     3.51ms   19.47ms 486.33ms   97.00%
    Req/Sec     6.23k     2.28k   23.62k    80.19%
  2350013 requests in 1.00m, 280.57MB read
Requests/sec:  39107.21
Transfer/sec:      4.67MB
```

```
java -jar -Xms512m -Xmx512m gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    18.26ms   71.02ms 820.88ms   93.99%
    Req/Sec     6.21k     2.38k   18.34k    78.50%
  2657738 requests in 1.00m, 317.31MB read
Requests/sec:  44234.18
Transfer/sec:      5.28MB
```


```
java -jar -Xms1g -Xmx1g gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    40.39ms  192.79ms   1.83s    95.17%
    Req/Sec     7.77k     1.39k   11.24k    90.59%
  2677664 requests in 1.00m, 319.69MB read
Requests/sec:  44551.65
Transfer/sec:      5.32MB
```

```
java -jar -Xms4g -Xmx4g gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    42.40ms  132.05ms 973.80ms   91.50%
    Req/Sec     5.47k     2.32k   12.00k    72.58%
  2317155 requests in 1.00m, 276.65MB read
Requests/sec:  38559.48
Transfer/sec:      4.60MB
```

### CMS GC
```
java -jar -XX:+UseConcMarkSweepGC -Xms128m -Xmx128m gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     9.73ms   51.79ms 676.18ms   96.73%
    Req/Sec     5.80k     2.17k   13.21k    79.77%
  2040392 requests in 1.00m, 243.60MB read
Requests/sec:  33964.12
Transfer/sec:      4.05MB
```

```
java -jar -XX:+UseConcMarkSweepGC -Xms512m -Xmx512m gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    12.99ms   48.30ms 519.26ms   93.92%
    Req/Sec     5.83k     2.34k   11.05k    77.44%
  2584508 requests in 1.00m, 308.57MB read
Requests/sec:  43031.65
Transfer/sec:      5.14MB
```


```
java -jar -XX:+UseConcMarkSweepGC -Xms1g -Xmx1g gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    13.38ms   48.61ms 419.08ms   93.65%
    Req/Sec     6.08k     2.27k   13.87k    78.64%
  2595407 requests in 1.00m, 309.87MB read
Requests/sec:  43211.31
Transfer/sec:      5.16MB
```

```
java -jar -XX:+UseConcMarkSweepGC -Xms4g -Xmx4g gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency     4.21ms   21.10ms 429.33ms   96.47%
    Req/Sec     6.10k     2.32k   12.16k    76.62%
  2252000 requests in 1.00m, 268.87MB read
Requests/sec:  37495.08
Transfer/sec:      4.48MB
```

### G1 GC
```
java -jar -XX:+UseG1GC -Xms128m -Xmx128m gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    14.27ms   48.17ms 387.21ms   93.13%
    Req/Sec     5.63k     2.17k   14.18k    78.21%
  2551701 requests in 1.00m, 304.65MB read
Requests/sec:  42465.93
Transfer/sec:      5.07MB
```

```
java -jar -XX:+UseG1GC -Xms512m -Xmx512m gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    13.12ms   56.82ms 600.78ms   95.09%
    Req/Sec     5.80k     2.06k   11.59k    76.57%
  2356011 requests in 1.00m, 281.28MB read
Requests/sec:  39219.79
Transfer/sec:      4.68MB
```


```
java -jar -XX:+UseG1GC -Xms1g -Xmx1g gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    34.22ms  125.33ms   1.18s    93.36%
    Req/Sec     5.58k     2.28k   16.45k    75.61%
  2425914 requests in 1.00m, 289.63MB read
Requests/sec:  40370.64
Transfer/sec:      4.82MB
```

```
java -jar -XX:+UseG1GC -Xms4g -Xmx4g gateway-server-0.0.1-SNAPSHOT.jar

# 压测结果
wrk -t8 -c40 -d60s http://localhost:8088/api/hello
Running 1m test @ http://localhost:8088/api/hello
  8 threads and 40 connections
  Thread Stats   Avg      Stdev     Max   +/- Stdev
    Latency    14.82ms   58.11ms 674.58ms   94.26%
    Req/Sec     5.79k     2.33k   13.82k    76.47%
  2398142 requests in 1.00m, 286.31MB read
Requests/sec:  39904.20
Transfer/sec:      4.76MB
```



