package io.kimmking.dubbo.demo.api.service;

import io.kimmking.dubbo.demo.api.entity.ChangeIntoCNHDTO;
import org.dromara.hmily.annotation.Hmily;

public interface ChangeIntoCNHService {

    @Hmily
    void changeFromUSD(ChangeIntoCNHDTO changeIntoCNHDTO);
}
