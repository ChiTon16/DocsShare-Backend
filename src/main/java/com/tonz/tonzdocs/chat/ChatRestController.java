package com.tonz.tonzdocs.chat;

import com.tonz.tonzdocs.dto.*;
import com.tonz.tonzdocs.service.ChatService;
import lombok.Data;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    private final ChatService chatService;

    public ChatRestController(ChatService chatService) {
        this.chatService = chatService;
    }

    /* ===================== ROOMS ===================== */

    /** List rooms (all hoặc 'mine' nếu có principal & bảng member) */
    @GetMapping("/rooms")
    public Page<ChatRoomRes> listRooms(
            @RequestParam(defaultValue = "false") boolean mine,
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size,
            Principal principal
    ) {
        String email = (mine ? principalEmail(principal) : null);
        return chatService.listRooms(mine, q, page, size, email);
    }

    /** Tạo room mới (code optional). Tự join creator nếu có bảng member */
    @PostMapping("/rooms")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatRoomRes createRoom(@RequestBody CreateRoomReq req, Principal principal) {
        String creatorEmail = principalEmail(principal);
        return chatService.createRoom(req.name, req.code, creatorEmail);
    }

    /* ===================== MESSAGES ===================== */

    @GetMapping("/rooms/{roomId}/history")
    public Page<ChatMessageRes> history(@PathVariable Long roomId,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "30") int size) {
        return chatService.history(roomId, page, size);
    }

    @PostMapping("/rooms/{roomId}/messages")
    @ResponseStatus(HttpStatus.CREATED)
    public ChatMessageRes sendMessage(@PathVariable Long roomId,
                                      @RequestBody SendMessageReq req,
                                      Principal principal) {
        String email = principalEmail(principal);
        return chatService.saveMessageByEmail(
                roomId,
                email,
                (req.type != null ? req.type : "TEXT"),
                (req.content != null ? req.content : ""),
                req.parentId
        );
    }

    /* ===================== REACTIONS ===================== */

    @PostMapping("/rooms/{roomId}/messages/{messageId}/reactions")
    public ReactionEventRes toggleReaction(@PathVariable Long roomId,
                                           @PathVariable Long messageId,
                                           @RequestBody ReactionToggleReq body,
                                           Principal principal) {
        String email = principalEmail(principal);
        String emoji = (body.emoji != null ? body.emoji : "👍");
        return chatService.toggleReaction(roomId, messageId, email, emoji);
    }

    @GetMapping("/rooms/{roomId}/messages/{messageId}/reactions")
    public Map<String, Long> reactionSummary(@PathVariable Long roomId, @PathVariable Long messageId) {
        return chatService.reactionSummary(messageId);
    }

    /* ===================== Join Room ===================== */
    /** Join bằng roomId */
    @PostMapping("/rooms/{roomId}/join")
    @ResponseStatus(HttpStatus.OK)
    public ChatRoomRes joinById(@PathVariable Long roomId, Principal principal) {
        if (principal == null || principal.getName() == null)
            throw new IllegalStateException("Unauthenticated");
        return chatService.joinRoom(roomId, principal.getName());
    }

    /** Join bằng code (body: { "code": "ABC123" }) */
    @PostMapping("/rooms/join-by-code")
    @ResponseStatus(HttpStatus.OK)
    public ChatRoomRes joinByCode(@RequestBody JoinCodeReq req, Principal principal) {
        if (principal == null || principal.getName() == null)
            throw new IllegalStateException("Unauthenticated");
        if (req.code == null || req.code.isBlank())
            throw new IllegalArgumentException("Code is required");
        return chatService.joinRoomByCode(req.code, principal.getName());
    }

    @Data
    public static class JoinCodeReq {
        public String code;
    }

    @GetMapping("/rooms/{roomId}")
    public ChatRoomRes roomDetail(@PathVariable Long roomId) {
        return chatService.getRoomDetail(roomId);
    }

    @GetMapping("/rooms/{roomId}/members")
    public Page<ChatMemberRes> roomMembers(@PathVariable Long roomId,
                                           @RequestParam(defaultValue = "0") int page,
                                           @RequestParam(defaultValue = "50") int size) {
        return chatService.listMembers(roomId, page, size);
    }

    /* ===================== helpers ===================== */

    private static String principalEmail(Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        return principal.getName();
    }

    /** Body cho POST /rooms */
    public static class CreateRoomReq {
        public String name;
        public String code; // optional
    }

    @DeleteMapping("/rooms/{roomId}/members/me")
    public Map<String, Object> leaveMyself(@PathVariable Long roomId, Principal principal) {
        if (principal == null || principal.getName() == null) {
            throw new IllegalStateException("Unauthenticated");
        }
        var res = chatService.leaveRoom(roomId, principal.getName());
        return Map.of(
                "roomId", res.roomId,
                "removed", res.removed,
                "remaining", res.remaining,
                "roomDeleted", res.roomDeleted
        );
    }
}
