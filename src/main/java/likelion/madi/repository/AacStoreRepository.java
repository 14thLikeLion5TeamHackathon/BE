package likelion.madi.repository;

import likelion.madi.domain.AacStore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AacStoreRepository extends JpaRepository<AacStore, Long> {
    Optional<AacStore> findByNameAndAddress(String name, String address);
}