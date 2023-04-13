package bg.ittalents.instagram.post.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class PostPreviewDTO {
    private Long id;
    private String mediaUrl;
    private int numberOfLikes;
    private int numberOfComments;
}
