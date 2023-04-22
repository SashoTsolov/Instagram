package bg.ittalents.instagram.util;

import bg.ittalents.instagram.exception.NotFoundException;
import bg.ittalents.instagram.user.UserRepository;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.appmesh.model.InternalServerErrorException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.S3Object;
import lombok.SneakyThrows;
import org.modelmapper.ModelMapper;
import bg.ittalents.instagram.user.User;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

public abstract class AbstractService {

    protected final UserRepository userRepository;
    protected final JavaMailSender javaMailSender;
    protected final ModelMapper mapper;
    protected final AmazonS3 s3Client;
    protected final String bucketName;

    public AbstractService(final UserRepository userRepository,
                           final JavaMailSender javaMailSender,
                           final ModelMapper mapper,
                           final AmazonS3 s3Client,
                           final String bucketName) {
        this.userRepository = userRepository;
        this.javaMailSender = javaMailSender;
        this.mapper = mapper;
        this.s3Client = s3Client;
        this.bucketName = bucketName;
    }

    protected User getUserById(final long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User was not found"));
    }

    protected User getUserByEmail(final String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User was not found"));
    }

    @SneakyThrows
    protected String uploadMedia(final MultipartFile file, final String ext) {
        final String name = UUID.randomUUID().toString() + "." + ext;
        try {
            // Upload to S3
            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentType(file.getContentType());
            metadata.setContentLength(file.getSize());
            s3Client.putObject(bucketName, name, file.getInputStream(), metadata);
            return s3Client.getUrl(bucketName, name).toString();
        } catch (AmazonServiceException e) {
            throw new InternalServerErrorException("Failed to upload file to S3");
        }
    }

    public InputStream downloadMedia(final String fileName) {
        if (s3Client.doesObjectExist(bucketName, fileName)) {
            S3Object s3Object = s3Client.getObject(bucketName, fileName);
            return s3Object.getObjectContent();
        }
        throw new NotFoundException("File not found");
    }
}
