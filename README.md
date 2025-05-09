# 🔐 OTP Service — One-Time Password Backend API

## 📌 Описание

Это backend-приложение, реализующее генерацию, отправку и проверку одноразовых паролей (OTP) для обеспечения двухфакторной аутентификации и подтверждения операций.

Сервис позволяет:

- Регистрация и авторизация с токенами
- Генерация и валидация кодов
- Отправка кодов через 4 канала: **Email** , **SMS (через SMPP эмулятор)** , **Telegram** , **сохранение в файл**
- Настройка параметров кода администратором
- Полное логирование событий

Реализовано согласно ТЗ от компании Promo IT.

---

## 🧩 Функционал

### ✅ Регистрация и Авторизация

- `/register` — регистрация пользователя (роль `ADMIN` или `USER`)
- `/login` — получение токена авторизации

### ✅ OTP Коды

- `/user/generate` — генерация OTP-кода, привязанного к операции
- `/user/validate` — проверка кода

### ✅ Администрирование (только для роли `ADMIN`)

- `/admin/config` — получить или обновить длину кода и время жизни
- `/admin/users` — список всех пользователей
- `/admin/users/{id}` — удаление пользователя и его кодов

### ✅ Механизмы отправки кодов

- Email (через JavaMail)
- SMS (через SMPP эмулятор SMPPSim)
- Telegram (через Bot API)
- Сохранение в файл `otp_codes.log`

---

## ⚙️ Технологии

| КОМПОНЕНТ                   | ИСПОЛЬЗОВАНИЕ                      |
|-----------------------------|------------------------------------|
| **Java 22**                 | Язык разработки                    |
| **Maven**                   | Система сборки                     |
| **PostgreSQL**              | Хранение данных                    |
| **JDBC**                    | Подключение к БД                   |
| **com.sun.net.httpserver**  | HTTP сервер                        |
| **SLF4J + Logback**         | Логирование                        |
| **SMPPSim**                 | Эмулятор SMPP для тестирования SMS |
| **Telegram Bot API**        | Отправка кодов через Telegram      |
| **Docker, Docker Compose**  | Запуск                             |

## 🗃️ Структура базы данных

### Таблицы:

1. **users**

    - id
    - login (уникальный)
    - password_hash
    - role (`ADMIN`, `USER`)
2. **otp_config**

    - id
    - code_length
    - expiration_time (в секундах)
3. **otp_codes**

    - id
    - user_id
    - operation_id
    - code
    - status (`ACTIVE`, `EXPIRED`, `USED`)
    - created_at
    - expires_at

---

## 🚀 Установка и Запуск

### 0. Клонировать репозиторий

```bash
git clone https://github.com/d-dmitriev/MephiOTPService.git
cd MephiOTPService
```

### 1. Установите зависимости

```bash
mvn clean install
```
При запуске через docker можно пропустить этот шаг.

### 2. Настройте PostgreSQL

Создайте БД и выполните SQL из файла `schema.sql`.
При запуске через docker можно пропустить этот шаг.

### 3. Настройте конфиги

Отредактируйте следующие файлы в `src/main/resources/`:

- `application.properties` — настройки БД
- `email.properties` — данные для почты
- `sms.properties` — настройки SMPP
- `telegram.properties` — токен бота
- `logback.xml` — уровень логгирования

### 4. Запустите проект

Через Maven:
```bash
mvn exec:java -Dexec.mainClass="org.example.otp.Main"
```

Через IDE:
- Запустите класс Main.java
- Убедитесь, что все .properties доступны

Через Docker:
```bash
docker-compose up -d
```

---

## 🌐 Примеры использования API

> Все запросы требуют заголовка `Authorization: Bearer <token>` (кроме `/login` и `/register`)

### Регистрация

```http
POST /register
Content-Type: application/json

{
  "login": "admin",
  "password": "secret123",
  "role": "ADMIN"
}
```
### Логин

```http
POST /login
Content-Type: application/json

{
  "login": "admin",
  "password": "secret123"
}
```

### Генерация кода

```http
POST /user/generate
Authorization: Bearer <token>
Content-Type: application/json

{
  "operationId": "op_123",
  "channel": "email",
  "destination": "user@example.com"
}
```
### Проверка кода

```http
POST /user/validate
Authorization: Bearer <token>
Content-Type: application/json

{
  "operationId": "op_123",
  "code": "123456"
}
```

---

## 📋 Логирование

Логи сохраняются:

- В консоль
- В файл `logs/app.log`

Пример:

```
2025-05-10 01:53:00 [main] INFO  org.example.otp.Main - HTTP-сервер запущен на порту 8080
2025-05-10 01:53:00 [main] INFO  org.example.otp.Main - Сервер работает...
```

---

## 🛠️ Дополнительные инструкции

### Для тестирования SMS:

- Запустите [SMPPSim](https://github.com/delhee/SMPPSim/releases/tag/3.0.0)
- Обновите `sms.properties`

### Для тестирования Telegram:

- Создайте бота через @BotFather
- Получите токен и chat_id через `getUpdates`
- Обновите `telegram.properties`

---

## 📁 Структура проекта

```
src/
├── main/
│   ├── java/org.example.otp/
│   │   ├── Main.java
│   │   ├── api/
│   │   │   ├── AuthHandler.java
│   │   │   ├── AdminApi.java
│   │   │   └── UserApi.java
│   │   ├── service/
│   │   │   ├── AuthService.java
│   │   │   ├── OtpService.java
│   │   │   ├── EmailNotificationService.java
│   │   │   ├── SmsNotificationService.java
│   │   │   ├── TelegramNotificationService.java
│   │   │   └── FileNotificationService.java
│   │   ├── dao/
│   │   │   ├── UserDao.java
│   │   │   ├── OtpConfigDao.java
│   │   │   └── OtpCodeDao.java
│   │   ├── model/
│   │   │   ├── User.java
│   │   │   ├── OtpConfig.java
│   │   │   └── OtpCode.java
│   │   ├── util/
│   │   │   ├── DbConnection.java
│   │   │   ├── PasswordHasher.java
│   │   │   └── TokenUtil.java
│   │   ├── handler/
│   │   │   ├── LoggingHandler.java
│   │   │   └── AuthFilter.java
│   │   └── scheduler/
│   │       └── OtpExpiryScheduler.java
│   │
│   └── resources/
│       ├── application.properties
│       ├── email.properties
│       ├── sms.properties
│       ├── telegram.properties
│       ├── logback.xml
│       └── schema.sql
```

---

## 📁 Как проверить работу

### Для Email:

- Убедитесь, что `email.properties` содержит правильные учетные данные
- Вызовите `/user/generate` с `channel=email` и проверьте почту

### Для SMS:

- Запустите SMPPSim:
```bash
./startsmppsim.sh
```

### Для Telegram:

- Запустите бота через @BotFather
- Получите `chat_id` через `getUpdates`
- Отправьте запрос с `channel=telegram`

### Для файла:

- Коды будут сохраняться в `otp_codes.log`

---

## 📊 Планировщик истёкших кодов

- Каждую минуту помечает устаревшие коды как `EXPIRED`
- Используется `OtpExpiryScheduler` с `TimerTask`

---

## 📄 Postman Collection в каталоге docs

После импорта в Postman вы можете использовать коллекцию для:

- Регистрации и входа
- Генерации кода через 4 канала
- Проверки кода
- Управления конфигурацией и пользователями

---

## 📈 Планы по улучшению

- Интеграция с Redis для хранения временных кодов
- Реализация TOTP вместо HOTP
- Добавление Swagger UI для документации API
- Использование Spring Boot (для продвинутых версий)
- Механизм повторной отправки кода
- Webhook-уведомления при успешном/неуспешном подтверждении