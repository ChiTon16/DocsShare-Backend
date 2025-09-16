// src/main/java/com/tonz/tonzdocs/dto/ChatRoomRes.java
package com.tonz.tonzdocs.dto;

import java.time.Instant;

public record ChatRoomRes(
        Long id,
        String code,
        String name,
        Long memberCount,
        Instant createdAt
) {}
