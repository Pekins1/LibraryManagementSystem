package dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record UpdateBookRequest(
        @NotBlank(message = "Title is required")
        String title,

        @NotBlank(message = "Author is required")
        String author,

        @NotBlank(message = "Genre is required")
        String genre,

        @NotBlank(message = "ISBN is required")
        @Pattern(regexp = "^[0-9-]{10,17}$", message = "ISBN must contain 10 to 17 digits or hyphens")
        String isbn,

        @Min(value = 1000, message = "Published year must be at least 1000")
        @Max(value = 2100, message = "Published year must not exceed 2100")
        int publishedYear
) {
}
