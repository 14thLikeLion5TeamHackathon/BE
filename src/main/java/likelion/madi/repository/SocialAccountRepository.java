package likelion.madi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import likelion.madi.domain.SocialAccount;
import likelion.madi.enums.SocialProvider;

import java.util.Optional;

public interface SocialAccountRepository extends JpaRepository<SocialAccount, Long> {

    Optional<SocialAccount> findByProviderAndProviderUserId(SocialProvider provider, String providerUserId);
}
