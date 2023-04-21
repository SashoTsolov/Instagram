package bg.ittalents.instagram.post;

import bg.ittalents.instagram.comment.DTOs.PageRequestDTO;
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
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.SneakyThrows;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
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


    private final PostService postService;
    private final MediaService mediaService;

    public PostController(PostService postService, MediaService mediaService) {
        this.postService = postService;
        this.mediaService = mediaService;
    }

    // View post
    @GetMapping("/posts/{id}")
    public ResponseEntity<PostWithCommentsDTO> getPostById(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            @ModelAttribute PageRequestDTO pageRequestDTO,
            HttpSession session) {
        PostWithCommentsDTO dto = postService.getPostById(getLoggedId(session), id,
                PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize()));
        return ResponseEntity.ok(dto);
    }


    // View user's posts sorted by upload date - DESC
    @GetMapping("/users/{id}/posts")
    public ResponseEntity<Slice<PostPreviewDTO>> getUserPostsById(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            @ModelAttribute PageRequestDTO pageRequestDTO,
            HttpSession session) {

        getLoggedId(session);
        Slice<PostPreviewDTO> postPreviewDTOsList = postService.getUserPostsById(
                getLoggedId(session),
                id,
                PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize()));
        return ResponseEntity.ok(postPreviewDTOsList);
    }

    //     View all by location
    @PostMapping("/posts/location")
    public ResponseEntity<Slice<PostPreviewDTO>> searchPostsByLocation(
            @RequestBody @Valid SearchRequestDTO searchRequestDTO,
            HttpSession session) {
        Slice<PostPreviewDTO> postPreviewDTOsList = postService.searchPostsByLocation(
                getLoggedId(session),
                searchRequestDTO.getSearchString(),
                PageRequest.of(searchRequestDTO.getPage(),
                        searchRequestDTO.getSize()));
        return ResponseEntity.ok(postPreviewDTOsList);
    }


    // View all by hashtag
    @PostMapping("/posts/hashtag")
    public ResponseEntity<Slice<PostPreviewDTO>> searchPostsByHashtags(
            @RequestBody @Valid SearchRequestDTO searchRequestDTO, HttpSession session) {
        Slice<PostPreviewDTO> postPreviewDTOsList = postService.searchPostsByHashtags(
                getLoggedId(session),
                searchRequestDTO.getSearchString(),
                PageRequest.of(searchRequestDTO.getPage(),
                        searchRequestDTO.getSize()));
        return ResponseEntity.ok(postPreviewDTOsList);
    }


    // Add post
    @PostMapping("/posts")
    public ResponseEntity<PostWithoutCommentsDTO> addPost(
            @RequestBody @Valid CreatePostDTO createPostDTO,
            HttpSession session) {

        PostWithoutCommentsDTO dto = postService.addPost(getLoggedId(session), createPostDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Add media
    @PostMapping("/posts/{id}/media")
    public ResponseEntity<PostWithoutCommentsDTO> addMedia(
            @RequestParam("file") List<MultipartFile> files,
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            HttpSession session) {
        getLoggedId(session);
        PostWithoutCommentsDTO dto = mediaService.upload(files, id);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
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
    public ResponseEntity<String> updateCaption(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            @RequestBody @Valid CaptionDTO caption,
            HttpSession session) {
        getLoggedId(session);
        String dto = postService.updateCaption(getLoggedId(session), id, caption);
        return ResponseEntity.ok(dto);
    }


    // DELETE - localhost:8080/posts/1
    @DeleteMapping("/posts/{id}")
    public ResponseEntity<String> deletePost(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id, HttpSession session) {
        getLoggedId(session);
        String dto = postService.deletePost(getLoggedId(session), id);
        return ResponseEntity.ok(dto);
    }

    // Like/Unlike
    @PostMapping("/posts/{id}/like")
    public ResponseEntity<Integer> likePost(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id, HttpSession session) {
        int numberOfLikes = postService.likePost(getLoggedId(session), id);
        return ResponseEntity.ok(numberOfLikes);
    }

    @PostMapping("/posts/{id}/save")
    public ResponseEntity<String> savePost(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id, HttpSession session) {
        getLoggedId(session);
        String dto = postService.savePost(getLoggedId(session), id);
        return ResponseEntity.ok(dto);
    }

    // View my saved posts
    @GetMapping("/posts/saved")
    public ResponseEntity<Slice<PostPreviewDTO>> getUserSavedPosts(
            @ModelAttribute PageRequestDTO pageRequestDTO,
            HttpSession session) {
        getLoggedId(session);
        Slice<PostPreviewDTO> savedPosts = postService.getUserSavedPosts(getLoggedId(session),
                PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize()));
        return ResponseEntity.ok(savedPosts);
    }

    @GetMapping("/users/{id}/tagged")
    public ResponseEntity<Slice<PostPreviewDTO>> getUserTaggedPostsById(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            @ModelAttribute PageRequestDTO pageRequestDTO,
            HttpSession session) {
        getLoggedId(session);
        Slice<PostPreviewDTO> postPreviewDTOsList = postService.getUserTaggedPostsById(
                getLoggedId(session),
                id,
                PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize()));
        return ResponseEntity.ok(postPreviewDTOsList);
    }

    // View recent posts from followed users - Feed
    @GetMapping("/posts")
    public ResponseEntity<Slice<PostWithoutCommentsDTO>> getPostsFromFeed(
            @ModelAttribute PageRequestDTO pageRequestDTO,
            HttpSession session) {

        Slice<PostWithoutCommentsDTO> feed = postService.getPostsFromFeed(
                getLoggedId(session),
                PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize()));
        return ResponseEntity.ok(feed);
    }
}
