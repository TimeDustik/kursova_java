package ua.edu.inventory.site;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SiteRepository extends JpaRepository<Site, UUID> {

    boolean existsByName(String name);

    Optional<Site> findByTeamLeadId(UUID teamLeadId);
}
