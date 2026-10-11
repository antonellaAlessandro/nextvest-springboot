package nextvest.repository;

import nextvest.model.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioRepository extends JpaRepository<Portfolio, Integer> {
    Optional<Portfolio> findByUsuarioId(Integer userId);
}