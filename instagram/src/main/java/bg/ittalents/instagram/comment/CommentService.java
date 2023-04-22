package bg.ittalents.instagram.comment;

import bg.ittalents.instagram.comment.DTOs.CommentContentDTO;
import bg.ittalents.instagram.comment.DTOs.CommentDTO;
import bg.ittalents.instagram.exception.NotFoundException;
import bg.ittalents.instagram.exception.UnauthorizedException;
import bg.ittalents.instagram.post.Post;
import bg.ittalents.instagram.post.PostRepository;
import bg.ittalents.instagram.user.User;
import bg.ittalents.instagram.user.UserRepository;
import bg.ittalents.instagram.util.AbstractService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CommentService extends AbstractService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final ModelMapper mapper;

    public CommentService(UserRepository userRepository,
                          ModelMapper mapper,
                          CommentRepository commentRepository,
                          PostRepository postRepository,
                          UserRepository userRepository1,
                          ModelMapper mapper1) {
        super(userRepository, mapper);
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository1;
        this.mapper = mapper1;
    }

    public Slice<CommentDTO> getCommentReplies(final long userId, final long commentId, final Pageable pageable) {

        final Comment c = findCommentById(userId, commentId);

        return commentRepository.findAllRepliesByParentIdOrderByNumberOfLikes(
                userId,
                c.getId(),
                pageable).map(comment -> commentToCommentDTO(comment));
    }

    public CommentDTO addCommentToPost(final long userId, final long postId,
                                       final CommentContentDTO commentContentDTO) {

        final User owner = getUserById(userId);
        final Post post = postRepository.findByIdAndIsCreatedIsTrue(userId, postId)
                .orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        final Comment comment = mapper.map(commentContentDTO, Comment.class);
        comment.setPost(post);
        comment.setDateTimeCreated(LocalDateTime.now());
        comment.setOwner(owner);
        return mapper.map(commentRepository.save(comment), CommentDTO.class);
    }

    @Transactional
    public int likePost(final long userId, final long commentId) {

        final User user = getUserById(userId);
        final Comment comment = findCommentById(userId, commentId);

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

    public CommentDTO deleteComment(final long userId, final long commentId) {

        final Comment comment = findCommentById(userId, commentId);

        if (comment.getOwner().getId() != userId) {
            throw new UnauthorizedException("You can't delete post that is not yours!");
        }

        final CommentDTO dto = commentToCommentDTO(comment);
        commentRepository.delete(comment);
        return dto;
    }

    public CommentDTO commentToCommentDTO(final Comment comment) {
        final CommentDTO dto = mapper.map(comment, CommentDTO.class);
        dto.setNumberOfLikes(comment.getLikedBy().size());
        dto.setNumberOfReplies(comment.getReplies().size());
        return dto;
    }

    public CommentDTO replyToComment(final long userId, final long parentId,
                                     final CommentContentDTO commentContentDTO) {

        final User owner = getUserById(userId);
        final Comment parentComment = commentRepository.findById(parentId)
                .orElseThrow(() -> new NotFoundException("The comment doesn't exist"));

        final Comment comment = mapper.map(commentContentDTO, Comment.class);
        comment.setPost(parentComment.getPost());
        comment.setDateTimeCreated(LocalDateTime.now());
        comment.setOwner(owner);
        comment.setParent(parentComment);
        return mapper.map(commentRepository.save(comment), CommentDTO.class);
    }

    public Slice<CommentDTO> getPostComments(final long userId, final long postId, final Pageable pageable) {

        postRepository.findByIdAndIsCreatedIsTrue(userId, postId)
                .orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        return commentRepository.findAllParentCommentsByPostIdOrderByNumberOfLikes(userId, postId, pageable)
                .map(comment -> commentToCommentDTO(comment));
    }

    private Comment findCommentById(final long userId, final long commentId) {
        return commentRepository.findCommentById(userId, commentId)
                .orElseThrow(() -> new NotFoundException("The comment doesn't exist"));
    }
}
