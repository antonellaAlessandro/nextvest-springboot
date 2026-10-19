package nextvest.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

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