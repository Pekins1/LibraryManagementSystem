package dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import model.BookCondition;

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
