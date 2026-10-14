package com.prahkel.library.dto;

import com.prahkel.library.model.BookCondition;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReturnBookRequest(
    @NotNull
    @Positive
    Long bookId,

    @NotNull
    @Positive
    Long borrowerId,

    @NotNull
    BookCondition condition
)
{
    
}
