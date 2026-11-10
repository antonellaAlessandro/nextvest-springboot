package nextvest.service;

import nextvest.dto.ChangePasswordRequest;
import nextvest.dto.LegalRepresentativeResponse;
import nextvest.dto.LoginRequest;
import nextvest.dto.LoginResponse;
import nextvest.dto.UpdateUserRequest;
import nextvest.dto.UserResponse;
import nextvest.exception.BusinessException;
import nextvest.exception.NotFoundException;
import nextvest.model.LegalRepresentative;
import nextvest.model.Role;
import nextvest.model.User;
import nextvest.repository.LegalRepresentativeRepository;
import nextvest.repository.UserRepository;
import nextvest.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

@Service
public class UserService {

    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";
    private static final int NAME_MAX_LENGTH = 45;
    private static final int EMAIL_MAX_LENGTH = 45;

    private final UserRepository userRepository;
    private final LegalRepresentativeRepository legalRepresentativeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final WalletService walletService;
    private final PortfolioService portfolioService;

    public UserService(UserRepository userRepository,
                       LegalRepresentativeRepository legalRepresentativeRepository,
                       PasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       WalletService walletService,
                       PortfolioService portfolioService) {
        this.userRepository = userRepository;
        this.legalRepresentativeRepository = legalRepresentativeRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.walletService = walletService;
        this.portfolioService = portfolioService;
    }

    @Transactional
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

        User saved = userRepository.save(user);
        walletService.createWallet(saved);
        portfolioService.createPortfolio(saved);
        return saved;
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

    /**
     * Actualiza nombre, apellido y email. El DNI y la fecha de nacimiento no se
     * pueden modificar porque se usan para validar la edad y la autorización.
     * Si cambia el email, el token anterior deja de servir (el email es su sujeto).
     */
    @Transactional
    public User update(Integer id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (request == null || isBlank(request.getName()) || isBlank(request.getLastName())
                || isBlank(request.getEmail())) {
            throw new BusinessException("Completá todos los datos obligatorios");
        }

        String name = request.getName().trim();
        String lastName = request.getLastName().trim();
        String email = normalizeEmail(request.getEmail());

        if (name.length() > NAME_MAX_LENGTH || lastName.length() > NAME_MAX_LENGTH) {
            throw new BusinessException("El nombre y el apellido pueden tener hasta "
                    + NAME_MAX_LENGTH + " caracteres");
        }
        if (!email.matches(EMAIL_REGEX) || email.length() > EMAIL_MAX_LENGTH) {
            throw new BusinessException("El email no tiene un formato válido");
        }
        if (!email.equals(user.getEmail()) && userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe un usuario con ese email");
        }

        user.setName(name);
        user.setLastName(lastName);
        user.setEmail(email);
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Integer id, ChangePasswordRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        if (request == null || isBlank(request.getCurrentPassword())
                || isBlank(request.getNewPassword())) {
            throw new BusinessException("Completá la contraseña actual y la nueva");
        }
        // Se informa como error de negocio (no 401) para no cerrar la sesión del usuario.
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BusinessException("La contraseña actual no es correcta");
        }
        if (request.getNewPassword().length() < 8 || request.getNewPassword().length() > 72) {
            throw new BusinessException("La contraseña debe tener entre 8 y 72 caracteres");
        }
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new BusinessException("La contraseña nueva tiene que ser distinta de la actual");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public LegalRepresentativeResponse findLegalRepresentative(Integer userId) {
        LegalRepresentative rep = legalRepresentativeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("No hay un representante legal cargado"));
        return new LegalRepresentativeResponse(
                rep.getId(),
                rep.getName(),
                rep.getLastName(),
                rep.getDni(),
                rep.getEmail(),
                rep.getUser().getId()
        );
    }

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getLastName(),
                user.getDni(),
                user.getBirthDate(),
                user.getEmail(),
                user.getAccountStatus(),
                user.getRole() != null ? user.getRole().name() : null
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