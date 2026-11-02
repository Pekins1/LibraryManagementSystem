package com.prahkel.library.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.prahkel.library.dto.BorrowBookRequest;
import com.prahkel.library.dto.BorrowingResponse;
import com.prahkel.library.dto.ReturnBookRequest;
import com.prahkel.library.model.Borrowing;
import com.prahkel.library.service.BorrowService;


@RestController                          // 1: this class serves HTTP, and return values become JSON
@RequestMapping("/api/v1/transactions")       // 2: every method below starts with this path
@Validated                              // 3: turns on validation for @PathVariable/@RequestParam
public class TransactionController {
    private final BorrowService borrowService;

    public TransactionController(BorrowService borrowService){
        this.borrowService = borrowService;
    }

    @PostMapping("/borrow")
    public ResponseEntity<BorrowingResponse> borrowABook(
            @Valid @RequestBody BorrowBookRequest request){
        Borrowing borrowed = borrowService.borrowBook(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BorrowingResponse.from(borrowed)); 
    }

    @PostMapping("/return")
    public ResponseEntity<BorrowingResponse> returnABook(
            @Valid @RequestBody  ReturnBookRequest request){
        Borrowing returned = borrowService.returnBook(request);
        return ResponseEntity.ok(BorrowingResponse.from(returned));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowingResponse> getABorrowRecord(
            @Positive @PathVariable Long id) {
        Borrowing borrowRecord = borrowService.getBorrowingById(id);
        return ResponseEntity.ok(BorrowingResponse.from(borrowRecord));
    }

    @GetMapping("/borrower/{borrowerId}/active")
    public ResponseEntity<List<BorrowingResponse>> borrowersActiveLoans(
            @Positive @PathVariable Long borrowerId) {
        List<Borrowing> activeLoans = borrowService.getActiveBorrowingsForBorrower(borrowerId);
        return ResponseEntity.ok(activeLoans.stream().map(BorrowingResponse::from).toList());        
    }

    @GetMapping("/borrower/{borrowerId}/history")
    public ResponseEntity<List<BorrowingResponse>> borrowersLoanHistory(
            @Positive @PathVariable Long borrowerId) {
        List<Borrowing> loanHistory = borrowService.getBorrowingHistoryForBorrower(borrowerId);
        return ResponseEntity.ok(loanHistory.stream().map(BorrowingResponse::from).toList());        
    }

    @GetMapping("/active")
    public ResponseEntity<List<BorrowingResponse>> allActiveLoans() {
        List<Borrowing> activeLoans = borrowService.getAllActiveBorrowings();
        return ResponseEntity.ok(activeLoans.stream().map(BorrowingResponse::from).toList());        
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowingResponse>> allOverdueLoans() {
        List<Borrowing> overdueLoans = borrowService.getOverdueBorrowings();
        return ResponseEntity.ok(overdueLoans.stream().map(BorrowingResponse::from).toList());        
    }
}
