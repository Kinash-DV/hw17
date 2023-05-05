package dv.kinash.hw15.service;

import dv.kinash.hw15.dto.UserDTO;
import dv.kinash.hw15.dto.UserNewDTO;
import dv.kinash.hw15.exception.UserAlreadyExistsException;
import dv.kinash.hw15.exception.UserNotFoundException;
import dv.kinash.hw15.repository.UserRepository;
import dv.kinash.hw15.repository.entity.User;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository repository;
    private final ModelMapper mapper;
    private final PasswordEncoder passwordEncoder;
    private final Environment env;

    public UserServiceImpl(UserRepository repository, ModelMapper mapper, PasswordEncoder passwordEncoder, Environment env) {
        this.repository = repository;
        this.mapper = mapper;
        this.passwordEncoder = passwordEncoder;
        this.env = env;
        checkAdmin();
    }

    @Override
    public void addUser(UserNewDTO newUser, Boolean isAdmin) {
        if (repository.findByEmail(newUser.getEmail()).isPresent()){
            throw new UserAlreadyExistsException(
                    String.format("User with e-mail <%s> already exists", newUser.getEmail()));
        }
        final User user = mapper.map(newUser, User.class);
        user.setPassword(passwordEncoder.encode(newUser.getPassword()));
        user.setIsAdmin(isAdmin);
        repository.save(user);
    }

    @Override
    public List<UserDTO> getUsers() {
        return mapper.map(repository.findAll(),
                new TypeToken<List<UserDTO>>() {}.getType());
    }

    @Override
    public User getUserByEmail(String email) {
        return repository.findByEmail(email).orElseThrow(() -> new UserNotFoundException(
                String.format("User with e-mail <%s> not found", email)));
    }

    @Override
    public void checkAdmin() {
        if (repository.findByEmail("admin@book.com").isPresent())
            return;
        User admin = new User();
        admin.setName(env.getProperty("admin.name"));
        admin.setEmail(env.getProperty("admin.email"));
        admin.setPassword(passwordEncoder.encode(env.getProperty("admin.password")));
        admin.setIsAdmin(true);
        repository.save(admin);
    }
}
