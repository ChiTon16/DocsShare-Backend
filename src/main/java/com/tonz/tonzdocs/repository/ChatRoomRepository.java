// src/main/java/com/tonz/tonzdocs/repository/ChatRoomRepository.java
package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {
    Optional<ChatRoom> findByCodeIgnoreCase(String code);

    @Query("""
        select r from ChatRoom r
        where (:q is null or :q = ''
           or lower(r.name) like lower(concat('%', :q, '%'))
           or lower(r.code) like lower(concat('%', :q, '%')))
        """)
    Page<ChatRoom> search(String q, Pageable pageable);

    boolean existsByCodeIgnoreCase(String finalCode);
}
