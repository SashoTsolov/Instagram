package bg.ittalents.instagram.comment;

import bg.ittalents.instagram.comment.DTOs.CommentContentDTO;
import bg.ittalents.instagram.comment.DTOs.CommentDTO;
import bg.ittalents.instagram.util.AbstractController;
import bg.ittalents.instagram.comment.DTOs.PageRequestDTO;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController extends AbstractController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<Slice<CommentDTO>> viewCommentReplies(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            @ModelAttribute PageRequestDTO pageRequestDTO,
            HttpSession session) {

        Slice<CommentDTO> replies = commentService.getCommentReplies(getLoggedId(session), id,
                PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize()));
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<Slice<CommentDTO>> viewParentCommentsByPost(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            @ModelAttribute PageRequestDTO pageRequestDTO,
            HttpSession session) {

        Slice<CommentDTO> comments = commentService.getPostComments(
                getLoggedId(session), id,
                PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize()));
        return ResponseEntity.ok(comments);
    }

    // Like/Unlike comment
    @PostMapping("/comments/{id}/like")
    public ResponseEntity<Integer> likeComment(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            HttpSession session) {

        int numberOfLikes = commentService.likePost(getLoggedId(session), id);
        return ResponseEntity.ok(numberOfLikes);
    }

    // Add comment
    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<CommentDTO> addCommentToPost(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            @RequestBody @Valid CommentContentDTO commentContentDTO,
            HttpSession session) {

        CommentDTO dto = commentService.addCommentToPost(getLoggedId(session), id, commentContentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Reply to a comment
    @PostMapping("/comments/{id}")
    public ResponseEntity<CommentDTO> replyToComment(
            @PathVariable @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            @RequestBody @Valid CommentContentDTO commentContentDTO,
            HttpSession session) {

        CommentDTO replyDTO = commentService.replyToComment(getLoggedId(session),
                id, commentContentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(replyDTO);
    }

    // Delete a comment
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<CommentDTO> deleteComment(
            @PathVariable @Min(value = 1, message = "ID must be greater than or equal to 1")
            long id,
            HttpSession session) {

        CommentDTO dto = commentService.deleteComment(getLoggedId(session), id);
        return ResponseEntity.ok(dto);
    }
}