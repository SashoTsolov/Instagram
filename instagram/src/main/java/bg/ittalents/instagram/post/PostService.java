package bg.ittalents.instagram.post;

import bg.ittalents.instagram.comment.Comment;
import bg.ittalents.instagram.comment.CommentRepository;
import bg.ittalents.instagram.comment.DTOs.CommentDTO;
import bg.ittalents.instagram.exception.NotFoundException;
import bg.ittalents.instagram.exception.UnauthorizedException;
import bg.ittalents.instagram.hashtag.Hashtag;
import bg.ittalents.instagram.hashtag.HashtagRepository;
import bg.ittalents.instagram.location.LocationRepository;
import bg.ittalents.instagram.media.Media;
import bg.ittalents.instagram.post.DTOs.CaptionDTO;
import bg.ittalents.instagram.post.DTOs.CreatePostDTO;
import bg.ittalents.instagram.post.DTOs.PostPreviewDTO;
import bg.ittalents.instagram.post.DTOs.PostWithCommentsDTO;
import bg.ittalents.instagram.post.DTOs.PostWithoutCommentsDTO;
import bg.ittalents.instagram.location.Location;
import bg.ittalents.instagram.user.User;
import bg.ittalents.instagram.user.UserRepository;
import bg.ittalents.instagram.util.AbstractService;
import com.amazonaws.services.s3.AmazonS3;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class PostService extends AbstractService {


    private final LocationRepository locationRepository;
    private final HashtagRepository hashtagRepository;
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public PostService(final UserRepository userRepository,
                       final JavaMailSender javaMailSender,
                       final ModelMapper mapper,
                       final AmazonS3 s3Client,
                       final @Value("${aws.s3.bucket}") String bucketName,
                       final LocationRepository locationRepository,
                       final HashtagRepository hashtagRepository,
                       final CommentRepository commentRepository,
                       final PostRepository postRepository) {
        super(userRepository, javaMailSender, mapper, s3Client, bucketName);
        this.locationRepository = locationRepository;
        this.hashtagRepository = hashtagRepository;
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public PostWithoutCommentsDTO addPost(final long userId, final CreatePostDTO createPostDTO) {

        final Optional<Location> location = locationRepository.findByName(createPostDTO.getLocation());

        Location newLocation = new Location();
        newLocation.setName(createPostDTO.getLocation());
        if (location.isEmpty()) {
            newLocation = locationRepository.save(newLocation);
        } else {
            newLocation = location.get();
        }
        final Post post = mapper.map(createPostDTO, Post.class);
        final User owner = getUserById(userId);
        post.setOwner(owner);
        post.setLocation(newLocation);
        post.setDateTimeCreated(LocalDateTime.now());
        post.setIsCreated(false);
        postRepository.save(post);

        return mapper.map(post, PostWithoutCommentsDTO.class);
    }

    @Transactional
    public void updatePostInfo(final Post post) {

        if (post.getHashtags() == null) {
            post.setHashtags(new HashSet<>());
        }
        if (post.getTaggedUsers() == null) {
            post.setTaggedUsers(new ArrayList<>());
        }

        post.getHashtags().forEach(hashtag -> hashtag.getPosts().remove(post));
        post.getHashtags().clear();
        post.getTaggedUsers().forEach(user -> user.getTaggedPosts().remove(post));
        post.getTaggedUsers().clear();

        final List<String> hashtags = findAllHashtags(post.getCaption());
        final List<String> userTags = findAllUserTags(post.getCaption());
        saveNewHashtags(hashtags, post);
        saveNewUserTags(userTags, post);

        postRepository.save(post);
    }

    public PostWithCommentsDTO getPostById(final long userId, final long postId, final Pageable pageable) {
        final Post post = findByIdAndIsCreatedIsTrue(userId, postId);
        return postToPostWithCommentsDTO(userId, post, pageable);
    }

    public Slice<PostPreviewDTO> getUserPostsById(final long userId, final long ownerId, final Pageable pageable) {
        final User owner = getUserById(ownerId);

        return postRepository.findByOwnerIdAndIsCreatedIsTrueOrderByDateTimeCreatedDesc(userId, owner.getId(), pageable)
                .map(post -> postToPostPreviewDTO(owner.getId(), post));
    }


    public Slice<PostPreviewDTO> searchPostsByHashtags(
            final long userId, final String searchString, final Pageable pageable) {

        final Hashtag hashtag = hashtagRepository.findByName(searchString)
                .orElseThrow(() -> new NotFoundException("No posts with this hashtag were found!"));

        return postRepository
                .findAllByHashtagNameSortedByDateTimeCreatedDesc(userId, hashtag.getName(), pageable)
                .map(post -> postToPostPreviewDTO(userId, post));
    }

    public Slice<PostPreviewDTO> searchPostsByLocation(
            final long userId, final String searchString, final Pageable pageable) {

        final Location location = locationRepository.findByName(searchString)
                .orElseThrow(() -> new NotFoundException("No posts with this location were found!"));

        return postRepository
                .findAllByLocationNameSortedByDateTimeCreatedDesc(userId, location.getName(), pageable)
                .map(post -> postToPostPreviewDTO(userId, post));

    }

    public String deletePost(final long userId, final long postId) {
        final Post post = postRepository.findById(postId).
                orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        if (post.getOwner().getId() != userId) {
            throw new UnauthorizedException("You can't delete post that is not yours!");
        }
        deleteMediaFiles(post);
        postRepository.delete(post);
        return "Post " + post.getId() + " deleted successfully!";
    }


    public String updateCaption(final long userId, final long postId, final CaptionDTO caption) {

        final Post post = findByIdAndIsCreatedIsTrue(userId, postId);

        if (post.getOwner().getId() != userId) {
            throw new UnauthorizedException("You can't edit post that is not yours!");
        }

        post.setCaption(caption.getCaption());
        updatePostInfo(post);
        return "Caption of post " + post.getId() + " updated Successfully!";
    }

    @Transactional
    public int likePost(final long userId, final long postId) {

        final User user = getUserById(userId);
        final Post post = findByIdAndIsCreatedIsTrue(userId, postId);

        if (user.getLikedPosts().contains(post) || post.getLikedBy().contains(user)) {
            user.getLikedPosts().remove(post);
            post.getLikedBy().remove(user);
        } else {
            user.getLikedPosts().add(post);
            post.getLikedBy().add(user);
        }

        userRepository.save(user);
        postRepository.save(post);
        return post.getLikedBy().size();
    }

    @Transactional
    public String savePost(final long userId, final long postId) {

        final User user = getUserById(userId);
        final Post post = findByIdAndIsCreatedIsTrue(userId, postId);

        if (user.getSavedPosts().contains(post) || post.getSavedBy().contains(user)) {
            user.getSavedPosts().remove(post);
            post.getSavedBy().remove(user);
        } else {
            user.getSavedPosts().add(post);
            post.getSavedBy().add(user);
        }

        userRepository.save(user);
        return "Saved Successfully!";
    }

    public Slice<PostPreviewDTO> getUserSavedPosts(final long userId, final Pageable pageable) {

        return postRepository.findAllSavedByUserIdOrderByUploadDateDesc(userId, pageable)
                .map(post -> postToPostPreviewDTO(userId, post));
    }

    public Slice<PostPreviewDTO> getUserTaggedPostsById(
            final long userId, final long ownerId, final Pageable pageable) {

        final User owner = getUserById(ownerId);

        return postRepository.findAllOwnerTaggedOrderByUploadDateDesc(userId, owner.getId(), pageable)
                .map(post -> postToPostPreviewDTO(owner.getId(), post));
    }

    public Slice<PostWithoutCommentsDTO> getPostsFromFeed(final long userId, final Pageable pageable) {

        return postRepository.findAllByUserFollowersOrderByUploadDateDesc(userId, pageable)
                .map(post -> postToPostWithoutCommentsDTO(userId, post));
    }

    private List<String> findAllUserTags(final String input) {

        final Pattern pattern = Pattern.compile("@\\w+");
        final Matcher matcher = pattern.matcher(input);
        final List<String> userTags = new ArrayList<>();

        while (matcher.find()) {
            userTags.add(matcher.group().substring(1));
        }

        return userTags;
    }

    private List<String> findAllHashtags(final String input) {
        final Pattern pattern = Pattern.compile("\\B#\\w+");
        final Matcher matcher = pattern.matcher(input);

        final List<String> hashtags = new ArrayList<>();

        while (matcher.find()) {
            hashtags.add(matcher.group().substring(1));
        }

        return hashtags;
    }


    private void saveNewHashtags(final List<String> hashtags, final Post post) {
        if (!hashtags.isEmpty()) {
            final List<Hashtag> hashtagsToSave = new ArrayList<>();
            for (String hashtagName : hashtags) {
                final Hashtag hashtag;
                if (hashtagRepository.existsByName(hashtagName)) {
                    hashtag = hashtagRepository.findByName(hashtagName).orElseThrow();
                } else {
                    hashtag = new Hashtag();
                    hashtag.setName(hashtagName);
                    hashtag.setPosts(new HashSet<>());
                    hashtagsToSave.add(hashtag);
                }
                hashtag.getPosts().add(post);
                post.getHashtags().add(hashtag);
            }
            hashtagRepository.saveAll(hashtagsToSave);
        }
    }

    private void saveNewUserTags(final List<String> userTags, final Post post) {
        if (!userTags.isEmpty()) {
            final List<User> usersToSave = new ArrayList<>();
            for (String userTag : userTags) {
                if (userRepository.existsByUsername(userTag)) {
                    final User user = userRepository.findByUsername(userTag)
                            .orElseThrow(() -> new NotFoundException("The user doesn't exist"));
                    if (user.getTaggedPosts() == null) {
                        user.setTaggedPosts(new ArrayList<>());
                    }
                    user.getTaggedPosts().add(post);
                    post.getTaggedUsers().add(user);
                    usersToSave.add(user);
                }
            }
            userRepository.saveAll(usersToSave);
        }
    }

    private Post findByIdAndIsCreatedIsTrue(final long userId, final long postId) {
        return postRepository.findByIdAndIsCreatedIsTrue(userId, postId).
                orElseThrow(() -> new NotFoundException("The post doesn't exist"));
    }

    private PostPreviewDTO postToPostPreviewDTO(final long userId, final Post post) {
        final PostPreviewDTO dto = mapper.map(post, PostPreviewDTO.class);
        dto.setNumberOfLikes(post.getLikedBy().size());
        dto.setNumberOfComments(commentRepository.countAllByPostId(userId, post.getId()));
        return dto;
    }

    private PostWithoutCommentsDTO postToPostWithoutCommentsDTO(final long userId, final Post post) {
        final PostWithoutCommentsDTO dto = mapper.map(post, PostWithoutCommentsDTO.class);
        dto.setNumberOfLikes(post.getLikedBy().size());
        dto.setNumberOfComments(commentRepository.countAllByPostId(userId, post.getId()));
        return dto;
    }


    private PostWithCommentsDTO postToPostWithCommentsDTO(final long userId, Post post, final Pageable pageable) {
        final PostWithCommentsDTO dto = mapper.map(post, PostWithCommentsDTO.class);
        dto.setNumberOfLikes(post.getLikedBy().size());
        dto.setNumberOfComments(commentRepository.countAllByPostId(userId, post.getId()));

        final Slice<Comment> comments = commentRepository.findAllParentCommentsByPostIdOrderByNumberOfLikes(
                userId,
                post.getId(),
                pageable);
        final List<CommentDTO> commentDTOs = comments.getContent().stream()
                .map(comment -> {
                    final CommentDTO commentDTO = mapper.map(comment, CommentDTO.class);
                    commentDTO.setNumberOfLikes(comment.getLikedBy().size());
                    commentDTO.setNumberOfReplies(comment.getReplies().size());
                    return commentDTO;
                })
                .collect(Collectors.toList());

        dto.setComments(commentDTOs);
        return dto;
    }

    private void deleteMediaFiles(final Post post) {
        final List<Media> mediaList = post.getMediaUrls();
        for (Media media : mediaList) {
            final String mediaUrl = media.getMediaUrl();
            final String objectKey = mediaUrl.substring(mediaUrl.lastIndexOf("/") + 1);
            s3Client.deleteObject(bucketName, objectKey);
        }
    }
}
