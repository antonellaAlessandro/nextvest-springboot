package nextvest.dto;

public record AdminUserResponse(
        Integer id,
        String name,
        String lastName,
        String email,
        String role,
        String accountStatus
) {}