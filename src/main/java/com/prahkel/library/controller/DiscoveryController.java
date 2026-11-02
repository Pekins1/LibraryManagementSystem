package com.prahkel.library.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


import com.prahkel.library.dto.BookCopyResponse;
import com.prahkel.library.model.BookCopy;
import com.prahkel.library.service.BookService;




@RestController                          // 1: this class serves HTTP, and return values become JSON
@RequestMapping("/api/v1/dicovery")       // 2: every method below starts with this path
@Validated                              // 3: turns on validation for @PathVariable/@RequestParam
public class DiscoveryController {

    private final BookService bookService;

    public DiscoveryController(BookService bookService) {
        this.bookService = bookService;
    }
    
    @GetMapping
    public ResponseEntity<List<BookCopyResponse>> getPubliclyAvailableCopies(){
        List<BookCopy> publiclyAvailable = bookService.getDiscoverableCopies();
        return ResponseEntity.ok(publiclyAvailable.stream().map(BookCopyResponse::from).toList());
    }
}
