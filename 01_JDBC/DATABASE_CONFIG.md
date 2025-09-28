# Конфигурация подключения к базе данных

## Способы настройки подключения

Проект поддерживает несколько способов настройки подключения к PostgreSQL:

### 1. Переменные окружения (рекомендуется для production)

Установите переменные окружения перед запуском тестов:

```bash
export DB_URL="jdbc:postgresql://localhost:5432/postgres"
export DB_USER="postgres"
export DB_PASSWORD="your_password"

mvn test
```

**Для Windows (CMD):**
```cmd
set DB_URL=jdbc:postgresql://localhost:5432/postgres
set DB_USER=postgres
set DB_PASSWORD=your_password

mvn test
```

**Для Windows (PowerShell):**
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/postgres"
$env:DB_USER="postgres"
$env:DB_PASSWORD="your_password"

mvn test
```

### 2. Файл db.properties (для локальной разработки)

Скопируйте example файл и настройте:

```bash
cp db.properties.example db.properties
```

Отредактируйте `db.properties`:
```properties
DB_URL=jdbc:postgresql://localhost:5432/postgres
DB_USER=postgres
DB_PASSWORD=your_real_password
```

**⚠️ ВАЖНО:** Файл `db.properties` добавлен в `.gitignore` и НЕ должен коммититься в git!

### 3. Значения по умолчанию

Если переменные окружения не установлены и файл `db.properties` отсутствует, используются значения по умолчанию:

- **URL**: `jdbc:postgresql://localhost:5432/postgres`
- **USER**: `postgres`
- **PASSWORD**: `123`

## CI/CD конфигурация

### GitHub Actions

```yaml
env:
  DB_URL: jdbc:postgresql://localhost:5432/testdb
  DB_USER: postgres
  DB_PASSWORD: ${{ secrets.DB_PASSWORD }}

services:
  postgres:
    image: postgres:16
    env:
      POSTGRES_PASSWORD: ${{ secrets.DB_PASSWORD }}
    options: >-
      --health-cmd pg_isready
      --health-interval 10s
      --health-timeout 5s
      --health-retries 5
```

### GitLab CI

```yaml
variables:
  DB_URL: "jdbc:postgresql://postgres:5432/testdb"
  DB_USER: "postgres"
  DB_PASSWORD: "test_password"

services:
  - postgres:16

before_script:
  - export POSTGRES_HOST_AUTH_METHOD=trust
```

## Docker Compose

Для локальной разработки с Docker:

```yaml
version: '3.8'
services:
  postgres:
    image: postgres:16
    environment:
      POSTGRES_PASSWORD: ${DB_PASSWORD:-123}
      POSTGRES_USER: ${DB_USER:-postgres}
      POSTGRES_DB: postgres
    ports:
      - "5432:5432"
    volumes:
      - postgres_data:/var/lib/postgresql/data

volumes:
  postgres_data:
```

Запуск:
```bash
docker-compose up -d
mvn test
```

## Приоритет настроек

Настройки применяются в следующем порядке (от высшего к низшему приоритету):

1. **Переменные окружения** (System.getenv) - наивысший приоритет
2. **Файл db.properties** (properties-maven-plugin)
3. **Значения по умолчанию** в коде - низший приоритет

## Безопасность

### ✅ Что делать:
- Использовать переменные окружения для production
- Использовать секреты CI/CD (GitHub Secrets, GitLab Variables)
- Хранить пароли в защищенных хранилищах (Vault, AWS Secrets Manager)
- Добавить `db.properties` в `.gitignore`

### ❌ Что НЕ делать:
- Коммитить пароли в git
- Хранить production пароли в `db.properties`
- Использовать одинаковые пароли для dev/test/prod
- Делиться `db.properties` файлом с другими разработчиками

## Проверка текущей конфигурации

Добавьте в начало теста для отладки:

```java
System.out.println("DB_URL: " + System.getenv("DB_URL"));
System.out.println("DB_USER: " + System.getenv("DB_USER"));
System.out.println("DB_PASSWORD: " + (System.getenv("DB_PASSWORD") != null ? "***" : "not set"));
```

## Troubleshooting

### Проблема: "password authentication failed for user postgres"

**Решение:**
1. Проверьте, что PostgreSQL запущен: `pg_isready`
2. Проверьте пароль в переменных окружения или `db.properties`
3. Попробуйте подключиться через psql: `psql -U postgres -h localhost`

### Проблема: "Connection refused"

**Решение:**
1. Убедитесь, что PostgreSQL слушает на порту 5432:
   ```bash
   netstat -an | grep 5432
   ```
2. Проверьте `postgresql.conf`: `listen_addresses = '*'`
3. Проверьте `pg_hba.conf`: разрешено ли подключение с localhost

### Проблема: "database does not exist"

**Решение:**
```bash
createdb -U postgres postgres
```

## Примеры использования

### Запуск только Cucumber тестов:

```bash
export DB_PASSWORD="my_secure_password"
mvn test -Dtest=RunCucumberTest
```

### Запуск всех тестов с кастомной БД:

```bash
export DB_URL="jdbc:postgresql://db-server:5432/testdb"
export DB_USER="testuser"
export DB_PASSWORD="testpass"
mvn clean test
```

### Запуск с профилем Maven:

```bash
mvn test -Pintegration-tests
```

## Дополнительные материалы

- [PostgreSQL JDBC Driver Documentation](https://jdbc.postgresql.org/documentation/)
- [Maven Properties Plugin](https://www.mojohaus.org/properties-maven-plugin/)
- [12-Factor App - Config](https://12factor.net/config)