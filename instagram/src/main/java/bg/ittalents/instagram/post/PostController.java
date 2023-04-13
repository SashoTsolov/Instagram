package bg.ittalents.instagram.post;

import bg.ittalents.instagram.post.DTOs.PostPreviewDTO;
import bg.ittalents.instagram.util.AbstractController;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/posts")
public class PostController extends AbstractController {


    private PostService postService;
    private ModelMapper mapper;

    @Autowired
    public PostController(PostService postService, ModelMapper mapper) {
        this.postService = postService;
        this.mapper = mapper;
    }

    // GET - localhost:8080/posts/1
    // View
    @GetMapping("/{id}")
    public void getPostById(@PathVariable Long id) {
        //TODO
    }

    // GET - localhost:8080/posts/saved
    // View my saved posts
    @GetMapping("/saved")
    public void getUserSavedPosts() {
        //TODO
    }

    // GET - localhost:8080/posts
    // View recent posts from followed users - Feed
    @GetMapping
    public void getPostsFromFeed() {
        //TODO
    }

    // POST - localhost:8080/posts/search
    // View all by hashtag
    @PostMapping("/search")
    public void searchPostsByHashtags() {
        //TODO
    }

    // POST - localhost:8080/posts/1/like
    // Like/Unlike
    @PostMapping("/{id}/like")
    public void likePost(@PathVariable Long id) {
        //TODO
    }

    // POST - localhost:8080/posts
    // Add
    @PostMapping
    public void addPost(@RequestBody PostPreviewDTO postPreviewDTORecord) {
        //TODO
    }

    // POST - localhost:8080/posts/1/save
    // Save
    @PostMapping("/{id}/save")
    public void savePost(@PathVariable Long id) {
        //TODO
    }

    // PUT - localhost:8080/posts/1/caption
    // Edit caption
    @PutMapping("/{id}/caption")
    public void updateCaption(@PathVariable Long id, @RequestBody String caption) {
        //TODO
    }

    // DELETE - localhost:8080/posts/1
    // Delete
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        //TODO
    }
}
