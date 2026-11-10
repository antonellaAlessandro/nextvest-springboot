package nextvest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LegalRepresentativeResponse {
    private Integer id;
    private String name;
    private String lastName;
    private Integer dni;
    private String email;
    private Integer userId;
}