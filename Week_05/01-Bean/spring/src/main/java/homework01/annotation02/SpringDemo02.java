package homework01.annotation02;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringDemo02 {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext02.xml");
        Student02 student02 = (Student02) context.getBean("student02");
        System.out.println(student02.toString());
    }

}
