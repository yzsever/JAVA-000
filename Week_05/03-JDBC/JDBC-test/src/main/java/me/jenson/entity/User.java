package me.jenson.entity;

public class User {
    private Long userID;
    private String userName;

    public User(String userName) {
        this.userName = userName;
    }

    public User(Long userID, String userName) {
        this.userID = userID;
        this.userName = userName;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Override
    public String toString() {
        return "User{" +
                "userID=" + userID +
                ", userName='" + userName + '\'' +
                '}';
    }
}
