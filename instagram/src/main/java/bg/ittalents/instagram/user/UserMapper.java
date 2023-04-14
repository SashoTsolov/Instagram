package bg.ittalents.instagram.user;

import bg.ittalents.instagram.user.DTOs.*;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface UserMapper {

    UserMapper INSTANCE = Mappers.getMapper(UserMapper.class);

    // DTO to entity mappings
    User registerDtoToUser(RegisterDTO registerDto);
    User userBasicInfoDtoToUser(UserBasicInfoDTO userBasicInfoDto);
    User userChangeInfoDtoToUser(UserChangeInfoDTO userChangeInfoDto);
    User userChangePasswordDtoToUser(UserChangePasswordDTO userChangePasswordDto);
    User userForgotPasswordDtoToUser(UserForgotPasswordDTO userForgotPasswordDto);
    User userLoginDtoToUser(UserLoginDTO userLoginDto);
    User userPasswordDtoToUser(UserPasswordDTO userPasswordDto);
    User userWithoutPassAndEmailDtoToUser(UserWithoutPassAndEmailDTO userWithoutPassAndEmailDto);

    // Entity to DTO mappings
    RegisterDTO userToRegisterDto(User user);
    UserBasicInfoDTO userToUserBasicInfoDto(User user);
    UserChangeInfoDTO userToUserChangeInfoDto(User user);
    UserChangePasswordDTO userToUserChangePasswordDto(User user);
    UserForgotPasswordDTO userToUserForgotPasswordDto(User user);
    UserLoginDTO userToUserLoginDto(User user);
    UserPasswordDTO userToUserPasswordDto(User user);
    UserWithoutPassAndEmailDTO userToUserWithoutPassAndEmailDto(User user);
}