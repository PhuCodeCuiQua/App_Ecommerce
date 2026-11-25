package com.example.login_register;

import java.util.Date;

public class AppNotification {
    private final String id;
    private final String title;
    private final String message;
    private final String type;
    private final Date createdAt;
    private final boolean read;

    public AppNotification(String id, String title, String message, String type,
                           Date createdAt, boolean read) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.createdAt = createdAt;
        this.read = read;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getType() {
        return type;
    }

    public Date getCreatedAt() {
        return createdAt == null ? null : new Date(createdAt.getTime());
    }

    public boolean isRead() {
        return read;
    }
}
