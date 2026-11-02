package nextvest.service;

import nextvest.model.RiskProfile;
import nextvest.model.RiskProfileType;
import nextvest.model.User;
import nextvest.repository.RiskProfileRepository;
import nextvest.repository.UserRepository;
import org.springframework.stereotype.Service;
import nextvest.dto.RiskProfileResponse;

import java.time.LocalDate;
import java.util.List;

@Service
public class RiskProfileService {

    private final RiskProfileRepository riskProfileRepository;
    private final UserRepository userRepository;

    public RiskProfileService(RiskProfileRepository riskProfileRepository,
                              UserRepository userRepository) {
        this.riskProfileRepository = riskProfileRepository;
        this.userRepository = userRepository;
    }

    public RiskProfile evaluate(Integer userId, List<String> answers) {
        RiskProfileType type = calculateProfile(answers);

        RiskProfile profile;

        if (riskProfileRepository.existsByUserId(userId)) {
            profile = riskProfileRepository.findByUserId(userId).get();
        } else {
            profile = new RiskProfile();
            User user = new User();
            user.setId(userId);
            profile.setUser(user);
        }

        profile.setType(type);
        profile.setEvaluationDate(LocalDate.now());
        profile.setAnswers(answers);

        return riskProfileRepository.save(profile);
    }

    public RiskProfile getByUserId(Integer userId) {
        return riskProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Perfil de riesgo no encontrado"));
    }

    public RiskProfile getByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return riskProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("Perfil de riesgo no encontrado"));
    }

    private RiskProfileType calculateProfile(List<String> answers) {
        long conservativeCount = answers.stream()
                .filter(a -> a.equalsIgnoreCase("A"))
                .count();

        if (conservativeCount >= answers.size() / 2) {
            return RiskProfileType.CONSERVATIVE;
        } else {
            return RiskProfileType.MODERATE;
        }
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