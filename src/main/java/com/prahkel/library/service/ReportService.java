package com.prahkel.library.service;

import java.math.BigDecimal;
import java.util.List;

import com.prahkel.library.model.Book;
import com.prahkel.library.model.BookCopy;
import com.prahkel.library.model.Borrowing;

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