// src/main/java/com/tonz/tonzdocs/repository/ChatMemberRepository.java
package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.ChatMember;
import com.tonz.tonzdocs.model.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {
    Page<ChatMember> findByRoomId(Long roomId, Pageable pageable);

    Optional<ChatMember> findByRoomIdAndUserEmail(Long roomId, String userEmail);

    long countByRoomId(Long roomId);

    boolean existsByRoomIdAndUserEmail(Long roomId, String userEmail);

    void deleteByRoomId(Long roomId);

    @Query("""
        select m.room from ChatMember m
        where m.userEmail = :email
          and (:q is null or :q = ''
               or lower(m.room.name) like lower(concat('%', :q, '%'))
               or lower(m.room.code) like lower(concat('%', :q, '%')))
        """)
    Page<ChatRoom> roomsOfUser(@Param("email") String email,
                               @Param("q") String q,
                               Pageable pageable);
}
