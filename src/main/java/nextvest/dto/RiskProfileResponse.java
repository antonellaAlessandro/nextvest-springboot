package nextvest.dto;

import nextvest.model.RiskProfileType;

import java.time.LocalDate;
import java.util.List;

public record RiskProfileResponse(
        Integer id,
        RiskProfileType type,
        LocalDate evaluationDate,
        List<String> answers,
        Integer userId
) {}