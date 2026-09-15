package dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record ReturnBookRequest(
    @NotNull
    @Positive
    Long bookId,

    @NotNull
    @Positive
    Long borrowerId
)
{
    
}
