package com.prahkel.library.service.impl;

import org.hibernate.exception.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.prahkel.library.dto.AddBookCopyRequest;
import com.prahkel.library.dto.CreateBookRequest;
import com.prahkel.library.dto.UpdateBookRequest;
import com.prahkel.library.exception.BookAlreadyExistsException;
import com.prahkel.library.exception.BookCopyAlreadyExistException;
import com.prahkel.library.exception.BookCopyDeletionNotAllowedException;
import com.prahkel.library.exception.BookCopyNotFoundException;
import com.prahkel.library.exception.BookDeletionNotAllowedException;
import com.prahkel.library.exception.BookHasHistoryException;
import com.prahkel.library.exception.BookNotAvailableException;
import com.prahkel.library.exception.BookNotFoundException;
import com.prahkel.library.exception.BookWithActiveLoanCannotBeArchivedException;
import com.prahkel.library.exception.InvalidCopyStateException;
import com.prahkel.library.model.Book;
import com.prahkel.library.model.BookCopy;
import com.prahkel.library.model.BookLifecycleStatus;
import com.prahkel.library.model.BorrowingStatus;
import com.prahkel.library.repository.BookCopyRepository;
import com.prahkel.library.repository.BookRepository;
import com.prahkel.library.repository.BorrowingRepository;
import com.prahkel.library.service.BookService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.*;
import java.time.LocalDate;
import java.time.Clock;


@Service
@Validated
@Transactional(readOnly = true)
public class BookServiceImpl implements BookService{
    
    private final BookRepository bookRepository;
    private final BookCopyRepository bookCopyRepository;
    private final BorrowingRepository borrowingRepository;
    private final Clock clock;


    public BookServiceImpl(
            BookRepository bookRepository,
            BookCopyRepository bookCopyRepository, 
            BorrowingRepository borrowingRepository,
            Clock clock) {

        this.bookRepository = bookRepository;
        this.bookCopyRepository = bookCopyRepository;
        this.borrowingRepository = borrowingRepository;
        this.clock = clock;
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
        try {
            return bookRepository.saveAndFlush(book);
        } catch (DataIntegrityViolationException exception) {
            if (isIsbnConstraintViolation(exception)) {
                throw new BookAlreadyExistsException(
                    "A book with ISBN " + request.isbn() + " already exists."
                );
            }
            throw exception;
        }
    }

    @Override 
    @Transactional
    public List<Book> createBooks(@NotEmpty List<@Valid CreateBookRequest> requests) {
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
            .toList();

        try {
            return bookRepository.saveAllAndFlush(books);
        } catch (DataIntegrityViolationException exception) {
            if (isIsbnConstraintViolation(exception)) {
                throw new BookAlreadyExistsException(
                    "A book with one of the supplied ISBNs already exists."
                );
            }
            throw exception;
        }
    }

    // Adding copies of a book 
    @Override 
    @Transactional
    public BookCopy addCopyToBook(@NotNull @Positive Long bookId, @Valid @NotNull AddBookCopyRequest request){
        // Validate books existence 
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException("Book with ID " + bookId + " not found.")
        );

        if (book.getLifecycleStatus() != BookLifecycleStatus.ACTIVE) {
            throw new BookNotAvailableException("Archived books cannot receive new copies.");
        }

        // Validate barcode uniqueness
        if(bookCopyRepository.existsByBarcode(request.barcode())){
            throw new BookCopyAlreadyExistException("Barcode already exists: " + request.barcode());
        }

        LocalDate today = LocalDate.now(clock);
        LocalDate acquired = request.acquiredDate() != null 
            ? request.acquiredDate()
            : today;

        // Aquired date cannot be in the future
        if (acquired.isAfter(today)){
            throw new InvalidCopyStateException("Acquired date cannot be in the future.");
        }

        BookCopy copy = new BookCopy(request.barcode(), 
            request.condition(), 
            acquired
        );

        book.addCopy(copy); // Keeps both sides of the relationship consistent via cascade mapping

        try {
            return bookCopyRepository.saveAndFlush(copy);
        } catch (DataIntegrityViolationException exception) {
            if (isBarcodeConstraintViolation(exception)) {
                throw new BookCopyAlreadyExistException(
                    "Barcode already exists: " + request.barcode()
                );
            }
            throw exception;
        }
    }

    @Transactional(readOnly = true)
    public List<BookCopy> getCopiesForBook(@NotNull @Positive Long bookId){
        bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException("Book with ID: " + bookId + " not found.")
        );
        
        return bookCopyRepository.findByBookId(bookId);
    }

    @Transactional(readOnly = true)
    public List<BookCopy> getAvailableCopiesForBook(@NotNull @Positive Long bookId){
        Book book = bookRepository.findById(bookId)
            .orElseThrow (() -> new BookNotFoundException("Book with ID: " + bookId + " not found.")
        );

        if (book.getLifecycleStatus() != BookLifecycleStatus.ACTIVE) {
            return List.of();
        }
        
        return bookCopyRepository.findByBookIdAndIsAvailableTrue(bookId);
    }

    @Transactional
    public void deleteCopy(@NotNull @Positive Long copyId){
        BookCopy copy =bookCopyRepository.findById(copyId)
            .orElseThrow(() -> new BookCopyNotFoundException("Copy with ID: " + copyId + " not found.")
        );

        if(!copy.isAvailable()){
            throw new BookCopyDeletionNotAllowedException("Cannot delete a copy that is currently borrowed.");
        }

        if (borrowingRepository.existsByBookCopyId(copyId)) {
            throw new BookCopyDeletionNotAllowedException(
                "Cannot delete a copy with borrowing history."
            );
        }

        bookCopyRepository.delete(copy);
    }

    private boolean isBarcodeConstraintViolation(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) {
                return "book_copy_barcode".equalsIgnoreCase(violation.getConstraintName());
            }
        }
        return false;
    }

    private boolean isIsbnConstraintViolation(Throwable exception) {
        for (Throwable cause = exception; cause != null; cause = cause.getCause()) {
            if (cause instanceof ConstraintViolationException violation) {
                return "book_isbn".equalsIgnoreCase(violation.getConstraintName());
            }
        }
        return false;
    }
    // Copy method ends here

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
        return bookRepository.findBooksWithAvailableCopies();
    }

    public List<Book> getUnavailableBooks() {
        return bookRepository.findBooksWithUnavailableCopies();
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
        return bookRepository.countBooksWithAvailableCopies();
    }

    public long countUnavailableBooks() {
        return bookRepository.countBooksWithUnavailableCopies();
    }

    @Transactional
    public void archiveBook(@NotNull @Positive Long bookId){
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException("Book with ID: " + bookId + " not found.")
        );

        boolean openLoan = borrowingRepository.existsByBookCopyBookIdAndStatusIn(
            bookId, 
            List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE)
        );

        if(openLoan){
            throw new BookWithActiveLoanCannotBeArchivedException(
                "Cannot achive a book with an active loan"
            );
        }

        book.setLifecycleStatus(BookLifecycleStatus.ARCHIVED);

        bookRepository.saveAndFlush(book);

    }
    

    // Method for deleting a book by its ID
    @Transactional
    public void deleteBook(@NotNull @Positive Long bookId) {
        Book book = bookRepository.findById(bookId)
            .orElseThrow(() -> new BookNotFoundException("Book with ID " + bookId + " not found."));

        // 1) Reject if any copy of this book has an open borrowing (ACTIVE/OVERDUE)
        boolean openBorrowingsExist = borrowingRepository
            .existsByBookCopyBookIdAndStatusIn(bookId, List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE));
        if (openBorrowingsExist) {
            throw new BookDeletionNotAllowedException("Cannot delete book with open borrowings. Archive/resolve first.");
        }

        // 2) If any borrowing history exists, do NOT delete; require explicit archive
        boolean historyExists = borrowingRepository.existsByBookCopyBookId(bookId);
        if (historyExists) {
            throw new BookHasHistoryException("Book has borrowing history. Archive it instead of deleting.");
        }

        // 3) Ensure no copies remain (orphan removal is fine, but this guard is explicit and safe)
        boolean copiesExist = bookCopyRepository.existsByBookId(bookId);
        if (copiesExist) {
            throw new BookDeletionNotAllowedException("Remove all copies before deleting the book.");
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

        try {
            return bookRepository.saveAndFlush(existingBook);
        } catch (DataIntegrityViolationException exception) {
            if (isIsbnConstraintViolation(exception)) {
                throw new BookAlreadyExistsException(
                    "A book with ISBN " + request.isbn() + " already exists."
                );
            }
            throw exception;
        }
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
