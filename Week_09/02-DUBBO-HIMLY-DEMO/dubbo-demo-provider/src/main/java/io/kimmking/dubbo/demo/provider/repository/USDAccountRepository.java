package io.kimmking.dubbo.demo.provider.repository;

import io.kimmking.dubbo.demo.provider.entity.CNHAccount;
import io.kimmking.dubbo.demo.provider.entity.USDAccount;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface USDAccountRepository {

    @Select("select * from usd_account")
    public List<USDAccount> findAll();

    @Select("SELECT * FROM usd_account WHERE id = #{id}")
    public USDAccount findById(long id);

    @Delete("DELETE FROM usd_account WHERE id = #{id}")
    public int deleteById(long id);

    @Insert("INSERT INTO usd_account(user_id, balance, create_time) " +
            " VALUES (#{userID}, #{balance}, unix_timestamp(now()))")
    @Options(useGeneratedKeys=true, keyProperty="id", keyColumn="id")
    public int insert(USDAccount USDAccount);

    @Update("Update usd_account set balance=#{balance}, " +
            " update_time=unix_timestamp(now()) where id=#{id}")
    public int update(USDAccount USDAccount);

    @Select("SELECT * FROM usd_account WHERE user_id = #{userID}")
    public USDAccount findByUserID(long userID);
}
