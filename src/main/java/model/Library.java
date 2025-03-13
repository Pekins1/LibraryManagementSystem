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

public class Library implements Serializable {

    // Attributes
    private List<Book> books;
    private Map<String, List<Book>> borrowedBooks;
    static final int MAX_BORROW_LIMIT = 6;

    // Constructor
    public Library(){
        this.books = new ArrayList<>();
        this.borrowedBooks = new HashMap<>();
    }

    /////////////////////////////////////Add a book to the library////////////////////////////////////

    // Add a book to the library
    public void addBook(Book book) throws BookAlreadyExistsException {
        // check is the book is already in the library

        if(book.isAvailable()){
            throw new BookAlreadyExistsException("Book found in the library.");
        }
        // add the book to the library
        books.add(book);
        // update the book avalability
        book.setIsAvailable(true);
    }

    /////////////////////////////////////Add a list of books to the library//////////////////////////////////// 

    public void addBooks(List<Book> books) throws BookAlreadyExistsException {
        // iterate the list of books and check if they are already in the library
        for(Book book : books){
            if(book.isAvailable()){
                throw new BookAlreadyExistsException(book + ": Book found in the library.");
            }
            // add the books to the library
            // this.books is the list of books in the library
            this.books.add(book);
            // update the book availability
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
        book.setIsAvailable(false);
    }

    /////////////////////////////////////Search for a book in the library////////////////////////////////////
       
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
    public void borrowBook(String isbn, String borrowerName) 
        throws BookNotFoundException, BookNotAvailableException, BorrowLimitExceededException {
        // find the book by isbn
        Book book = findBookByISBN(isbn);
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
    public void returnBook(String isbn, String borrowerName) 
        throws BookNotFoundException, BookNotBorrowedException {
        // check if the book is borrowed
        Book book = findBookByISBN(isbn);
        if(book.isAvailable()){
            throw new BookNotBorrowedException("Book is not borrowed.");
        }
        // remove the book from the borrower's list of borrowed books
        List<Book> booksBorrowed = borrowedBooks.get(borrowerName);
        booksBorrowed.remove(book);
        // update the borrowedBooks Map
        borrowedBooks.put(borrowerName,booksBorrowed);
        // update the book availability
        book.setIsAvailable(true);
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

    /////////////////////////////////////Get all borrowed books in the library//////////////////////////////////// 

    // Get all borrowed books in the library
    public List<Book> getAllBorrowedBooks(){
        // iterate the borrowedBooks Map and check if the book is borrowed
        List<Book> borrowedBooks = new ArrayList<>();
        // iterate the borrowedBooks Map and add the books to the list
        for(List<Book> books: this.borrowedBooks.values()){
            for(Book book: books){
                if(!book.isAvailable()){
                    borrowedBooks.add(book);
                }
             }
        }
        return borrowedBooks;

    }

    /////////////////////////////////////Get all borrowers in the library and a list of books they have borrowed//////////////////////////////////// 

    // Get all borrowers in the library and a list of books they have borrowed
    public Map<String,List<Book>> getBorrowerReport() {
        Map<String,List<Book>> borrowers = new HashMap<>();
        // iterate the borrowedBooks Map and add the books to the list
        for(Map.Entry<String,List<Book>> entry: borrowedBooks.entrySet()){
            String borrowerName = entry.getKey();
            List<Book> booksBorrowed = entry.getValue();

            borrowers.put(borrowerName,booksBorrowed);
        }
        return borrowers;
    }

    /////////////////////////////////////Get the total number of books in the library//////////////////////////////////// 

    // Get the total number of books in the library
    public int getTotalBookCount(){
        return books.size();
    }

    /////////////////////////////////////Get the number of borrowed books in the library//////////////////////////////////// 

    // Get the number of borrowed books in the library
    public int getNumberOfBorrowedBooks(){
        return getAllBorrowedBooks().size();    
    }

    /////////////////////////////////////Get the number of borrowers in the library//////////////////////////////////// 

    // Get the number of borrowers in the library
    public int getNumberOfBorrowers(){
        return getBorrowerReport().size();
    }
}