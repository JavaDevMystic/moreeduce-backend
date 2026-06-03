package bunyodbek.uz.moreeduce.repository;

import bunyodbek.uz.moreeduce.entity.Role;
import bunyodbek.uz.moreeduce.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    long countByRole(Role role);
    List<User> findByRole(Role role);
    long countByCreatedAtAfter(LocalDateTime date);
    void deleteByIsEmailVerifiedFalseAndCreatedAtBefore(LocalDateTime date);
}
