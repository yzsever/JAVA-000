package io.jenson.service;

import io.jenson.entity.School;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class JensonService {

    @Autowired
    private School school;

    public void print() {
        school.ding();
        school.getClass1().dong();
    }
}
