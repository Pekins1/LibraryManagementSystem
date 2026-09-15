package service.impl;

import dto.CreateBookRequest;
import dto.UpdateBookRequest;
import exception.BookAlreadyExistsException;
import exception.BookNotFoundException;
import model.Book;
import model.BorrowingStatus;
import exception.BookDeletionNotAllowedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.BookRepository;
import repository.BorrowingRepository;
import service.impl.BookServiceImpl;

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
    private BorrowingRepository borrowingRepository;

    @InjectMocks
    private BookServiceImpl bookService;

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
        savedBook.setIsAvailable(true);

        when(bookRepository.existsByIsbn(request.isbn())).thenReturn(false);
        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);

        Book result = bookService.createBook(request);

        assertNotNull(result);
        assertEquals("Clean Code",result.getTitle());
        assertEquals("978-0132350884",result.getIsbn());
        assertTrue(result.isAvailable());

        verify(bookRepository).existsByIsbn(request.isbn());
        verify(bookRepository).save(any(Book.class));

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
        verify(bookRepository, never()).save(any(Book.class));
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
        when(borrowingRepository.existsByBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        )).thenReturn(true);

        assertThrows(BookDeletionNotAllowedException.class,
            () -> bookService.deleteBook(bookId)
        );


        verify(bookRepository).findById(bookId);
        verify(borrowingRepository).existsByBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        );
        
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
        when(borrowingRepository.existsByBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        )).thenReturn(false);
        
        bookService.deleteBook(bookId);

        verify(bookRepository).findById(bookId);
        verify(borrowingRepository).existsByBookIdAndStatusIn(
            eq(bookId),
            eq(List.of(BorrowingStatus.ACTIVE,BorrowingStatus.OVERDUE))
        );

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
        verify(bookRepository, never()).save(any(Book.class));

        
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
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        Book result = bookService.updateBook(bookId, updateRequest);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("Clean Code v.2",result.getTitle());
        assertEquals("978-0132350995",result.getIsbn());
        assertEquals(2010, result.getPublishedYear());

        verify(bookRepository).findById(bookId);
        verify(bookRepository).existsByIsbnAndIdNot(updateRequest.isbn(), bookId);
        verify(bookRepository).save(any(Book.class));

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
        firstSaved.setIsAvailable(true);

        Book secondSaved = new Book(
        "Refactoring",
        "Martin Fowler",
        "Software",
        "978-0201485677",
        1999
        );
        secondSaved.setIsAvailable(true);
        
        List<Book> savedBooks = List.of(firstSaved, secondSaved);
        

        when(bookRepository.existsByIsbn(requests.get(0).isbn())).thenReturn(false);
        when(bookRepository.existsByIsbn(requests.get(1).isbn())).thenReturn(false);
        when(bookRepository.saveAll(any())).thenReturn(savedBooks);

        List<Book> result = bookService.createBooks(requests);

        assertNotNull(result);
        assertEquals(2, result.size());

        assertEquals("Clean Code", result.get(0).getTitle());
        assertEquals("978-0132350884", result.get(0).getIsbn());
        assertTrue(result.get(0).isAvailable());

        assertEquals("Refactoring", result.get(1).getTitle());
        assertEquals("978-0201485677", result.get(1).getIsbn());
        assertTrue(result.get(1).isAvailable());

        verify(bookRepository).existsByIsbn(requests.get(0).isbn());
        verify(bookRepository).existsByIsbn(requests.get(1).isbn());
        verify(bookRepository).saveAll(any());
    }
}
