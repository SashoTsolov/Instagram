package bg.ittalents.instagram.post.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostPreviewDTO {
    private Long id;
    private List<String> mediaUrls;
    private int numberOfLikes;
    private int numberOfComments;
}