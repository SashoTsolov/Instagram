package bg.ittalents.instagram.user;

import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.user.DTOs.*;
import bg.ittalents.instagram.util.AbstractController;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
    public UserWithoutPassAndEmailDTO getUserById(@PathVariable long id, HttpSession s) {
        getLoggedId(s);
        return userService.getById(id);
    }

    // GET localhost:8080/users/1/followers
//    @GetMapping("/{id}/followers")
//    public Page<UserBasicInfoDTO> getFollowers(@PathVariable long id, HttpSession s) {
//        long userId = getLoggedId(s);
//        return userService.getAllUserFollowers(id, userId);
//    }
//    // GET localhost:8080/users/1/following
//    @GetMapping("/{id}/following")
//    public Page<UserBasicInfoDTO> getFollowing(@PathVariable long id, HttpSession s) {
//        long userId = getLoggedId(s);
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
    public UserWithoutPassAndEmailDTO login(@RequestBody UserLoginDTO dto, HttpSession s) {
        //Please keep in mind that the login method does the mapping.
        if (s.getAttribute("LOGGED_ID") != null) {
            throw new UnauthorizedException("You're already logged into an account");
        }
        UserWithoutPassAndEmailDTO respDto = userService.login(dto);
        s.setAttribute("LOGGED", true);
        s.setAttribute("LOGGED_ID", respDto.getId());
        return respDto;
    }

    // POST localhost:8080/users/logout
    @PostMapping("/logout")
    public void logout(HttpSession s) {
        if (s.getAttribute("LOGGED") != null){
            s.invalidate();
        }
        else {
            throw new UnauthorizedException("You have no permission to execute this request");
        }
    }


//     //PUT localhost:8080/users/password/forgot
//    @PutMapping("/password/forgot")
//    public void forgotPassword(@Valid @RequestBody @NotBlank @Email String email) {
//        boolean result = userService.forgotPassword(email);
//    }

    // POST localhost:8080/users/2/block
    @PostMapping("/{id}/block")
    public void blockUser(@PathVariable("id") long blockedId, HttpSession s) {
        long blockingUserId = getLoggedId(s);
        userService.block(blockingUserId, blockedId);
    }

     //POST localhost:8080/users/2/follow
    @PostMapping("/{id}/follow")
    public int followUser(@PathVariable("id") long followedId, HttpSession s) {
        long followerId = getLoggedId(s);
        return userService.follow(followerId, followedId);
    }

//     //PUT localhost:8080/users/picture
//    @PutMapping("/picture")
//    public void updateProfilePicture(@RequestParam("file") MultipartFile file, HttpSession s) {
//        long userId = getLoggedId(s);
//        return userService.updateProfilePicture(userId, file);
//    }
//
    // PUT localhost:8080/users/password
    @PutMapping("/password")
    public void updatePassword(@RequestBody UserChangePasswordDTO dto, HttpSession s) {
        long userId = getLoggedId(s);
        userService.changePassword(userId, dto);
    }

    // PUT localhost:8080/users/info
    @PutMapping("/info")
    public void updateUserInfo(@Valid @RequestBody UserChangeInfoDTO dto, HttpSession s) {
        long userId = getLoggedId(s);
        userService.changeInfo(userId, dto);
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
    public void deleteUser(@RequestBody UserPasswordDTO dto, HttpSession session) {
        long userId = getLoggedId(session);
        userService.deleteUserById(userId, dto);
        session.invalidate();
    }
}
