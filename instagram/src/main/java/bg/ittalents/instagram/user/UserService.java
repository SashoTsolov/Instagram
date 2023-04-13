package bg.ittalents.instagram.user;

import bg.ittalents.instagram.exceptions.BadRequestException;
import bg.ittalents.instagram.exceptions.NotFoundException;
import bg.ittalents.instagram.exceptions.UnauthorizedException;
import bg.ittalents.instagram.exceptions.UserAlreadyExistsException;
import bg.ittalents.instagram.user.DTOs.RegisterDTO;
import bg.ittalents.instagram.user.DTOs.UserLoginDTO;
import bg.ittalents.instagram.user.DTOs.UserWithoutPassAndEmailDTO;
import bg.ittalents.instagram.util.AbstractService;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService extends AbstractService {

    public UserWithoutPassAndEmailDTO create(RegisterDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())){
            throw new BadRequestException("Password confirmation mismatch");
        }
        //Additional validation needed
        //TODO
        if (userRepository.existsByEmail(dto.getEmail())){
            throw new UserAlreadyExistsException("Email already being used");
        }
        User u = mapper.map(dto, User.class);
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        userRepository.save(u);
        return mapper.map(u, UserWithoutPassAndEmailDTO.class);
    }


    public UserWithoutPassAndEmailDTO login(UserLoginDTO dto) {
        Optional<User> opt = userRepository.getByEmail(dto.getEmail());
        if (!opt.isPresent()){
            throw new UnauthorizedException("Wrong credentials");
        }
        if(!passwordEncoder.matches(dto.getPassword(), opt.get().getPassword())){
            throw new UnauthorizedException("Wrong credentials");
        }
        return mapper.map(opt, UserWithoutPassAndEmailDTO.class);
    }

    public UserWithoutPassAndEmailDTO getById(long id) {
        Optional<User> u = userRepository.findById(id);
        if (u.isPresent()) {
            return mapper.map(u, UserWithoutPassAndEmailDTO.class);
        }
        throw new NotFoundException("User not found");
    }
}
