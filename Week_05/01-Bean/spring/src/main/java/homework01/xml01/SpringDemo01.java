package homework01.xml01;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class SpringDemo01 {

    public static void main(String[] args) {
        ApplicationContext context = new ClassPathXmlApplicationContext("applicationContext01.xml");
        Student01 student01 = (Student01) context.getBean("student01");
        System.out.println(student01.toString());
    }

}
