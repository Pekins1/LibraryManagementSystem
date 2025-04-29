import model.Book;
import model.Library;
import util.FileUtils;
import exception.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.InputMismatchException;

public class LibraryApplication {
    private static final String DATA_FILE = "library.data";
    private static Library library;
    private static Scanner scanner;

    public static void main(String [] args){
        initialize();
        showMainMenu();
    }

    // Initialize library and load data if available
    private static void initialize() {
        scanner = new Scanner(System.in);

        // Try to load existing library data
        try{
            library = FileUtils.loadLibraryFromFile(DATA_FILE);
            System.out.println("Library data loaded successfully.");
        }catch(IOException | ClassNotFoundException e){
            // If no data file exists or there's an error, create a new library
            System.out.println("Creating new library...");
            library = new Library();
        }
    }

    // Main Menu method
    private static void showMainMenu(){
        boolean exit = false;

        // Main loop to display menu options
        while(!exit){
            System.out.println("\n=== Library Management System ===");
            System.out.println("1. Add a new Book");
            System.out.println("2. Remove a Book");
            System.out.println("3. Search for a Book");
            System.out.println("4. Borrow a Book");
            System.out.println("5. Return a Book");
            System.out.println("6. View All Available Books");
            System.out.println("7. View Borrower Report");
            System.out.println("8. View All Books");
            System.out.println("9. View All Borrowed Books");
            System.out.println("10. Import/Export data");
            System.out.println("0. Save and exit");

            System.out.print("\nEnter your choice: ");
            int choice = getIntInput();

            switch(choice){
                case 1:
                    addBookMenu();
                    break;
                case 2:
                    removeBookMenu();
                    break;
                case 3: 
                    searchBookMenu();
                    break;
                case 4:
                    borrowBookMenu();
                    break;
                case 5:
                    returnBookMenu();
                    break;
                case 6:
                    viewAvailableBooks();
                    break;
                case 7:
                    viewBorrowerReport();
                    break;
                case 8:
                    viewAllBooks();
                    break;
                case 9:
                    viewAllBorrowedBooks();
                    break;
                case 10:
                    importExportMenu();
                    break;
                case 0:
                    saveAndExit();
                    exit = true;
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
            
        }
    }

    // Helper method for input validation

    // Get integer input from user with validation and error handling
    private static int getIntInput(){
        int input = 0;
        boolean validInput = false;
        
        while(!validInput){
            try{
                input = scanner.nextInt();
                validInput = true;
            }catch(InputMismatchException e){
                System.out.println("Invalid input. Please enter a valid integer.");
            }finally{
                // clear the buffer because nextInt() leaves newline character in the buffer and it will cause an error
                scanner.nextLine();
            }
        }
        return input;
    }

    // Get string input with non-empty validation
    // This method is used to get string input from user when adding,removing,searching for a book
    private static String getStringInput(String prompt){
        String input = "";
        boolean validInput = false;
        
        while(!validInput){
            System.out.print(prompt);
            input = scanner.nextLine().trim(); // trim() removes leading and trailing whitespace

            if(!input.isEmpty()){
                validInput = true;
            }else{
                System.out.println("Input cannot be empty. Please try again.");
            }
        }
        return input;
    }

    // Implimentation of all menu options


    // 1. Add a new book to the library
    private static void addBookMenu(){
        System.out.println("\n=== Add a new Book ===");

        String title = getStringInput("Enter title: ");
        String author = getStringInput("Enter author: ");
        String genre = getStringInput("Enter genre: ");
        String isbn = getStringInput("Enter ISBN: ");

        System.out.print("Enter year published: ");
        int year = getIntInput();

        Book newBook = new Book(title, author, genre, isbn, year);

        // add the new book to the library and check if it already exists
        try{
            library.addBook(newBook);
            System.out.println("Book added successfully.");
        }catch(BookAlreadyExistsException e){
            System.out.println("Error: " + e.getMessage());
        }
    } 

    // 2. Remove a book from the library
    private static void removeBookMenu(){
        System.out.println("\n=== Remove a Book ===");
        String isbn = getStringInput("Enter ISBN of the book to remove: ");

        try{
            library.removeBook(isbn);
            System.out.println("Book removed successfully.");
        }catch(BookNotFoundException e){
            System.out.println("Error: " + e.getMessage());
        }
    }
    // 3. Search for a book in the library
    private static void searchBookMenu(){
        System.out.println("\n=== Search for a Book ===");
        System.out.println("1. Search by ISBN");
        System.out.println("2. Search by Title");
        System.out.println("3. Search by Author");
        System.out.println("4. Search by Genre");

        System.out.print("\nEnter your choice: ");
        int choice = getIntInput();
        
        try{
            switch(choice){
                case 1:
                    String isbn = getStringInput("Enter ISBN:");
                    Book book = library.findBookByISBN(isbn);
                    System.out.println("Book found: " + book);
                    break;
                case 2: 
                    String title = getStringInput("Enter title: ");
                    Book bookByTitle = library.findBookByTitle(title);
                    System.out.println("Book found: " + bookByTitle);
                    break;
                case 3:
                    String author = getStringInput("Enter author: ");
                    Book bookByAuthor = library.findBookByAuthor(author);
                    System.out.println("Book found: " + bookByAuthor);
                    break;
                case 4:
                    String genre = getStringInput("Enter genre: ");
                    Book bookByGenre = library.findBookByGenre(genre);
                    System.out.println("Book found: " + bookByGenre);
                    break;
                default:
                    System.out.println("Invalid choice. Please try again.");
            }
        }catch(BookNotFoundException e){
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 4. Borrow a book from the library
    private static void borrowBookMenu(){
        System.out.println("\n=== Borrow a Book ===");
        String title = getStringInput("Enter title of the book to borrow: ");
        String borrowerName = getStringInput("Enter borrower's name: ");

        try{
            library.borrowBook(title, borrowerName);
            System.out.println("Book borrowed successfully.");
        }catch(BookNotFoundException | BookNotAvailableException | BorrowLimitExceededException e){
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 5. Return a book to the library
    private static void returnBookMenu(){
        System.out.println("\n=== Return a Book ===");
        String title = getStringInput("Enter title of the book to return: ");
        String borrowerName = getStringInput("Enter borrower's name: ");

        try{
            library.returnBook(title, borrowerName);
            System.out.println("Book returned successfully.");
        }catch(BookNotFoundException | BookNotBorrowedException e){
            System.out.println("Error: " + e.getMessage());
        }
    }

    // 6. View all available books
    private static void viewAvailableBooks(){
        System.out.println("\n=== Available Books ===");
        List<Book> availableBooks = library.getAllAvailableBooks();

        // check if there are any books available
        if(availableBooks.isEmpty()){
            System.out.println("No available books in the library.");
        }else{
            for(Book book: availableBooks){
                displayBook(book);
            }
            System.out.println("Total available books: " + availableBooks.size());
        }
    }

    // 7. View all borrower report
    private static void viewBorrowerReport(){
        System.out.println("\n=== Borrower Report ===");
        Map<String, List<Book>> report = library.getBorrowerReport();

        // check if there are any borrowers in our report
        if(report.isEmpty()){
            System.out.println("No borrowers in the library.");
        }else{
            for(Map.Entry<String, List<Book>> entry: report.entrySet()){
               System.out.println("Borrower: " + entry.getKey());
               System.out.println("Books borrowed: ");

               List<Book> books = entry.getValue();
               for(Book book: books){
                    System.out.println(" - " + book.getTitle() + " by " + book.getAuthor() 
                    + "( ISBN: " + book.getISBN()+ ")");
               }
            }
        }
    }

    // 8. View all books
    private static void viewAllBooks(){
        System.out.println("\n=== All Books ===");
        List<Book> allBooks = library.getAllBooks();
        if(allBooks.isEmpty()){
            System.out.println("No books in the library.");
        }else{
            for(Book book: allBooks){
                displayBook(book);
            }
            System.out.println("Total books: " + allBooks.size());
        }
    }
    // 9. View all borrowed books
    private static void viewAllBorrowedBooks(){
        System.out.println("\n=== All Borrowed Books ===");
        List<Book> borrowedBooks = library.getAllBorrowedBooks();
        if(borrowedBooks.isEmpty()){
            System.out.println("No borrowed books in the library.");
        }else{
            
        }
    }
    // 8. Import/Export data
    private static void importExportMenu(){
        System.out.println("\n=== Import/Export Data ===");
        System.out.println("1. Export books to CSV");
        System.out.println("2. Import books from CSV");
        System.out.println("0. Back to main menu");

        System.out.print("\nEnter your choice: ");
        int choice = getIntInput();

        switch(choice){
            case 1:
                exportBooksToCSV();
                break;
            case 2:
                importBooksFromCSV();
                break;
            case 0:
                return;
            default:
                System.out.println("Invalid choice.");
        }
    }

    //Export books to CSV
    private static void exportBooksToCSV(){
        String fileName = getStringInput("Enter the file name to export to: ");
        try{
            FileUtils.exportBooksToCSV(library.getAllAvailableBooks(), fileName);
            System.out.println("Books exported to CSV successfully.");
        }catch(IOException e){
            System.out.println("Error exporting books to CSV: " + e.getMessage());
        }
    }

    //Import books from CSV
    private static void importBooksFromCSV(){
        String fileName = getStringInput("Enter the file name to import from: ");
        try{
            List<Book> importedBooks = FileUtils.importBooksFromCSV(fileName);

            // check if file is empty
            if(importedBooks.isEmpty()){
                System.out.println("No books found in the file.");
                return;
            }
            // check the amount of books to import
            System.out.println("Found " + importedBooks.size() + " books to import.");
            // add books to the library
            try{
                library.addBooks(importedBooks);
                System.out.println("Books imported successfully.");
            }catch(BookAlreadyExistsException e){
                System.out.println("Error during import: " + e.getMessage());
            }
        }catch(IOException e){
            System.out.println("Error reading file: " + e.getMessage());
        }
        
    }

    // Helper method to display a book
    private static void displayBook(Book book){
        System.out.println("\nTitle: " + book.getTitle());
        System.out.println("Author: " + book.getAuthor());
        System.out.println("Genre: " + book.getGenre());
        System.out.println("ISBN: " + book.getISBN());
        System.out.println("Published Year: " + book.getPublishedYear());
        System.out.println("Availability: " + (book.isAvailable() ? "Available" : "Borrowed"));
    }

    // Save and exit
    private static void saveAndExit(){
        try{
            FileUtils.saveLibraryToFile(library, DATA_FILE);
            System.out.println("Library data saved successfully.");
            System.out.println("Thank you for using the Library Management System!");
        }catch(IOException e){
            System.out.println("Error saving library data: " + e.getMessage());
        }
    }

}
