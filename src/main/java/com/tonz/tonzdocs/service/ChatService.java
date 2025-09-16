package com.tonz.tonzdocs.service;

import com.tonz.tonzdocs.dto.*;
import com.tonz.tonzdocs.model.ChatMember;
import com.tonz.tonzdocs.model.ChatMessage;
import com.tonz.tonzdocs.model.ChatReaction;
import com.tonz.tonzdocs.model.ChatRoom;
import com.tonz.tonzdocs.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.*;
import org.springframework.messaging.simp.SimpMessagingTemplate;     // ✅ NEW
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class ChatService {

    private final ChatMessageRepo msgRepo;
    private final ChatReactionRepo reactionRepo;
    private final ChatRoomRepository roomRepo;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;      // ✅ NEW

    @Autowired(required = false)
    private ChatMemberRepository memberRepo;

    public ChatService(ChatMessageRepo msgRepo,
                       ChatReactionRepo reactionRepo,
                       ChatRoomRepository roomRepo,
                       UserRepository userRepository,
                       SimpMessagingTemplate messagingTemplate) {  // ✅ NEW
        this.msgRepo = msgRepo;
        this.reactionRepo = reactionRepo;
        this.roomRepo = roomRepo;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
    }

    /* ------------------------- ROOM APIs ------------------------- */

    public Page<ChatRoomRes> listRooms(boolean mine, String q, int page, int size, String email) {
        Pageable pageable = PageRequest.of(page, size);
        Page<ChatRoom> roomsPage;

        if (mine && memberRepo != null && email != null && !email.isBlank()) {
            roomsPage = memberRepo.roomsOfUser(email, safe(q), pageable);
        } else {
            roomsPage = roomRepo.search(safe(q), pageable);
        }

        List<ChatRoomRes> mapped = roomsPage.getContent().stream()
                .map(r -> new ChatRoomRes(
                        r.getId(),
                        r.getCode(),
                        r.getName(),
                        (memberRepo != null ? memberRepo.countByRoomId(r.getId()) : null),
                        r.getCreatedAt()
                ))
                .toList();

        return new PageImpl<>(mapped, pageable, roomsPage.getTotalElements());
    }

    public ChatRoomRes createRoom(String name, String code, String creatorEmail) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Room name is required");
        }
        String finalCode = (code == null || code.isBlank())
                ? randomCode()
                : code.trim().toUpperCase(Locale.ROOT);

        if (roomRepo.existsByCodeIgnoreCase(finalCode)) {
            throw new IllegalStateException("Room code already exists");
        }

        ChatRoom r = new ChatRoom();
        r.setName(name.trim());
        r.setCode(finalCode);
        r.setCreatedAt(Instant.now());
        roomRepo.save(r);

        if (memberRepo != null && creatorEmail != null && !creatorEmail.isBlank()) {
            Integer uid = userRepository.findByEmail(creatorEmail)
                    .map(u -> u.getUserId())
                    .orElse(null);

            if (uid != null) {
                ChatMember m = new ChatMember();
                m.setRoom(r);
                m.setUserEmail(creatorEmail);
                m.setUserId(uid);
                m.setJoinedAt(Instant.now());
                try {
                    memberRepo.save(m);
                } catch (DataIntegrityViolationException ignored) {}
            }
        }

        Long memberCount = (memberRepo != null ? memberRepo.countByRoomId(r.getId()) : null);
        return new ChatRoomRes(r.getId(), r.getCode(), r.getName(), memberCount, r.getCreatedAt());
    }

    /* ------------------------- MESSAGE APIs ------------------------- */

    private ChatRoom getRoomOrThrow(Long roomId) {
        return roomRepo.findById(roomId).orElseThrow(() -> new IllegalArgumentException("Room not found"));
    }

    public ChatMessageRes saveMessageByEmail(Long roomId, String senderEmail, String type, String content, Long parentId) {
        getRoomOrThrow(roomId);

        Integer senderId = userRepository.findByEmail(senderEmail)
                .map(u -> u.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found by email: " + senderEmail));

        ChatMessage m = new ChatMessage();
        m.roomId = roomId;
        m.senderId = senderId;         // cần cho cột sender_id NOT NULL
        m.senderEmail = senderEmail;
        m.type = (type != null ? type : "TEXT");
        m.content = (content != null ? content : "");
        m.parentId = parentId;
        msgRepo.save(m);

        ChatMessageRes r = new ChatMessageRes();
        r.id = m.id; r.roomId = m.roomId; r.senderEmail = m.senderEmail;
        r.type = m.type; r.content = m.content; r.parentId = m.parentId; r.createdAt = m.createdAt;

        // ✅ PHÁT NGAY SAU KHI LƯU (sau commit để tránh race-condition)
        publishAfterCommit("/topic/rooms/" + roomId, r);

        return r;
    }

    public Page<ChatMessageRes> history(Long roomId, int page, int size) {
        getRoomOrThrow(roomId);
        return msgRepo.findByRoomIdOrderByIdDesc(roomId, PageRequest.of(page, size))
                .map(m -> {
                    ChatMessageRes r = new ChatMessageRes();
                    r.id = m.id; r.roomId = m.roomId; r.senderEmail = m.senderEmail;
                    r.type = m.type; r.content = m.content; r.parentId = m.parentId; r.createdAt = m.createdAt;
                    return r;
                });
    }

    public ReactionEventRes toggleReaction(Long roomId, Long messageId, String senderEmail, String emoji) {
        getRoomOrThrow(roomId);
        var exists = reactionRepo.findByMessageIdAndSenderEmailAndEmoji(messageId, senderEmail, emoji);
        String action;
        if (exists.isPresent()) {
            reactionRepo.deleteById(exists.get().id);
            action = "REMOVED";
        } else {
            ChatReaction r = new ChatReaction();
            r.messageId = messageId;
            r.senderEmail = senderEmail;
            r.emoji = emoji;
            reactionRepo.save(r);
            action = "ADDED";
        }
        long total = reactionRepo.countByMessageIdAndEmoji(messageId, emoji);
        ReactionEventRes ev = new ReactionEventRes();
        ev.roomId = roomId; ev.messageId = messageId; ev.emoji = emoji; ev.total = total;
        ev.action = action; ev.senderEmail = senderEmail;

        // ✅ broadcast reaction
        publishAfterCommit("/topic/rooms/" + roomId + "/reactions", ev);

        return ev;
    }

    public Map<String, Long> reactionSummary(Long messageId) {
        return reactionRepo.findByMessageId(messageId).stream()
                .collect(Collectors.groupingBy(r -> r.emoji, Collectors.counting()));
    }

    /* ------------------------- helpers ------------------------- */

    private static String safe(String s) { return (s == null ? "" : s.trim()); }

    private static String randomCode() {
        String alphabet = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
        Random rd = new Random();
        StringBuilder sb = new StringBuilder(6);
        for (int i = 0; i < 6; i++) sb.append(alphabet.charAt(rd.nextInt(alphabet.length())));
        return sb.toString();
    }

    // ✅ Gửi sau khi transaction commit để client fetch history là đã có dữ liệu
    private void publishAfterCommit(String dest, Object payload) {
        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override public void afterCommit() {
                    messagingTemplate.convertAndSend(dest, payload);
                }
            });
        } else {
            messagingTemplate.convertAndSend(dest, payload);
        }
    }

    //  joinRoom
    private ChatRoomRes mapRoom(ChatRoom r) {
        Long cnt = (memberRepo != null ? memberRepo.countByRoomId(r.getId()) : null);
        return new ChatRoomRes(r.getId(), r.getCode(), r.getName(), cnt, r.getCreatedAt());
    }

    /** Join bằng roomId (id) */
    public ChatRoomRes joinRoom(Long roomId, String userEmail) {
        var room = getRoomOrThrow(roomId);

        if (memberRepo.existsByRoomIdAndUserEmail(roomId, userEmail)) {
            return mapRoom(room); // đã là member -> trả về luôn
        }

        Integer uid = userRepository.findByEmail(userEmail)
                .map(u -> u.getUserId())
                .orElse(null);

        ChatMember m = new ChatMember();
        m.setRoom(room);
        m.setUserEmail(userEmail);
        m.setUserId(uid);                 // nếu cột NOT NULL thì uid phải != null
        m.setJoinedAt(Instant.now());
        memberRepo.save(m);

        return mapRoom(room);
    }

    /** Join bằng code */
    public ChatRoomRes joinRoomByCode(String code, String userEmail) {
        String c = code == null ? "" : code.trim();
        var room = roomRepo.findByCodeIgnoreCase(c)
                .orElseThrow(() -> new IllegalArgumentException("Room code not found"));

        return joinRoom(room.getId(), userEmail);
    }

    public ChatRoomRes getRoomDetail(Long roomId) {
        ChatRoom r = getRoomOrThrow(roomId);
        Long memberCount = (memberRepo != null ? memberRepo.countByRoomId(r.getId()) : null);
        return new ChatRoomRes(
                r.getId(),
                r.getCode(),
                r.getName(),
                memberCount,
                r.getCreatedAt()
        );
    }

    /** Liệt kê thành viên phòng. Nếu chưa có bảng ChatMember -> trả trang rỗng. */
    public Page<ChatMemberRes> listMembers(Long roomId, int page, int size) {
        getRoomOrThrow(roomId);
        var pageable = PageRequest.of(page, size);

        if (memberRepo == null) {
            return Page.empty(pageable);
        }

        var membersPage = memberRepo.findByRoomId(roomId, pageable);

        return membersPage.map(m -> {
            Integer uid = null;
            String email = null;
            java.time.Instant joined = null;
            Long id = null;

            // tuỳ entity của bạn là field public hay getter, điều chỉnh cho khớp:
            try { id = (Long) m.getClass().getField("id").get(m); } catch (Exception ignore) {}
            try { uid = (Integer) m.getClass().getField("userId").get(m); } catch (Exception ignore) {}
            try { email = (String) m.getClass().getField("userEmail").get(m); } catch (Exception ignore) {}
            try { joined = (java.time.Instant) m.getClass().getField("joinedAt").get(m); } catch (Exception ignore) {}

            // nếu có getter thì ưu tiên dùng (tránh reflection)
            try { if (id == null) id = (Long) m.getClass().getMethod("getId").invoke(m); } catch (Exception ignore) {}
            try { if (uid == null) uid = (Integer) m.getClass().getMethod("getUserId").invoke(m); } catch (Exception ignore) {}
            try { if (email == null) email = (String) m.getClass().getMethod("getUserEmail").invoke(m); } catch (Exception ignore) {}
            try { if (joined == null) joined = (java.time.Instant) m.getClass().getMethod("getJoinedAt").invoke(m); } catch (Exception ignore) {}

            // enrich name/avatar từ bảng users (nếu có userId hoặc email)
            String name = null, avatar = null;
            if (uid != null) {
                var uOpt = userRepository.findById(uid);
                if (uOpt.isPresent()) {
                    var u = uOpt.get();
                    try { name = (String) u.getClass().getMethod("getName").invoke(u); } catch (Exception ignore) {}
                    try { avatar = (String) u.getClass().getMethod("getAvatarUrl").invoke(u); } catch (Exception ignore) {}
                    if (email == null) {
                        try { email = (String) u.getClass().getMethod("getEmail").invoke(u); } catch (Exception ignore) {}
                    }
                }
            } else if (email != null) {
                var uOpt = userRepository.findByEmail(email);
                if (uOpt.isPresent()) {
                    var u = uOpt.get();
                    try { name = (String) u.getClass().getMethod("getName").invoke(u); } catch (Exception ignore) {}
                    try { avatar = (String) u.getClass().getMethod("getAvatarUrl").invoke(u); } catch (Exception ignore) {}
                }
            }

            return new ChatMemberRes(id, uid, email, name, avatar, joined);
        });
    }

    /* ---------------- leaveRoom: xóa membership -> broadcast -> rỗng thì xoá phòng ---------------- */

    public LeaveRoomRes leaveRoom(Long roomId, String userEmail) {
        getRoomOrThrow(roomId);

        boolean removed = false;
        long remaining = -1L;
        boolean roomDeleted = false;

        if (memberRepo != null) {
            var memOpt = memberRepo.findByRoomIdAndUserEmail(roomId, userEmail);
            if (memOpt.isPresent()) {
                memberRepo.deleteById(memOpt.get().getId());
                removed = true;
            }

            // 🔔 bắn thông báo hệ thống ngay
            if (removed) {
                broadcastSystem(roomId, userEmail + " left the room");
            }

            remaining = memberRepo.countByRoomId(roomId);
            if (remaining == 0) {
                // Cho client biết phòng sắp bị xoá
                messagingTemplate.convertAndSend("/topic/rooms/" + roomId, Map.of(
                        "type", "ROOM_DELETED",
                        "roomId", roomId
                ));

                // Nếu KHÔNG dùng cascade ở DB, dọn thủ công theo thứ tự: reactions -> messages -> members -> room
                try {
                    var allMsgs = msgRepo.findByRoomId(roomId);
                    if (!allMsgs.isEmpty()) {
                        var ids = allMsgs.stream().map(m -> m.id).toList(); // hoặc getId()
                        reactionRepo.deleteByMessageIdIn(ids);
                    }
                    msgRepo.deleteByRoomId(roomId);
                    memberRepo.deleteByRoomId(roomId);
                } catch (Exception ignore) {}
                roomRepo.deleteById(roomId);
                roomDeleted = true;
            }
        } else {
            // Không có bảng member -> coi như không có gì để xoá
            removed = false;
            remaining = 0L; // hoặc -1 nếu bạn thích thể hiện "không xác định"
            // Không thể tự động xoá phòng theo số member trong trường hợp này
        }

        return new LeaveRoomRes(roomId, removed, remaining, roomDeleted);
    }

    private void broadcastSystem(Long roomId, String content) {
        var ev = Map.of(
                "type", "SYSTEM",
                "roomId", roomId,
                "content", content,
                "ts", java.time.Instant.now().toString()
        );
        messagingTemplate.convertAndSend("/topic/rooms/" + roomId, ev);
    }
}
