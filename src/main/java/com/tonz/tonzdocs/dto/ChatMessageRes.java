package com.tonz.tonzdocs.dto;

import java.time.Instant;

public class ChatMessageRes {
    public Long id;
    public Long roomId;          // ✅ trả roomId
    public String senderEmail;
    public String type;
    public String content;
    public Long parentId;
    public Instant createdAt;
}