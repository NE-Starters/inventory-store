package inventory.ne.manage.repository;

import inventory.ne.manage.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    List<User> findByEmailVerifiedFalse();

    Optional<User> findByVerificationCode(String verificationCode);
}
