package nextvest.dto;

public record AdminUserResponse(
        Integer id,
        String nombre,
        String apellido,
        String email,
        String role,
        String estadoCuenta
) {}