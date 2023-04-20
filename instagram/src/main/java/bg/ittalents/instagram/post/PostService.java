package bg.ittalents.instagram.post;

import bg.ittalents.instagram.comment.Comment;
import bg.ittalents.instagram.comment.CommentRepository;
import bg.ittalents.instagram.comment.DTOs.CommentDTO;
import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.hashtag.Hashtag;
import bg.ittalents.instagram.hashtag.HashtagRepository;
import bg.ittalents.instagram.location.LocationRepository;
import bg.ittalents.instagram.post.DTOs.CaptionDTO;
import bg.ittalents.instagram.post.DTOs.CreatePostDTO;
import bg.ittalents.instagram.post.DTOs.PostPreviewDTO;
import bg.ittalents.instagram.post.DTOs.PostWithCommentsDTO;
import bg.ittalents.instagram.post.DTOs.PostWithoutCommentsDTO;
import bg.ittalents.instagram.location.Location;
import bg.ittalents.instagram.user.User;
import bg.ittalents.instagram.util.AbstractService;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;

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

    public PostService(LocationRepository locationRepository,
                       HashtagRepository hashtagRepository, CommentRepository commentRepository) {
        this.locationRepository = locationRepository;
        this.hashtagRepository = hashtagRepository;
        this.commentRepository = commentRepository;
    }

    @Transactional
    public PostWithoutCommentsDTO addPost(long userId, CreatePostDTO createPostDTO) {

        Optional<Location> location = locationRepository.findByName(createPostDTO.getLocation());

        Location newLocation = new Location();
        newLocation.setName(createPostDTO.getLocation());
        if (location.isEmpty()) {
            newLocation = locationRepository.save(newLocation);
        } else {
            newLocation = location.get();
        }
        Post post = mapper.map(createPostDTO, Post.class);
        User owner = getUserById(userId);
        post.setOwner(owner);
        post.setLocation(newLocation);
        post.setDateTimeCreated(LocalDateTime.now());
        post.setIsCreated(false);
        // TODO
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

        List<String> hashtags = findAllHashtags(post.getCaption());
        List<String> userTags = findAllUserTags(post.getCaption());
        saveNewHashtags(hashtags, post);
        saveNewUserTags(userTags, post);

        postRepository.save(post);
    }

    private List<String> findAllUserTags(String input) {

        Pattern pattern = Pattern.compile("@\\w+");
        Matcher matcher = pattern.matcher(input);
        List<String> userTags = new ArrayList<>();

        while (matcher.find()) {
            userTags.add(matcher.group().substring(1));
        }

        return userTags;
    }

    private List<String> findAllHashtags(String input) {
        Pattern pattern = Pattern.compile("\\B#\\w+");
        Matcher matcher = pattern.matcher(input);

        List<String> hashtags = new ArrayList<>();

        while (matcher.find()) {
            hashtags.add(matcher.group().substring(1));
        }

        return hashtags;
    }

    private void saveNewHashtags(List<String> hashtags, Post post) {
        if (!hashtags.isEmpty()) {
            List<Hashtag> hashtagsToSave = new ArrayList<>();
            for (String hashtagName : hashtags) {
                Hashtag hashtag;
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

    private void saveNewUserTags(List<String> userTags, Post post) {
        if (!userTags.isEmpty()) {
            List<User> usersToSave = new ArrayList<>();
            for (String userTag : userTags) {
                if (userRepository.existsByUsername(userTag)) {
                    User user = userRepository.findByUsername(userTag)
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

    public PostWithCommentsDTO getPostById(final long userId, long postId, Pageable pageable) {
        Post post = postRepository.findByIdAndIsCreatedIsTrue(userId, postId).
                orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        return postToPostWithCommentsDTO(userId, post, pageable);
    }

    public Slice<PostPreviewDTO> getUserPostsById(long userId, long ownerId, Pageable pageable) {
        final User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        return postRepository.findByOwnerIdAndIsCreatedIsTrueOrderByDateTimeCreatedDesc(userId, owner.getId(), pageable)
                .map(post -> postToPostPreviewDTO(owner.getId(), post));
    }


    private PostWithCommentsDTO postToPostWithCommentsDTO(long userId, Post post, Pageable pageable) {
        PostWithCommentsDTO dto = mapper.map(post, PostWithCommentsDTO.class);
        dto.setNumberOfLikes(post.getLikedBy().size());
        dto.setNumberOfComments(commentRepository.countAllByPostId(userId, post.getId()));

        Slice<Comment> comments = commentRepository.findAllParentCommentsByPostIdOrderByNumberOfLikes(
                userId,
                post.getId(),
                pageable);
        List<CommentDTO> commentDTOs = comments.getContent().stream()
                .map(comment -> {
                    CommentDTO commentDTO = mapper.map(comment, CommentDTO.class);
                    commentDTO.setNumberOfLikes(comment.getLikedBy().size());
                    commentDTO.setNumberOfReplies(comment.getReplies().size());
                    return commentDTO;
                })
                .collect(Collectors.toList());

        dto.setComments(commentDTOs);
        return dto;
    }

    private PostPreviewDTO postToPostPreviewDTO(long userId, Post post) {
        PostPreviewDTO dto = mapper.map(post, PostPreviewDTO.class);
        dto.setNumberOfLikes(post.getLikedBy().size());
        dto.setNumberOfComments(commentRepository.countAllByPostId(userId, post.getId()));
        return dto;
    }

    private PostWithoutCommentsDTO postToPostWithoutCommentsDTO(long userId, Post post) {
        PostWithoutCommentsDTO dto = mapper.map(post, PostWithoutCommentsDTO.class);
        dto.setNumberOfLikes(post.getLikedBy().size());
        dto.setNumberOfComments(commentRepository.countAllByPostId(userId, post.getId()));
        return dto;
    }


    public Slice<PostPreviewDTO> searchPostsByHashtags(final long userId, String searchString, Pageable pageable) {

        Hashtag hashtag = hashtagRepository.findByName(searchString)
                .orElseThrow(() -> new NotFoundException("No posts with this hashtag were found!"));

        return postRepository
                .findAllByHashtagNameSortedByDateTimeCreatedDesc(userId, hashtag.getName(), pageable)
                .map(post -> postToPostPreviewDTO(userId, post));
    }

    public Slice<PostPreviewDTO> searchPostsByLocation(final long userId, String searchString, Pageable pageable) {

        Location location = locationRepository.findByName(searchString)
                .orElseThrow(() -> new NotFoundException("No posts with this location were found!"));

        return postRepository
                .findAllByLocationNameSortedByDateTimeCreatedDesc(userId, location.getName(), pageable)
                .map(post -> postToPostPreviewDTO(userId, post));

    }

    public String deletePost(long userId, long postId) {
        Post post = postRepository.findByIdAndIsCreatedIsFalse(postId).
                orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        if (post.getOwner().getId() != userId) {
            throw new UnauthorizedException("You can't delete post that is not yours!");
        }
        postRepository.delete(post);
        return "Post " + post.getId() + " deleted successfully!";
//        return "postToPostWithCommentsDTO(post)";
    }


    public String updateCaption(long userId, long postId, CaptionDTO caption) {

        Post post = postRepository.findByIdAndIsCreatedIsTrue(userId, postId).
                orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        if (post.getOwner().getId() != userId) {
            throw new UnauthorizedException("You can't edit post that is not yours!");
        }

        post.setCaption(caption.getCaption());
        updatePostInfo(post);
        return "Caption of post " + post.getId() + " updated Successfully!";
//        return postToPostWithCommentsDTO(post);
    }

    @Transactional
    public int likePost(long userId, long postId) {

        User user = userRepository.findById(userId).
                orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        Post post = postRepository.findByIdAndIsCreatedIsTrue(userId, postId).
                orElseThrow(() -> new NotFoundException("The post doesn't exist"));

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
    public String savePost(long userId, long postId) {

        User user = userRepository.findById(userId).
                orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        Post post = postRepository.findByIdAndIsCreatedIsTrue(userId, postId).
                orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        if (user.getSavedPosts().contains(post) || post.getSavedBy().contains(user)) {
            user.getSavedPosts().remove(post);
            post.getSavedBy().remove(user);
        } else {
            user.getSavedPosts().add(post);
            post.getSavedBy().add(user);
        }

        userRepository.save(user);
        return "Saved Successfully!";
//        return postToPostWithCommentsDTO(post);
    }

    public Slice<PostPreviewDTO> getUserSavedPosts(final long userId, Pageable pageable) {

        return postRepository.findAllSavedByUserIdOrderByUploadDateDesc(userId, pageable)
                .map(post -> postToPostPreviewDTO(userId, post));
    }

    public Slice<PostPreviewDTO> getUserTaggedPostsById(long userId, long ownerId, Pageable pageable) {

        final User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        return postRepository.findAllOwnerTaggedOrderByUploadDateDesc(userId, owner.getId(), pageable)
                .map(post -> postToPostPreviewDTO(owner.getId(), post));
    }

    public Slice<PostWithoutCommentsDTO> getPostsFromFeed(final long userId, Pageable pageable) {

        return postRepository.findAllByUserFollowersOrderByUploadDateDesc(userId, pageable)
                .map(post -> postToPostWithoutCommentsDTO(userId, post));
    }
}
