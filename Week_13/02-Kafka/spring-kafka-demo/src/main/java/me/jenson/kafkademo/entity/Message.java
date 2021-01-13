package me.jenson.kafkademo.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Message {

    private Long id;

    private String message;

    private Date sendTime;
}
