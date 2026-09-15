package service;

import dto.CreateBookRequest;
import dto.UpdateBookRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.util.*;
import model.Book;

public interface BookService {
      Book createBook(@Valid @NotNull CreateBookRequest request);

      List<Book> createBooks(@NotEmpty List<@Valid CreateBookRequest> requests);

      Book getBookById(Long id);

      Book getBookByIsbn(String isbn);

      List<Book> getAllBooks();

      List<Book> getAvailableBooks();

      List<Book> getUnavailableBooks();

      List<Book> findBooksByTitle(String title);

      List<Book> findBooksByAuthor(String author);

      List<Book> findBooksByGenre(String genre);

      List<Book> findBooksByAuthorAndGenre(String author, String genre);

      List<Book> findBooksByTitleAndAuthor(String title, String author);

      long countAvailableBooks();

      long countUnavailableBooks();

      void deleteBook(@NotNull @Positive Long bookId);

      Book updateBook(@Positive Long id, @Valid @NotNull UpdateBookRequest request);


}
