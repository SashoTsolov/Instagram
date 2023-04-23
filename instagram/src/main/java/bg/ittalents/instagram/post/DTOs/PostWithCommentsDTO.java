package bg.ittalents.instagram.post.DTOs;

import bg.ittalents.instagram.comment.DTOs.CommentDTO;
import bg.ittalents.instagram.user.DTOs.UserBasicInfoWithoutNameDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class PostWithCommentsDTO {
    private long id;
    private String location;
    private String caption;
    private List<String> mediaUrls;
    private UserBasicInfoWithoutNameDTO owner;
    private List<CommentDTO> comments;
    private int numberOfLikes;
    private int numberOfComments;
    private LocalDateTime dateTimeCreated;
}
