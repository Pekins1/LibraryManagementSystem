# Library Management System

**A Spring Boot REST API for traditional library management and peer-to-peer book sharing.** See the [modernization roadmap](./ROADMAP.md) for the full plan.

## Overview

LibraryManagementSystem catalogs books and individual-owned copies, with copies marked `PRIVATE` for owner-only use or `PUBLIC` for community discovery and borrowing. Platform members discover and borrow public copies, while borrowings are tracked from loan through return. The domain also distinguishes `LibraryMember`, a platform member that owns copies, from `Borrower`, an external patron associated with a library or organization member.

The system is being modernized into a peer-to-peer book-sharing platform where individuals and organizations can manage their collections and exchanges. Owners can also give books away or donate them in the future, but the donation workflow is not implemented yet.

The architecture is layered: controllers handle REST resources, services implement business rules, and repositories persist domain entities through Spring Data JPA. `GlobalExceptionHandler` returns structured errors as `{timestamp, status, error, message, path}`.

The domain model includes `Book`, `BookCopy`, `LibraryMember`, `Borrower`, and `Borrowing`.

## Tech stack

- Java 21
- Spring Boot 4.1
- Spring Data JPA
- Flyway
- PostgreSQL 17
- H2 (dev profile)
- JUnit 5 + Mockito
- Maven

## Features

- REST API for catalog, member, transaction, and discovery resources
- Member-owned book copies with `PRIVATE` and `PUBLIC` visibility
- Community discovery of available public copies
- Borrowing and return workflows
- Reporting through the 16-method `ReportService`, including counts, active and overdue borrowings, borrower history, and late fees (`$1.50` per day for 14-day loans, with overdue status calculated from due dates)
- Structured validation and application error responses
- PostgreSQL/Flyway persistence and H2 development persistence
- 110 passing tests

## Project structure

```text
.
├── mvnw
├── pom.xml
├── README.md
├── ROADMAP.md
├── src/
│   ├── legacy/
│   │   ├── LibraryApplication.java.disabled
│   │   ├── LibraryServiceTest.java.disabled
│   │   ├── library.data
│   │   ├── test1
│   │   └── util.disaple/
│   │       └── FileUtils.java.disabled
│   ├── main/
│   │   ├── java/
│   │   │   └── com/prahkel/library/
│   │   │       ├── P2PLibraryWebApplication.java
│   │   │       ├── config/
│   │   │       ├── controller/
│   │   │       │   └── exceptionHandler/
│   │   │       ├── dto/
│   │   │       ├── exception/
│   │   │       ├── model/
│   │   │       ├── repository/
│   │   │       └── service/
│   │   │           └── impl/
│   │   └── resources/
│   │       └── db/migration/
│   │           └── V1__initial__schema.sql
│   └── test/
│       ├── java/
│       │   └── com/prahkel/library/
│       └── resources/
│           ├── application-pgtest.properties
│           ├── application-test.properties
│           └── application.properties
```

The root Java package is `com.prahkel.library`. It contains the entry point `P2PLibraryWebApplication` and the requested application layers. Legacy sources and data under `src/legacy/` are disabled and not compiled or run.

## Setup & run

JDK 21 is required.

The default `dev` profile uses H2 in-memory persistence:

```powershell
./mvnw spring-boot:run
```

Run the test suite:

```powershell
./mvnw test
```

The `postgres` profile is documented in the Database Setup section below.

### Database Setup (PostgreSQL)

The app has two run profiles:

| Profile | Database | Schema | Use |
|---|---|---|---|
| `dev` (default) | H2 in-memory | Hibernate `create-drop` | Tests and quick runs; data is disposable |
| `postgres` | PostgreSQL 17 | Flyway migrations, Hibernate `validate` | Persistent local data |

**1. Install PostgreSQL 17** (Windows):

```powershell
winget install -e --id PostgreSQL.PostgreSQL.17
```

The winget installation runs unattended and may leave the `postgres` superuser password set to `postgres`. Change it after installation, for example with `ALTER USER postgres PASSWORD 'your-new-password';`.

**2. Create the application user and database** as the `postgres` superuser:

```sql
CREATE USER library WITH PASSWORD 'devpassword';
CREATE DATABASE library OWNER library;
```

**3. Configure credentials.** The `postgres` profile reads `DB_URL`, `DB_USERNAME`, and `DB_PASSWORD` from the environment, falling back to local defaults in `application-postgres.properties`. To use your own values, set them in PowerShell before running:

```powershell
$env:DB_PASSWORD = 'your-local-password'
```

Alternatively, copy `.env.example` to `.env` and load it from your IDE run configuration. In VS Code, set `"envFile": "${workspaceFolder}/.env"` in a `launch.json` configuration. Never commit `.env` or a real password. `.env` is gitignored, and the defaults in `application-postgres.properties` are local-only placeholders.

**4. Run with PostgreSQL:**

```powershell
mvn spring-boot:run "-Dspring-boot.run.profiles=postgres"
```

On first boot, Flyway applies `src/main/resources/db/migration/V1__initial__schema.sql`. Hibernate then validates the schema against the entities and refuses to start if they disagree. Never edit an applied migration; ship schema changes in new `V2__...`, `V3__...` migration files.

**Reset the local database** (drops all data):

Connect to a different database (such as `postgres`) before running these commands; PostgreSQL cannot drop a database while you are connected to it.

```sql
DROP DATABASE library;
CREATE DATABASE library OWNER library;
```

## API endpoints

### Catalog

Base path: `/api/v1/catalog`

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/catalog` | Create a book |
| `GET` | `/api/v1/catalog` | Search books by title, author, or genre |
| `GET` | `/api/v1/catalog/{id}` | Get a book by id |
| `PUT` | `/api/v1/catalog/{id}` | Update a book |
| `DELETE` | `/api/v1/catalog/{id}` | Delete a book |
| `POST` | `/api/v1/catalog/{id}/copies` | Add a copy to a book |
| `GET` | `/api/v1/catalog/{id}/copies` | List copies of a book |
| `GET` | `/api/v1/catalog/{id}/copies/available` | List available copies of a book |
| `DELETE` | `/api/v1/catalog/{id}/copies/{copyId}` | Delete a copy |
| `POST` | `/api/v1/catalog/{id}/archive` | Archive a book |
| `PATCH` | `/api/v1/catalog/{copyId}/visibility` | Change a copy's visibility (`PRIVATE` or `PUBLIC`) |

### Members

Base path: `/api/v1/members`

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/members` | Create a member |
| `GET` | `/api/v1/members/{id}` | Get a member by id |
| `GET` | `/api/v1/members?email=...` | Get a member by email |

### Transactions

Base path: `/api/v1/transactions`

| Method | Path | Description |
|---|---|---|
| `POST` | `/api/v1/transactions/borrow` | Borrow a copy |
| `POST` | `/api/v1/transactions/return` | Return a copy |
| `GET` | `/api/v1/transactions/{id}` | Get a transaction by id |
| `GET` | `/api/v1/transactions/active` | List all active borrowings |
| `GET` | `/api/v1/transactions/overdue` | List overdue borrowings |
| `GET` | `/api/v1/transactions/borrower/{borrowerId}/active` | List active borrowings for a borrower |
| `GET` | `/api/v1/transactions/borrower/{borrowerId}/history` | List a borrower's borrowing history |

### Discovery

Base path: `/api/v1/discovery`

| Method | Path | Description |
|---|---|---|
| `GET` | `/api/v1/discovery` | Browse discoverable public, available copies |

### Curl examples

Create a book:

```bash
curl -X POST http://localhost:8080/api/v1/catalog \
  -H "Content-Type: application/json" \
  -d '{"title":"The Hobbit","author":"J.R.R. Tolkien","genre":"Fantasy","isbn":"978-0-618-00221-3","publishedYear":1937}'
```

Borrow a copy:

```bash
curl -X POST http://localhost:8080/api/v1/transactions/borrow \
  -H "Content-Type: application/json" \
  -d '{"bookId":1,"borrowerId":1}'
```

## Testing

Run the complete Maven test suite with:

```powershell
./mvnw test
```

The repository currently has 110 tests. The 3 repository tests connect to a local PostgreSQL `library_test` database (see `src/test/resources/application-pgtest.properties`), so PostgreSQL must be running locally or those 3 tests will fail.

## History

The project started as a CLI app: a single `Library` class with menu-driven interaction and JSON-file persistence (`FileUtils`). It is being modernized into a Spring Boot platform. The legacy code is preserved under `src/legacy/` as disabled files for reference — it is not compiled or run.

## What's next

Planned work includes authentication and authorization, OpenAPI documentation, donation endpoints, a React web interface, and AI recommendations. See [ROADMAP.md](./ROADMAP.md) for the full plan.

## Contributing

This is a portfolio project. Contributions that improve the REST API, persistence, reporting, validation, documentation, or platform workflows are welcome.
