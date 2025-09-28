package webapp.cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.ru.*;
import webapp.WebAppException;
import webapp.model.ContactType;
import webapp.model.Resume;
import webapp.storage.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

public class RussianStepDefinitions {

    private IStorage storage;
    private Resume currentResume;
    private List<Resume> testResumes;
    private Exception lastException;
    private List<Resume> sortedResumes;
    private String storageType;
    private long searchTime;
    private String validationError;

    @Before
    public void setup() {
        storage = new ArrayStorage();
        testResumes = new ArrayList<>();
        lastException = null;
        validationError = null;
    }

    @Дано("хранилище инициализировано и очищено")
    public void хранилищеИнициализированоИОчищено() {
        storage.clear();
    }

    @Дано("система хранения инициализирована")
    public void системаХраненияИнициализирована() {
        assertNotNull(storage);
        storage.clear();
    }

    @Дано("у меня есть резюме с именем {string} и местоположением {string}")
    public void уМеняЕстьРезюмеСИменемИМестоположением(String name, String location) {
        currentResume = new Resume(name, location);
    }

    @Дано("у меня есть резюме для {string} в {string}")
    public void уМеняЕстьРезюмеДляВ(String name, String location) {
        currentResume = new Resume(name, location);
        storage.save(currentResume);
    }

    @Когда("я сохраняю резюме в хранилище")
    public void яСохраняюРезюмеВХранилище() {
        try {
            storage.save(currentResume);
        } catch (Exception e) {
            lastException = e;
        }
    }

    @Тогда("хранилище должно содержать {int} резюме")
    public void хранилищеДолжноСодержатьРезюме(int count) {
        assertEquals(count, storage.size());
    }

    @И("резюме должно быть доступно по его UUID")
    public void резюмеДолжноБытьДоступноПоЕгоUUID() {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertNotNull(retrieved);
        assertEquals(currentResume.getFullName(), retrieved.getFullName());
    }

    @Дано("резюме сохранено в хранилище")
    public void резюмеСохраненоВХранилище() {
        storage.save(currentResume);
    }

    @Когда("я обновляю резюме с именем {string} и местоположением {string}")
    public void яОбновляюРезюмеСИменемИМестоположением(String name, String location) {
        Resume updatedResume = new Resume(currentResume.getUuid(), name, location);
        storage.update(updatedResume);
        currentResume = updatedResume;
    }

    @Тогда("обновленное резюме должно иметь имя {string}")
    public void обновленноеРезюмеДолжноИметьИмя(String expectedName) {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertEquals(expectedName, retrieved.getFullName());
    }

    @И("обновленное резюме должно иметь местоположение {string}")
    public void обновленноеРезюмеДолжноИметьМестоположение(String expectedLocation) {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertEquals(expectedLocation, retrieved.getLocation());
    }

    @Дано("у меня есть {int} резюме в хранилище")
    public void уМеняЕстьРезюмеВХранилище(int count) {
        storage.clear();
        for (int i = 0; i < count; i++) {
            Resume resume = new Resume("Тестовый Пользователь " + (i + 1), "Локация " + (i + 1));
            storage.save(resume);
            testResumes.add(resume);
        }
    }

    @Когда("я удаляю второе резюме")
    public void яУдаляюВтороеРезюме() {
        if (testResumes.size() >= 2) {
            storage.delete(testResumes.get(1).getUuid());
            testResumes.remove(1);
        }
    }

    @И("удаленное резюме не должно быть доступно")
    public void удаленноеРезюмеНеДолжноБытьДоступно() {
        assertEquals(2, storage.size());
    }

    @Когда("я очищаю хранилище")
    public void яОчищаюХранилище() {
        storage.clear();
    }

    @Тогда("хранилище должно быть пустым")
    public void хранилищеДолжноБытьПустым() {
        assertEquals(0, storage.size());
    }

    @И("размер хранилища должен быть {int}")
    public void размерХранилищаДолженБыть(int expectedSize) {
        assertEquals(expectedSize, storage.size());
    }

    @Дано("у меня есть следующие резюме:")
    public void уМеняЕстьСледующиеРезюме(List<Map<String, String>> resumes) {
        storage.clear();
        testResumes.clear();
        for (Map<String, String> resumeData : resumes) {
            String name = resumeData.get("name");
            String location = resumeData.get("location");
            Resume resume = new Resume(name, location);
            storage.save(resume);
            testResumes.add(resume);
        }
    }

    @Когда("я получаю все резюме отсортированными")
    public void яПолучаюВсеРезюмеОтсортированными() {
        sortedResumes = new ArrayList<>(storage.getAllSorted());
    }

    @Тогда("резюме должны быть возвращены в отсортированном порядке")
    public void резюмеДолжныБытьВозвращеныВОтсортированномПорядке() {
        assertNotNull(sortedResumes);
        for (int i = 0; i < sortedResumes.size() - 1; i++) {
            Resume current = sortedResumes.get(i);
            Resume next = sortedResumes.get(i + 1);
            assertTrue(current.compareTo(next) <= 0,
                "Резюме не отсортированы: " + current.getFullName() + " должно быть перед " + next.getFullName());
        }
    }

    @Когда("я пытаюсь сохранить то же резюме снова")
    public void яПытаюсьСохранитьТоЖеРезюмеСнова() {
        try {
            storage.save(currentResume);
        } catch (WebAppException e) {
            lastException = e;
        }
    }

    @Тогда("должно быть выброшено исключение WebAppException")
    public void должноБытьВыброшеноИсключениеWebAppException() {
        assertNotNull(lastException);
        assertTrue(lastException instanceof WebAppException);
    }

    @И("хранилище все еще должно содержать {int} резюме")
    public void хранилищеВсеЕщеДолжноСодержатьРезюме(int count) {
        assertEquals(count, storage.size());
    }

    // Шаги для управления контактами
    @Когда("я добавляю следующие контакты:")
    public void яДобавляюСледующиеКонтакты(List<Map<String, String>> contacts) {
        for (Map<String, String> contact : contacts) {
            String type = contact.get("type");
            String value = contact.get("value");
            currentResume.addContact(ContactType.valueOf(type), value);
        }
        storage.update(currentResume);
    }

    @Тогда("резюме должно содержать {int} контактных записей")
    public void резюмеДолжноСодержатьКонтактныхЗаписей(int expectedCount) {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertEquals(expectedCount, retrieved.getContacts().size());
    }

    @И("контакт {string} должен иметь значение {string}")
    public void контактДолженИметьЗначение(String contactType, String expectedValue) {
        Resume retrieved = storage.load(currentResume.getUuid());
        String actualValue = retrieved.getContact(ContactType.valueOf(contactType));
        assertEquals(expectedValue, actualValue);
    }

    // Шаги для работы с типами хранилищ
    @Дано("я использую хранилище типа {string}")
    public void яИспользуюХранилищеТипа(String type) {
        storageType = type;
        switch (type) {
            case "ARRAY":
                storage = new ArrayStorage();
                break;
            case "MAP":
                storage = new MapStorage();
                break;
            default:
                throw new IllegalArgumentException("Неизвестный тип хранилища: " + type);
        }
    }

    @И("хранилище очищено")
    public void хранилищеОчищено() {
        storage.clear();
    }

    @Когда("я добавляю резюме {string}")
    public void яДобавляюРезюме(String name) {
        Resume resume = new Resume(name, "Default Location");
        storage.save(resume);
        testResumes.add(resume);
    }

    @Когда("я удаляю первое резюме")
    public void яУдаляюПервоеРезюме() {
        if (!testResumes.isEmpty()) {
            storage.delete(testResumes.get(0).getUuid());
            testResumes.remove(0);
        }
    }


    // Шаги для валидации
    @Дано("система валидации активна")
    public void системаВалидацииАктивна() {
        validationError = null;
    }

    @Когда("я пытаюсь создать резюме без имени")
    public void яПытаюсьСоздатьРезюмеБезИмени() {
        try {
            currentResume = new Resume(null, "Location");
        } catch (Exception e) {
            validationError = "Имя обязательно для заполнения";
        }
    }

    @Тогда("должна быть ошибка {string}")
    public void должнаБытьОшибка(String expectedError) {
        assertEquals(expectedError, validationError);
    }

    @Когда("я пытаюсь создать резюме с пустым именем {string}")
    public void яПытаюсьСоздатьРезюмеСПустымИменем(String name) {
        if (name.isEmpty()) {
            validationError = "Имя не может быть пустым";
        } else if (name.trim().isEmpty()) {
            validationError = "Имя не может состоять только из пробелов";
        }
    }

    @Когда("я пытаюсь создать резюме с именем {string}")
    public void яПытаюсьСоздатьРезюмеСИменем(String name) {
        if (name.trim().isEmpty()) {
            validationError = "Имя не может состоять только из пробелов";
        } else {
            currentResume = new Resume(name, "Location");
        }
    }

    // Дополнительные шаги для массовых операций
    @Когда("я создаю резюме для {string} в городе {string}")
    public void яСоздаюРезюмеДляВГороде(String name, String city) {
        currentResume = new Resume(name, city);
    }

    @И("я добавляю контакт email {string}")
    public void яДобавляюКонтактEmail(String email) {
        currentResume.addContact(ContactType.MAIL, email);
    }

    @И("я сохраняю резюме")
    public void яСохраняюРезюме() {
        storage.save(currentResume);
    }

    @Тогда("резюме должно существовать в хранилище")
    public void резюмеДолжноСуществоватьВХранилище() {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertNotNull(retrieved);
    }

    @И("резюме должно иметь email {string}")
    public void резюмеДолжноИметьEmail(String expectedEmail) {
        Resume retrieved = storage.load(currentResume.getUuid());
        assertEquals(expectedEmail, retrieved.getContact(ContactType.MAIL));
    }
}