package nextvest.service;

import nextvest.dto.LoginRequest;
import nextvest.dto.LoginResponse;
import nextvest.model.Usuario;
import nextvest.repository.UsuarioRepository;
import nextvest.security.JwtService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public Usuario registrar(Usuario usuario) {
        if (usuarioRepository.existsByEmail(usuario.getEmail())) {
            throw new RuntimeException("Ya existe un usuario con ese email");
        }
        if (usuarioRepository.existsByDni(usuario.getDni())) {
            throw new RuntimeException("Ya existe un usuario con ese DNI");
        }
        usuario.setContrasenia(passwordEncoder.encode(usuario.getContrasenia()));
        usuario.setEstadoCuenta("PENDIENTE");
        return usuarioRepository.save(usuario);
    }

    public Optional<Usuario> buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email);
    }

    public Optional<Usuario> buscarPorId(Integer id) {
        return usuarioRepository.findById(id);
    }

    public LoginResponse login(LoginRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Email o contraseña incorrectos"));

        if (!passwordEncoder.matches(request.getContrasenia(), usuario.getContrasenia())) {
            throw new RuntimeException("Email o contraseña incorrectos");
        }

        String token = jwtService.generarToken(usuario.getEmail());

        return new LoginResponse(
                token,
                usuario.getEmail(),
                usuario.getId(),
                usuario.getEstadoCuenta()
        );
    }
}