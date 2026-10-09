package com.prahkel.library.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.*;

import com.prahkel.library.dto.BorrowBookRequest;
import com.prahkel.library.dto.ReturnBookRequest;
import com.prahkel.library.model.Borrowing;


public interface BorrowService {

    Borrowing borrowBook(@Valid @NotNull BorrowBookRequest borrow);

    Borrowing returnBook(@Valid @NotNull ReturnBookRequest request);

    Borrowing getBorrowingById(Long borrowingId);

    List<Borrowing> getActiveBorrowingsForBorrower(Long borrowerId);

    List<Borrowing> getBorrowingHistoryForBorrower(Long borrowerId);

    Optional<Borrowing> getCurrentBorrowingForBook(Long bookId);

    List<Borrowing> getAllActiveBorrowings();

    List<Borrowing> getOverdueBorrowings();
    
}
