package com.prahkel.library.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.prahkel.library.model.Borrowing;
import com.prahkel.library.model.BorrowingStatus;

public record BorrowingResponse (
    Long id,
    Long bookId,
    Long bookCopyId,
    Long borrowerId,
    LocalDate borrowDate,
    LocalDate returnDate,
    LocalDate dueDate,
    BorrowingStatus status,
    BigDecimal lateFee
){
    public static BorrowingResponse from(Borrowing borrowing) {
        return new BorrowingResponse(
                borrowing.getId(),
                borrowing.getBook().getId(),
                borrowing.getBookCopy().getId(),
                borrowing.getBorrower().getId(),
                borrowing.getBorrowDate(),
                borrowing.getReturnDate(),
                borrowing.getDueDate(),
                borrowing.getStatus(),
                borrowing.getLateFee()
        );
    }
}
