package me.jenson;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import me.jenson.config.MySQLConstant;
import me.jenson.entity.User;
import me.jenson.utils.DaoUtil;
import me.jenson.utils.StringUtil;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class HikariDemo03 {

    private static final String url = MySQLConstant.URL;
    private static final String username = MySQLConstant.USERNAME;
    private static final String password = MySQLConstant.PASSWORD;
    private static final String jdbcDriver = MySQLConstant.JDBC_DRIVER;

    public static void main(String[] args) {
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
        // batch insert
        List<User> users = new ArrayList<User>();
        users.add(new User("zhangsan"));
        users.add(new User("lisi"));
        users.add(new User("wangwu"));
        List<Long> userIDs = insertUsers(dataSource, users);
        // query insert datas
        List<User> queryUsers = selectUserByUserIDs(dataSource, userIDs);
        System.out.println(queryUsers);
    }

    public static List<Long> insertUsers(DataSource dataSource, List<User> users) {
        String sql = "insert into user(user_name) values (?)";
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        List<Long> userIDs = new ArrayList<Long>();
        try {
            // 获取连接
            conn = DriverManager.getConnection(url, username, password);
            //Assume a valid connection object conn
            conn.setAutoCommit(false);
            // 创建会话
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            for (User user : users) {
                ps.setString(1, user.getUserName());
                ps.addBatch();
            }
            ps.executeBatch();
            // If there is no error.
            conn.commit();
            rs = ps.getGeneratedKeys();
            while (rs.next()) {
                userIDs.add(rs.getLong(1));
            }
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
        return userIDs;
    }

    public static List<User> selectUserByUserIDs(DataSource dataSource, List<Long> userIDs) {
        String sql = "select * from user where user_id in (" + StringUtil.convertIDsToIDStr(userIDs, ",") + ")";
        List<User> users = new ArrayList<User>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = dataSource.getConnection();
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                Long userID = rs.getLong("user_id");
                String userName = rs.getString("user_name");
                User user = new User(userID, userName);
                users.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DaoUtil.close(ps, conn);
        }
        return users;
    }
}
