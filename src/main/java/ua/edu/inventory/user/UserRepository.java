package ua.edu.inventory.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID>, JpaSpecificationExecutor<User> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findAllBySiteIdAndActiveTrue(UUID siteId);

    Page<User> findAllBySiteIdAndActiveTrue(UUID siteId, Pageable pageable);

    List<User> findAllBySiteIdAndRole(UUID siteId, UserRole role);

    Optional<User> findBySiteIdAndRole(UUID siteId, UserRole role);
}
