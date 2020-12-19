package io.kimmking.dubbo.demo.provider.repository;

import io.kimmking.dubbo.demo.provider.entity.FreezeAccount;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface FreezeAccountRepository {

    @Select("select * from freeze_account")
    public List<FreezeAccount> findAll();

    @Select("SELECT * FROM freeze_account WHERE id = #{id}")
    public FreezeAccount findById(long id);

    @Delete("DELETE FROM freeze_account WHERE id = #{id}")
    public long deleteById(long id);

    @Insert("INSERT INTO freeze_account(user_id, freeze_usd, freeze_cnh, create_time) " +
            " VALUES (#{userID}, #{freezeUSD}, #{freezeCNH}, unix_timestamp(now()))")
    @Options(useGeneratedKeys=true, keyProperty="id", keyColumn="id")
    public void insert(FreezeAccount freezeAccount);

    @Update("Update freeze_account set freeze_usd=#{freezeUSD}, freeze_cnh=#{freezeCNH}, " +
            " update_time=unix_timestamp(now()) where id=#{id}")
    public long update(FreezeAccount freezeAccount);

}
