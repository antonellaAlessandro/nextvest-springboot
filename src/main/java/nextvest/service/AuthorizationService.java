package nextvest.service;

import nextvest.dto.AuthorizationTokenResponse;
import nextvest.dto.LegalRepresentativeResponse;
import nextvest.model.AuthorizationToken;
import nextvest.model.LegalRepresentative;
import nextvest.model.User;
import nextvest.repository.AuthorizationTokenRepository;
import nextvest.repository.LegalRepresentativeRepository;
import nextvest.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthorizationService {

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
        User user = new User();
        user.setId(userId);
        representative.setUser(user);
        return representativeRepository.save(representative);
    }

    public AuthorizationToken generateToken(Integer userId) {
        tokenRepository.findByUserId(userId)
                .ifPresent(tokenRepository::delete);

        AuthorizationToken token = new AuthorizationToken();
        User user = new User();
        user.setId(userId);
        token.setUser(user);
        token.setUuid(UUID.randomUUID().toString());
        token.setExpiration(LocalDateTime.now().plusHours(48));
        token.setUsed(false);

        return tokenRepository.save(token);
    }

    public void authorize(String uuid) {
        AuthorizationToken token = tokenRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Token no encontrado"));

        if (token.getUsed()) {
            throw new RuntimeException("El token ya fue utilizado");
        }

        if (token.getExpiration().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("El token ha vencido");
        }

        token.setUsed(true);
        tokenRepository.save(token);

        User user = userRepository.findById(token.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setAccountStatus("ACTIVA");
        userRepository.save(user);
    }

    public void reject(String uuid) {
        AuthorizationToken token = tokenRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Token no encontrado"));

        if (token.getUsed()) {
            throw new RuntimeException("El token ya fue utilizado");
        }

        token.setUsed(true);
        tokenRepository.save(token);

        User user = userRepository.findById(token.getUser().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        user.setAccountStatus("RECHAZADA");
        userRepository.save(user);
    }

    public LegalRepresentative getRepresentativeByUserId(Integer userId) {
        return representativeRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Representante legal no encontrado"));
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
}