package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.ChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ChatMessageRepo extends JpaRepository<ChatMessage, Long> {
    Page<ChatMessage> findByRoomIdOrderByIdDesc(Long roomId, Pageable pageable); // ✅ theo roomId
    List<ChatMessage> findByRoomId(Long roomId); // <— thêm (dùng để gom id xóa reaction)
    long deleteByRoomId(Long roomId);
}
