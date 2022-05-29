package com.smart.chat.api.model;

public enum ChatError {
    ROOM_ALREADY_EXISTS("Room name already exists"),
    ROOM_NOT_FOUND("Room does not exist");

    private final String description;

    ChatError(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}
