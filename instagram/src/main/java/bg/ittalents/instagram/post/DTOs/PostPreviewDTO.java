package bg.ittalents.instagram.post.DTOs;

public record PostPreviewDTO(
        Long id,
        String mediaUrl,
        int numberOfLikes,
        int numberOfComments) { }

