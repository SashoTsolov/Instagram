package bg.ittalents.instagram.comment.DTOs;

import bg.ittalents.instagram.user.DTOs.UserBasicInfoWithoutNameDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class CommentWithoutRepliesDTO {

    private long id;
    private UserBasicInfoWithoutNameDTO owner;
    private String content;
    private int numberOfLikes;
    private int numberOfReplies;
    private LocalDateTime dateTimeCreated;
}
