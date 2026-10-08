package service;

import model.Book;
import model.BookCopy;
import model.Borrowing;

import java.math.BigDecimal;
import java.util.List;

public interface ReportService {

    long getTotalBookCount();

    long getArchivedBookCount();

    long getTotalBookCopyCount();

    long getAvailableBookCopyCount();

    long getUnavailableBookCopyCount();

    long getActiveBorrowingCount();

    long getOverdueBorrowingCount();

    List<Borrowing> getAllActiveBorrowings();

    List<Borrowing> getAllOverdueBorrowings();

    List<Borrowing> getBorrowingHistoryForBorrower(Long borrowerId);

    List<Borrowing> getCurrentBorrowingsForBorrower(Long borrowerId);

    List<BookCopy> getCopiesForBook(Long bookId);

    List<BookCopy> getAvailableCopiesForBook(Long bookId);

    long getCopyCountForBook(Long bookId);

    long getAvailableCopyCountForBook(Long bookId);

    BigDecimal getTotalLateFeesAssessed();
}