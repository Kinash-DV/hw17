package dv.kinash.hw15.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BookNewDTO {
    @NotBlank(message = "Description is required")
    private String description;
    @NotBlank(message = "Author is required")
    private String author;
    @NotNull(message = "ISBN is required")
    @Min(value = 1000000000L, message = "ISBN cannot be less than 10 digits")
    @Max(value = 9999999999999L, message = "ISBN cannot be more than 13 digits")
    private Long ISBN;
}
