docker exec -it redis-7000 redis-cli -p 7000 cluster meet 172.18.0.3 7001
docker exec -it redis-7000 redis-cli -p 7000 cluster meet 172.18.0.4 7002
docker exec -it redis-7000 redis-cli -p 7000 cluster meet 172.18.0.5 7003
docker exec -it redis-7000 redis-cli -p 7000 cluster meet 172.18.0.6 7004
docker exec -it redis-7000 redis-cli -p 7000 cluster meet 172.18.0.7 7005
