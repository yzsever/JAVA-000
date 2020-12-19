package io.kimmking.dubbo.demo.provider.service;

import io.kimmking.dubbo.demo.api.entity.ChangeIntoUSDDTO;
import io.kimmking.dubbo.demo.api.service.ChangeIntoUSDService;
import io.kimmking.dubbo.demo.provider.entity.CNHAccount;
import io.kimmking.dubbo.demo.provider.entity.FreezeAccount;
import io.kimmking.dubbo.demo.provider.entity.USDAccount;
import io.kimmking.dubbo.demo.provider.repository.CNHAccountRepository;
import io.kimmking.dubbo.demo.provider.repository.FreezeAccountRepository;
import io.kimmking.dubbo.demo.provider.repository.USDAccountRepository;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("changeIntoUSDService")
public class ChangeIntoUSDServiceImpl implements ChangeIntoUSDService {

    @Autowired
    private USDAccountRepository usdAccountRepository;
    @Autowired
    private FreezeAccountRepository freezeAccountRepository;
    @Autowired
    private CNHAccountRepository cnhAccountRepository;

    @Override
    @HmilyTCC(confirmMethod = "confirmChangeFromCNH", cancelMethod = "cancelChangeFromCNH")
    public void changeFromCNH(ChangeIntoUSDDTO changeIntoUSDDTO) {
        System.out.println("==================TryChangeFromCNH===================");
        long userID = changeIntoUSDDTO.getUserID();
        int amount = changeIntoUSDDTO.getAmount();
        CNHAccount cnhAccount = cnhAccountRepository.findByUserId(userID);
        System.out.println(cnhAccount);
        if(cnhAccount != null && cnhAccount.getBalance() >= amount){
            // 冻结人民币，人民币账户表减少兑换的美元数量
            cnhAccount.setBalance(cnhAccount.getBalance()-amount);
            cnhAccountRepository.update(cnhAccount);
            FreezeAccount freezeAccount = new FreezeAccount();
            freezeAccount.setFreezeUSD(0);
            freezeAccount.setFreezeCNH(amount);
            freezeAccount.setUserID(userID);
            // 冻结资产表增加记录
            freezeAccountRepository.insert(freezeAccount);
            changeIntoUSDDTO.setFreezeAccountID(freezeAccount.getId());
        }else{
            throw new RuntimeException("Cannot change into USD!");
        }
        System.out.println(changeIntoUSDDTO);
    }

    public void confirmChangeFromCNH(ChangeIntoUSDDTO changeIntoUSDDTO){
        System.out.println("==================ConfirmChangeFromCNH : start===================");
        System.out.println(changeIntoUSDDTO);
        // 1、将要兑换的人民币数量转换成美元存入美元账户
        long userID = changeIntoUSDDTO.getUserID();
        int amount = changeIntoUSDDTO.getAmount();
        USDAccount usdAccount = usdAccountRepository.findByUserID(userID);
        if(usdAccount == null){
            usdAccount = new USDAccount();
            usdAccount.setUserID(userID);
            usdAccount.setBalance(amount/7);
            usdAccountRepository.insert(usdAccount);
        }else{
            usdAccount.setBalance(usdAccount.getBalance()+amount/7);
            usdAccountRepository.update(usdAccount);
        }
        System.out.println("==================ConfirmChangeFromCNH : update usdAccount done===================");

        // 2、删除冻结资产表的记录
        freezeAccountRepository.deleteById(changeIntoUSDDTO.getFreezeAccountID());
        System.out.println("==================ConfirmChangeFromCNH : delete FreezeAccount done===================");

    }

    public void cancelChangeFromCNH(ChangeIntoUSDDTO changeIntoUSDDTO){
        System.out.println("==================CancelChangeFromCNH===================");
        // 1、解除冻结资金
        CNHAccount cnhAccount = cnhAccountRepository.findByUserId(changeIntoUSDDTO.getUserID());
        cnhAccount.setBalance(cnhAccount.getBalance()+changeIntoUSDDTO.getAmount());
        cnhAccountRepository.update(cnhAccount);
        // 2、删除冻结资产表的记录
        freezeAccountRepository.deleteById(changeIntoUSDDTO.getFreezeAccountID());
    }
}
