package nextvest.repository;

import nextvest.model.LegalRepresentative;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LegalRepresentativeRepository extends JpaRepository<LegalRepresentative, Integer> {
    Optional<LegalRepresentative> findByUsuarioId(Integer userId);
}