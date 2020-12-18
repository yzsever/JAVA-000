package io.kimmking.dubbo.demo.consumer.service.impl;

import io.kimmking.dubbo.demo.api.ChangeIntoCNHService;
import io.kimmking.dubbo.demo.api.ChangeIntoUSDService;
import io.kimmking.dubbo.demo.consumer.service.CurrencyTradeService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.stereotype.Service;

@Service
public class CurrencyTradeServiceImpl implements CurrencyTradeService {

    @DubboReference(version = "1.0.0", url = "dubbo://127.0.0.1:12345")
    private ChangeIntoUSDService changeIntoUSDService;

    @DubboReference(version = "1.0.0", url = "dubbo://127.0.0.1:12345")
    private ChangeIntoCNHService changeIntoCNHService;

    @Override
    @HmilyTCC(confirmMethod = "confirmUserAAndBCurrenyTrade", cancelMethod = "cancelUserAAndBCurrenyTrade")
    public void userAAndBCurrenyTrade() {
        // 1. 用户 A 使用 1 美元兑换 7 人民币 ;
        changeIntoCNHService.changeFromUSD(2, 1);
        // 2. 用户 B 使用 7 人民币兑换 1 美元 ;
        changeIntoUSDService.changeFromCNH(3, 7);
    }

    public void confirmUserAAndBCurrenyTrade() {
        System.out.println("==================ConfirmUserAAndBCurrenyTrade===================");
    }

    public void cancelUserAAndBCurrenyTrade() {
        System.out.println("==================CancelUserAAndBCurrenyTrade===================");
    }
}
