package service;

import model.Borrowing;

import java.util.List;

public interface ReportService {

    long getTotalBookCount();

    long getAvailableBookCount();

    long getUnavailableBookCount();

    long getTotalBorrowerCount();

    long getActiveBorrowingCount();

    long getOverdueBorrowingCount();

    List<Borrowing> getAllActiveBorrowings();

    List<Borrowing> getAllOverdueBorrowings();

    List<Borrowing> getBorrowingHistoryForBorrower(Long borrowerId);
    
}
