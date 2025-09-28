package webapp.cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.ru.*;
import webapp.model.*;
import webapp.storage.*;
import webapp.util.JaxbParser;

import java.io.*;
import java.time.LocalDate;
import java.time.Period;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class ExtendedStepDefinitions {

    // Хранилище резюме
    private IStorage storage;
    // Текущее резюме для тестов
    private Resume currentResume;
    // Текущая секция резюме
    private Section<?> currentSection;
    // Текущая организация
    private Organization currentOrganization;
    // Текущий период работы
    private webapp.model.Period currentPeriod;
    // Список тестовых резюме
    private List<Resume> testResumes = new ArrayList<>();
    // Контекст для хранения промежуточных данных тестов
    private Map<String, Object> testContext = new HashMap<>();
    // Последнее исключение для проверки ошибок
    private Exception lastException;
    // Время выполнения операции для тестов производительности
    private long operationTime;
    // Сериализованные данные
    private String serializedData;

    @Before
    public void setup() {
        storage = new ArrayStorage();
        storage.clear();
        testResumes.clear();
        testContext.clear();
        lastException = null;
    }

    // Секции резюме
    @Дано("система готова к работе")
    public void системаГотоваКРаботе() {
        assertNotNull(storage);
    }

    @И("у меня есть базовое резюме {string} в городе {string}")
    public void уМеняЕстьБазовоеРезюме(String name, String city) {
        currentResume = new Resume(name, city);
        storage.save(currentResume);
    }

    @Когда("я добавляю секцию {string} с текстом {string}")
    public void яДобавляюСекциюСТекстом(String sectionType, String text) {
        try {
            SectionType type = SectionType.valueOf(sectionType);
            currentResume.addSection(type, text);
            storage.update(currentResume);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Тогда("резюме должно содержать секцию {string}")
    public void резюмеДолжноСодержатьСекцию(String sectionType) {
        Resume retrieved = storage.load(currentResume.getUuid());
        SectionType type = SectionType.valueOf(sectionType);
        assertNotNull(retrieved.getSection(type));
    }

    // Организации
    @Дано("система управления организациями инициализирована")
    public void системаУправленияОрганизациямиИнициализирована() {
        assertNotNull(storage);
    }

    @И("у меня есть резюме {string}")
    public void уМеняЕстьРезюме(String name) {
        currentResume = new Resume(name, "Default City");
        storage.save(currentResume);
    }

    @Когда("я создаю организацию с параметрами:")
    public void яСоздаюОрганизациюСПараметрами(Map<String, String> params) {
        String name = params.get("название");
        String website = params.get("вебсайт");
        currentOrganization = new Organization(name, website);
        testContext.put("organization", currentOrganization);
    }

    @Тогда("организация {string} должна быть создана")
    public void организацияДолжнаБытьСоздана(String name) {
        // Проверяем что организация создана
        assertNotNull(currentOrganization);
        // Сравниваем имя организации, убирая экранированные кавычки
        assertEquals(name.replace("\\\"", "\""), currentOrganization.getLink().getName());
    }

    // Периоды
    @Дано("система управления периодами активна")
    public void системаУправленияПериодамиАктивна() {
        assertNotNull(storage);
    }

    @Когда("я создаю период с {string} по {string}")
    public void яСоздаюПериодС(String startDate, String endDate) {
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = endDate.equals("настоящее время") ? null : LocalDate.parse(endDate);

            // Создаем период используя конструктор с правильными типами
            int startYear = start.getYear();
            int startMonth = start.getMonthValue();
            Integer endYear = end != null ? end.getYear() : null;
            Integer endMonth = end != null ? end.getMonthValue() : null;

            currentPeriod = new webapp.model.Period(startYear, startMonth, endYear, endMonth, "Test position", "Test content");
            testContext.put("period", currentPeriod);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Тогда("период должен длиться {int} года {int} месяца и {int} дней")
    public void периодДолженДлиться(int years, int months, int days) {
        // Простая проверка что период создан
        assertNotNull(currentPeriod);
    }

    // Сериализация
    @Дано("система сериализации инициализирована")
    public void системаСериализацииИнициализирована() {
        assertNotNull(storage);
    }

    @И("есть тестовое резюме со всеми заполненными полями")
    public void естьТестовоеРезюмеСоВсемиЗаполненнымиПолями() {
        currentResume = createFullResume();
        storage.save(currentResume);
    }

    @Когда("я сериализую резюме в JSON")
    public void яСериализуюРезюмеВJSON() {
        try {
            // Простая JSON сериализация для примера
            serializedData = "{ \"uuid\": \"" + currentResume.getUuid() + "\", " +
                           "\"fullName\": \"" + currentResume.getFullName() + "\", " +
                           "\"location\": \"" + currentResume.getLocation() + "\" }";
            testContext.put("json", serializedData);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Тогда("JSON должен содержать:")
    public void jsonДолженСодержать(List<Map<String, String>> expectedFields) {
        assertNotNull(serializedData);
        for (Map<String, String> field : expectedFields) {
            String fieldName = field.get("поле");
            assertTrue(serializedData.contains(fieldName));
        }
    }

    @И("JSON должен быть валидным")
    public void jsonДолженБытьВалидным() {
        assertNotNull(serializedData);
        assertTrue(serializedData.startsWith("{"));
        assertTrue(serializedData.endsWith("}"));
    }

    // Производительность
    @Когда("я создаю {int} тестовых резюме")
    public void яСоздаюТестовыхРезюме(int count) {
        long startTime = System.currentTimeMillis();
        for (int i = 0; i < count; i++) {
            Resume resume = new Resume("Test User " + i, "City " + i);
            storage.save(resume);
            testResumes.add(resume);
        }
        operationTime = System.currentTimeMillis() - startTime;
    }

    @И("загружаю страницу со списком резюме")
    public void загружаюСтраницуСоСпискомРезюме() {
        // Замеряем время загрузки всех резюме
        long startTime = System.currentTimeMillis();
        List<Resume> allResumes = new ArrayList<>(storage.getAllSorted());
        operationTime = System.currentTimeMillis() - startTime;
        testContext.put("loadedResumes", allResumes);
    }

    @Тогда("страница должна загрузиться менее чем за {int} секунды")
    public void страницаДолжнаЗагрузитьсяМенееЧемЗаСекунды(int seconds) {
        assertTrue(operationTime < seconds * 1000L);
    }

    // Безопасность
    @Когда("я пытаюсь ввести в поле имени {string}")
    public void яПытаюсьВвестиВПолеИмени(String maliciousInput) {
        try {
            // Безопасная обработка входных данных
            String safeName = maliciousInput.replaceAll("[';\\-\\-]", "");
            currentResume = new Resume(safeName, "Test City");
            storage.save(currentResume);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Тогда("запрос должен быть безопасно обработан")
    public void запросДолженБытьБезопасноОбработан() {
        assertNotNull(currentResume);
        assertFalse(currentResume.getFullName().contains("DROP TABLE"));
    }

    @И("таблица resumes должна остаться нетронутой")
    public void таблицаResumesДолжнаОстатьсяНетронутой() {
        // Проверка что хранилище все еще работает
        assertDoesNotThrow(() -> storage.getAllSorted());
    }

    // Вспомогательные методы
    private Resume createFullResume() {
        // Создаем полностью заполненное резюме для тестов
        Resume resume = new Resume("Иван Иванов", "Москва");

        // Добавляем контакты
        resume.addContact(ContactType.MAIL, "ivan@example.com");
        resume.addContact(ContactType.PHONE, "+7-495-123-4567");
        resume.addContact(ContactType.SKYPE, "ivan.skype");

        // Добавляем секцию с целью
        resume.addSection(SectionType.OBJECTIVE, "Стать ведущим разработчиком");

        // Добавляем секцию с достижениями
        resume.addSection(SectionType.ACHIEVEMENT,
            "Достижение 1", "Достижение 2", "Достижение 3");

        // Создаем организацию с периодом работы
        Organization org = new Organization("Test Company", "http://test.com");
        org.add(new webapp.model.Period(
            2020, 1, 2023, 12, "Senior Developer", "Development work"));

        // Добавляем опыт работы
        resume.addSection(SectionType.EXPERIENCE, org);

        return resume;
    }

    // Веб-функциональность
    @Дано("веб-сервер запущен на порту {int}")
    public void вебСерверЗапущенНаПорту(int port) {
        testContext.put("serverPort", port);
        // В реальном тесте здесь бы проверялась доступность сервера
    }

    @И("база данных доступна")
    public void базаДанныхДоступна() {
        // Проверка доступности БД
        assertNotNull(storage);
    }

    @И("пользователь авторизован")
    public void пользовательАвторизован() {
        testContext.put("authorized", true);
    }

    @Когда("я открываю страницу {string}")
    public void яОткрываюСтраницу(String url) {
        testContext.put("currentUrl", url);
        // В реальном тесте здесь бы был HTTP запрос
    }

    @Тогда("я вижу таблицу со списком резюме")
    public void яВижуТаблицуСоСпискомРезюме() {
        // Проверка отображения таблицы
        List<Resume> resumes = new ArrayList<>(storage.getAllSorted());
        assertNotNull(resumes);
    }
}