package com.tonz.tonzdocs.dto;

public class ReactionEventRes {
    public Long roomId;       // ✅
    public Long messageId;
    public String emoji;
    public long total;
    public String action;     // ADDED | REMOVED
    public String senderEmail;
}