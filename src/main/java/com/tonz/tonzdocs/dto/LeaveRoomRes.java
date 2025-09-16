package com.tonz.tonzdocs.dto;

public class LeaveRoomRes {
    public Long roomId;
    public boolean removed;      // đã xóa membership của tôi
    public long remaining;       // còn lại bao nhiêu member
    public boolean roomDeleted;  // đã xóa phòng vì remaining == 0


    public LeaveRoomRes(Long roomId, boolean removed, long remaining, boolean roomDeleted) {
        this.roomId = roomId;
        this.removed = removed;
        this.remaining = remaining;
        this.roomDeleted = roomDeleted;
    }
}
