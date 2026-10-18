package com.prahkel.library.repository;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.Optional;
import java.util.List;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;



import com.prahkel.library.model.Book;
import com.prahkel.library.model.BookCondition;
import com.prahkel.library.model.BookCopy;
import com.prahkel.library.model.CopyVisibility;
import com.prahkel.library.model.LibraryMember;
import com.prahkel.library.model.MemberType;

@DataJpaTest

class BookCopyRepositoryTest {

    @Autowired 
    private TestEntityManager entityManager;

    @Autowired 
    private BookCopyRepository bookCopyRepository;

    @Test 
    void findFirstAvailablePublicCopy_shouldSkipPrivateCopies() {
        LibraryMember owner = entityManager.persistAndFlush(
            new LibraryMember("Test Owner", "owner@test.com", MemberType.INDIVIDUAL)
        );

        Book book = entityManager.persistAndFlush(
                new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441172719", 1965)
        );

        BookCopy privateCopy = new BookCopy("BC-PRIVATE", BookCondition.GOOD,
                LocalDate.now(), owner, CopyVisibility.PRIVATE);
        privateCopy.setBook(book);
        entityManager.persistAndFlush(privateCopy);

        BookCopy publicCopy = new BookCopy("BC-PUBLIC", BookCondition.GOOD,
                LocalDate.now(), owner, CopyVisibility.PUBLIC);
        publicCopy.setBook(book);
        entityManager.persistAndFlush(publicCopy);

        Optional<BookCopy> result = bookCopyRepository
            .findFirstByBookIdAndIsAvailableTrueAndVisibilityOrderByIdAsc(
                book.getId(), CopyVisibility.PUBLIC
        );

        assertThat(result).isPresent();
        assertThat(result.get().getBarcode()).isEqualTo("BC-PUBLIC");
    }

    @Test 
    void findAvailablePublicCopy_shouldSkipPrivateAndPublicUnavailableCopies() {
        LibraryMember owner = entityManager.persistAndFlush(
            new LibraryMember("Test Owner", "owner@test.com", MemberType.INDIVIDUAL)
        );

       Book bookTwo = entityManager.persistAndFlush(
                new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441172719", 1965)
        );

        BookCopy privateCopy = new BookCopy("BC-PRIVATE", BookCondition.GOOD,
                LocalDate.now(), owner, CopyVisibility.PRIVATE);
        privateCopy.setBook(bookTwo);
        entityManager.persistAndFlush(privateCopy);

        BookCopy publicCopy = new BookCopy("BC-PUBLIC", BookCondition.GOOD,
                LocalDate.now(), owner, CopyVisibility.PUBLIC);
        publicCopy.setBook(bookTwo);
        publicCopy.setIsAvailable(false);
        entityManager.persistAndFlush(publicCopy);

        BookCopy publicSecondCopy = new BookCopy("BC-PUBLIC-2", BookCondition.GOOD,
                LocalDate.now(), owner, CopyVisibility.PUBLIC);
        publicSecondCopy.setBook(bookTwo);
        publicSecondCopy.setIsAvailable(true);
        entityManager.persistAndFlush(publicSecondCopy);

        List<BookCopy> result = bookCopyRepository
            .findByBookIdAndIsAvailableTrueAndVisibility(
                bookTwo.getId(), CopyVisibility.PUBLIC
        );

        assertThat(result).containsExactly(publicSecondCopy);
        assertThat(result.get(0).getBarcode()).isEqualTo("BC-PUBLIC-2");
        assertThat(result.get(0).isAvailable()).isTrue();
    }
    
}
