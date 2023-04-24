package bg.ittalents.instagram.media;

import bg.ittalents.instagram.exception.BadRequestException;
import bg.ittalents.instagram.post.DTOs.PostWithoutCommentsDTO;
import bg.ittalents.instagram.post.PostRepository;
import bg.ittalents.instagram.post.Post;
import bg.ittalents.instagram.post.PostService;
import bg.ittalents.instagram.user.UserRepository;
import bg.ittalents.instagram.util.AbstractService;
import com.amazonaws.services.s3.AmazonS3;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class MediaService extends AbstractService {


    private final PostRepository postRepository;
    private final MediaRepository mediaRepository;
    private final PostService postService;

    public MediaService(final UserRepository userRepository,
                        final JavaMailSender javaMailSender,
                        final ModelMapper mapper,
                        final AmazonS3 s3Client,
                        final @Value("${aws.s3.bucket}") String bucketName,
                        final PostRepository postRepository,
                        final MediaRepository mediaRepository,
                        final PostService postService) {
        super(userRepository, javaMailSender, mapper, s3Client, bucketName);
        this.postRepository = postRepository;
        this.mediaRepository = mediaRepository;
        this.postService = postService;
    }

    @SneakyThrows
    @Transactional
    public PostWithoutCommentsDTO uploadMediaToPost(final List<MultipartFile> files, final long postId) {

        final Post post = postRepository.findByIdAndIsCreatedIsFalse(postId)
                .orElseThrow(() -> new BadRequestException("You can't add media to this post!"));

        if (post.getIsCreated()) {
            throw new BadRequestException("You cannot add media to an already created post!");
        }
        final List<Media> allMedia = new ArrayList<>();
        for (MultipartFile currentFile : files) {
            final String ext = FilenameUtils.getExtension(currentFile.getOriginalFilename());
            if (!Arrays.asList("jpg", "jpeg", "png", "mp4").contains(ext)) {
                throw new BadRequestException("File type not supported. " +
                        "Only JPG, JPEG, PNG, and MP4 formats are allowed.");
            }

            final String url = uploadMedia(currentFile, ext);
            final Media media = new Media();
            media.setMediaUrl(url);
            media.setPost(post);
            allMedia.add(media);
        }
        mediaRepository.saveAll(allMedia);
        post.setIsCreated(true);
        postService.updatePostInfo(post);
        return mapper.map(post, PostWithoutCommentsDTO.class);
    }
}
