package bg.ittalents.instagram.user;

import bg.ittalents.instagram.exceptions.BadRequestException;
import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.exceptions.UserAlreadyExistsException;
import bg.ittalents.instagram.follower.Follow;
import bg.ittalents.instagram.follower.FollowKey;
import bg.ittalents.instagram.user.DTOs.*;
import bg.ittalents.instagram.util.AbstractService;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Random;
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
        User user = getUserByEmail(dto.getEmail());
        //Check if password matches with the one in the database
        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Wrong credentials");
        }
        if (user.isDeactivated()) {
            user.setDeactivated(false);
        }
        return mapper.map(user, UserWithoutPassAndEmailDTO.class);
    }

    public UserWithoutPassAndEmailDTO getById(long id) {
        User user = getUserById(id);
        return mapper.map(user, UserWithoutPassAndEmailDTO.class);
    }

    //This method should be ready to go!!!
    public ResponseEntity<String> block(long blockingUserId, long blockedUserId) {
        User blocker = getUserById(blockingUserId);
        User blocked = getUserById(blockedUserId);
        //Check if user is trying to block himself
        if (blocker.getId() == blocked.getId()) {
            throw new RuntimeException("You can't block yourself");
        }
        //Check if user is trying to block someone that's already blocked
        if (blocker.getBlocked().contains(blocked)) {
            throw new RuntimeException("You have already blocked this user");
        }
        blocker.getBlocked().add(blocked);
        userRepository.save(blocker);
        return ResponseEntity.ok(blocked.getName() + " was successfully blocked");
    }

    private String generateVerificationCode() {
        UUID uuid = UUID.randomUUID();
        String code = uuid.toString().substring(0, 6);
        return code;
    }

    //TODO
    @Transactional
    public void follow(long followerId, long followedId) {
        User follower = getUserById(followerId);
        User followed = getUserById(followedId);
        if (follower == null || followed == null) {
            throw new NotFoundException("User not found");
        }
        FollowKey followKey = new FollowKey(followed.getId(), follower.getId());
        if (followerId == followedId) {
            throw new BadRequestException("You can't follow yourself");
        }
        if (follower.getFollowing().contains(followed)) {
            followRepository.deleteById(followKey);
        }
        // create and save the Follower entity
        Follow followEntity = new Follow();
        followEntity.setId(followKey);
        followEntity.setFollowingUser(follower);
        followEntity.setFollowedUser(followed);
        followEntity.setDateTimeOfFollow(Timestamp.valueOf(LocalDateTime.now()));
        followRepository.save(followEntity);
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

    public ResponseEntity<String> changePassword(long userId, UserChangePasswordDTO dto) {
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
        return ResponseEntity.ok("Password changed");
    }

    public boolean containsIgnoreCase(String str, String subStr) {
        return str.toLowerCase().contains(subStr.toLowerCase());
    }

    public ResponseEntity<String> deleteUserById(long userId, UserPasswordDTO dto) {
        User user = getUserById(userId);
        if (encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadRequestException("Wrong credentials");
        }
        userRepository.delete(user);
        return ResponseEntity.ok("Account successfully deleted");
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

    public ResponseEntity<String> changeInfo(long userId, UserChangeInfoDTO dto) {
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
        return ResponseEntity.ok("Info changed!");
    }

    @SneakyThrows
    public UserBasicInfoDTO updateProfilePicture(long userId, MultipartFile file) {
        String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        String name = UUID.randomUUID().toString() + "." + ext;
        File dir = new File("pfpUploads");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File f = new File(dir, name);
        Files.copy(file.getInputStream(), f.toPath());
        String url = dir.getName() + File.separator + f.getName();
        User user = getUserById(userId);
        user.setProfilePictureUrl(url);
        userRepository.save(user);
        return mapper.map(user, UserBasicInfoDTO.class);
    }

    public File download(String fileName) {
        File dir = new File("pfpUploads");
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
        String subject = "Reset your password";
        String resetLink = "https://localhost:8080/reset/password?id=" + identifier;
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

    private void sendEmail(String to, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    public String generateRandomPassword() {
        int passwordLength = (int)(Math.random() * 3) + 8; // random password length between 8 and 10 characters
        String uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
        String numbers = "0123456789";
        String symbols = "!@#$%^&?";

        String password = "";
        String allCharacters = uppercaseLetters + lowercaseLetters + numbers + symbols;

        Random rand = new Random();

        for(int i = 0; i < passwordLength; i++) {
            password += allCharacters.charAt(rand.nextInt(allCharacters.length()));
        }

        return password;
    }
}
