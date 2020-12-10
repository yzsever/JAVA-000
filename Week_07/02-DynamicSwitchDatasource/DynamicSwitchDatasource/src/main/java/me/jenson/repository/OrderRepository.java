package me.jenson.repository;

import me.jenson.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderRepository extends CommonRepository<Order, Long> {

}

