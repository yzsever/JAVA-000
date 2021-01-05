## 配置redis的sentinel高可用

### Sentinel系统简介
Redis 的 Sentinel 系统用于管理多个 Redis 服务器（instance）， 该系统执行以下三个任务：
- 监控（Monitoring）： Sentinel 会不断地检查你的主服务器和从服务器是否运作正常。
- 提醒（Notification）： 当被监控的某个 Redis 服务器出现问题时， Sentinel 可以通过 API 向管理员或者其他应用程序发送通知。
- 自动故障迁移（Automatic failover）： 当一个主服务器不能正常工作时， Sentinel 会开始一次自动故障迁移操作， 它会将失效主服务器的其中一个从服务器升级为新的主服务器， 并让失效主服务器的其他从服务器改为复制新的主服务器； 当客户端试图连接失效的主服务器时， 集群也会向客户端返回新主服务器的地址， 使得集群可以使用新主服务器代替失效服务器。

Redis Sentinel 是一个分布式系统， 你可以在一个架构中运行多个 Sentinel 进程（progress）， 这些进程使用流言协议（gossip protocols)来接收关于主服务器是否下线的信息， 并使用投票协议（agreement protocols）来决定是否执行自动故障迁移， 以及选择哪个从服务器作为新的主服务器。


### 搭建前准备
1、获取并修改配置文件
```
$ wget http://download.redis.io/redis-stable/sentinel.conf
```

sentinel.conf配置：
```
# 第一行配置指示Sentinel去监视一个名为mymaster的主服务器，这个主服务器的IP地址为172.17.0.5(主从复制搭建中的主redis地址)，端口号为6379，而将这个主服务器判断为失效至少需要2个Sentinel同意（只要同意Sentinel的数量不达标，自动故障迁移就不会执行）。
sentinel monitor mymaster 172.17.0.5 6379 2
# down-after-milliseconds 选项指定了 Sentinel 认为服务器已经断线所需的毫秒数。这里指定了60s。
sentinel down-after-milliseconds mymaster 60000
# failover-timeout指定故障转移超时（以毫秒为单位）。
sentinel failover-timeout mymaster 180000
# parallel-syncs 选项指定了在执行故障转移时，最多可以有多少个从服务器同时对新的主服务器进行同步，这个数字越小，完成故障转移所需的时间就越长。这里指定了一个。
sentinel parallel-syncs mymaster 1
# 配置log日志位置，方便后面分析验证
logfile "/var/log/redis/sentinel.log"
# 守护进程方式运行
daemonize yes
```

> 每个参数配置文件里面都有详细的解释

两种启动方式：
```
$ redis-sentinel sentinel.conf
$ redis-server redis.conf --sentinel
```
这里我们使用第一种方式。

> 不需要配置从节点，也不需要配置其他sentinel信息

### 搭建Sentinel系统
配置文件中，我们设置了主服务器判断为失效至少需要2个Sentinel同意（只要同意Sentinel的数量不达标，自动故障迁移就不会执行）。所以我们通过docker搭建3个哨兵的环境。

**哨兵1搭建**
```
$ docker run -it --name sentinel1 -p 26379:26379 -v /root/redis/sentinel.conf:/etc/redis/sentinel.conf -d redis
# 进入容器配置log文件并启动
$ docker exec -it sentinel1 /bin/bash
sentinel1$ mkdir /var/log/redis
sentinel1$ touch /var/log/redis/sentinel.log
sentinel1$ redis-sentinel /etc/redis/sentinel.conf
# 查看log日志
sentinel1$ tail -100f /var/log/redis/sentinel.log
```

通过日志可以看到哨兵监控到了1主1从的信息
```
15:X 05 Jan 2021 09:36:31.924 # Sentinel ID is 8a130773515d72b75803a312413b57195238dc1a
15:X 05 Jan 2021 09:36:31.924 # +monitor master mymaster 172.17.0.5 6379 quorum 2
15:X 05 Jan 2021 09:36:31.925 * +slave slave 172.17.0.6:6379 172.17.0.6 6379 @ mymaster 172.17.0.5 6379
```

**哨兵2搭建**
```
$ docker run -it --name sentinel2 -p 26380:26379 -v /root/redis/sentinel.conf:/etc/redis/sentinel.conf -d redis /bin/bash
# 其他步骤与哨兵1相同
```

**哨兵3搭建**
```
$ docker run -it --name sentinel3 -p 26381:26379 -v /root/redis/sentinel.conf:/etc/redis/sentinel.conf -d redis /bin/bash
# 其他步骤与哨兵1相同
```

### 系统功能验证
1、我们手动进入主redis1，执行shutdown的操作。等待60s后，哨兵进行投票，执行故障转移操作。
```
15:X 05 Jan 2021 09:43:51.329 # +sdown master mymaster 172.17.0.5 6379
15:X 05 Jan 2021 09:43:51.404 # +new-epoch 1
15:X 05 Jan 2021 09:43:51.407 # +vote-for-leader 3891e59a7b14bb281d38ccef10a630a4169b1370 1
15:X 05 Jan 2021 09:43:52.424 # +odown master mymaster 172.17.0.5 6379 #quorum 3/2
15:X 05 Jan 2021 09:43:52.424 # Next failover delay: I will not start a failover before Tue Jan  5 09:49:51 2021
15:X 05 Jan 2021 09:43:52.462 # +config-update-from sentinel 3891e59a7b14bb281d38ccef10a630a4169b1370 172.17.0.8 26379 @ mymaster 172.17.0.5 6379
15:X 05 Jan 2021 09:43:52.462 # +switch-master mymaster 172.17.0.5 6379 172.17.0.6 6379
15:X 05 Jan 2021 09:43:52.462 * +slave slave 172.17.0.5:6379 172.17.0.5 6379 @ mymaster 172.17.0.6 6379
```

2、查看从redis2的信息，已变为主redis2
```
$ docker exec -it redis2 /bin/bash
redis2$ redis-cli
127.0.0.1:6379> info replication
# Replication
role:master
connected_slaves:0
master_replid:fc7c5694ab40a54b17c719c8a6e8e2fa65ec06bf
master_replid2:f4f8f1bf0a99632e4b1b24bb968619424db4e400
master_repl_offset:137529
second_repl_offset:115907
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:1
repl_backlog_histlen:137529
```

3、重新启动redis1，再查看信息：redis1已成为从redis
```
$ docker container start redis1
$ docker exec -it redis1 /bin/bash
redis1$ redis-cli
127.0.0.1:6379> info replication
# Replication
role:slave
master_host:172.17.0.6
master_port:6379
master_link_status:up
master_last_io_seconds_ago:0
master_sync_in_progress:0
slave_repl_offset:174235
slave_priority:100
slave_read_only:1
connected_slaves:0
master_replid:fc7c5694ab40a54b17c719c8a6e8e2fa65ec06bf
master_replid2:0000000000000000000000000000000000000000
master_repl_offset:174235
second_repl_offset:-1
repl_backlog_active:1
repl_backlog_size:1048576
repl_backlog_first_byte_offset:160480
repl_backlog_histlen:13756

```

4、redis1启动后哨兵日志:将redis1转换成从redis
```
15:X 05 Jan 2021 09:47:24.930 # -sdown slave 172.17.0.5:6379 172.17.0.5 6379 @ mymaster 172.17.0.6 6379
15:X 05 Jan 2021 09:47:34.932 * +convert-to-slave slave 172.17.0.5:6379 172.17.0.5 6379 @ mymaster 172.17.0.6 6379

```

### 参考文档
1. [redis sentinel原理介绍](http://www.redis.cn/topics/sentinel.html)
2. [redis复制与高可用配置](https://www.cnblogs.com/itzhouq/p/redis5.html)
3. [Docker搭建Redis一主两从三哨兵](https://www.cnblogs.com/fan-gx/p/11463400.html)
