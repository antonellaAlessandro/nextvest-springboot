package nextvest.service;

import nextvest.model.AuthorizationToken;
import nextvest.model.LegalRepresentative;
import nextvest.model.Usuario;
import nextvest.repository.AuthorizationTokenRepository;
import nextvest.repository.LegalRepresentativeRepository;
import nextvest.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class AuthorizationService {

    private final AuthorizationTokenRepository tokenRepository;
    private final LegalRepresentativeRepository representativeRepository;
    private final UsuarioRepository usuarioRepository;

    public AuthorizationService(AuthorizationTokenRepository tokenRepository,
                                 LegalRepresentativeRepository representativeRepository,
                                 UsuarioRepository usuarioRepository) {
        this.tokenRepository = tokenRepository;
        this.representativeRepository = representativeRepository;
        this.usuarioRepository = usuarioRepository;
    }

    public LegalRepresentative registerRepresentative(Integer userId,
                                                       LegalRepresentative representative) {
        Usuario usuario = new Usuario();
        usuario.setId(userId);
        representative.setUsuario(usuario);
        return representativeRepository.save(representative);
    }

    public AuthorizationToken generateToken(Integer userId) {
        tokenRepository.findByUsuarioId(userId)
                .ifPresent(tokenRepository::delete);

        AuthorizationToken token = new AuthorizationToken();
        Usuario usuario = new Usuario();
        usuario.setId(userId);
        token.setUsuario(usuario);
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

        Usuario usuario = usuarioRepository.findById(token.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setEstadoCuenta("ACTIVA");
        usuarioRepository.save(usuario);
    }

    public void reject(String uuid) {
        AuthorizationToken token = tokenRepository.findByUuid(uuid)
                .orElseThrow(() -> new RuntimeException("Token no encontrado"));

        if (token.getUsed()) {
            throw new RuntimeException("El token ya fue utilizado");
        }

        token.setUsed(true);
        tokenRepository.save(token);

        Usuario usuario = usuarioRepository.findById(token.getUsuario().getId())
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        usuario.setEstadoCuenta("RECHAZADA");
        usuarioRepository.save(usuario);
    }

    public LegalRepresentative getRepresentativeByUserId(Integer userId) {
        return representativeRepository.findByUsuarioId(userId)
                .orElseThrow(() -> new RuntimeException("Representante legal no encontrado"));
    }
}