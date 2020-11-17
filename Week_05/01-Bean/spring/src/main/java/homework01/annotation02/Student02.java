package homework01.annotation02;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component("student02")
public class Student02 implements Serializable {

    @Autowired
    @Value("002")
    private int id;

    @Autowired
    @Value("JensonYao002")
    private String name;

}
