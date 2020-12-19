package io.kimmking.dubbo.demo.provider;

import io.kimmking.dubbo.demo.api.ChangeIntoCNHService;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.stereotype.Service;

@Service("changeIntoCNHService")
public class ChangeIntoCNHServiceImpl implements ChangeIntoCNHService {

    @Override
    @HmilyTCC(confirmMethod = "confirmChangeFromUSD", cancelMethod = "cancelChangeFromUSD")
    public void changeFromUSD(Integer userID, Integer amount) {
        System.out.println("==================TryChangeFromUSD===================");
    }

    public void confirmChangeFromUSD(Integer userID, Integer amount){
        System.out.println("==================ConfirmChangeFromUSD===================");
    }

    public void cancelChangeFromUSD(Integer userID, Integer amount){
        System.out.println("==================CancelChangeFromUSD===================");
    }
}
