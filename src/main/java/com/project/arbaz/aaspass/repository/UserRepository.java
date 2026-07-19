package com.project.arbaz.aaspass.repository;

import com.project.arbaz.aaspass.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users , Long> {
    Optional<Users> findByProviderAndProviderSubject(
            String provider,
            String providerSubject
    );
}
