package io.jenson;

import io.jenson.config.MySQLConstant;
import io.jenson.entity.User;
import io.jenson.utils.DaoUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class JDBCCRUDDemo01 {

    private static final String url = MySQLConstant.URL;
    private static final String username = MySQLConstant.USERNAME;
    private static final String password = MySQLConstant.PASSWORD;

    public static void main(String[] args) {
        // insert
        User user01 = new User("zhangsan");
        User user02 = new User("lisi");
        User user03 = new User("wangwu");
        user01.setUserID(insertUser(user01));
        System.out.println("user01:" + user01);
        user02.setUserID(insertUser(user02));
        user03.setUserID(insertUser(user03));
        // delete
        deleteUserByUserID(user03.getUserID());
        // update
        user01.setUserName("wangwu");
        updateUser(user01);
        // select
        user01 = selectUserByUserID(user01.getUserID());
        if (user01 != null) {
            System.out.println("After upadte, user01:"+ user01);
        }
    }

    public static long insertUser(User user) {
        String sql = "insert into user(user_name) values (?)";
        Connection conn = null;
        PreparedStatement ps = null;
        long userID = 0;
        try {
            // 获取连接
            conn = DriverManager.getConnection(url, username, password);
            // 创建会话
            ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, user.getUserName());
            ps.executeUpdate();
            ResultSet generatedKeys = ps.getGeneratedKeys();
            if (generatedKeys.next()) {
                userID = generatedKeys.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DaoUtil.close(ps, conn);
        }
        return userID;
    }

    public static int deleteUserByUserID(Long userID) {
        String sql = "delete from user where user_id=?";
        int n = 0;
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            ps = connection.prepareStatement(sql);
            ps.setLong(1, userID);
            n = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DaoUtil.close(ps, connection);
        }
        return n;
    }

    public static int updateUser(User user) {
        String sql = "update user set user_name=? where user_id=?";
        int n = 0;
        Connection connection = null;
        PreparedStatement ps = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            ps = connection.prepareStatement(sql);
            ps.setString(1, user.getUserName());
            ps.setLong(2, user.getUserID());
            n = ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DaoUtil.close(ps, connection);
        }
        return n;
    }

    public static User selectUserByUserID(Long userID) {
        String sql = "select * from user where user_id=?";
        User user = null;
        Connection connection = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            ps = connection.prepareStatement(sql);
            ps.setLong(1, userID);
            rs = ps.executeQuery();
            if (rs.next()) {
                userID = rs.getLong("user_id");
                String userName = rs.getString("user_name");
                user = new User(userID, userName);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            DaoUtil.close(rs, ps, connection);
        }
        return user;
    }
}
