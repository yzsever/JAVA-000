package homework01.xml01;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.io.Serializable;


@Data
@AllArgsConstructor
@NoArgsConstructor
@ToString

public class Student01 implements Serializable {
    
    private int id;
    private String name;

}
