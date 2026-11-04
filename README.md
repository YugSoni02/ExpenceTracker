# Verdant Expense Tracker

A Java 21 / Spring Boot 4.1.2 expense tracker with a Thymeleaf web interface, a JSON REST API, and an in-memory H2 database for local development.

## Run locally

```powershell
.\mvnw.cmd spring-boot:run
```

Open [http://localhost:8080](http://localhost:8080) and sign in with the development account:

- Username: `expense`
- Password: `expense123`

Set `EXPENSE_TRACKER_USER` and `EXPENSE_TRACKER_PASSWORD` before starting the application to use your own credentials. The local database is in memory and is cleared when the application stops. The H2 console is available at `/h2-console` with JDBC URL `jdbc:h2:mem:expensedb` and username `sa`.

## REST API

All API routes require authentication. The endpoints accept and return JSON:

| Method | Route | Description |
| --- | --- | --- |
| `GET` | `/api/expenses` | List expenses, newest first |
| `GET` | `/api/expenses/{id}` | Get an expense |
| `POST` | `/api/expenses` | Create an expense |
| `PUT` | `/api/expenses/{id}` | Update an expense |
| `DELETE` | `/api/expenses/{id}` | Delete an expense |
| `GET` | `/api/expenses/category/{category}/total` | Sum expenses in a category |

The Actuator health endpoint is available at `/actuator/health`; authenticated users can inspect metrics at `/actuator/metrics`.

## PostgreSQL

`src/main/resources/application.properties` includes the PostgreSQL driver settings as comments. For a production deployment, configure `spring.datasource.url`, `spring.datasource.username`, and `spring.datasource.password` through environment-specific configuration, use `spring.jpa.hibernate.ddl-auto=validate`, and set strong account credentials. CSRF is disabled to support this development application and should be enabled with appropriate CSRF tokens before exposing browser form authentication to production users.
