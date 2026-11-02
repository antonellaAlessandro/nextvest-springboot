package nextvest.dto;

import java.time.LocalDate;

public record AdminUserDetailResponse(
        Integer id,
        String nombre,
        String apellido,
        Integer dni,
        LocalDate fechaNacimiento,
        String email,
        String role,
        String estadoCuenta,
        LegalRepresentativeInfo legalRepresentative
) {
    public record LegalRepresentativeInfo(String name, String lastName, String email, String dni) {}
}