package com.prahkel.library.dto;

import com.prahkel.library.model.Book;
import com.prahkel.library.model.BookLifecycleStatus;

public record BookResponse(
    Long id,
    String title,
    String author,
    String genre,
    String isbn,
    int publishedYear,
    BookLifecycleStatus lifecycle
) {
    public static BookResponse from(Book book) {
        return new BookResponse(
            book.getId(),
            book.getTitle(),
            book.getAuthor(),
            book.getGenre(),
            book.getIsbn(),
            book.getPublishedYear(),
            book.getLifecycleStatus()
        );
    }
}
