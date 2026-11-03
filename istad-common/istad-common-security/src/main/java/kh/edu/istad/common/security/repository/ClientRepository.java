package kh.edu.istad.common.security.repository;

import kh.edu.istad.common.security.entity.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClientRepository extends JpaRepository<Client,String> {
    Optional<Client> findByClientId(String clientId);
}
