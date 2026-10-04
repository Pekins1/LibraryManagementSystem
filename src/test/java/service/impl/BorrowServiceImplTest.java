package service.impl;

import dto.BorrowBookRequest;
import dto.ReturnBookRequest;
import exception.BookAlreadyBorrowedException;
import exception.BookNotAvailableException;
import exception.BookNotFoundException;
import exception.BorrowLimitExceededException;
import exception.BorrowerNotFoundException;
import model.Book;
import model.BookCondition;
import model.BookCopy;
import model.BookLifecycleStatus;
import model.Borrower;
import model.Borrowing;
import model.BorrowingStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import repository.BookCopyRepository;
import repository.BookRepository;
import repository.BorrowerRepository;
import repository.BorrowingRepository;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BorrowServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private BorrowerRepository borrowerRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    private BorrowServiceImpl borrowService;

    private final List<BorrowingStatus> OPEN_STATUSES = List.of(
        BorrowingStatus.ACTIVE,
        BorrowingStatus.OVERDUE
    );

    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(
            LocalDate.of(2026, 9, 15)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant(),
            ZoneId.systemDefault()
        );

        borrowService = new BorrowServiceImpl(
            bookRepository,
            bookCopyRepository,
            borrowerRepository,
            borrowingRepository,
            fixedClock
        );
    }

    @Test
    void borrowBook_shouldSaveBorrowingWhenBookIsAvailableAndBorrowerIsEligible() {
        Long bookId = 7L;
        Long borrowerId = 20L;

        Book book = new Book(
            "Clean Code",
            "Robert C. Martin",
            "Software",
            "978-0132350884",
            2008
        );
        book.setId(bookId);
        book.setLifecycleStatus(BookLifecycleStatus.ACTIVE);

        Borrower borrower = new Borrower("Eric Ford", "testemail@gmail.com");
        borrower.setId(borrowerId);

        BookCopy copy = new BookCopy("BC-100", BookCondition.NEW, LocalDate.of(2026, 9, 1));
        copy.setBook(book);
        copy.setIsAvailable(true);

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(book));
        when(borrowerRepository.findByIdForUpdate(borrowerId)).thenReturn(Optional.of(borrower));
        when(borrowingRepository.existsByBookCopyBookIdAndBorrowerIdAndStatusIn(
            eq(bookId), eq(borrowerId), eq(OPEN_STATUSES)
        )).thenReturn(false);
        when(borrowingRepository.countByBorrowerIdAndStatusIn(
            eq(borrowerId), eq(OPEN_STATUSES)
        )).thenReturn(2L);
        when(bookCopyRepository.findFirstByBookIdAndIsAvailableTrueOrderByIdAsc(bookId))
            .thenReturn(Optional.of(copy));
        when(bookCopyRepository.save(copy)).thenReturn(copy);
        when(borrowingRepository.save(any(Borrowing.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Borrowing result = borrowService.borrowBook(new BorrowBookRequest(bookId, borrowerId));

        assertNotNull(result);
        assertEquals(book, result.getBook());
        assertEquals(borrower, result.getBorrower());
        assertEquals(LocalDate.of(2026, 9, 15), result.getBorrowDate());
        assertEquals(LocalDate.of(2026, 9, 29), result.getDueDate());
        assertEquals(BorrowingStatus.ACTIVE, result.getStatus());
        assertEquals(new BigDecimal("0.00"), result.getLateFee());
        assertFalse(copy.isAvailable());
        assertEquals(1, copy.getTimesBorrowed());

        verify(bookRepository).findById(bookId);
        verify(borrowerRepository).findByIdForUpdate(borrowerId);
        verify(bookCopyRepository).save(copy);
        verify(borrowingRepository).save(any(Borrowing.class));
    }

    @Test
    void borrowBook_shouldThrowWhenBookNotFound() {
        BorrowBookRequest request = new BorrowBookRequest(7L, 20L);

        when(bookRepository.findById(request.bookId())).thenReturn(Optional.empty());

        assertThrows(BookNotFoundException.class, () -> borrowService.borrowBook(request));
        verify(bookRepository).findById(request.bookId());
        verify(borrowerRepository, never()).findByIdForUpdate(anyLong());
    }

    @Test
    void borrowBook_shouldThrowWhenBorrowerNotFound() {
        Book book = new Book("Clean Code", "Robert C. Martin", "Software", "978-0132350884", 2008);
        book.setId(7L);

        when(bookRepository.findById(7L)).thenReturn(Optional.of(book));
        when(borrowerRepository.findByIdForUpdate(20L)).thenReturn(Optional.empty());

        assertThrows(BorrowerNotFoundException.class,
            () -> borrowService.borrowBook(new BorrowBookRequest(7L, 20L))
        );
    }

    @Test
    void borrowBook_shouldThrowWhenBorrowerReachedLimit() {
        Book book = new Book("Clean Code", "Robert C. Martin", "Software", "978-0132350884", 2008);
        book.setId(7L);

        Borrower borrower = new Borrower("Eric Ford", "testemail@gmail.com");
        borrower.setId(20L);

        when(bookRepository.findById(7L)).thenReturn(Optional.of(book));
        when(borrowerRepository.findByIdForUpdate(20L)).thenReturn(Optional.of(borrower));
        when(borrowingRepository.existsByBookCopyBookIdAndBorrowerIdAndStatusIn(
            eq(7L), eq(20L), eq(OPEN_STATUSES)
        )).thenReturn(false);
        when(borrowingRepository.countByBorrowerIdAndStatusIn(20L, OPEN_STATUSES)).thenReturn(6L);

        assertThrows(BorrowLimitExceededException.class,
            () -> borrowService.borrowBook(new BorrowBookRequest(7L, 20L))
        );
    }

    @Test
    void returnBook_shouldUpdateConditionAndMakeCopyAvailable() {
        Book book = new Book("Clean Code", "Robert C. Martin", "Software", "978-0132350884", 2008);
        book.setId(7L);

        Borrower borrower = new Borrower("Eric Ford", "testemail@gmail.com");
        borrower.setId(20L);

        BookCopy copy = new BookCopy("BC-200", BookCondition.GOOD, LocalDate.of(2026, 9, 1));
        copy.setBook(book);
        copy.setIsAvailable(false);

        Borrowing borrowing = new Borrowing(book, copy, borrower, LocalDate.of(2026, 9, 1));
        borrowing.setStatus(BorrowingStatus.ACTIVE);

        when(bookRepository.findById(7L)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(20L)).thenReturn(Optional.of(borrower));
        when(borrowingRepository.findByBookCopyBookIdAndBorrowerIdAndStatusIn(
            eq(7L), eq(20L), eq(OPEN_STATUSES)
        )).thenReturn(Optional.of(borrowing));
        when(bookCopyRepository.save(copy)).thenReturn(copy);
        when(borrowingRepository.save(borrowing)).thenReturn(borrowing);

        Borrowing result = borrowService.returnBook(new ReturnBookRequest(7L, 20L, BookCondition.LIKE_NEW));

        assertNotNull(result);
        assertEquals(BorrowingStatus.RETURNED, result.getStatus());
        assertEquals(LocalDate.of(2026, 9, 15), result.getReturnDate());
        assertTrue(copy.isAvailable());
        assertEquals(BookCondition.LIKE_NEW, copy.getCondition());
        assertEquals(new BigDecimal("0.00"), result.getLateFee());

        verify(bookCopyRepository).save(copy);
        verify(borrowingRepository).save(borrowing);
    }

    @Test
    void returnBook_shouldThrowWhenBookNotCurrentlyBorrowedByBorrower() {
        Book book = new Book("Clean Code", "Robert C. Martin", "Software", "978-0132350884", 2008);
        book.setId(7L);

        Borrower borrower = new Borrower("Eric Ford", "testemail@gmail.com");
        borrower.setId(20L);

        when(bookRepository.findById(7L)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(20L)).thenReturn(Optional.of(borrower));
        when(borrowingRepository.findByBookCopyBookIdAndBorrowerIdAndStatusIn(
            eq(7L), eq(20L), eq(OPEN_STATUSES)
        )).thenReturn(Optional.empty());

        assertThrows(exception.BookNotBorrowedException.class,
            () -> borrowService.returnBook(new ReturnBookRequest(7L, 20L, BookCondition.GOOD))
        );
    }

    @Test
    void returnBook_shouldSetLateFeeWhenReturnedAfterDueDate() {
        Book book = new Book("Clean Code", "Robert C. Martin", "Software", "978-0132350884", 2008);
        book.setId(7L);

        Borrower borrower = new Borrower("Eric Ford", "testemail@gmail.com");
        borrower.setId(20L);

        BookCopy copy = new BookCopy("BC-300", BookCondition.NEW, LocalDate.of(2026, 9, 1));
        copy.setBook(book);
        copy.setIsAvailable(false);

        Borrowing borrowing = new Borrowing(book, copy, borrower, LocalDate.of(2026, 8, 25));
        borrowing.setStatus(BorrowingStatus.ACTIVE);
        borrowing.setDueDate(LocalDate.of(2026, 9, 8));

        when(bookRepository.findById(7L)).thenReturn(Optional.of(book));
        when(borrowerRepository.findById(20L)).thenReturn(Optional.of(borrower));
        when(borrowingRepository.findByBookCopyBookIdAndBorrowerIdAndStatusIn(
            eq(7L), eq(20L), eq(OPEN_STATUSES)
        )).thenReturn(Optional.of(borrowing));
        when(bookCopyRepository.save(copy)).thenReturn(copy);
        when(borrowingRepository.save(borrowing)).thenReturn(borrowing);

        Borrowing result = borrowService.returnBook(new ReturnBookRequest(7L, 20L, BookCondition.GOOD));

        assertEquals(new BigDecimal("10.50"), result.getLateFee());
    }
}

