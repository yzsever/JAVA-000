package io.jenson.configure;

import io.jenson.service.JensonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties()
@ConditionalOnClass(JensonService.class)
@ConditionalOnProperty(prefix = "jenson", value = "enabled", matchIfMissing = true)
public class JensonServiceAutoConfiguration {

    @Autowired
    private JensonServiceProperties jensonServiceProperties;

    public JensonService jensonService(){
        JensonService jensonService = new JensonService();
        jensonService.setMsg(jensonServiceProperties.getMsg());
        return jensonService;
    }

}
