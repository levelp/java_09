package ru.levelp.beans;

/**
 * Пример Java Bean
 * JavaBeans - классы в языке Java, написанные по определённым правилам.
 * <p>
 * ПРАВИЛА:
 * 1. Класс должен иметь конструктор без параметров, с модификатором доступа public.
 * Такой конструктор позволяет инструментам создать объект без дополнительных сложностей с параметрами.
 * 2. Свойства класса должны быть доступны через get, set и другие методы (так называемые методы доступа),
 * которые должны подчиняться стандартному соглашению об именах.
 * Это легко позволяет инструментам автоматически определять и обновлять содержание bean’ов.
 * Многие инструменты даже имеют специализированные редакторы для различных типов свойств.
 * 3. Класс должен быть сериализуем. Это даёт возможность надёжно сохранять, хранить и восстанавливать
 * состояние bean независимым от платформы и виртуальной машины способом.
 */
public class BeanExample {

    String login;

    String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
