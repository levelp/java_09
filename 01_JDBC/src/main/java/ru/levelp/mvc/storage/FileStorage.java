package ru.levelp.mvc.storage;

import java.io.*;
import java.util.Scanner;

/**
 * Хранение резюме в текстовом файле
 */
public class FileStorage {
    public static final String NOT_FOUND = "Ничего не найдено!";
    static String fileName = "resume.txt";

    /**
     * Добавить строчку в файл
     *
     * @param line строка
     * @throws IOException
     */
    public static void addLine(String line) throws IOException {
        PrintWriter writer = new PrintWriter(
                new OutputStreamWriter(
                        new FileOutputStream(fileName, /* Append */ true),
                        "UTF-8")
        );
        writer.println(line);
        writer.close();
    }

    /**
     * Поиск по подстроке
     *
     * @param query запрос (подстрока)
     * @return Вся строка
     */
    public static String search(String query) {
        try {
            Scanner scanner = new Scanner(new File(fileName));
            while (scanner.hasNext()) {
                String resume = scanner.nextLine();
                // Сравнение с игнорированием
                if (resume.toUpperCase().contains(query.toUpperCase())) {
                    scanner.close();
                    return resume;
                }
            }
            scanner.close();
        } catch (FileNotFoundException e) {
            return NOT_FOUND;
        }
        return NOT_FOUND;
    }

    /**
     * Тестируем работу
     */
    public static void main(String[] args) throws IOException {
        addLine("Добавим строчку...");
        addLine("И ещё одну строчку..");
    }
}
