package nextvest.repository;

import nextvest.model.RiskProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RiskProfileRepository extends JpaRepository<RiskProfile, Integer> {
    Optional<RiskProfile> findByUsuarioId(Integer userId);
    boolean existsByUsuarioId(Integer userId);
}