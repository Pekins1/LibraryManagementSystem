package dto;

import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.NotNull;

public record BorrowBookRequest(
    @NotNull
    @Positive 
    Long bookId,

    @NotNull 
    @ Positive
    Long borrowerId
) {
    
}
