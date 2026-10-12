package nextvest.service;

import nextvest.model.RiskProfile;
import nextvest.model.RiskProfileType;
import nextvest.model.Usuario;
import nextvest.repository.RiskProfileRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RiskProfileService {

    private final RiskProfileRepository riskProfileRepository;

    public RiskProfileService(RiskProfileRepository riskProfileRepository) {
        this.riskProfileRepository = riskProfileRepository;
    }

    public RiskProfile evaluate(Integer userId, List<String> answers) {
        RiskProfileType type = calculateProfile(answers);

        RiskProfile profile;

        if (riskProfileRepository.existsByUsuarioId(userId)) {
            profile = riskProfileRepository.findByUsuarioId(userId).get();
        } else {
            profile = new RiskProfile();
            Usuario usuario = new Usuario();
            usuario.setId(userId);
            profile.setUsuario(usuario);
        }

        profile.setType(type);
        profile.setEvaluationDate(LocalDate.now());
        profile.setAnswers(answers);

        return riskProfileRepository.save(profile);
    }

    public RiskProfile getByUserId(Integer userId) {
        return riskProfileRepository.findByUsuarioId(userId)
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
}