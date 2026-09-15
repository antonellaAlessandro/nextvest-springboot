package nextvest.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 45)
    private String nombre;

    @Column(nullable = false, length = 45)
    private String apellido;

    @Column(nullable = false, unique = true)
    private Integer dni;

    @Column(name = "fecha_nacimiento", nullable = false)
    private java.time.LocalDate fechaNacimiento;

    @Column(nullable = false, unique = true, length = 45)
    private String email;

    @Column(nullable = false, length = 100)
    private String contrasenia;

    @Column(name = "estado_cuenta", length = 45)
    private String estadoCuenta;
}