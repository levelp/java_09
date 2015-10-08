package ru.levelp.mvc.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.levelp.mvc.forms.LoginForm;

/**
 *
 */
@Controller
@RequestMapping("/")
@Scope("session")
public class UserController {
    boolean auth = false;
    int count = 0;
    String userName;

    /**
     * Вход на сайт
     *
     * @param model    Параметры для представления
     * @param login    Введённый пользователем логин
     * @param password Введённый пароль
     * @return имя представления
     */
    @RequestMapping(value = "/login")
    public String login(ModelMap model,
                        @RequestParam(value = "login", required = false) String login,
                        @RequestParam(value = "password", required = false) String password) {

        LoginForm loginForm = new LoginForm();
        if (loginForm.tryLogin(login, password)) {
            count++;
            auth = true;
            userName = login;
            model.addAttribute("count", count);
            model.addAttribute("userName", userName);
            return "success";
        } else
            return "login";
    }

    /**
     * Выход с сайта
     *
     * @param model параметры для представления view
     * @return имя представления
     */
    @RequestMapping(value = "/logout")
    public String logout(ModelMap model) {
        model.addAttribute("count", count);
        auth = false;
        return "login";
    }

    @RequestMapping(value = "/success")
    public String success(ModelMap model) {
        count++;
        model.addAttribute("counter", count);
        return "success";
    }
}
