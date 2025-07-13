package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.School;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SchoolRepository extends JpaRepository<School, Integer> {
}
