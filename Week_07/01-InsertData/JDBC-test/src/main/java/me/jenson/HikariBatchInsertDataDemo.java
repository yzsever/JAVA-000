package me.jenson;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.jenson.config.MySQLConstant;
import me.jenson.entity.User;
import me.jenson.utils.DaoUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HikariBatchInsertDataDemo {

    private static final String url = MySQLConstant.URL;
    private static final String username = MySQLConstant.USERNAME;
    private static final String password = MySQLConstant.PASSWORD;
    private static final String jdbcDriver = MySQLConstant.JDBC_DRIVER;
    private static final String createOrderTable = "CREATE TABLE t_order (order_id BIGINT AUTO_INCREMENT, user_id INT NOT NULL, status VARCHAR(50), PRIMARY KEY (order_id))";
    private static final String dropTableOrder = "DROP TABLE IF EXISTS t_order";

    public static void main(String[] args) {
        DataSource dataSource = getDataSource();
        // batch insert
        insertOrdersOnce(dataSource);
    }

    public static void insertOrdersSplit(DataSource dataSource) {
        String sql = "INSERT INTO t_order (user_id, status) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Long> userIDs = new ArrayList<Long>();
        try {
            // 获取连接
            conn = DriverManager.getConnection(url, username, password);
            //Assume a valid connection object conn
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(dropTableOrder);
            ps.execute();
            ps = conn.prepareStatement(createOrderTable);
            ps.execute();
            long start = System.currentTimeMillis();
            // 创建会话
            ps = conn.prepareStatement(sql);
            for (int i=0; i<100; i++) {
                for (int j = 0; j < 10000; j++) {
                    ps.setObject(1, i);
                    ps.setObject(2, "init");
                    ps.addBatch();
                }
                ps.executeBatch();
                long now = System.currentTimeMillis();
                System.out.println("Insert "+i+"w data time : "+(now - start)+"ms");
            }
            long end = System.currentTimeMillis();
            System.out.println("Insert total time : "+(end - start)+"ms");
            // If there is no error.
            conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
            // If there is any error.
            try {
                conn.rollback();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } finally {
            DaoUtil.close(ps, conn);
        }
    }

    public static void insertOrdersOnce(DataSource dataSource) {
        String sql = "INSERT INTO t_order (user_id, status) VALUES (?, ?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Long> userIDs = new ArrayList<Long>();
        try {
            // 获取连接
            conn = DriverManager.getConnection(url, username, password);
            //Assume a valid connection object conn
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(dropTableOrder);
            ps.execute();
            ps = conn.prepareStatement(createOrderTable);
            ps.execute();
            long start = System.currentTimeMillis();
            // 创建会话
            ps = conn.prepareStatement(sql);
            for (int i=0; i<1000000; i++) {
                ps.setObject(1, i);
                ps.setObject(2, "init");
                ps.addBatch();
            }
            ps.executeBatch();
            long end = System.currentTimeMillis();
            System.out.println("Insert total time : "+(end - start)+"ms");
            // If there is no error.
            conn.rollback();
        } catch (SQLException e) {
            e.printStackTrace();
            // If there is any error.
            try {
                conn.rollback();
            } catch (SQLException throwables) {
                throwables.printStackTrace();
            }
        } finally {
            DaoUtil.close(ps, conn);
        }
    }

    private static DataSource getDataSource() {
        // Build hikariConfig
        final HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setDriverClassName(jdbcDriver);
        hikariConfig.setJdbcUrl(url);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);

        hikariConfig.setMaximumPoolSize(5);
        hikariConfig.setConnectionTestQuery("SELECT 1");
        hikariConfig.setPoolName("springHikariCP");

        hikariConfig.addDataSourceProperty("dataSource.cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSize", "250");
        hikariConfig.addDataSourceProperty("dataSource.prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("dataSource.useServerPrepStmts", "true");

        DataSource dataSource = new HikariDataSource(hikariConfig);
        return dataSource;
    }
}
