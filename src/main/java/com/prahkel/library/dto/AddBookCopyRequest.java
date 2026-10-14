package com.prahkel.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

import com.prahkel.library.model.BookCondition;
import com.prahkel.library.model.CopyVisibility;

public record AddBookCopyRequest(
    @NotBlank 
    String barcode,

    @NotNull 
    BookCondition condition,

    LocalDate acquiredDate,

    @NotNull 
    @Positive 
    Long ownerId,

    @NotNull 
    CopyVisibility visibility
) {
    
}
