package nextvest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDate;

@Entity
@Table(name = "usuario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "nombre", nullable = false, length = 45)
    private String name;

    @Column(name = "apellido", nullable = false, length = 45)
    private String lastName;

    @Column(nullable = false, unique = true)
    private Integer dni;

    @Column(name = "fecha_nacimiento", nullable = false)
    private LocalDate birthDate;

    @Column(nullable = false, unique = true, length = 45)
    private String email;

    @ToString.Exclude
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "contrasenia", nullable = false, length = 100)
    private String password;

    @Column(name = "estado_cuenta", length = 45)
    private String accountStatus;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.MINOR;
}