package com.prahkel.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.prahkel.library.model.Borrower;

import jakarta.persistence.LockModeType;

import java.util.Optional;
import java.util.List;

public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
    Optional<Borrower> findByEmail(String email);

    List<Borrower> findByName(String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT b FROM Borrower b WHERE b.id = :borrowerId")
    Optional<Borrower> findByIdForUpdate(@Param("borrowerId") Long borrowerId);

    // Case-insensitive search (recommended)

    List<Borrower> findByNameContainingIgnoreCase(String name);

    // Check if a borrower with the same email already exists in the database
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmail(String email);


    // Useful for reports
    List<Borrower> findAllByOrderByNameAsc();

    Optional<Borrower> getBorrowerByEmail(String email);
}
