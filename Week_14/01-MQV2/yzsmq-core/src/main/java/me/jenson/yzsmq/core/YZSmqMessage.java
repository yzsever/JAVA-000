package me.jenson.yzsmq.core;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.HashMap;

@AllArgsConstructor
@Data
public class YZSmqMessage<T> {

    private HashMap<String,Object> headers;

    private T body;

}
