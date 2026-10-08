package repository;

import model.BookCopy;
import model.BookCondition;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.List;
import java.util.Collection;


public interface BookCopyRepository extends JpaRepository<BookCopy, Long> { 

    List<BookCopy> findByBookId(Long bookId);
    
    List<BookCopy> findByBookIdAndIsAvailableTrue(Long bookId);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<BookCopy> findFirstByBookIdAndIsAvailableTrueOrderByIdAsc(
        Long bookId
    );

    List<BookCopy> findByBookIdAndConditionIn(
        Long bookId,
        Collection<BookCondition> conditions
    );

    boolean existsByBarcode(String barcode);

    boolean existsByBookId(Long bookId);

    long countByBookId(Long bookId);

    long countByBookIdAndIsAvailableTrue(Long bookId);
    
    List<BookCopy> findByIsAvailableTrue();

    List<BookCopy> findByIsAvailableFalse();

    long countByIsAvailableTrue();

    long countByIsAvailableFalse();




}
