# IT Inventory System — Курсовий проект

Система управління ІТ-інвентарем для ІТ-департаменту підприємства.
Відслідковує обладнання, програмні ліцензії та серійні номери.

---

## Зміст

1. [Технологічний стек](#технологічний-стек)
2. [Архітектура та дизайн-патерни](#архітектура-та-дизайн-патерни)
3. [Структура проекту](#структура-проекту)
4. [Швидкий старт (Docker)](#швидкий-старт-docker)
5. [Локальний запуск](#локальний-запуск)
6. [REST API](#rest-api)
7. [Ролева модель](#ролева-модель)
8. [Тестові облікові записи](#тестові-облікові-записи)
9. [Тести](#тести)
10. [UML-діаграми](#uml-діаграми)

---

## Технологічний стек

| Шар              | Технологія                                       |
|------------------|--------------------------------------------------|
| Мова / Runtime   | Java 17 (local), eclipse-temurin:21 (Docker)     |
| Framework        | Spring Boot 3.3.5                                |
| Security         | Spring Security 6 — JWT (JJWT 0.12.x) + form-login |
| Persistence      | Spring Data JPA + Hibernate 6 + PostgreSQL 16    |
| Migrations       | Flyway (V1 schema, V2 seed)                      |
| Mapper           | MapStruct 1.5.5 + Lombok 1.18.30                 |
| Messaging        | RabbitMQ (Spring AMQP, DirectExchange)           |
| Reports          | Apache POI 5.3 (xlsx)                            |
| Templates        | Thymeleaf 3 + Bootstrap 5.3 CDN                  |
| Crypto           | AES/GCM/NoPadding 256-bit (ліцензійні ключі)    |
| Tests            | JUnit 5 + Mockito + Testcontainers               |
| Coverage         | Jacoco ≥ 60%                                     |
| Containerization | Docker (multi-stage) + docker-compose            |
| API Docs         | SpringDoc OpenAPI 3 / Swagger UI                 |
| Build            | Maven 3.9                                        |

---

## Архітектура та дизайн-патерни

Мінімум 7 патернів GoF/GRASP задокументовані у JavaDoc відповідних файлів:

| Патерн                  | Файл(и)                                        | Суть застосування                                               |
|-------------------------|------------------------------------------------|-----------------------------------------------------------------|
| **Strategy**            | `InventoryPermissionEvaluator.java`            | Вибір стратегії перевірки прав залежно від типу сутності        |
| **Strategy**            | `NotificationStrategy.java` + реалізації       | Email / InApp / Telegram стратегії надсилання сповіщень         |
| **Factory Method**      | `EquipmentFactory.java`                        | Фабрика обладнання з дефолтними термінами гарантії за типом     |
| **Builder**             | `EquipmentReportRequest.java`                  | Покроковий незмінний об'єкт запиту звіту                        |
| **Builder**             | `LicenseReportRequest.java`                    | Аналогічно для ліцензій                                         |
| **Observer**            | `EntityChangedEvent` + `AuditLogListener`      | Spring ApplicationEvent → автоматичний запис аудит-логу         |
| **Specification**       | `EquipmentSpecification.java`                  | Composable JPA Specification для динамічних фільтрів            |
| **Specification**       | `LicenseSpecification.java`                    | Аналогічно для ліцензій та аудиту                               |
| **Decorator**           | `LicenseKeyAttributeConverter.java`            | Прозоре AES-256-GCM шифрування поля при збереженні в БД         |
| **Composite/Template**  | `fragments/layout.html`                        | Спільні HTML-фрагменти через Thymeleaf `th:replace`             |
| **Singleton**           | Всі Spring-біни                                | Spring IoC гарантує один екземпляр кожного компонента           |

---

## Структура проекту

```
src/
├── main/java/ua/edu/inventory/
│   ├── auth/          # JWT + form-login, refresh tokens
│   ├── audit/         # AuditLog, EntityChangedEvent (Observer)
│   ├── common/        # BaseAuditableEntity, SecurityUtils, PermissionEvaluator
│   ├── config/        # SecurityConfig, RabbitConfig, WebMvcConfig, OpenApiConfig
│   ├── crypto/        # LicenseKeyAttributeConverter (Decorator + AES-GCM)
│   ├── equipment/     # Equipment, EquipmentFactory, EquipmentSpecification
│   ├── license/       # License, LicenseSpecification
│   ├── notification/  # NotificationStrategy, LicenseExpiryScheduler
│   ├── report/        # ExcelReportGenerator, ReportStore, Builder requests
│   ├── site/          # Site CRUD
│   ├── user/          # User CRUD, UserRole
│   └── web/           # Thymeleaf controllers (Dashboard, Admin, Team)
├── main/resources/
│   ├── db/migration/  # V1__init.sql, V2__seed.sql
│   ├── templates/     # Thymeleaf HTML (Bootstrap 5, Ukrainian)
│   └── application.yml
└── test/java/ua/edu/inventory/
    ├── auth/          # JwtServiceTest, AuthIntegrationTest
    ├── audit/         # AuditLogListenerTest
    └── equipment/     # EquipmentServiceTest, EquipmentIntegrationTest
docs/uml/
├── class-diagram.puml
└── sequence-login.puml
Dockerfile
docker-compose.yml
```

---

## Швидкий старт (Docker)

```bash
# 1. Клонувати репозиторій
git clone <url> && cd kursova_java

# 2. Запустити (PostgreSQL + RabbitMQ + App)
docker-compose up --build -d

# 3. Відкрити у браузері
#    Web UI:      http://localhost:8080        (логін: admin / admin123!)
#    Swagger UI:  http://localhost:8080/swagger-ui.html
#    RabbitMQ UI: http://localhost:15672       (guest / guest)
```

---

## Локальний запуск

Потрібно: **Java 17**, **Maven 3.9**, **PostgreSQL 16**, **RabbitMQ 3**.

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/inventory
export SPRING_DATASOURCE_USERNAME=inventory
export SPRING_DATASOURCE_PASSWORD=inventory

mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

---

## REST API

Swagger UI: `http://localhost:8080/swagger-ui.html`

Основні ендпоінти (prefix `/api/v1`):

| Метод | URL                          | Права                     | Опис                          |
|-------|------------------------------|---------------------------|-------------------------------|
| POST  | `/auth/login`                | Публічний                 | Отримати JWT + refresh token  |
| POST  | `/auth/refresh`              | Публічний                 | Оновити access token          |
| POST  | `/auth/logout`               | Authenticated             | Відкликати refresh token      |
| GET   | `/equipment`                 | Всі ролі                  | Список обладнання (фільтри)   |
| POST  | `/equipment`                 | ADMIN, TEAM_LEAD          | Додати обладнання             |
| POST  | `/equipment/{id}/assign`     | ADMIN, TEAM_LEAD          | Призначити користувачеві      |
| GET   | `/licenses`                  | Всі ролі                  | Список ліцензій               |
| POST  | `/licenses`                  | ADMIN, TEAM_LEAD          | Додати ліцензію               |
| GET   | `/users`                     | ADMIN, AUDITOR            | Список користувачів           |
| POST  | `/users`                     | ADMIN                     | Створити користувача          |
| GET   | `/reports/inventory.xlsx`    | ADMIN, TEAM_LEAD, AUDITOR | Запустити генерацію xlsx      |
| GET   | `/reports/{id}/download`     | ADMIN, TEAM_LEAD, AUDITOR | Завантажити готовий звіт      |
| GET   | `/audit-log`                 | ADMIN, AUDITOR, TEAM_LEAD | Журнал аудиту                 |

---

## Ролева модель

| Роль       | Дашборд    | Мій інвентар | Команда | Обладнання  | Ліцензії    | Користувачі | Об'єкти  | Аудит    |
|------------|:----------:|:------------:|:-------:|:-----------:|:-----------:|:-----------:|:--------:|:--------:|
| ADMIN      | ✓ (global) | ✓            | —       | ✓ CRUD      | ✓ CRUD      | ✓ CRUD      | ✓ CRUD   | ✓        |
| TEAM_LEAD  | ✓ (site)   | ✓            | ✓       | ✓ site CRUD | ✓ site CRUD | —           | —        | ✓ (site) |
| WORKER     | ✓ (own)    | ✓            | —       | —           | —           | —           | —        | —        |
| AUDITOR    | ✓ (global) | ✓            | —       | ✓ read      | ✓ read      | ✓ read      | ✓ read   | ✓        |

---

## Тестові облікові записи

| Логін        | Пароль      | Роль      | Об'єкт          |
|--------------|-------------|-----------|-----------------|
| `admin`      | `admin123!` | ADMIN     | —               |
| `auditor`    | `Audit1!`   | AUDITOR   | —               |
| `teamlead1`  | `Lead123!`  | TEAM_LEAD | Київ — Головний |
| `teamlead2`  | `Lead123!`  | TEAM_LEAD | Львів — Філія   |
| `worker1`    | `Worker1!`  | WORKER    | Київ — Головний |

---

## Тести

```bash
# Unit-тести (без Docker/DB)
mvn test -Dtest="EquipmentServiceTest,LicenseServiceTest,JwtServiceTest,AuditLogListenerTest"

# Всі тести, включно з інтеграційними (потрібен Docker)
mvn verify

# Відкрити звіт Jacoco
open target/site/jacoco/index.html
```

**20 unit-тестів** (Mockito, без Spring context):
- `EquipmentServiceTest` — 8 тестів (create, assign, unassign, inventory number)
- `LicenseServiceTest` — 4 тести (PERPETUAL/SUBSCRIPTION валідація)
- `JwtServiceTest` — 6 тестів (генерація, валідація, claims)
- `AuditLogListenerTest` — 2 тести (Observer pattern)

**Інтеграційні тести** (Testcontainers: PostgreSQL + RabbitMQ):
- `AuthIntegrationTest` — login, refresh rotation, invalid token, JWT bearer
- `EquipmentIntegrationTest` — RBAC, create, inventory number format, audit log

---

## UML-діаграми

- **Class Diagram** → [`docs/uml/class-diagram.puml`](docs/uml/class-diagram.puml)
- **Login Sequence** → [`docs/uml/sequence-login.puml`](docs/uml/sequence-login.puml)

Для перегляду: [PlantUML Online](https://www.plantuml.com/plantuml/uml/) або IntelliJ IDEA PlantUML plugin.
