package nextvest.service;

import nextvest.dto.AdminUserDetailResponse;
import nextvest.dto.AdminUserResponse;
import nextvest.model.User;
import nextvest.repository.LegalRepresentativeRepository;
import nextvest.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Set;

@Service
public class AdminService {

    private static final Set<String> VALID_STATUSES =
            Set.of("ACTIVA", "PENDIENTE", "SUSPENDIDA", "RECHAZADA");

    private final UserRepository userRepository;
    private final LegalRepresentativeRepository legalRepresentativeRepository;

    public AdminService(UserRepository userRepository,
                        LegalRepresentativeRepository legalRepresentativeRepository) {
        this.userRepository = userRepository;
        this.legalRepresentativeRepository = legalRepresentativeRepository;
    }

    public List<AdminUserResponse> listUsers() {
        return userRepository.findAll().stream()
                .map(this::toSummary)
                .toList();
    }

    public AdminUserDetailResponse getUserDetail(Integer id) {
        User user = findOrThrow(id);

        AdminUserDetailResponse.LegalRepresentativeInfo representative = legalRepresentativeRepository
                .findByUserId(id)
                .map(r -> new AdminUserDetailResponse.LegalRepresentativeInfo(
                        r.getName(), r.getLastName(), r.getEmail(), String.valueOf(r.getDni())))
                .orElse(null);

        return new AdminUserDetailResponse(user.getId(), user.getName(), user.getLastName(), user.getDni(),
                user.getBirthDate(), user.getEmail(), user.getRole().name(), user.getAccountStatus(),
                representative);
    }

    public AdminUserResponse updateAccountStatus(Integer id, String newStatus) {
        if (newStatus == null || !VALID_STATUSES.contains(newStatus)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Estado inválido. Valores permitidos: " + VALID_STATUSES);
        }
        User user = findOrThrow(id);
        user.setAccountStatus(newStatus);
        return toSummary(userRepository.save(user));
    }

    private User findOrThrow(Integer id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado"));
    }

    private AdminUserResponse toSummary(User user) {
        return new AdminUserResponse(user.getId(), user.getName(), user.getLastName(),
                user.getEmail(), user.getRole().name(), user.getAccountStatus());
    }
}