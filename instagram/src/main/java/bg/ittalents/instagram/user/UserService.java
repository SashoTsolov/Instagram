package bg.ittalents.instagram.user;

import bg.ittalents.instagram.exception.BadRequestException;
import bg.ittalents.instagram.exception.NotFoundException;
import bg.ittalents.instagram.exception.UnauthorizedException;
import bg.ittalents.instagram.exception.UserAlreadyExistsException;
import bg.ittalents.instagram.follower.Follow;
import bg.ittalents.instagram.follower.FollowKey;
import bg.ittalents.instagram.user.DTOs.*;
import bg.ittalents.instagram.util.AbstractService;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class UserService extends AbstractService {

    public void create(RegisterDTO dto) {
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
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));
        user.setDateTimeCreated(Timestamp.valueOf(LocalDateTime.now()));
        userRepository.save(user);

        // Send verification email to user
        String subject = "Account Verification";
        String verificationLink = "https://localhost:8080/users/verify?verification-token=" + user.getVerificationCode();
        String text = "Click the link below to verify your account: \n" + verificationLink;
        sendEmail(user.getEmail(), subject, text);
    }

    public UserWithoutPassAndEmailDTO login(UserLoginDTO dto) {
        User user = getUserByEmail(dto.getEmail());
        //Check if password matches with the one in the database
        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Wrong credentials");
        }
//        if (!user.isVerified()) {
//            throw new BadRequestException("Account is not verified yet");
//        }
        if (user.isDeactivated()) {
            user.setDeactivated(false);
            userRepository.save(user);
        }
        return getUserWithoutPassAndEmailDTO(user.getId());
    }

    public UserWithoutPassAndEmailDTO getById(long id) {
        User user = getUserById(id);
        return getUserWithoutPassAndEmailDTO(user.getId());
    }

    //This method should be ready to go!!!
    @Transactional
    public void block(long blockingUserId, long blockedUserId) {
        User blocker = getUserById(blockingUserId);
        User blocked = getUserById(blockedUserId);
        //Check if user is trying to block himself
        if (blocker.getId() == blocked.getId()) {
            throw new BadRequestException("You can't block yourself");
        }
        //Check if user is trying to block someone that's already blocked
        if (blocker.getBlocked().contains(blocked)) {
            blocker.getBlocked().remove(blocked);
            blocked.getBlockedBy().remove(blocker);
            userRepository.save(blocker);
            userRepository.save(blocked);
            return;
        }
        blocker.getBlocked().add(blocked);
        blocked.getBlockedBy().add(blocker);
        if (blocker.getFollowing().contains(blocked)){
            blocker.getFollowing().remove(blocked);
            blocked.getFollowers().remove(blocker);
        }
        userRepository.save(blocker);
        userRepository.save(blocked);
    }

    private String generateVerificationCode() {
        UUID uuid = UUID.randomUUID();
        String code = uuid.toString().substring(0, 12);
        return code;
    }

    @Transactional
    public String follow(long followerId, long followedId) {
        User follower = getUserById(followerId);
        User followed = getUserById(followedId);
        if (follower == null || followed == null) {
            throw new NotFoundException("User not found");
        }
        FollowKey followKey = new FollowKey(followed.getId(), follower.getId());
        if (followerId == followedId) {
            throw new BadRequestException("You can't follow yourself");
        }
        if (follower.getBlocked().contains(followed) || follower.getBlockedBy().contains(followed)){
            throw new BadRequestException("Unable to follow user");
        }
        if (follower.getFollowing().contains(followed)) {
            followRepository.deleteById(followKey);
            userRepository.save(follower);
            userRepository.save(followed);
            return followed.getUsername() + " unfollowed";
        }
        // create and save the Follower entity
        Follow followEntity = new Follow();
        followEntity.setId(followKey);
        followEntity.setFollowingUser(follower);
        followEntity.setFollowedUser(followed);
        followEntity.setDateTimeOfFollow(Timestamp.valueOf(LocalDateTime.now()));
        followRepository.save(followEntity);
        return followed.getUsername() + " followed";
    }

    public void changePassword(long userId, UserChangePasswordDTO dto) {
        User user = getUserById(userId);
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

    public void deleteUserById(long userId, UserPasswordDTO dto) {
        User user = getUserById(userId);
        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadRequestException("Wrong credentials");
        }
        userRepository.delete(user);
    }

    public boolean deactivateUser(long userId, UserPasswordDTO dto) {
        User user = getUserById(userId);
        if (encoder.matches(dto.getPassword(), user.getPassword())) {
            user.setDeactivated(true);
            userRepository.save(user);
            return true;
        }
        return false;
    }

    public void changeInfo(long userId, UserChangeInfoDTO dto) {
        User user = getUserById(userId);
        // Update the user's information
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            // Check if the new username is already taken by another user
            String newUsername = dto.getUsername();
            if (userRepository.existsByName(dto.getUsername())) {
                throw new UserAlreadyExistsException("Username is already taken.");
            }
            user.setUsername(newUsername);
        }
        if (dto.getName() != null) {
            user.setName(dto.getName());
        }
        if (dto.getBio() != null) {
            user.setBio(dto.getBio());
        }

        userRepository.save(user);
    }

    @Transactional
    @SneakyThrows
    public UserBasicInfoDTO updateProfilePicture(long userId, MultipartFile file) {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!Arrays.asList("jpg", "jpeg", "png").contains(ext)) {
            throw new BadRequestException("File type not supported. Only JPG, JPEG and PNG formats are allowed.");
        }
        String name = UUID.randomUUID().toString() + "." + ext;
        File dir = new File("uploads_user_profile_picture");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File oldFile = null;
        User user = getUserById(userId);
        if (user.getProfilePictureUrl() != null) {
            oldFile = new File(user.getProfilePictureUrl());
            if (oldFile.exists()) {
                oldFile.delete();
            }
        }
        File newFile = new File(dir, name);
        Files.copy(file.getInputStream(), newFile.toPath());
        String url = dir.getName() + File.separator + newFile.getName();
        user.setProfilePictureUrl(url);
        userRepository.save(user);
        if (oldFile != null) {
            oldFile.delete();
        }
        return mapper.map(user, UserBasicInfoDTO.class);
    }


    public File download(String fileName) {
        File dir = new File("uploads_user_profile_picture");
        File f = new File(dir, fileName);
        if (f.exists()) {
            return f;
        }
        throw new NotFoundException("File not found");
    }

    public void forgotPassword(UserEmailDTO dto) {
        User user = getUserByEmail(dto.getEmail());
        String identifier = UUID.randomUUID().toString();
        user.setResetIdentifier(identifier);
        user.setResetIdentifierExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        // Send email to user with reset link
        String subject = "Email verification";
        String resetLink = "https://localhost:8080/password/reset?id=" + identifier;
        String text = "Click the link below to reset your password: \n" + resetLink;
        sendEmail(user.getEmail(), subject, text);
    }

    public void resetPassword(String identifier) {
        User user = userRepository.findByResetIdentifier(identifier)
                .orElseThrow(() -> new NotFoundException("Invalid identifier"));
        if (user.getResetIdentifierExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }
        String generatePassword = generateRandomPassword();
        user.setPassword(encoder.encode(generatePassword));
        user.setResetIdentifier(null);
        user.setResetIdentifierExpiry(null);
        userRepository.save(user);
        String subject = "New password";
        String text = "This is your new password: \n" + generatePassword;
        sendEmail(user.getEmail(), subject, text);
    }

    public void sendEmail(String to, String subject, String text) {
        new Thread(() -> {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            javaMailSender.send(message);
        }).start();
    }

    public String generateRandomPassword() {
        int passwordLength = (int) (Math.random() * 3) + 8; // random password length between 8 and 10 characters
        String allCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&?";
        return RandomStringUtils.random(passwordLength, allCharacters);
    }

    public void sendVerificationEmail(String email) {
        User user = getUserByEmail(email);
        String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        // Send verification email to user
        String subject = "Email verification";
        String verificationLink = "https://localhost:8080/users/email?verification-token=" + verificationCode;
        String text = "Click the link below to verify your email address: \n" + verificationLink;
        sendEmail(user.getEmail(), subject, text);
    }

    public void verifyEmail(String verificationToken) {
        User user = userRepository.findByVerificationCode(verificationToken)
                .orElseThrow(() -> new NotFoundException("Invalid verification token"));
        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code has expired");
        }
        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);
    }

    public Slice<UserBasicInfoDTO> getFollowers(long id, Pageable pageable) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        return userRepository.findAllFollowersOrderByDateOfFollowDesc(user.getId(), pageable)
                .map(u -> mapToUserBasicInfoDTO(u));
    }

    private UserBasicInfoDTO mapToUserBasicInfoDTO(User user) {
        UserBasicInfoDTO dto = mapper.map(user, UserBasicInfoDTO.class);
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        return dto;
    }

    public Slice<UserBasicInfoDTO> getFollowing(long id, Pageable pageable) {
        return userRepository.findAllFollowedOrderByDateOfFollowDesc(id, pageable)
                .map(u -> mapToUserBasicInfoDTO(u));
    }

    public UserWithoutPassAndEmailDTO getUserWithoutPassAndEmailDTO(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        int numFollowers = user.getFollowers().size();
        int numFollowing = user.getFollowing().size();
        int numPosts = user.getPosts().size();

        return new UserWithoutPassAndEmailDTO(user.getId(), user.getName(), user.getUsername(), user.getProfilePictureUrl(), numFollowers, numFollowing, numPosts);
    }

    public List<UserBasicInfoDTO> getSearchResult(String username, long userId) {
        List<User> list = userRepository.findAllUsersOrderById(username, userId);
        List<UserBasicInfoDTO> result = new ArrayList<>();
        for (User user : list){
            UserBasicInfoDTO dto = mapper.map(user, UserBasicInfoDTO.class);
            result.add(dto);
        }
        if (result.size() < 1){
            throw new NotFoundException("No users found");
        }
        return result;
    }
}
