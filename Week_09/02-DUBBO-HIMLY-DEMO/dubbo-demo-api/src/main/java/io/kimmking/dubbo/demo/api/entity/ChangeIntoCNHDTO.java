package io.kimmking.dubbo.demo.api.entity;

import java.io.Serializable;

public class ChangeIntoCNHDTO implements Serializable {
    private Integer userID;
    private Integer amount;
    private Long freezeAccountID;

    public ChangeIntoCNHDTO(int userID, int amount) {
        this.userID = userID;
        this.amount = amount;
    }

    public ChangeIntoCNHDTO() {
    }

    public ChangeIntoCNHDTO(Integer userID, Integer amount, Long freezeAccountID) {
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
        return "ChangeIntoCNHDTO{" +
                "userID=" + userID +
                ", amount=" + amount +
                ", freezeAccountID=" + freezeAccountID +
                '}';
    }
}
