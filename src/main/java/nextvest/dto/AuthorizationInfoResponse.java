package nextvest.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class AuthorizationInfoResponse {
    private String minorName;
    private String minorLastName;
    private Integer minorAge;
    private String representativeName;
    private LocalDateTime expiration;
    private boolean used;
    private boolean expired;
    private boolean pending;
}