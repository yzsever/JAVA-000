## 配置redis的主从复制

> 环境为阿里云 CentOS Linux release 8.3.2011

### redis的主从复制
#### 1、实现核心步骤
redis的主从复制实现非常的简单，极简的风格, 从节点执行:
```
> SLAVEOF 127.0.0.1 6379
```
也可以在配置文件中设置。
> 注意：从节点只读、异步复制。


#### 2、使用docker搭建主从的redis环境
根据上节课老师推荐的redis安装方式：
```
$ docker run -p 6379:6379 --name redis01 -v /etc/redis/redis.conf:/etc/redis/redis.conf -v /etc/redis/data:/data -d redis redis-server /etc/redis/redis.conf --appendonly yes
```
让我们来解析一下这条命令：
> docker run [OPTIONS] IMAGE [COMMAND] [ARG...]
1. docker run : 创建一个容器，然后启动它
2. -p 6379:6379 : 发布容器的6379端口到宿主机的6379端口(前一个6379)。
3. --name redis01 : 为容器分配一个名称redis01 
4. -v /etc/redis/redis.conf:/etc/redis/redis.conf : -v和选项--volume相同，绑定挂载卷，将主机的/etc/redis/redis.conf(前一个)安装到容器中的/etc/redis/redis.conf。
5. -v /etc/redis/data:/data : 将主机的/etc/redis/data安装到容器中的/data
6. -d 同选项--detach 在后台运行容器并打印容器ID
7. redis 使用的镜象名称(IMAGE)
7. redis-server /etc/redis/redis.conf --appendonly yes: 在容器启动后执行的命令:指定配置文件启动redis-server进程，开启数据持久化

理解完整条命令以后，我们自然发现几个问题：
1. 主机中的redis.conf并没有
2. /etc/redis/data当多个redis需要区分目录


#### 3、配置redis.conf
获取redis.conf的方式：
```
$ cd ~
$ mkdir redis
$ cd redis
$ wget http://download.redis.io/redis-stable/redis.conf
$ cp redis.conf redis1.conf
$ cp redis.conf redis2.conf
```

下载好管网提供的配置文件后，复制两份出来作为主从redis的配置文件。

**primary配置**
```
# 注释这一行，表示Redis可以接受任意ip的连接
# bind 127.0.0.1 

# 当客户端需要从其他host访问的时候，需要关闭保护模式
protected-mode no 

```


**replica配置**
```
# 注释这一行，表示Redis可以接受任意ip的连接
# bind 127.0.0.1 

# 当客户端需要从其他host访问的时候，需要关闭保护模式
protected-mode no 

```

这里我们主从redis的配置一致，将使用命令SLAVEOF的方式开启主从复制。

#### 4、创建主从redis容器实例
**primary容器**
```
$ docker run -p 6380:6379 --name redis1 -v /root/redis/redis1.conf:/etc/redis/redis.conf -v /etc/redis/data1:/data -d redis redis-server /etc/redis/redis.conf --appendonly yes
```

**replica容器**
```
$ docker run -p 6381:6379 --name redis2 -v /root/redis/redis2.conf:/etc/redis/redis.conf -v /etc/redis/data2:/data -d redis redis-server /etc/redis/redis.conf --appendonly yes
```

查看一下：
```
$ docker ps
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                               NAMES
145b5cf0be0e        redis               "docker-entrypoint.s…"   4 seconds ago       Up 3 seconds        0.0.0.0:6381->6379/tcp              redis2
4ff66c1b0f36        redis               "docker-entrypoint.s…"   38 seconds ago      Up 37 seconds       0.0.0.0:6380->6379/tcp              redis1
```

查看主redis的容器IP：
```
$ docker inspect 4ff | grep IPAddress
            "SecondaryIPAddresses": null,
            "IPAddress": "172.17.0.5",
                    "IPAddress": "172.17.0.5",

```

进入从redis开始主从复制功能：
```
$ docker exec -it 145 /bin/bash
redis2$ redis-cli
127.0.0.1:6379> info
...
# Replication
role:master
connected_slaves:0
master_replid:32b30f3d7e5e1c8debe150fd7cf5d26d87385ae6
master_replid2:0000000000000000000000000000000000000000
...
127.0.0.1:6379> slaveof 172.17.0.5 6379
OK
127.0.0.1:6379> info
...
# Replication
role:slave
master_host:172.17.0.5
master_port:6379
master_link_status:up
master_last_io_seconds_ago:2
master_sync_in_progress:0
slave_repl_offset:28
slave_priority:100
slave_read_only:1
connected_slaves:0
...
```

#### 5、主从复制验证
进入主redis，设置一些数据：
```
$ docker exec -it 4ff /bin/bash
redis1$ redis-cli
127.0.0.1:6379> set key1 1
OK
127.0.0.1:6379> set key2 2
OK
127.0.0.1:6379> set key3 3
OK

```

进入从redis，查看同步数据
```
$ docker exec -it 145 /bin/bash
redis1$ redis-cli
127.0.0.1:6379> keys *
1) "key2"
2) "key3"
3) "key1"
127.0.0.1:6379> get key1
"1"
```

### conf配置的方式
修改redis2.conf
```
#replicaof <masterip> <masterport>
replicaof 172.17.0.5 6379
```

### 参考文档
1. https://www.cnblogs.com/fan-gx/p/11463400.html
2. [docker run使用手册](https://docs.docker.com/engine/reference/commandline/run/#options)



