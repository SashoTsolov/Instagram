package bg.ittalents.instagram.post.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class SearchRequestDTO {
    @NotBlank(message = "Search string must not be blank")
    private String searchString;

    @PositiveOrZero(message = "Page number must be greater than or equal to zero")
    private int page;

    @PositiveOrZero(message = "Page size must be greater than or equal to zero")
    private int size;
}
