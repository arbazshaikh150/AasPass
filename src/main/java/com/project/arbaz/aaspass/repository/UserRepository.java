package com.project.arbaz.aaspass.repository;

import com.project.arbaz.aaspass.entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<Users , Long> {
    Optional<Users> findByProviderAndProviderSubject(
            String provider,
            String providerSubject
    );

    @Query("""
        SELECT u.email
        FROM Users u
        WHERE u.userId IN :userIds
    """)
    List<String> findEmailsByUserIdIn(@Param("userIds") List<Long> userIds);

}
