package com.prahkel.library.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.prahkel.library.model.Book;

import java.util.Optional;
import java.util.List;


public interface BookRepository extends JpaRepository<Book, Long> {
    Optional<Book> findByIsbn(String isbn);
    Optional<Book> findById(Long id);
 

       @Query("SELECT b FROM Book b WHERE b.lifecycle = com.prahkel.library.model.BookLifecycleStatus.ACTIVE " +
          "AND LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%'))")
       List<Book> findByTitleContainingIgnoreCase(@Param("title") String title); 

       @Query("SELECT b FROM Book b WHERE b.lifecycle = com.prahkel.library.model.BookLifecycleStatus.ACTIVE " +
          "AND LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))")
       List<Book> findByAuthorContainingIgnoreCase(@Param("author") String author);

       @Query("SELECT b FROM Book b WHERE b.lifecycle = com.prahkel.library.model.BookLifecycleStatus.ACTIVE " +
          "AND LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))")
       List<Book> findByGenreContainingIgnoreCase(@Param("genre") String genre);


   @Query("SELECT b FROM Book b WHERE b.lifecycle = com.prahkel.library.model.BookLifecycleStatus.ACTIVE " +
         "AND LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%')) " +
         "AND LOWER(b.genre) LIKE LOWER(CONCAT('%', :genre, '%'))")
   List<Book> findByAuthorContainingIgnoreCaseAndGenreContainingIgnoreCase(
      @Param("author") String author,
      @Param("genre") String genre
   );

   @Query("SELECT b FROM Book b WHERE b.lifecycle = com.prahkel.library.model.BookLifecycleStatus.ACTIVE " +
         "AND LOWER(b.title) LIKE LOWER(CONCAT('%', :title, '%')) " +
         "AND LOWER(b.author) LIKE LOWER(CONCAT('%', :author, '%'))")
   List<Book> findByTitleContainingIgnoreCaseAndAuthorContainingIgnoreCase(
      @Param("title") String title,
      @Param("author") String author
   );
    
    // check if a book with the same ISBN already exists in the database
    boolean existsByIsbn(String isbn);
    
    boolean existsByIsbnAndIdNot(String isbn, Long id);

    @Query("SELECT DISTINCT c.book FROM BookCopy c " +
       "WHERE c.isAvailable = true " +
       "AND c.book.lifecycle = com.prahkel.library.model.BookLifecycleStatus.ACTIVE")
    List<Book> findBooksWithAvailableCopies();

    @Query("SELECT b FROM Book b " +
       "WHERE b.lifecycle = com.prahkel.library.model.BookLifecycleStatus.ACTIVE " +
       "AND EXISTS (SELECT c.id FROM BookCopy c WHERE c.book = b) " +
       "AND NOT EXISTS (SELECT c2.id FROM BookCopy c2 " +
       "WHERE c2.book = b AND c2.isAvailable = true)")
    List<Book> findBooksWithUnavailableCopies();

    @Query("SELECT COUNT(DISTINCT c.book.id) FROM BookCopy c " +
       "WHERE c.isAvailable = true " +
       "AND c.book.lifecycle = com.prahkel.library.model.BookLifecycleStatus.ACTIVE")
    Long countBooksWithAvailableCopies();

    @Query("SELECT COUNT(b.id) FROM Book b " +
       "WHERE b.lifecycle = com.prahkel.library.model.BookLifecycleStatus.ACTIVE " +
       "AND EXISTS (SELECT c.id FROM BookCopy c WHERE c.book = b) " +
       "AND NOT EXISTS (SELECT c2.id FROM BookCopy c2 " +
       "WHERE c2.book = b AND c2.isAvailable = true)")
    Long countBooksWithUnavailableCopies();
}