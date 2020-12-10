package me.jenson.rwsplitting.service;


import me.jenson.rwsplitting.entity.Order;
import me.jenson.rwsplitting.repository.OrderRepository;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

@Service
@Primary
public class OrderServiceImpl implements ExampleService {

    @Resource
    private OrderRepository orderRepository;

    @Override
    public void initEnvironment() throws SQLException {
        orderRepository.dropTable();
        orderRepository.createTableIfNotExists();
        orderRepository.truncateTable();
    }

    @Override
    public void cleanEnvironment() throws SQLException {
        orderRepository.dropTable();
    }

    @Override
    @Transactional
    public void processSuccess() throws SQLException {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> orderIds = insertData();
        printData();
//        deleteData(orderIds);
//        printData();
        System.out.println("-------------- Process Success Finish --------------");
    }

    @Override
    @Transactional
    public void processFailure() throws SQLException {
        System.out.println("-------------- Process Failure Begin ---------------");
        insertData();
        System.out.println("-------------- Process Failure Finish --------------");
        throw new RuntimeException("Exception occur for transaction test.");
    }

    private List<Long> insertData() throws SQLException {
        System.out.println("---------------------------- Insert Data ----------------------------");
        List<Long> result = new ArrayList<>(10);
        for (int i = 1; i <= 10; i++) {
            Order order = new Order();
            order.setUserId(i);
            order.setAddressId(i);
            order.setStatus("INSERT_TEST");
            orderRepository.insert(order);
            result.add(order.getOrderId());
        }
        return result;
    }

    private void deleteData(final List<Long> orderIds) throws SQLException {
        System.out.println("---------------------------- Delete Data ----------------------------");
        for (Long each : orderIds) {
            orderRepository.delete(each);
        }
    }

    @Override
    public void printData() throws SQLException {
        System.out.println("---------------------------- Print Order Data -----------------------");
        for (Object each : orderRepository.selectAll()) {
            System.out.println(each);
        }
    }
}

