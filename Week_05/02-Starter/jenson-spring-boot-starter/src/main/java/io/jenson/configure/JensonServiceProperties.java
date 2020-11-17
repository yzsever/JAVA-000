package io.jenson.configure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jenson")
public class JensonServiceProperties {
    private static final String MSG = "world";

    private String msg = MSG;

    public String getMsg(){
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
}
