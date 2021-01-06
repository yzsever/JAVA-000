# 将16384个槽分配到3个主节点去, 每个节点平均分的5461个槽
# 7000 0~5460
docker exec -it redis-7000 redis-cli -p 7000 cluster addslots {0..5460}
# 7002 5461~10920
docker exec -it redis-7002 redis-cli -p 7002 cluster addslots {5461..10920}
# 7004 10920~16383
docker exec -it redis-7004 redis-cli -p 7004 cluster addslots {10921..16383}
