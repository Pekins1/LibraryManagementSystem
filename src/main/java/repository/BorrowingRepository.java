package repository;

import model.Borrowing;
import model.BorrowingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

import java.util.Optional;
import java.util.List;
import java.util.Collection;

public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    // BY borrower and status count
    long countByBorrowerIdAndStatusIn(Long borrowerId, Collection<BorrowingStatus> statuses);

    // Existanse check
    boolean existsByBookIdAndBorrowerIdAndStatusIn(
        Long bookId, 
        Long borrowerId,
        Collection<BorrowingStatus> statuses
    );

    // By borrower and book and status
    Optional<Borrowing> findByBookIdAndBorrowerIdAndStatusIn(
        Long bookId, 
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
    Optional<Borrowing> findByBookIdAndStatusIn(
        Long bookId,
         Collection<BorrowingStatus> statuses
    );

    // By status
    List<Borrowing> findByStatus(BorrowingStatus status);

    // By statuses
    List<Borrowing> findByStatusIn(Collection<BorrowingStatus> statuses);


    long countByStatus (BorrowingStatus status);

    // By book and status
    boolean existsByBookIdAndStatusIn(
        Long bookId,
        Collection<BorrowingStatus> statuses
    );

    boolean existsByBookIdAndStatus(
        Long bookId,
        BorrowingStatus status
    );
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
