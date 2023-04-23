package bg.ittalents.instagram.util;

import bg.ittalents.instagram.user.User;
import bg.ittalents.instagram.user.UserRepository;
import com.amazonaws.services.s3.AmazonS3;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class UserLoginChecker extends AbstractService {

    public UserLoginChecker(final UserRepository userRepository,
                            final JavaMailSender javaMailSender,
                            final ModelMapper mapper,
                            final AmazonS3 s3Client,
                            final @Value("${aws.s3.bucket}") String bucketName) {
        super(userRepository, javaMailSender, mapper, s3Client, bucketName);
    }

    //    @Scheduled(cron = "0 0 0 * * ?")
    @Scheduled(cron = "0 * * * * *")
    public void checkUserLogins() {
        final List<User> users = userRepository.findAll();
//        final Timestamp threeMonthsAgo = new Timestamp(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(90));
        final Timestamp oneMinuteAgo = new Timestamp(System.currentTimeMillis() - TimeUnit.MINUTES.toMillis(1));
        for (User user : users) {
            final Timestamp lastOnlineTime = user.getLastBeenOnline();
            if (lastOnlineTime != null && lastOnlineTime.before(oneMinuteAgo) && !user.isCheckedForInactivity()) {
                user.setCheckedForInactivity(true);
                userRepository.save(user);
                new Thread(() -> sendEmail(user.getEmail())).start();
            }
        }
    }

    private void sendEmail(String recipient) {
        final SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(recipient);
        message.setSubject("Your account has been inactive");
        message.setText("Please go back to Instagram! Your Instagram friends and community miss you! :(");
        javaMailSender.send(message);
    }
}