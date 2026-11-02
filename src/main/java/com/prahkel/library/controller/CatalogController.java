package com.prahkel.library.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.prahkel.library.dto.AddBookCopyRequest;
import com.prahkel.library.dto.BookCopyResponse;
import com.prahkel.library.dto.BookResponse;
import com.prahkel.library.dto.CreateBookRequest;
import com.prahkel.library.dto.UpdateBookRequest;
import com.prahkel.library.dto.UpdateCopyVisibilityRequest;
import com.prahkel.library.model.Book;
import com.prahkel.library.model.BookCopy;
import com.prahkel.library.service.BookService;
import com.prahkel.library.exception.BookCopyNotFoundException;



@RestController                          // 1: this class serves HTTP, and return values become JSON
@RequestMapping("/api/v1/catalog")       // 2: every method below starts with this path
@Validated                              // 3: turns on validation for @PathVariable/@RequestParam
public class CatalogController {

    private final BookService bookService;
    
    public CatalogController(BookService bookService) {   // constructor injection, Spring wires it
        this.bookService = bookService;
    }

    @PostMapping                          // POST /api/v1/catalog 
    public ResponseEntity<BookResponse> createBook(
            @Valid @RequestBody CreateBookRequest request) {  // JSON body -> DTO, validated
        Book saved = bookService.createBook(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)   // 201, the correct status for "created"
                .body(BookResponse.from(saved));

    }

    @GetMapping("/{id}")                  // GET /api/v1/catalog/5  ->  id = 5
    public ResponseEntity<BookResponse> getBookById(
            @Positive @PathVariable Long id) {   // {id} in the path lands in this parameter
        Book book = bookService.getBookById(id);
        return ResponseEntity.ok(BookResponse.from(book));  // 200
    }

    @GetMapping
    public ResponseEntity<List<BookResponse>> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author,
            @RequestParam(required = false) String genre){
        List<Book> books;
        boolean hasTitle = title != null && !title.isBlank();
        boolean hasAuthor = author != null && !author.isBlank();
        boolean hasGenre = genre != null && !genre.isBlank();     
        
        if (hasTitle && hasAuthor && hasGenre){
            books = bookService.findBooksByTitleAndAuthorAndGenre(title, author, genre);
        } else if (hasTitle && hasAuthor){
            books = bookService.findBooksByTitleAndAuthor(title, author);
        } else if (hasTitle && hasGenre){
            books = bookService.findBooksByTitleAndGenre(title, genre);
        } else if (hasAuthor && hasGenre){
            books = bookService.findBooksByAuthorAndGenre(author, genre);
        } else if (hasTitle){
            books = bookService.findBooksByTitle(title);
        } else if (hasAuthor){
            books = bookService.findBooksByAuthor(author);
        } else if (hasGenre){
            books = bookService.findBooksByGenre(genre);
        } else {
            books = bookService.getAllBooks();
        }
        return ResponseEntity.ok(books.stream().map(BookResponse::from).toList());
    }

    @PostMapping("/{id}/copies")
    public ResponseEntity<BookCopyResponse> addCopy(
            @Positive @PathVariable Long id, 
            @Valid @RequestBody AddBookCopyRequest request){
        BookCopy saved = bookService.addCopyToBook(id, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BookCopyResponse.from(saved));
    }

    @GetMapping("/{id}/copies")
    public ResponseEntity<List<BookCopyResponse>> getCopies(
            @Positive @PathVariable Long id){
        List<BookCopy> bookCopies = bookService.getCopiesForBook(id);
        return ResponseEntity.ok(bookCopies.stream().map(BookCopyResponse::from).toList());
    }

    @GetMapping("/{id}/copies/available")
    public ResponseEntity<List<BookCopyResponse>> getAvailableCopies(
            @Positive @PathVariable Long id){
        List<BookCopy> bookCopies = bookService.getAvailableCopiesForBook(id);
        return ResponseEntity.ok(bookCopies.stream().map(BookCopyResponse::from).toList());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBook(
            @Positive @PathVariable Long id){
        bookService.deleteBook(id);
        return ResponseEntity
            .noContent()
            .build();
    }

    @DeleteMapping("/{id}/copies/{copyId}")
    public ResponseEntity<Void> deleteCopy(
            @Positive @PathVariable Long id,
            @Positive @PathVariable Long copyId){
        List<BookCopy> copies = bookService.getCopiesForBook(id);
        if (copies.stream().noneMatch(copy -> copy.getId().equals(copyId))){
            throw new BookCopyNotFoundException("copy" + copyId + " not found for book " + id);
        }
        bookService.deleteCopy(copyId);
        return ResponseEntity
            .noContent()
            .build();
    }
    

    @PutMapping("/{id}")
    public ResponseEntity<BookResponse> updateBook(
            @Positive @PathVariable Long id,
            @Valid @RequestBody UpdateBookRequest request){
        Book updatedBook = bookService.updateBook(id, request);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BookResponse.from(updatedBook));
    }

    @PostMapping("/{id}/archive")
    public ResponseEntity<Void> archiveBook(
            @Positive @PathVariable Long id){
        bookService.archiveBook(id);
        return ResponseEntity
        .noContent()
        .build();
    }

    @PatchMapping("/{copyId}/visibility")
    public ResponseEntity<BookCopyResponse> updateCopyVisibility(
            @Positive @PathVariable Long copyId,
            @Valid @RequestBody UpdateCopyVisibilityRequest request) {
        BookCopy updated = bookService.setCopyVisibility(copyId, request);
        return ResponseEntity.ok(BookCopyResponse.from(updated));
    }

}
