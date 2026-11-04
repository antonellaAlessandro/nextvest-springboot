package nextvest.service;

import nextvest.dto.LoginRequest;
import nextvest.dto.LoginResponse;
import nextvest.dto.UserResponse;
import nextvest.exception.BusinessException;
import nextvest.model.Role;
import nextvest.model.User;
import nextvest.repository.UserRepository;
import nextvest.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserService {

    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public User register(User user) {
        validateNewUser(user);
        user.setEmail(normalizeEmail(user.getEmail()));

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese email");
        }
        if (userRepository.existsByDni(user.getDni())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese DNI");
        }

        user.setId(null);
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
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw invalidCredentials();
        }

        User user = userRepository.findByEmail(normalizeEmail(request.getEmail()))
                .orElseThrow(this::invalidCredentials);

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw invalidCredentials();
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

    private void validateNewUser(User user) {
        if (isBlank(user.getName()) || isBlank(user.getLastName()) || isBlank(user.getEmail())
                || isBlank(user.getPassword()) || user.getDni() == null
                || user.getBirthDate() == null) {
            throw new BusinessException("Completá todos los datos obligatorios");
        }
        if (!user.getEmail().trim().matches(EMAIL_REGEX)) {
            throw new BusinessException("El email no tiene un formato válido");
        }
        if (user.getPassword().length() < 8 || user.getPassword().length() > 72) {
            throw new BusinessException("La contraseña debe tener entre 8 y 72 caracteres");
        }
        if (!user.getBirthDate().isBefore(LocalDate.now())) {
            throw new BusinessException("La fecha de nacimiento no es válida");
        }
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase();
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    private ResponseStatusException invalidCredentials() {
        return new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                "Email o contraseña incorrectos");
    }
}