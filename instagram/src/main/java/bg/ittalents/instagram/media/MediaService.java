package bg.ittalents.instagram.media;

import bg.ittalents.instagram.exceptions.BadRequestException;
import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.post.DTOs.PostWithoutCommentsDTO;
import bg.ittalents.instagram.post.PostRepository;
import bg.ittalents.instagram.post.Post;
import lombok.SneakyThrows;
import org.apache.commons.io.FilenameUtils;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.UUID;

@Service
public class MediaService {

    @Autowired
    PostRepository postRepository;

    @Autowired
    MediaRepository mediaRepository;

    @Autowired
    ModelMapper mediaMapper;

    @SneakyThrows
    public PostWithoutCommentsDTO upload(List<MultipartFile> files, long loggedId, long postId) {


        Post post = postRepository.findById(postId).orElseThrow(() -> new NotFoundException("Post doesn't exist"));

        if(post.getIsCreated()) {
            throw new BadRequestException("You cannot add media to an already created post!");
        }
        for (MultipartFile file : files) {
            final String ext = FilenameUtils.getExtension(file.getOriginalFilename());
            final String name = UUID.randomUUID().toString() + "." + ext;
            final File dir = new File("uploads");
            if (!dir.exists()) {
                dir.mkdirs();
            }
            File f = new File(dir, name);
            Files.copy(file.getInputStream(), f.toPath());
            String url = dir.getName() + File.separator + f.getName();


            Media media = new Media();
            media.setMediaUrl(url);
            media.setPost(post);
            mediaRepository.save(media);

            post.getMediaUrls().add(media);
        }

        post.setIsCreated(true);
        postRepository.save(post);
        return mediaMapper.map(post, PostWithoutCommentsDTO.class);
    }

    public File download(String fileName) {
        File dir = new File("uploads");
        File f = new File(dir, fileName);
        if (f.exists()) {
            return f;
        }
        throw new NotFoundException("File not found");
    }
}
