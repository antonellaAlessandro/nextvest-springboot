package nextvest.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class WalletResponse {
    private Integer id;
    private Double availableBalance;
    private Integer userId;
}