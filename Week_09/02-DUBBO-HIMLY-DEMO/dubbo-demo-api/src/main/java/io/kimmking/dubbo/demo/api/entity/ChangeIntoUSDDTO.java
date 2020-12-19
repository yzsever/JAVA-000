package io.kimmking.dubbo.demo.api.entity;

import java.io.Serializable;

public class ChangeIntoUSDDTO implements Serializable {
    private Integer userID;
    private Integer amount;
    private Long freezeAccountID;

    public ChangeIntoUSDDTO(int userID, int amount) {
        this.userID = userID;
        this.amount = amount;
    }

    public ChangeIntoUSDDTO() {
    }

    public ChangeIntoUSDDTO(Integer userID, Integer amount, Long freezeAccountID) {
        this.userID = userID;
        this.amount = amount;
        this.freezeAccountID = freezeAccountID;
    }

    public Integer getUserID() {
        return userID;
    }

    public void setUserID(Integer userID) {
        this.userID = userID;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Long getFreezeAccountID() {
        return freezeAccountID;
    }

    public void setFreezeAccountID(Long freezeAccountID) {
        this.freezeAccountID = freezeAccountID;
    }

    @Override
    public String toString() {
        return "ChangeIntoUSDDTO{" +
                "userID=" + userID +
                ", amount=" + amount +
                ", freezeAccountID=" + freezeAccountID +
                '}';
    }
}
