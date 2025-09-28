package ru.levelp.mvc.controller;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.levelp.mvc.model.Resume;
import ru.levelp.mvc.storage.FileStorage;

import java.io.IOException;

@Controller
@RequestMapping("/")
@Scope("session")
public class ResumeController {

    /**
     * Поиск резюме
     *
     * @param query Запрос для поиска
     */
    @RequestMapping("/search")
    public String search(ModelMap modelMap,
                         @RequestParam("search") String query) {
        Resume resume = new Resume();
        resume.setName("Нужное нам резюме: " + query);

        modelMap.addAttribute("resume", resume);
        return "search";
    }

    /**
     * Добавление нового резюме
     *
     * @param modelMap
     * @param resume
     * @param name
     * @return
     * @throws IOException
     */
    @RequestMapping("/addResume")
    public String resume(ModelMap modelMap,
                         @RequestParam(value = "resume", required = false)
                         Resume resume,
                         @RequestParam(value = "name", required = false)
                         String name) throws IOException {
        if (name != null) {
            FileStorage.addLine(name);
        }

        if (resume == null)
            resume = new Resume();
        //   resume.setName("Нужное нам резюме: " + query);
        modelMap.addAttribute("resume", resume);
        return "add_resume";
    }
}