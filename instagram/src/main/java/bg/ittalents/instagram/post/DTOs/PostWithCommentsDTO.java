package bg.ittalents.instagram.post.DTOs;

import bg.ittalents.instagram.comment.DTOs.CommentWithoutRepliesDTO;
import bg.ittalents.instagram.user.DTOs.UserBasicInfoDTO;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


@Getter
@Setter
@NoArgsConstructor
public class PostWithCommentsDTO {
    private Long id;
    private String location;
    private String caption;
    private List<String> mediaUrls;
    private UserBasicInfoDTO owner;
    private List<CommentWithoutRepliesDTO> commentWithoutRepliesDtos;
    private int numberOfLikes;
    private int numberOfComments;
    private LocalDateTime dateTimeCreated;
}
