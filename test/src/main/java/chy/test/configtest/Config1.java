package chy.test.configtest;

import com.chy.summer.framework.annotation.beans.Value;
import com.chy.summer.framework.context.annotation.Configuration;

@Configuration
public class Config1 {

    @Value("${nishiyigegde1}")
    private String value;


    public void pf() {
        System.out.println(value);
    }


}
