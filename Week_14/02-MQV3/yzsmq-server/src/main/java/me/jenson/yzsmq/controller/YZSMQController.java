package me.jenson.yzsmq.controller;

import me.jenson.yzsmq.service.YZSMQService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class YZSMQController {

    @Autowired
    private YZSMQService yzsmqService;

    @GetMapping("/poll")
    public String poll(@RequestParam String topic, @RequestParam String uuid){
        return yzsmqService.poll(topic, uuid);
    }

    @PostMapping("/send")
    public void send(@RequestParam String topic, @RequestParam String message){
        yzsmqService.send(topic, message);
    }

    @PostMapping("/poll_ack")
    public void pollAck(@RequestParam String topic, @RequestParam String uuid){
        yzsmqService.pollAck(topic, uuid);
    }

    @PostMapping("/send_ack")
    public void sendAck(@RequestParam String topic){
        yzsmqService.sendAck(topic);
    }

}
