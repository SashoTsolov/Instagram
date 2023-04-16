package bg.ittalents.instagram.user;

import bg.ittalents.instagram.exceptions.BadRequestException;
import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.exceptions.UserAlreadyExistsException;
import bg.ittalents.instagram.user.DTOs.*;
import bg.ittalents.instagram.util.AbstractService;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService extends AbstractService {

    public UserWithoutPassAndEmailDTO create(RegisterDTO dto) {
        //Check if email already exists
            if (userRepository.existsByEmail(dto.getEmail())) {
                throw new UserAlreadyExistsException("Email already being used");
            }
            //Check if username already exists
            if (userRepository.existsByUsername(dto.getUsername())) {
                throw new UserAlreadyExistsException("Username already being used");
            }
            //Check if password and confirm password match
            if (!dto.getPassword().equals(dto.getConfirmPassword())) {
                throw new BadRequestException("Password confirmation mismatch");
            }
            User user = mapper.map(dto, User.class);
            user.setPassword(encoder.encode(user.getPassword()));
            user.setVerified(false);
            user.setDeactivated(false);
            user.setVerificationCode(generateVerificationCode());
            user.setDateTimeCreated(Timestamp.valueOf(LocalDateTime.now()));
            userRepository.save(user);
            return mapper.map(user, UserWithoutPassAndEmailDTO.class);
    }


    public UserWithoutPassAndEmailDTO login(UserLoginDTO dto) {
        User user = userRepository.findByEmail(dto.getEmail()).
                orElseThrow(() -> new UnauthorizedException("Wrong credentials"));
        //Check if password matches with the one in the database
        if(!encoder.matches(dto.getPassword(), user.getPassword())){
            throw new UnauthorizedException("Wrong credentials");
        }
        return mapper.map(user, UserWithoutPassAndEmailDTO.class);
    }

    public UserWithoutPassAndEmailDTO getById(long id) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new NotFoundException("User not found"));
        return mapper.map(user, UserWithoutPassAndEmailDTO.class);
    }

    //This method should be ready to go!!!
    public void block(long blockingUserId, long blockedUserId) {
        User blocker = getUserById(blockingUserId);
        User blocked = getUserById(blockedUserId);
        //Check if user is trying to block himself
        if (blocker.getId().equals(blocked.getId())) {
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
                .map(user -> mapper.map(user, UserBasicInfoDTO.class)).collect(Collectors.toList());
    }

    public void changePassword(long userId, UserChangePasswordDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (!encoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is invalid");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new BadRequestException("Confirm password does not match");
        }

        String newPassword = encoder.encode(dto.getNewPassword());
        user.setPassword(newPassword);

        userRepository.save(user);
    }
    public boolean containsIgnoreCase(String str, String subStr) {
        return str.toLowerCase().contains(subStr.toLowerCase());
    }

    public void deleteUserById(Long userId, UserPasswordDTO dto) {
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && encoder.matches(dto.getPassword(), user.getPassword())) {
            userRepository.delete(user);
        } else {
            throw new BadRequestException("Yikes");
        }
    }

//    public Page<UserBasicInfoDTO> getAllUserFollowers(Long id, long userId) {
//    }
//
//    public boolean forgotPassword(String email) {
//    }
}
