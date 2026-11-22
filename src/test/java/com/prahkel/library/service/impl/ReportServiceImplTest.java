package com.prahkel.library.service.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.prahkel.library.model.BookCondition;
import com.prahkel.library.model.BookCopy;
import com.prahkel.library.model.BookLifecycleStatus;
import com.prahkel.library.model.Borrowing;
import com.prahkel.library.model.BorrowingStatus;
import com.prahkel.library.model.CopyVisibility;
import com.prahkel.library.model.LibraryMember;
import com.prahkel.library.model.MemberType;
import com.prahkel.library.repository.BookCopyRepository;
import com.prahkel.library.repository.BookRepository;
import com.prahkel.library.repository.BorrowingRepository;
import com.prahkel.library.service.BookService;
import com.prahkel.library.service.BorrowService;

@ExtendWith(MockitoExtension.class)
class ReportServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private BookCopyRepository bookCopyRepository;

    @Mock
    private BorrowingRepository borrowingRepository;

    @Mock
    private BookService bookService;

    @Mock
    private BorrowService borrowService;

    private ReportServiceImpl reportService;
    private Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(
            LocalDate.of(2026, 11, 22)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant(),
            ZoneId.systemDefault()
        );

        reportService = new ReportServiceImpl(
            bookRepository,
            bookCopyRepository,
            borrowingRepository,
            bookService,
            borrowService,
            fixedClock);
    }

    @Test
    void getCopiesForBook_shouldDelegateToBookService() {
        LibraryMember owner = new LibraryMember("exampleOwner", "example@gmail.com", MemberType.INDIVIDUAL);
        
        BookCopy copy = new BookCopy("BC_001", BookCondition.NEW,
            LocalDate.of(2026, 1, 1), owner, CopyVisibility.PUBLIC);
        when(bookService.getCopiesForBook(20L)).thenReturn(List.of(copy));

        List<BookCopy> result = reportService.getCopiesForBook(20L);

        assertThat(result).containsExactly(copy);
        verify(bookService).getCopiesForBook(20L);
    }

    @Test
    void getAllActiveBorrowings_shouldDelegateToBorrowService() {
        Borrowing borrowing = mock(Borrowing.class);
        when(borrowService.getAllActiveBorrowings()).thenReturn(List.of(borrowing));

        assertThat(reportService.getAllActiveBorrowings()).containsExactly(borrowing);
        verify(borrowService).getAllActiveBorrowings();
    }

    @Test
    void getBorrowingHistoryForBorrower_shouldDelegateToBorrowService() {
        Borrowing borrowing = mock(Borrowing.class);
        when(borrowService.getBorrowingHistoryForBorrower(7L)).thenReturn(List.of(borrowing));

        assertThat(reportService.getBorrowingHistoryForBorrower(7L)).containsExactly(borrowing);
        verify(borrowService).getBorrowingHistoryForBorrower(7L);
    }

    @Test
    void getCurrentBorrowingsForBorrower_shouldDelegateToBorrowService() {
        Borrowing borrowing = mock(Borrowing.class);
        when(borrowService.getActiveBorrowingsForBorrower(7L)).thenReturn(List.of(borrowing));

        assertThat(reportService.getCurrentBorrowingsForBorrower(7L)).containsExactly(borrowing);
        verify(borrowService).getActiveBorrowingsForBorrower(7L);
    }

    @Test
    void getAvailableCopiesForBook_shouldDelegateToBookService() {
        BookCopy copy = newBookCopy();
        when(bookService.getAvailableCopiesForBook(20L)).thenReturn(List.of(copy));

        assertThat(reportService.getAvailableCopiesForBook(20L)).containsExactly(copy);
        verify(bookService).getAvailableCopiesForBook(20L);
    }

    @Test
    void getTotalBookCopyCount_shouldReturnRepositoryCount() {
        when(bookCopyRepository.count()).thenReturn(12L);

        assertThat(reportService.getTotalBookCopyCount()).isEqualTo(12L);
        verify(bookCopyRepository).count();
    }

    @Test
    void getTotalBookCount_shouldReturnRepositoryCount() {
        when(bookRepository.count()).thenReturn(8L);

        assertThat(reportService.getTotalBookCount()).isEqualTo(8L);
        verify(bookRepository).count();
    }

    @Test
    void getTotalArchivedBookCount_shouldReturnRepositoryCount() {
        when(bookRepository.countByLifecycle(BookLifecycleStatus.ARCHIVED)).thenReturn(3L);

        assertThat(reportService.getTotalArchivedBookCount()).isEqualTo(3L);
        verify(bookRepository).countByLifecycle(BookLifecycleStatus.ARCHIVED);
    }

    @Test
    void getAvailableBookCopyCount_shouldReturnRepositoryCount() {
        when(bookCopyRepository.countByIsAvailableTrue()).thenReturn(9L);

        assertThat(reportService.getAvailableBookCopyCount()).isEqualTo(9L);
        verify(bookCopyRepository).countByIsAvailableTrue();
    }

    @Test
    void getUnavailableBookCopyCount_shouldReturnRepositoryCount() {
        when(bookCopyRepository.countByIsAvailableFalse()).thenReturn(3L);

        assertThat(reportService.getUnavailableBookCopyCount()).isEqualTo(3L);
        verify(bookCopyRepository).countByIsAvailableFalse();
    }

    @Test
    void getActiveBorrowingCount_shouldReturnRepositoryCount() {
        when(borrowingRepository.countByStatus(BorrowingStatus.ACTIVE)).thenReturn(4L);

        assertThat(reportService.getActiveBorrowingCount()).isEqualTo(4L);
        verify(borrowingRepository).countByStatus(BorrowingStatus.ACTIVE);
    }

    @Test
    void getOverdueBorrowingCount_shouldCountOverdueBorrowings() {
        Borrowing overdue = borrowingDueOn(LocalDate.of(2026, 11, 21));
        Borrowing inTime = borrowingDueOn(LocalDate.of(2026, 11, 22));
        when(borrowingRepository.findByStatusIn(List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE)))
            .thenReturn(List.of(overdue, inTime));

        assertThat(reportService.getOverdueBorrowingCount()).isEqualTo(1L);
        verify(borrowingRepository).findByStatusIn(
            List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE));
    }

    @Test
    void getCopyCountForBook_shouldReturnRepositoryCount() {
        when(bookCopyRepository.countByBookId(20L)).thenReturn(5L);

        assertThat(reportService.getCopyCountForBook(20L)).isEqualTo(5L);
        verify(bookCopyRepository).countByBookId(20L);
    }

    @Test
    void getAvailableCopyCountForBook_shouldReturnRepositoryCount() {
        when(bookCopyRepository.countByBookIdAndIsAvailableTrue(20L)).thenReturn(2L);

        assertThat(reportService.getAvailableCopyCountForBook(20L)).isEqualTo(2L);
        verify(bookCopyRepository).countByBookIdAndIsAvailableTrue(20L);
    }

    @Test
    void getAllOverdueBorrowings_shouldOnlyReturnBorrowingsPastTheirDueDate() {
        Borrowing overdue = borrowingDueOn(LocalDate.of(2026, 11, 21));
        Borrowing inTime = borrowingDueOn(LocalDate.of(2026, 11, 23));
        when(borrowingRepository.findByStatusIn(List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE)))
            .thenReturn(List.of(overdue, inTime));

        assertThat(reportService.getAllOverdueBorrowings()).containsExactly(overdue);
        verify(borrowingRepository).findByStatusIn(
            List.of(BorrowingStatus.ACTIVE, BorrowingStatus.OVERDUE));
    }

    @Test
    void getTotalLateFeesAssessed_shouldReturnRepositorySum() {
        BigDecimal total = new BigDecimal("12.50");
        when(borrowingRepository.sumLateFees()).thenReturn(total);

        assertThat(reportService.getTotalLateFeesAssessed()).isEqualByComparingTo("12.50");
        verify(borrowingRepository).sumLateFees();
    }

    private BookCopy newBookCopy() {
        LibraryMember owner = new LibraryMember("exampleOwner", "example@gmail.com", MemberType.INDIVIDUAL);
        return new BookCopy("BC_001", BookCondition.NEW,
            LocalDate.of(2026, 1, 1), owner, CopyVisibility.PUBLIC);
    }

    private Borrowing borrowingDueOn(LocalDate dueDate) {
        Borrowing borrowing = new Borrowing(null, null, null, dueDate.minusDays(14));
        borrowing.setDueDate(dueDate);
        return borrowing;
    }
}
