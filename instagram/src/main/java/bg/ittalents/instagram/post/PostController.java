package bg.ittalents.instagram.post;

import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.post.DTOs.PostPreviewDTO;
import bg.ittalents.instagram.post.entities.Post;
import bg.ittalents.instagram.util.AbstractController;
import jakarta.servlet.http.HttpSession;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
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
    private PostMapper postMapper;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

//    // GET - localhost:8080/posts/1
//    // View
//    @GetMapping("/{id}")
//    public ResponseEntity<Post> getPostById(@PathVariable Long id) {
//        Post post = postService.getPostById(id);
//        return ResponseEntity.ok(post);
//    }
//
//    // GET - localhost:8080/posts/saved
//    // View my saved posts
//    @GetMapping("/saved")
//    public ResponseEntity<List<Post>> getUserSavedPosts() {
//        List<Post> savedPosts = postService.getUserSavedPosts();
//        return ResponseEntity.ok(savedPosts);
//    }
//
//    // GET - localhost:8080/posts
//    // View recent posts from followed users - Feed
//    @GetMapping
//    public ResponseEntity<List<Post>> getPostsFromFeed() {
//        List<Post> posts = postService.getPostsFromFeed();
//        return ResponseEntity.ok(posts);
//    }
//
//    // POST - localhost:8080/posts/search
//    // View all by hashtag
//    @PostMapping("/search")
//    public ResponseEntity<List<Post>> searchPostsByHashtags(@RequestBody String hashtag) {
//        List<Post> posts = postService.searchPostsByHashtags(hashtag);
//        return ResponseEntity.ok(posts);
//    }
//
//    // POST - localhost:8080/posts/1/like
//    // Like/Unlike
//    @PostMapping("/{id}/like")
//    public ResponseEntity<Void> likePost(@PathVariable Long id) {
//        postService.likePost(id);
//        return ResponseEntity.ok().build();
//    }

    // POST - localhost:8080/posts
    // Add
    @PostMapping
    public ResponseEntity<Post> addPost(@RequestBody PostPreviewDTO postPreviewDTORecord) {
//        Post post = postMapper.INSTANCE.
//        postService.addPost(PostPreviewDTO);
//        return ResponseEntity.ok(post);
        return null;
    }

    // POST - localhost:8080/posts/1/save
    // Save
//    @PostMapping("/{id}/save")
//    public ResponseEntity<Void> savePost(@PathVariable Long id) {
//        postService.savePost(id);
//        return ResponseEntity.ok().build();
//    }
//
//    // PUT - localhost:8080/posts/1/caption
//    // Edit caption
//    @PutMapping("/{id}/caption")
//    public ResponseEntity<Void> updateCaption(@PathVariable Long id, @RequestBody String caption) {
//        postService.updateCaption(id, caption);
//        return ResponseEntity.ok().build();
//    }
//
//    // DELETE - localhost:8080/posts/1
//    // Delete
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deletePost(@PathVariable Long id) {
//        postService.deletePost(id);
//        return ResponseEntity.ok().build();
//    }
}

