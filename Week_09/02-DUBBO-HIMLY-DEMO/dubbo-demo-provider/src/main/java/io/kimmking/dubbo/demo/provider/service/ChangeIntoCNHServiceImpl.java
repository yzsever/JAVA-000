package io.kimmking.dubbo.demo.provider.service;

import io.kimmking.dubbo.demo.api.entity.ChangeIntoCNHDTO;
import io.kimmking.dubbo.demo.api.service.ChangeIntoCNHService;
import io.kimmking.dubbo.demo.provider.entity.CNHAccount;
import io.kimmking.dubbo.demo.provider.entity.FreezeAccount;
import io.kimmking.dubbo.demo.provider.entity.USDAccount;
import io.kimmking.dubbo.demo.provider.repository.CNHAccountRepository;
import io.kimmking.dubbo.demo.provider.repository.FreezeAccountRepository;
import io.kimmking.dubbo.demo.provider.repository.USDAccountRepository;
import org.dromara.hmily.annotation.HmilyTCC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("changeIntoCNHService")
public class ChangeIntoCNHServiceImpl implements ChangeIntoCNHService {

    @Autowired
    private USDAccountRepository usdAccountRepository;
    @Autowired
    private FreezeAccountRepository freezeAccountRepository;
    @Autowired
    private CNHAccountRepository cnhAccountRepository;

    @Override
    @HmilyTCC(confirmMethod = "confirmChangeFromUSD", cancelMethod = "cancelChangeFromUSD")
    public void changeFromUSD(ChangeIntoCNHDTO changeIntoCNHDTO) {
        System.out.println("==================TryChangeFromUSD===================");
        long userID = changeIntoCNHDTO.getUserID();
        int amount = changeIntoCNHDTO.getAmount();
        USDAccount usdAccount = usdAccountRepository.findByUserID(userID);
        System.out.println(usdAccount);
        if(usdAccount != null && usdAccount.getBalance() >= amount){
            // 冻结美元，美元账户表减少兑换的美元数量
            usdAccount.setBalance(usdAccount.getBalance()-amount);
            usdAccountRepository.update(usdAccount);
            FreezeAccount freezeAccount = new FreezeAccount();
            freezeAccount.setFreezeUSD(amount);
            freezeAccount.setFreezeCNH(0);
            freezeAccount.setUserID(userID);
            // 冻结资产表增加记录
            freezeAccountRepository.insert(freezeAccount);
            changeIntoCNHDTO.setFreezeAccountID(freezeAccount.getId());
            System.out.println(changeIntoCNHDTO);
        }else{
            throw new RuntimeException("Cannot change into CNH!");
        }
    }

    public void confirmChangeFromUSD(ChangeIntoCNHDTO changeIntoCNHDTO){
        System.out.println("==================ConfirmChangeFromUSD : start===================");
        System.out.println(changeIntoCNHDTO);
        // 1、将要兑换的美元数量转换成人民币存入人民币账户
        long userID = changeIntoCNHDTO.getUserID();
        int amount = changeIntoCNHDTO.getAmount();
        CNHAccount cnhAccount = cnhAccountRepository.findByUserId(userID);
        if(cnhAccount == null){
            cnhAccount = new CNHAccount();
            cnhAccount.setUserID(userID);
            cnhAccount.setBalance(amount*7);
            cnhAccountRepository.insert(cnhAccount);
        }else{
            cnhAccount.setBalance(cnhAccount.getBalance()+amount*7);
            cnhAccountRepository.update(cnhAccount);
        }
        System.out.println("==================ConfirmChangeFromUSD : update CNHAccount done===================");
        // 2、删除冻结资产表的记录
        freezeAccountRepository.deleteById(changeIntoCNHDTO.getFreezeAccountID());
        System.out.println("==================ConfirmChangeFromUSD : deletes FreezeAccount done===================");
    }

    public void cancelChangeFromUSD(ChangeIntoCNHDTO changeIntoCNHDTO){
        System.out.println("==================CancelChangeFromUSD===================");
        // 1、解除冻结资金
        System.out.println(changeIntoCNHDTO);
        USDAccount usdAccount = usdAccountRepository.findByUserID(changeIntoCNHDTO.getUserID());
        usdAccount.setBalance(usdAccount.getBalance()+changeIntoCNHDTO.getAmount());
        usdAccountRepository.update(usdAccount);
        // 2、删除冻结资产表的记录
        freezeAccountRepository.deleteById(changeIntoCNHDTO.getFreezeAccountID());
    }
}
