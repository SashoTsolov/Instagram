package bg.ittalents.instagram.comment;

import bg.ittalents.instagram.comment.DTOs.CommentContentDTO;
import bg.ittalents.instagram.comment.DTOs.CommentDTO;
import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.post.Post;
import bg.ittalents.instagram.post.PostRepository;
import bg.ittalents.instagram.user.User;
import bg.ittalents.instagram.user.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommentService {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ModelMapper mapper;

    public Slice<CommentDTO> getCommentReplies(long id, Pageable pageable) {

        Comment c = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The comment doesn't exist"));

        return commentRepository.findRepliesByParentIdOrderByNumberOfLikes(
                c.getId(),
                pageable).map(comment -> commentToCommentDTO(comment));
    }

    public CommentDTO addCommentToPost(long userId, long postId, CommentContentDTO commentContentDTO) {

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        Comment comment = mapper.map(commentContentDTO, Comment.class);
        comment.setPost(post);
        comment.setDateTimeCreated(LocalDateTime.now());
        comment.setOwner(owner);
        return mapper.map(commentRepository.save(comment), CommentDTO.class);
    }

    @Transactional
    public int likePost(long commentId, long loggedId) {
        User user = userRepository.findById(loggedId).
                orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        Comment comment = commentRepository.findById(commentId).
                orElseThrow(() -> new NotFoundException("The comment doesn't exist"));

        if (user.getLikedComments().contains(comment) || comment.getLikedBy().contains(user)) {
            user.getLikedComments().remove(comment);
            comment.getLikedBy().remove(user);
        } else {
            user.getLikedComments().add(comment);
            comment.getLikedBy().add(user);
        }

        userRepository.save(user);
        commentRepository.save(comment);
        return comment.getLikedBy().size();
    }

    public CommentDTO deleteComment(long commentId, long userId) {

        Comment comment = commentRepository.findById(commentId).
                orElseThrow(() -> new NotFoundException("The comment doesn't exist"));

        if (comment.getOwner().getId() != userId) {
            throw new UnauthorizedException("You can't delete post that is not yours!");
        }

        CommentDTO dto = commentToCommentDTO(comment);
        commentRepository.delete(comment);
        return dto;
    }

    public CommentDTO commentToCommentDTO(Comment comment) {
        CommentDTO dto = mapper.map(comment, CommentDTO.class);
        dto.setNumberOfLikes(comment.getLikedBy().size());
        dto.setNumberOfReplies(comment.getReplies().size());
        return dto;
    }

    public CommentDTO replyToComment(long loggedId, long parentId,
                                     CommentContentDTO commentContentDTO) {

        User owner = userRepository.findById(loggedId)
                .orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("The comment doesn't exist"));

        Comment comment = mapper.map(commentContentDTO, Comment.class);
        comment.setPost(parentComment.getPost());
        comment.setDateTimeCreated(LocalDateTime.now());
        comment.setOwner(owner);
        comment.setParent(parentComment);
        return mapper.map(commentRepository.save(comment), CommentDTO.class);
    }
}
