package nextvest.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class OrderResponse {
    private Integer id;
    private String type;
    private Integer units;
    private Double unitPrice;
    private Double totalAmount;
    private LocalDateTime dateTime;
    private String instrumentName;
    private String ticker;
}