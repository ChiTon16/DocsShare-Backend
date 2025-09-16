package com.tonz.tonzdocs.dto;

import java.time.Instant;

public class ChatMemberRes {
    public Long id;
    public Integer userId;
    public String userEmail;
    public String userName;    // lấy từ bảng users nếu có
    public String avatarUrl;   // lấy từ bảng users nếu có
    public Instant joinedAt;

    public ChatMemberRes() {}

    public ChatMemberRes(Long id, Integer userId, String userEmail, String userName, String avatarUrl, Instant joinedAt) {
        this.id = id;
        this.userId = userId;
        this.userEmail = userEmail;
        this.userName = userName;
        this.avatarUrl = avatarUrl;
        this.joinedAt = joinedAt;
    }
}
