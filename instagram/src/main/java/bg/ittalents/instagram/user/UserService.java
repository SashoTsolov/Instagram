package bg.ittalents.instagram.user;

import bg.ittalents.instagram.exceptions.BadRequestException;
import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.exceptions.UserAlreadyExistsException;
import bg.ittalents.instagram.user.DTOs.*;
import bg.ittalents.instagram.util.AbstractService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService extends AbstractService {


     UserMapper userMapper;


    public UserWithoutPassAndEmailDTO create(RegisterDTO dto) {
        //Check if email already exists
            if (userRepository.existsByEmail(dto.email())) {
                throw new UserAlreadyExistsException("Email already being used");
            }
            //Check if username already exists
            if (userRepository.existsByUsername(dto.username())) {
                throw new UserAlreadyExistsException("Username already being used");
            }
            //Check if password and confirm password match
            if (!dto.password().equals(dto.confirmPassword())) {
                throw new BadRequestException("Password confirmation mismatch");
            }
            User user = userMapper.INSTANCE.registerDtoToUser(dto);
            user.setPassword(encoder.encode(user.getPassword()));
            user.setVerified(false);
            user.setDeactivated(false);
//            user.setDateOfBirth(java.sql.Date.valueOf(localDate));
//        u.setDateOfBirth((Date) dto.getDateOfBirth());
            //TODO
            user.setVerified(false);
            user.setVerificationCode(generateVerificationCode());
//        u.setDateTimeCreated(Timestamp.valueOf(LocalDateTime.now()));
            //TODO
            userRepository.save(user);
            return userMapper.INSTANCE.userToUserWithoutPassAndEmailDto(user);
    }


    public UserWithoutPassAndEmailDTO login(UserLoginDTO dto) {
        User user = userRepository.findByEmail(dto.email()).
                orElseThrow(() -> new UnauthorizedException("Wrong credentials"));
        //Check if password matches with the one in the database
        if(!encoder.matches(dto.password(), user.getPassword())){
            throw new UnauthorizedException("Wrong credentials");
        }
        return userMapper.INSTANCE.userToUserWithoutPassAndEmailDto(user);
    }

    public UserWithoutPassAndEmailDTO getById(long id) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.INSTANCE.userToUserWithoutPassAndEmailDto(user);
    }

    //This method should be ready to go!!!
    public void block(long blockingUserId, long blockedUserId) {
        User blocker = getUserById(blockingUserId);
        User blocked = getUserById(blockedUserId);
        //Check if user is trying to block himself
        if (blocker.getId() == blocked.getId()) {
            throw new RuntimeException("You cannot block yourself");
        }
        //Check if user is trying to block someone that's already blocked
        if (blocker.getBlocked().contains(blocked)) {
            throw new RuntimeException("You have already blocked this user");
        }
        blocker.getBlocked().add(blocked);
        userRepository.save(blocker);
    }

    private String generateVerificationCode() {
            UUID uuid = UUID.randomUUID();
            String code = uuid.toString().substring(0, 6);
            return code;
    }

    public int follow(long followerId, long followedId) {
        Optional<User> follower = userRepository.findById(followerId);
        Optional<User> followed = userRepository.findById(followedId);
        if (follower.isEmpty() || followed.isEmpty()){
            throw new NotFoundException("User not found");
        }
        if (followerId == followedId){
            throw new BadRequestException("You can't follow yourself");
        }
        if (follower.get().getFollowing().contains(followed.get())) {
            follower.get().getFollowing().remove(followed.get());
            followed.get().getFollowers().remove(follower.get());
        } else {
            follower.get().getFollowing().add(followed.get());
            followed.get().getFollowers().add(follower.get());
        }
        userRepository.save(follower.get());
        userRepository.save(followed.get());
        return followed.get().getFollowers().size();
    }

    public List<UserBasicInfoDTO> searchUsersByUsername(String username) {
        List<User> users = userRepository.findAll();
        List<User> filteredUsers = users.stream()
                .filter(user -> containsIgnoreCase(user.getUsername(), username))
                .limit(55)
                .toList();
        System.out.println(filteredUsers.size());
        return filteredUsers.stream()
                .map(user -> userMapper.INSTANCE.userToUserBasicInfoDto(user)).collect(Collectors.toList());
    }

    public void changePassword(long userId, UserChangePasswordDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!encoder.matches(dto.currentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is invalid");
        }
        if (!dto.newPassword().equals(dto.confirmNewPassword())) {
            throw new BadRequestException("Confirm password does not match");
        }

        String newPassword = encoder.encode(dto.newPassword());
        user.setPassword(newPassword);

        userRepository.save(user);
    }
    public boolean containsIgnoreCase(String str, String subStr) {
        return str.toLowerCase().contains(subStr.toLowerCase());
    }

//    public Page<UserBasicInfoDTO> getAllUserFollowers(Long id, long userId) {
//    }
//
//    public boolean forgotPassword(String email) {
//    }
}
