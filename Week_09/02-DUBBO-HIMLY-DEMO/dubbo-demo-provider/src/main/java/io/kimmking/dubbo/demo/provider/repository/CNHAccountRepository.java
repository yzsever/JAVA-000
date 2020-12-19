package io.kimmking.dubbo.demo.provider.repository;

import io.kimmking.dubbo.demo.provider.entity.CNHAccount;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface CNHAccountRepository {

    @Select("select * from cnh_account")
    public List<CNHAccount> findAll();

    @Select("SELECT * FROM cnh_account WHERE id = #{id}")
    public CNHAccount findById(long id);

    @Delete("DELETE FROM cnh_account WHERE id = #{id}")
    public int deleteById(long id);

    @Insert("INSERT INTO cnh_account(user_id, balance, create_time) " +
            " VALUES (#{userID}, #{balance}, unix_timestamp(now()))")
    @Options(useGeneratedKeys=true, keyProperty="id", keyColumn="id")
    public int insert(CNHAccount CNHAccount);

    @Update("Update cnh_account set balance=#{balance}, " +
            " update_time=unix_timestamp(now()) where id=#{id}")
    public int update(CNHAccount CNHAccount);

    @Select("SELECT * FROM cnh_account WHERE user_id = #{userID}")
    public CNHAccount findByUserId(long userID);
}
