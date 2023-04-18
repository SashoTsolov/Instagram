package bg.ittalents.instagram.comment;

import bg.ittalents.instagram.comment.DTOs.CommentContentDTO;
import bg.ittalents.instagram.comment.DTOs.CommentWithoutRepliesDTO;
import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.post.Post;
import bg.ittalents.instagram.post.PostRepository;
import bg.ittalents.instagram.user.User;
import bg.ittalents.instagram.user.UserRepository;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    public Page<CommentWithoutRepliesDTO> getCommentReplies(long id, Pageable pageable) {

        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("The comment doesn't exist"));

        return commentRepository.findByParentIdOrderByNumberOfLikes(
                comment.getId(),
                pageable).map(com -> commentToCommentWithoutRepliesDTO(com));
    }

    private CommentWithoutRepliesDTO commentToCommentWithoutRepliesDTO(Comment comment) {
        CommentWithoutRepliesDTO dto = mapper.map(comment, CommentWithoutRepliesDTO.class);
        dto.setNumberOfLikes(comment.getLikedBy().size());
        dto.setNumberOfReplies(comment.getReplies().size());
        return dto;
    }

    public CommentWithoutRepliesDTO addCommentToPost(long userId, long postId, CommentContentDTO commentContentDTO) {

        User owner = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        Comment comment = mapper.map(commentContentDTO, Comment.class);
        comment.setPost(post);
        comment.setDateTimeCreated(LocalDateTime.now());
        comment.setOwner(owner);
        return mapper.map(commentRepository.save(comment), CommentWithoutRepliesDTO.class);
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
}
