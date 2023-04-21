package bg.ittalents.instagram.comment;

import bg.ittalents.instagram.comment.DTOs.CommentContentDTO;
import bg.ittalents.instagram.comment.DTOs.CommentDTO;
import bg.ittalents.instagram.util.AbstractController;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController extends AbstractController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<Slice<CommentDTO>> viewCommentReplies(
            @RequestParam
            @Min(value = 0, message = "Page must be greater than or equal to 0")
            int page,
            @RequestParam
            @Min(value = 1, message = "Size must be greater than or equal to 1")
            @Max(value = 100, message = "Size must be less than or equal to 100")
            int size,
            @PathVariable @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            HttpSession session) {

        Slice<CommentDTO> replies = commentService.getCommentReplies(getLoggedId(session), id,
                PageRequest.of(page, size));
        return new ResponseEntity<>(replies, HttpStatus.OK);
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<Slice<CommentDTO>> viewParentCommentsByPost(
            @RequestParam
            @Min(value = 0, message = "Page must be greater than or equal to 0")
            int page,
            @RequestParam
            @Min(value = 1, message = "Size must be greater than or equal to 1")
            @Max(value = 100, message = "Size must be less than or equal to 100")
            int size,
            @PathVariable @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            HttpSession session) {

        Slice<CommentDTO> comments = commentService.getPostComments(
                getLoggedId(session), id,
                PageRequest.of(page, size));
        return new ResponseEntity<>(comments, HttpStatus.OK);
    }

    // Like/Unlike comment
    @PostMapping("/comments/{id}/like")
    public ResponseEntity<Integer> likeComment(
            @PathVariable @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            HttpSession session) {
        int numberOfLikes = commentService.likePost(getLoggedId(session), id);
        return new ResponseEntity<>(numberOfLikes, HttpStatus.OK);
    }

    // Add comment
    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<CommentDTO> addCommentToPost(
            @PathVariable @Min(value = 1, message = "ID must be greater than or equal to 1") long id,
            @Valid @RequestBody CommentContentDTO commentContentDTO,
            HttpSession session) {

        CommentDTO dto = commentService.addCommentToPost(getLoggedId(session), id, commentContentDTO);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    // Reply to a comment
    @PostMapping("/comments/{id}")
    public ResponseEntity<CommentDTO> replyToComment(
            @PathVariable("id") long parentId,
            @Valid @RequestBody CommentContentDTO commentContentDTO,
            HttpSession session) {

        CommentDTO replyDTO = commentService.replyToComment(getLoggedId(session),
                parentId, commentContentDTO);
        return new ResponseEntity<>(replyDTO, HttpStatus.CREATED);
    }

    // Delete a comment
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<CommentDTO> deleteComment(@PathVariable long id, HttpSession session) {


        CommentDTO dto = commentService.deleteComment(getLoggedId(session), id);
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}