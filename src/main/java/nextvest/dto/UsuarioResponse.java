package nextvest.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class UsuarioResponse {
    private Integer id;
    private String nombre;
    private String apellido;
    private Integer dni;
    private String email;
    private String estadoCuenta;
}