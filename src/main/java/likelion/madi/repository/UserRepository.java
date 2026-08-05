package likelion.madi.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import likelion.madi.domain.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
