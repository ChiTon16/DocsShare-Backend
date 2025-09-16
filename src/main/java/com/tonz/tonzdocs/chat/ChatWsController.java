package com.tonz.tonzdocs.chat;

import com.tonz.tonzdocs.dto.ChatMessageRes;
import com.tonz.tonzdocs.dto.ReactionEventRes;
import com.tonz.tonzdocs.dto.ReactionToggleReq;
import com.tonz.tonzdocs.dto.SendMessageReq;
import com.tonz.tonzdocs.service.ChatService;
import org.springframework.messaging.handler.annotation.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class ChatWsController {

    private final SimpMessagingTemplate messagingTemplate;
    private final ChatService chatService;
    // 👇 CHÍNH LÀ 'broker' bị thiếu

    public ChatWsController(SimpMessagingTemplate messagingTemplate, ChatService chatService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
    }

    // FE publish -> /app/rooms/{roomId}/send
    @MessageMapping("/rooms/{roomId}/send")
    public void send(@DestinationVariable Long roomId,
                     @Payload SendMessageReq req,
                     Principal principal) {

        // principal.getName() = email (đã lấy từ JwtFilter / interceptor)
        ChatMessageRes saved = chatService.saveMessageByEmail(
                roomId,
                principal.getName(),
                (req.type != null ? req.type : "TEXT"),
                (req.content != null ? req.content : ""),
                req.parentId
        );
        // broadcast -> /topic/rooms/{roomId}
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, saved);
    }

    // EMOTE -> /app/rooms.{roomId}.react
    @MessageMapping("/rooms.{roomId}.react")
    public void react(@DestinationVariable Long roomId,
                      @Payload ReactionToggleReq req,
                      Principal principal) {
        if (principal == null || principal.getName() == null) throw new IllegalStateException("Unauthenticated");
        String email = principal.getName();

        ReactionEventRes ev = chatService.toggleReaction(roomId, req.messageId, email, req.emoji);
        messagingTemplate.convertAndSend("/topic/rooms." + roomId + ".reactions", ev);
    }
}
