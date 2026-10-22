# Library Management System

See the [modernization roadmap](./ROADMAP.md) for the current phase, implementation checklist, and recommended next steps.

## Overview

This repository is modernizing a traditional library management system into a peer-to-peer book sharing platform. The backend currently contains domain, service, DTO, and persistence foundations; the target capabilities and implementation checklist are tracked in the [modernization roadmap](./ROADMAP.md).

## Features

- **Book Management**: Add, remove, and search for books
- **Borrowing System**: Track who has borrowed books and manage returns
- **Search Functionality**: Find books by ISBN, title, author, or genre
- **Reporting**: View all available books and generate borrower reports
- **Data Persistence**: Save and load library data between sessions
- **Import/Export**: Support for CSV import and export

## Project Structure

src/main/java/
├── LibraryApplication.java (Main class)
├── model/
│ ├── Book.java (Book entity)
│ └── Library.java (Core business logic)
├── exception/
│ ├── BookAlreadyExistsException.java
│ ├── BookNotFoundException.java
│ ├── BookNotAvailableException.java
│ ├── BookNotBorrowedException.java
│ └── BorrowLimitExceededException.java
└── util/
└── FileUtils.java (Data persistence utilities)

## Setup Instructions

1. Ensure you have Java JDK 21 installed and selected as `JAVA_HOME`
2. Clone the repository or download the source code
3. Compile the project:
   ```
   javac -d bin src/main/java/*.java src/main/java/*/*.java
   ```
4. Run the application:
   ```
   java -cp bin LibraryApplication
   ```

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

## Usage Guide

### Adding Books

Select option 1 from the main menu and follow prompts to enter:

- Book title
- Author name
- Genre
- ISBN
- Publication year

### Borrowing Books

1. Select option 4 from the main menu
2. Enter the ISBN of the book to borrow
3. Enter the borrower's name

### Returning Books

1. Select option 5 from the main menu
2. Enter the ISBN of the book being returned
3. Enter the borrower's name

### Searching Books

1. Select option 3 from the main menu
2. Choose search criteria (ISBN, title, author, or genre)
3. Enter the search term

### Saving Data

Data is automatically saved when exiting the application (option 0)

## Technical Details

### Requirements

- Java JDK 21

### Object Model

- **Book**: Represents a book with title, author, genre, ISBN, publication year, and availability status
- **Library**: Manages the collection of books and borrowing operations

### Exception Handling

Custom exceptions for specific error scenarios:

- BookAlreadyExistsException
- BookNotFoundException
- BookNotAvailableException
- BookNotBorrowedException
- BorrowLimitExceededException

### Data Persistence

The application uses JPA/Hibernate with H2 in the default `dev` profile or PostgreSQL in the `postgres` profile. Flyway migrations own the database schema; Hibernate validates it when using PostgreSQL.

## Future Improvements

- Graphical user interface
- User authentication system
- Advanced search capabilities
- Book reservation system
- Late return tracking and fines

## Contributing

This project was created as a learning exercise. Feel free to fork the repository and enhance it with your own improvements. Some possible contributions:

- Adding unit tests
- Implementing any of the future improvements
- Enhancing validation and error handling
- Improving code documentation

---

_This project was developed as part of a Java programming curriculum focused on object-oriented programming, collections, and exception handling._
