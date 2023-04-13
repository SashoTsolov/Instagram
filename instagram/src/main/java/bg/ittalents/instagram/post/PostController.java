package bg.ittalents.instagram.post;

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
public class PostController {


    private PostService postService;
    private ModelMapper mapper;

    @Autowired
    public PostController(PostService postService, ModelMapper mapper) {
        this.postService = postService;
        this.mapper = mapper;
    }

    // GET
    // View
    // localhost:8080/posts/1
    @GetMapping("/{id}")
    public void getPostById(@PathVariable Long id) {
        //TODO
    }

    // GET
    // View my saved posts
    // localhost:8080/posts/saved
    @GetMapping("/saved")
    public void getUserSavedPosts() {
        //TODO
    }

    // GET
    // View recent posts from followed users - Feed
    // localhost:8080/posts
    @GetMapping
    public void getPostsFromFeed() {
        //TODO
    }

    // POST
    // View all by hashtag
    // localhost:8080/posts/search
    @PostMapping("/search")
    public void searchPostsByHashtags() {
        //TODO
    }

    // POST
    // Like/Unlike
    // localhost:8080/posts/1/like
    @PostMapping("/{id}/like")
    public void likePost(@PathVariable Long id) {
        //TODO
    }

    // POST
    // Add
    // localhost:8080/posts
    @PostMapping
    public void addPost(@RequestBody PostPreviewDTORecord postPreviewDTORecord) {
        //TODO
    }

    // POST
    // Save
    // localhost:8080/posts/1/save
    @PostMapping("/{id}/save")
    public void savePost(@PathVariable Long id) {
        //TODO
    }

    // PUT
    // Edit caption
    // localhost:8080/posts/1/caption
    @PutMapping("/{id}/caption")
    public void updateCaption(@PathVariable Long id, @RequestBody String caption) {
        //TODO
    }

    // DELETE
    // Delete
    // localhost:8080/posts/1
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        //TODO
    }
}
