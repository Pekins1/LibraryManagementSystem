package com.prahkel.library.dto;

import com.prahkel.library.model.BookCopy;
import com.prahkel.library.model.CopyVisibility;
import com.prahkel.library.model.BookCondition;

import java.time.LocalDate;


public record BookCopyResponse(
    Long id,
    Long bookId,
    Long ownerId,
    CopyVisibility visibility,
    boolean isAvailable,
    BookCondition condition,
    String barcode,
    int timesBorrowed,
    LocalDate acquiredDate


) {
    public static BookCopyResponse from(BookCopy bookCopy){
        return new BookCopyResponse(
            bookCopy.getId(),
            bookCopy.getBook().getId(),
            bookCopy.getOwner().getId(),
            bookCopy.getVisibility(),
            bookCopy.isAvailable(),
            bookCopy.getCondition(),
            bookCopy.getBarcode(),
            bookCopy.getTimesBorrowed(),
            bookCopy.getAcquiredDate()
        );
     
    }
    
}
