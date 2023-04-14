package bg.ittalents.instagram.user.DTOs;

public record UserBasicInfoDTO(
        long id,
        String username,
        int name,
        String profilePictureUrl) {}

