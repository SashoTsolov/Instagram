package bg.ittalents.instagram.user;

import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.user.DTOs.*;
import bg.ittalents.instagram.util.AbstractController;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
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
    public UserWithoutPassAndEmailDTO getUserById(@PathVariable Long id, HttpSession s) {
        getLoggedId(s);
        return userService.getById(id);
    }

    // GET localhost:8080/users/1/followers
    @GetMapping("/{id}/followers")
    public Page<UserBasicInfoDTO> getFollowers(@PathVariable Long id, HttpSession s) {
        long userId = getLoggedId(s);
        return userService.getAllUserFollowers(id, userId);
    }
//    // GET localhost:8080/users/1/following
//    @GetMapping("/{id}/following")
//    public Page<UserBasicInfoDTO> getFollowing(@PathVariable long id, HttpSession s) {
//        long userId = getLoggedId(s);
//        return userService.getAllFollowingById(id, userId);
//    }
//
//    // POST localhost:8080/users/search
//    @PostMapping("/search")
//    public Page<UserBasicInfoDTO> searchUsers(@RequestBody String name, HttpSession s) {
//        long userId = getLoggedId(s);
//        return userService.getAllByName(name, userId);
//        //TODO
//    }

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
        //Add logic that you can't log in if you're already logged in!
        UserWithoutPassAndEmailDTO respDto = userService.login(dto);
        s.setAttribute("LOGGED", true);
        s.setAttribute("LOGGED_ID", respDto.id());
        return respDto;
    }

    // POST localhost:8080/users/logout
    @PostMapping("/logout")
    public void logout(HttpSession s) {
        if (s.getAttribute("LOGGED").equals(true)){
            s.invalidate();
        }
        else {
            throw new UnauthorizedException("You have no permission to execute this request");
        }
    }

     //PUT localhost:8080/users/password/forgot
    @PutMapping("/password/forgot")
    public void forgotPassword(@Valid @RequestBody @NotBlank @Email String email) {
        boolean result = userService.forgotPassword(email);
    }

    // POST localhost:8080/users/2/block
    @PostMapping("/{id}/block")
    public void blockUser(@PathVariable("id") Long blockedId, HttpSession s) {
        long blockingUserId = getLoggedId(s);
        userService.block(blockingUserId, blockedId);
    }

     //POST localhost:8080/users/2/follow
    @PostMapping("/{id}/follow")
    public int followUser(@PathVariable("id") Long followedId, HttpSession s) {
        long followerId = getLoggedId(s);
        return userService.follow(followerId, followedId);
    }
//
//
//     //PUT localhost:8080/users/picture
//    @PutMapping("/picture")
//    public void updateProfilePicture(@RequestParam("file") MultipartFile file, HttpSession s) {
//        long userId = getLoggedId(s);
//        return userService.updateProfilePicture(userId, file);
//    }
//
//    // PUT localhost:8080/users/password
//    @PutMapping("/password")
//    public void updatePassword(@RequestBody UserChangePasswordDTO dto, HttpSession s) {
//        long userId = getLoggedId(s);
//        userService.changePassword(userId, dto);
//    }
//
//    // PUT localhost:8080/users/info
//    @PutMapping("/info")
//    public void updateUserInfo(@RequestBody UserChangeInfoDTO dto, HttpSession s) {
//        long userId = getLoggedId(s);
//        userService.changeInfo(userId, dto);
//    }
//
//    // PUT localhost:8080/users/deactivate
//    @PutMapping("/deactivate")
//    public void deactivateUser(UserPasswordDTO dto, HttpSession s) {
//        long userId = getLoggedId(s);
//        userService.deactiveAccount(userId, dto);
//    }
//
//    // DELETE localhost:8080/users
//    @DeleteMapping
//    public void deleteUser(UserPasswordDTO dto, HttpSession s) {
//        long userId = getLoggedId(s);
//        userService.deleteAccount(userId, dto);
//    }
}
