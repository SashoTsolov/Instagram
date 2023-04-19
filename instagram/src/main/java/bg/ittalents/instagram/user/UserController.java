package bg.ittalents.instagram.user;

import bg.ittalents.instagram.exceptions.BadRequestException;
import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.user.DTOs.*;
import bg.ittalents.instagram.util.AbstractController;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController extends AbstractController {
    @Autowired
    private UserService userService;

    // GET localhost:8080/users/email?verification-token=QWERTY123456
//    @GetMapping("/email")
//    public UserWithoutPassAndEmailDTO getUserByEmail(@RequestParam String verificationToken) {
//        return userService.getUserByEmail(verificationToken);
//    }

    // GET localhost:8080/users/2
    @GetMapping("/{id}")
    public UserWithoutPassAndEmailDTO getUserById(@PathVariable long id, HttpSession session) {
        getLoggedId(session);
        return userService.getById(id);
    }

    // GET localhost:8080/users/1/followers
//    @GetMapping("/{id}/followers")
//    public Page<UserBasicInfoDTO> getFollowers(@PathVariable long id, HttpSession session) {
//        long userId = getLoggedId(session);
//        return userService.getAllUserFollowers(id, userId);
//    }
//    // GET localhost:8080/users/1/following
//    @GetMapping("/{id}/following")
//    public Page<UserBasicInfoDTO> getFollowing(@PathVariable long id, HttpSession session) {
//        long userId = getLoggedId(session);
//        return userService.getAllFollowingById(id, userId);
//    }
//
    @GetMapping("/search")
    public List<UserBasicInfoDTO> searchUsersByUsername(@RequestParam String username) {
        return userService.searchUsersByUsername(username);
    }

    // POST localhost:8080/users
    @PostMapping
    public UserWithoutPassAndEmailDTO createUser(@Valid @RequestBody RegisterDTO dto, HttpSession s) {
            if (s.getAttribute("LOGGED_ID") != null) {
                throw new UnauthorizedException("You can't register while logged into an account");
            }
            return userService.create(dto);
    }

    // POST localhost:8080/users/login
    @PostMapping("/login")
    public UserWithoutPassAndEmailDTO login(@RequestBody UserLoginDTO dto, HttpSession session) {
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
    public void logout(HttpSession session) {
        session.invalidate();
    }


     //PUT localhost:8080/users/password/forgot
    @PutMapping("/password/forgot")
    public ResponseEntity<String> forgotPassword(@Valid @RequestBody UserEmailDTO dto) {
        userService.forgotPassword(dto);
        return ResponseEntity.ok("Email for password reset was successfully sent");
    }

    @GetMapping("/password/reset")
    public void resetPassword(@RequestParam("id") String identifier){
        userService.resetPassword(identifier);
    }

    // POST localhost:8080/users/2/block
    @PostMapping("/{id}/block")
    public ResponseEntity<String> blockUser(@PathVariable("id") long blockedId, HttpSession session) {
        long blockingUserId = getLoggedId(session);
        return userService.block(blockingUserId, blockedId);
    }

     //POST localhost:8080/users/2/follow
    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> followUser(@PathVariable("id") long followedId, HttpSession session) {
        long followerId = getLoggedId(session);
        userService.follow(followerId, followedId);
        return new ResponseEntity<>(HttpStatus.OK);
    }

//     PUT localhost:8080/users/picture
     @PutMapping("/picture")
     public UserBasicInfoDTO uploadProfilePicture(@RequestParam("file") MultipartFile file, HttpSession session) {
         long userId = getLoggedId(session);
         return userService.updateProfilePicture(userId, file);
     }

     @GetMapping("/picture/{fileName}")
     public void download(@PathVariable("fileName") String fileName, HttpServletResponse response) throws IOException {
        File file = userService.download(fileName);
        response.setContentType("image/jpeg");
        Files.copy(file.toPath(), response.getOutputStream());
     }

    // PUT localhost:8080/users/password
    @PutMapping("/password")
    public ResponseEntity<String> updatePassword(@RequestBody UserChangePasswordDTO dto, HttpSession session) {
        long userId = getLoggedId(session);
        return userService.changePassword(userId, dto);
    }

    // PUT localhost:8080/users/info
    @PutMapping("/info")
    public ResponseEntity<String> updateUserInfo(@Valid @RequestBody UserChangeInfoDTO dto, HttpSession session) {
        long userId = getLoggedId(session);
        return userService.changeInfo(userId, dto);
    }

    // PUT localhost:8080/users/deactivate
    @PutMapping("/deactivate")
    public ResponseEntity<String> deactivateUser(HttpSession session, @RequestBody UserPasswordDTO dto) {
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
    public ResponseEntity<String> deleteUser(@RequestBody UserPasswordDTO dto, HttpSession session) {
        long userId = getLoggedId(session);
        ResponseEntity<String> responseEntity = userService.deleteUserById(userId, dto);
        session.invalidate();
        return responseEntity;
    }
}
