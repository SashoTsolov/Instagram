package bg.ittalents.instagram.comment.DTOs;

import bg.ittalents.instagram.user.DTOs.UserBasicInfoDTO;

import java.time.LocalDateTime;

public record CommentDTO(
        Long id,
        UserBasicInfoDTO owner,
        String content,
        LocalDateTime dateTimeCreated
) {}