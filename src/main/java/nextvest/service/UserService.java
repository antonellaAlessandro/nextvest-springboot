package nextvest.service;

import nextvest.dto.LoginRequest;
import nextvest.dto.LoginResponse;
import nextvest.dto.UserResponse;
import nextvest.model.Role;
import nextvest.model.User;
import nextvest.repository.UserRepository;
import nextvest.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(User user) {
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }
        if (userRepository.existsByDni(user.getDni())) {
            throw new RuntimeException("Ya existe un usuario con ese DNI");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setAccountStatus("PENDIENTE");
        user.setRole(Role.MINOR);
        return userRepository.save(user);
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Integer id) {
        return userRepository.findById(id);
    }

    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new RuntimeException("Email o contraseña incorrectos");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new LoginResponse(
                token,
                user.getEmail(),
                user.getId(),
                user.getAccountStatus()
        );
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getLastName(),
                user.getDni(),
                user.getEmail(),
                user.getAccountStatus()
        );
    }
}