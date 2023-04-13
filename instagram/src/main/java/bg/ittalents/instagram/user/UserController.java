package bg.ittalents.instagram.user;

import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.user.DTOs.*;
import bg.ittalents.instagram.util.AbstractController;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
public class UserController extends AbstractController {

    @Autowired
    private UserService userService;

    // GET localhost:8080/users/email?verification-token=QWERTY123456
    @GetMapping("/email")
    public UserWithoutPassAndEmailDTO getUserByEmail(@RequestParam String verificationToken) {
        // implementation
        return null;
        //TODO
    }

    // GET localhost:8080/users/2
    @GetMapping("/{id}")
    public UserWithoutPassAndEmailDTO getUserById(@PathVariable long id) {
        return userService.getById(id);
    }

    // GET localhost:8080/users/2/tagged
    @GetMapping("/{id}/tagged")
    public List<Post> getTaggedPosts(@PathVariable long id, HttpSession s) {
        long userId = getLoggedId(s);
        return userService.getTaggedPostsById(id, userId);
    }

    // GET localhost:8080/users/1/followers
    @GetMapping("/{id}/followers")
    public List<UserBasicInfoDTO> getFollowers(@PathVariable long id, HttpSession s) {
        long userId = getLoggedId(s);
        return userService.getAllFollowersById(id, userId);
    }

    // GET localhost:8080/users/1/following
    @GetMapping("/{id}/following")
    public List<UserBasicInfoDTO> getFollowing(@PathVariable long id, HttpSession s) {
        long userId = getLoggedId(s);
        return userService.getAllFollowingById(id, userId);
    }

    // POST localhost:8080/users/search
    @PostMapping("/search")
    public List<UserBasicInfoDTO> searchUsers(@RequestBody String name, HttpSession s) {
        long userId = getLoggedId(s);
        return userService.getAllByName(name);
        //TODO
    }

    // POST localhost:8080/users
    @PostMapping
    public UserWithoutPassAndEmailDTO createUser(@RequestBody RegisterDTO dto) {
        return userService.register(dto);
    }

    // POST localhost:8080/users/login
    @PostMapping("/login")
    public UserWithoutPassAndEmailDTO login(@RequestBody UserLoginDTO dto, HttpSession s) {
        UserWithoutPassAndEmailDTO respDto = userService.login(dto);
        s.setAttribute("LOGGED", true);
        s.setAttribute("LOGGED_ID", respDto.getId());
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

    // PUT localhost:8080/users/password/forgot
    @PutMapping("/password/forgot")
    public void forgotPassword(@RequestBody UserForgotPasswordDTO dto) {
        String email = dto.getEmail();
        boolean result = userService.forgotPassword(email);
        if(!result){

        }
    }

    // POST localhost:8080/users/2/block
    @PostMapping("/{id}/block")
    public void blockUser(@PathVariable long blockedId, HttpSession s) {
        long blockerId = getLoggedId(s);
        userService.block(blockerId, blockedId);
    }

    // POST localhost:8080/users/2/follow
    @PostMapping("/{id}/follow")
    public int followUser(@PathVariable long followedId, HttpSession s) {
        long followerId = getLoggedId(s);
        return userService.follow(followerId, followedId);
    }

    // POST localhost:8080/users/2/unfollow
    @PostMapping("/{id}/unfollow")
    public int unfollowUser(@PathVariable long unfollowedId, HttpSession s) {
       long unfollowerId = getLoggedId(s);
       return userService.unfollow(unfollowerId, unfollowedId);
    }

    // PUT localhost:8080/users/picture
    @PutMapping("/picture")
    public void updateProfilePicture(@RequestParam("file") MultipartFile file, HttpSession s) {
        long userId = getLoggedId(s);
        return userService.updateProfilePicture(file, userId);
    }

    // PUT localhost:8080/users/password
    @PutMapping("/password")
    public void updatePassword(@RequestBody UpdatePasswordRequest request) {
        // implementation
        //TODO
    }

    // PUT localhost:8080/users/info
    @PutMapping("/info")
    public void updateUserInfo(@RequestBody UpdateUserInfoRequest request) {
        // implementation
        //TODO
    }

    // PUT localhost:8080/users/deactivate
    @PutMapping("/deactivate")
    public void deactivateUser() {
        // implementation
        //TODO
    }

    // DELETE localhost:8080/users
    @DeleteMapping
    public void deleteUser() {
        // implementation
        //TODO
    }
}
