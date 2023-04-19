package bg.ittalents.instagram.comment;

import bg.ittalents.instagram.comment.DTOs.CommentContentDTO;
import bg.ittalents.instagram.comment.DTOs.CommentDTO;
import bg.ittalents.instagram.util.AbstractController;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
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


    @Autowired
    CommentService commentService;

    @GetMapping("/comments/{id}")
    public ResponseEntity<Slice<CommentDTO>> viewCommentReplies(@RequestParam int page,
                                                                @RequestParam int size,
                                                                @PathVariable long id,
                                                                HttpSession session) {
        getLoggedId(session);
        Slice<CommentDTO> replies = commentService.getCommentReplies(id,
                PageRequest.of(page, size));
        return new ResponseEntity<>(replies, HttpStatus.OK);
    }

    // POST - localhost:8080/comments/1/like
    // Like/Unlike comment
    @PostMapping("/comments/{id}/like")
    public ResponseEntity<Integer> likeComment(@PathVariable long id, HttpSession session) {
        int numberOfLikes = commentService.likePost(id, getLoggedId(session));
        return new ResponseEntity<>(numberOfLikes, HttpStatus.OK);
    }

    // POST - localhost:8080/posts/1/comments
    // Add comment
    @PostMapping("/posts/{id}/comments")
    public ResponseEntity<CommentDTO> addCommentToPost(
            @PathVariable("id") long postId,
            @RequestBody CommentContentDTO commentContentDTO,
            HttpSession session) {

        CommentDTO dto = commentService.addCommentToPost(getLoggedId(session), postId, commentContentDTO);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
    }

    // POST - localhost:8080/comments/1
    // Reply to a comment
    @PostMapping("/comments/{id}")
    public ResponseEntity<CommentDTO> replyToComment(
            @PathVariable("id") long parentId,
            @RequestBody CommentContentDTO commentContentDTO,
            HttpSession session) {

        CommentDTO replyDTO = commentService.replyToComment(getLoggedId(session),
                parentId, commentContentDTO);
        return new ResponseEntity<>(replyDTO, HttpStatus.CREATED);
    }

    // DELETE - localhost:8080/comments/1
    // Delete a comment
    @DeleteMapping("/comments/{id}")
    public ResponseEntity<CommentDTO> deleteComment(@PathVariable long id, HttpSession session) {


        CommentDTO dto = commentService.deleteComment(id, getLoggedId(session));
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
}