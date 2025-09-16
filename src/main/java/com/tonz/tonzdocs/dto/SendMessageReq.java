package com.tonz.tonzdocs.dto;

public class SendMessageReq {
    public String type;     // "TEXT"...
    public String content;
    public Long parentId;   // optional
}