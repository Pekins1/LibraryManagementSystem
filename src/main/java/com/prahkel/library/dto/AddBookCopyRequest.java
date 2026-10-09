package com.prahkel.library.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

import com.prahkel.library.model.BookCondition;

public record AddBookCopyRequest(
    @NotBlank 
    String barcode,

    @NotNull 
    BookCondition condition,

    LocalDate acquiredDate
) {
    
}
