package com.prahkel.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.prahkel.library.model.Borrowing;
import com.prahkel.library.model.BorrowingStatus;

import java.time.LocalDate;

import java.util.Optional;
import java.util.List;
import java.util.Collection;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    // BY borrower and status count
    long countByBorrowerIdAndStatusIn(Long borrowerId, Collection<BorrowingStatus> statuses);

    // Existanse check
    boolean existsByBookCopyIdAndBorrowerIdAndStatusIn(
        Long bookCopyId, 
        Long borrowerId,
        Collection<BorrowingStatus> statuses
    );

    boolean existsByBookCopyBookIdAndBorrowerIdAndStatusIn(
        Long bookId,
        Long borrowerId,
        Collection<BorrowingStatus> statuses
    );

    Optional<Borrowing> findByBookCopyBookIdAndBorrowerIdAndStatusIn(
        Long bookId,
        Long borrowerId,
        Collection<BorrowingStatus> statuses
    );

    // By borrower and book and status
    Optional<Borrowing> findByBookCopyIdAndBorrowerIdAndStatusIn(
        Long bookCopyId, 
        Long borrowerId, 
        Collection<BorrowingStatus> statuses
    );

    // By borrower
    List<Borrowing> findByBorrowerId(Long borrowerId);

    // By borrower and status
    List<Borrowing> findByBorrowerIdAndStatusIn(
        Long borrowerId,
        Collection<BorrowingStatus> statuses
    );

    // By book and status
    Optional<Borrowing> findByBookCopyIdAndStatusIn(
        Long bookCopyId,
         Collection<BorrowingStatus> statuses
    );

    Optional<Borrowing> findByBookCopyBookIdAndStatusIn(
        Long bookId,
        Collection<BorrowingStatus> statuses
    );

    // By status
    List<Borrowing> findByStatus(BorrowingStatus status);

    // By statuses
    List<Borrowing> findByStatusIn(Collection<BorrowingStatus> statuses);


    long countByStatus (BorrowingStatus status);

    // By book and status
    boolean existsByBookCopyIdAndStatusIn(
        Long bookCopyId,
        Collection<BorrowingStatus> statuses
    );

    boolean existsByBookCopyIdAndStatus(
        Long bookCopyId,
        BorrowingStatus status
    );

    boolean existsByBookCopyBookIdAndStatusIn(Long bookId, Collection<BorrowingStatus> statuses);

    boolean existsByBookCopyBookId(Long bookId);

    boolean existsByBookCopyId(Long bookCopyId);
    // Reports & dates
    List<Borrowing> findByBorrowDateBetween(LocalDate start, LocalDate end);
    List<Borrowing> findByDueDateBetween(LocalDate start, LocalDate end);

    // Bonus useful methods
    List<Borrowing> findByBorrowerIdAndDueDateBeforeAndStatusIn(
        Long borrowerId, 
        LocalDate date, 
        Collection<BorrowingStatus> statuses
    );


}
