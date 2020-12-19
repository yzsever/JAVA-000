package io.kimmking.dubbo.demo.consumer.service.impl;

import io.kimmking.dubbo.demo.api.entity.ChangeIntoCNHDTO;
import io.kimmking.dubbo.demo.api.entity.ChangeIntoUSDDTO;
import io.kimmking.dubbo.demo.api.service.ChangeIntoCNHService;
import io.kimmking.dubbo.demo.api.service.ChangeIntoUSDService;
import io.kimmking.dubbo.demo.consumer.service.CurrencyTradeService;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CurrencyTradeServiceImpl implements CurrencyTradeService {

    private ChangeIntoUSDService changeIntoUSDService;

    private ChangeIntoCNHService changeIntoCNHService;

    @Autowired(required = false)
    public CurrencyTradeServiceImpl(ChangeIntoUSDService changeIntoUSDService,
                                    ChangeIntoCNHService changeIntoCNHService) {
        this.changeIntoUSDService = changeIntoUSDService;
        this.changeIntoCNHService = changeIntoCNHService;
    }

    @Override
    @HmilyTCC(confirmMethod = "confirmUserAAndBCurrenyTrade", cancelMethod = "cancelUserAAndBCurrenyTrade")
    public void userAAndBCurrenyTrade() {
        // 2. 用户 B 使用 7 人民币兑换 1 美元 ;
        ChangeIntoUSDDTO changeIntoUSDDTO = new ChangeIntoUSDDTO(3, 7);
        changeIntoUSDService.changeFromCNH(changeIntoUSDDTO);
        // 1. 用户 A 使用 1 美元兑换 7 人民币 ;
        ChangeIntoCNHDTO changeIntoCNHDTO = new ChangeIntoCNHDTO(2, 1);
        changeIntoCNHService.changeFromUSD(changeIntoCNHDTO);
    }

    public void confirmUserAAndBCurrenyTrade() {
        System.out.println("==================ConfirmUserAAndBCurrenyTrade===================");
    }

    public void cancelUserAAndBCurrenyTrade() {
        System.out.println("==================CancelUserAAndBCurrenyTrade===================");
    }
}
