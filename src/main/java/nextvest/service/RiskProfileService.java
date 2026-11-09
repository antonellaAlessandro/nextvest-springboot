package nextvest.service;

import nextvest.dto.RiskProfileResponse;
import nextvest.exception.BusinessException;
import nextvest.exception.NotFoundException;
import nextvest.model.RiskProfile;
import nextvest.model.RiskProfileType;
import nextvest.model.User;
import nextvest.repository.RiskProfileRepository;
import nextvest.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Service
public class RiskProfileService {

    private static final int QUESTION_COUNT = 6;
    private static final Set<String> VALID_ANSWERS = Set.of("A", "B");

    private final RiskProfileRepository riskProfileRepository;
    private final UserRepository userRepository;

    public RiskProfileService(RiskProfileRepository riskProfileRepository,
                              UserRepository userRepository) {
        this.riskProfileRepository = riskProfileRepository;
        this.userRepository = userRepository;
    }

    public RiskProfile evaluate(Integer userId, List<String> answers) {
        List<String> normalized = normalizeAnswers(answers);
        RiskProfileType type = calculateProfile(normalized);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));

        RiskProfile profile = riskProfileRepository.findByUserId(userId)
                .orElseGet(() -> {
                    RiskProfile created = new RiskProfile();
                    created.setUser(user);
                    return created;
                });

        profile.setType(type);
        profile.setEvaluationDate(LocalDate.now());
        profile.setAnswers(normalized);

        return riskProfileRepository.save(profile);
    }

    public RiskProfile getByUserId(Integer userId) {
        return riskProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundException(
                        "Todavía no hiciste el test de perfil de inversor"));
    }

    public RiskProfile getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Usuario no encontrado"));
        return riskProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new NotFoundException(
                        "Todavía no hiciste el test de perfil de inversor"));
    }

    // El test tiene QUESTION_COUNT preguntas y cada respuesta es A o B.
    private List<String> normalizeAnswers(List<String> answers) {
        if (answers == null || answers.size() != QUESTION_COUNT) {
            throw new BusinessException(
                    "Respondé las " + QUESTION_COUNT + " preguntas del test");
        }
        List<String> normalized = new ArrayList<>();
        for (String answer : answers) {
            normalized.add(answer == null ? "" : answer.trim().toUpperCase());
        }
        if (!VALID_ANSWERS.containsAll(normalized)) {
            throw new BusinessException("Las respuestas del test deben ser A o B");
        }
        return normalized;
    }

    // A = opción cautelosa. Con la mitad o más de respuestas A, el perfil es conservador.
    private RiskProfileType calculateProfile(List<String> answers) {
        long conservativeCount = answers.stream()
                .filter("A"::equals)
                .count();

        if (conservativeCount * 2 >= answers.size()) {
            return RiskProfileType.CONSERVATIVE;
        }
        return RiskProfileType.MODERATE;
    }

    public RiskProfileResponse toResponse(RiskProfile profile) {
        return new RiskProfileResponse(
                profile.getId(),
                profile.getType(),
                profile.getEvaluationDate(),
                profile.getAnswers(),
                profile.getUser().getId()
        );
    }
}