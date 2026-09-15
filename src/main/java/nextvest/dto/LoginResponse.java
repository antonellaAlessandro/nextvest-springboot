package nextvest.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class LoginResponse {
    private String token;
    private String email;
    private Integer id;
    private String estadoCuenta;
}