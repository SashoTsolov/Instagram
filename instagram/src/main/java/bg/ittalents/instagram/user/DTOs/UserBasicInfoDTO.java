package bg.ittalents.instagram.user.DTOs;

public record UserBasicInfoDTO(
        Long id,
        String username,
        String name,
        String profilePictureUrl
) {}
