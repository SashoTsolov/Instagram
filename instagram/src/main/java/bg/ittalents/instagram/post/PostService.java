package bg.ittalents.instagram.post;

import bg.ittalents.instagram.hashtag.Hashtag;
import bg.ittalents.instagram.hashtag.HashtagRepository;
import bg.ittalents.instagram.location.LocationRepository;
import bg.ittalents.instagram.post.DTOs.CreatePostDTO;
import bg.ittalents.instagram.post.DTOs.PostWithoutCommentsDTO;
import bg.ittalents.instagram.location.Location;
import bg.ittalents.instagram.user.User;
import bg.ittalents.instagram.util.AbstractService;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class PostService extends AbstractService {

//    private PostMapper postMapper;

    @Autowired
    LocationRepository locationRepository;
    @Autowired
    HashtagRepository hashtagRepository;

    @Autowired
    ModelMapper postMapper;



    public PostWithoutCommentsDTO addPost(CreatePostDTO createPostDTO, Long loggedId) {

        Optional<Location> location = locationRepository.findByName(createPostDTO.getLocation());

        Location newLocation = newLocation = new Location();
        newLocation.setName(createPostDTO.getLocation());
        if (location.isEmpty()) {
            newLocation = locationRepository.save(newLocation);
        } else {
            newLocation = location.get();
        }
        Post post = postMapper.map(createPostDTO, Post.class);
        //Post post = postMapper.INSTANCE.createPostDtoToPost(createPostDTO);
        User owner = getUserById(loggedId);
        post.setOwner(owner);
        post.setIsStory(false);
        post.setLocation(newLocation);
        post.setDateTimeCreated(LocalDateTime.now());
        post.setIsCreated(false);
        List<String> hashtags = findAllHashtags(createPostDTO.getCaption());
        saveNewHashtags(hashtags, post);
        postRepository.save(post);

        return postMapper.map(post, PostWithoutCommentsDTO.class);
//        return postMapper.INSTANCE.postToPostWithoutCommentsDto(postRepository.save(post));
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
}
