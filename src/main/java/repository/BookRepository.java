package repository;

import model.Book;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.List;


public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
    Optional<Book> findById(Long id);
    Optional<Book> findByBookIdAndReturnedFalse(Long id);

    List<Book> findByTitleContainingIgnoreCase(String title); 

    List<Book> findByAuthorContainingIgnoreCase(String author);

    List<Book> findByGenreContainingIgnoreCase(String genre);

    List<Book> findByIsAvailableTrue();

    List<Book> findByIsAvailableFalse();
    
    List<Book> findByAuthorContainingIgnoreCaseAndGenreContainingIgnoreCase(String author, String genre);

    List<Book> findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(String title, String author);
    
    // check if a book with the same ISBN already exists in the database
    boolean existsByIsbn(String isbn);
    
    boolean existsByIsbnAndIdNot(String isbn, Long id);

    long countByIsAvailableTrue();

    long countByIsAvailableFalse();

}