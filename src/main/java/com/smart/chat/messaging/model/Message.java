package com.smart.chat.messaging.model;

import lombok.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class Message {
    private String username;
    private String roomId;
    private String directMessageDestination;
    private String body;
    private Long timestamp;
}
