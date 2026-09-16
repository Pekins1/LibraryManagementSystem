package service.impl;

import model.Book;
import model.BorrowingStatus;
import dto.CreateBookRequest;
import dto.UpdateBookRequest;
import exception.BookAlreadyExistsException;
import exception.BookNotFoundException;
import exception.BookDeletionNotAllowedException;
import repository.BookRepository;
import repository.BorrowingRepository;
import service.BookService;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.*;

@Service
@Validated
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService{
    
    private final BookRepository bookRepository;
    private final BorrowingRepository borrowingRepository;

    public BookServiceImpl(BookRepository bookRepository, BorrowingRepository borrowingRepository) {
        this.bookRepository = bookRepository;
        this.borrowingRepository = borrowingRepository;
    }

    // Method to create a new book in the database
    // Setters
    @Override 
    @Transactional
    public Book createBook(@Valid @NotNull CreateBookRequest request) {
    // Check if a book with the same ISBN already exists
    // Exceptions are now handled as unchecked exceptions, so we don't need to declare them in the method signature.
        if (bookRepository.existsByIsbn(request.isbn())) {
            throw new BookAlreadyExistsException("A book with ISBN " + request.isbn() + " already exists.");
        }

        Book book = toBook(request);
        book.setIsAvailable(true); // Set the book as available when created
        return bookRepository.save(book);
    }

    @Override 
    @Transactional
    public List<Book> createBooks(@NotBlank List<@Valid CreateBookRequest> requests) {
        // Check for duplicate ISBNs in the input list
        Set<String> seenIsbns = new HashSet<>();

        for (CreateBookRequest request : requests) {
            // Check if the ISBN has already been seen in the input list
            if (!seenIsbns.add(request.isbn())) {
                throw new BookAlreadyExistsException(
                    "Duplicate ISBN " + request.isbn() + " found in the input list.");
            }

            // Check if a book with the same ISBN already exists in the database
            if (bookRepository.existsByIsbn(request.isbn())){
                throw new BookAlreadyExistsException(
                    "A book with ISBN " + request.isbn() + " already exists in the database."
                );
            }
        }

        // If all checks pass, save all books to the database
        List<Book> books = requests.stream()
            .map(this::toBook)
            .peek(book -> book.setIsAvailable(true)) // Set all books as available when created
            .toList();

        return bookRepository.saveAll(books);
    }


    // Getters for readOnly operations

    // Method to retrieve books by Id
    public Book getBookById(@NotNull @Positive Long id) {
        return bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException("Book with ID " + id + " not found."));
    }

    // Method to retrieve a book by its ISBN
    public Book getBookByIsbn(@NotNull @NotBlank String isbn) {
        return bookRepository.findByIsbn(isbn)
            .orElseThrow(() -> new BookNotFoundException("Book with ISBN " + isbn + " not found."));

    }

    // Method to retrieve all books
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    public List<Book> getAvailableBooks() {
        return bookRepository.findByIsAvailableTrue();
    }

    public List<Book> getUnavailableBooks() {
        return bookRepository.findByIsAvailableFalse();
    }

    public List<Book> findBooksByTitle(@NotBlank String title) {
        return bookRepository.findByTitleContainingIgnoreCase(title);
    }

    public List<Book> findBooksByAuthor(@NotBlank  String author) {
        return bookRepository.findByAuthorContainingIgnoreCase(author);
    }

    public List<Book> findBooksByGenre(@NotBlank String genre) {
        return bookRepository.findByGenreContainingIgnoreCase(genre);
    }

    // Optional area but good to have.
    public List<Book> findBooksByAuthorAndGenre(@NotBlank String author, @NotBlank String genre) {
        return bookRepository.findByAuthorContainingIgnoreCaseAndGenreContainingIgnoreCase(author, genre);
    }

    public List<Book> findBooksByTitleAndAuthor(@NotBlank  String title, @NotBlank String author) {
        return bookRepository.findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(title, author);
    }

    public long countAvailableBooks() {
        return bookRepository.countByIsAvailableTrue();
    }

    public long countUnavailableBooks() {
        return bookRepository.countByIsAvailableFalse();
    }
    

    // Method for deleting a book by its ID
    @Override 
    @Transactional
    public void deleteBook(@NotNull @Positive Long bookId){
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException(
                "Book with ID " + bookId + " not found."
            )); // This will throw BookNotFoundException if the book doesn't exist;

        // check if the book is currently borrowed (not available)
        boolean currentlyBorrowed = borrowingRepository.existsByBookIdAndStatusIn(
                bookId, 
            List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE));

        if (currentlyBorrowed) {
            throw new BookDeletionNotAllowedException(
                "Cannot delete book that is currently borrowed. Book id: "
            + bookId
            );
        }

        bookRepository.delete(book);

    }

    // Method for updating book
    @Override 
    @Transactional
    public Book updateBook(@Positive Long id, @Valid @NotNull UpdateBookRequest request) {
        Book existingBook = bookRepository.findById(id)
            .orElseThrow(() -> new BookNotFoundException(
                "Book with ID "+ id + " not found."));

        if (bookRepository.existsByIsbnAndIdNot(request.isbn(), id)) {
            throw new BookAlreadyExistsException(
                    "A book with ISBN " + request.isbn() + " already exists.");
        }

        existingBook.setTitle(request.title());
        existingBook.setAuthor(request.author());
        existingBook.setGenre(request.genre());
        existingBook.setIsbn(request.isbn());
        existingBook.setPublishedYear(request.publishedYear());

        return bookRepository.save(existingBook);
    }

    private Book toBook(CreateBookRequest request) {
        return new Book(
            request.title(),
            request.author(),
            request.genre(),
            request.isbn(),
            request.publishedYear()
        );
    }
}
