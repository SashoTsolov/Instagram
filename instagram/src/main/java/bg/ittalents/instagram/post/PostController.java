package bg.ittalents.instagram.post;

import bg.ittalents.instagram.media.MediaService;
import bg.ittalents.instagram.post.DTOs.CreatePostDTO;
import bg.ittalents.instagram.post.DTOs.PostWithoutCommentsDTO;
import bg.ittalents.instagram.util.AbstractController;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping
public class PostController extends AbstractController {

    private PostService postService;

    @Autowired
    MediaService mediaService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    // POST - localhost:8080/posts
    // Add post
    @PostMapping("/posts")
    public ResponseEntity<PostWithoutCommentsDTO> addPost(@RequestBody CreatePostDTO createPostDTO,
                                                          HttpSession session) {

        PostWithoutCommentsDTO dto = postService.addPost(createPostDTO, getLoggedId(session));
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    // POST - localhost:8080/posts/media
    // Add media
    @PostMapping("/posts/{id}/media")
    public ResponseEntity<PostWithoutCommentsDTO> addMedia(@RequestParam("file") List<MultipartFile> files,
                                                           @PathVariable int id,
                                                           HttpSession session) {
        PostWithoutCommentsDTO dto = mediaService.upload(files, getLoggedId(session), id);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }
}

