package likelion.madi.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "BLACKLISTED_TOKEN")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class BlacklistedToken {

    @Id
    private String tokenId;

    private LocalDateTime expiresAt;

    @Builder
    public BlacklistedToken(String tokenId, LocalDateTime expiresAt) {
        this.tokenId = tokenId;
        this.expiresAt = expiresAt;
    }
}