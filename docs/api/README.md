# REST API — Система управління інвентарем

Базовий URL: `http://localhost:8080/api/v1`

Формат відповідей: JSON. Помилки — RFC 7807 `ProblemDetail`.

Повна інтерактивна документація: **http://localhost:8080/swagger-ui.html**

## Автентифікація

| Метод | Шлях | Опис |
|-------|------|------|
| POST | `/auth/login` | Логін, повертає `accessToken` + `refreshToken` |
| POST | `/auth/refresh` | Оновлення токенів (ротація refresh) |
| POST | `/auth/logout` | Анулювання refresh-токена |
| GET | `/auth/me` | Поточний користувач |

### Формат запиту `/auth/login`
```json
{ "username": "admin", "password": "password" }
```

### Формат відповіді
```json
{
  "accessToken": "eyJ...",
  "refreshToken": "eyJ...",
  "user": { "id": "...", "username": "admin", "role": "ADMIN" }
}
```

Для захищених ендпоінтів додавати заголовок:
```
Authorization: Bearer <accessToken>
```

## Протокол взаємодії — основні сценарії

### Сценарій 1: Логін і отримання токенів
```
Client → POST /api/v1/auth/login {username, password}
Server → 200 {accessToken, refreshToken, user}

Client (every request) → Authorization: Bearer <accessToken>
Server → 200 <data>  OR  401 (token expired)

Client (on 401) → POST /api/v1/auth/refresh {refreshToken}
Server → 200 {accessToken, refreshToken}  (старий refresh анульовано)
```

### Сценарій 2: Призначення обладнання
```
TEAM_LEAD → POST /api/v1/equipment/{id}/assign {userId}
Server validates:
  - equipment.siteId == currentUser.siteId
  - targetUser.siteId == equipment.siteId
  - equipment.status != DECOMMISSIONED
Server → 200 EquipmentDto
Observer → AuditLog записано (action=ASSIGN)
```

### Сценарій 3: Асинхронний звіт
```
Client → GET /api/v1/reports/inventory.xlsx?siteId=...
Server → 202 Accepted {reportId, statusUrl}
Server publishes message → RabbitMQ queue "inventory.reports"
Consumer generates Excel via Apache POI, saves to temp storage
Client → GET /api/v1/reports/{reportId}/status
Server → 200 {status: "READY", downloadUrl: "/api/v1/reports/{reportId}/download"}
Client → GET /api/v1/reports/{reportId}/download
Server → 200 application/vnd.openxmlformats-officedocument.spreadsheetml.sheet
```

## Коди помилок (RFC 7807)

| HTTP | type | Опис |
|------|------|------|
| 400 | validation-error | Помилка валідації полів |
| 401 | unauthorized | Не автентифіковано або токен прострочений |
| 403 | forbidden | Недостатньо прав |
| 404 | not-found | Ресурс не знайдено |
| 409 | conflict | Конфлікт (дублікат username/email, перевищено seats) |
| 500 | internal-error | Внутрішня помилка сервера |
