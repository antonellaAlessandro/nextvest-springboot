package nextvest.repository;

import nextvest.model.AuthorizationToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AuthorizationTokenRepository extends JpaRepository<AuthorizationToken, Integer> {
    Optional<AuthorizationToken> findByUuid(String uuid);
    Optional<AuthorizationToken> findByUsuarioId(Integer userId);
}