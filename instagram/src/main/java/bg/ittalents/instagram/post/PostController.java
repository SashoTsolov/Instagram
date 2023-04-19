package bg.ittalents.instagram.post;

import bg.ittalents.instagram.media.MediaService;
import bg.ittalents.instagram.post.DTOs.CaptionDTO;
import bg.ittalents.instagram.post.DTOs.CreatePostDTO;
import bg.ittalents.instagram.post.DTOs.PostPreviewDTO;
import bg.ittalents.instagram.post.DTOs.PostWithCommentsDTO;
import bg.ittalents.instagram.post.DTOs.PostWithoutCommentsDTO;
import bg.ittalents.instagram.post.DTOs.SearchRequestDTO;
import bg.ittalents.instagram.util.AbstractController;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;

@RestController
@RequestMapping
public class PostController extends AbstractController {

    @Autowired
    protected PostService postService;

    @Autowired
    MediaService mediaService;

    @Autowired
    public PostController(PostService postService) {
        this.postService = postService;
    }

    // View post
    @GetMapping("/posts/{id}")
    public ResponseEntity<PostWithCommentsDTO> getPostById(
            @PathVariable long id,
            @RequestParam int page,
            @RequestParam int size,
            HttpSession session) {
        getLoggedId(session);
        PostWithCommentsDTO dto = postService.getPostById(id, PageRequest.of(page, size));
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }


    // View user's posts sorted by upload date - DESC
    @GetMapping("/users/{id}/posts")
    public ResponseEntity<Slice<PostPreviewDTO>> getUserPostsById(
            @RequestParam int page,
            @RequestParam int size,
            @PathVariable long id, HttpSession session) {

        getLoggedId(session);
        Slice<PostPreviewDTO> postPreviewDTOsList = postService.getUserPostsById(
                id,
                PageRequest.of(page, size));
        return new ResponseEntity<>(postPreviewDTOsList, HttpStatus.OK);
    }

    //     View all by location
    @PostMapping("/posts/location")
    public ResponseEntity<Slice<PostPreviewDTO>> searchPostsByLocation(@RequestBody SearchRequestDTO searchRequestDTO) {
        Slice<PostPreviewDTO> postPreviewDTOsList = postService.searchPostsByLocation(
                searchRequestDTO.getSearchString(),
                PageRequest.of(searchRequestDTO.getPage(),
                        searchRequestDTO.getSize()));
        return new ResponseEntity<>(postPreviewDTOsList, HttpStatus.OK);
    }


    // View all by hashtag
    @PostMapping("/posts/hashtag")
    public ResponseEntity<Slice<PostPreviewDTO>> searchPostsByHashtags(@RequestBody SearchRequestDTO searchRequestDTO) {
        Slice<PostPreviewDTO> postPreviewDTOsList = postService.searchPostsByHashtags(
                searchRequestDTO.getSearchString(),
                PageRequest.of(searchRequestDTO.getPage(),
                        searchRequestDTO.getSize()));
        return new ResponseEntity<>(postPreviewDTOsList, HttpStatus.OK);
    }


    // Add post
    @PostMapping("/posts")
    public ResponseEntity<PostWithoutCommentsDTO> addPost(@RequestBody CreatePostDTO createPostDTO,
                                                          HttpSession session) {

        PostWithoutCommentsDTO dto = postService.addPost(createPostDTO, getLoggedId(session));
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    // Add media
    @PostMapping("/posts/{id}/media")
    public ResponseEntity<PostWithoutCommentsDTO> addMedia(@RequestParam("file") List<MultipartFile> files,
                                                           @PathVariable long id,
                                                           HttpSession session) {
        getLoggedId(session);
        PostWithoutCommentsDTO dto = mediaService.upload(files, id);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    // View media
    @SneakyThrows
    @GetMapping("/posts/media/{fileName}")
    public void download(@PathVariable("fileName") String fileName, HttpServletResponse response, HttpSession session) {
        getLoggedId(session);
        File file = mediaService.download(fileName);
        Files.copy(file.toPath(), response.getOutputStream());
    }

    // Edit caption - localhost:8080/posts/1/caption
    @PutMapping("/posts/{id}/caption")
    public ResponseEntity<String> updateCaption(@PathVariable long id,
                                                @RequestBody CaptionDTO caption,
                                                HttpSession session) {
        getLoggedId(session);
        String dto = postService.updateCaption(id, caption, getLoggedId(session));
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }


    // DELETE - localhost:8080/posts/1
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<String> deletePost(@PathVariable long id, HttpSession session) {
        getLoggedId(session);
        String dto = postService.deletePost(id, getLoggedId(session));
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    // Like/Unlike
    @PostMapping("/posts/{id}/like")
    public ResponseEntity<Integer> likePost(@PathVariable long id, HttpSession session) {
        int numberOfLikes = postService.likePost(id, getLoggedId(session));
        return new ResponseEntity<>(numberOfLikes, HttpStatus.OK);
    }

    @PostMapping("/posts/{id}/save")
    public ResponseEntity<String> savePost(@PathVariable long id, HttpSession session) {
        getLoggedId(session);
        String dto = postService.savePost(id, getLoggedId(session));
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }

    // View my saved posts
    @GetMapping("/posts/saved")
    public ResponseEntity<Slice<PostPreviewDTO>> getUserSavedPosts(
            @RequestParam int page,
            @RequestParam int size,
            HttpSession session) {
        getLoggedId(session);
        Slice<PostPreviewDTO> savedPosts = postService.getUserSavedPosts(getLoggedId(session),
                PageRequest.of(page, size));
        return new ResponseEntity<>(savedPosts, HttpStatus.OK);
    }

    @GetMapping("/users/{id}/tagged")
    public ResponseEntity<Slice<PostPreviewDTO>> getUserTaggedPostsById(
            @PathVariable long id,
            @RequestParam int page,
            @RequestParam int size,
            HttpSession session) {

        getLoggedId(session);
        Slice<PostPreviewDTO> postPreviewDTOsList = postService.getUserTaggedPostsById(
                id,
                PageRequest.of(page, size));
        return new ResponseEntity<>(postPreviewDTOsList, HttpStatus.OK);
    }
}
