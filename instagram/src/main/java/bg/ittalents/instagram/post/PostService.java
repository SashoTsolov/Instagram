package bg.ittalents.instagram.post;

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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    LocationRepository locationRepository;
    @Autowired
    HashtagRepository hashtagRepository;


    @Transactional
    public PostWithoutCommentsDTO addPost(CreatePostDTO createPostDTO, Long loggedId) {

        Optional<Location> location = locationRepository.findByName(createPostDTO.getLocation());

        Location newLocation = newLocation = new Location();
        newLocation.setName(createPostDTO.getLocation());
        if (location.isEmpty()) {
            newLocation = locationRepository.save(newLocation);
        } else {
            newLocation = location.get();
        }
        Post post = mapper.map(createPostDTO, Post.class);
        User owner = getUserById(loggedId);
        post.setOwner(owner);
        post.setIsStory(false);
        post.setLocation(newLocation);
        post.setDateTimeCreated(LocalDateTime.now());
        post.setIsCreated(false);
        List<String> hashtags = findAllHashtags(createPostDTO.getCaption());
        saveNewHashtags(hashtags, post);
        postRepository.save(post);

        return mapper.map(post, PostWithoutCommentsDTO.class);
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
            if (post.getHashtags() == null) {
                post.setHashtags(new HashSet<>());
            }
            for (String hashtagName : hashtags) {
                Hashtag hashtag;
                if (hashtagRepository.existsByName(hashtagName)) {
                    hashtag = hashtagRepository.findByName(hashtagName).orElseThrow();
                } else {
                    hashtag = new Hashtag();
                    hashtag.setName(hashtagName);
                    hashtag.setPosts(new HashSet<>());
                    hashtagRepository.save(hashtag);
                }
                hashtag.getPosts().add(post);
                post.getHashtags().add(hashtag);
            }
        }
    }

    public PostWithCommentsDTO getPostById(Long id) {
        Post post = postRepository.findById(id).
                orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        return mapper.map(post, PostWithCommentsDTO.class);
    }

    public List<PostPreviewDTO> getUserPostsById(Long id) {

        User user = userRepository.findById(id).
                orElseThrow(() -> new NotFoundException("The user doesn't exist"));

        List<Post> posts = postRepository.findByOwnerIdOrderByUploadDateDesc(user.getId());
        return posts.stream()
                .map(post -> mapper.map(post, PostPreviewDTO.class))
                .collect(Collectors.toList());
    }

    public Page<PostPreviewDTO> searchPostsByHashtags(String searchString, Pageable pageable) {

        Hashtag hashtag = hashtagRepository.findByName(searchString)
                .orElseThrow(() -> new NotFoundException("No posts with this hashtag were found!"));

        return postRepository
                .findByHashtagNameSortedByDateTimeCreatedDesc(hashtag.getName(), pageable)
                .map(post -> mapper.map(post, PostPreviewDTO.class));
    }

    public Page<PostPreviewDTO> searchPostsByLocation(String searchString, Pageable pageable) {

        Location location = locationRepository.findByName(searchString)
                .orElseThrow(() -> new NotFoundException("No posts with this location were found!"));

        return postRepository
                .findByLocationNameSortedByDateTimeCreatedDesc(location.getName(), pageable)
                .map(post -> mapper.map(post, PostPreviewDTO.class));

    }

    public PostWithoutCommentsDTO deletePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId).
                orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        if (post.getOwner().getId() != userId) {
            throw new UnauthorizedException("You can't delete post that is not yours!");
        }
        postRepository.delete(post);
        return mapper.map(post, PostWithoutCommentsDTO.class);
    }

    public PostWithoutCommentsDTO updateCaption(Long postId, CaptionDTO caption, Long userId) {

        Post post = postRepository.findById(postId).
                orElseThrow(() -> new NotFoundException("The post doesn't exist"));

        if (post.getOwner().getId() != userId) {
            throw new UnauthorizedException("You can't edit post that is not yours!");
        }

        post.setCaption(caption.getCaption());
        return mapper.map(postRepository.save(post), PostWithoutCommentsDTO.class);
    }


}
