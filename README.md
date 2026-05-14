# Система управління інвентарем для ІТ-департаменту

Курсовий проект. Веб-застосунок для обліку обладнання, програмних ліцензій та серійних номерів в ІТ-департаменті компанії.

## Стек технологій

| Шар | Технологія |
|-----|-----------|
| Backend | Spring Boot 3.3, Java 21 |
| Безпека | Spring Security 6, JWT (JJWT 0.12), BCrypt |
| БД | PostgreSQL 16, Spring Data JPA, Hibernate 6 |
| Міграції | Flyway |
| Черга | RabbitMQ 3 |
| UI | Thymeleaf + Bootstrap 5 |
| Маппінг | MapStruct |
| Звіти | Apache POI (Excel) |
| Документація API | springdoc-openapi (Swagger UI) |
| Тести | JUnit 5, Mockito, Testcontainers |
| Контейнеризація | Docker, Docker Compose |

## Швидкий старт (Docker)

```bash
# 1. Клонувати репозиторій
git clone <repo-url>
cd inventory-system

# 2. Налаштувати змінні середовища
cp .env.example .env
# відредагувати .env (змінити паролі та секретні ключі)

# 3. Запустити всі сервіси
docker compose up --build

# 4. Відкрити у браузері
open http://localhost:8080
```

## Локальний запуск (без Docker)

Потрібно мати: Java 21, Maven 3.9+, PostgreSQL 16, RabbitMQ 3.

```bash
# Запустити PostgreSQL та RabbitMQ вручну або через docker compose (тільки інфраструктура):
docker compose up postgres rabbitmq -d

# Встановити Maven Wrapper (перший раз)
mvn wrapper:wrapper

# Запустити застосунок з dev-профілем
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```

## Запуск тестів

```bash
./mvnw verify
# Звіт Jacoco: target/site/jacoco/index.html
```

## Тестові користувачі (seed-дані)

| Логін | Пароль | Роль | Об'єкт |
|-------|--------|------|--------|
| `admin` | `password` | ADMIN | — |
| `auditor` | `password` | AUDITOR | — |
| `teamlead1` | `password` | TEAM_LEAD | Офіс Київ |
| `teamlead2` | `password` | TEAM_LEAD | Офіс Харків |
| `teamlead3` | `password` | TEAM_LEAD | Офіс Львів |
| `worker1` | `password` | WORKER | Офіс Київ |
| `worker2` | `password` | WORKER | Офіс Київ |
| `worker3` | `password` | WORKER | Офіс Харків |

## API документація

Swagger UI доступний за адресою: **http://localhost:8080/swagger-ui.html**

OpenAPI JSON: http://localhost:8080/api-docs

## Застосовані патерни проектування

| Патерн | Де застосовано |
|--------|---------------|
| **Strategy** | `NotificationStrategy` — вибір каналу сповіщення (Email / InApp / Telegram) |
| **Factory Method** | `EquipmentFactory` — створення обладнання різних типів з налаштуваннями за замовчуванням |
| **Builder** | `EquipmentReportRequest` — побудова запиту на складний звіт |
| **Observer** | Spring Application Events → `AuditLogListener` — автоматичне логування змін |
| **Specification** | JPA Specifications для динамічних фільтрів Equipment/License/User |
| **Decorator** | `LicenseKeyAttributeConverter` — прозоре AES-шифрування поля в БД |
| **Singleton** | Всі Spring-біни — синглтони за замовчуванням (IoC container) |

## Структура проекту

```
src/main/java/ua/edu/inventory/
├── config/          # SecurityConfig, JwtConfig, OpenApiConfig, RabbitConfig
├── auth/            # JWT-фільтр, refresh-token, login/logout endpoints
├── user/            # User entity, service, controller, DTO, mapper
├── site/            # Site entity, service, controller, DTO, mapper
├── equipment/       # Equipment entity, service, controller, DTO, mapper, factory
├── license/         # License entity, service, controller, DTO, mapper, crypto
├── audit/           # AuditLog entity, event listener, service, controller
├── notification/    # Strategies, RabbitMQ producer/consumer
├── report/          # Async Excel report generation
├── web/             # Thymeleaf controllers
├── common/          # Exceptions, ProblemDetail handler, BaseEntity, SecurityUtils
└── crypto/          # AES AttributeConverter
```

## UML-діаграми

- `docs/uml/class-diagram.puml` — діаграма класів (PlantUML)
- `docs/uml/sequence-login.puml` — діаграма послідовності для аутентифікації

Рендер: https://www.plantuml.com/plantuml/uml/ або плагін PlantUML для IntelliJ IDEA.
