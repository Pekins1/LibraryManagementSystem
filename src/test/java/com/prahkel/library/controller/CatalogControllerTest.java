package com.prahkel.library.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.prahkel.library.dto.AddBookCopyRequest;
import com.prahkel.library.dto.CreateBookRequest;
import com.prahkel.library.dto.UpdateBookRequest;
import com.prahkel.library.dto.UpdateCopyVisibilityRequest;
import com.prahkel.library.exception.BookAlreadyExistsException;
import com.prahkel.library.exception.BookCopyNotFoundException;
import com.prahkel.library.exception.BookNotFoundException;
import com.prahkel.library.exception.BookWithActiveLoanCannotBeArchivedException;
import com.prahkel.library.model.Book;
import com.prahkel.library.model.BookCondition;
import com.prahkel.library.model.BookCopy;
import com.prahkel.library.model.CopyVisibility;
import com.prahkel.library.model.LibraryMember;
import com.prahkel.library.model.MemberType;
import com.prahkel.library.service.BookService;

import tools.jackson.databind.ObjectMapper;

@WebMvcTest(CatalogController.class)
class CatalogControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @Test 
    void createBook_shouldReturn201WhenValid() throws Exception {
        CreateBookRequest request = new CreateBookRequest(
            "Dune", "Frank Herbert", "Sci-Fi", "9780441172719", 1965);
        Book saved = new Book(
            "Dune", "Frank Herbert", "Sci-Fi", "9780441172719", 1965);
        saved.setId(1L); 

        when(bookService.createBook(any())).thenReturn(saved);

        mockMvc.perform(post("/api/v1/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                    )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Dune"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void createBook_shouldReturn400WhenRequestIsInvalid() throws Exception{
        String invalidBody = """
                {"author":"Frank Herbert","genre":"Sci-Fi",
                "isbn":"9780441172719","publishedYear":1965}
                """;
        
        mockMvc.perform(post("/api/v1/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("title")));
            
        verify(bookService, never()).createBook(any());
    }

    @Test 
    void createBook_shouldReturn409WhenBookAlreadyExists() throws Exception{
        CreateBookRequest request = new CreateBookRequest(
            "Dune", "Frank Herbert", "Sci-Fi", "9780441172719", 1965);

        when(bookService.createBook(any())).thenThrow(
            new BookAlreadyExistsException("Book already with: " + request.isbn() + " already exists.")
        );

        mockMvc.perform(post("/api/v1/catalog")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                    )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }


    @Test 
    void getBookById_shouldReturn200WhenBookExists() throws Exception{

        Book saved = new Book(
            "Dune", "Frank Herbert", "Sci-Fi", "9780441172719", 1965);
        saved.setId(1L); 

        when(bookService.getBookById(1L)).thenReturn(saved);
        
        mockMvc.perform(get("/api/v1/catalog/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.author").value("Frank Herbert"))
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test 
    void getBookById_shouldReturn404WhenMissing() throws Exception {
        when(bookService.getBookById(99L)).thenThrow(
                new BookNotFoundException("Book not found with ID: 99")
        );

        mockMvc.perform(get("/api/v1/catalog/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test 
    void searchBooks_shouldReturnBooksMatchingTitle() throws Exception{
        Book saved = new Book(
            "Dune", "Frank Herbert", "Sci-Fi", "9780441172719", 1965);
        saved.setId(1L); 

        when(bookService.findBooksByTitle("dune")).thenReturn(List.of(saved));

        mockMvc.perform(get("/api/v1/catalog")
                        .param("title", "dune"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].genre").value("Sci-Fi")
        );
        
        verify(bookService).findBooksByTitle("dune");
    }

    @Test 
    void searchBooks_shouldReturnAllBooksWhenNoParametersGiven() throws Exception{
        Book bookOne = new Book(
            "Dune", "Frank Herbert", "Sci-Fi", "9780441172719", 1965);
        bookOne.setId(1L); 

        Book bookTwo = new Book("Clean Code", "Robert C. Martin", "Software", "978-0132350884", 2008 );
        bookTwo.setId(2L);

        when(bookService.getAllBooks()).thenReturn(List.of(bookOne, bookTwo));

        mockMvc.perform(get("/api/v1/catalog"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].title").value("Dune"))
                .andExpect(jsonPath("$[1].title").value("Clean Code"))
                .andExpect(jsonPath("$[0].author").value("Frank Herbert"))
                .andExpect(jsonPath("$[1].author").value("Robert C. Martin")
        );

        verify(bookService).getAllBooks();
    }

    @Test 
    void addCopy_shouldReturn201WhenBookExists() throws Exception{

        LocalDate fixedDate = LocalDate.of(2026, 1, 1);

        LibraryMember owner = new LibraryMember("Rahkel Pekins", "example@gmail.com", MemberType.INDIVIDUAL);
        owner.setId(2L);

        Book saved = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441172719", 1965);
        saved.setId(1L);

        AddBookCopyRequest request = new AddBookCopyRequest("BC_001", BookCondition.NEW, fixedDate, 2L, CopyVisibility.PRIVATE);

        BookCopy copy = new BookCopy("BC_001", BookCondition.NEW, fixedDate, owner , CopyVisibility.PRIVATE);
        copy.setBook(saved);

        when(bookService.addCopyToBook(1L, request)).thenReturn(copy);

        mockMvc.perform(post("/api/v1/catalog/1/copies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                    )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.ownerId").value(2))
                .andExpect(jsonPath("$.bookId").value(1))
                .andExpect(jsonPath("$.barcode").value("BC_001"));

    } 

     @Test 
    void addCopy_shouldReturn404WhenBookDoesNotExists() throws Exception{

        LocalDate fixedDate = LocalDate.of(2026, 1, 1);

        AddBookCopyRequest request = new AddBookCopyRequest("BC_001", BookCondition.NEW, fixedDate, 2L, CopyVisibility.PRIVATE);

        when(bookService.addCopyToBook(1L, request)).thenThrow(
            new BookNotFoundException("Book with ID:1 not found.")
        );

        mockMvc.perform(post("/api/v1/catalog/1/copies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                    )
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));

    } 

    @Test 
    void getCopies_shouldReturn200WithCopyList() throws Exception{
        
        LocalDate fixedDate = LocalDate.of(2026, 1, 1);

        LibraryMember owner = new LibraryMember("Rahkel Pekins", "example@gmail.com", MemberType.INDIVIDUAL);
        owner.setId(2L);

        Book saved = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441172719", 1965);
        saved.setId(1L);


        BookCopy firstCopy = new BookCopy("BC_001", BookCondition.NEW, fixedDate, owner , CopyVisibility.PUBLIC);
        firstCopy.setBook(saved);

        BookCopy thirdCopy = new BookCopy("BC_003", BookCondition.GOOD, fixedDate, owner , CopyVisibility.PUBLIC);
        thirdCopy.setBook(saved);

        when(bookService.getCopiesForBook(1L)).thenReturn(List.of(firstCopy, thirdCopy));

        mockMvc.perform(get("/api/v1/catalog/1/copies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].barcode").value("BC_001"))
                .andExpect(jsonPath("$[1].barcode").value("BC_003"))
                .andExpect(jsonPath("$[0].visibility").value("PUBLIC"))
                .andExpect(jsonPath("$[1].visibility").value("PUBLIC"));
        
    }

    @Test 
    void getCopies_shouldReturnEmptyListWhenBookHasNoCopies() throws Exception{

        Book saved = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441172719", 1965);
        saved.setId(1L);

        when(bookService.getCopiesForBook(1L)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/catalog/1/copies"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test 
    void getAvailableCopies_shouldReturnOnlyPublicAvailableCopies() throws Exception{
        LocalDate fixedDate = LocalDate.of(2026, 1, 1);

        LibraryMember owner = new LibraryMember("Rahkel Pekins", "example@gmail.com", MemberType.INDIVIDUAL);
        owner.setId(2L);

        Book saved = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441172719", 1965);
        saved.setId(1L);


        BookCopy firstCopy = new BookCopy("BC_001", BookCondition.NEW, fixedDate, owner , CopyVisibility.PUBLIC);
        firstCopy.setIsAvailable(false);
        firstCopy.setBook(saved);

        BookCopy secondCopy = new BookCopy("BC_002", BookCondition.FAIR, fixedDate, owner , CopyVisibility.PRIVATE);
        secondCopy.setBook(saved);

        BookCopy thirdCopy = new BookCopy("BC_003", BookCondition.GOOD, fixedDate, owner , CopyVisibility.PUBLIC);
        thirdCopy.setIsAvailable(true);
        thirdCopy.setBook(saved);

        when(bookService.getAvailableCopiesForBook(1L)).thenReturn(List.of(thirdCopy));

        mockMvc.perform(get("/api/v1/catalog/1/copies/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].barcode").value("BC_003"))
                .andExpect(jsonPath("$[0].visibility").value("PUBLIC"));

    }

    @Test 
    void updateBook_shouldReturn200WhenBookExists() throws Exception{

        Book savedUpdate = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441173481", 1965);
        savedUpdate.setId(1L);

        UpdateBookRequest update = new UpdateBookRequest("Dune", "Frank Herbert", "Sci-Fi", "9780441173481", 1965);
        
        when(bookService.updateBook(eq(1L), any(UpdateBookRequest.class))).thenReturn(savedUpdate);

        mockMvc.perform(put("/api/v1/catalog/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(update)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.isbn").value("9780441173481"));
    }

    @Test 
    void updateBook_shouldReturn404WhenBookDoesNotExist() throws Exception{

        UpdateBookRequest update = new UpdateBookRequest("Dune", "Frank Herbert", "Sci-Fi", "9780441173481", 1965);
        
        when(bookService.updateBook(eq(1L), any(UpdateBookRequest.class))).thenThrow(
            new BookNotFoundException("Book with Id:1 not found")
        );

        mockMvc.perform(put("/api/v1/catalog/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test 
    void archiveBook_shouldReturn204WhenBookExists() throws Exception{
       
        doNothing().when(bookService).archiveBook(1L);

        mockMvc.perform(post("/api/v1/catalog/1/archive"))
                .andExpect(status().isNoContent());
    }

    @Test 
    void archiveBook_shouldReturn400WhenBookHasActiveLoan() throws Exception{

        doThrow(new BookWithActiveLoanCannotBeArchivedException("Book with id:1 has an active loan"))
        .when(bookService).archiveBook(1L);

        mockMvc.perform(post("/api/v1/catalog/1/archive"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test 
    void deleteBook_shouldReturn204WhenBookExists() throws Exception{

        doNothing().when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/v1/catalog/1"))
                .andExpect(status().isNoContent());
    }

    @Test 
    void deleteBook_shouldReturn404WhenBookDoesNotExist() throws Exception{

        doThrow(new BookNotFoundException("Book with id:1 not found."))
        .when(bookService).deleteBook(1L);

        mockMvc.perform(delete("/api/v1/catalog/1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteCopy_shouldReturn204WhenCopyBelongsToBook() throws Exception {
        LibraryMember owner = new LibraryMember("Rahkel Pekins", "example@gmail.com", MemberType.INDIVIDUAL);
        owner.setId(2L);

        BookCopy copy = new BookCopy("BC_002", BookCondition.FAIR, LocalDate.of(2026, 1, 1), owner, CopyVisibility.PUBLIC);
        copy.setId(2L);

        when(bookService.getCopiesForBook(1L)).thenReturn(List.of(copy));
        doNothing().when(bookService).deleteCopy(2L);

        mockMvc.perform(delete("/api/v1/catalog/1/copies/2"))
                .andExpect(status().isNoContent());
    }


    @Test 
    void deleteCopy_shouldReturn404WhenCopyDoesNotBelongToBook() throws Exception{

        when(bookService.getCopiesForBook(1L)).thenReturn(List.of());

        doThrow(new BookNotFoundException("Book with id:2 not found."))
        .when(bookService).deleteBook(2L);

        mockMvc.perform(delete("/api/v1/catalog/1/copies/2"))
                .andExpect(status().isNotFound());
    }

    @Test 
    void updateCopyVisibility_shouldReturn200WhenCopyExists() throws Exception{

        LocalDate fixedDate = LocalDate.of(2026, 1, 1);

        LibraryMember owner = new LibraryMember("Rahkel Pekins", "example@gmail.com", MemberType.INDIVIDUAL);
        owner.setId(2L);
        
        Book saved = new Book("Dune", "Frank Herbert", "Sci-Fi", "9780441172719", 1965);
        saved.setId(1L);
        
        BookCopy firstCopy = new BookCopy("BC_001", BookCondition.NEW, fixedDate, owner , CopyVisibility.PRIVATE);
        firstCopy.setId(2L);
        firstCopy.setBook(saved);
                
        UpdateCopyVisibilityRequest copyUpdateRequest = new UpdateCopyVisibilityRequest(CopyVisibility.PRIVATE);


        when(bookService.setCopyVisibility(eq(2L), any(UpdateCopyVisibilityRequest.class))).thenReturn(firstCopy);

        mockMvc.perform(patch("/api/v1/catalog/2/visibility")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(copyUpdateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.visibility").value("PRIVATE"));
                        
    }

    @Test 
    void updateCopyVisibility_shouldReturn404WhenCopyDoesNotExist() throws Exception{

        UpdateCopyVisibilityRequest copyUpdateRequest = new UpdateCopyVisibilityRequest(CopyVisibility.PRIVATE);


        when(bookService.setCopyVisibility(eq(2L), any(UpdateCopyVisibilityRequest.class))).thenThrow(
            new BookCopyNotFoundException("Copy with Id:2 not found.")
        );

        mockMvc.perform(patch("/api/v1/catalog/2/visibility")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(copyUpdateRequest)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value(404));
    }
}
