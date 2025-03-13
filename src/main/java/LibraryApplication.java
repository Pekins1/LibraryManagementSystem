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
            System.out.println("8. Import/Export data");
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

        System.out.print("Enter the number of copies to add: ");
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
                    String author = getStringInput("Enter author: "):
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

    // 4.

}
