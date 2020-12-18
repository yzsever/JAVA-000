package io.kimmking.dubbo.demo.api;

import org.dromara.hmily.annotation.Hmily;

public interface ChangeIntoCNHService {

    @Hmily
    void changeFromUSD(Integer userID, Integer amount);
}
