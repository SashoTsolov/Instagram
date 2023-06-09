package bg.ittalents.instagram.user;

import bg.ittalents.instagram.comment.DTOs.PageRequestDTO;
import bg.ittalents.instagram.exception.UnauthorizedException;
import bg.ittalents.instagram.user.DTOs.*;
import bg.ittalents.instagram.util.AbstractController;
import com.amazonaws.util.IOUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.SneakyThrows;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController extends AbstractController {

    private final UserService userService;

    public UserController(HttpServletRequest request,
                          HttpSession session,
                          UserService userService) {
        super(request, session);
        this.userService = userService;
    }

    // GET localhost:8080/users/email?verification-token=QWERTY123456
    @PutMapping("/send/verification")
    public ResponseEntity<Void> sendVerificationEmail(
            @RequestParam("email")
            @NotBlank(message = "Email is required")
            @Email(message = "Invalid email format") final String email) {
        userService.sendVerificationEmail(email);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/verify/email")
    public ResponseEntity<String> verifyEmail(
            @RequestParam("verification-token")
            @NotBlank(message = "Provide verification token!") final String verificationToken) {
        userService.verifyEmail(verificationToken);
        return ResponseEntity.ok("Email verification successful");
    }


    // GET localhost:8080/users/2
    @GetMapping("/{id}")
    public ResponseEntity<UserWithoutPassAndEmailDTO> getUserById(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1") final long id) {
        return ResponseEntity.ok(userService.getById(getLoggedId(), id));
    }


    // GET localhost:8080/users/1/followers
    @GetMapping("/{id}/followers")
    public ResponseEntity<Slice<UserBasicInfoDTO>> getFollowers(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1") final long id,
            @ModelAttribute final PageRequestDTO pageRequestDTO) {

        final Slice<UserBasicInfoDTO> userBasicInfoDTOsList = userService.getFollowers(getLoggedId(),
                id,
                PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize()));

        return ResponseEntity.ok(userBasicInfoDTOsList);
    }

    //    // GET localhost:8080/users/1/following
    @GetMapping("/{id}/following")
    public ResponseEntity<Slice<UserBasicInfoDTO>> getFollowing(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1") final long id,
            @ModelAttribute final PageRequestDTO pageRequestDTO) {
        final Slice<UserBasicInfoDTO> userBasicInfoDTOsList = userService.getFollowing(getLoggedId(),
                id, PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize()));
        return ResponseEntity.ok(userBasicInfoDTOsList);
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserBasicInfoDTO>> searchUsersByUsername(
            @RequestParam
            @NotBlank(message = "Provide search string!") final String username) {
        final long userId = getLoggedId();
        final List<UserBasicInfoDTO> userBasicInfoDTOList = userService.getSearchResult(username, userId);
        return ResponseEntity.ok(userBasicInfoDTOList);
    }

    // POST localhost:8080/users
    @PostMapping
    public ResponseEntity<String> createUser(@RequestBody @Valid final RegisterDTO dto, final HttpSession session) {
        if (session.getAttribute("LOGGED_ID") != null) {
            throw new UnauthorizedException("You can't register while logged into an account");
        }
        userService.create(dto);
        return ResponseEntity.ok("Account successfully created, we've sent a verification link to your email");
    }

    // POST localhost:8080/users/login
    @PostMapping("/login")
    public ResponseEntity<UserWithoutPassAndEmailDTO> login(@RequestBody @Valid final UserLoginDTO dto,
                                                            final HttpSession session) {
        //Please keep in mind that the login method does the mapping.
        if (session.getAttribute("LOGGED_ID") != null) {
            throw new UnauthorizedException("You're already logged into an account");
        }
        final UserWithoutPassAndEmailDTO respDto = userService.login(dto);
        session.setAttribute("LOGGED_ID", respDto.getId());
        session.setAttribute("IP", request.getRemoteAddr());
        return ResponseEntity.ok(respDto);
    }

    // POST localhost:8080/users/logout
    @PostMapping("/logout")
    public ResponseEntity<String> logout(final HttpSession session) {
        userService.logout(getLoggedId());
        session.invalidate();
        return ResponseEntity.ok("Logout successful");
    }


    //PUT localhost:8080/users/password/forgot
    @PutMapping("/password/forgot")
    public ResponseEntity<String> forgotPassword(@RequestBody @Valid final UserEmailDTO dto) {
        userService.forgotPassword(dto);
        return ResponseEntity.ok("A password reset link has been sent to your email");
    }

    @GetMapping("/password/reset")
    public ResponseEntity<String> resetPassword(
            @RequestParam("id")
            @NotBlank(message = "Provide identifier!") final String identifier) {
        userService.resetPassword(identifier);
        return ResponseEntity.ok("Your new password has been sent to your email");
    }

    // POST localhost:8080/users/2/block
    @PostMapping("/{id}/block")
    public ResponseEntity<String> blockUser(
            @PathVariable("id")
            @Min(value = 1, message = "ID must be greater than or equal to 1") final long blockedId) {
        final long blockingUserId = getLoggedId();
        String message = userService.block(blockingUserId, blockedId);
        return ResponseEntity.ok(message);
    }

    //POST localhost:8080/users/2/follow
    @PostMapping("/{id}/follow")
    public ResponseEntity<String> followUser(
            @PathVariable("id")
            @Min(value = 1, message = "ID must be greater than or equal to 1") final long followedId) {
        final long followerId = getLoggedId();
        final String response = userService.follow(followerId, followedId);
        return ResponseEntity.ok(response);
    }

    //     PUT localhost:8080/users/picture
    @PutMapping("/picture")
    public ResponseEntity<UserBasicInfoDTO> uploadProfilePicture(
            @RequestParam final MultipartFile file) {
        final long userId = getLoggedId();
        final UserBasicInfoDTO userBasicInfoDTO = userService.updateProfilePicture(userId, file);
        return ResponseEntity.ok(userBasicInfoDTO);
    }

    //     PUT localhost:8080/users/picture
    @DeleteMapping("/picture")
    public ResponseEntity<String> deleteProfilePicture() {
        userService.deleteProfilePicture(getLoggedId());
        return ResponseEntity.ok("Profile picture deleted successfully");
    }

    @GetMapping("/picture/{fileName}")
    @SneakyThrows
    public ResponseEntity<Void> downloadProfilePicture(
            @PathVariable @NotBlank(message = "Provide file name!") final String fileName,
            final HttpServletResponse response) {
        getLoggedId();
        final InputStream inputStream = userService.downloadMedia(fileName);
        response.setContentType("image/jpeg");
        IOUtils.copy(inputStream, response.getOutputStream());
        return ResponseEntity.ok().build();
    }

    // PUT localhost:8080/users/password
    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestBody @Valid final UserChangePasswordDTO dto) {
        final long userId = getLoggedId();
        userService.changePassword(userId, dto);
        return ResponseEntity.ok("Password changed");
    }

    // PUT localhost:8080/users/info
    @PutMapping("/info")
    public ResponseEntity<String> updateUserInfo(@RequestBody @Valid final UserChangeInfoDTO dto) {
        final long userId = getLoggedId();
        userService.changeInfo(userId, dto);
        return ResponseEntity.ok("Info changed!");
    }

    // PUT localhost:8080/users/deactivate
    @PutMapping("/deactivate")
    public ResponseEntity<String> deactivateUser(@RequestBody @Valid final UserPasswordDTO dto) {
        final long userId = getLoggedId();
        final String message = userService.deactivateUser(userId, dto);
        session.invalidate();
        return ResponseEntity.ok(message);
    }

    // DELETE localhost:8080/users
    @DeleteMapping
    public ResponseEntity<String> deleteUser(@RequestBody @Valid final UserPasswordDTO dto) {
        final long userId = getLoggedId();
        userService.deleteUserById(userId, dto);
        session.invalidate();
        return ResponseEntity.ok("Account successfully deleted");
    }
}
