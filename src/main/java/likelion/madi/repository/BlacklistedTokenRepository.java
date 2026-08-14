package likelion.madi.repository;

import likelion.madi.domain.BlacklistedToken;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {

    boolean existsByTokenId(String tokenId);
}