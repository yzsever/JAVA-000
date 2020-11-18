package io.jenson;

import io.jenson.entity.Klass;
import io.jenson.entity.School;
import io.jenson.entity.Student;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

@Configuration
@EnableConfigurationProperties(Student.class)
@ConditionalOnClass(School.class)
@ConditionalOnProperty(prefix = "student", value = "enabled", matchIfMissing = true)
public class JensonServiceAutoConfiguration {

    @Autowired
    Student student100;

    @Bean
    School newSchool() {
        School school = new School();
        school.setStudent100(student100);
        return school;
    }

    @Bean
    Klass klass() {
        Klass klass = new Klass();
        klass.setStudents(Arrays.asList(student100));
        return klass;
    }

    @Bean("student100")
    Student student() {
        student100.setId(100);
        student100.setName("Jenson100");
        return student100;
    }

}
