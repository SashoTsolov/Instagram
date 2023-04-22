package bg.ittalents.instagram.comment;

import bg.ittalents.instagram.comment.DTOs.CommentContentDTO;
import bg.ittalents.instagram.comment.DTOs.CommentDTO;
import bg.ittalents.instagram.util.AbstractController;
import bg.ittalents.instagram.comment.DTOs.PageRequestDTO;
import jakarta.servlet.http.HttpServletRequest;
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

    public CommentController(HttpServletRequest request,
                             HttpSession session,
                             CommentService commentService) {
        super(request, session);
        this.commentService = commentService;
    }

    @GetMapping("/comments/{id}")
    public ResponseEntity<Slice<CommentDTO>> viewCommentReplies(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            final long id,
            @ModelAttribute final PageRequestDTO pageRequestDTO) {

        final Slice<CommentDTO> replies = commentService.getCommentReplies(getLoggedId(), id,
                PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize()));
        return ResponseEntity.ok(replies);
    }

    @GetMapping("/posts/{id}/comments")
    public ResponseEntity<Slice<CommentDTO>> viewParentCommentsByPost(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            final long id,
            @ModelAttribute final PageRequestDTO pageRequestDTO) {

        final Slice<CommentDTO> comments = commentService.getPostComments(
                getLoggedId(), id,
                PageRequest.of(pageRequestDTO.getPage(), pageRequestDTO.getSize()));
        return ResponseEntity.ok(comments);
    }

    // Like/Unlike comment
    @PostMapping("/comments/{id}/like")
    public ResponseEntity<Integer> likeComment(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            final long id) {

        final int numberOfLikes = commentService.likePost(getLoggedId(), id);
        return ResponseEntity.ok(numberOfLikes);
    }

    // Add comment
    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<CommentDTO> addCommentToPost(
            @PathVariable
            @Min(value = 1, message = "ID must be greater than or equal to 1")
            final long id,
            @RequestBody @Valid final CommentContentDTO commentContentDTO) {

        final CommentDTO dto = commentService.addCommentToPost(getLoggedId(), id, commentContentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(dto);
    }

    // Reply to a comment
    @PostMapping("/comments/{id}")
    public ResponseEntity<CommentDTO> replyToComment(
            @PathVariable @Min(value = 1, message = "ID must be greater than or equal to 1")
            final long id,
            @RequestBody @Valid final CommentContentDTO commentContentDTO) {

        final CommentDTO replyDTO = commentService.replyToComment(getLoggedId(),
                id, commentContentDTO);
        return ResponseEntity.status(HttpStatus.CREATED).body(replyDTO);
    }

    // Delete a comment
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<CommentDTO> deleteComment(
            @PathVariable @Min(value = 1, message = "ID must be greater than or equal to 1")
            final long id) {

        final CommentDTO dto = commentService.deleteComment(getLoggedId(), id);
        return ResponseEntity.ok(dto);
    }
}