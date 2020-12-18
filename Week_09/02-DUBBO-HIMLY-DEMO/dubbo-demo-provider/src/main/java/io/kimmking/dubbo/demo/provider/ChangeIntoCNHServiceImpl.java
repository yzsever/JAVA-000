package io.kimmking.dubbo.demo.provider;

import io.kimmking.dubbo.demo.api.ChangeIntoCNHService;
import org.apache.dubbo.config.annotation.DubboService;
import org.dromara.hmily.annotation.HmilyTCC;

@DubboService(version = "1.0.0")
public class ChangeIntoCNHServiceImpl implements ChangeIntoCNHService {

    @Override
    @HmilyTCC(confirmMethod = "confirmChangeFromUSD", cancelMethod = "cancelChangeFromUSD")
    public void changeFromUSD(Integer userID, Integer amount) {
        System.out.println("==================TryChangeFromUSD===================");
    }

    public void confirmChangeFromUSD(){
        System.out.println("==================ConfirmChangeFromUSD===================");
    }

    public void cancelChangeFromUSD(){
        System.out.println("==================CancelChangeFromUSD===================");
    }
}
