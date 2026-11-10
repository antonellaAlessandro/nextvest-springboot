package nextvest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDate;

@Data
@AllArgsConstructor
public class UserResponse {
    private Integer id;
    private String name;
    private String lastName;
    private Integer dni;
    private LocalDate birthDate;
    private String email;
    private String accountStatus;
    private String role;
}