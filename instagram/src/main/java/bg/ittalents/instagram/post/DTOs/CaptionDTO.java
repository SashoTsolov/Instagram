package bg.ittalents.instagram.post.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CaptionDTO {
    @NotBlank(message = "Caption must not be blank")
    @Size(max = 2000, message = "Caption must have maximum size of {max} characters")
    private String caption;
}