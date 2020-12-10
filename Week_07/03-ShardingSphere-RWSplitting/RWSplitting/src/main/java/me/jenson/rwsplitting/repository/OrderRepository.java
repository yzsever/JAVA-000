package me.jenson.rwsplitting.repository;

import me.jenson.rwsplitting.entity.Order;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OrderRepository extends CommonRepository<Order, Long> {

}

