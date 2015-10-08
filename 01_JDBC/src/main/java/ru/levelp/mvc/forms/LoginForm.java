package ru.levelp.mvc.forms;

/**
 *
 */
public class LoginForm {
    public boolean tryLogin(String login, String password) {
        if (login == null)
            return false;
        if (password == null)
            return false;
        if (login.equals("admin") && password.equals("123"))
            return true;
        return false;
    }
}
