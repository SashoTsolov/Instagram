package bg.ittalents.instagram.media;

import bg.ittalents.instagram.exceptions.BadRequestException;
import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.post.DTOs.PostWithoutCommentsDTO;
import bg.ittalents.instagram.post.PostRepository;
import bg.ittalents.instagram.post.Post;
import bg.ittalents.instagram.post.PostService;
import jakarta.transaction.Transactional;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {


    private final PostRepository postRepository;
    private final MediaRepository mediaRepository;
    private final PostService postService;
    private final ModelMapper mediaMapper;

    public MediaService(PostRepository postRepository,
                        MediaRepository mediaRepository, PostService postService, ModelMapper mediaMapper) {
        this.postRepository = postRepository;
        this.mediaRepository = mediaRepository;
        this.postService = postService;
        this.mediaMapper = mediaMapper;
    }

    @SneakyThrows
    @Transactional
    public PostWithoutCommentsDTO upload(List<MultipartFile> files, long postId) {

        Post post = postRepository.findByIdAndIsCreatedIsFalse(postId)
                .orElseThrow(() -> new NotFoundException("Post doesn't exist"));

        if (post.getIsCreated()) {
            throw new BadRequestException("You cannot add media to an already created post!");
        }
        List<Media> allMedia = new ArrayList<>();
        for (MultipartFile file : files) {
            final String ext = FilenameUtils.getExtension(file.getOriginalFilename());
            final String name = UUID.randomUUID().toString() + "." + ext;
            final File dir = new File("upload_user_posts_media");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File f = new File(dir, name);
            Files.copy(file.getInputStream(), f.toPath());
            String url = dir.getName() + File.separator + f.getName();

            Media media = new Media();
            media.setMediaUrl(url);
            media.setPost(post);
            allMedia.add(media);
            post.getMediaUrls().add(media);
        }
        mediaRepository.saveAll(allMedia);
        postService.updatePostInfo(post);

        post.setIsCreated(true);
        postRepository.save(post);
        return mediaMapper.map(post, PostWithoutCommentsDTO.class);
    }

    public File download(String fileName) {
        File dir = new File("upload_user_posts_media");
        File file = new File(dir, fileName);
        if (file.exists()) {
            return file;
        }
        throw new NotFoundException("File not found");
    }
}
