package io.kimmking.dubbo.demo.provider;

import io.kimmking.dubbo.demo.api.ChangeIntoUSDService;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.hmily.annotation.HmilyTCC;

@DubboService(version = "1.0.0")
public class ChangeIntoUSDServiceImpl implements ChangeIntoUSDService {

    @Override
    @HmilyTCC(confirmMethod = "confirmChangeFromCNH", cancelMethod = "cancelChangeFromCNH")
    public void changeFromCNH(Integer userID, Integer amount) {
        System.out.println("==================TryChangeFromCNH===================");
    }

    public void confirmChangeFromCNH(){
        System.out.println("==================ConfirmChangeFromCNH===================");
    }

    public void cancelChangeFromCNH(){
        System.out.println("==================CancelChangeFromCNH===================");
    }
}
