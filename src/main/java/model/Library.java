package model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import exception.BookAlreadyExistsException;
import exception.BookNotFoundException;
import exception.BookNotAvailableException;
import exception.BookNotBorrowedException;
import exception.BorrowLimitExceededException;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonIgnore;

public class Library implements Serializable {

    // Attributes
    @JsonProperty("books")
    private final List<Book> books;
    @JsonProperty("borrowerMap")
    private Map<String, List<Book>> borrowedBooks;
    static final int MAX_BORROW_LIMIT = 6;

    // Constructor
    @JsonCreator
    public Library() {
        this.books = new ArrayList<>();
        this.borrowedBooks = new HashMap<>();
    }

    /////////////////////////////////////Add a book to the library////////////////////////////////////

    // Add a book to the library
    public void addBook(Book book) throws BookAlreadyExistsException {
        // Check for duplicate ISBN
        boolean duplicate = books.stream()
            .anyMatch(b -> b.getISBN().equals(book.getISBN()));
        
        if(duplicate) {
            throw new BookAlreadyExistsException("Book with same ISBN already exists in the library.");
        }
        
        books.add(book);
        book.setIsAvailable(true);
    }

    /////////////////////////////////////Add a list of books to the library//////////////////////////////////// 

    public void addBooks(List<Book> books) throws BookAlreadyExistsException {
        // First check for duplicates in the input list
        for(Book book : books) {
            boolean duplicateInInput = books.stream()
                .filter(b -> b != book) // Don't compare with itself
                .anyMatch(b -> b.getISBN().equals(book.getISBN()));
            if(duplicateInInput) {
                throw new BookAlreadyExistsException(book + ": Book with same ISBN found in the input list.");
            }
            
            // Check against existing books in library
            boolean duplicateInLibrary = this.books.stream()
                .anyMatch(b -> b.getISBN().equals(book.getISBN()));
            if(duplicateInLibrary) {
                throw new BookAlreadyExistsException(book + ": Book with same ISBN already exists in the library.");
            }
        }
        
        // If no duplicates found, add all books
        for(Book book : books) {
            this.books.add(book);
            book.setIsAvailable(true);
        }
    }

    /////////////////////////////////////Remove a book from the library////////////////////////////////////

    // Remove a book from the library
    public void removeBook(String isbn) throws BookNotFoundException {
        // find book in library
        Book book = findBookByISBN(isbn);
        // check if the book is available
        if(!book.isAvailable()){
            throw new BookNotFoundException("Book not found in the library.");
        }
        // remove the book from the library
        books.remove(book);
        // update the book availability
        // book.setIsAvailable(false);
    }

    /////////////////////////////////////Search for a book in the library////////////////////////////////////
    
    // Search Methods
    // Find book by ISBN
    public Book findBookByISBN(String isbn) throws BookNotFoundException {
        // iterate the list of books and check if the ISBN matches
        for(Book book : books){
            if(book.getISBN().equals(isbn)){
                return book;
            }
        }
        // if no book is found, throw an exception
        throw new BookNotFoundException("Book not found in the library.");
    }

    // Find book by title
    public Book findBookByTitle(String title) throws BookNotFoundException {
        // iterate the list of books and check if the title matches
        for(Book book: books){
            if(book.getTitle().equals(title)){
                return book;
            }
        }
        // if no book is found, throw an exception
        throw new BookNotFoundException("Book not found in the library.");
    }

    // Find book by author
    public Book findBookByAuthor(String author) throws BookNotFoundException {
        // iterate the list of books and check if the author matches
        for(Book book: books){
            if(book.getAuthor().equals(author)){
                return book;
            }
        }
        // if no book is found, throw an exception
        throw new BookNotFoundException("Book not found in the library.");
    }

    // Find book by genre
    public Book findBookByGenre(String genre) throws BookNotFoundException {
        // iterate the list of books and check if the genre matches
        for(Book book: books){
            if(book.getGenre().equals(genre)){
                return book;
            }
        }
        // if no book is found, throw an exception
        throw new BookNotFoundException("Book not found in the library.");
    }

    /*  

    //////////////////////////////////Borrowing books system//////////////////////////////////

    */
    public void borrowBook(String title, String borrowerName) 
        throws BookNotFoundException, BookNotAvailableException, BorrowLimitExceededException {
        // find the book by isbn
        Book book = findBookByTitle(title);
        if(!book.isAvailable()){
            throw new BookNotAvailableException("Book is not available.");
        }
        // Check if the borrower is already in the borrowedbooks Map
        if(borrowedBooks.containsKey(borrowerName)){
            // borrowedBooks.get(borrowerName) is the list of books borrowed by the borrower
            if(borrowedBooks.get(borrowerName).size() >= MAX_BORROW_LIMIT){
                throw new BorrowLimitExceededException("Borrower has reached the maximum borrow limit.");
            }
        }
        // check if the borrower has a list of borrowed books
        List<Book> booksBorrowed = borrowedBooks.get(borrowerName);
        if(booksBorrowed == null){
            booksBorrowed = new ArrayList<>();
        }
        // Add the book to the borrower's list of borrowed books
        booksBorrowed.add(book);
        // update the borrowedBooks Map
        borrowedBooks.put(borrowerName,booksBorrowed);
         
        // update the book availability
        book.setIsAvailable(false);
    }

    /////////////////////////////////////Returning books system//////////////////////////////////// 

    // Returning books System
    public void returnBook(String title, String borrowerName) 
        throws BookNotFoundException, BookNotBorrowedException {
        // Find the book by title
        Book book = findBookByTitle(title);
        
        // Check if the book is available (should be unavailable if borrowed)
        if(book.isAvailable()){
            throw new BookNotBorrowedException("Book is not borrowed.");
        }
        
        // Check if the borrower exists in our records
        if(!borrowedBooks.containsKey(borrowerName)){
            throw new BookNotBorrowedException("Borrower has not borrowed any books.");
        }
        
        // Get the borrower's list directly (no need to loop through all entries)
        List<Book> booksBorrowed = borrowedBooks.get(borrowerName);
        
        // Check if this borrower has this specific book
        if(!booksBorrowed.contains(book)){
            throw new BookNotBorrowedException("This borrower did not borrow this book.");
        }
        
        // Remove the book from the borrower's list
        booksBorrowed.remove(book);
        
        // Update the book availability
        book.setIsAvailable(true);
        
        // If borrower has no more books, remove them from the map
        if(booksBorrowed.isEmpty()){
            borrowedBooks.remove(borrowerName);
        }
    }

    /////////////////////////////////////Get all available books in the library//////////////////////////////////// 

    // Utility methods
    // Get all available books in the library
    public List<Book> getAllAvailableBooks(){
        // iterate the list of books and check if they are available
        List<Book> availableBooks = new ArrayList<>();
        for(Book book: books){
            if(book.isAvailable()){
                availableBooks.add(book);
            }
        }
        return availableBooks;
    }

    // Get all books in the library
    public List<Book> getAllBooks(){
        List<Book> allBooks = new ArrayList<>();
        for(Book book: books){
            allBooks.add(book);
        }
        return allBooks;
    }

    /////////////////////////////////////Get all borrowed books in the library//////////////////////////////////// 

    // Get all borrowed books in the library
    @JsonProperty("borrowedBooksList")
    public List<Book> getAllBorrowedBooks() {
        List<Book> allBorrowed = new ArrayList<>();
        for (List<Book> books : borrowedBooks.values()) {
            allBorrowed.addAll(books);
        }
        return allBorrowed;
    }

    /////////////////////////////////////Get all borrowers in the library and a list of books they have borrowed//////////////////////////////////// 

    // Get all borrowers in the library and a list of books they have borrowed
    @JsonProperty("borrowerReport")
    public Map<String, List<Book>> getBorrowerReport() {
        return new HashMap<>(borrowedBooks);
    }

    /////////////////////////////////////Get the total number of books in the library//////////////////////////////////// 

    // Get the total number of books in the library
    @JsonIgnore
    public int getTotalBookCount(){
        return books.size();
    }

    /////////////////////////////////////Get the number of borrowed books in the library//////////////////////////////////// 

    // Get the number of borrowed books in the library
    @JsonIgnore
    public int getNumberOfBorrowedBooks() {
        return getAllBorrowedBooks().size();
    }

    /////////////////////////////////////Get the number of borrowers in the library//////////////////////////////////// 

    // Get the number of borrowers in the library
    @JsonIgnore
    public int getNumberOfBorrowers() {
        return borrowedBooks.size();
    }

    public List<Book> getBooks() {
        return new ArrayList<>(books);
    }
}