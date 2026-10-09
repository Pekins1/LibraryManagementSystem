package com.prahkel.library.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotBlank;
import java.util.*;

import com.prahkel.library.dto.AddBookCopyRequest;
import com.prahkel.library.dto.CreateBookRequest;
import com.prahkel.library.dto.UpdateBookRequest;
import com.prahkel.library.model.Book;
import com.prahkel.library.model.BookCopy;

public interface BookService {
      Book createBook(@Valid @NotNull CreateBookRequest request);

      List<Book> createBooks(@NotEmpty List<@Valid CreateBookRequest> requests);

      BookCopy addCopyToBook(@NotNull @Positive Long bookId, @Valid @NotNull AddBookCopyRequest request);

      List<BookCopy> getCopiesForBook(@NotNull @Positive Long bookId);

      List<BookCopy> getAvailableCopiesForBook(@NotNull @Positive Long bookId);

      void deleteCopy(@NotNull @Positive Long copyId);

      Book getBookById(@NotNull @Positive Long id);

      Book getBookByIsbn(@NotNull @NotBlank String isbn);

      List<Book> getAllBooks();

      List<Book> getAvailableBooks();

      List<Book> getUnavailableBooks();

      List<Book> findBooksByTitle(@NotBlank String title);

      List<Book> findBooksByAuthor(@NotBlank String author);

      List<Book> findBooksByGenre(@NotBlank String genre);

      List<Book> findBooksByAuthorAndGenre(@NotBlank String author, @NotBlank String genre);

      List<Book> findBooksByTitleAndAuthor(@NotBlank String title, @NotBlank String author);

      long countAvailableBooks();

      long countUnavailableBooks();

      void archiveBook(@NotNull @Positive Long bookId);

      void deleteBook(@NotNull @Positive Long bookId);

      Book updateBook(@Positive Long id, @Valid @NotNull UpdateBookRequest request);


}
