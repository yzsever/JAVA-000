package io.kimmking.dubbo.demo.api.service;

import io.kimmking.dubbo.demo.api.entity.ChangeIntoUSDDTO;
import org.dromara.hmily.annotation.Hmily;

public interface ChangeIntoUSDService {

    @Hmily
    void changeFromCNH(ChangeIntoUSDDTO changeIntoUSDDTO);
}
