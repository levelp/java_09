package ru.levelp.mvc.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;
import java.util.List;

//-->
@Controller
@RequestMapping("/")
public class HelloController {
    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String printWelcome(ModelMap model) {
        // Добавляем аттрибут ${message} для hello.jsp
        model.addAttribute("message", "Добро пожаловать на наш сайт!");
        model.addAttribute("a", 2);
        model.addAttribute("b", 12);
        return "hello";
    }

    @RequestMapping(value = "/add", method = RequestMethod.GET)
    public String add(ModelMap model, @RequestParam("a") int a,
                      @RequestParam("b") int b) {
        model.addAttribute("a", a);
        model.addAttribute("b", b);
        model.addAttribute("sum", a + b);
        return "add";
    }

    /**
     * http://localhost:8080/table - выведется таблица умножения
     * http://localhost:8080/table?size=5 - задаём размер
     */
    @RequestMapping(value = "/table", method = RequestMethod.GET)
    public String printMulTable(Model model, @RequestParam(value = "size", required = false) Integer size) {
        model.addAttribute("html", "This <b>is</b> <strike>HTML</strike> from controller!");
        // Размер таблицы по-умолчанию (значение по-умолчанию)
        if (size == null) {
            size = 10;
        }
        model.addAttribute("size", size);
        return "table";
    }

    /**
     * Вывод таблицы умножения
     * http://localhost:8080/table - выведется таблица умножения
     * size - размер таблицы
     */
    @RequestMapping(value = "/table/{size}", method = RequestMethod.GET)
    public String printMulTable2(Model model, @PathVariable("size") Integer size) {
        model.addAttribute("html", "size = " + size);
        model.addAttribute("size", size);
        return "table";
    }

    @RequestMapping(value = "/table/{op}/{size}", method = RequestMethod.GET)
    public String printOpTable(Model model,
                               @PathVariable("op") String op,
                               @PathVariable("size") Integer size) {
        model.addAttribute("html", "size = " + size);
        model.addAttribute("size", size);
        int[][] result = new int[size][size];
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                int x = i + 1;
                int y = j + 1;
                switch (op) {
                    case "sum":
                        result[i][j] = x + y;
                        break;
                    case "sub":
                        result[i][j] = x - y;
                        break;
                    case "mul":
                        result[i][j] = x * y;
                        break;
                }
            }
        }
        model.addAttribute("result", result);
        return "any_table";
    }

    /**
     * Вывод списка: Питерский метрополитен
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public ModelAndView showList() {
        List<String> list = new ArrayList<String>();
        list.add("1. Красная - Кировско-Выборгская");
        list.add("2. Синяя");
        list.add("3. Зелёная");
        list.add("4. Оранжевая");
        list.add("5. Фиолетовая");

        //return back to index.jsp
        ModelAndView model = new ModelAndView("list");
        model.addObject("lists", list);

        return model;
    }
}
//<--