package bg.ittalents.instagram.comment;

import bg.ittalents.instagram.util.AbstractController;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CommentController extends AbstractController {


    // POST - localhost:8080/comments/1/like
    // Like/Unlike comment
    @PostMapping("/comments/{id}/like")
    public void likeComment(@PathVariable("id") long commentId) {
        //TODO: implement method to like a comment with the given commentId
    }

    // POST - localhost:8080/posts/1/comments
    // Add comment
    @PostMapping("/posts/{id}/comments")
    public void addCommentToPost(@PathVariable("id") long postId, @RequestBody Comment comment) {
        //TODO: implement method to add the given comment to the post with the given postId
    }

    // POST - localhost:8080/comments/1
    // Reply to a comment
    @PostMapping("/comments/{id}")
    public void createComment(@PathVariable("id") long parentId, @RequestBody Comment comment) {
        //TODO: implement method to create a new comment as a child of the comment with the given parentId
    }

    // DELETE - localhost:8080/comments/1
    // Delete a comment
    @DeleteMapping("/comments/{id}")
    public void deleteComment(@PathVariable("id") long commentId) {
        //TODO: implement method to delete the comment with the given commentId
    }
}