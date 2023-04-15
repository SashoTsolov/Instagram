package bg.ittalents.instagram.user.DTOs;

public record UserWithoutPassAndEmailDTO(
        Long id,
        String username,
        String name,
        String bio,
        String profilePictureUrl
) {}
