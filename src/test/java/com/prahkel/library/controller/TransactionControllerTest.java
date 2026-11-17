package com.prahkel.library.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.postgresql.translation.messages_bg;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.prahkel.library.dto.BorrowBookRequest;
import com.prahkel.library.dto.ReturnBookRequest;
import com.prahkel.library.exception.BookCopyNotFoundException;
import com.prahkel.library.exception.BookNotAvailableException;
import com.prahkel.library.exception.BookNotBorrowedException;
import com.prahkel.library.exception.BorrowingNotFoundException;
import com.prahkel.library.model.Book;
import com.prahkel.library.model.BookCondition;
import com.prahkel.library.model.BookCopy;
import com.prahkel.library.model.Borrower;
import com.prahkel.library.model.Borrowing;
import com.prahkel.library.model.BorrowingStatus;
import com.prahkel.library.model.CopyVisibility;
import com.prahkel.library.model.LibraryMember;
import com.prahkel.library.model.MemberType;
import com.prahkel.library.service.impl.BorrowServiceImpl;


import tools.jackson.databind.ObjectMapper;

@WebMvcTest(TransactionController.class)
public class TransactionControllerTest {
     @Autowired
    private MockMvc mockMvc;

    @MockitoBean 
    private BorrowServiceImpl borrowService;

    @Autowired 
    private ObjectMapper objectMapper;

    @Test 
    void borrowBook_shouldReturn201WhenCopyIsAvailable() throws Exception{
        Borrower borrower = new Borrower("test", "example@gmail.com");
        borrower.setId(9L);

        Book savedBook = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441173481", 
        1965);
        savedBook.setId(20L);

        LibraryMember owner = new LibraryMember("owner", "example2@gmail.com ", MemberType.ORGANIZATION);

        BookCopy savedCopy = new BookCopy(
            "BC_001", BookCondition.LIKE_NEW, LocalDate.of(2025, 9, 23),
             owner, CopyVisibility.PUBLIC);
        savedCopy.setId(30L);
        savedCopy.setBook(savedBook);

        BorrowBookRequest request = new BorrowBookRequest(20L, 9L);

        Borrowing borrowing = new Borrowing( savedBook, savedCopy, borrower, 
            LocalDate.of(2026, 2, 23));

        when(borrowService.borrowBook(any(BorrowBookRequest.class))).thenReturn(borrowing);

        mockMvc.perform(post("/api/v1/transactions/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.bookId").value(20))
                .andExpect(jsonPath("$.bookCopyId").value(30))
                .andExpect(jsonPath("$.borrowerId").value(9));

    }

    @Test 
    void borrowBook_shouldReturn409WhenCopyIsNotAvailable() throws Exception{
        BorrowBookRequest request = new BorrowBookRequest(20L, 9L);

        when(borrowService.borrowBook(any(BorrowBookRequest.class))).thenThrow(
            new BookNotAvailableException("No available copy for book with id: 20"));

        mockMvc.perform(post("/api/v1/transactions/borrow")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test 
    void returnBook_shouldReturn200WhenBorrowingIsActive() throws Exception{

        Borrower borrower = new Borrower("test", "example@gmail.com");
        borrower.setId(9L);

        Book savedBook = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441173481", 
        1965);
        savedBook.setId(20L);

        LibraryMember owner = new LibraryMember("owner", "example2@gmail.com ", MemberType.ORGANIZATION);

        BookCopy savedCopy = new BookCopy(
            "BC_001", BookCondition.LIKE_NEW, LocalDate.of(2025, 9, 23),
             owner, CopyVisibility.PUBLIC);
        savedCopy.setId(30L);
        savedCopy.setBook(savedBook);

        ReturnBookRequest request = new ReturnBookRequest(20L, 9L, BookCondition.GOOD);

        Borrowing borrowing = new Borrowing( savedBook, savedCopy, borrower, 
            LocalDate.of(2026, 2, 23));
        borrowing.setStatus(BorrowingStatus.RETURNED);

        when(borrowService.returnBook(any(ReturnBookRequest.class))).thenReturn(borrowing);

        mockMvc.perform(post("/api/v1/transactions/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bookId").value(20))
                .andExpect(jsonPath("$.bookCopyId").value(30))
                .andExpect(jsonPath("$.borrowerId").value(9));
    }

    @Test 
    void returnBook_shouldReturn400WhenBookNotBorrowed() throws Exception{
        ReturnBookRequest request = new ReturnBookRequest(20L, 5L, BookCondition.POOR);

        when(borrowService.returnBook(any(ReturnBookRequest.class))).thenThrow(
            new BookNotBorrowedException("Book with Id: 20 is not borrowed.")
        );

        mockMvc.perform(post("/api/v1/transactions/return")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));                   
    }

    @Test 
    void getBorrowingById_shouldReturn200WhenBorrowingBorrowingExists() throws Exception{
        Borrower borrower = new Borrower("test", "example@gmail.com");
        borrower.setId(9L);

        Book savedBook = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441173481", 
        1965);
        savedBook.setId(20L);

        LibraryMember owner = new LibraryMember("owner", "example2@gmail.com ", MemberType.ORGANIZATION);

        BookCopy savedCopy = new BookCopy(
            "BC_001", BookCondition.LIKE_NEW, LocalDate.of(2025, 9, 23),
             owner, CopyVisibility.PUBLIC);
        savedCopy.setId(30L);
        savedCopy.setBook(savedBook);

        Borrowing borrowing = new Borrowing( savedBook, savedCopy, borrower, 
            LocalDate.of(2026, 2, 23));
        borrowing.setId(38L);
        when(borrowService.getBorrowingById(38L)).thenReturn(borrowing);

        mockMvc.perform(get("/api/v1/transactions/38"))
                .andExpect((status().isOk()))
                .andExpect(jsonPath("$.bookId").value(20))
                .andExpect(jsonPath("$.bookCopyId").value(30))
                .andExpect(jsonPath("$.borrowerId").value(9));
    }

    @Test 
    void getBorrowingById_shouldReturn404WhenBorrowingBorrowingDoesNotExist() throws Exception{
        when(borrowService.getBorrowingById(40L)).thenThrow(
            new BorrowingNotFoundException("Borrowing with Id: 40 not found.")
        );

        mockMvc.perform(get("/api/v1/transactions/40"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test 
    void getActiveBorrowingsForBorrower_shouldReturn200WithActiveList() throws Exception{
        Borrower borrower = new Borrower("test", "example@gmail.com");
        borrower.setId(9L);

        Book savedBook = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441173481", 
        1965);
        savedBook.setId(20L);

        LibraryMember owner = new LibraryMember("owner", "example2@gmail.com ", MemberType.ORGANIZATION);

        BookCopy savedCopy = new BookCopy(
            "BC_001", BookCondition.LIKE_NEW, LocalDate.of(2025, 9, 23),
             owner, CopyVisibility.PUBLIC);
        savedCopy.setId(30L);
        savedCopy.setBook(savedBook);

        Borrowing secondBorrowing = new Borrowing( savedBook, savedCopy, borrower, 
            LocalDate.of(2026, 4, 9));
        secondBorrowing.setId(48L);
        secondBorrowing.setStatus(BorrowingStatus.ACTIVE);

        when(borrowService.getActiveBorrowingsForBorrower(9L)).thenReturn(List.of(secondBorrowing));

        mockMvc.perform(get("/api/v1/transactions/borrower/9/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(20))
                .andExpect(jsonPath("$[0].bookCopyId").value(30))
                .andExpect(jsonPath("$[0].borrowerId").value(9))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"));
    }

    @Test 
    void getBorrowingHistoryForBorrower_shouldReturn200WithHistoryList() throws Exception{
        Borrower borrower = new Borrower("test", "example@gmail.com");
        borrower.setId(9L);

        Book savedBook = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441173481", 
        1965);
        savedBook.setId(20L);

        LibraryMember owner = new LibraryMember("owner", "example2@gmail.com ", MemberType.ORGANIZATION);

        BookCopy savedCopy = new BookCopy(
            "BC_001", BookCondition.LIKE_NEW, LocalDate.of(2025, 9, 23),
             owner, CopyVisibility.PUBLIC);
        savedCopy.setId(30L);
        savedCopy.setBook(savedBook);

        Borrowing borrowing = new Borrowing( savedBook, savedCopy, borrower, 
            LocalDate.of(2026, 4, 9));
        borrowing.setId(38L);
        borrowing.setStatus(BorrowingStatus.RETURNED);

        Borrowing secondBorrowing = new Borrowing( savedBook, savedCopy, borrower, 
            LocalDate.of(2026, 4, 9));
        secondBorrowing.setId(48L);
        secondBorrowing.setStatus(BorrowingStatus.ACTIVE);

        when(borrowService.getBorrowingHistoryForBorrower(9L)).thenReturn(List.of(borrowing, secondBorrowing));

        mockMvc.perform(get("/api/v1/transactions/borrower/9/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(20))
                .andExpect(jsonPath("$[0].bookCopyId").value(30))
                .andExpect(jsonPath("$[0].borrowerId").value(9))
                .andExpect(jsonPath("$[0].status").value("RETURNED"))
                .andExpect(jsonPath("$[1].status").value("ACTIVE"));
    }

    @Test 
    void getAllActiveBorrowings_shouldReturn200WithActiveList() throws Exception{
        Borrower borrower = new Borrower("test", "example@gmail.com");
        borrower.setId(9L);

        Book savedBook = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441173481", 
        1965);
        savedBook.setId(20L);

        LibraryMember owner = new LibraryMember("owner", "example2@gmail.com ", MemberType.ORGANIZATION);

        BookCopy savedCopy = new BookCopy(
            "BC_001", BookCondition.LIKE_NEW, LocalDate.of(2025, 9, 23),
             owner, CopyVisibility.PUBLIC);
        savedCopy.setId(30L);
        savedCopy.setBook(savedBook);

        Book savedBook2 = new Book("Clean Code", "Robert C. Martin", "Software", 
        "978-0132350884", 2008);
        savedBook2.setId(10L);

        BookCopy savedCopy2 = new BookCopy(
            "BC_005", BookCondition.LIKE_NEW, LocalDate.of(2025, 9, 23),
             owner, CopyVisibility.PUBLIC);
        savedCopy2.setId(2L);
        savedCopy2.setBook(savedBook2);

        Borrowing borrowing = new Borrowing( savedBook2, savedCopy2, borrower, 
            LocalDate.of(2026, 5, 19));
        borrowing.setId(38L);
        borrowing.setStatus(BorrowingStatus.ACTIVE);

        Borrowing secondBorrowing = new Borrowing( savedBook, savedCopy, borrower, 
            LocalDate.of(2026, 4, 9));
        secondBorrowing.setId(48L);
        secondBorrowing.setStatus(BorrowingStatus.ACTIVE);

        when(borrowService.getAllActiveBorrowings()).thenReturn(List.of(borrowing, secondBorrowing));

        mockMvc.perform(get("/api/v1/transactions/active"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(10))
                .andExpect(jsonPath("$[0].bookCopyId").value(2))
                .andExpect(jsonPath("$[0].borrowerId").value(9))
                .andExpect(jsonPath("$[0].status").value("ACTIVE"))
                .andExpect(jsonPath("$[1].bookId").value(20))
                .andExpect(jsonPath("$[1].bookCopyId").value(30))
                .andExpect(jsonPath("$[1].borrowerId").value(9))
                .andExpect(jsonPath("$[1].status").value("ACTIVE"));
    }

    @Test 
    void getOverdueBorrowings_shouldReturn200WithOverdueList() throws Exception{
        Borrower borrower = new Borrower("test", "example@gmail.com");
        borrower.setId(9L);

        Book savedBook = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441173481", 
        1965);
        savedBook.setId(20L);

        LibraryMember owner = new LibraryMember("owner", "example2@gmail.com ", MemberType.ORGANIZATION);

        BookCopy savedCopy = new BookCopy(
            "BC_001", BookCondition.LIKE_NEW, LocalDate.of(2025, 9, 23),
             owner, CopyVisibility.PUBLIC);
        savedCopy.setId(30L);
        savedCopy.setBook(savedBook);

        Book savedBook2 = new Book("Clean Code", "Robert C. Martin", "Software", 
        "978-0132350884", 2008);
        savedBook2.setId(10L);

        BookCopy savedCopy2 = new BookCopy(
            "BC_005", BookCondition.LIKE_NEW, LocalDate.of(2025, 9, 23),
             owner, CopyVisibility.PUBLIC);
        savedCopy2.setId(2L);
        savedCopy2.setBook(savedBook2);

        Borrowing borrowing = new Borrowing( savedBook2, savedCopy2, borrower, 
            LocalDate.of(2026, 5, 19));
        borrowing.setId(38L);
        borrowing.setStatus(BorrowingStatus.OVERDUE);

        Borrowing secondBorrowing = new Borrowing( savedBook, savedCopy, borrower, 
            LocalDate.of(2026, 4, 9));
        secondBorrowing.setId(48L);
        secondBorrowing.setStatus(BorrowingStatus.OVERDUE);

        when(borrowService.getOverdueBorrowings()).thenReturn(List.of(borrowing, secondBorrowing));

        mockMvc.perform(get("/api/v1/transactions/overdue"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value(10))
                .andExpect(jsonPath("$[0].bookCopyId").value(2))
                .andExpect(jsonPath("$[0].borrowerId").value(9))
                .andExpect(jsonPath("$[0].status").value("OVERDUE"))
                .andExpect(jsonPath("$[1].bookId").value(20))
                .andExpect(jsonPath("$[1].bookCopyId").value(30))
                .andExpect(jsonPath("$[1].borrowerId").value(9))
                .andExpect(jsonPath("$[1].status").value("OVERDUE"));
    }
    
}
