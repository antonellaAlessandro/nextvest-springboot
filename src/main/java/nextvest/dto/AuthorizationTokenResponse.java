package nextvest.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuthorizationTokenResponse {
    private Integer id;
    private String uuid;
    private LocalDateTime expiration;
    private Boolean used;
    private Integer userId;
}