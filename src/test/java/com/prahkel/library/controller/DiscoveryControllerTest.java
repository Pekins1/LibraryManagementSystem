package com.prahkel.library.controller;


import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.prahkel.library.model.Book;
import com.prahkel.library.model.BookCondition;
import com.prahkel.library.model.BookCopy;
import com.prahkel.library.model.BookLifecycleStatus;
import com.prahkel.library.model.CopyVisibility;
import com.prahkel.library.model.LibraryMember;
import com.prahkel.library.model.MemberType;
import com.prahkel.library.service.impl.BookServiceImpl;

@WebMvcTest(DiscoveryController.class)
public class DiscoveryControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private BookServiceImpl bookService;

    @Test 
    void getDiscoverableCopies_shouldReturn200WithPublicAvailableCopies() throws Exception{
        LibraryMember owner = new LibraryMember("owner", "example2@gmail.com ", MemberType.ORGANIZATION);
        owner.setId(1L);

        Book savedBook = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441173481", 
        1965);
        savedBook.setId(20L);
        savedBook.setLifecycleStatus(BookLifecycleStatus.ACTIVE);


        BookCopy savedCopy = new BookCopy(
            "BC_001", BookCondition.LIKE_NEW, LocalDate.of(2025, 9, 23),
             owner, CopyVisibility.PUBLIC);
        savedCopy.setId(30L);
        savedCopy.setIsAvailable(true);
        savedCopy.setBook(savedBook);

        Book savedBook2 = new Book("Clean Code", "Robert C. Martin", "Software", 
        "978-0132350884", 2008);
        savedBook2.setId(10L);
        savedBook2.setLifecycleStatus(BookLifecycleStatus.ACTIVE);
        

        BookCopy savedCopy2 = new BookCopy(
            "BC_005", BookCondition.LIKE_NEW, LocalDate.of(2025, 9, 23),
             owner, CopyVisibility.PUBLIC);
        savedCopy2.setId(2L);
        savedCopy2.setIsAvailable(true);
        savedCopy2.setBook(savedBook2);


        when(bookService.getDiscoverableCopies()).thenReturn(List.of(savedCopy, savedCopy2));

        mockMvc.perform(get("/api/v1/discovery"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(20))
                .andExpect(jsonPath("$[0].ownerId").value(1))
                .andExpect(jsonPath("$[0].visibility").value("PUBLIC"))
                .andExpect(jsonPath("$[1].bookId").value(10))
                .andExpect(jsonPath("$[1].ownerId").value(1))
                .andExpect(jsonPath("$[1].visibility").value("PUBLIC"));
    }

    @Test 
    void getDiscoverableCopies_shouldReturnEmptyListWhenNoneAvailable() throws Exception{
        when(bookService.getDiscoverableCopies()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/discovery"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }
}
