package ru.levelp.mvc.model;

/**
 *
 */
public class Resume {

    @Name("Имя")
    String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
