package com.smart.chat.service.model;

import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

@Builder
@Getter
@ToString
public class PrivateMessage {
    private String username;
    private String body;
    private String destinationUsername;
}
