package io.kimmking.dubbo.demo.provider;

import io.kimmking.dubbo.demo.api.ChangeIntoUSDService;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.stereotype.Service;

@Service("changeIntoUSDService")
public class ChangeIntoUSDServiceImpl implements ChangeIntoUSDService {

    @Override
    @HmilyTCC(confirmMethod = "confirmChangeFromCNH", cancelMethod = "cancelChangeFromCNH")
    public void changeFromCNH(Integer userID, Integer amount) {
        System.out.println("==================TryChangeFromCNH===================");
    }

    public void confirmChangeFromCNH(Integer userID, Integer amount){
        System.out.println("==================ConfirmChangeFromCNH===================");
    }

    public void cancelChangeFromCNH(Integer userID, Integer amount){
        System.out.println("==================CancelChangeFromCNH===================");
    }
}
