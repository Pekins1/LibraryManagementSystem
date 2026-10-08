package com.prahkel.library.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
import com.prahkel.library.model.BookCondition;
import com.prahkel.library.model.BookCopy;
import com.prahkel.library.model.BookLifecycleStatus;
import com.prahkel.library.model.BorrowingStatus;
import com.prahkel.library.repository.BookCopyRepository;
import com.prahkel.library.repository.BookRepository;
import com.prahkel.library.repository.BorrowingRepository;
import com.prahkel.library.service.impl.BookServiceImpl;

import java.time.LocalDate;
import java.time.Clock;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;




@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock 
    private BookRepository bookRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock 
    private BorrowingRepository borrowingRepository;
    
    private BookServiceImpl bookService;
    private Clock fixedClock;

    @BeforeEach 
    void setUp() {
        fixedClock = Clock.fixed(
            LocalDate.of(2026, 10, 2)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant(),
            ZoneId.systemDefault()
        );

        bookService = new BookServiceImpl(
            bookRepository, 
            bookCopyRepository,
            borrowingRepository, 
            fixedClock
        );
    }

    @Test
    void createBook_shouldSaveBookWhenIsbnDoesNotExist() {
        CreateBookRequest request = new CreateBookRequest(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );

        Book savedBook = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008 
        );

        when(bookRepository.existsByIsbn(request.isbn())).thenReturn(false);
        when(bookRepository.saveAndFlush(any(Book.class))).thenReturn(savedBook);

        Book result = bookService.createBook(request);

        assertNotNull(result);
        assertEquals("Clean Code",result.getTitle());
        assertEquals("978-0132350884",result.getIsbn());

        verify(bookRepository).existsByIsbn(request.isbn());
        verify(bookRepository).saveAndFlush(any(Book.class));

    }

    @Test
    void createBook_shouldThrowWhenIsbnAlreadyExists(){
        CreateBookRequest request = new CreateBookRequest(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );

        when(bookRepository.existsByIsbn(request.isbn())).thenReturn(true);

        assertThrows(BookAlreadyExistsException.class,
            () -> bookService.createBook(request)
        );

        verify(bookRepository).existsByIsbn(request.isbn());
        verify(bookRepository, never()).saveAndFlush(any(Book.class));
    }
    
    @Test 
    void addCopyToBook_shouldThrowWhenBookIsArchived(){
        Long bookId = 1L;

        AddBookCopyRequest request = new AddBookCopyRequest(
            "LMS-9780132350884-01",
            BookCondition.NEW,
            LocalDate.of(2026,10,01)
        );

        Book savedBook = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008 
        );

        savedBook.setId(bookId);
        savedBook.setLifecycleStatus(BookLifecycleStatus.ARCHIVED);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(savedBook));

        assertThrows(BookNotAvailableException.class, 
            () -> bookService.addCopyToBook(bookId, request)
        );

        verify(bookRepository).findById(bookId);
        verify(bookCopyRepository, never()).saveAndFlush(any(BookCopy.class));

    }

    @Test
    void addCopyToBook_shouldThrowWhenBarcodeAlreadyExist(){
         Long bookId = 1L;

        AddBookCopyRequest request = new AddBookCopyRequest(
            "LMS-9780132350884-01",
            BookCondition.NEW,
            LocalDate.of(2026,10,01)
        );

        Book savedBook = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008 
        );

        savedBook.setId(bookId);
        savedBook.setLifecycleStatus(BookLifecycleStatus.ACTIVE);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(savedBook));
        when(bookCopyRepository.existsByBarcode(request.barcode())).thenReturn(true);

        assertThrows(BookCopyAlreadyExistException.class,
            () -> bookService.addCopyToBook(bookId, request)
        );

        verify(bookRepository).findById(bookId);
        verify(bookCopyRepository).existsByBarcode(request.barcode());
        verify(bookCopyRepository, never()).saveAndFlush(any(BookCopy.class));

    }

    @Test 
    void addCopyToBook_shouldThrowWhenAcquiredDateIsAfutureDate(){
          Long bookId = 1L;

        AddBookCopyRequest request = new AddBookCopyRequest(
            "LMS-9780132350884-01",
            BookCondition.NEW,
            LocalDate.of(2026,10,03)
        );

        Book savedBook = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008 
        );
        savedBook.setId(bookId);
        savedBook.setLifecycleStatus(BookLifecycleStatus.ACTIVE);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(savedBook));
        when(bookCopyRepository.existsByBarcode(request.barcode())).thenReturn(false);

        assertThrows(InvalidCopyStateException.class,
            () -> bookService.addCopyToBook(bookId, request)
        );

        verify(bookRepository).findById(bookId);
        verify(bookCopyRepository).existsByBarcode(request.barcode());
        verify(bookCopyRepository, never()).saveAndFlush(any(BookCopy.class));
    }

    @Test 
    void addCopyToBook_shouldSaveCopyWhenAdded(){
        Long bookId = 1L;

        AddBookCopyRequest request = new AddBookCopyRequest(
            "LMS-9780132350884-01",
            BookCondition.LIKE_NEW,
            LocalDate.of(2026,10,02)
        );

        Book savedBook = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008 
        );
        savedBook.setId(bookId);
        savedBook.setLifecycleStatus(BookLifecycleStatus.ACTIVE);

        BookCopy savedCopy = new BookCopy(
            request.barcode(),
            request.condition(),
            request.acquiredDate()
        );

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(savedBook));
        when(bookCopyRepository.existsByBarcode(request.barcode())).thenReturn(false);
        when(bookCopyRepository.saveAndFlush(any(BookCopy.class))).thenReturn(savedCopy);

        BookCopy result = bookService.addCopyToBook(bookId, request);
        
        assertNotNull(result);
        assertEquals(request.barcode(), result.getBarcode());
        assertEquals(request.condition(), result.getCondition());
        assertEquals(request.acquiredDate(), result.getAcquiredDate());
        assertTrue(savedCopy.isAvailable());
        assertEquals(0, savedCopy.getTimesBorrowed());


        verify(bookRepository).findById(bookId);
        verify(bookCopyRepository).existsByBarcode(request.barcode());
        verify(bookCopyRepository).saveAndFlush(any(BookCopy.class));

    }

    @Test 
    void archiveBook_shouldThrowWhenBookIsBorrowed(){
        Long bookId = 4L;

        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );
        book.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookCopyBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE,
                BorrowingStatus.OVERDUE))
        )).thenReturn(true);

        assertThrows(BookWithActiveLoanCannotBeArchivedException.class,
            () -> bookService.archiveBook(bookId)
        );

        verify(bookRepository).findById(bookId);
        verify(borrowingRepository).existsByBookCopyBookIdAndStatusIn(eq(bookId),eq(List.of(BorrowingStatus.ACTIVE,BorrowingStatus.OVERDUE)));
        verify(bookRepository, never()).saveAndFlush(any(Book.class));

    }

    @Test 
    void archiveBook_shouldSaveWhenBookIsNotBorrowed(){
        Long bookId = 4L;

        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );
        book.setId(bookId);

        book.setLifecycleStatus(BookLifecycleStatus.ACTIVE);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookCopyBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE,
                BorrowingStatus.OVERDUE))
        )).thenReturn(false);
        when(bookRepository.saveAndFlush(any(Book.class))).thenReturn(book);

        bookService.archiveBook(bookId);

        assertEquals(BookLifecycleStatus.ARCHIVED, book.getLifecycleStatus());

        verify(bookRepository).findById(bookId);
        verify(borrowingRepository).existsByBookCopyBookIdAndStatusIn(eq(bookId),eq(List.of(BorrowingStatus.ACTIVE,BorrowingStatus.OVERDUE)));
        verify(bookRepository).saveAndFlush(any(Book.class));
    }


    @Test 
    void deleteCopy_shouldThrowWhenCopyIsNotFound(){
        Long copyId = 5L;

       when(bookCopyRepository.findById(copyId)).thenReturn(Optional.empty());

       assertThrows(BookCopyNotFoundException.class,
            () -> bookService.deleteCopy(copyId)
       );

       verify(bookCopyRepository).findById(copyId);
       verify(bookCopyRepository, never()).delete(any(BookCopy.class));
        
    }

    @Test 
    void deleteCopy_shouldThrowWhenCopyIsBorrowed(){
        Long copyId = 5L;

        BookCopy bookCopy = new BookCopy(
            "LMS-9780132350884-01", 
            BookCondition.FAIR,
            LocalDate.of(2026,10,03)
        );
        bookCopy.setIsAvailable(false);
        bookCopy.setTimesBorrowed(2);

        bookCopy.setId(copyId);

        when(bookCopyRepository.findById(copyId)).thenReturn(Optional.of(bookCopy));
        
        assertThrows(BookCopyDeletionNotAllowedException.class,
            () -> bookService.deleteCopy(copyId)
        );

        verify(bookCopyRepository).findById(copyId);
        verify(bookCopyRepository, never()).delete(any(BookCopy.class));
    }

    @Test 
    void deleteCopy_shouldThrowWhenCopyHasBorrowingHistory(){
        Long copyId = 5L;

        BookCopy bookCopy = new BookCopy(
            "LMS-9780132350884-01", 
            BookCondition.FAIR,
            LocalDate.of(2026,10,03)
        );
        bookCopy.setIsAvailable(true);
        bookCopy.setTimesBorrowed(2);

        bookCopy.setId(copyId);

        when(bookCopyRepository.findById(copyId)).thenReturn(Optional.of(bookCopy));
        when(borrowingRepository.existsByBookCopyId(copyId)).thenReturn(true);

        assertThrows(BookCopyDeletionNotAllowedException.class,
            () -> bookService.deleteCopy(copyId)
        );

        verify(bookCopyRepository).findById(copyId);
        verify(borrowingRepository).existsByBookCopyId(copyId);
        verify(bookCopyRepository, never()).delete(any(BookCopy.class));
    }

     @Test 
    void deleteCopy_shouldDeleteSuccessfully(){
        Long copyId = 5L;

        BookCopy bookCopy = new BookCopy(
            "LMS-9780132350884-01", 
            BookCondition.FAIR,
            LocalDate.of(2026,10,03)
        );
        bookCopy.setIsAvailable(true);
        bookCopy.setTimesBorrowed(0);

        bookCopy.setId(copyId);

        when(bookCopyRepository.findById(copyId)).thenReturn(Optional.of(bookCopy));
        when(borrowingRepository.existsByBookCopyId(copyId)).thenReturn(false);

        bookService.deleteCopy(copyId);

        assertTrue(bookCopy.isAvailable());
        assertEquals(0, bookCopy.getTimesBorrowed());

        verify(bookCopyRepository).findById(copyId);
        verify(borrowingRepository).existsByBookCopyId(copyId);
        verify(bookCopyRepository).delete(bookCopy);
    }


    @Test
    void deleteBook_shouldThrowWhenBookIsCurrentlyBorrowed(){
        Long bookId = 1L;

        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );
        book.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookCopyBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        )).thenReturn(true);

        assertThrows(BookDeletionNotAllowedException.class,
            () -> bookService.deleteBook(bookId)
        );


        verify(bookRepository).findById(bookId);
        verify(borrowingRepository).existsByBookCopyBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        );
        
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test 
    void deleteBook_shouldThrowWhenBookHasBorrowHistory(){
         Long bookId = 1L;

        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );
        book.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookCopyBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        )).thenReturn(false);
        when(borrowingRepository.existsByBookCopyBookId(bookId)).thenReturn(true);

        assertThrows(BookHasHistoryException.class,
            () -> bookService.deleteBook(bookId)
        );
        
        verify(bookRepository).findById(bookId);
        verify(borrowingRepository).existsByBookCopyBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        );
        verify(borrowingRepository).existsByBookCopyBookId(bookId);
        
        verify(bookRepository, never()).delete(any(Book.class));
    }

    @Test 
    void deleteBook_shouldThrowWhenBookHasCopies(){
         Long bookId = 1L;

        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );
        book.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookCopyBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        )).thenReturn(false);
        when(borrowingRepository.existsByBookCopyBookId(bookId)).thenReturn(false);
        when(bookCopyRepository.existsByBookId(bookId)).thenReturn(true);

        assertThrows(BookDeletionNotAllowedException.class,
            () -> bookService.deleteBook(bookId)
        );
        
        verify(bookRepository).findById(bookId);
        verify(borrowingRepository).existsByBookCopyBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        );
        verify(borrowingRepository).existsByBookCopyBookId(bookId);
        verify(bookCopyRepository).existsByBookId(bookId);
        
        verify(bookRepository, never()).delete(any(Book.class));
    }
    
    @Test
    void deleteBook_shouldDeleteWhenBookIsNotCurrentlyBorrowed(){
        Long bookId = 1L;
        
        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );
        book.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowingRepository.existsByBookCopyBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        )).thenReturn(false);
        when(borrowingRepository.existsByBookCopyBookId(bookId)).thenReturn(false);
        when(bookCopyRepository.existsByBookId(bookId)).thenReturn(false);
        
        bookService.deleteBook(bookId);

        verify(bookRepository).findById(bookId);
        verify(borrowingRepository).existsByBookCopyBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        );
        verify(borrowingRepository).existsByBookCopyBookId(bookId);
        verify(bookCopyRepository).existsByBookId(bookId);
        verify(bookRepository).delete(book);

    }

    @Test
    void updateBook_shouldThrowWhenIsbnAlreadyExistsForAnotherBook() {
        Long bookId = 1L;
        UpdateBookRequest updateRequest = new UpdateBookRequest(
            "Clean Code v.2",
            "Robert C. Martin",
            "Software",
            "978-0132350995",
            2010  
        );

        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );
        book.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnAndIdNot(updateRequest.isbn(), bookId)).thenReturn(true);

        assertThrows(BookAlreadyExistsException.class, 
            () -> bookService.updateBook(bookId, updateRequest)
        );

        verify(bookRepository).findById(bookId);
        verify(bookRepository).existsByIsbnAndIdNot(updateRequest.isbn(), bookId);
        verify(bookRepository, never()).saveAndFlush(any(Book.class));

        
    }

    @Test
    void updateBook_shouldUpdateBookWhenIsbnIsUnique() {
        Long bookId = 1L;
        UpdateBookRequest updateRequest = new UpdateBookRequest(
            "Clean Code v.2",
            "Robert C. Martin",
            "Software",
            "978-0132350995",
            2010  
        );

        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );
        book.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookRepository.existsByIsbnAndIdNot(updateRequest.isbn(), bookId)).thenReturn(false);
        when(bookRepository.saveAndFlush(any(Book.class))).thenReturn(book);

        Book result = bookService.updateBook(bookId, updateRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Clean Code v.2",result.getTitle());
        assertEquals("978-0132350995",result.getIsbn());
        assertEquals(2010, result.getPublishedYear());

        verify(bookRepository).findById(bookId);
        verify(bookRepository).existsByIsbnAndIdNot(updateRequest.isbn(), bookId);
        verify(bookRepository).saveAndFlush(any(Book.class));

    }

    @Test 
    void getCopiesForBook_shouldReturnBookCopiesWhenFound(){
        Long bookId = 1L;

        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008 
        );
        book.setId(bookId);

        Long firstCopyId = 1L;

        BookCopy firstCopy = new BookCopy(
            "LMS-9780132350884-01", 
            BookCondition.FAIR,
            LocalDate.of(2026,10,02)
        );
        firstCopy.setIsAvailable(false);
        firstCopy.setTimesBorrowed(2);

        firstCopy.setId(firstCopyId);

        Long secondCopyId = 2L;

        BookCopy secondCopy = new BookCopy(
            "LMS-9780132350884-02", 
            BookCondition.FAIR,
            LocalDate.of(2026,10,03)
        );
        secondCopy.setIsAvailable(true);
        secondCopy.setTimesBorrowed(0);

        secondCopy.setId(secondCopyId);

        book.addCopy(firstCopy);
        book.addCopy(secondCopy);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookCopyRepository.findByBookId(bookId)).thenReturn(List.of(firstCopy, secondCopy));

        List<BookCopy> result = bookService.getCopiesForBook(bookId);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(firstCopy.getBarcode(), result.get(0).getBarcode());
        assertEquals(secondCopy.getBarcode(), result.get(1).getBarcode());
        
        verify(bookRepository).findById(bookId);

    }

     @Test 
    void getCopiesForBook_shouldThrowWhenBookDoesNotExist(){
        Long bookId = 1L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());
        
        assertThrows(BookNotFoundException.class,
            () -> bookService.getCopiesForBook(bookId)
        );

        verify(bookRepository).findById(bookId);

    }

    @Test 
    void getAvailableCopiesForBook_shouldReturnAvailableBookCopiesWhenFound(){
        Long bookId = 1L;

        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008 
        );
        book.setId(bookId);

        Long firstCopyId = 1L;

        BookCopy firstCopy = new BookCopy(
            "LMS-9780132350884-01", 
            BookCondition.FAIR,
            LocalDate.of(2026,10,02)
        );
        firstCopy.setIsAvailable(false);
        firstCopy.setTimesBorrowed(2);

        firstCopy.setId(firstCopyId);

        Long secondCopyId = 2L;

        BookCopy secondCopy = new BookCopy(
            "LMS-9780132350884-02", 
            BookCondition.FAIR,
            LocalDate.of(2026,10,03)
        );
        secondCopy.setIsAvailable(true);
        secondCopy.setTimesBorrowed(0);

        secondCopy.setId(secondCopyId);

        book.addCopy(firstCopy);
        book.addCopy(secondCopy);

        book.setLifecycleStatus(BookLifecycleStatus.ACTIVE);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(bookCopyRepository.findByBookIdAndIsAvailableTrue(bookId)).thenReturn(List.of(secondCopy));

        List<BookCopy> result = bookService.getAvailableCopiesForBook(bookId);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).isAvailable());
        assertEquals(secondCopy.getBarcode(), result.get(0).getBarcode());
        
        verify(bookRepository).findById(bookId);

    }

    @Test 
    void getAvailableCopiesForBook_shouldReturnAnEmptyListWhenBookIsArchived(){
        Long bookId = 1L;

        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008 
        );
        book.setId(bookId);


        Long firstCopyId = 1L;

        BookCopy firstCopy = new BookCopy(
            "LMS-9780132350884-01", 
            BookCondition.FAIR,
            LocalDate.of(2026,10,02)
        );
        firstCopy.setIsAvailable(false);
        firstCopy.setTimesBorrowed(2);

        firstCopy.setId(firstCopyId);

        Long secondCopyId = 2L;

        BookCopy secondCopy = new BookCopy(
            "LMS-9780132350884-02", 
            BookCondition.FAIR,
            LocalDate.of(2026,10,03)
        );
        secondCopy.setIsAvailable(true);
        secondCopy.setTimesBorrowed(0);

        secondCopy.setId(secondCopyId);

        book.addCopy(firstCopy);
        book.addCopy(secondCopy);

        book.setLifecycleStatus(BookLifecycleStatus.ARCHIVED);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        List<BookCopy> result = bookService.getAvailableCopiesForBook(bookId);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    
        
        verify(bookRepository).findById(bookId);

    }


    @Test
    void getBookById_shouldReturnBookWhenFound(){
        Long bookId = 1L;

        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008 
        );
        book.setId(bookId);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));

        Book result = bookService.getBookById(bookId);

        assertNotNull(result);
        assertEquals(bookId, result.getId());
        assertEquals("Clean Code", result.getTitle());
        assertEquals("978-0132350884", result.getIsbn());

        verify(bookRepository).findById(bookId);
    }

    @Test 
    void getBookById_shouldThrowWhenBookNotFound(){
        Long bookId = 1L;

        when(bookRepository.findById(bookId)).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class,
            () -> bookService.getBookById(bookId)
        );

        verify(bookRepository).findById(bookId);
    }

    @Test 
    void getBookByIsbn_shouldReturnBookWhenFound(){
        // Fake data
        String isbn = "978-0132350884";

        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008 
        );

        // Scenario being tested
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.of(book));

        // Captured Results 
        Book result = bookService.getBookByIsbn(isbn);

        // Verifying data accuracy from test
        assertNotNull(result);
        assertEquals(isbn, result.getIsbn());
        assertEquals("Robert C. Martin", result.getAuthor());

        // Verifying that method was actually called from 
        verify(bookRepository).findByIsbn(isbn);
    }

    @Test 
    void getBookByIsbn_shouldThrowWhenBookIsNotFound(){
        String isbn =  "978-0132350884";

        // Senario being tested
        when(bookRepository.findByIsbn(isbn)).thenReturn(Optional.empty());

        // Capture results and verify that exception is thrown
        assertThrows(BookNotFoundException.class, 
            () -> bookService.getBookByIsbn(isbn)
        );

        verify(bookRepository).findByIsbn(isbn);
    }

    @Test
    void createBooks_shouldThrowWhenDuplicateIsbnExistsInRequestList(){
         CreateBookRequest first = new CreateBookRequest(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );

        CreateBookRequest second = new CreateBookRequest(
            "Clean Code v2",
            "Robert C. Martin",
            "Software",
            "978-0132350884", // same ISBN
            2010  
        );

        List<CreateBookRequest> requests = List.of(first,second);

        when(bookRepository.existsByIsbn(requests.get(0).isbn())).thenReturn(false);

        assertThrows(BookAlreadyExistsException.class, 
            () -> bookService.createBooks(requests)
        );

        verify(bookRepository, times(1)).existsByIsbn(requests.get(0).isbn());
        verify(bookRepository, never()).saveAll(any());
    }

    @Test
    void createBooks_shouldThrowWhenDuplicateIsbnExistsInDatabase(){
          CreateBookRequest first = new CreateBookRequest(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );

        CreateBookRequest second = new CreateBookRequest(
            "Clean Code v2",
            "Robert C. Martin",
            "Software",
            "978-0132350995", // already exists in database
            2010  
        );

        List<CreateBookRequest> requests = List.of(first, second);

        when(bookRepository.existsByIsbn(requests.get(0).isbn())).thenReturn(false);
        when(bookRepository.existsByIsbn(requests.get(1).isbn())).thenReturn(true);

        assertThrows(BookAlreadyExistsException.class, 
            () -> bookService.createBooks(requests)
        );

        verify(bookRepository).existsByIsbn(requests.get(0).isbn());
        verify(bookRepository).existsByIsbn(requests.get(1).isbn());
        verify(bookRepository, never()).saveAll(any());
    }

    @Test
    void createBooks_shouldSaveAllBooksWhenValid(){
        CreateBookRequest first = new CreateBookRequest(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );

        CreateBookRequest second = new CreateBookRequest(
            "Refactoring",
            "Martin Fowler",
            "Software",
            "978-0201485677",
            1999 
        );

        List<CreateBookRequest> requests = List.of(first, second);

        Book firstSaved = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008  
        );

        Book secondSaved = new Book(
        "Refactoring",
        "Martin Fowler",
        "Software",
        "978-0201485677",
        1999
        );
        
        List<Book> savedBooks = List.of(firstSaved, secondSaved);
        

        when(bookRepository.existsByIsbn(requests.get(0).isbn())).thenReturn(false);
        when(bookRepository.existsByIsbn(requests.get(1).isbn())).thenReturn(false);
        when(bookRepository.saveAllAndFlush(any())).thenReturn(savedBooks);

        List<Book> result = bookService.createBooks(requests);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("Clean Code", result.get(0).getTitle());
        assertEquals("978-0132350884", result.get(0).getIsbn());

        assertEquals("Refactoring", result.get(1).getTitle());
        assertEquals("978-0201485677", result.get(1).getIsbn());

        verify(bookRepository).existsByIsbn(requests.get(0).isbn());
        verify(bookRepository).existsByIsbn(requests.get(1).isbn());
        verify(bookRepository).saveAllAndFlush(any());
    }
}
