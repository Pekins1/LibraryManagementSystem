package service.impl;

import dto.BorrowBookRequest;
import dto.ReturnBookRequest;

import model.Book;
import model.Borrower;
import model.Borrowing;
import model.BorrowingStatus;

import repository.BookRepository;
import repository.BorrowerRepository;
import repository.BorrowingRepository;

import service.impl.BorrowServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
// import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.Clock;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
public class BorrowServiceImplTest {
    
    @Mock
    private BookRepository bookRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    private BorrowServiceImpl borrowService;

    private Clock fixedClock;
    
    @BeforeEach
    void setUp(){
        fixedClock = Clock.fixed(
            LocalDate.of(2026,9,15)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant(),
            ZoneId.systemDefault()
        );

        borrowService = new BorrowServiceImpl(
            bookRepository,
            borrowerRepository,
            borrowingRepository,
            fixedClock
        );
    }

    @Test
    void borrowBook_shouldSaveBorrowingWhenBookIsAvailableAndBorrowerIsEligible(){
        Long bookId = 7L;
        Long borrowerId = 20L;
       
        
        BorrowBookRequest request = new BorrowBookRequest(
            7L, 20L
            );
            
        Book savedBook = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008 
        );
        savedBook.setIsAvailable(true);
        savedBook.setId(bookId);
            
        int before = savedBook.getTimesBorrowed();
        
        Borrower savedBorrower  = new Borrower(
            "Eric Ford",
            "testemail@gmail.com"
        );
        savedBorrower.setId(borrowerId);

        Borrowing savedBorrowing = new Borrowing(
            savedBook, 
            savedBorrower,
            LocalDate.of(2026,9,15)
        );

        
        when(bookRepository.findById(request.bookId())).thenReturn(Optional.of(savedBook));
        when(borrowerRepository.findById(request.borrowerId())).thenReturn(Optional.of(savedBorrower));

        when(borrowingRepository.countByBorrowerIdAndStatusIn(
            eq(request.borrowerId()), 
            eq(List.of(
            BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        )).thenReturn(2L);

        when(borrowingRepository.existsByBookIdAndBorrowerIdAndStatusIn(
            eq(request.bookId()),
            eq(request.borrowerId()),
            eq(List.of(
                BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        )).thenReturn(false);

        when(bookRepository.save(any(Book.class))).thenReturn(savedBook);
        when(borrowingRepository.save(any(Borrowing.class))).thenReturn(savedBorrowing);

        
        Borrowing result = borrowService.borrowBook(request);

        assertNotNull(result);
        assertEquals(savedBook, result.getBook());
        assertEquals(savedBorrower, result.getBorrower());
        assertEquals(LocalDate.of(2026,9,15),result.getBorrowDate());
        assertEquals(LocalDate.of(2026,9, 29),result.getDueDate());
        assertEquals(BorrowingStatus.ACTIVE,result.getStatus());
        assertEquals(new BigDecimal("0.00"), result.getLateFee());
        assertFalse(result.getBook().isAvailable());
        assertEquals(before + 1 , result.getBook().getTimesBorrowed());

        
        verify(bookRepository).findById(request.bookId());
        verify(borrowerRepository).findById(request.borrowerId());
        verify(borrowingRepository).countByBorrowerIdAndStatusIn( 
            eq(request.borrowerId()),
            eq(List.of(
                BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        );
        verify(borrowingRepository).existsByBookIdAndBorrowerIdAndStatusIn(
            eq(request.bookId()),
            eq(request.borrowerId()),
            eq(List.of(
                BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE))
        );
        verify(bookRepository).save(any(Book.class));
        verify(borrowingRepository).save(any(Borrowing.class));
    }

    @Test
    void borrowBook_shouldThrowWhenBookNotFound(){}
    @Test
    void borrowBook_shouldThrowWhenBorrowerNotFound() {}
    @Test
    void borrowBook_shouldThrowWhenBookIsNotAvailable() {}
    @Test
    void borrowBook_shouldThrowWhenBorrowerReachedLimit() {}
    @Test
    void borrowBook_shouldThrowWhenBorrowerAlreadyHasBook() {}
    @Test
    void returnBook_shouldReturnBookWhenBorrowingIsActive() {}
    @Test
    void returnBook_shouldThrowWhenBookNotCurrentlyBorrowedByBorrower() {}
    @Test
    void returnBook_shouldSetLateFeeWhenReturnedAfterDueDate() {}
    @Test
    void returnBook_shouldSetBookAvailableAfterReturn() {}

}
