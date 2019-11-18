package chy.test.service;

public class BeanDemo2 {


    private String name;



    public String getName() {
        System.out.println("执行了 BeanDemo2 的 getName() == "+name);
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
