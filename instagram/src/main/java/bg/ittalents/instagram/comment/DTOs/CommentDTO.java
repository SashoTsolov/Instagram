package bg.ittalents.instagram.comment.DTOs;

import bg.ittalents.instagram.comment.Comment;
import bg.ittalents.instagram.post.entities.Post;
import bg.ittalents.instagram.user.User;

import java.time.LocalDateTime;

public record CommentDTO(
        Long id,
        Post post,
        User owner,
        Comment parent,
        String content,
        LocalDateTime dateTimeCreated
) {}