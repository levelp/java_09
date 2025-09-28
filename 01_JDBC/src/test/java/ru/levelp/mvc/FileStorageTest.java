package ru.levelp.mvc;

import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static ru.levelp.mvc.storage.FileStorage.*;

/**
 * Тестирование работы файлового хранилища
 */
public class FileStorageTest {

    @Test
    public void testFileStorage() throws IOException {
        assertEquals(NOT_FOUND, search("Нет такой строки"));
        String s1 = "Одна строка";
        addLine(s1);
        assertEquals(s1, search("строка"));
        assertEquals(s1, search("ОДНА СТРОКА"));
        String s2 = "Другое резюме";
        addLine(s2);
        assertEquals(s2, search("РЕЗЮМЕ"));
    }
}
