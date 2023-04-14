package bg.ittalents.instagram.user.DTOs;

public record UserWithoutPassAndEmailDTO(
        long id,
        String username,
        String name,
        String bio,
        String profilePictureUrl
) {}
