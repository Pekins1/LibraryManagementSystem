# Library Management System

## Overview

The Library Management System is a Java console application for managing a library's book collection, borrowing operations, and record-keeping. This project demonstrates core Java programming concepts including object-oriented design, collections framework, exception handling, file I/O, and serialization.

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

1. Ensure you have Java JDK 8+ installed
2. Clone the repository or download the source code
3. Compile the project:
   ```
   javac -d bin src/main/java/*.java src/main/java/*/*.java
   ```
4. Run the application:
   ```
   java -cp bin LibraryApplication
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

- Java JDK 8 or higher

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

The application uses Java serialization to save and load library data between sessions. It also supports importing and exporting book data in CSV format.

## Future Improvements

- Graphical user interface
- Database integration (JDBC)
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
