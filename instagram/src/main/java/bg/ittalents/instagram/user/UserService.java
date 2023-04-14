package bg.ittalents.instagram.user;

import bg.ittalents.instagram.exceptions.BadRequestException;
import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.exceptions.UserAlreadyExistsException;
import bg.ittalents.instagram.user.DTOs.RegisterDTO;
import bg.ittalents.instagram.user.DTOs.UserLoginDTO;
import bg.ittalents.instagram.user.DTOs.UserWithoutPassAndEmailDTO;
import bg.ittalents.instagram.util.AbstractService;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class UserService extends AbstractService {

    UserMapper userMapper;

//    public UserWithoutPassAndEmailDTO create(RegisterDTO dto) {
//        if (!dto.getPassword().equals(dto.getConfirmPassword())){
//            throw new BadRequestException("Password confirmation mismatch");
//        }
//        //Additional validation needed
//        //TODO
//        if (userRepository.existsByEmail(dto.getEmail())){
//            throw new UserAlreadyExistsException("Email already being used");
//        }
//        User u = mapper.map(dto, User.class);
//        u.setPassword(passwordEncoder.encode(u.getPassword()));
//        u.setVerified(false);
//        //Have to add for timestamp and verification code
//        u.setDeactivated(false);
//        userRepository.save(u);
//        return mapper.map(u, UserWithoutPassAndEmailDTO.class);
//    }

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
        User user = userMapper.registerDtoToUser(dto);
        user.setPassword(encoder.encode(user.getPassword()));
        user.setVerified(false);
        user.setDeactivated(false);
//        u.setDateOfBirth((Date) dto.getDateOfBirth());
        //TODO
        user.setVerified(false);
        user.setVerificationCode(generateVerificationCode());
//        u.setDateTimeCreated(Timestamp.valueOf(LocalDateTime.now()));
        //TODO
        userRepository.save(user);

        return userMapper.userToUserWithoutPassAndEmailDto(user);
    }


    public UserWithoutPassAndEmailDTO login(UserLoginDTO dto) {
        User user = userRepository.findByEmail(dto.email()).
                orElseThrow(() -> new UnauthorizedException("Wrong credentials"));
        //Check if password matches with the one in the database
        if(!encoder.matches(dto.password(), user.getPassword())){
            throw new UnauthorizedException("Wrong credentials");
        }
        return userMapper.userToUserWithoutPassAndEmailDto(user);
    }

    public UserWithoutPassAndEmailDTO getById(long id) {
        User user = userRepository.findById(id).
                orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.userToUserWithoutPassAndEmailDto(user);
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

    //Don't forget to add NOT NULL in SQL table after finishing testing for date_time_of_follow
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
}
