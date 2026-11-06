package kh.edu.istad.iam.feature.user;

import kh.edu.istad.iam.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserJapRepository
        extends JpaRepository<User, Long> {


    boolean existsByUsername(String tak);

    Optional<User> findByUsernameOrEmail(String username, String username1);
}
