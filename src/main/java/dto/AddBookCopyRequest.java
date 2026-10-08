package dto;

import model.BookCondition;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record AddBookCopyRequest(
    @NotBlank 
    String barcode,

    @NotNull 
    BookCondition condition,

    LocalDate acquiredDate
) {
    
}
