package ru.levelp.beans;

import org.springframework.web.servlet.view.InternalResourceViewResolver;

/**
 * Пример Java Bean
 */
public class MyBean {

    org.springframework.web.servlet.view.InternalResourceViewResolver
            view;
    String name;

    public MyBean() {
        System.out.println("MyBean.MyBean");
    }

    public InternalResourceViewResolver getView() {
        return view;
    }

    public void setView(InternalResourceViewResolver view) {
        System.out.println("view = " + view);
        this.view = view;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        System.out.println("MyBean.setName(\"" + name + "\")");
        name = name + '!';
        this.name = name;
    }
}
