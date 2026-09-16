package repository;

import model.Borrower;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;

public interface BorrowerRepository extends JpaRepository<Borrower, Long> {
    Optional<Borrower> findByEmail(String email);

    List<Borrower> findByName(String name);

    // Case-insensitive search (recommended)

    List<Borrower> findByNameContainingIgnoreCase(String name);

    // Check if a borrower with the same email already exists in the database
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByEmail(String email);


    // Useful for reports
    List<Borrower> findAllByOrderByNameAsc();

    Optional<Borrower> getBorrowerByEmail(String email);
}
