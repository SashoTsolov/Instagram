package bg.ittalents.instagram.util;

import bg.ittalents.instagram.exception.NotFoundException;
import bg.ittalents.instagram.user.UserRepository;
import org.modelmapper.ModelMapper;
import bg.ittalents.instagram.user.User;

public abstract class AbstractService {

    protected final UserRepository userRepository;
    protected final ModelMapper mapper;

    public AbstractService(UserRepository userRepository, ModelMapper mapper) {
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    protected User getUserById(long id){
        return userRepository.findById(id).orElseThrow(() -> new NotFoundException("User was not found"));
    }

    protected User getUserByEmail(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new NotFoundException("User was not found"));
    }
}
