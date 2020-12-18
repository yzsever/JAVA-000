package io.kimmking.dubbo.demo.api;

import org.dromara.hmily.annotation.Hmily;

public interface ChangeIntoUSDService {

    @Hmily
    void changeFromCNH(Integer userID, Integer amount);

}
