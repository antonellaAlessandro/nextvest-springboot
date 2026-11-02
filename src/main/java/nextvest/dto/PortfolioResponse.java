package nextvest.dto;

public record PortfolioResponse(
        Integer id,
        Double totalValue,
        Integer userId
) {}