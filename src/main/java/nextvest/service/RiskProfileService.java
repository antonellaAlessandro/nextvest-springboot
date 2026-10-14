package nextvest.service;

import nextvest.model.RiskProfile;
import nextvest.model.RiskProfileType;
import nextvest.model.Usuario;
import nextvest.repository.RiskProfileRepository;
import nextvest.repository.UsuarioRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class RiskProfileService {

    private final RiskProfileRepository riskProfileRepository;
    private final UsuarioRepository usuarioRepository;

    public RiskProfileService(RiskProfileRepository riskProfileRepository,
                               UsuarioRepository usuarioRepository) {
        this.riskProfileRepository = riskProfileRepository;
        this.usuarioRepository = usuarioRepository;
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

    public RiskProfile getByEmail(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
        return riskProfileRepository.findByUsuarioId(usuario.getId())
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