package bg.ittalents.instagram.post.DTOs;

import bg.ittalents.instagram.user.DTOs.UserBasicInfoDTO;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class PostWithoutCommentsDTO {
    private long id;
    private String location;
    private String caption;
    private List<String> mediaUrls;
    private UserBasicInfoDTO owner;
    private int numberOfLikes;
    private int numberOfComments;
    private LocalDateTime dateTimeCreated;
}