package io.jenson;

import io.jenson.service.JensonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class StarterTestRunner  implements CommandLineRunner {
    @Autowired
    private JensonService jensonService;

    @Override
    public void run(String... args) {
        jensonService.print();
    }
}