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
