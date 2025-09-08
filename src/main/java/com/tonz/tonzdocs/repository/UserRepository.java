package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.User;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);

    @EntityGraph(attributePaths = {"role", "school", "major"})
    Optional<User> findWithRelationsByUserId(Integer userId);
}
