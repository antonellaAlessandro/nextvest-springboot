package nextvest.dto;

import java.time.LocalDate;

public record AdminUserDetailResponse(
        Integer id,
        String name,
        String lastName,
        Integer dni,
        LocalDate birthDate,
        String email,
        String role,
        String accountStatus,
        LegalRepresentativeInfo legalRepresentative
) {
    public record LegalRepresentativeInfo(String name, String lastName, String email, String dni) {}
}