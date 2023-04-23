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
import com.amazonaws.services.s3.AmazonS3;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class UserService extends AbstractService {

    private final BCryptPasswordEncoder encoder;
    private final FollowRepository followRepository;

    public UserService(final UserRepository userRepository,
                       final JavaMailSender javaMailSender,
                       final ModelMapper mapper,
                       final AmazonS3 s3Client,
                       final @Value("${aws.s3.bucket}") String bucketName,
                       final BCryptPasswordEncoder encoder,
                       final FollowRepository followRepository) {
        super(userRepository, javaMailSender, mapper, s3Client, bucketName);
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
        user.setCheckedForInactivity(false);
        userRepository.save(user);

        // Send verification email to user
        final String subject = "Account Verification";
        final String verificationLink = "https://localhost:8080/users/verify?verification-token="
                + user.getVerificationCode();
        final String text = "Click the link below to verify your account: \n" + verificationLink;
        new Thread(() -> sendEmail(user.getEmail(), subject, text)).start();
    }

    public UserWithoutPassAndEmailDTO login(final UserLoginDTO dto) {
        final User user = getUserByEmail(dto.getEmail());
        //Check if password matches with the one in the database
        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("Wrong credentials");
        }
        user.setCheckedForInactivity(false);
        user.setLastBeenOnline(null);
//        if (!user.isVerified()) {
//            throw new BadRequestException("Account is not verified yet");
//        }
        if (user.isDeactivated()) {
            user.setDeactivated(false);
        }
        userRepository.save(user);
        return getUserWithoutPassAndEmailDTO(user.getId());
    }

    public UserWithoutPassAndEmailDTO getById(final long id) {
        final User user = getUserById(id);
        if (user.isDeactivated()) {
            throw new NotFoundException("User not found");
        }
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
        return uuid.toString().substring(0, 12);
    }

    @Transactional
    public String follow(final long followerId, final long followedId) {
        final User follower = getUserById(followerId);
        final User followed = getUserById(followedId);

        if (follower == null || followed == null) {
            throw new NotFoundException("User not found");
        }
        final FollowKey followKey = new FollowKey(followed.getId(), follower.getId());
        if (followerId == followedId) {
            throw new BadRequestException("You can't follow yourself");
        }
        if (follower.getBlocked().contains(followed) || follower.getBlockedBy().contains(followed)) {
            throw new BadRequestException("Unable to follow user");
        }
        if (follower.getFollowing().contains(followed)) {
            followRepository.deleteById(followKey);
            userRepository.save(follower);
            userRepository.save(followed);
            return followed.getUsername() + " unfollowed";
        }
        // create and save the Follower entity
        final Follow followEntity = new Follow();
        followEntity.setId(followKey);
        followEntity.setFollowingUser(follower);
        followEntity.setFollowedUser(followed);
        followEntity.setDateTimeOfFollow(Timestamp.valueOf(LocalDateTime.now()));
        followRepository.save(followEntity);
        return followed.getUsername() + " followed";
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

    public void deleteUserById(final long userId, final UserPasswordDTO dto) {
        final User user = getUserById(userId);
        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadRequestException("Wrong credentials");
        }
        userRepository.delete(user);
    }

    public String deactivateUser(final long userId, final UserPasswordDTO dto) {
        final User user = getUserById(userId);
        if (!encoder.matches(dto.getPassword(), user.getPassword())) {
            throw new BadRequestException("Wrong credentials");
        }
        user.setDeactivated(true);
        logout(userId);
        return "Deactivation successful";
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

    @Transactional
    @SneakyThrows
    public UserBasicInfoDTO updateProfilePicture(final long userId, final MultipartFile file) {
        final String ext = FilenameUtils.getExtension(file.getOriginalFilename());
        if (!Arrays.asList("jpg", "jpeg", "png").contains(ext)) {
            throw new BadRequestException("File type not supported. Only JPG, JPEG and PNG formats are allowed.");
        }

        final String newUrl = uploadMedia(file, ext);
        final User user = getUserById(userId);
        final String oldUrl = user.getProfilePictureUrl();
        user.setProfilePictureUrl(newUrl);
        userRepository.save(user);

        // Delete old file from S3 if it exists
        if (oldUrl != null) {
            final String oldFileName = oldUrl.substring(oldUrl.lastIndexOf('/') + 1);
            s3Client.deleteObject(bucketName, oldFileName);
        }

        return mapper.map(user, UserBasicInfoDTO.class);
    }


    public void forgotPassword(final UserEmailDTO dto) {
        final User user = getUserByEmail(dto.getEmail());
        final String identifier = UUID.randomUUID().toString();
        user.setResetIdentifier(identifier);
        user.setResetIdentifierExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        // Send email to user with reset link
        final String subject = "Forgot password";
        final String resetLink = "https://localhost:8080/password/reset?id=" + identifier;
        final String text = "Click the link below to reset your password: \n" + resetLink;
        new Thread(() -> sendEmail(user.getEmail(), subject, text)).start();
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
        new Thread(() -> sendEmail(user.getEmail(), subject, text)).start();
    }

    public void sendEmail(final String to, final String subject, final String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(subject);
        message.setText(text);
        javaMailSender.send(message);
    }

    public String generateRandomPassword() {
        final int passwordLength = (int) (Math.random() * 3) + 8; // random password length between 8 and 10 characters
        final String uppercaseLetters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        final String lowercaseLetters = "abcdefghijklmnopqrstuvwxyz";
        final String numbers = "0123456789";
        final String symbols = "!@#$%^&?";

        String password = "";
        final String allCharacters = uppercaseLetters + lowercaseLetters + numbers + symbols;

        final Random rand = new Random();

        for (int i = 0; i < passwordLength; i++) {
            password += allCharacters.charAt(rand.nextInt(allCharacters.length()));
        }
        return password;
    }

    public void sendVerificationEmail(final String email) {
        final User user = getUserByEmail(email);
        final String verificationCode = generateVerificationCode();
        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);
        // Send verification email to user
        final String subject = "Email verification";
        final String verificationLink = "https://localhost:8080/users/email?verification-token=" + verificationCode;
        final String text = "Click the link below to verify your email address: \n" + verificationLink;
        new Thread(() -> sendEmail(user.getEmail(), subject, text)).start();
    }

    public void verifyEmail(final String verificationToken) {
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
        final User user = getUserById(id);
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
        final User user = getUserById(id);
        return userRepository.findAllFollowedOrderByDateOfFollowDesc(user.getId(), pageable)
                .map(u -> mapToUserBasicInfoDTO(u));
    }

    @Transactional
    public UserWithoutPassAndEmailDTO getUserWithoutPassAndEmailDTO(final long userId) {
        final User user = getUserById(userId);
        final int numFollowers = userRepository.countFollowersByUserIdAndDeactivatedFalse(userId);
        final int numFollowing = userRepository.countFollowingByUserIdAndDeactivatedFalse(userId);
        final int numPosts = user.getPosts().size();

        return new UserWithoutPassAndEmailDTO(user.getId(), user.getName(),
                user.getUsername(), user.getProfilePictureUrl(), numFollowers, numFollowing, numPosts, user.getBio());
    }

    public void logout(final long userId) {
        final User user = getUserById(userId);
        user.setLastBeenOnline(Timestamp.valueOf(LocalDateTime.now()));
        userRepository.save(user);
    }

    public List<UserBasicInfoDTO> getSearchResult(final String username, final long userId) {
        final List<User> list = userRepository.findAllUsersOrderById(username, userId);
        final List<UserBasicInfoDTO> result = new ArrayList<>();
        for (User user : list) {
            UserBasicInfoDTO dto = mapper.map(user, UserBasicInfoDTO.class);
            result.add(dto);
        }
        if (result.size() < 1) {
            throw new NotFoundException("No users found");
        }
        return result;
    }

    public void deleteProfilePicture(final long userId) {
        final User user = getUserById(userId);
        final String profilePictureUrl = user.getProfilePictureUrl();
        if (profilePictureUrl != null) {
            final String objectKey = profilePictureUrl.substring(profilePictureUrl.lastIndexOf("/") + 1);
            user.setProfilePictureUrl(null);
            userRepository.save(user);
            s3Client.deleteObject(bucketName, objectKey);
        } else {
            throw new NotFoundException("No profile picture to delete!");
        }
    }
}
