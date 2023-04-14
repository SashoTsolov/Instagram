package bg.ittalents.instagram.util;

import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.post.PostRepository;
import bg.ittalents.instagram.user.UserRepository;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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

    protected User getUserById(long id){
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User was not found"));
    }

}
