package com.tonz.tonzdocs.repository;

import com.tonz.tonzdocs.model.ChatReaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ChatReactionRepo extends JpaRepository<ChatReaction, Long> {
    Optional<ChatReaction> findByMessageIdAndSenderEmailAndEmoji(Long messageId, String senderEmail, String emoji);
    long countByMessageIdAndEmoji(Long messageId, String emoji);
    List<ChatReaction> findByMessageId(Long messageId);

    void deleteByMessageIdIn(List<Long> ids);
}
