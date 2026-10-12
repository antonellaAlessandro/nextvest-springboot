package nextvest.dto;

import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class PositionResponse {
    private Integer id;
    private Integer units;
    private Double averageBuyPrice;
    private String instrumentName;
    private String ticker;
    private Double currentPrice;
    private Double currentValue;
    private Double gainLoss;
}