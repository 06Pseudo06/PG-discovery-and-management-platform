package com.pgfinder.model;

import java.time.LocalDateTime;

public class ChatPreview {

    private int senderId;
    private String senderName;
    private String lastMessage;
    private LocalDateTime messageTime;
    private boolean unread;

    public ChatPreview() {
    }

    public ChatPreview(int senderId,
                       String senderName,
                       String lastMessage,
                       LocalDateTime messageTime,
                       boolean unread) {

        this.senderId = senderId;
        this.senderName = senderName;
        this.lastMessage = lastMessage;
        this.messageTime = messageTime;
        this.unread = unread;
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public LocalDateTime getMessageTime() {
        return messageTime;
    }

    public void setMessageTime(LocalDateTime messageTime) {
        this.messageTime = messageTime;
    }

    public boolean isUnread() {
        return unread;
    }

    public void setUnread(boolean unread) {
        this.unread = unread;
    }
} 