package bg.ittalents.instagram.user;

import bg.ittalents.instagram.comment.DTOs.PageRequestDTO;
import bg.ittalents.instagram.exception.UnauthorizedException;
import bg.ittalents.instagram.user.DTOs.*;
import bg.ittalents.instagram.util.AbstractController;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.SneakyThrows;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController extends AbstractController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET localhost:8080/users/email?verification-token=QWERTY123456
    @PutMapping("/send/verification")
    public void sendVerificationEmail(
            @RequestParam("email")
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format")
            String email) {
        userService.sendVerificationEmail(email);
    }

    @GetMapping("/verify/email")
    public ResponseEntity<String> verifyEmail(
            @RequestParam("verification-token")
            @NotBlank(message = "Provide verification token!")
            String verificationToken) {
        userService.verifyEmail(verificationToken);
        return ResponseEntity.ok("Email verification successful");
    }


    // GET localhost:8080/users/2
    @GetMapping("/{id}")
    public UserWithoutPassAndEmailDTO getUserById(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id, HttpSession session) {
        getLoggedId(session);
        return userService.getById(id);
    }


    // GET localhost:8080/users/1/followers
    @GetMapping("/{id}/followers")
    public ResponseEntity<Slice<UserBasicInfoDTO>> getFollowers(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            @ModelAttribute PageRequestDTO pageRequestDTO,
            HttpSession session) {
        getLoggedId(session);
        Slice<UserBasicInfoDTO> userBasicInfoDTOsList = userService.getFollowers(
                id,
                PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize()));

        return ResponseEntity.ok(userBasicInfoDTOsList);
    }

    //    // GET localhost:8080/users/1/following
    @GetMapping("/{id}/following")
    public ResponseEntity<Slice<UserBasicInfoDTO>> getFollowing(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            @ModelAttribute PageRequestDTO pageRequestDTO,
            HttpSession session) {
        getLoggedId(session);
        Slice<UserBasicInfoDTO> userBasicInfoDTOsList = userService.getFollowing(
                id, PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize()));
        return ResponseEntity.ok(userBasicInfoDTOsList);
    }

    @GetMapping("/search")
    public List<UserBasicInfoDTO> searchUsersByUsername(
            @RequestParam
            @NotBlank(message = "Provide search string!")
            String username, HttpSession session) {
        getLoggedId(session);
        return userService.searchUsersByUsername(username);
    }

    // POST localhost:8080/users
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody @Valid RegisterDTO dto, HttpSession session) {
        if (session.getAttribute("LOGGED_ID") != null) {
            throw new UnauthorizedException("You can't register while logged into an account");
        }
        userService.create(dto);
        return ResponseEntity.ok("Account successfully created, we've sent a verification link to your email");
    }

    // POST localhost:8080/users/login
    @PostMapping("/login")
    public UserWithoutPassAndEmailDTO login(@RequestBody @Valid UserLoginDTO dto, HttpSession session) {
        //Please keep in mind that the login method does the mapping.
        if (session.getAttribute("LOGGED_ID") != null) {
            throw new UnauthorizedException("You're already logged into an account");
        }
        UserWithoutPassAndEmailDTO respDto = userService.login(dto);
        session.setAttribute("LOGGED_ID", respDto.getId());
        return respDto;
    }

    // POST localhost:8080/users/logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(HttpSession session) {
        session.invalidate();
        return ResponseEntity.ok("Logout successful");
    }


    //PUT localhost:8080/users/password/forgot
    @PutMapping("/password/forgot")
    public ResponseEntity<String> forgotPassword(@RequestBody @Valid UserEmailDTO dto) {
        userService.forgotPassword(dto);
        return ResponseEntity.ok("A password reset link has been sent to your email");
    }

    @GetMapping("/password/reset")
    public ResponseEntity<String> resetPassword(
            @RequestParam("id")
            @NotBlank(message = "Provide identifier!")
            String identifier) {
        userService.resetPassword(identifier);
        return ResponseEntity.ok("Your new password has been sent to your email");
    }

    // POST localhost:8080/users/2/block
    @PostMapping("/{id}/block")
    public ResponseEntity<Void> blockUser(
            @PathVariable("id")
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long blockedId, HttpSession session) {
        long blockingUserId = getLoggedId(session);
        userService.block(blockingUserId, blockedId);
        return ResponseEntity.ok().build();
    }

    //POST localhost:8080/users/2/follow
    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> followUser(
            @PathVariable("id")
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long followedId,
            HttpSession session) {
        long followerId = getLoggedId(session);
        userService.follow(followerId, followedId);
        return ResponseEntity.ok().build();
    }

    //     PUT localhost:8080/users/picture
    @PutMapping("/picture")
    public UserBasicInfoDTO uploadProfilePicture(
            @RequestParam MultipartFile file,
            HttpSession session) {
        long userId = getLoggedId(session);
        return userService.updateProfilePicture(userId, file);
    }

    @GetMapping("/picture/{fileName}")
    @SneakyThrows
    public ResponseEntity<Void> download(
            @PathVariable
            @NotBlank(message = "Provide file name!")
            String fileName,
            HttpServletResponse response, HttpSession session) {
        getLoggedId(session);
        File file = userService.download(fileName);
        response.setContentType("image/jpeg");
        Files.copy(file.toPath(), response.getOutputStream());
        return ResponseEntity.ok().build();
    }

    // PUT localhost:8080/users/password
    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestBody @Valid UserChangePasswordDTO dto, HttpSession session) {
        long userId = getLoggedId(session);
        userService.changePassword(userId, dto);
        return ResponseEntity.ok("Password changed");
    }

    // PUT localhost:8080/users/info
    @PutMapping("/info")
    public ResponseEntity<String> updateUserInfo(@RequestBody @Valid UserChangeInfoDTO dto, HttpSession session) {
        long userId = getLoggedId(session);
        userService.changeInfo(userId, dto);
        return ResponseEntity.ok("Info changed!");
    }

    // PUT localhost:8080/users/deactivate
    @PutMapping("/deactivate")
    public ResponseEntity<String> deactivateUser(@RequestBody @Valid UserPasswordDTO dto, HttpSession session) {
        long userId = getLoggedId(session);
        boolean success = userService.deactivateUser(userId, dto);
        if (success) {
            session.invalidate();
            return ResponseEntity.ok("User deactivated successfully");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Incorrect password");
        }
    }

    // DELETE localhost:8080/users
    @DeleteMapping
    public ResponseEntity<String> deleteUser(@RequestBody @Valid UserPasswordDTO dto, HttpSession session) {
        long userId = getLoggedId(session);
        userService.deleteUserById(userId, dto);
        session.invalidate();
        return ResponseEntity.ok("Account successfully deleted");
    }
}
