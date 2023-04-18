package bg.ittalents.instagram.comment;

import bg.ittalents.instagram.comment.DTOs.CommentContentDTO;
import bg.ittalents.instagram.comment.DTOs.CommentWithoutRepliesDTO;
import bg.ittalents.instagram.util.AbstractController;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
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
    public ResponseEntity<Page<CommentWithoutRepliesDTO>> viewCommentReplies(@RequestParam int page,
                                                                             @RequestParam int size,
                                                                             @PathVariable long id,
                                                                             HttpSession session) {
        getLoggedId(session);
        Page<CommentWithoutRepliesDTO> replies = commentService.getCommentReplies(id,
                PageRequest.of(page, size));
        return new ResponseEntity<>(replies, HttpStatus.OK);
        //TODO: implement method to like a comment with the given commentId
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
    public ResponseEntity<CommentWithoutRepliesDTO> addCommentToPost(
            @PathVariable("id") long postId,
            @RequestBody CommentContentDTO commentContentDTO,
            HttpSession session) {

        CommentWithoutRepliesDTO dto = commentService.addCommentToPost(getLoggedId(session), postId, commentContentDTO);
        return new ResponseEntity<>(dto, HttpStatus.CREATED);
        //TODO: implement method to add the given comment to the post with the given postId
    }

    // POST - localhost:8080/comments/1
    // Reply to a comment
    @PostMapping("/comments/{id}")
    public void replyToComment(@PathVariable("id") long parentId, @RequestBody Comment comment) {
        //TODO: implement method to create a new comment as a child of the comment with the given parentId
    }

    // DELETE - localhost:8080/comments/1
    // Delete a comment
    @DeleteMapping("/comments/{id}")
    public void deleteComment(@PathVariable("id") long commentId) {
        //TODO: implement method to delete the comment with the given commentId
    }
}