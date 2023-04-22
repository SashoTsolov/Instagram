package bg.ittalents.instagram.comment.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class CommentContentDTO {
    @NotBlank(message = "Comment content must not be blank")
    @Size(max = 2000, message = "Comment content must have maximum size of {max} characters")
    private String content;
}