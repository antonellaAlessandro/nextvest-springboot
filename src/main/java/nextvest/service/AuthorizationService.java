package nextvest.service;

import nextvest.dto.AuthorizationInfoResponse;
import nextvest.dto.AuthorizationTokenResponse;
import nextvest.dto.LegalRepresentativeResponse;
import nextvest.exception.BusinessException;
import nextvest.exception.NotFoundException;
import nextvest.model.AuthorizationToken;
import nextvest.model.LegalRepresentative;
import nextvest.model.User;
import nextvest.repository.AuthorizationTokenRepository;
import nextvest.repository.LegalRepresentativeRepository;
import nextvest.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.util.UUID;

@Service
public class AuthorizationService {

    private static final String EMAIL_REGEX = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$";

    private final AuthorizationTokenRepository tokenRepository;
    private final LegalRepresentativeRepository representativeRepository;
    private final UserRepository userRepository;

    public AuthorizationService(AuthorizationTokenRepository tokenRepository,
                                LegalRepresentativeRepository representativeRepository,
                                UserRepository userRepository) {
        this.tokenRepository = tokenRepository;
        this.representativeRepository = representativeRepository;
        this.userRepository = userRepository;
    }

    public LegalRepresentative registerRepresentative(Integer userId,
                                                      LegalRepresentative representative) {
        if (isBlank(representative.getName()) || isBlank(representative.getLastName())
                || isBlank(representative.getEmail()) || representative.getDni() == null) {
            throw new BusinessException("Completá todos los datos del representante legal");
        }
        if (!representative.getEmail().trim().matches(EMAIL_REGEX)) {
            throw new BusinessException("El email del representante no tiene un formato válido");
        }

        User user = findUser(userId);
        representative.setId(null);
        representative.setUser(user);
        return representativeRepository.save(representative);
    }

    public AuthorizationToken generateToken(Integer userId) {
        User user = findUser(userId);

        tokenRepository.findByUserId(userId)
                .ifPresent(tokenRepository::delete);

        AuthorizationToken token = new AuthorizationToken();
        token.setUser(user);
        token.setUuid(UUID.randomUUID().toString());
        token.setExpiration(LocalDateTime.now().plusHours(48));
        token.setUsed(false);

        return tokenRepository.save(token);
    }

    /**
     * Datos mínimos para que el representante sepa a qué cuenta corresponde el link.
     * Es público: el token (uuid) funciona como secreto y no se expone nada sensible
     * (ni DNI, ni email, ni fecha de nacimiento).
     */
    @Transactional(readOnly = true)
    public AuthorizationInfoResponse getInfo(String uuid) {
        AuthorizationToken token = tokenRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Token no encontrado"));

        User user = findUser(token.getUser().getId());
        String representativeName = representativeRepository.findByUserId(user.getId())
                .map(r -> r.getName() + " " + r.getLastName())
                .orElse(null);

        Integer age = user.getBirthDate() != null
                ? Period.between(user.getBirthDate(), LocalDate.now()).getYears()
                : null;

        return new AuthorizationInfoResponse(
                user.getName(),
                user.getLastName(),
                age,
                representativeName,
                token.getExpiration(),
                Boolean.TRUE.equals(token.getUsed()),
                token.getExpiration().isBefore(LocalDateTime.now()),
                "PENDIENTE".equals(user.getAccountStatus())
        );
    }

    @Transactional
    public void authorize(String uuid) {
        AuthorizationToken token = findUnusedToken(uuid);

        if (token.getExpiration().isBefore(LocalDateTime.now())) {
            throw new BusinessException("El token ha vencido");
        }

        User user = findUser(token.getUser().getId());
        requirePending(user);

        token.setUsed(true);
        tokenRepository.save(token);

        user.setAccountStatus("ACTIVA");
        userRepository.save(user);
    }

    @Transactional
    public void reject(String uuid) {
        AuthorizationToken token = findUnusedToken(uuid);

        User user = findUser(token.getUser().getId());
        requirePending(user);

        token.setUsed(true);
        tokenRepository.save(token);

        user.setAccountStatus("RECHAZADA");
        userRepository.save(user);
    }

    public LegalRepresentative getRepresentativeByUserId(Integer userId) {
        return representativeRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException("Representante legal no encontrado"));
    }

    public LegalRepresentativeResponse toRepresentativeResponse(LegalRepresentative representative) {
        return new LegalRepresentativeResponse(
                representative.getId(),
                representative.getName(),
                representative.getLastName(),
                representative.getDni(),
                representative.getEmail(),
                representative.getUser().getId()
        );
    }

    public AuthorizationTokenResponse toTokenResponse(AuthorizationToken token) {
        return new AuthorizationTokenResponse(
                token.getId(),
                token.getUuid(),
                token.getExpiration(),
                token.getUsed(),
                token.getUser().getId()
        );
    }

    private User findUser(Integer userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
    }

    private AuthorizationToken findUnusedToken(String uuid) {
        AuthorizationToken token = tokenRepository.findByUuid(uuid)
                .orElseThrow(() -> new NotFoundException("Token no encontrado"));
        if (token.getUsed()) {
            throw new BusinessException("El token ya fue utilizado");
        }
        return token;
    }

    private void requirePending(User user) {
        if (!"PENDIENTE".equals(user.getAccountStatus())) {
            throw new BusinessException("La cuenta no está pendiente de autorización");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}