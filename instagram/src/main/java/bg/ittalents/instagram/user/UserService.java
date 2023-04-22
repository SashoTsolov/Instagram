package bg.ittalents.instagram.user;

import bg.ittalents.instagram.exception.BadRequestException;
import bg.ittalents.instagram.exception.NotFoundException;
import bg.ittalents.instagram.exception.UnauthorizedException;
import bg.ittalents.instagram.exception.UserAlreadyExistsException;
import bg.ittalents.instagram.follower.Follow;
import bg.ittalents.instagram.follower.FollowKey;
import bg.ittalents.instagram.follower.FollowRepository;
import bg.ittalents.instagram.user.DTOs.*;
import bg.ittalents.instagram.util.AbstractService;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
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

    private final BCryptPasswordEncoder encoder;
    private final FollowRepository followRepository;

    public UserService(UserRepository userRepository,
                       JavaMailSender javaMailSender,
                       ModelMapper mapper,
                       BCryptPasswordEncoder encoder,
                       FollowRepository followRepository) {
        super(userRepository, javaMailSender, mapper);
        this.encoder = encoder;
        this.followRepository = followRepository;
    }

    public void create(final RegisterDTO dto) {
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
        final User user = mapper.map(dto, User.class);
        user.setPassword(encoder.encode(user.getPassword()));
        user.setVerified(false);
        user.setDeactivated(false);
        user.setVerificationCode(generateVerificationCode());
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));
        user.setDateTimeCreated(Timestamp.valueOf(LocalDateTime.now()));
        userRepository.save(user);

        // Send verification email to user
        final String subject = "Account Verification";
        final String verificationLink = "https://localhost:8080/users/verify?verification-token="
                + user.getVerificationCode();
        final String text = "Click the link below to verify your account: \n" + verificationLink;
        sendEmail(user.getEmail(), subject, text);
    }

    public UserWithoutPassAndEmailDTO login(final UserLoginDTO dto) {
        final User user = getUserByEmail(dto.getEmail());
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

    public UserWithoutPassAndEmailDTO getById(final long id) {
        final User user = getUserById(id);
        return getUserWithoutPassAndEmailDTO(user.getId());
    }

    //This method should be ready to go!!!
    @Transactional
    public void block(final long blockingUserId, final long blockedUserId) {
        final User blocker = getUserById(blockingUserId);
        final User blocked = getUserById(blockedUserId);
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
        if (blocker.getFollowing().contains(blocked)) {
            blocker.getFollowing().remove(blocked);
            blocked.getFollowers().remove(blocker);
        }
        userRepository.save(blocker);
        userRepository.save(blocked);
    }

    private String generateVerificationCode() {
        final UUID uuid = UUID.randomUUID();
        final String code = uuid.toString().substring(0, 12);
        return code;
    }

    //TODO
    @Transactional
    public void follow(final long followerId, final long followedId) {
        final User follower = getUserById(followerId);
        final User followed = getUserById(followedId);
        if (follower == null || followed == null) {
            throw new NotFoundException("User not found");
        }
        final FollowKey followKey = new FollowKey(followed.getId(), follower.getId());
        if (followerId == followedId) {
            throw new BadRequestException("You can't follow yourself");
        }
        if (follower.getFollowing().contains(followed)) {
            followRepository.deleteById(followKey);
        }
        // create and save the Follower entity
        final Follow followEntity = new Follow();
        followEntity.setId(followKey);
        followEntity.setFollowingUser(follower);
        followEntity.setFollowedUser(followed);
        followEntity.setDateTimeOfFollow(Timestamp.valueOf(LocalDateTime.now()));
        followRepository.save(followEntity);
    }

    //TODO -- make this into a query and filter by blocked/blockedBy/deactivated
    public List<UserBasicInfoDTO> searchUsersByUsername(final String username) {
        final List<User> users = userRepository.findAll();
        final List<User> filteredUsers = users.stream()
                .filter(user -> containsIgnoreCase(user.getUsername(), username))
                .filter(user -> !user.isDeactivated())
                .limit(55)
                .toList();
        System.out.println(filteredUsers.size());
        return filteredUsers.stream()
                .map(user -> mapper.map(user, UserBasicInfoDTO.class)).collect(Collectors.toList());
    }

    public void changePassword(final long userId, final UserChangePasswordDTO dto) {
        final User user = getUserById(userId);
        if (!encoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is invalid");
        }
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new BadRequestException("Confirm password does not match");
        }

        final String newPassword = encoder.encode(dto.getNewPassword());
        user.setPassword(newPassword);

        userRepository.save(user);
    }

    public boolean containsIgnoreCase(final String str, final String subStr) {
        return str.toLowerCase().contains(subStr.toLowerCase());
    }

    public void deleteUserById(final long userId, final UserPasswordDTO dto) {
        final User user = getUserById(userId);
        if (encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadRequestException("Wrong credentials");
        }
        userRepository.delete(user);
    }

    public boolean deactivateUser(final long userId, final UserPasswordDTO dto) {
        final User user = getUserById(userId);
        if (encoder.matches(dto.getPassword(), user.getPassword())) {
            user.setDeactivated(true);
            logout(userId);
            return true;
        }
        return false;
    }

    public void changeInfo(final long userId, final UserChangeInfoDTO dto) {
        final User user = getUserById(userId);
        // Update the user's information
        if (dto.getUsername() != null && !dto.getUsername().isBlank()) {
            // Check if the new username is already taken by another user
            final String newUsername = dto.getUsername();
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

    @SneakyThrows
    public UserBasicInfoDTO updateProfilePicture(final long userId, final MultipartFile file) {
        final String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        final String name = UUID.randomUUID().toString() + "." + ext;
        final File dir = new File("uploads_user_profile_picture");
        if (!dir.exists()) {
            dir.mkdirs();
        }
        final File f = new File(dir, name);
        Files.copy(file.getInputStream(), f.toPath());
        final String url = dir.getName() + File.separator + f.getName();
        final User user = getUserById(userId);
        user.setProfilePictureUrl(url);
        userRepository.save(user);
        return mapper.map(user, UserBasicInfoDTO.class);
    }

    public File download(final String fileName) {
        final File dir = new File("uploads_user_profile_picture");
        final File f = new File(dir, fileName);
        if (f.exists()) {
            return f;
        }
        throw new NotFoundException("File not found");
    }

    public void forgotPassword(final UserEmailDTO dto) {
        final User user = getUserByEmail(dto.getEmail());
        final String identifier = UUID.randomUUID().toString();
        user.setResetIdentifier(identifier);
        user.setResetIdentifierExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        // Send email to user with reset link
        final String subject = "Email verification";
        final String resetLink = "https://localhost:8080/password/reset?id=" + identifier;
        final String text = "Click the link below to reset your password: \n" + resetLink;
        sendEmail(user.getEmail(), subject, text);
    }

    public void resetPassword(final String identifier) {
        final User user = userRepository.findByResetIdentifier(identifier)
                .orElseThrow(() -> new NotFoundException("Invalid identifier"));
        if (user.getResetIdentifierExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }
        final String generatePassword = generateRandomPassword();
        user.setPassword(encoder.encode(generatePassword));
        user.setResetIdentifier(null);
        user.setResetIdentifierExpiry(null);
        userRepository.save(user);
        final String subject = "New password";
        final String text = "This is your new password: \n" + generatePassword;
        sendEmail(user.getEmail(), subject, text);
    }

    private void sendEmail(String to, String subject, String text) {
        final SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    public String generateRandomPassword() {
        final int passwordLength = (int) (Math.random() * 3) + 8; // random password length between 8 and 10 characters
        final String allCharacters = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&?";
        return RandomStringUtils.random(passwordLength, allCharacters);
    }

    public void sendVerificationEmail(String email) {
        final User user = getUserByEmail(email);
        final String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        // Send verification email to user
        final String subject = "Email verification";
        final String verificationLink = "https://localhost:8080/users/email?verification-token=" + verificationCode;
        final String text = "Click the link below to verify your email address: \n" + verificationLink;
        sendEmail(user.getEmail(), subject, text);
    }

    public void verifyEmail(String verificationToken) {
        final User user = userRepository.findByVerificationCode(verificationToken)
                .orElseThrow(() -> new NotFoundException("Invalid verification token"));
        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification code has expired");
        }
        user.setVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);
    }

    public Slice<UserBasicInfoDTO> getFollowers(final long id, final Pageable pageable) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        return userRepository.findAllFollowersOrderByDateOfFollowDesc(user.getId(), pageable)
                .map(u -> mapToUserBasicInfoDTO(u));
    }

    private UserBasicInfoDTO mapToUserBasicInfoDTO(final User user) {
        final UserBasicInfoDTO dto = mapper.map(user, UserBasicInfoDTO.class);
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setName(user.getName());
        dto.setProfilePictureUrl(user.getProfilePictureUrl());
        return dto;
    }

    public Slice<UserBasicInfoDTO> getFollowing(final long id, final Pageable pageable) {
        final User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        return userRepository.findAllFollowedOrderByDateOfFollowDesc(user.getId(), pageable)
                .map(u -> mapToUserBasicInfoDTO(u));
    }

    @Transactional
    public UserWithoutPassAndEmailDTO getUserWithoutPassAndEmailDTO(final long userId) {
        final User user = getUserById(userId);
        final int numFollowers = user.getFollowers().size();
        final int numFollowing = user.getFollowing().size();
        final int numPosts = user.getPosts().size();

        return new UserWithoutPassAndEmailDTO(user.getId(), user.getName(),
                user.getUsername(), user.getProfilePictureUrl(), numFollowers, numFollowing, numPosts);
    }

    public void logout(final long userId) {
        final User user = getUserById(userId);
        user.setLastLogoutTime(Timestamp.valueOf(LocalDateTime.now()));
        userRepository.save(user);
    }
}
