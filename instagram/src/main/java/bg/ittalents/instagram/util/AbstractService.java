package bg.ittalents.instagram.util;

import bg.ittalents.instagram.exception.NotFoundException;
import bg.ittalents.instagram.user.UserRepository;
import org.modelmapper.ModelMapper;
import bg.ittalents.instagram.user.User;
import org.springframework.mail.javamail.JavaMailSender;

public abstract class AbstractService {

    protected final UserRepository userRepository;
    protected final JavaMailSender javaMailSender;
    protected final ModelMapper mapper;

    public AbstractService(UserRepository userRepository, JavaMailSender javaMailSender, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.javaMailSender = javaMailSender;
        this.mapper = mapper;
    }

    protected User getUserById(final long id) {
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User was not found"));
    }

    protected User getUserByEmail(final String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User was not found"));
    }
}
