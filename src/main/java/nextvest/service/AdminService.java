package nextvest.service;

import nextvest.dto.AdminUserDetailResponse;
import nextvest.dto.AdminUserResponse;
import nextvest.model.Usuario;
import nextvest.repository.LegalRepresentativeRepository;
import nextvest.repository.UsuarioRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class AdminService {

    private static final Set<String> VALID_STATUSES =
            Set.of("ACTIVA", "PENDIENTE", "SUSPENDIDA", "RECHAZADA");

    private final UsuarioRepository usuarioRepository;
    private final LegalRepresentativeRepository legalRepresentativeRepository;

    public AdminService(UsuarioRepository usuarioRepository,
                        LegalRepresentativeRepository legalRepresentativeRepository) {
        this.usuarioRepository = usuarioRepository;
        this.legalRepresentativeRepository = legalRepresentativeRepository;
    }

    public List<AdminUserResponse> listUsers() {
        return usuarioRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    public AdminUserDetailResponse getUserDetail(Integer id) {
        Usuario u = findOrThrow(id);

        AdminUserDetailResponse.LegalRepresentativeInfo rep = legalRepresentativeRepository
                .findByUsuarioId(u)
                .map(r -> new AdminUserDetailResponse.LegalRepresentativeInfo(
                        r.getName(), r.getLastName(), r.getEmail(), String.valueOf(r.getDni())))
                .orElse(null);

        return new AdminUserDetailResponse(u.getId(), u.getNombre(), u.getApellido(), u.getDni(),
                u.getFechaNacimiento(), u.getEmail(), u.getRole().name(), u.getEstadoCuenta(), rep);
    }

    public AdminUserResponse updateAccountStatus(Integer id, String newStatus) {
        if (newStatus == null || !VALID_STATUSES.contains(newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado inválido. Valores permitidos: " + VALID_STATUSES);
        }
        Usuario u = findOrThrow(id);
        u.setEstadoCuenta(newStatus);
        return toSummary(usuarioRepository.save(u));
    }

    private Usuario findOrThrow(Integer id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private AdminUserResponse toSummary(Usuario u) {
        return new AdminUserResponse(u.getId(), u.getNombre(), u.getApellido(),
                u.getEmail(), u.getRole().name(), u.getEstadoCuenta());
    }
}