package bg.ittalents.instagram.media;

import bg.ittalents.instagram.exception.BadRequestException;
import bg.ittalents.instagram.exception.NotFoundException;
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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {


    private final PostRepository postRepository;
    private final MediaRepository mediaRepository;
    private final PostService postService;
    private final ModelMapper mapper;

    public MediaService(PostRepository postRepository,
                        MediaRepository mediaRepository, PostService postService, ModelMapper mapper) {
        this.postRepository = postRepository;
        this.mediaRepository = mediaRepository;
        this.postService = postService;
        this.mapper = mapper;
    }

    @SneakyThrows
    @Transactional
    public PostWithoutCommentsDTO upload(final List<MultipartFile> files, final long postId) {

        final Post post = postRepository.findByIdAndIsCreatedIsFalse(postId)
                .orElseThrow(() -> new BadRequestException("You can't add media to this post!"));

        if (post.getIsCreated()) {
            throw new BadRequestException("You cannot add media to an already created post!");
        }
        final List<Media> allMedia = new ArrayList<>();
        for (MultipartFile currentFile : files) {
            final String ext = FilenameUtils.getExtension(currentFile.getOriginalFilename());
            if (!Arrays.asList("jpg", "jpeg", "png", "mp4").contains(ext)) {
                throw new BadRequestException("File type not supported. Only JPG, JPEG, PNG, and MP4 formats are allowed.");
            }
            final String name = UUID.randomUUID().toString() + "." + ext;
            final File dir = new File("uploads_user_posts_media");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            final File file = new File(dir, name);
            Files.copy(currentFile.getInputStream(), file.toPath());
            final String url = dir.getName() + File.separator + file.getName();

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


    public File download(final String fileName) {
        final File dir = new File("uploads_user_posts_media");
        final File file = new File(dir, fileName);
        if (file.exists()) {
            return file;
        }
        throw new NotFoundException("File not found");
    }
}
