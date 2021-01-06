## 配置redis的Cluster集群

### redis集群介绍

主从复制从容量角度来说，还是单机。Redis Cluster真正的走向分片。Redis Cluster通过一致性hash的方式，将数据分散到多个服务器节点：先设计 16384个哈希槽，分配到多台redis-server。当需要在 Redis Cluster中存取一个 key时，Redis 客户端先对 key 使用 crc16 算法计算一个数值，然后对 16384 取模，这样每个key 都会对应一个编号在 0-16383 之间的哈希槽，然后在 此槽对应的节点上操作。可使用cluster-enabled yes方式开启。

注意：
1. 节点间使用gossip通信，规模<1000
2. 默认所有槽位可用，才提供服务
3. 一般会配合主从模式使用


### 搭建Redis Cluster(三主三从)
#### 1、创建属于redis的集群网络
```
root:~# docker network create redis-cluster-net
ca60604793a38f4512501c4283079771cae4e4cffcd459f7a139030b92f871f2
```

查看网关信息：172.18.0.1
```
root:~# docker network inspect redis-cluster-net
[
    {
        "Name": "redis-cluster-net",
        "Id": "ca60604793a38f4512501c4283079771cae4e4cffcd459f7a139030b92f871f2",
        "Created": "2021-01-06T09:50:29.813937802+08:00",
        "Scope": "local",
        "Driver": "bridge",
        "EnableIPv6": false,
        "IPAM": {
            "Driver": "default",
            "Options": {},
            "Config": [
                {
                    "Subnet": "172.18.0.0/16",
                    "Gateway": "172.18.0.1"
                }
            ]
        },
...
```

#### 2、创建redis实例配置文件

redis配置信息模版：redis-cluster.tmpl
```
# 基本配置
## 开放端口
port ${port}
## 不作为守护进程
daemonize no
## 启用aof持久化模式
appendonly yes

# 集群配置
## 开启集群配置
cluster-enabled yes
## 存放集群节点的配置文件 系统自动建立
cluster-config-file nodes-${port}.conf
## 节点连接超时时间
cluster-node-timeout 50000 
## 实际为各节点网卡分配ip
cluster-announce-ip ${ip}
## 节点映射端口
cluster-announce-port ${port}
## 节点总线端口
cluster-announce-bus-port 1${port}
cluster-slave-validity-factor 10
cluster-migration-barrier 1
cluster-require-full-coverage yes
```

create.sh的脚本内容：主要就是根据redis配置模版，复制出来6份配置信息。
```
# 主目录
dir_redis_cluster='/root/redis/redis-cluster'
# docker redis集群网关
gateway='172.18.0.1'
# 节点地址号 从2开始
idx=1
# 逐个创建各节点目录和配置文件
for port in `seq 7000 7005`; do
    # 创建存放redis数据路径
    mkdir -p ${dir_redis_cluster}/${port}/data;
    # 通过模板个性化各个节点的配置文件
    idx=$(($idx+1));
    port=${port} ip=`echo ${gateway} | sed "s/1$/$idx/g"` \
        envsubst < ${dir_redis_cluster}/redis-cluster.tmpl \
        > ${dir_redis_cluster}/${port}/redis-${port}.conf
done
```

执行结果如下：
```
root:~/redis/redis-cluster# bash create.sh 
root:~/redis/redis-cluster# ls
7000  7001  7002  7003  7004  7005  create.sh  redis-cluster.tmpl
```

#### 3、创建redis实例

创建create.sh脚本，创建然后再分别创建redis容器实例进行运行，redis容器创建时指定网络为之前创建的集群网络。

create.sh脚本：
```
# 主目录
dir_redis_cluster='/root/redis/redis-cluster'
# 创建容器配置并运行
for port in `seq 7000 7005`; do
    docker run --name redis-${port} --net redis-cluster-net -d \
        -p ${port}:${port} -p 1${port}:1${port} \
        -v ${dir_redis_cluster}/${port}/data:/data \
        -v ${dir_redis_cluster}/${port}/redis-${port}.conf:/usr/local/etc/redis/redis.conf redis \
        redis-server /usr/local/etc/redis/redis.conf
done
```


执行结果如下：
```
root:~/redis/redis-cluster# bash build.sh 
0ccb799ade7e50c7a618d62b58b73fd4d47a49c19d397b0079b3cc72c1653c31
6252d0e9c665dc3ed6126083f37704a7459ff48d491d2839f20f391cba5f6e78
bee54745678f7a506fb04d161b2f2946dff39aba89a54da19cc2281c6806699e
fbd97c2beb5d82175f4a72df07e9e3cadd3b81a33ed3db9dd7bd5ea0b9e6feeb
e1dd7e05026ee2fd77b4c1734f3ed37d7a7b3d2798145abba9c1a1b864fd95ac
506ed7f67af09a6f29e21f79781ead0e763ea0f7bf020ebda72406f5c95082cc

root:~/redis/redis-cluster# docker ps 
CONTAINER ID        IMAGE               COMMAND                  CREATED             STATUS              PORTS                                                        NAMES
506ed7f67af0        redis               "docker-entrypoint.s…"   5 seconds ago       Up 4 seconds        0.0.0.0:7005->7005/tcp, 6379/tcp, 0.0.0.0:17005->17005/tcp   redis-7005
e1dd7e05026e        redis               "docker-entrypoint.s…"   6 seconds ago       Up 5 seconds        0.0.0.0:7004->7004/tcp, 6379/tcp, 0.0.0.0:17004->17004/tcp   redis-7004
fbd97c2beb5d        redis               "docker-entrypoint.s…"   7 seconds ago       Up 5 seconds        0.0.0.0:7003->7003/tcp, 6379/tcp, 0.0.0.0:17003->17003/tcp   redis-7003
bee54745678f        redis               "docker-entrypoint.s…"   7 seconds ago       Up 6 seconds        0.0.0.0:7002->7002/tcp, 6379/tcp, 0.0.0.0:17002->17002/tcp   redis-7002
6252d0e9c665        redis               "docker-entrypoint.s…"   8 seconds ago       Up 7 seconds        0.0.0.0:7001->7001/tcp, 6379/tcp, 0.0.0.0:17001->17001/tcp   redis-7001
0ccb799ade7e        redis               "docker-entrypoint.s…"   9 seconds ago       Up 8 seconds        0.0.0.0:7000->7000/tcp, 6379/tcp, 0.0.0.0:17000->17000/tcp   redis-7000
```

#### 4、构建集群
redis-7000的集群已经开启，查看集群的开启状态：
```
root:~/redis/redis-cluster# docker exec -it redis-7000 redis-cli -p 7000 info cluster
# Cluster
cluster_enabled:1
```

通过meet命令将其他实例，连接到集群上，connect.sh脚本内容如下：
```
docker exec -it redis-7000 redis-cli -p 7000 cluster meet 172.18.0.3 7001
docker exec -it redis-7000 redis-cli -p 7000 cluster meet 172.18.0.4 7002
docker exec -it redis-7000 redis-cli -p 7000 cluster meet 172.18.0.5 7003
docker exec -it redis-7000 redis-cli -p 7000 cluster meet 172.18.0.6 7004
docker exec -it redis-7000 redis-cli -p 7000 cluster meet 172.18.0.7 7005
```

执行结果：
```
root:~/redis/redis-cluster# bash connect.sh 
OK
OK
OK
OK
OK

# 查看集群的节点信息
root:~/redis/redis-cluster# docker exec -it redis-7000 redis-cli -p 7000 cluster nodes
1bd03ba912da1159c13bb500ced4ed9888b89cb7 172.18.0.3:7001@17001 master - 0 1609899500000 1 connected
92e105bbfe1b07e3257e10da294c9036c707f2c5 172.18.0.6:7004@17004 master - 0 1609899502830 3 connected
ee51501c9a24779556f625057d10af34866b6ddd 172.18.0.2:7000@17000 myself,master - 0 1609899500000 0 connected
5dd99f289ddc7bd41943c099297f50966afd85f2 172.18.0.7:7005@17005 master - 0 1609899501000 5 connected
71402606cf858335876b78f0f53a84379fec64be 172.18.0.4:7002@17002 master - 0 1609899501000 4 connected
ba1fef2657d4822f15864a536fb19a68ac7e002b 172.18.0.5:7003@17003 master - 0 1609899502000 2 connected
```

#### 5、设置主从结构

通过cluster replicate命令，设置当前节点的主节点
```
# 设置7001节点为7000节点的从节点
docker exec -it redis-7001 redis-cli -p 7001 cluster replicate ee51501c9a24779556f625057d10af34866b6ddd # 7001 --> 7000
# 设置7003节点为7002节点的从节点
docker exec -it redis-7003 redis-cli -p 7003 cluster replicate 71402606cf858335876b78f0f53a84379fec64be # 7003 --> 7002
# 设置7005节点为7004节点的从节点
docker exec -it redis-7005 redis-cli -p 7005 cluster replicate 92e105bbfe1b07e3257e10da294c9036c707f2c5 # 7005 --> 7004
```

设置完成后，通过集群节点信息可以看到主从关系已经生效：
```
root:~/redis/redis-cluster# docker exec -it redis-7000 redis-cli -p 7000 cluster nodes
1bd03ba912da1159c13bb500ced4ed9888b89cb7 172.18.0.3:7001@17001 slave ee51501c9a24779556f625057d10af34866b6ddd 0 1609899723384 0 connected
92e105bbfe1b07e3257e10da294c9036c707f2c5 172.18.0.6:7004@17004 master - 0 1609899724385 3 connected
ee51501c9a24779556f625057d10af34866b6ddd 172.18.0.2:7000@17000 myself,master - 0 1609899721000 0 connected
5dd99f289ddc7bd41943c099297f50966afd85f2 172.18.0.7:7005@17005 slave 92e105bbfe1b07e3257e10da294c9036c707f2c5 0 1609899723000 3 connected
71402606cf858335876b78f0f53a84379fec64be 172.18.0.4:7002@17002 master - 0 1609899722000 4 connected
ba1fef2657d4822f15864a536fb19a68ac7e002b 172.18.0.5:7003@17003 slave 71402606cf858335876b78f0f53a84379fec64be 0 1609899723000 4 connected
```

#### 6、分配集群的槽
通过cluster addslots方式，我们手动的指定每个主节点所分配的槽。allot_slot.sh脚本如下：
```
# 将16384个槽分配到3个主节点去, 每个节点平均分的5461个槽
# 7000 0~5460
docker exec -it redis-7000 redis-cli -p 7000 cluster addslots {0..5460}
# 7002 5461~10920
docker exec -it redis-7002 redis-cli -p 7002 cluster addslots {5461..10920}
# 7004 10920~16383
docker exec -it redis-7004 redis-cli -p 7004 cluster addslots {10921..16383}
```

执行成功后，查看槽的分配情况：
```
root:~/redis/redis-cluster# docker exec -it redis-7000 redis-cli -p 7000 cluster info
cluster_state:ok
cluster_slots_assigned:16384
cluster_slots_ok:16384
...

root:~/redis/redis-cluster# docker exec -it redis-7000 redis-cli -p 7000 cluster slots
1) 1) (integer) 10921
   2) (integer) 16383
   3) 1) "172.18.0.6"
      2) (integer) 7004
      3) "92e105bbfe1b07e3257e10da294c9036c707f2c5"
   4) 1) "172.18.0.7"
      2) (integer) 7005
      3) "5dd99f289ddc7bd41943c099297f50966afd85f2"
2) 1) (integer) 0
   2) (integer) 5460
   3) 1) "172.18.0.2"
      2) (integer) 7000
      3) "ee51501c9a24779556f625057d10af34866b6ddd"
   4) 1) "172.18.0.3"
      2) (integer) 7001
      3) "1bd03ba912da1159c13bb500ced4ed9888b89cb7"
3) 1) (integer) 5461
   2) (integer) 10920
   3) 1) "172.18.0.4"
      2) (integer) 7002
      3) "71402606cf858335876b78f0f53a84379fec64be"
   4) 1) "172.18.0.5"
      2) (integer) 7003
      3) "ba1fef2657d4822f15864a536fb19a68ac7e002b"
```


#### 7、集群验证
```
root:~/redis/redis-cluster# docker exec -it redis-7000 redis-cli -c -p 7000
127.0.0.1:7000> set hello world
OK
127.0.0.1:7000> set test world
-> Redirected to slot [6918] located at 172.18.0.4:7002
OK
```
可以看到，当key为test时，所对应的槽不在当前redis上，发生了重定向。


### 参考文档
1. [手动搭建标准6节点Redis集群(docker)](https://www.cnblogs.com/slowbirdoflsh/p/11633113.html):实操步骤完全参考该文档，简单易懂，感谢。
2. [redis cluster介绍](http://redisdoc.com/topic/cluster-spec.html)
3. [redis cluster原理](https://www.cnblogs.com/williamjie/p/11132211.html)
4. [redis cluster详细配置](https://www.cnblogs.com/renpingsheng/p/9813959.html)
