package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.Major;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MajorRepository extends JpaRepository<Major, Integer> {
    Optional<Major> findByCode(String code);
}