package io.kimmking.dubbo.demo.provider.entity;

public class FreezeAccount {

    private Long id;
    private Long userID;
    private Integer freezeUSD;
    private Integer freezeCNH;
    private Long createTime;
    private Long updateTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getUserID() {
        return userID;
    }

    public void setUserID(Long userID) {
        this.userID = userID;
    }

    public Integer getFreezeUSD() {
        return freezeUSD;
    }

    public void setFreezeUSD(Integer freezeUSD) {
        this.freezeUSD = freezeUSD;
    }

    public Integer getFreezeCNH() {
        return freezeCNH;
    }

    public void setFreezeCNH(Integer freezeCNH) {
        this.freezeCNH = freezeCNH;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Long updateTime) {
        this.updateTime = updateTime;
    }
}
