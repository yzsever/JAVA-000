# 创建容器配置并运行
dir_redis_cluster='/root/redis/redis-cluster'
for port in `seq 7000 7005`; do
    docker run --name redis-${port} --net redis-cluster-net -d \
        -p ${port}:${port} -p 1${port}:1${port} \
        -v ${dir_redis_cluster}/${port}/data:/data \
        -v ${dir_redis_cluster}/${port}/redis-${port}.conf:/usr/local/etc/redis/redis.conf redis \
        redis-server /usr/local/etc/redis/redis.conf
done
