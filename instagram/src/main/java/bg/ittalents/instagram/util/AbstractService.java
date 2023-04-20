package bg.ittalents.instagram.util;

import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.follower.FollowRepository;
import bg.ittalents.instagram.post.PostRepository;
import bg.ittalents.instagram.user.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import bg.ittalents.instagram.user.User;

public abstract class AbstractService {
    @Autowired
    protected UserRepository userRepository;
    @Autowired
    protected PostRepository postRepository;

    @Autowired
    protected BCryptPasswordEncoder encoder;

    @Autowired
    protected ModelMapper mapper;

    @Autowired
    protected FollowRepository followRepository;

    @Autowired
    protected JavaMailSender javaMailSender;

    protected User getUserById(long id){
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User was not found"));
    }

    protected User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User was not found"));
    }
}
