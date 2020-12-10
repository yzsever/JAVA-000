package me.jenson.service;


import me.jenson.dynamicdatasource.DataSourceSelector;
import me.jenson.dynamicdatasource.DynamicDataSourceEnum;
import me.jenson.entity.Order;
import me.jenson.repository.OrderRepository;
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
    @DataSourceSelector(value = DynamicDataSourceEnum.MASTER)
    public void initEnvironment() throws SQLException {
        orderRepository.dropTable();
        orderRepository.createTableIfNotExists();
        orderRepository.truncateTable();
    }

    @Override
    @DataSourceSelector(value = DynamicDataSourceEnum.MASTER)
    public void cleanEnvironment() throws SQLException {
        orderRepository.dropTable();
    }

    @Override
    @Transactional
    @DataSourceSelector(value = DynamicDataSourceEnum.MASTER)
    public void processSuccess() throws SQLException {
        System.out.println("-------------- Process Success Begin ---------------");
        List<Long> orderIds = insertData();
        printData();
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
    @DataSourceSelector(value = DynamicDataSourceEnum.SLAVE)
    public void printData() throws SQLException {
        print();
    }

    private void print() throws SQLException {
        System.out.println("---------------------------- Print Order Data -----------------------");
        for (Object each : orderRepository.selectAll()) {
            System.out.println(each);
        }
    }
}

